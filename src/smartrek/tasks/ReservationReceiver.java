package smartrek.tasks;

import smartrek.models.Route;
import smartrek.util.TimeRange;
import smartrek.util.ValidationParameters;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;

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
		
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				validateRoute(location);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
				Log.d(this.getClass().toString(), String.format("onStatusChanged: %s, %d, %s", provider, status, extras));
			}

			public void onProviderEnabled(String provider) {
				Log.d(this.getClass().toString(), String.format("onProviderEnabled: %s", provider));
			}

			public void onProviderDisabled(String provider) {
				Log.d(this.getClass().toString(), String.format("onProviderDisabled: %s", provider));
			}
		};

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 1, locationListener);
		
		// TODO: What's going to happen when the app terminates in the middle of validation?
	}

}
