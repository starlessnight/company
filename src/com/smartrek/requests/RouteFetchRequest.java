package com.smartrek.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.format.Time;

import com.smartrek.exceptions.RouteNotFoundException;
import com.smartrek.models.Route;
import com.smartrek.utils.GeoPoint;

public class RouteFetchRequest extends FetchRequest<List<Route>> {
	
	private long departureTime;
	
	private boolean fake;
	
	public static String buildUrl(GeoPoint origin, GeoPoint destination, long departureTime) {
		Time t = new Time();
		t.set(departureTime);
		t.switchTimezone(TIME_ZONE);
		
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
		super(HOST + "/getroutesTucsonNavigation/fake");
		//super("http://static.suminb.com/smartrek/fake-routes.html");
		
		this.departureTime = departureTime;
		fake = true;
	}
	
	public List<Route> execute(Context ctx) throws IOException, JSONException, RouteNotFoundException {
		String response = null;
		try{
		    response = executeFetchRequest(url, ctx);
		}catch(IOException e){
		    String msg = null;
		    if(responseCode == 400){
		        msg = "The requested route is out of service area.";
		    }else if(responseCode == 500){
		        msg = "The server is busy.";
		    }
		    if(msg == null){
		        throw e;
		    }else{
		        throw new IOException(msg);
		    }
		}
		
		// Begin parsing the server response
		List<Route> routes = new ArrayList<Route>();

		JSONArray array = new JSONArray(response);
		for(int i = 0; i < array.length(); i++) {
			Route route = Route.parse((JSONObject) array.get(i), departureTime);
		    route.setFake(fake);
		    route.setSeq(i);
		    if(!route.getNodes().isEmpty()){
		        routes.add(route);
		    }
		}
		
		if (routes.size() == 0) {
			throw new RouteNotFoundException("Could not find a route.");
		}
		
		return routes;
	}
	

}
