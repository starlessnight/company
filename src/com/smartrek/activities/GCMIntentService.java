package com.smartrek.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.smartrek.utils.Preferences;

public class GCMIntentService extends GCMBaseIntentService {

	public static final String GCM_SENDER_ID = "968174328058";
	public static final String LOG_TAG = "GCMIntentService";

	public GCMIntentService() {
		super(GCM_SENDER_ID);
		Log.i(LOG_TAG, "GCMIntentService constructor called");
	}

	@Override
	protected void onError(Context arg0, String errorId) {
		Log.i(LOG_TAG, "GCMIntentService onError called: " + errorId);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i(LOG_TAG, "GCMIntentService onMessage called");
		Log.i(LOG_TAG, "Origin: " + intent.getStringExtra("origin"));
		Log.i(LOG_TAG, "Destination: " + intent.getStringExtra("destination"));
		Log.i(LOG_TAG, "Time: " + intent.getStringExtra("time"));
		Log.i(LOG_TAG, "Message: " + intent.getStringExtra("message"));
		
		String origin = intent.getStringExtra("origin");
		String destination = intent.getStringExtra("destination");
		String message = intent.getStringExtra("message");
		long departureTime = intent.getLongExtra("time", 0) * 1000;
		
		if (message == null || message.equals("")) {
			message = "(placeholder)";
		}
		
		Intent routeIntent = new Intent(context, RouteActivity.class);
        routeIntent.putExtra(RouteActivity.ORIGIN_ADDR, origin);
        routeIntent.putExtra(RouteActivity.DEST_ADDR, destination);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, routeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Notification notification = new Notification(R.drawable.icon_small, "Smartrek", departureTime);
        notification.setLatestEventInfo(context, "Smartrek", message, pendingIntent);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(LOG_TAG, "GCMIntentService onRegistered called");
		Log.i(LOG_TAG, "Registration id is: " + registrationId);
		
		SharedPreferences prefs = Preferences.getGlobalPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putString("GCMRegistrationID", registrationId);
		editor.commit();
	}

	@Override
	protected void onUnregistered(Context arg0, String registrationId) {
		Log.i(LOG_TAG, "GCMIntentService onUnregistered called");
		Log.i(LOG_TAG, "Registration id is: " + registrationId);
	}
}