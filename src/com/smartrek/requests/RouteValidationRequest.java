package com.smartrek.requests;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartrek.exceptions.SmarTrekException;

/**
 * Marks a reserved trip as validated
 *
 */
public class RouteValidationRequest extends Request {
	
	public RouteValidationRequest(int uid, int rid) {
		url = String.format("%s/validationdone/?uid=%d&rid=%d", HOST, uid, rid);
	}
	
	public void execute() throws Exception {
		String res = executeHttpGetRequest(url);
        JSONObject json = new JSONArray(res).getJSONObject(0);
        if("FAILED".equalsIgnoreCase(json.getString("STATUS"))){
            throw new SmarTrekException(json.getString("MESSAGE"));
        }
	}
}
