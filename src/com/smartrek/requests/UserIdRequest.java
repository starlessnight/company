package com.smartrek.requests;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

public final class UserIdRequest extends FetchRequest<Integer> {

    public static class Trekpoint {
        
        public long credit;
        
        public long lifeTimeCredit;
        
    }
    
	public UserIdRequest(String username) {
		super(getLinkUrl(Link.query_username).replaceAll("\\{username\\}", username));
	}
	
	@Override
	public Integer execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		JSONObject json  = new JSONObject(response);
		JSONArray data = json.getJSONArray("data");
		Integer id = null;
		if(data.length() > 0){
		    id = data.getJSONObject(0).getInt("id");
		}
        return id;
	}

}
