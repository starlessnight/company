package com.smartrek.requests;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smartrek.models.User;

public class UserRegistrationRequest extends Request {

	public void execute(User user) throws Exception {
		String url = String.format("%s/adduser/username=%s&password=%s&email=%s&firstname=%s&lastname=%s",
				HOST,
				URLEncoder.encode(user.getUsername()),
				URLEncoder.encode(user.getPassword()),
				URLEncoder.encode(user.getEmail()),
				URLEncoder.encode(user.getFirstname()),
				URLEncoder.encode(user.getLastname()));
		
		String res = executeHttpGetRequest(url);
		try {
            JSONObject json = new JSONArray(res).getJSONObject(0);
            if("FAILED".equals(json.getString("STATUS"))){
                throw new Exception(json.getString("MESSAGE"));
            }else{
                user.setId(json.getInt("UID"));
            }
        }
        catch (JSONException e) {
        }
	}
}
