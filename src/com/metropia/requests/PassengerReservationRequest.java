package com.metropia.requests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.metropia.exceptions.ServiceFailException;
import com.metropia.exceptions.WrappedIOException;
import com.metropia.models.User;
import com.metropia.utils.HTTP.Method;

public class PassengerReservationRequest extends Request {
	
	private String id;
	private User user;
	private String departureTime;
	private String version;
	
	public PassengerReservationRequest(User user, String version) {
		Date now = new Date(System.currentTimeMillis());
		DateFormat idDf = new SimpleDateFormat("yyyyMMddHHmm");
		idDf.setTimeZone(TimeZone.getTimeZone("GMT"));
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		id = idDf.format(now);
		departureTime = dateFormat.format(now); 
		this.user = user;
		this.version = version;
		url = getLinkUrl(Link.passenger_reservation);
	}
	
	public Long execute(Context ctx) throws Exception {
		this.username = user.getUsername();
        this.password = user.getPassword();
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("origin", "");
        params.put("destination", "");
        params.put("start_datetime", departureTime);
        params.put("estimated_travel_time", 0);
        params.put("route", new JSONArray("[]"));
        params.put("app_version", version);
        params.put("validated", 0);
        params.put("navigation_url", "");
        params.put("trajectory_fields", "lat,lon,altitude,heading,timestamp,speed,link,accuracy");
        
        String res = null;
        boolean throwException = false;
        try {
            res = executeHttpRequest(Method.POST, url, params, ctx);
        } catch (Exception e){
        	throwException = true;
        	if(e instanceof WrappedIOException) {
        		res = ((WrappedIOException)e).getDetailMessage();
        	}
        	else {
        		res = e.getMessage();
        	}
        }
        
        JSONObject resJson = new JSONObject(res);
        JSONObject json;
        if(throwException) {
        	json = new JSONObject(resJson.optString(RESPONSE, ""));
        }
        else {
          	json = resJson;
        }
        Long reservId;    
        if("fail".equals(json.getString("status"))){
            throw new ServiceFailException("", resJson.optString(ERROR_MESSAGE, ""));
        }else{
        	JSONObject data = json.getJSONObject("data");
            reservId = data.getLong("id");
        }
        return reservId;
	}
}
