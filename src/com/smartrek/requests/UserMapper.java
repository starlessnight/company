package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

import android.util.Log;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP;


/****************************************************************************************************
 * 
 * 
 * @author timothyolivas
 * @deprecated
 *
 ****************************************************************************************************/
public class UserMapper extends Request {
	
	public UserMapper() {
		super();
	}
	
	public void register(User user) throws IOException {
		String url = String.format("%s/adduser/username=%s&password=%s&email=%s&firstname=%s&lastname=%s",
				HOST,
				URLEncoder.encode(user.getUsername()),
				URLEncoder.encode(user.getPassword()),
				URLEncoder.encode(user.getEmail()),
				URLEncoder.encode(user.getFirstname()),
				URLEncoder.encode(user.getLastname()));
		
		HTTP http = new HTTP(url);
		http.connect();
		
		int responseCode = http.getResponseCode();
		if (responseCode == 200) {
			Log.d("UserMapper", http.getResponseBody());
		}
		else {
			throw new IOException(String.format("HTTP %d: %s", responseCode, http.getResponseBody()));
		}
	}
}
