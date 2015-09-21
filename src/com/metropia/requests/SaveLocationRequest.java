package com.metropia.requests;

import android.content.Context;

import com.metropia.models.User;
import com.metropia.utils.HTTP.Method;

public class SaveLocationRequest extends Request {
	
	public SaveLocationRequest(User user) {
		this.username = user.getUsername();
		this.password = user.getPassword();
	}
	
	public void execute(Context ctx, double lat, double lon) throws Exception {
		String apiUrl = "https://sandbox.metropia.com/dev_v1/savelocation/SaveLocation.json?lat={lat}&lon={lon}";
		url = apiUrl.replaceAll("\\{lat\\}", Double.toString(lat)).replaceAll("\\{lon\\}", Double.toString(lon));
		
        executeHttpRequest(Method.GET, url, ctx);
    }
	
}
