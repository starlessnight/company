package com.metropia;

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
import com.metropia.activities.MainActivity;
import com.metropia.activities.MapDisplayActivity;
import com.metropia.models.User;
import com.metropia.receivers.CalendarNotification;
import com.metropia.utils.Geocoding;
import com.metropia.utils.RouteNode;
import com.metropia.utils.ValidationParameters;
import com.metropia.utils.CalendarContract.Instances;
import com.metropia.utils.Geocoding.Address;

public class CalendarService extends IntentService {

    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String GEOCODING_SIZE = "geoSize";
    
    private static final long FIFTHTEEN_MINS = 15 * 60 * 1000/*10000*/;
    
    private static final long FOUR_HOURS = 4 * 60 * 60 * 1000;
    
    private static final long ONE_HOURS = Double.valueOf(60 * 60 * 1000).longValue();
    
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
                            Instances.BEGIN, Instances.EVENT_LOCATION, Instances.END, Instances.ALL_DAY}, null, null, Instances.BEGIN + " asc");
                        boolean hasNotification = false;
                        while(events.moveToNext()) {
                           String eventId = events.getString(0);
                           File file = getFile(eventId);
                           long start = Long.parseLong(events.getString(2));
                           long end = Long.parseLong(events.getString(4));
                           String location = events.getString(3);
                           Integer allDayEvent = events.getInt(5); // all day event : 1 , not all day event : 0
                           long notiTime = start - ONE_HOURS;
                           String title = events.getString(1);
                           List<Address> addresses = geocode(location);
                           if((!file.exists() || file.length() == 0) 
                        		   && allDayEvent.equals(0) // skip all day event
                                   && StringUtils.isNotBlank(location)
                                   && isAtLeastThreeWords(location)
                                   && canBeGeocoded(addresses) 
                                   && !isDuplicate(CalendarService.this, eventId, title, start, end) 
                                   && System.currentTimeMillis() < notiTime/* true */){
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
                               .put(Instances.ALL_DAY, allDayEvent)
                               .put(GEOCODING_SIZE, addresses.size())
                               .put(LAT, addresses.isEmpty()?0:addresses.get(0).getLatitude())
                               .put(LON, addresses.isEmpty()?0:addresses.get(0).getLongitude());
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
    
    private static boolean isAtLeastThreeWords(String address){
        return ArrayUtils.getLength(StringUtils.split(address)) >= 3;
    }
    
    private static boolean canBeGeocoded(List<Geocoding.Address> addresses){
        return addresses != null && !addresses.isEmpty();
    }
    
    private List<Geocoding.Address> geocode(String location){
        try {
            LocationInfo loc = new LocationInfo(CalendarService.this);
            return Geocoding.searchPoiForCalendar(this, location, 
                Float.valueOf(loc.lastLat).doubleValue(), Float.valueOf(loc.lastLong).doubleValue());
        }catch(Throwable t){
        	return new ArrayList<Geocoding.Address>();
        }
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
