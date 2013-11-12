package com.smartrek.requests;

import org.json.JSONObject;

import android.content.Context;

public final class CityRequest extends FetchRequest<String> {
    
	public CityRequest(double lat, double lon) {
		super(getLinkUrl(Link.city)
	        .replaceAll("\\{lat\\}", String.valueOf(lat))
	        .replaceAll("\\{lon\\}", String.valueOf(lon))
        );
	}
	
	@Override
	public String execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		JSONObject json  = new JSONObject(response);
		String msg;
		if("success".equalsIgnoreCase(json.optString("status"))){
		    msg = null;
		}else{
		    msg = json.optString("html");
		}
        return msg;
	}

}
