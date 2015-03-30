package com.metropia.requests;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.metropia.utils.HTTP.Method;

public class UpdateDeviceIdRequest extends Request {

	public void execute(int userId, String deviceId, String username,
           String password, Context ctx, String versionNumber) throws Exception {
		String url = getLinkUrl(Link.auth_user) + "/" + userId;
	    Map<String, String> params = new HashMap<String, String>();
	    params.put("device_id", deviceId);
	    params.put("app_version", versionNumber);
	    this.username = username;
	    this.password = password;
        executeHttpRequest(Method.PUT, url, params, ctx);
	}
	
}
