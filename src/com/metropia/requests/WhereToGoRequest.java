package com.metropia.requests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.metropia.models.User;
import com.metropia.requests.WhereToGoRequest.Location;



public final class WhereToGoRequest extends FetchRequest<List<Location>> {

    public static class Location {
        
        public String label;
        
        public double lat;
        
        public double lon;
        
        public String addr;
        
    }
    
	public WhereToGoRequest(User user, double lat, double lon) {
        super(buildUrl(user.getId(), lat, lon));
        this.username = user.getUsername();
        this.password = user.getPassword();
    }
	
	@Override
	public List<Location> execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		List<Location> locs = new ArrayList<Location>();
	    JSONArray list = new JSONObject(response).getJSONArray("data");
        for(int j=0; j<list.length(); j++){
            JSONObject json = list.getJSONObject(j);
            Location l = new Location();
            l.addr = json.getString("addr");
            l.label = json.getString("label");
            l.lat = json.getDouble("lat");
            l.lon = json.getDouble("lon");
            locs.add(l);
        }
        return locs;
	}
	
	private static String buildUrl(int userId, double lat, double lon) {
		DateFormat YYYYMMDDHHMM = new SimpleDateFormat("yyyyMMddHHmm");
		YYYYMMDDHHMM.setTimeZone(TimeZone.getTimeZone("UTC"));
		return getLinkUrl(Link.where_to_go)
				.replaceAll("\\{lat\\}", String.valueOf(lat))
	            .replaceAll("\\{lon\\}", String.valueOf(lon))
	            .replaceAll("\\{user_id\\}", String.valueOf(userId))
	            .replaceAll("\\{YYYYmmddHHMM\\}", YYYYMMDDHHMM.format(new Date(System.currentTimeMillis())));
	}
	

}
