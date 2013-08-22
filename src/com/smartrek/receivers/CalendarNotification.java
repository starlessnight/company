package com.smartrek.receivers;

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

import com.smartrek.CalendarService;
import com.smartrek.activities.MapDisplayActivity;
import com.smartrek.activities.R;
import com.smartrek.activities.RouteActivity;
import com.smartrek.utils.CalendarContract.Instances;

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
        if(event != null && MapDisplayActivity.isCalendarIntegrationEnabled(context)){
            long startTime = event.optLong(Instances.BEGIN);
            
            String notiInfo = "Title: " + event.optString(Instances.TITLE)
                    + "\nTime: " + new SimpleDateFormat("h:mm a").format(new Date(startTime));
            String location = event.optString(Instances.EVENT_LOCATION);
            if(StringUtils.isNotBlank(location)){
                notiInfo += "\nLocation: " + location;
            }
            long expiryTime = startTime - THIRTY_MINS;
            if(System.currentTimeMillis() < expiryTime /* || true */){
                Intent routeIntent = new Intent(context, RouteActivity.class);
                routeIntent.putExtra(RouteActivity.EVENT_ID, eventId);
                PendingIntent sender = PendingIntent.getActivity(context, eventId, routeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
                               .setContentTitle("Smartrek")
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
                    notification = new Notification(R.drawable.icon_small, "Smartrek", startTime);
                    notification.setLatestEventInfo(context, "Smartrek", notiInfo, sender);
                }
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(eventId, notification);
                
                AlarmManager expiryMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                expiryMgr.set(AlarmManager.RTC, expiryTime, pendingExpiry);
            }
        }
	}

}
