package com.smartrek;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.util.Log;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.smartrek.activities.MainActivity;
import com.smartrek.activities.MapDisplayActivity;
import com.smartrek.models.User;
import com.smartrek.receivers.CalendarNotification;
import com.smartrek.utils.CalendarContract.Instances;
import com.smartrek.utils.Geocoding;
import com.smartrek.utils.Geocoding.Address;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.ValidationParameters;

public class CalendarService extends IntentService {

    public static final String LAT = "lat";
    
    public static final String LON = "lon";
    
    private static final long FIFTHTEEN_MINS = 15 * 60 * 1000/*10000*/;
    
    private static final long FOUR_HOURS = 4 * 60 * 60 * 1000;
    
    private static final long TWO_AND_A_HALF_HOURS = Double.valueOf(2.5 * 60 * 60 * 1000).longValue();
    
    private static final long ONE_DAY = 24 * 60 * 60 * 1000;

    public CalendarService() {
        super(CalendarService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("CalendarService", "onHandleIntent");
        User user = User.getCurrentUserWithoutCache(CalendarService.this);
        if (user != null && MapDisplayActivity.isCalendarIntegrationEnabled(CalendarService.this)) {
            MainActivity.initApiLinksIfNecessary(this, new Runnable() {
                @Override
                public void run() {
                    try {
                        long now = System.currentTimeMillis();
                        Uri.Builder eventsUriBuilder = Instances.CONTENT_URI.buildUpon();
                        ContentUris.appendId(eventsUriBuilder, now);
                        ContentUris.appendId(eventsUriBuilder, now + FOUR_HOURS);
                        Cursor events = getContentResolver().query(eventsUriBuilder.build(), new String[] { BaseColumns._ID, Instances.TITLE,
                            Instances.BEGIN, Instances.EVENT_LOCATION, Instances.END}, null, null, Instances.BEGIN + " asc");
                        boolean hasNotification = false;
                        while(events.moveToNext()) {
                           String eventId = events.getString(0);
                           File file = getFile(eventId);
                           long start = Long.parseLong(events.getString(2));
                           long end = Long.parseLong(events.getString(4));
                           String location = events.getString(3);
                           long notiTime = start - TWO_AND_A_HALF_HOURS;
                           String title = events.getString(1);
                           Address address = geocode(location);
                           if((!file.exists() || file.length() == 0) 
                                   && StringUtils.isNotBlank(location)
                                   && isAtLeastFourWords(location)
                                   && canBeGeocoded(address) 
                                   && !isDuplicate(CalendarService.this, eventId, title, start, end) 
                                   && System.currentTimeMillis() < notiTime/* true */
                                   && !isOnDestination(address)){
                               hasNotification = true;
                               Intent noti = new Intent(CalendarService.this, 
                                   CalendarNotification.class);
                               noti.putExtra(CalendarNotification.EVENT_ID, Integer.parseInt(eventId));
                               PendingIntent pendingNoti = PendingIntent.getBroadcast(
                                   CalendarService.this, Integer.parseInt(eventId), noti, 
                                   PendingIntent.FLAG_UPDATE_CURRENT);
                               AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                               am.set(AlarmManager.RTC_WAKEUP, notiTime /*System.currentTimeMillis() + 3000*/, pendingNoti);
                           }
                           JSONObject eventJson = new JSONObject()
                               .put(BaseColumns._ID, eventId)
                               .put(Instances.TITLE, title)
                               .put(Instances.BEGIN, start)
                               .put(Instances.END, end)
                               .put(Instances.EVENT_LOCATION, location)
                               .put(LAT, address == null?0:address.getLatitude())
                               .put(LON, address == null?0:address.getLongitude());
                           FileUtils.write(file, eventJson.toString());
                           if(hasNotification){
                               break;
                           }
                        }
                        events.close();
                    }catch (Throwable t) {
                        Log.w("CalendarService", Log.getStackTraceString(t));
                    }
                }
            });
        }
        File[] oFiles = getDir().listFiles();
        if(ArrayUtils.isNotEmpty(oFiles)){
            for (File f : oFiles) {
                if(f.lastModified() < System.currentTimeMillis() - ONE_DAY){
                    FileUtils.deleteQuietly(f);
                }
            }
        }
    }
    
    private static boolean isAtLeastFourWords(String address){
        return ArrayUtils.getLength(StringUtils.split(address)) >= 4;
    }
    
    private static boolean canBeGeocoded(Geocoding.Address address){
        return address != null && !address.getGeoPoint().isEmpty();
    }
    
    private boolean isOnDestination(Geocoding.Address address) {
    	LocationInfo loc = new LocationInfo(CalendarService.this);
    	ValidationParameters params = ValidationParameters.getInstance();
    	return RouteNode.distanceBetween(loc.lastLat, loc.lastLong, address.getLatitude(), address.getLongitude()) < params.getArrivalDistanceThreshold();
    }
    
    private Geocoding.Address geocode(String location){
        Geocoding.Address rs = null;
        try {
            LocationInfo loc = new LocationInfo(CalendarService.this);
            List<Geocoding.Address> addresses = Geocoding.lookup(this, location, 
                Float.valueOf(loc.lastLat).doubleValue(), Float.valueOf(loc.lastLong).doubleValue());
            rs = (addresses != null && !addresses.isEmpty())?addresses.get(0):null;
        }catch(Throwable t){}
        return rs;
    }
    
    private File getDir(){
        return getDir(this);
    }
    
    private static File getDir(Context ctx){
        return new File(ctx.getExternalFilesDir(null), "calendar");
    }
    
    private File getFile(String eventId){
        return getFile(this, eventId);
    }
    
    private static File getFile(Context ctx, String eventId){
        return new File(getDir(ctx), eventId);
    }
    
    public static JSONObject getEvent(Context ctx, int id){
        JSONObject event = null;
        File file = getFile(ctx, String.valueOf(id));
        if(file.exists() && file.length() != 0){
            try {
                event = new JSONObject(FileUtils.readFileToString(file));
            }
            catch (JSONException e) {
            }
            catch (IOException e) {
            }
        }
        return event;
    }
    
    private static boolean isDuplicate(Context ctx, String eventId, String title,
            long start, long end){
        boolean duplicate = false;
        for(JSONObject event : getEvents(ctx)){
            if(!eventId.equals(event.optString(BaseColumns._ID))){
                String eTitle = event.optString(Instances.TITLE);
                long eStart = event.optLong(Instances.BEGIN);
                long eEnd = event.optLong(Instances.END);
                if(duplicate = title.equals(eTitle) && start == eStart && end == eEnd){
                    break;
                }
            }
        }
        return duplicate;
    }
    
    private static List<JSONObject> getEvents(Context ctx){
        List<JSONObject> events = new ArrayList<JSONObject>();
        File[] files = getDir(ctx).listFiles();
        if(files != null){
            for(File file:files){
                if(file.length() != 0){
                    try {
                        JSONObject event = new JSONObject(FileUtils.readFileToString(file));
                        events.add(event);
                    }
                    catch (JSONException e) {
                    }
                    catch (IOException e) {
                    }
                }
            }
        }
        return events;
    }

    public static void schedule(Context ctx) {
        PendingIntent sendTrajServ = PendingIntent.getService(ctx, 0,
                new Intent(ctx, CalendarService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + FIFTHTEEN_MINS, FIFTHTEEN_MINS, sendTrajServ);
    }

}
