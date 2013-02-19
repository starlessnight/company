package com.smartrek.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class ReservationNotificationExpiry extends BroadcastReceiver {
	
	public static final String LOG_TAG = "ReservationNotificationExpiry";
	
	public static final int NOTIFICATION_ID = 0;

	public static final int REQUEST_CODE = 0; 
	
	@Override
	public void onReceive(Context context, Intent intent) {
	    Log.d(LOG_TAG, "onReceive");
	    NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    nMgr.cancel(NOTIFICATION_ID);
	}

}
