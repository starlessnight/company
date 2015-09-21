package com.metropia.requests;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.metropia.models.User;
import com.metropia.utils.HTTP.Method;

public class UpdateDeviceIdRequest extends Request {
	
	public UpdateDeviceIdRequest(User user) {
		url = getLinkUrl(Link.auth_user) + "/" + user.getId();

	    this.username = user.getUsername();
	    this.password = user.getPassword();
	}

	public void execute(Context ctx, String deviceId, String versionNumber) throws Exception {
	    Map<String, String> params = new HashMap<String, String>();
	    params.put("device_id", deviceId);
	    params.put("app_version", versionNumber);
        executeHttpRequest(Method.PUT, url, params, ctx);
	}
	
}
