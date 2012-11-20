package com.smartrek.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


/**
 * Geocoding is the process of converting addresses
 * (like "1600 Amphitheatre Parkway, Mountain View, CA") into geographic
 * coordinates (like latitude 37.423021 and longitude -122.083739), which you
 * can use to place markers or position the map.
 * http://code.google.com/apis/maps/documentation/geocoding/
 */
public final class Geocoding {
	
	public static class Address {
		private double latitude;
		private double longitude;
		
		/**
		 * e.g. "2nd Street, Tucson, Pima, Arizona, 85748, United States of America"
		 */
		private String name;
		
		/**
		 * e.g. "place", "highway", ...
		 */
		private String class_;
		
		/**
		 * e.g. "house", "residential", ...
		 */
		private String type;

		public double getLatitude() {
			return latitude;
		}

		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}
		
		public GeoPoint getGeoPoint() {
			return new GeoPoint(latitude, longitude);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getClass_() {
			return class_;
		}

		public void setClass_(String class_) {
			this.class_ = class_;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

	}
	
	/**
	 * http://code.google.com/apis/maps/documentation/geocoding/#GeocodingResponses
	 */
	public static final String URL = "http://nominatim.openstreetmap.org/search";

	/**
	 * Converts an address into a geographic coordinate. This function does not
	 * make use of multithreading for networking.
	 * 
	 * @param query A postal address
	 * @return A geographic coordinate (latitude, longitude)
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public static List<Address> lookup(String query) throws IOException, JSONException {
		String url = String.format("%s?q=%s&format=json", URL, URLEncoder.encode(query));
		Log.d("Geocoding", "url = " + url);
		
		HTTP http = new HTTP(url);
		http.connect();
		
		List<Address> addresses = new ArrayList<Address>();
		
		int responseCode = http.getResponseCode();
		if (responseCode == 200) {
			String response = http.getResponseBody();
			
			JSONArray array = new JSONArray(response);
			
			for (int i=0; i<array.length(); i++) {
				JSONObject object = (JSONObject) array.get(i);
				
				Address address = new Address();
				address.setLatitude(object.getDouble("lat"));
				address.setLongitude(object.getDouble("lon"));
				address.setName(object.getString("display_name"));
				address.setClass_(object.getString("class"));
				address.setType(object.getString("type"));
				
				addresses.add(address);
			}
		}
		
		return addresses;
	}
}
