package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.User;

public class UserLoginRequest extends FetchRequest<User> {
    
    private int id;
    
    private boolean newApi;
    
	/**
	 * 
	 * @param username
	 * @param password
	 * @param gcmRegistrationId Google Cloud Messaging registration ID
	 */
	public UserLoginRequest(String username, String password, String gcmRegistrationId) {
		super(
		    String.format("%s/V0.2/verifyaccount/username=%s&password=%s&deviceid=%s&flag=1",
			HOST,
			URLEncoder.encode(username),
			URLEncoder.encode(password),
			URLEncoder.encode(gcmRegistrationId))
		);
	}
	
    public UserLoginRequest(int id, String username, String password) {
        super(getLinkUrl(Link.auth_user) + "/" + id);
        this.username = username;
        this.password = password;
        this.id = id;
        newApi = true;
    }
	
	public User execute(Context ctx) throws IOException, JSONException {
	    String response = executeFetchRequest(getURL(), ctx).trim();
	    User user = null;
	    if(newApi){
	        JSONObject json  = new JSONObject(response);
	        JSONObject data = json.getJSONObject("data");
	        user = new User();
	        user.setId(id);
	        user.setUsername(username);
	        user.setPassword(password);
	        user.setEmail(data.getString("email"));
	        user.setFirstname(data.getString("first_name"));
	        user.setLastname(data.getString("last_name"));
	        user.setDeviceId(data.getString("device_id"));
	    }else{
	        // Since the server returns a JSON array for no apparent reason...
	        user = User.parse(response.substring(1, response.length()-1));
	    }
		return user;
	}
}
