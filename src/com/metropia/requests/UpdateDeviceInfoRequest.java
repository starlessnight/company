package com.metropia.requests;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.metropia.models.User;
import com.metropia.utils.HTTP.Method;

public class UpdateDeviceInfoRequest extends Request {
	
	User user;
	
	public UpdateDeviceInfoRequest(User user) {
		url = getLinkUrl(Link.auth_user) + "/" + user.getId();

		this.user = user;
	    this.username = user.getUsername();
	    this.password = user.getPassword();
	}

	public void execute(Context ctx, String deviceId) throws Exception {
		
		String appVersion = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
		String osVersion = "android " + android.os.Build.VERSION.RELEASE;
		String brandname = android.os.Build.BRAND;
		String model = android.os.Build.MODEL;
		String mobileProvider = ((TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName();
		
	    Map<String, String> params = new HashMap<String, String>();
	    params.put("device_id", deviceId);
	    params.put("metropia_version", appVersion);
	    params.put("os_version", osVersion);
	    params.put("brandname", brandname);
	    params.put("model", model);
	    params.put("mobile_browser", mobileProvider);
        executeHttpRequest(Method.PUT, url, params, ctx);
        
        user.setAppVersion(appVersion);
        user.setDeviceId(deviceId);
	}
	
}
