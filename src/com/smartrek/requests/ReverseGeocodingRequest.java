package com.smartrek.requests;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.User;

public class ReverseGeocodingRequest extends FetchRequest<String>{
	
	public ReverseGeocodingRequest(User user, double lat, double lon) {
		super(getLinkUrl(Link.reverse_geocoding)
				.replaceAll("\\{dont_use_this\\}", "")
				.replaceAll("\\{lat\\}", String.valueOf(lat))
				.replaceAll("\\{lon\\}", String.valueOf(lon)));
		username = user.getUsername();
		password = user.getPassword();
	}

	@Override
	public String execute(Context ctx) throws Exception {
		String result = "";
		String response = executeFetchRequest(getURL(), ctx);
		JSONObject json  = new JSONObject(response);
		if("success".equalsIgnoreCase(json.optString("status"))){
		    JSONArray datas = json.getJSONArray("data");
		    if(datas.length() > 0) {
		    	JSONObject data = datas.getJSONObject(0);
		    	result = data.getString("address");
		    }
		}
		return result;
	}

}
