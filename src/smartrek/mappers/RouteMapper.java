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
import smartrek.util.HTTP;
import smartrek.util.RouteNode;
import android.text.format.Time;
import android.util.Log;

import com.google.android.maps.GeoPoint;


/**
 * 
 *
 */
public class RouteMapper extends Mapper {
	
	private String loc1;
	private String loc2;
	private String coupon_url;
	private Time time;
	
	/**
	 * Default constructor
	 */
	public RouteMapper(){
		super();
		coupon_url = "http://www.api.smartrekmobile.com/finddiscount?routeid=";
	}

	/**
	 * Retrieves all possible routes from the server.
	 * 
	 * @param origin Coordinate of the origin address
	 * @param destination Coordinate of the destinatino address
	 * @param time Departure time
	 * @return A list of all possible routes
	 * @throws JSONException
	 */
	public List<Route> getPossibleRoutes(GeoPoint origin, GeoPoint destination, Time time) throws JSONException {
			
//		this.loc1 = loc1;
//		this.loc2 = loc2;
		this.time = time;
		// Set Up the request to the sever
		//String routeurl = sturl + appendToUrl();
		
		String routeurl = String.format("http://50.56.81.42:8080/getroutes/startlat=%f%%20startlon=%f%%20endlat=%f%%20endlon=%f%%20departtime=5:00",
				origin.getLatitudeE6()/1.0E6, origin.getLongitudeE6()/1.0E6, destination.getLatitudeE6()/1.0E6, destination.getLongitudeE6()/1.0E6); 
		
		// Querry the server for the routes
		Log.d("Route_Communicator", "Querying Sever with");
		Log.d("Route_Communicator",routeurl);
		String route_response = HTTP.downloadText(routeurl);
		Log.d("Route_Communicator", "Query Complete, Got Route Information");
		
		
		
		// Begin parsing the server response
		List<Route> routes = new ArrayList<Route>();
		

		JSONArray array = new JSONArray(route_response);
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
			
			double ett = object.getDouble("ESTIMATED_TRAVEL_TIME");

			Route route = new Route(routeNodes, 0, (float)ett);
			routes.add(route);
		}

		return routes;
	}
	
	/**
	 * 
	 */
	protected String appendToUrl() {
		
		String toAppend = "/route.json?origin=";
		String[] splitstr1 = loc1.split(" ");
		String[] splitstr2 = loc2.split(" ");
		int i = 0;
		while(i < splitstr1.length) {
			toAppend += splitstr1[i] + "%20";
			i++;
		}
		toAppend += "&destination=";
		i = 0;
		while(i < splitstr2.length) {
			toAppend += splitstr2[i] + "%20";
			i++;	
		}

		toAppend += "&time_slot=%20" + time.hour + ":" + time.minute;
		Log.d("Route_Communicator", "Got url: " + sturl + toAppend);
		
		return toAppend;
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
		pairs.add(new BasicNameValuePair("DID", route.getDiscount().getDID() + ""));
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
		ArrayList<RouteNode> nodes = route.getPoints();
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(sturl + "/addroute");
		
		for (int i = 0; i < route.getPoints().size(); i++) {
			
			RouteNode rn = nodes.get(i);
			
			logAddRoute(i,rn,route.getRID());
			
			pairs.add(new BasicNameValuePair("RID", route.getRID() + ""));
			pairs.add(new BasicNameValuePair("ROUTE_ORDER", (i+1) + ""));
			pairs.add(new BasicNameValuePair("LATITUDE", rn.getFloatLat()));
			pairs.add(new BasicNameValuePair("LONGITUDE", rn.getFloatLon()));
			
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
		Log.d("Route_Communicator", "LATITUDE = " + p.getFloatLat());
		Log.d("Route_Communicator", "LONGITUDE = " + p.getFloatLon());
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
		Log.d("Route_Communicator", "DID = " + route.getDiscount().getDID());
		Log.d("Route_Communicator", "RID = " + route.getRID() + "");
		Log.d("Route_Communicator", "VALIDATED_FLAG = 0");
		Log.d("Route_Communicator", "ORIGIN_ADDRESS = "+ route.getOrigin() );
		Log.d("Route_Communicator", "DESTINATION_ADRESS = " + route.getDestination() );
	}
}
