package com.smartrek.requests;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.User;
import com.smartrek.utils.GeoPoint;

public class SearchAddressRequest extends FetchRequest<List<GeoPoint>>{
	
	public SearchAddressRequest(User user, String addrInput, String lat, String lon) {
		super(getLinkUrl(Link.search).replaceAll("\\{dont_use_this\\}", "")
				.replaceAll("\\{lat\\}", lat).replaceAll("\\{lon\\}", lon)
				.replaceAll("\\{query\\}", URLEncoder.encode(addrInput))
				.replaceAll("\\{radius_in_meters\\}", ""));
		username = user.getUsername();
		password = user.getPassword();
	}

	@Override
	public List<GeoPoint> execute(Context ctx) throws Exception {
		List<GeoPoint> result = new ArrayList<GeoPoint>();
		String response = executeFetchRequest(getURL(), ctx);
		JSONObject json  = new JSONObject(response);
		if("success".equalsIgnoreCase(json.optString("status"))){
		    JSONArray datas = json.getJSONArray("data");
		    for(int i = 0 ; i < datas.length() ; i++) {
		    	JSONObject data = datas.getJSONObject(i);
		    	double lat = data.getDouble("lat");
		    	double lon = data.getDouble("lon");
		    	GeoPoint point = new GeoPoint(lat, lon);
		    	result.add(point);
		    }
		}
		return result;
	}
	
}
