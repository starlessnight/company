package com.metropia.receivers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.metropia.CalendarService;
import com.metropia.activities.LandingActivity2;
import com.metropia.activities.MapDisplayActivity;
import com.metropia.activities.RouteActivity;
import com.metropia.ui.NavigationView;
import com.metropia.utils.Misc;
import com.metropia.utils.RouteNode;
import com.metropia.utils.CalendarContract.Instances;
import com.metropia.activities.R;

public final class CalendarNotification extends BroadcastReceiver {
	
	public static final String LOG_TAG = "CalendarNotification";
	
	public static final String EVENT_ID = "eventId";
	
	private static final long THIRTY_MINS = 30 * 60 * 1000;
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onReceive(Context context, Intent intent) {
	    Log.d(LOG_TAG, "onReceive");
	    
	    int eventId = intent.getIntExtra(EVENT_ID, 0);
	    JSONObject event = CalendarService.getEvent(context, eventId);
	    int geoSize = event.optInt(CalendarService.GEOCODING_SIZE, 0);
	    double lat = event.optDouble(CalendarService.LAT, 0);
	    double lon = event.optDouble(CalendarService.LON, 0);
        if(event != null && MapDisplayActivity.isCalendarIntegrationEnabled(context) && isNotNear(context, lat, lon)){
            long startTime = event.optLong(Instances.BEGIN);
            
            String notiInfo = "Title: " + event.optString(Instances.TITLE)
                    + "\nTime: " + new SimpleDateFormat("h:mm a").format(new Date(startTime));
            String location = event.optString(Instances.EVENT_LOCATION);
            if(StringUtils.isNotBlank(location)){
                notiInfo += "\nLocation: " + location;
            }
            long expiryTime = startTime - THIRTY_MINS;
            if(System.currentTimeMillis() < expiryTime /* || true */){
                Intent nextIntent = geoSize > 1 ? new Intent(context, LandingActivity2.class) : new Intent(context, RouteActivity.class);
                nextIntent.putExtra(RouteActivity.EVENT_ID, eventId);
                PendingIntent sender = PendingIntent.getActivity(context, eventId, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Intent delay = new Intent(context, CalendarNotificationDelay.class);
                delay.putExtra(CalendarNotificationDelay.EVENT_ID, eventId);
                PendingIntent pendingDelay = PendingIntent.getBroadcast(context, eventId, delay, 
                    PendingIntent.FLAG_UPDATE_CURRENT);
                Intent expiry = new Intent(context, NotificationExpiry.class);
                expiry.putExtra(NotificationExpiry.NOTIFICATION_ID, eventId);
                PendingIntent pendingExpiry = PendingIntent.getBroadcast(context, eventId, 
                    expiry, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification notification;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                    notification = new Notification.BigTextStyle(
                            new Notification.Builder(context)
                               .setContentTitle("Metropia")
                               .setContentText(notiInfo)
                               .setContentIntent(sender)
                               .setWhen(startTime)
                               .addAction(0, "Plan the trip", sender)
                               .addAction(0, "Remind me later", pendingDelay)
                               .addAction(0, "Dismiss", pendingExpiry)
                               .setSmallIcon(R.drawable.icon_small)
                            )
                        .bigText(notiInfo)
                        .build();
                }else{
                    notification = new Notification(R.drawable.icon_small, "Metropia", startTime);
                    notification.setLatestEventInfo(context, "Metropia", notiInfo, sender);
                }
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(eventId, notification);
                
                Misc.playDefaultNotificationSound(context);
                Misc.wakeUpScreen(context, CalendarNotification.class.getSimpleName());
                
                AlarmManager expiryMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                expiryMgr.set(AlarmManager.RTC, expiryTime, pendingExpiry);
            }
        }
	}
	
	private static boolean isNotNear(Context ctx, double lat, double lon) {
        LocationInfo loc = new LocationInfo(ctx);
        double distance = NavigationView.metersToFeet(RouteNode.distanceBetween(
            loc.lastLat, loc.lastLong, lat, lon));
        return distance > 500;
    }

}
