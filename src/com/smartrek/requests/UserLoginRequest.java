package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONException;

import com.smartrek.models.User;

public class UserLoginRequest extends FetchRequest<User> {

	public UserLoginRequest(String username, String password) {
		super(String.format("%s/verifyaccount/username=%s&password=%s",
				HOST, URLEncoder.encode(username), URLEncoder.encode(password)));
//	    super("http://static.suminb.com/smartrek/login.html");
	}
	
	public User execute() throws IOException, JSONException {
		String response = executeFetchRequest(getURL()).trim();
		
		// Since the server returns a JSON array for no apparent reason...
		return User.parse(response.substring(1, response.length()-1));
	}
}
