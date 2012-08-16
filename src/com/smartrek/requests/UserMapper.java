package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONException;

import smartrek.parsers.Parser;
import android.util.Log;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP;


/****************************************************************************************************
 * 
 * 
 * @author timothyolivas
 *
 ****************************************************************************************************/
public class UserMapper extends Request {
	
	// values are database field names
	public static final String UID = "UID";
	public static final String USERNAME = "USERNAME";
	public static final String FIRSTNAME = "FIRSTNAME";
	public static final String LASTNAME = "LASTNAME";
	public static final String EMAIL = "EMAIL";
	
	public UserMapper() {
		super();
	}
	
	public User login(String name, String pwd) throws JSONException, IOException {
		String loginurl = String.format("%s/account/%s%%20%s", HOST, name, pwd);

		HTTP http = new HTTP(loginurl);
		http.connect();
		
		int responseCode = http.getResponseCode();
		if (responseCode == 200) {
			String jsonString = http.getResponseBody();
			User user = Parser.parse_User(name, jsonString);
			
			return user;
		}
		else {
			throw new IOException(String.format("HTTP %d: %s", responseCode, http.getResponseBody()));
		}
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
