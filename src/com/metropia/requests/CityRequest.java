package com.metropia.requests;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.metropia.activities.LandingActivity2;
import com.metropia.requests.CityRequest.City;
import com.metropia.tasks.ICallback;
import com.metropia.utils.HTTP;

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
        
        public String signUp;
        
        public String link;
        
    }
    
	public CityRequest(double lat, double lon, int timeoutInMillis) {
		super(getLinkUrl(Link.city)
	        .replaceAll("\\{lat\\}", String.valueOf(lat))
	        .replaceAll("\\{lon\\}", String.valueOf(lon))
        );
		this.timeout = timeoutInMillis;
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
		    city.signUp = data.optString("sign_up", "");
		}else{
		    city.html = json.optString("html");
		    city.link = json.optString("link", "");
		}
        return city;
	}
	
	public void executeAsync(final Context context, final ICallback cb) {
		new AsyncTask<Void, Void, City>(){

			@Override
			protected City doInBackground(Void... arg0) {
				City result;
                try{
                    invalidateCache(context);
                    result = CityRequest.this.execute(context);
                }catch(Throwable t){
                    result = null;
                }
                return result;
			}
			
			@Override
            protected void onPostExecute(City result) {
				if (cb!=null) cb.run(result);
			}
		}.execute();
		
		
	}

}
