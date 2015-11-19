package com.metropia.activities;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMConstants;
import com.localytics.android.Localytics;
import com.metropia.LocalyticsUtils;
import com.metropia.receivers.NotificationExpiry;
import com.metropia.utils.Misc;
import com.metropia.utils.Preferences;

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

	
	private static Integer ID = 123451;
	@SuppressLint("NewApi")
	@Override
	protected void onMessage(Context context, Intent intent) {
	    try {
    		Log.i(LOG_TAG, "GCMIntentService onMessage called");
    		String type = intent.getStringExtra("type");
    		Localytics.integrate(this);
    		LocalyticsUtils.tagAppStartFromPush();
    		if("pretrip".equalsIgnoreCase(type)){
    		    String msg = intent.getStringExtra("message");
                Intent alertIntent = new Intent(context, PreTripAlertActivity.class);
                String rUrl = intent.getStringExtra("reservation");
                alertIntent.putExtra(PreTripAlertActivity.URL, rUrl);
                alertIntent.putExtra(PreTripAlertActivity.MSG, msg);
                alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(alertIntent);
            }
    		else {
    			String message = intent.getStringExtra("message");
    			
    			Intent intentMain = new Intent(this, MainActivity.class);
    			intentMain.setAction(Intent.ACTION_MAIN);
    			intentMain.addCategory(Intent.CATEGORY_LAUNCHER);
    	        PendingIntent sender = PendingIntent.getActivity(this, ID, intentMain, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification notification = new Notification(R.drawable.icon_small, "Metropia", System.currentTimeMillis());
                
                
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                	Intent dismistIntent = new Intent(context, NotificationExpiry.class);
                    dismistIntent.putExtra(NotificationExpiry.NOTIFICATION_ID, ID);
                    PendingIntent pendingExpiry = PendingIntent.getBroadcast(context, ID, dismistIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                	
                    notification = new Notification.BigTextStyle(
                            new Notification.Builder(context)
                               .setContentTitle("Metropia")
                               .setContentText(message)
                               .setContentIntent(sender)
                               .setWhen(System.currentTimeMillis())
                               .addAction(0, "Dismiss", pendingExpiry)
                               .setSmallIcon(R.drawable.icon_small)
                            )
                        .bigText(message)
                        .build();
                }
                

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notification.setLatestEventInfo(this, "Metropia", message, sender);
                notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;            
                notificationManager.notify(ID++, notification);
    			
    			
    			
    			/*PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
    			NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
    			builder.setSmallIcon(R.drawable.icon_small).setContentTitle("Metropia").setContentText(message);
    			builder.setContentIntent(pendingIntent);
    			
    			
    			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0, builder.build());*/
    		}
    		
    		/*
    		 * deprecated on_my_way notification
    		else{
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
            }*/
            
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
		
		editor.putString(Preferences.Global.GCM_REG_ID, registrationId);
		editor.commit();
	}

	@Override
	protected void onUnregistered(Context arg0, String registrationId) {
		Log.i(LOG_TAG, "GCMIntentService onUnregistered called");
		Log.i(LOG_TAG, "Registration id is: " + registrationId);
	}
}