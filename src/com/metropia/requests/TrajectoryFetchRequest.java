package com.metropia.requests;

import org.json.JSONObject;

import android.content.Context;

public final class TrajectoryFetchRequest extends FetchRequest<JSONObject> {
  
    
    public TrajectoryFetchRequest(String url) {
        super(url);
        username = "metropian";
        password = "only4metropians";
    }
	
	@Override
	public JSONObject execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		return new JSONObject(response);
	}

}
