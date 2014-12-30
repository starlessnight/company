package com.metropia.receivers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public final class CalendarNotificationDelay extends BroadcastReceiver {
	
	public static final String LOG_TAG = "CalendarNotificationDelay";
	
	public static final String EVENT_ID = "eventId";
	
	private static final long TEN_MINS = 10 * 60 * 1000;
	
	@Override
	public void onReceive(Context context, Intent intent) {
	    Log.d(LOG_TAG, "onReceive");
		
	    int eventId = intent.getIntExtra(EVENT_ID, 0);
	    NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(eventId);
        
	    Intent noti = new Intent(context, CalendarNotification.class);
        noti.putExtra(CalendarNotification.EVENT_ID, String.valueOf(eventId));
        PendingIntent pendingNoti = PendingIntent.getBroadcast(context, eventId, noti, 
            PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TEN_MINS, pendingNoti);
	}

}
