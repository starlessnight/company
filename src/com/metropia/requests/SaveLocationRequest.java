package com.metropia.requests;

import android.content.Context;

import com.metropia.models.User;
import com.metropia.utils.HTTP.Method;

public class SaveLocationRequest extends Request {
	
	public SaveLocationRequest(User user) {
		this.username = user.getUsername();
		this.password = user.getPassword();
	}
	
	public void execute(Context ctx, double lat, double lon, boolean firstLogin) throws Exception {
		String apiUrl = getLinkUrl(Link.savelocation);
		url = apiUrl.replaceAll("\\{lat\\}", Double.toString(lat)).replaceAll("\\{lon\\}", Double.toString(lon)).replaceAll("\\{lon\\}", firstLogin? "1":"2");
		
        executeHttpRequest(Method.GET, url, ctx);
    }
	
}
