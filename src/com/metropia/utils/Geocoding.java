package com.metropia.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.metropia.models.User;
import com.metropia.requests.ReverseGeocodingRequest;
import com.metropia.requests.SearchAddressRequest;
import com.metropia.ui.NavigationView;


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
		
		private String address;
		
		private double distance = -1; 
		
		private String iconName;
		
		// for DebugOptionsActivity
		private Long inputTime = 0L;
		
		public static Address fromModelAddress(com.metropia.models.Address addr, Location userLoc) {
			Address address = new Address();
			address.setAddress(addr.getAddress());
			// old favorite without icon name, give default icon name "star"
			address.setIconName(StringUtils.isBlank(addr.getIconName()) || "null".equals(addr.getIconName()) ? "star" : addr.getIconName());
			address.setLatitude(addr.getLatitude());
			address.setLongitude(addr.getLongitude());
			address.setName(addr.getName());
			address.setDistance(-1);
			if(userLoc != null) {
				NumberFormat nf = new DecimalFormat("#.#");
	    		address.setDistance(Double.valueOf(nf.format(NavigationView.metersToMiles(
	    				RouteNode.distanceBetween(userLoc.getLatitude(), userLoc.getLongitude(), address.getLatitude(), address.getLongitude())))));
			}
			return address;
		}

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

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public double getDistance() {
			return distance;
		}

		public void setDistance(double distance) {
			this.distance = distance;
		}

		public String getIconName() {
			return iconName;
		}

		public void setIconName(String iconName) {
			this.iconName = iconName;
		}

		public Long getInputTime() {
			return inputTime;
		}

		public void setInputTime(Long inputTime) {
			this.inputTime = inputTime;
		}

	}
	
	/**
	 * http://code.google.com/apis/maps/documentation/geocoding/#GeocodingResponses
	 */
	public static final String GOOGLE_URL = "http://maps.googleapis.com/maps/api/geocode/json";
	
	public static final String URL = "http://nominatim.openstreetmap.org/search";
	
	public static final String DECARTA_URL = "http://api.decarta.com/v1/814192d44ada190313e7639881bf7226";
	

	/**
	 * Converts an address into a geographic coordinate. This function does not
	 * make use of multithreading for networking.
	 * 
	 * @param query A postal address
	 * @return A geographic coordinate (latitude, longitude)
	 * @throws IOException 
	 * @throws JSONException 
	 */
