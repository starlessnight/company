package com.smartrek.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMConstants;
import com.smartrek.utils.Misc;
import com.smartrek.utils.Preferences;

public class GCMIntentService extends GCMBaseIntentService {

	public static final String GCM_SENDER_ID = "968174328058";
	public static final String LOG_TAG = "GCMIntentService";

	public GCMIntentService() {
		super(GCM_SENDER_ID);
		Log.i(LOG_TAG, "GCMIntentService constructor called");
	}

	@Override
	protected void onError(Context ctx, String errorId) {
		Log.i(LOG_TAG, "GCMIntentService onError called: " + errorId);
		if(GCMConstants.ERROR_ACCOUNT_MISSING.equals(errorId)){
		    Misc.setAddGoogleAccount(ctx, true);
		}
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
		double lat = Double.parseDouble(intent.getStringExtra("lat"));
        double lon = Double.parseDouble(intent.getStringExtra("lon"));
        String eta = intent.getStringExtra("eta");
        String mile = intent.getStringExtra("mile");
		
		long departureTime = intent.getLongExtra("time", 0) * 1000;
		
		Intent routeIntent = new Intent(context, RouteActivity.class);
        routeIntent.putExtra(RouteActivity.ORIGIN_ADDR, origin);
        routeIntent.putExtra(RouteActivity.DEST_ADDR, destination);
        
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Notification notification = new Notification(R.drawable.icon_small, "Smartrek", departureTime);
        PendingIntent pendingIntent;
        if(eta != null){
            String msg = message + "\n" + mile + " miles to go estimated arrival time: " + eta;
            Intent landingIntent = new Intent(context, LandingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            landingIntent.putExtra(LandingActivity.LAT, lat);
            landingIntent.putExtra(LandingActivity.LON, lon);
            landingIntent.putExtra(LandingActivity.MSG, msg);
            pendingIntent = PendingIntent.getActivity(context, 0, landingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }else{
            pendingIntent = PendingIntent.getActivity(context, 0, routeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        notification.setLatestEventInfo(context, "Smartrek", message, pendingIntent);
        
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
        
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        if(!pm.isScreenOn()){
           WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE, 
               GCMIntentService.class.getSimpleName() + "Lock");
           wl.acquire(10000);
           WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
               GCMIntentService.class.getSimpleName() + "CpuLock");
           wl_cpu.acquire(10000);
        }
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(LOG_TAG, "GCMIntentService onRegistered called");
		Log.i(LOG_TAG, "Registration id is: " + registrationId);
		
		Misc.setAddGoogleAccount(context, false);
		
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