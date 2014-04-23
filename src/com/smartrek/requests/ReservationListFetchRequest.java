package com.smartrek.requests;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.utils.Cache;

public class ReservationListFetchRequest extends FetchRequest<List<Reservation>> {
	
    private static String start_datetime_attr = "start_datetime";
    
    private static String start_datetime_utc_attr = "start_datetime_utc";
    
	public ReservationListFetchRequest(User user) {
		super(buildUrl(user));
		if(NEW_API){
		    username = user.getUsername();
		    password = user.getPassword();
		}
	}

	@Override
	public List<Reservation> execute(Context ctx) throws Exception {
	    // FIXME: Not going to use cache for now
	    Cache.getInstance(ctx).clear();
	    
		String response = executeFetchRequest(getURL(), ctx);
		
		List<Reservation> reservations = new ArrayList<Reservation>();
		if(NEW_API){
		    JSONArray array = new JSONObject(response).getJSONArray("data");
            for(int i = 0; i < array.length(); i++) {
                JSONObject object = (JSONObject) array.get(i);
                Reservation r = parse(object);
                
                reservations.add(r);
            }
		}else{
            JSONArray array = new JSONArray(response.replaceAll("\"DISTANCE\":,", "\"DISTANCE\":0,"));
            for (int i = 0; i < array.length(); i++) {
                Reservation r = Reservation.parse(new JSONObject(array.get(i).toString()));
                reservations.add(r);
            }
		}
        
		return reservations;
	}
	
	private static String buildUrl(User user){
	    String url;
	    if(NEW_API){
	        Date now = new Date(System.currentTimeMillis() - 15*60*1000);
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
	        dateFormat.setTimeZone(TimeZone.getTimeZone(getTimeZone()));
	        url = getLinkUrl(Link.query_upcoming_reservation).replaceAll("\\{YYYYmmddHHMM\\}", dateFormat.format(now));
	        if(!StringUtils.contains(url, start_datetime_utc_attr)){
	            url = url.replaceAll(start_datetime_attr, start_datetime_attr + "," + start_datetime_utc_attr); 
	        }
	    }else{
	        url = String.format("%s/getreservations/%d", FetchRequest.HOST, user.getId());
	    }
	    return url; 
	}
	
	static Reservation parse(JSONObject object) throws JSONException, ParseException, IOException {
        Reservation r = new Reservation();
        String id = object.getString("id");
        String separator = "_";
        r.setRid(Long.parseLong(StringUtils.contains(id, separator)?
            StringUtils.substringAfter(id, separator):id));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone(Request.getTimeZone()));
        long departureTime = dateFormat.parse(object.getString(start_datetime_attr)).getTime();
        r.setDepartureTime(departureTime);
        
        SimpleDateFormat dateFormatUtc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormatUtc.setTimeZone(TimeZone.getTimeZone(UTC_TIMEZONE));
        long departureTimeUtc = dateFormatUtc.parse(object.getString(start_datetime_utc_attr)).getTime();
        r.setDepartureTimeUtc(departureTimeUtc);

        // travel duration
        r.setDuration(object.getInt("estimated_travel_time") * 60);
        
        r.setOriginAddress(object.optString("origin"));
        r.setDestinationAddress(object.optString("destination"));
        r.setCredits(object.optInt("credit"));
        r.setMpoint(object.optInt("mpoint"));
        r.setValidatedFlag(object.optInt("validated"));
        r.setRoute(Route.parse(object, departureTime));
        r.setNavLink(object.optString("navigation_url"));
        r.setEndlat(object.optDouble("endlat", 0));
        r.setEndlon(object.optDouble("endlon", 0));
        return r;
	}

}