//	public static List<Address> lookup(String query) throws IOException, JSONException {
//	    return lookup(query, true);
//	}
	
	/**
	 * Converts an address into a geographic coordinate. This function does not
	 * make use of multithreading for networking.
	 * 
	 * @param query A postal address
	 * @param lat user current location lat
	 * @param lon user current location lon
	 * @return A geographic coordinate (latitude, longitude)
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public static List<Address> lookup(Context ctx, String query) throws Exception {
		return lookup(ctx, query, null, null);
	}
	
	public static List<Address> lookup(Context ctx, String query, Double lat, Double lon) throws Exception{
		User user = User.getCurrentUser(ctx);
		SearchAddressRequest request = new SearchAddressRequest(user, query, lat, lon, false);
//		SearchAddressRequest request = new SearchAddressRequest(user, query, null, null);
		List<Address> result = request.execute(ctx);
		
		List<Address> addresses = new ArrayList<Address>();
		if(!result.isEmpty()) {
			addresses.add(result.get(0));
		}
		return addresses;
	}
	 
	public static List<Address> lookup(String query, boolean usOnly) throws IOException, JSONException {
        /*String url = String.format("%s?q=%s&format=json", URL, URLEncoder.encode(
            replaceLaInitials(query)));
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
        */
	    
	    List<Address> addresses = new ArrayList<Address>();
        //if(addresses.isEmpty()){
            GeoPoint gp = decartaLookup(query, usOnly);
            Address address = new Address();
            address.setLatitude(gp.getLatitude());
            address.setLongitude(gp.getLongitude());
            addresses.add(address);
        //}
        
        return addresses;
    }
	
	private static String removeZipCodes(String address){
	    return address.replaceAll("(?<=\\S[,\\s]{1,10})([0-9]{5}(-[0-9]{4})?)(?=([,\\s]{1,10}($|\\S)|$))", "");
	}
	
	private static String replaceLaInitials(String address){
	    return address.replaceAll("(?i)(?<=(^|(^|\\S)[,\\s]{1,10}))la(?=([,\\s]{1,10}($|\\S)|$))", "los angeles");
	}
	
	private static String removeStreets(String address){
        return address.replaceAll("(?i)(?<=(^|(^|\\S)[,\\s]{1,10}))((st\\.?)|(street))(?=([,\\s]{1,10}($|\\S)|$))", "");
    }
	
    private static GeoPoint googleLookup(String address, boolean usOnly) throws IOException, JSONException {
        String url = String.format("%s?address=%s&sensor=false" + (usOnly?"&components=country:us":""), GOOGLE_URL, URLEncoder.encode(address));
        Log.d("Geocoding", "url = " + url);
        
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
    
    private static GeoPoint decartaLookup(String address, boolean usOnly) throws IOException, JSONException {
    	String url = String.format("%s/geocode/%s.json?limit=1" + (usOnly?"&countrySet=US":""), DECARTA_URL, URLEncoder.encode(address));
    	Log.d("Geocoding", "url = " + url);
    	
    	double lat = 0.0;
        double lng = 0.0;
        
        HTTP http = new HTTP(url);
        http.connect();
        
        int responseCode = http.getResponseCode();
        if (responseCode == 200) {
            String response = http.getResponseBody();
            JSONObject object = new JSONObject(response);
            JSONObject summary = object.getJSONObject("summary");
            // if status == "OK"
            if(summary.getInt("numResults") > 0) {
                JSONArray results = (JSONArray) object.get("results");
                if(results.length() > 0) {
                    JSONObject result = (JSONObject) results.get(0);
                    JSONObject position = result.getJSONObject("position");
                
                    lat = position.getDouble("lat");
                    lng = position.getDouble("lon");
                }
            }
        }
        
        return new GeoPoint(lat, lng);
    }
    
    public static String lookup(Context ctx, double lat, double lon) throws Exception {
    	/*
        String url = String.format("%s?latlng=%f,%f&language=en&sensor=false", GOOGLE_URL, lat, lon);
        Log.d("Geocoding", "url = " + url);
        
        HTTP http = new HTTP(url);
        http.connect();
        
        String address = null;
        
        int responseCode = http.getResponseCode();
        if (responseCode == 200) {
            String response = http.getResponseBody();
            
            JSONObject object = new JSONObject(response);
            
            // if status == "OK"
            if("OK".equals(object.get("status"))) {
                JSONArray results = (JSONArray) object.get("results");
                if(results.length() > 0) {
                    JSONObject result = (JSONObject) results.get(0);
                    address = result.getString("formatted_address");
                }
            }
        }
        
        return address;
        */
    	User user = User.getCurrentUser(ctx);
        ReverseGeocodingRequest request = new ReverseGeocodingRequest(user, lat, lon);
        String address = request.execute(ctx);
        return address;
    }
    
    public static List<Address> searchPoi(Context ctx, String address) throws Exception {
    	return searchPoi(ctx, address, null, null);
    }
    
    public static List<Address> searchPoi(Context ctx, String query, Double lat, Double lon) throws Exception{
    	User user = User.getCurrentUser(ctx);
		SearchAddressRequest request = new SearchAddressRequest(user, query, lat, lon, false);
		return request.execute(ctx);
		
    }
    
    public static List<Address> searchPoiForCalendar(Context ctx, String query, Double lat, Double lon) {
    	try {
	    	User user = User.getCurrentUser(ctx);
			SearchAddressRequest request = new SearchAddressRequest(user, query, lat, lon, true);
			return request.execute(ctx);
    	}
    	catch(Exception ignore) {
    		return new ArrayList<Address>();
    	}
    }
    
}
