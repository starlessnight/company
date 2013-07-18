package com.smartrek.requests;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartrek.exceptions.SmarTrekException;
import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

/**
 * Marks a reserved trip as validated
 *
 */
public class RouteValidationRequest extends Request {
	
	public RouteValidationRequest(User user, int rid) {
	    if(NEW_API){
	        url = getLinkUrl(Link.reservation) + "/" + rid;
	        this.username = user.getUsername();
	        this.password = user.getPassword();
	    }else{
	        url = String.format("%s/validationdone/?uid=%d&rid=%d", HOST, user.getId(), rid);
	    }
	}
	
	public void execute() throws Exception {
	    if(NEW_API){
	        Map<String, String> params = new HashMap<String, String>();
	        params.put("validated", String.valueOf(1));
	        executeHttpRequest(Method.PUT, url, params);
	    }else{
	        String res = executeHttpGetRequest(url);
	        JSONObject json = new JSONArray(res).getJSONObject(0);
	        if("FAILED".equalsIgnoreCase(json.getString("STATUS"))){
	            throw new SmarTrekException(json.getString("MESSAGE"));
	        }
	    }
	}
}
