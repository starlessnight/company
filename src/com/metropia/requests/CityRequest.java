package com.metropia.requests;

import org.json.JSONObject;

import android.content.Context;

import com.metropia.requests.CityRequest.City;

public final class CityRequest extends FetchRequest<City> {
    
    public class City {
        
        public String name;
        
        public String temperatureUnit;
        
        public String logo;
        
        public double temperature;
        
        public String skyline; 
        
        public String html;
        
        public double maxLat;
        
        public double maxLon;
        
        public double minLat;
        
        public double minLon;
        
        public int timezone;
        
        public String incidents;
        
    }
    
	public CityRequest(double lat, double lon) {
		super(getLinkUrl(Link.city)
	        .replaceAll("\\{lat\\}", String.valueOf(lat))
	        .replaceAll("\\{lon\\}", String.valueOf(lon))
        );
	}
	
	public static final String NO_CITY_NAME = "noCityName";
	
	@Override
	public City execute(Context ctx) throws Exception {
	    City city = new City();
		String response = executeFetchRequest(getURL(), ctx);
		JSONObject json  = new JSONObject(response);
		if("success".equalsIgnoreCase(json.optString("status"))){
		    JSONObject data = json.getJSONObject("data");
		    city.logo = data.optString("logo");
		    city.name = data.optString("name", "noCityName");
		    city.skyline = data.optString("skyline");
		    city.temperature = data.optDouble("temperature");
		    city.temperatureUnit = data.optString("temperature_unit");
		    city.maxLat = data.getDouble("MaxLat");
		    city.minLat = data.getDouble("MinLat");
		    city.maxLon = data.getDouble("MaxLon");
		    city.minLon = data.getDouble("MinLon");
		    city.timezone = data.getInt("timezone");
		    city.incidents = data.optString("incidents");
		}else{
		    city.html = json.optString("html");
		}
        return city;
	}

}
