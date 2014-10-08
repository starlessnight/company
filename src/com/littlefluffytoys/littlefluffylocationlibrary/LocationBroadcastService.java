/*
 * Copyright 2012 Little Fluffy Toys Ltd
 * Adapted from work by Reto Meier, Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.littlefluffytoys.littlefluffylocationlibrary;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * This is an example of implementing an application service that will run in
 * response to an alarm, allowing us to move long duration work out of an intent
 * receiver.
 * 
 * @see AlarmService
 * @see AlarmService_Alarm
 */
public class LocationBroadcastService extends Service {
    
    private static final String TAG = "LocationBroadcastService"; 

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": onStartCommand");

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        new Thread(null, mTask, TAG).start();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": onDestroy");
    }
    
    /**
     * The function that runs in our worker thread
     */
    Runnable mTask = new Runnable() {
        public void run() {
            boolean stopServiceOnCompletion = true;

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            final long lastLocationUpdateTimestamp = prefs.getLong(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_TIME, 0);
            final long lastLocationBroadcastTimestamp = prefs.getLong(LocationLibraryConstants.SP_KEY_LAST_LOCATION_BROADCAST_TIME, 0);
            final boolean forceLocationToUpdate = prefs.getBoolean(LocationLibraryConstants.SP_KEY_FORCE_LOCATION_UPDATE, false);

            if (lastLocationBroadcastTimestamp == lastLocationUpdateTimestamp) {
                // no new location found
                if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": No new location update found since " + LocationInfo.formatTimestampForDebug(lastLocationUpdateTimestamp));

                if (forceLocationToUpdate || System.currentTimeMillis() - lastLocationUpdateTimestamp > LocationLibrary.getLocationMaximumAge()) {
                    if (forceLocationToUpdate) {
                        prefs.edit().putBoolean(LocationLibraryConstants.SP_KEY_FORCE_LOCATION_UPDATE, false).commit();
                    }
                    // Current location is out of date. Force an update, and stop service if required.
                    stopServiceOnCompletion = !forceLocationUpdate();
                }
            } else {
                if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": New location update found at " + LocationInfo.formatTimestampForDebug(lastLocationUpdateTimestamp));
                
                final Editor prefsEditor = prefs.edit();
                prefsEditor.putLong(LocationLibraryConstants.SP_KEY_LAST_LOCATION_BROADCAST_TIME, lastLocationUpdateTimestamp);
                if (forceLocationToUpdate) {
                    prefsEditor.putBoolean(LocationLibraryConstants.SP_KEY_FORCE_LOCATION_UPDATE, false);
                }
                prefsEditor.commit();
                sendBroadcast(getBaseContext(), true);
            }

            if (stopServiceOnCompletion) {
                // Done with our work... stop the service!
                LocationBroadcastService.this.stopSelf();
            }
        }
    };
    
    protected static void sendBroadcast(final Context context, final boolean isPeriodicBroadcast) {
        final Intent locationIntent = new Intent(LocationLibrary.broadcastPrefix + (isPeriodicBroadcast ? LocationLibraryConstants.LOCATION_CHANGED_PERIODIC_BROADCAST_ACTION : LocationLibraryConstants.LOCATION_CHANGED_TICKER_BROADCAST_ACTION));
        final LocationInfo locationInfo = new LocationInfo(context);
        locationIntent.putExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO, locationInfo);
        if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": Broadcasting " + (isPeriodicBroadcast ? "periodic" : "latest") + " location update timed at " + LocationInfo.formatTimeAndDay(PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getLong(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_TIME, System.currentTimeMillis()), true));
        context.sendBroadcast(locationIntent, "android.permission.ACCESS_FINE_LOCATION");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    /**
     * 
     * @return true if the service should stay awake, false if not
     */
    @TargetApi(9)
    public boolean forceLocationUpdate() {
        final LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean useFineAccuracyForRequests = LocationLibrary.useFineAccuracyForRequests(getApplicationContext());
        boolean useGPS = useFineAccuracyForRequests || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isUsingGPS = pref.getBoolean(LocationLibraryConstants.SP_KEY_IS_USING_GPS, false);
        final Intent gpsIntent = new Intent(getApplicationContext(), PassiveLocationChangedReceiver.class).addCategory(LocationLibraryConstants.INTENT_CATEGORY_GPS_UPDATE);
        final PendingIntent gpsPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, gpsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if(useGPS && !isUsingGPS){
            pref.edit().putBoolean(LocationLibraryConstants.SP_KEY_IS_USING_GPS, true).commit();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LocationLibrary.getAlarmFrequency(), 0, gpsPendingIntent);
        }else if(!useGPS && isUsingGPS){
            pref.edit().putBoolean(LocationLibraryConstants.SP_KEY_IS_USING_GPS, false).commit();
            locationManager.removeUpdates(gpsPendingIntent);
        }

        // stop the service
        return false;
    }
    
    /**
     * Forces this service to be called after the given delay
     */
    public static void forceDelayedServiceCall(final Context context, final int delayInSeconds) {
        final Intent serviceIntent = new Intent(context, LocationBroadcastService.class);
        final PendingIntent pIntent = PendingIntent.getService(context, LocationLibraryConstants.LOCATION_BROADCAST_REQUEST_CODE_SINGLE_SHOT, serviceIntent, PendingIntent.FLAG_ONE_SHOT);
        final AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (delayInSeconds * 1000), pIntent);
    }

    final LocationListener preGingerbreadUpdatesListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (LocationLibrary.showDebugOutput) Log.d(LocationLibraryConstants.TAG, TAG + ": Single Location Update Received: " + location.getLatitude() + "," + location.getLongitude());
            ((LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).removeUpdates(preGingerbreadUpdatesListener);
            
            if (!LocationLibraryConstants.SUPPORTS_FROYO) {
                // this will not be broadcast by the passive location updater, so we will process it ourselves
                PassiveLocationChangedReceiver.processLocation(LocationBroadcastService.this, location);
            }
            
            // Broadcast it without significant delay.
            forceDelayedServiceCall(getApplicationContext(), 1);
            // Done with our work... stop the service!
            LocationBroadcastService.this.stopSelf();
        }
        
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}    
        public void onProviderDisabled(String provider) {}
      };
      
    /**
     * This is the object that receives interactions from clients. See
     * RemoteService for a more complete example.
     */
    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
}
