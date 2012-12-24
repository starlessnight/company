package com.smartrek.requests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;


public final class FavoriteAddressUpdateRequest extends UpdateRequest {

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
	public FavoriteAddressUpdateRequest(int aid, int uid, String name, String address, double latitude, double longitude) throws UnsupportedEncodingException {
		super(String.format("%s/updatefavadd/?fid=%d&uid=%d&name=%s&address=%s&lat=%.7f&lon=%.7f",
				HOST, aid, uid, URLEncoder.encode(name, "UTF-8"), URLEncoder.encode(address, "UTF-8"), latitude, longitude));
	}
	
	public void execute() throws IOException, JSONException {
		executeUpdateRequest(url);
	}
}
