package com.smartrek.requests;

import java.util.HashMap;
import java.util.Map;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

public class TripValidationRequest extends Request {
	
    private long rid;
    
	public TripValidationRequest(User user, long rid) {
        url = getLinkUrl(Link.trip).replaceAll("\\{user_id\\}", String.valueOf(user.getId()));
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.rid = rid;
	}
	
	public void execute() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("reservation_id", String.valueOf(rid));
        executeHttpRequest(Method.POST, url, params);
	}
}