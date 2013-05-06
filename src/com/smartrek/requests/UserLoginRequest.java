package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONException;

import android.content.Context;

import com.smartrek.models.User;

public class UserLoginRequest extends FetchRequest<User> {
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @param gcmRegistrationId Google Cloud Messaging registration ID
	 */
	public UserLoginRequest(String username, String password, String gcmRegistrationId) {
		super(String.format("%s/V0.2/verifyaccount/username=%s&password=%s&deviceid=%s&flag=1",
				HOST,
				URLEncoder.encode(username),
				URLEncoder.encode(password),
				URLEncoder.encode(gcmRegistrationId)));
//	    super("http://static.suminb.com/smartrek/login.html");
	}
	
	public User execute(Context ctx) throws IOException, JSONException {
		String response = executeFetchRequest(getURL(), ctx).trim();
		
		// Since the server returns a JSON array for no apparent reason...
		return User.parse(response.substring(1, response.length()-1));
	}
}
