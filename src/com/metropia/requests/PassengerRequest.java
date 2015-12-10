package com.metropia.requests;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.metropia.exceptions.SmarTrekException;
import com.metropia.models.Passenger;
import com.metropia.models.User;
import com.metropia.tasks.ICallback;
import com.metropia.utils.HTTP.Method;

public class PassengerRequest extends Request {
	
	public PassengerRequest(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
	}
    
    public ArrayList<Passenger> execute(Context ctx, long rid) throws Exception {
        String apiUrl = getLinkUrl(Link.passenger_bubble_head);
        String url = apiUrl.replaceAll("\\{reservation_id\\}", String.valueOf(rid));
        
        try{
        	ArrayList<Passenger> passengers = new ArrayList<Passenger>();
            String str = executeHttpRequest(Method.POST, url, ctx);
            if (str==null) return passengers;
            
            JSONObject json = new JSONObject(str);
            JSONArray names = json.getJSONObject("data").getJSONArray("o_users_names");
            JSONArray photos = json.getJSONObject("data").getJSONArray("o_users_pic");
            JSONArray ids = json.getJSONObject("data").getJSONArray("o_users_id");
            JSONObject exception = json.optJSONObject("exception");
            
            if (exception!=null) throw new InvalidTripException(exception.getString("header"), exception.getString("content"));
            
            for (int i=0 ; i<names.length() ; i++) {
            	passengers.add(new Passenger(ids.getInt(i), names.getString(i), photos.getString(i)));
            }
            
            return passengers;
        }catch(Exception e){
            if(responseCode >= 400 && responseCode <= 499){
                throw new SmarTrekException(responseCode);
            }else{
                throw e;
            }
        }
    }
    
    public void executeAsync(final Context ctx, final long rid, final ICallback cb) {
    	
    	new AsyncTask<Void, Void, ArrayList<Passenger>>() {

			@Override
			protected ArrayList<Passenger> doInBackground(Void... params) {
				ArrayList<Passenger> passengers = null;
				try {
					passengers = PassengerRequest.this.execute(ctx, rid);
				} catch (InvalidTripException e) {
					if (cb!=null) cb.run(e);
					return null;
				} catch (Exception e) {
					
				}
				
				if (cb!=null) cb.run(passengers);
				return null;
			}
		}.execute();
		
    }
    
    public class InvalidTripException extends Exception {
    	
    	public String header;
    	public String message;
    	
    	public InvalidTripException(String header, String message) {
    		this.header = header;
    		this.message = message;
    	}
    }
}
