package com.smartrek.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.Trip;
import com.smartrek.models.User;
import com.smartrek.utils.datetime.RecurringTime;

public final class TripListFetchRequest extends FetchRequest<List<Trip>> {
	
	public TripListFetchRequest(User user) {
		super(NEW_API?getLinkUrl(Link.commute):String.format("%s/V0.2/favroutes-list/?userid=%d", HOST, user.getId()));
		if(NEW_API){
            this.username = user.getUsername();
            this.password = user.getPassword();
        }
	}
	
	@Override
	public List<Trip> execute(Context ctx) throws JSONException, IOException {
	    List<Trip> trips = new ArrayList<Trip>();
		String response = executeFetchRequest(getURL(), ctx);
		
		if(NEW_API){
		    JSONArray array = new JSONObject(response).getJSONArray("data");
		    for (int i = 0; i < array.length(); i++) {
                JSONObject object = (JSONObject) array.get(i);
                int id = object.getInt("id");
                int oid = object.getInt("origin_id");
                int did = object.getInt("destination_id");
                String name = object.getString("name");
                String origin = object.optString("origin_address");
                String destination = object.optString("destination_address");

                byte hour = 0;
                byte minute = 0;
                byte second = 0;
                
                // FIXME: This should be desired arrival time
                String departureTime = object.getString("arrival_time");
                if (departureTime.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
                    String[] cols = departureTime.split(":");
                    
                    hour = (byte) (Integer.parseInt(cols[0]) + 1);
                    minute = (byte) Integer.parseInt(cols[1]);
                    second = (byte) Integer.parseInt(cols[2]);
                }
                
                byte weekdays = (byte) object.getInt("datetype");
                
                trips.add(new Trip(id, name, oid, origin, did, destination, new RecurringTime(hour, minute, second, weekdays)));
            }
		}else{
		    JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = (JSONObject) array.get(i);
                trips.add(Trip.parse(obj));
            }
		}
		
		return trips;
	}
}
