package com.smartrek.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Time;

import com.smartrek.exceptions.RouteNotFoundException;
import com.smartrek.models.Route;
import com.smartrek.utils.GeoPoint;

public class RouteFetchRequest extends FetchRequest<List<Route>> {
	
	private long departureTime;
	
	public static String buildUrl(GeoPoint origin, GeoPoint destination, long departureTime) {
		Time t = new Time();
		t.set(departureTime);
		
		return String.format("%s/getroutes/startlat=%.7f%%20startlon=%.7f%%20endlat=%.7f%%20endlon=%.7f%%20departtime=%d:%02d",
				HOST, origin.getLatitude(), origin.getLongitude(),
				destination.getLatitude(), destination.getLongitude(),
				t.hour, t.minute);
	}
	
	public RouteFetchRequest(GeoPoint origin, GeoPoint destination, long departureTime) {
		super(buildUrl(origin, destination, departureTime));
		this.departureTime = departureTime;
	}
	
	/**
	 * Enables debug mode
	 * 
	 * @param departureTime
	 */
	public RouteFetchRequest(long departureTime) {
		//super(HOST + "/getroutesTucson/fake");
		//super(HOST + "/getroutesTucsonNavigation");
		super("http://static.suminb.com/smartrek/fake-routes.html");
		
		this.departureTime = departureTime;
	}
	
	public List<Route> execute() throws IOException, JSONException, RouteNotFoundException {
		String response = executeFetchRequest(url);
		
		// Begin parsing the server response
		List<Route> routes = new ArrayList<Route>();

		JSONArray array = new JSONArray(response);
		for(int i = 0; i < array.length(); i++) {
			Route route = Route.parse((JSONObject) array.get(i), departureTime);
			routes.add(route);
		}
		
		if (routes.size() == 0) {
			throw new RouteNotFoundException("Could not find a route (0xb615)");
		}
		
		return routes;
	}
	

}
