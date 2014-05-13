package com.smartrek.activities;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMConstants;
import com.smartrek.utils.Misc;
import com.smartrek.utils.Preferences;

public class GCMIntentService extends GCMBaseIntentService {

	public static final String GCM_SENDER_ID = "5407850735";
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
	    try {
    		Log.i(LOG_TAG, "GCMIntentService onMessage called");
    		String type = intent.getStringExtra("type");
    		if("pretrip".equalsIgnoreCase(type)){
    		    String msg = intent.getStringExtra("message");
                Intent alertIntent = new Intent(context, PreTripAlertActivity.class);
                String rUrl = intent.getStringExtra("reservation");
                alertIntent.putExtra(PreTripAlertActivity.URL, rUrl);
                alertIntent.putExtra(PreTripAlertActivity.MSG, msg);
                alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(alertIntent);
            }else{ 
                Log.i(LOG_TAG, "Origin: " + intent.getStringExtra("origin"));
                Log.i(LOG_TAG, "Destination: " + intent.getStringExtra("destination"));
                Log.i(LOG_TAG, "Time: " + intent.getStringExtra("time"));
                Log.i(LOG_TAG, "Message: " + intent.getStringExtra("message"));
                
                String origin = intent.getStringExtra("origin");
                String destination = intent.getStringExtra("destination");
                String message = intent.getStringExtra("message");
                
        		long departureTime = intent.getLongExtra("time", 0) * 1000;
        		if(departureTime == 0){
        		    departureTime = System.currentTimeMillis();
        		}
        		
        		Intent routeIntent = new Intent(context, RouteActivity.class);
                routeIntent.putExtra(RouteActivity.ORIGIN_ADDR, origin);
                routeIntent.putExtra(RouteActivity.DEST_ADDR, destination);
                
        		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                
                Notification notification = new Notification(R.drawable.icon_small, "Metropia", departureTime);
                PendingIntent pendingIntent;
                
                String body = intent.getStringExtra("body");
                if(body != null){
                    body += "\n" + new SimpleDateFormat("MMM d, h:m a").format(new Date());
                    double lat = Double.parseDouble(intent.getStringExtra("lat"));
                    double lon = Double.parseDouble(intent.getStringExtra("lon"));
                    Intent landingIntent = new Intent(context, RouteActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    landingIntent.putExtra(RouteActivity.LAT, lat);
                    landingIntent.putExtra(RouteActivity.LON, lon);
                    landingIntent.putExtra(RouteActivity.MSG, body);
                    pendingIntent = PendingIntent.getActivity(context, 0, landingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Intent onTheWayIntent = new Intent(LandingActivity2.ON_THE_WAY_NOTICE);
                    onTheWayIntent.putExtra(LandingActivity2.LAT, lat);
                    onTheWayIntent.putExtra(LandingActivity2.LON, lon);
                    onTheWayIntent.putExtra(LandingActivity2.MSG, body);
                    sendBroadcast(onTheWayIntent);
                }else{
                    pendingIntent = PendingIntent.getActivity(context, 0, routeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                }
                notification.setLatestEventInfo(context, "Metropia", message, pendingIntent);
                
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                notificationManager.notify(0, notification);
            }
            
            Misc.playDefaultNotificationSound(context);
            Misc.wakeUpScreen(context, GCMIntentService.class.getSimpleName());
	    }
        catch (Throwable t) {
            Log.d("GCMIntentService", Log.getStackTraceString(t));
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