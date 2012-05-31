package com.smartrek.mappers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Time;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.smartrek.models.Route;
import com.smartrek.utils.Cache;
import com.smartrek.utils.HTTP;
import com.smartrek.utils.RouteNode;


/**
 * 
 *
 */
public final class RouteMapper extends Mapper {
	
	/**
	 * Default constructor
	 */
	public RouteMapper(){
		super();
	}
	
	public Route parseRoute(JSONObject routeObject, Time departureTime) throws JSONException, IOException {
	    JSONArray rts = (JSONArray) routeObject.get("ROUTE");
	    
	    ArrayList<RouteNode> routeNodes = new ArrayList<RouteNode>();
        for(int j = 0; j < rts.length(); j++) {
            JSONObject ro = (JSONObject) rts.get(j);
            
            RouteNode node = new RouteNode((float)ro.getDouble("LATITUDE"),
                    (float)ro.getDouble("LONGITUDE"), 0, ro.getInt("NODEID"));
            routeNodes.add(node);
        }
        
        // Route ID
        int rid = routeObject.getInt("RID");
        
        // Web service returns the estimated travel time in minutes, but we
        // internally store it as seconds.
        double ett = routeObject.getDouble("ESTIMATED_TRAVEL_TIME");

        Route route = new Route(routeNodes, rid, departureTime, (int)(ett * 60));
        route.setCredits(getRouteCredits(rid));
        
        return route;
	}

	/**
	 * Retrieves all possible routes from the server.
	 * 
	 * @param origin Coordinate of the origin address
	 * @param destination Coordinate of the destination address
	 * @param time Departure time
	 * @return A list of all possible routes
	 * @throws JSONException
	 * @throws IOException 
	 */
	public List<Route> getPossibleRoutes(GeoPoint origin, GeoPoint destination, Time time) throws JSONException, IOException {
		
		String routeurl = String.format("%s/getroutes/startlat=%f%%20startlon=%f%%20endlat=%f%%20endlon=%f%%20departtime=%d:%02d",
				host, origin.getLatitudeE6()/1.0E6, origin.getLongitudeE6()/1.0E6, destination.getLatitudeE6()/1.0E6, destination.getLongitudeE6()/1.0E6,
				time.hour, time.minute); 
		
		Log.d("RouteMapper", routeurl);
		
		Cache cache = Cache.getInstance();
		String routeResponse = (String) cache.fetch(routeurl);
		
		// Begin parsing the server response
		List<Route> routes = new ArrayList<Route>();

		JSONArray array = new JSONArray(routeResponse);
		for(int i = 0; i <array.length(); i++) {
			Route route = parseRoute((JSONObject) array.get(i), time);
			routes.add(route);
		}

		return routes;
	}
	
	/**
	 * 
	 * @param rid Route ID
	 * @return Credits associated with a specific route
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public int getRouteCredits(int rid) throws IOException, JSONException {
		String url = String.format("%s/getroutecredits/%d", host, rid);
		HTTP http = new HTTP(url);
		http.connect();
		
		Cache cache = Cache.getInstance();
		String response = (String) cache.fetch(url);
		
		int credits = 0;
		JSONArray array = new JSONArray(response);
		if (array.length() > 0) {
			JSONObject object = array.getJSONObject(0);
			credits = object.getInt("CREDIT");
		}
		return credits;
	}
	
	/**
	 * 
	 * @param route
	 * @return
	 */
	public String reservation(Route route) {
		logReservationPost(route);

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(sturl + "/reservation");

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("START_DATETIME", route.timeToString()));
		pairs.add(new BasicNameValuePair("END_DATETIME", route.timeToString()));

		pairs.add(new BasicNameValuePair("UID", route.getUserId() + ""));
		pairs.add(new BasicNameValuePair("DID", route.getDiscount().getDid() + ""));
		pairs.add(new BasicNameValuePair("RID", route.getId() + ""));

		pairs.add(new BasicNameValuePair("VALIDATED_FLAG", "0"));

		pairs.add(new BasicNameValuePair("ORIGIN_ADDRESS", route.getOrigin()));
		pairs.add(new BasicNameValuePair("DESTINATION_ADDRESS", route.getDestination()));

		HttpResponse response = null;

		try {
			post.setEntity(new UrlEncodedFormEntity(pairs));
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.d("Route_Communicator", response.toString());
		Log.d("Route_Communicator", response.getEntity().toString());
		Log.d("Route_Communicator", response.getHeaders("uid").toString());

		addroute(route);
		
		return "";

	}
	
	/**
	 * 
	 * @param route
	 */
	public void addroute(Route route) {
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		List<RouteNode> nodes = route.getNodes();
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(sturl + "/addroute");
		
		for (int i = 0; i < route.getNodes().size(); i++) {
			
			RouteNode rn = nodes.get(i);
			
			logAddRoute(i,rn,route.getId());
			
			pairs.add(new BasicNameValuePair("RID", route.getId() + ""));
			pairs.add(new BasicNameValuePair("ROUTE_ORDER", (i+1) + ""));
			pairs.add(new BasicNameValuePair("LATITUDE", String.valueOf(rn.getLatitude())));
			pairs.add(new BasicNameValuePair("LONGITUDE", String.valueOf(rn.getLongitude())));
			
			HttpResponse response = null;

//			try {
//				post.setEntity(new UrlEncodedFormEntity(pairs));
//				response = client.execute(post);
//			} catch (ClientProtocolException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}			
		}
	}
	
	/**
	 * 
	 * @param i
	 * @param p
	 * @param lable
	 */
	private void logAddRoute(int i, RouteNode p, float lable){
		Log.d("Route_Communicator", "In route communicator trying to add route");
		Log.d("Route_Communicator", "ROUTE_NODE = " + i);
		Log.d("Route_Communicator", "ROUTE_LABLE = " +  lable);
		Log.d("Route_Communicator", "LATITUDE = " + p.getLatitude());
		Log.d("Route_Communicator", "LONGITUDE = " + p.getLongitude());
	}

	/**
	 * 
	 * @param route
	 */
	private void logReservationPost(Route route) {
		Log.d("Route_Communicator", "In route communicator trying to reserve route");
		Log.d("Route_Communicator", "START_DATETIME = " + route.timeToString());
		Log.d("Route_Communicator", "END_DATETIME = " +  route.timeToString());
		Log.d("Route_Communicator", "UID = " + route.getUserId());
		Log.d("Route_Communicator", "DID = " + route.getDiscount().getDid());
		Log.d("Route_Communicator", "RID = " + route.getId() + "");
		Log.d("Route_Communicator", "VALIDATED_FLAG = 0");
		Log.d("Route_Communicator", "ORIGIN_ADDRESS = "+ route.getOrigin() );
		Log.d("Route_Communicator", "DESTINATION_ADRESS = " + route.getDestination() );
	}
}
