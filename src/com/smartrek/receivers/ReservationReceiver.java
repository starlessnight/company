package com.smartrek.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.format.Time;
import android.util.Log;

import com.smartrek.activities.R;
import com.smartrek.activities.ReservationConfirmationActivity;
import com.smartrek.models.Route;
import com.smartrek.utils.TimeRange;
import com.smartrek.utils.ValidationParameters;

/**
 * Route validation happens here
 *
 */
public final class ReservationReceiver extends BroadcastReceiver {
	
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
		Log.d("ReservationReceiver", "location = " + location);
		
		
		return false;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
	    Log.d("ReservationReceiver", "onReceive");
		
	    Route route = intent.getExtras().getParcelable("route");
		
        Intent reservationConfirmationIntent = new Intent(context, ReservationConfirmationActivity.class);
        intent.putExtra("route", route);
        //intent.putExtra("reservation", reservation);
        // In reality, you would want to have a static variable for the
        // request code instead of 192837
        PendingIntent sender = PendingIntent.getBroadcast(context, 192837, reservationConfirmationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Notification notification = new Notification(R.drawable.icon, "SmarTrek", route.getDepartureTime());
        notification.setLatestEventInfo(context, "SmarTrek", "Your reserved trip is about to start", sender);
        notificationManager.notify(0, notification);
		
		
        // TODO: We probably want to ask user if she wants to open up
        // ValidatinoActivity
		// TODO: Pass 'route' instance to ValidationActivity
//		Intent intent2 = new Intent(context, ReservationDetailsActivity.class);
//		intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		intent2.putExtra("route", intent.getExtras().getParcelable("route"));
//		context.startActivity(intent2);
		
		/*
		
		Bundle extras = intent.getExtras();
		Route route = (Route) extras.get("route");
		
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

}
