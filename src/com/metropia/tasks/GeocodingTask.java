package com.metropia.tasks;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;

import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.Geocoding;

/**
 * Asynchronous task that converts a postal address to a geographic coordinate.
 */
public final class GeocodingTask extends AsyncTask<String, Void, Void> {
	
    private boolean registerException;
	private ExceptionHandlingService ehs;
	private GeocodingTaskCallback callback;
	private Context ctx;
	private GeoPoint currentLocation;
	
	public GeocodingTask(Context ctx, GeoPoint currentLocation, ExceptionHandlingService ehs, GeocodingTaskCallback callback, 
	        boolean registerException) {
		super();
		this.ctx = ctx;
		this.ehs = ehs;
		this.callback = callback;
		this.registerException = registerException;
		this.currentLocation = currentLocation;
	}
	
	public GeocodingTask(Context ctx, GeoPoint currentLocation, ExceptionHandlingService ehs, GeocodingTaskCallback callback) {
        this(ctx, currentLocation, ehs, callback, true);
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
        List<Geocoding.Address> addresses = null;
		try {
			addresses = Geocoding.lookup(ctx, postalAddress, currentLocation.getLatitude(), currentLocation.getLongitude());
			
	        if(addresses == null || addresses.size() == 0) {
	            if(registerException){
	                ehs.registerException(new Exception(String.format("Could not find a coordinate of the address '%s'.", postalAddress)));
	            }
	        }
	        else {
	        	callback.callback(addresses);
	        }
		}
		catch (IOException e) {
		    if(registerException){
		        ehs.registerException(e, "Could not complete geocoding request");
		    }
		}
		catch (JSONException e) {
		    if(registerException){
		        ehs.registerException(e);
		    }
		}
		catch (Exception e) {
		    if(registerException){
		        ehs.registerException(e);
		    }
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