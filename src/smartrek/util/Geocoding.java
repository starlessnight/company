package smartrek.util;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

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
	 */
	public static GeoPoint lookup(String address) {
		String url = String.format("%s?address=%s&sensor=false", URL, URLEncoder.encode(address));
		String response = HTTP.downloadText(url);
		
		double lat = 0.0;
		double lng = 0.0;
		
		try {
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
		catch (JSONException e) {
			e.printStackTrace();
		}
		
		return new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6));
	}
}
