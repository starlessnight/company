package com.metropia.requests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import android.content.Context;

import com.metropia.models.User;
import com.metropia.utils.HTTP.Method;


public final class FavoriteAddressUpdateRequest extends UpdateRequest {

    private User user;
    private String name;
    private String address;
    private double lat;
    private double lon;
    private String icon;
    
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
	public FavoriteAddressUpdateRequest(String link, int aid, User user, String name, String address, String icon, double latitude, double longitude) throws UnsupportedEncodingException {
		super(NEW_API?
		    link.replaceAll("\\{id\\}", String.valueOf(aid))
	        :
	        String.format("%s/updatefavadd/?fid=%d&uid=%d&name=%s&address=%s&lat=%.7f&lon=%.7f&icon=%s",
			HOST, aid, user.getId(), URLEncoder.encode(name, "UTF-8"), URLEncoder.encode(address, "UTF-8"), latitude, longitude, icon)
        );
		if(NEW_API){
		    this.user = user;
	        this.name = name;
	        this.address = address;
	        this.lat = latitude;
	        this.lon = longitude;
	        this.icon = icon;
		}
	}
	
	public void execute(Context ctx) throws IOException, JSONException, InterruptedException {
	    if(NEW_API){
	        this.username = user.getUsername();
            this.password = user.getPassword();
            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", String.valueOf(user.getId()));
            params.put("name", StringUtils.substring(name, 0, 30));
            params.put("address", StringUtils.substring(address, 0, 80));
            params.put("lat", String.format("%.7f", lat));
            params.put("lon", String.format("%.7f", lon));
            params.put("icon", icon);
            executeHttpRequest(Method.PUT, url, params, ctx);
	    }else{
	        executeUpdateRequest(url, ctx);
	    }
	}
}
