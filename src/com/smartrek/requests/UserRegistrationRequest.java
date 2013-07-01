package com.smartrek.requests;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

public class UserRegistrationRequest extends Request {

	public void execute(User user) throws Exception {
	    String username = user.getUsername();
        String password = user.getPassword();
        String email = user.getEmail();
        String firstname = user.getFirstname();
        String lastname = user.getLastname();
		if(NEW_API){
		    String url = getLinkUrl(Link.auth_user);
		    Map<String, String> params = new HashMap<String, String>();
		    params.put("username", username);
		    params.put("password", password);
		    params.put("email", email);
		    params.put("first_name", firstname);
		    params.put("last_name", lastname);
		    String res = null;
            try {
                res = executeHttpRequest(Method.POST, url, params);
            } catch (Exception e){
                res = e.getMessage();
            }
            JSONObject json = new JSONObject(res);
            JSONObject data = json.getJSONObject("data"); 
            if("fail".equals(json.getString("status"))){
                String msg = "";
                Iterator keys = data.keys();
                while(keys.hasNext()){
                    Object attr = keys.next();
                    msg += (msg.length() == 0?"":".\n") + attr +  ": " + data.getString(attr.toString());
                }
                throw new Exception(msg);
            }else{
                user.setId(data.getInt("id"));
            }
		}else{
            String url = String.format("%s/adduser/username=%s&password=%s&email=%s&firstname=%s&lastname=%s",
	                HOST,
	                URLEncoder.encode(username),
	                URLEncoder.encode(password),
	                URLEncoder.encode(email),
	                URLEncoder.encode(firstname),
	                URLEncoder.encode(lastname));
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
}
