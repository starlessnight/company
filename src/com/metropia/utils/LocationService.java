package com.metropia.utils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.metropia.activities.LoginActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public abstract class LocationService implements ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener, ResultCallback<LocationSettingsResult>, LocationListener {
	
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
	
	/*private LocationService(Context context) {
		this.context = context;
	}*/
	
	/*public static LocationService getInstance(Context context) {
		if (instance == null) {
			instance = new LocationService(context);
		}
		return instance;
	}*/
	
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
	
	public static double distanceBetween(double lat1, double lng1, double lat2, double lng2) {
		float[] results = new float[3];
	    Location.distanceBetween(lat1, lng1, lat2, lng2, results);
	    return results[0];
	}
	
	
	public  static final Integer REQUEST_CHECK_SETTINGS = Integer.valueOf(1111);
	
	private GoogleApiClient googleApiClient;
	private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationManager locationManager;
    
    public  boolean isGooglePlayServicesAvailable = false;
    private boolean requestingLocationUpdates = false;
	
	public LocationService() {
		
	}
	
	public void init(Context context) {
		this.context = context;
		isGooglePlayServicesAvailable = (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS);
		if (isGooglePlayServicesAvailable) {
			requestingLocationUpdates = true;
			createGoogleApiClient();
	        createLocationRequest();
	        buildLocationSettingsRequest();
		}
	}
	
	public void createGoogleApiClient() {
    	GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context);
    	googleApiClient =  builder.addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
    }
	public void connect() {
		if (googleApiClient!=null) googleApiClient.connect();
	}
	public void disconnect() {
		if (googleApiClient!=null) googleApiClient.disconnect();
	}
	
	public void createLocationRequest() {
		locationRequest = new LocationRequest();
		locationRequest.setInterval(5000);
		locationRequest.setFastestInterval(2500);
		locationRequest.setSmallestDisplacement(5);
		locationRequest.setNumUpdates(1);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
	
	public void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }
	
	protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest).setAlwaysShow(true);
        locationSettingsRequest = builder.build();
    }
	protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);
        result.setResultCallback(this);
    }
	
	
	public void prepareGPS() {
		if(googleApiClient != null && requestingLocationUpdates) {
    		checkLocationSettings();
    	}
    	else if(googleApiClient == null){
    		closeGPS();
    		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean gps_location_provided = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean network_location_provided = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (gps_location_provided && network_location_provided) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
            else if (network_location_provided) {
            	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }else{
                SystemService.alertNoGPS(context, true);
            }
    	}
	}
	
	public void closeGPS(){
    	if(googleApiClient != null && googleApiClient.isConnected()) {
	    	LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this).setResultCallback(new ResultCallback<Status>() {
	            @Override
	            public void onResult(Status status) {
	                requestingLocationUpdates = true;
	            }
	        });
    	}
    	else if(locationManager != null){
    		locationManager.removeUpdates(this);
    	}
    }

	public void onConnected(Bundle arg0) {}
	public void onConnectionSuspended(int arg0) {}
	public void onConnectionFailed(ConnectionResult arg0) {}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode==REQUEST_CHECK_SETTINGS && resultCode==Activity.RESULT_OK) requestingLocationUpdates = false;
        if (requestCode==REQUEST_CHECK_SETTINGS) startLocationUpdates();
	}

}
