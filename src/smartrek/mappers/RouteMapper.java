package smartrek.mappers;

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

import smartrek.models.Route;
import smartrek.util.Cache;
import smartrek.util.RouteNode;
import android.text.format.Time;
import android.util.Log;

import com.google.android.maps.GeoPoint;


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
			JSONObject object = (JSONObject) array.get(i);
			JSONArray rts = (JSONArray) object.get("ROUTE");
			
			ArrayList<RouteNode> routeNodes = new ArrayList<RouteNode>();
			for(int j = 0; j < rts.length(); j++) {
				JSONObject ro = (JSONObject) rts.get(j);
				
				RouteNode node = new RouteNode((float)ro.getDouble("LATITUDE"),
						(float)ro.getDouble("LONGITUDE"), 0, ro.getInt("NODEID"));
				routeNodes.add(node);
			}
			
			// Web service returns the estimated travel time in minutes, but we
			// internally store it as seconds.
			double ett = object.getDouble("ESTIMATED_TRAVEL_TIME");

			// FIXME: Route ID
			Route route = new Route(routeNodes, 0, time, (int)(ett * 60));
			routes.add(route);
		}

		return routes;
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
		pairs.add(new BasicNameValuePair("RID", route.getRID() + ""));

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
			
			logAddRoute(i,rn,route.getRID());
			
			pairs.add(new BasicNameValuePair("RID", route.getRID() + ""));
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
		Log.d("Route_Communicator", "RID = " + route.getRID() + "");
		Log.d("Route_Communicator", "VALIDATED_FLAG = 0");
		Log.d("Route_Communicator", "ORIGIN_ADDRESS = "+ route.getOrigin() );
		Log.d("Route_Communicator", "DESTINATION_ADRESS = " + route.getDestination() );
	}
}
