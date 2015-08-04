package com.metropia.requests;

import org.json.JSONObject;

import android.content.Context;

import com.metropia.models.User;

public class UserFBLoginRequest extends FetchRequest<User> {

	public UserFBLoginRequest(String accessToken) {
        super(getLinkUrl(Link.fb_login).replace("{access_token}", accessToken));
	}

	@Override
	public User execute(Context ctx) throws Exception {
		User user = new User();
		String response = executeFetchRequest(getURL(), ctx).trim();
		JSONObject responseJSON = new JSONObject(response);
		
		user.setUsername(responseJSON.getJSONObject("data").getString("username"));
		user.setPassword(responseJSON.getJSONObject("data").getString("password"));
		return user;
	}
}
