package com.metropia.requests;

import org.json.JSONObject;

import android.content.Context;

import com.metropia.models.User;
import com.metropia.utils.HTTP.Method;

public class DuoTripCheckRequest extends Request {
	
	public DuoTripCheckRequest(User user) {
		this.username = user.getUsername();
		this.password = user.getPassword();
		
		url = getLinkUrl(Link.query_DUO).replaceAll("\\{user_id\\}", String.valueOf(user.getId()));
	}
	
	public int execute(Context ctx) throws Exception {
        String str = executeHttpRequest(Method.GET, url, ctx);
        
        JSONObject json = new JSONObject(str);
        int timeToNext = json.getJSONObject("data").getInt("time_to_next");
        return timeToNext;
    }
	
}
