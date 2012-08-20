package com.smartrek.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smartrek.models.Address;
import com.smartrek.utils.Cache;

public final class FavoriteAddressMapper extends FetchRequest {

	private List<Address> addresses = new ArrayList<Address>();
	
	public List<Address> getAddresses(int uid) throws JSONException, IOException {
		String url = String.format("%s/getfavadd/%d", HOST, uid);
		
		String response = executeFetchRequest(url);
		
		JSONArray array = new JSONArray(response);
		addresses.clear();
		
		for(int i = 0; i < array.length(); i++) {
			JSONObject object = (JSONObject) array.get(i);
			
			Address address = new Address();
			address.setAid(object.getInt("FID"));
			address.setUid(object.getInt("UID"));
			address.setName(object.getString("NAME"));
			address.setAddress(object.getString("ADDRESS"));
			
			addresses.add(address);
		}
		
		return addresses;
	}

}
