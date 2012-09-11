package com.smartrek.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartrek.models.Trip;

public final class TripListFetchRequest extends FetchRequest<List<Trip>> {
	
	/**
	 * User ID
	 */
	private int uid;
	
	public TripListFetchRequest(int uid) {
		super(String.format("%s/favroutes-list/?userid=%d", HOST, uid));
		this.uid = uid;
	}
	
	@Override
	public List<Trip> execute() throws Exception {
		String response = executeFetchRequest(getURL());

		JSONArray array = new JSONArray(response);
		List<Trip> trips = new ArrayList<Trip>();
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = (JSONObject) array.get(i);
			int id = obj.getInt("FID");
			String name = obj.getString("NAME");
			String origin = obj.getString("ORIGIN_ADDRESS");
			String destination = obj.getString("DESTINATION_ADDRESS");
			
			trips.add(new Trip(id, name, origin, destination));
		}
		
		return trips;
	}
}
