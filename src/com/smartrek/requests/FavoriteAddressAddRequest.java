package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

public class FavoriteAddressAddRequest extends Request {
	
	private User user;
	private String name;
	private String address;
	private double lat;
	private double lon;
	
	public FavoriteAddressAddRequest(User user, String name, String address, double lat, double lon) {
		this.user = user;
		this.name = name;
		this.address = address;
		this.lat = lat;
		this.lon = lon;
	}
	
	public int execute(Context ctx) throws IOException, JSONException, InterruptedException {
		if(NEW_API){
		    this.username = user.getUsername();
		    this.password = user.getPassword();
		    url = getLinkUrl(Link.address);
		    Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", String.valueOf(user.getId()));
            params.put("name", StringUtils.substring(name, 0, 30));
            params.put("address", StringUtils.substring(address, 0, 80));
            params.put("lat", String.format("%.7f", lat));
            params.put("lon", String.format("%.7f", lon));
            return new JSONObject(executeHttpRequest(Method.POST, url, params, ctx)).getJSONObject("data").getInt("id");
		}else{
		    String url = String.format("%s/V0.2/addfavadd/?UID=%d&NAME=%s&ADDRESS=%s&lat=%.7f&lon=%.7f",
	                HOST, user.getId(), URLEncoder.encode(name), URLEncoder.encode(address), lat, lon);
		    executeHttpGetRequest(url, ctx);
		    return 0;
		}
	}
	
}
