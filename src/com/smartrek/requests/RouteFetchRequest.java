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
import com.smartrek.utils.RouteNode;

public class RouteFetchRequest extends FetchRequest<List<Route>> {
	
	private long time;
	
	public static String buildUrl(GeoPoint origin, GeoPoint destination, long time) {
		Time t = new Time();
		t.set(time);
		
		return String.format("%s/getroutes/startlat=%f%%20startlon=%f%%20endlat=%f%%20endlon=%f%%20departtime=%d:%02d",
				HOST, origin.getLatitude(), origin.getLongitude(),
				destination.getLatitude(), destination.getLongitude(),
				t.hour, t.minute);
	}

	public RouteFetchRequest(GeoPoint origin, GeoPoint destination, long time) {
		super(buildUrl(origin, destination, time));
	}
	
	public List<Route> execute() throws IOException, JSONException, RouteNotFoundException {
		String response = executeFetchRequest(url);
		
		// Begin parsing the server response
		List<Route> routes = new ArrayList<Route>();

		JSONArray array = new JSONArray(response);
		for(int i = 0; i <array.length(); i++) {
			Route route = parseRoute((JSONObject) array.get(i), time);
			routes.add(route);
		}
		
		if (routes.size() == 0) {
			throw new RouteNotFoundException("Could not find a route (0xb615)");
		}
		
		return routes;
	}
	
	public Route parseRoute(JSONObject routeObject, long departureTime) throws JSONException, IOException {
	    JSONArray rts = (JSONArray) routeObject.get("ROUTE");
	    
	    ArrayList<RouteNode> routeNodes = new ArrayList<RouteNode>();
        for (int i = 0; i < rts.length(); i++) {
            JSONObject ro = (JSONObject) rts.get(i);
            
            RouteNode node = new RouteNode((float)ro.getDouble("LATITUDE"),
                    (float)ro.getDouble("LONGITUDE"), 0, ro.getInt("NODEID"));
            routeNodes.add(node);
        }
        
        // Route ID
        int rid = routeObject.getInt("RID");
        
        // Web service returns the estimated travel time in minutes, but we
        // internally store it as seconds.
        double ett = routeObject.getDouble("ESTIMATED_TRAVEL_TIME");
        
        // FIXME: User long instead of Time
        Time dTime = new Time();
        dTime.set(departureTime);

        Route route = new Route(routeNodes, rid, dTime, (int)(ett * 60));
        // FIXME: Implement getRouteCredits()
        //route.setCredits(getRouteCredits(rid));
        
        return route;
	}
}
