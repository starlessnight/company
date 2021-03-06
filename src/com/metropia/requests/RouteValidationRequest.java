package com.metropia.requests;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.metropia.exceptions.SmarTrekException;
import com.metropia.models.User;
import com.metropia.utils.HTTP.Method;

/**
 * Marks a reserved trip as validated
 *
 */
public class RouteValidationRequest extends Request {
	
    private long rid;
    
	public RouteValidationRequest(User user, long rid) {
	    if(NEW_API){
	        url = getLinkUrl(Link.claim).replaceAll("\\{user_id\\}", String.valueOf(user.getId()));
	        this.username = user.getUsername();
	        this.password = user.getPassword();
	        this.rid = rid;
	    }else{
	        url = String.format("%s/validationdone/?uid=%d&rid=%d", HOST, user.getId(), rid);
	    }
	}
	
	public void execute(Context ctx) throws Exception {
	    if(NEW_API){
	        Map<String, String> params = new HashMap<String, String>();
	        params.put("reservation_id", String.valueOf(rid));
	        try{
	            executeHttpRequest(Method.POST, url, params, ctx);
	        }catch(Exception e){
	            if(responseCode >= 400 && responseCode <= 499){
	                throw new SmarTrekException(responseCode);
	            }else{
	                throw e;
	            }
	        }
	    }else{
	        String res = executeHttpGetRequest(url, ctx);
	        JSONObject json = new JSONArray(res).getJSONObject(0);
	        if("FAILED".equalsIgnoreCase(json.getString("STATUS"))){
	            throw new SmarTrekException(json.getString("MESSAGE"));
	        }
	    }
	}
}
