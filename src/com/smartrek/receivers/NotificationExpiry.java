package com.smartrek.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class NotificationExpiry extends BroadcastReceiver {
	
	public static final String LOG_TAG = "NotificationExpiry";
	
	public static final String NOTIFICATION_ID = "notificationId";
	
	@Override
	public void onReceive(Context context, Intent intent) {
	    Log.d(LOG_TAG, "onReceive");
	    NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    nMgr.cancel(intent.getIntExtra(NOTIFICATION_ID, 0));
	}

}
