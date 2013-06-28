package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

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
	
	public void execute() throws IOException {
		if(NEW_API){
		    this.username = user.getUsername();
		    this.password = user.getPassword();
		    String url = getLinkUrl(Link.address);
		    Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", String.valueOf(user.getId()));
            params.put("name", name);
            params.put("address", address);
            params.put("lat", String.format("%.7f", lat));
            params.put("lon", String.format("%.7f", lon));
		    executeHttpRequest(Method.POST, url, params);
		}else{
		    String url = String.format("%s/V0.2/addfavadd/?UID=%d&NAME=%s&ADDRESS=%s&lat=%.7f&lon=%.7f",
	                HOST, user.getId(), URLEncoder.encode(name), URLEncoder.encode(address), lat, lon);
		    executeHttpGetRequest(url);
		}
	}
	
}
