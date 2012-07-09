package com.smartrek.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.format.Time;
import android.util.Log;

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
		long d = route.getDepartureTime().toMillis(false);
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
		Log.d("ReservationReceiver", "Validation has started.");
		
		
        // TODO: We probably want to ask user if she wants to open up
        // ValidatinoActivity
		// TODO: Pass 'route' instance to ValidationActivity
		Intent intent2 = new Intent(context, ReservationConfirmationActivity.class);
		intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent2.putExtra("route", intent.getExtras().getParcelable("route"));
		context.startActivity(intent2);
		
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
