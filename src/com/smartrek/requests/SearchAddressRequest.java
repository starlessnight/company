package com.smartrek.requests;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.smartrek.models.User;
import com.smartrek.utils.Geocoding.Address;

public class SearchAddressRequest extends FetchRequest<List<Address>>{
	
	public SearchAddressRequest(User user, String addrInput, Double lat, Double lon) {
		super(getLinkUrl(Link.search)
				.replaceAll("\\{lat\\}", lat!=null?lat.toString():"")
				.replaceAll("\\{lon\\}", lon!=null?lon.toString():"")
				.replaceAll("\\{query\\}", URLEncoder.encode(addrInput))
				.replaceAll("\\{radius_in_meters\\}", ""));
		username = user.getUsername();
		password = user.getPassword();
	}

	@Override
	public List<Address> execute(Context ctx) throws Exception {
		List<Address> result = new ArrayList<Address>();
		String response = executeFetchRequest(getURL(), ctx);
		Log.d("SearchAddressRequest", getURL());
		JSONObject json  = new JSONObject(response);
		if("success".equalsIgnoreCase(json.optString("status"))){
		    JSONArray datas = json.getJSONArray("data");
		    for(int i = 0 ; i < datas.length() ; i++) {
		    	JSONObject data = datas.getJSONObject(i);
		    	double lat = data.getDouble("lat");
		    	double lon = data.getDouble("lon");
		    	String addr = data.getString("address");
		    	Address address = new Address();
		    	address.setLatitude(lat);
		    	address.setLongitude(lon);
		    	address.setName(addr);
		    	result.add(address);
		    }
		}
		return result;
	}
	
}
