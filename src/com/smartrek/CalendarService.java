package com.smartrek;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
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

import com.smartrek.models.User;
import com.smartrek.receivers.CalendarNotification;
import com.smartrek.utils.CalendarContract.Instances;

public class CalendarService extends IntentService {

    private static final long FIFTHTEEN_MINS = 15 * 60 * 1000 /* 10000 */;
    
    private static final long FOUR_HOURS = 4 * 60 * 60 * 1000;
    
    private static final long TWO_AND_A_HALF_HOURS = Double.valueOf(2.5 * 60 * 60 * 1000).longValue();
    
    private static final long ONE_DAY = 24 * 60 * 60 * 1000;

    public CalendarService() {
        super(CalendarService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("CalendarService", "onHandleIntent");
        User user = User.getCurrentUser(this);
        if (user != null) {
            try {
                long now = System.currentTimeMillis();
                Uri.Builder eventsUriBuilder = Instances.CONTENT_URI.buildUpon();
                ContentUris.appendId(eventsUriBuilder, now);
                ContentUris.appendId(eventsUriBuilder, now + FOUR_HOURS);
                Cursor events = getContentResolver().query(eventsUriBuilder.build(), new String[] { BaseColumns._ID, Instances.TITLE,
                    Instances.BEGIN, Instances.EVENT_LOCATION}, null, null, Instances.BEGIN + " asc");
                while(events.moveToNext()) {
                   String eventId = events.getString(0);
                   File file = getFile(eventId);
                   long start = Long.parseLong(events.getString(2));
                   long notiTime = start - TWO_AND_A_HALF_HOURS;
                   if((!file.exists() || file.length() == 0) && System.currentTimeMillis() < notiTime /* true */){
                       JSONObject eventJson = new JSONObject()
                           .put(BaseColumns._ID, eventId)
                           .put(Instances.TITLE, events.getString(1))
                           .put(Instances.BEGIN, start)
                           .put(Instances.EVENT_LOCATION, events.getString(3));
                       FileUtils.write(file, eventJson.toString());
                       Intent noti = new Intent(CalendarService.this, 
                           CalendarNotification.class);
                       noti.putExtra(CalendarNotification.EVENT_ID, Integer.parseInt(eventId));
                       PendingIntent pendingNoti = PendingIntent.getBroadcast(
                           CalendarService.this, Integer.parseInt(eventId), noti, 
                           PendingIntent.FLAG_UPDATE_CURRENT);
                       AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                       am.set(AlarmManager.RTC_WAKEUP, notiTime /* System.currentTimeMillis() */, pendingNoti);
                       break;
                   }
                }
                events.close();
            }catch (Throwable t) {
                Log.w("CalendarService", Log.getStackTraceString(t));
            }
            /*
            GoogleAccountManager gam = new GoogleAccountManager(this);
            for (Account account : gam.getAccounts()) {
                GoogleAccountCredential cred = GoogleAccountCredential
                        .usingOAuth2(this, CalendarScopes.CALENDAR_READONLY);
                cred.setSelectedAccountName(account.name);
                final Calendar client = new Calendar.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(), cred).setApplicationName(
                        getString(R.string.app_name)).build();
                try {
                    List<CalendarListEntry> cals = client.calendarList().list()
                            .execute().getItems();
                    if(cals != null){
                        BatchRequest batch = client.batch();
                        for (CalendarListEntry c : cals) {
                            long nowMillis = System.currentTimeMillis();
                            client.events().list(c.getId())
                                .setTimeMin(new DateTime(nowMillis))
                                .setTimeMax(new DateTime(nowMillis + FOUR_HOURS))
                                .queue(batch, new JsonBatchCallback<Events>() {
                                    @Override
                                    public void onSuccess(Events rs,
                                            HttpHeaders h)
                                            throws IOException {
                                        List<Event> events = rs.getItems();
                                        if(events != null){
                                            for(Event e:events){
                                                String eventId = e.getId();
                                                File file = getFile(eventId);
                                                if(true || !file.exists() || file.length() == 0){
                                                    FileUtils.write(file, e.toString());
                                                    Intent noti = new Intent(CalendarService.this, 
                                                        CalendarNotification.class);
                                                    noti.putExtra(CalendarNotification.EVENT_ID, eventId);
                                                    PendingIntent pendingNoti = PendingIntent.getBroadcast(
                                                        CalendarService.this, 0, noti, 
                                                        PendingIntent.FLAG_UPDATE_CURRENT);
                                                    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                                                    am.set(AlarmManager.RTC_WAKEUP, Math.max(
                                                        getTime(e) - TWO_HOURS, System.currentTimeMillis()), pendingNoti);
                                                }
                                            }
                                        }
                                    }
                                    @Override
                                    public void onFailure(GoogleJsonError e,
                                            HttpHeaders h)
                                            throws IOException {
                                    }
                                });
                        }
                        batch.execute();
                    }
                }
                catch (GooglePlayServicesAvailabilityIOException availabilityException) {
                }
                catch (UserRecoverableAuthIOException userRecoverableException) {
                }
                catch (Throwable t) {
                    Log.w("CalendarService", Log.getStackTraceString(t));
                }
            }
            */
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

    public static void schedule(Context ctx) {
        PendingIntent sendTrajServ = PendingIntent.getService(ctx, 0,
                new Intent(ctx, CalendarService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(), FIFTHTEEN_MINS, sendTrajServ);
    }

}
