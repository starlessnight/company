package com.smartrek.tasks;

import java.io.IOException;
import java.util.Stack;

import org.json.JSONException;

import android.os.AsyncTask;

import com.google.android.maps.GeoPoint;
import com.smartrek.utils.Geocoding;

/**
 * Asynchronous task that converts a postal address to a geographic coordinate.
 */
public final class GeocodingTask extends AsyncTask<String, Void, Void> {
	
	private Stack<Exception> exceptions;
	private GeocodingTaskCallback callback;
	
	public GeocodingTask(Stack<Exception> exceptions, GeocodingTaskCallback callback) {
		super();
		this.exceptions = exceptions;
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
		}
		catch (IOException e) {
			e.printStackTrace();
			exceptions.push(e);
		}
		catch (JSONException e) {
			e.printStackTrace();
			exceptions.push(e);
		}
        
        if(coordinate.getLatitudeE6() == 0 && coordinate.getLongitudeE6() == 0) {
            exceptions.push(new Exception(String.format("Could not find a coordinate of the address '%s'.", postalAddress)));
        }
        else {
        	callback.callback(coordinate);
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