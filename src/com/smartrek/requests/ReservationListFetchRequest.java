package com.smartrek.requests;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.utils.Cache;

public class ReservationListFetchRequest extends FetchRequest<List<Reservation>> {
	
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
                Reservation r = new Reservation();
                r.setRid(object.getLong("id"));
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone(Request.TIME_ZONE));
                long departureTime = dateFormat.parse(object.getString("start_datetime")).getTime();
                r.setDepartureTime(departureTime);

                // travel duration
                r.setDuration(object.getInt("estimated_travel_time") * 60);
                
                r.setOriginAddress(object.getString("origin"));
                r.setDestinationAddress(object.getString("destination"));
                r.setCredits(object.optInt("credit"));
                r.setValidatedFlag(object.optInt("validated"));
                r.setRoute(Route.parse(object, departureTime, true));
                
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
	        Date now = new Date();
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
	        dateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
	        url = getLinkUrl(Link.query_upcoming_reservation).replaceAll("\\{YYYYmmddHHMM\\}", dateFormat.format(now)); 
	    }else{
	        url = String.format("%s/getreservations/%d", FetchRequest.HOST, user.getId());
	    }
	    return url; 
	}

}
