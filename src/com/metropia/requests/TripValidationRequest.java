package com.metropia.requests;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.metropia.activities.CongratulationActivity;
import com.metropia.activities.PassengerActivity;
import com.metropia.activities.ValidationActivity;
import com.metropia.exceptions.SmarTrekException;
import com.metropia.models.User;
import com.metropia.utils.Misc;
import com.metropia.utils.HTTP.Method;
import com.metropia.activities.R;

public class TripValidationRequest extends Request {
	
    private long rid;
    
	public TripValidationRequest(User user, long rid, String mode) {
		Link apiLink = PassengerActivity.PASSENGER_TRIP_VALIDATOR.equals(mode)? Link.passenger_trip:Link.trip;
        url = getLinkUrl(apiLink).replaceAll("\\{user_id\\}", String.valueOf(user.getId()));
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.rid = rid;
	}
	
	public JSONObject execute(Context ctx) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("reservation_id", String.valueOf(rid));
        try{
            String res = executeHttpRequest(Method.POST, url, params, ctx);
            JSONObject json = new JSONObject(res);
            return json;
        }catch(Exception e){
            Log.w("TripValidationRequest", Log.getStackTraceString(e));
            if(responseCode >= 400 && responseCode <= 499){
                throw new SmarTrekException(responseCode);
            }else{
            	throw e;
            }
        }
	}
	
	public JSONObject executeImd(Context ctx) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("reservation_id", String.valueOf(rid));
        timeout = fifteenSecsTimeout;
        JSONObject json = null;
        try{
            String res = executeHttpRequest(Method.POST, url, params, ctx);
            json = new JSONObject(res);
        }catch(Exception e){
            if(responseCode >= 400 && responseCode <= 499){
                throw new SmarTrekException(responseCode);
            }else{
            	throw e;
            }
        }
        return json;
	}
	
	
}
