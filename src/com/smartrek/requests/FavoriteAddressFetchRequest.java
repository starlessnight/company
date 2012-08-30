package com.smartrek.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartrek.models.Address;

public final class FavoriteAddressFetchRequest extends FetchRequest<List<Address>> {

	public FavoriteAddressFetchRequest(int uid) {
		super(String.format("%s/getfavadd/%d", HOST, uid));
	}
	
	@Override
	public List<Address> execute() throws Exception {
		List<Address> addresses = new ArrayList<Address>();
		
		String response = executeFetchRequest(getURL());
		JSONArray array = new JSONArray(response);
		
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
