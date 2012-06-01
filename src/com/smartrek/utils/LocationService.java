package com.smartrek.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public final class LocationService {
	
	public interface LocationServiceListener {
		/**
		 * This will be called once (not repeatedly) when the current location is determined.
		 * @param location
		 */
		public void locationAcquired(Location location);
	}
	
	private static LocationService instance;
	
	private Context context;
	
	/**
	 * Last known current location
	 */
	private Location currentLocation;
	
	private LocationService(Context context) {
		this.context = context;
	}
	
	public static LocationService getInstance(Context context) {
		if (instance == null) {
			instance = new LocationService(context);
		}
		return instance;
	}
	
	public void cacheCurrentLocation() {
		requestCurrentLocation(null);
	}
	
	public void requestCurrentLocation(final LocationServiceListener listener) {
		// Acquire a reference to the system Location Manager
		final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				currentLocation = location;
				if (listener != null) {
					listener.locationAcquired(location);
				}
				locationManager.removeUpdates(this);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}

	/**
	 * WARNING: The value of currentLocation might be old.
	 * 
	 * @return
	 */
	public Location getCurrentLocation() {
		return currentLocation;
	}
	
	public static float distanceBetween(float lat1, float lng1, float lat2, float lng2) {
	    float[] results = new float[3];
	    Location.distanceBetween(lat1, lng1, lat2, lng2, results);
	    return results[0];
	}
}
