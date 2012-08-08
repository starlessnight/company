package com.smartrek.utils;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.smartrek.utils.GeoPoint;


/**
 * Geocoding is the process of converting addresses
 * (like "1600 Amphitheatre Parkway, Mountain View, CA") into geographic
 * coordinates (like latitude 37.423021 and longitude -122.083739), which you
 * can use to place markers or position the map.
 * http://code.google.com/apis/maps/documentation/geocoding/
 */
public final class Geocoding {
	
	/**
	 * http://code.google.com/apis/maps/documentation/geocoding/#GeocodingResponses
	 */
	public static final String URL = "http://maps.googleapis.com/maps/api/geocode/json";

	/**
	 * Converts an address into a geographic coordinate. This function does not
	 * make use of multithreading for networking.
	 * 
	 * @param address A postal address
	 * @return A geographic coordinate (latitude, longitude)
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public static GeoPoint lookup(String address) throws IOException, JSONException {
		String url = String.format("%s?address=%s&sensor=false", URL, URLEncoder.encode(address));
		
		double lat = 0.0;
		double lng = 0.0;
		
		HTTP http = new HTTP(url);
		http.connect();
		
		int responseCode = http.getResponseCode();
		if (responseCode == 200) {
			String response = http.getResponseBody();
			
			JSONObject object = new JSONObject(response);
			
			// if status == "OK"
			if("OK".equals(object.get("status"))) {
				JSONArray results = (JSONArray) object.get("results");
				if(results.length() > 0) {
					JSONObject result = (JSONObject) results.get(0);
					
					JSONObject geometry = (JSONObject) result.get("geometry");
					JSONObject location = (JSONObject) geometry.get("location");
				
					lat = location.getDouble("lat");
					lng = location.getDouble("lng");
				}
			}
		}
		
		return new GeoPoint(lat, lng);
	}
}
