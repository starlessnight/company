package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

import com.smartrek.models.User;

public class UserRegistrationRequest extends Request {

	public void execute(User user) throws IOException {
		String url = String.format("%s/adduser/username=%s&password=%s&email=%s&firstname=%s&lastname=%s",
				HOST,
				URLEncoder.encode(user.getUsername()),
				URLEncoder.encode(user.getPassword()),
				URLEncoder.encode(user.getEmail()),
				URLEncoder.encode(user.getFirstname()),
				URLEncoder.encode(user.getLastname()));
		
		executeHttpGetRequest(url);
	}
}
