package smartrek.tasks;

import smartrek.models.Route;
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
	
	/**
	 * This is going to be a bit tricky as we need to know whether the user actually has "departed".
	 * @param route
	 * @param actualDeptTime
	 * @return
	 */
	private boolean validateDepartureTime(Route route, Time actualDeptTime) {
		return false;
	}
	
	private boolean validateRoute(Location location) {
		Log.d("ReservationReceiver", "location = " + location);
		
		
		return false;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("ReservationReceiver", "Validation has started.");
		
		// TODO: Validate departure time
		// TODO: Trigger GPS receiver
		
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
