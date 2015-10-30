package com.metropia.requests;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.metropia.models.Address;
import com.metropia.models.User;

public final class FavoriteAddressFetchRequest extends FetchRequest<List<Address>> {
	
	public FavoriteAddressFetchRequest(User user, Double latitude, Double longitude) {
        super(StringUtils.defaultString(getLinkUrl(Link.address)));
        
        if (latitude!=null && longitude!=null)
    		url = url + "?lat=" + latitude + "&lon=" + longitude;
        
        if(user != null){
            this.username = user.getUsername();
            this.password = user.getPassword();
        }
    }
	
	public List<Address> execute(Context ctx) throws Exception {
		List<Address> addresses = new ArrayList<Address>();
		
		
		
		String response = executeFetchRequest(getURL(), ctx);
		if(NEW_API){
		    JSONArray array = new JSONObject(response).getJSONArray("data");
            for(int i = 0; i < array.length(); i++) {
                JSONObject object = (JSONObject) array.get(i);
                
                Address address = new Address();
                address.setId(object.optInt("id"));
                address.setUid(object.optInt("user_id"));
                address.setName(object.getString("name"));
                address.setAddress(object.getString("address"));
                address.setLatitude(object.optDouble("lat", 0));
                address.setLongitude(object.optDouble("lon", 0));
                address.setIconName(object.optString("icon", ""));
                address.setIconURL(object.optString("iconURL", ""));
                address.setPOITYPEID(object.optInt("POITYPEID", -1));
                addresses.add(address);
            }
		}
		
		return addresses;
	}

}
