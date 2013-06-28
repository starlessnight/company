package com.smartrek.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.Address;
import com.smartrek.models.User;

public final class FavoriteAddressFetchRequest extends FetchRequest<List<Address>> {
	
	public FavoriteAddressFetchRequest(User user) {
        super(NEW_API?getLinkUrl(Link.address):String.format("%s/V0.2/getfavadd/%d", HOST, user.getId()));
        if(NEW_API){
            this.username = user.getUsername();
            this.password = user.getPassword();
        }
    }
	
	@Override
	public List<Address> execute(Context ctx) throws Exception {
		List<Address> addresses = new ArrayList<Address>();
		
		String response = executeFetchRequest(getURL(), ctx);
		if(NEW_API){
		    JSONArray array = new JSONObject(response).getJSONArray("data");
            for(int i = 0; i < array.length(); i++) {
                JSONObject object = (JSONObject) array.get(i);
                
                Address address = new Address();
                address.setId(object.getInt("id"));
                address.setUid(object.getInt("user_id"));
                address.setName(object.getString("name"));
                address.setAddress(object.getString("address"));
                address.setLatitude(object.optDouble("lat", 0));
                address.setLongitude(object.optDouble("lon", 0));
                
                addresses.add(address);
            }
		}else{
		    JSONArray array = new JSONArray(response);
	        for(int i = 0; i < array.length(); i++) {
	            JSONObject object = (JSONObject) array.get(i);
	            
	            Address address = new Address();
                address.setId(object.getInt("FID"));
                address.setUid(object.getInt("UID"));
                address.setName(object.getString("NAME"));
                address.setAddress(object.getString("ADDRESS"));
                address.setLatitude(object.getDouble("LAT"));
                address.setLongitude(object.getDouble("LON"));
	            
	            addresses.add(address);
	        }
		}
		
		return addresses;
	}

}
