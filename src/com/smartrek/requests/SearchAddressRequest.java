package com.smartrek.requests;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.User;
import com.smartrek.ui.NavigationView;
import com.smartrek.utils.Geocoding.Address;
import com.smartrek.utils.RouteNode;

public class SearchAddressRequest extends FetchRequest<List<Address>>{
	
	private Double userLat = null;
	private Double userLon = null;
	
	public SearchAddressRequest(User user, String addrInput, Double lat, Double lon, boolean forCalendar) {
		super(getLinkUrl(Link.search)
				.replaceAll("\\{lat\\}", lat!=null?lat.toString():"")
				.replaceAll("\\{lon\\}", lon!=null?lon.toString():"")
				.replaceAll("\\{query\\}", URLEncoder.encode(addrInput))
				.replaceAll("\\{radius_in_meters\\}", "")
				.replaceAll("\\{scenario\\}", forCalendar?"calendar":"{scenario}"));
		this.userLat = lat;
		this.userLon = lon;
		username = user.getUsername();
		password = user.getPassword();
	}

	@Override
	public List<Address> execute(Context ctx) throws Exception {
		List<Address> result = new ArrayList<Address>();
		String response = executeFetchRequest(getURL(), ctx);
		JSONObject json  = new JSONObject(response);
		if("success".equalsIgnoreCase(json.optString("status"))){
		    JSONArray datas = json.getJSONArray("data");
		    for(int i = 0 ; i < datas.length() ; i++) {
		    	JSONObject data = datas.getJSONObject(i);
		    	double lat = data.getDouble("lat");
		    	double lon = data.getDouble("lon");
		    	String addr = data.getString("address");
		    	String name = data.getString("name");
		    	String icon = data.optString("icon", "");
		    	Address address = new Address();
		    	address.setLatitude(lat);
		    	address.setLongitude(lon);
		    	address.setName(name);
		    	address.setAddress(addr);
		    	address.setIconName(icon);
		    	if(userLat!=null && userLon!=null) {
		    		NumberFormat nf = new DecimalFormat("#.#");
		    		address.setDistance(Double.valueOf(nf.format(NavigationView.metersToMiles(
		    				RouteNode.distanceBetween(userLat, userLon, lat, lon)))));
		    	}
		    	result.add(address);
		    }
		}
		return result;
	}
	
}
