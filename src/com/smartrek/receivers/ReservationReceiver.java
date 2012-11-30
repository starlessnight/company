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
import com.smartrek.activities.ReservationDetailsActivity;
import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.utils.TimeRange;
import com.smartrek.utils.ValidationParameters;

/**
 * Route validation happens here
 *
 */
public final class ReservationReceiver extends BroadcastReceiver {
	
	public static final String RESERVATION_ID = "reservationId";
	
	public static final String RESERVATION = "reservation";
	
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
	    Reservation reservation = intent.getExtras().getParcelable("reservation");
	    int reservationId = intent.getExtras().getInt(RESERVATION_ID);
		
        Intent reservationIntent = new Intent(context, ReservationDetailsActivity.class);
        reservationIntent.putExtra(RESERVATION_ID, reservationId);
        reservationIntent.putExtra("route", route);
        reservationIntent.putExtra(RESERVATION, reservation);
        PendingIntent sender = PendingIntent.getActivity(context, 0, reservationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Notification notification = new Notification(R.drawable.icon_small, "Smartrek", route.getDepartureTime());
        notification.setLatestEventInfo(context, "Smartrek", "Your reserved trip is about to start", sender);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
		
		
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

}
