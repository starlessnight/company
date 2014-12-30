package com.metropia.requests;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.metropia.models.User;
import com.metropia.utils.HTTP.Method;

public class UserRegistrationRequest extends Request {

	public void execute(User user, Context ctx) throws Exception {
	    String username = user.getUsername();
        String password = user.getPassword();
        String email = user.getEmail();
        String firstname = user.getFirstname();
        String lastname = user.getLastname();
        String zipcode = user.getZipCode();
		if(NEW_API){
		    String url = getLinkUrl(Link.auth_user);
		    Map<String, String> params = new HashMap<String, String>();
		    params.put("username", StringUtils.lowerCase(email));
		    params.put("password", password);
		    params.put("email", StringUtils.lowerCase(email));
		    params.put("first_name", firstname);
		    params.put("last_name", lastname);
		    params.put("zipcode", zipcode);
		    String res = null;
		    boolean throwException = false;
            try {
                res = executeHttpRequest(Method.POST, url, params, ctx);
            } catch (Exception e){
            	throwException = true;
                res = e.getMessage();
            }
            JSONObject resJson = new JSONObject(res);
            if(throwException) {
            	resJson = new JSONObject(resJson.optString(RESPONSE, ""));
            }
            JSONObject data = resJson.getJSONObject("data"); 
            if("fail".equals(resJson.getString("status"))){
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
            String url = String.format("%s/adduser/username=%s&password=%s&email=%s&firstname=%s&lastname=%s&zipcode=%s",
	                HOST,
	                URLEncoder.encode(email),
	                URLEncoder.encode(password),
	                URLEncoder.encode(email),
	                URLEncoder.encode(firstname),
	                URLEncoder.encode(lastname), 
	                URLEncoder.encode(zipcode));
		    String res = executeHttpGetRequest(url, ctx);
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
