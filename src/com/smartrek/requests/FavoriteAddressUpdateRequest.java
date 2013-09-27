package com.smartrek.requests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;


public final class FavoriteAddressUpdateRequest extends UpdateRequest {

    private User user;
    private String name;
    private String address;
    private double lat;
    private double lon;
    
	/**
	 * 
	 * @param aid Address ID
	 * @param uid User ID
	 * @param name Address name
	 * @param address Postal address
	 * @param latitude
	 * @param longitude
	 * @throws UnsupportedEncodingException 
	 */
	public FavoriteAddressUpdateRequest(String link, int aid, User user, String name, String address, double latitude, double longitude) throws UnsupportedEncodingException {
		super(NEW_API?
		    link.replaceAll("\\{id\\}", String.valueOf(aid))
	        :
	        String.format("%s/updatefavadd/?fid=%d&uid=%d&name=%s&address=%s&lat=%.7f&lon=%.7f",
			HOST, aid, user.getId(), URLEncoder.encode(name, "UTF-8"), URLEncoder.encode(address, "UTF-8"), latitude, longitude)
        );
		if(NEW_API){
		    this.user = user;
	        this.name = name;
	        this.address = address;
	        this.lat = latitude;
	        this.lon = longitude;
		}
	}
	
	public void execute() throws IOException, JSONException {
	    if(NEW_API){
	        this.username = user.getUsername();
            this.password = user.getPassword();
            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", String.valueOf(user.getId()));
            params.put("name", name);
            params.put("address", address);
            params.put("lat", String.format("%.7f", lat));
            params.put("lon", String.format("%.7f", lon));
            executeHttpRequest(Method.PUT, url, params);
	    }else{
	        executeUpdateRequest(url);
	    }
	}
}
