package com.smartrek.tasks;

import android.os.AsyncTask;

import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Geocoding;

/**
 * Asynchronous task that converts a postal address to a geographic coordinate.
 */
public final class GeocodingTask extends AsyncTask<String, Void, Void> {
	
	private ExceptionHandlingService ehs;
	private GeocodingTaskCallback callback;
	
	public GeocodingTask(ExceptionHandlingService ehs, GeocodingTaskCallback callback) {
		super();
		this.ehs = ehs;
		this.callback = callback;
	}
    
    @Override
    protected void onPreExecute () {
    	if(callback != null) {
    		callback.preCallback();
    	}
    }
    
    @Override
    protected Void doInBackground(String... args) {
        String postalAddress = args[0];
        GeoPoint coordinate = null;
		try {
			coordinate = Geocoding.lookup(postalAddress);
			
	        if(coordinate.getLatitudeE6() == 0 && coordinate.getLongitudeE6() == 0) {
	            ehs.registerException(new Exception(String.format("Could not find a coordinate of the address '%s'.", postalAddress)));
	        }
	        else {
	        	callback.callback(coordinate);
	        }
		}
		catch (Exception e) {
			ehs.registerException(e);
		}
        
        return null;
    }
    
    @Override
    protected void onPostExecute(Void v) {
        if(callback != null) {
        	callback.postCallback();
        }
    }

}