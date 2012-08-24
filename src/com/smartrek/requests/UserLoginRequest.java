package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONException;


import com.smartrek.models.User;

public class UserLoginRequest extends FetchRequest<User> {

	public UserLoginRequest(String username, String password) {
		super(String.format("%s/verifyaccount/username=%s&password=%s",
				HOST, URLEncoder.encode(username), URLEncoder.encode(password)));
	}
	
	public User execute() throws IOException, JSONException {
		String response = executeFetchRequest(url).trim();
		
		// Since the server returns a JSON array for no apparent reason...
		return User.parse(response.substring(1, response.length()-1));
	}
}
