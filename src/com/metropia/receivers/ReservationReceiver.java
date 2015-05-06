package com.metropia.receivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;

import com.metropia.activities.LandingActivity2;
import com.metropia.activities.R;
import com.metropia.models.Reservation;
import com.metropia.models.Route;
import com.metropia.utils.Misc;
import com.metropia.utils.ValidationParameters;
import com.metropia.utils.datetime.TimeRange;

/**
 * Route validation happens here
 *
 */
public final class ReservationReceiver extends BroadcastReceiver {
	
	public static final String LOG_TAG = "ReservationReceiver";
	
	public static final String RESERVATION_ID = "reservationId";
	
	public static final int ID = 0;
	
	private ValidationParameters parameters;
	
	private boolean departureTimeValidated;
	
	/**
	 * This is going to be a bit tricky as we need to know whether the user actually has "departed".
	 * @param route
	 * @param actualDeptTime
	 * @return
	 */
	private boolean validateDepartureTime(Route route, Time actualDeptTime) {
		long d = route.getDepartureTime();
		long n = parameters.getDepartureTimeNegativeThreshold() * 1000;
		long p = parameters.getDepartureTimePositiveThreshold() * 1000;
		
		TimeRange range = new TimeRange(d - n, d + p);
		
		return range.isInRange(actualDeptTime);
	}
	
	private boolean validateRoute(Location location) {
		Log.d(LOG_TAG, "location = " + location);
		
		
		return false;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
	    Log.d(LOG_TAG, "onReceive");
		
	    Route route = intent.getExtras().getParcelable("route");
	    long reservationId = intent.getExtras().getLong(RESERVATION_ID);
        
        Intent landingIntent = new Intent(context, LandingActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        long departureTime = route.getDepartureTime();
        
        if(route != null && Reservation.isEligibleTrip(departureTime)){
            PendingIntent sender = PendingIntent.getActivity(context, 0, landingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            Notification notification = new Notification(R.drawable.icon_small, "Metropia", route.getDepartureTime());
            notification.setLatestEventInfo(context, "Metropia", "Your reserved trip is about to start", sender);
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(ID, notification);
            
//            playLouderNotification(context);
            Misc.playOnMyWaySound(context);
            Misc.wakeUpScreen(context, ReservationReceiver.class.getSimpleName());
            
            Intent expiry = new Intent(context, NotificationExpiry.class);
            expiry.putExtra(NotificationExpiry.NOTIFICATION_ID, ID);
            PendingIntent pendingExpiry = PendingIntent.getBroadcast(context, 0, 
                expiry, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager expiryMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            expiryMgr.set(AlarmManager.RTC, Reservation.getExpiryTime(departureTime), pendingExpiry);
        }
		
		/*
		
		if (parameters == null) {
			parameters = new ValidationParameters();
			Log.d(getClass().toString(), "ValidationParameters initialized.");
		}
		
		// TODO: Validate departure time
		if (departureTimeValidated == false) {
			Time currentTime = new Time();
			currentTime.setToNow();
			
			departureTimeValidated = validateDepartureTime(route, currentTime);
			Log.d(getClass().toString(), "departureTimeValidated = " + departureTimeValidated);
		}
		
		
		// TODO: What's going to happen when the app terminates in the middle of validation?
		 */
	}
	
	private void playLouderNotification(Context ctx) {
		try {
			final AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
			final int userVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//			int mode = audioManager.getMode();
			Log.d(LOG_TAG, "current volume : " + userVolume);
			if(userVolume > 0) {
				final MediaPlayer mp = new MediaPlayer();
				Uri ding = Uri.parse("android.resource://" + ctx.getPackageName() + "/" + R.raw.omw);
		    	if(!mp.isPlaying()) {
		    		mp.setDataSource(ctx, ding);
		    		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
		            mp.setLooping(false);
		            mp.prepare();
		            mp.start();
		    	}
		    	audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);
		    	new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						try {
							audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, userVolume, AudioManager.FLAG_PLAY_SOUND);
							mp.stop();
							mp.reset();
							mp.release();
						}
						catch(Exception ignore) {}
					}
		    	}, 3000);
			}
		}
		catch(Exception ignore) {}
	}

}
