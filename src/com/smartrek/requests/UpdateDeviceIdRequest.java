package com.smartrek.requests;

import java.util.HashMap;
import java.util.Map;

import com.smartrek.utils.HTTP.Method;

public class UpdateDeviceIdRequest extends Request {

	public void execute(int userId, String deviceId, String username,
           String password) throws Exception {
		String url = getLinkUrl(Link.auth_user) + "/" + userId;
	    Map<String, String> params = new HashMap<String, String>();
	    params.put("device_id", deviceId);
	    this.username = username;
	    this.password = password;
        executeHttpRequest(Method.PUT, url, params);
	}
	
}
