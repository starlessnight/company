package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

public class FavoriteAddressAddRequest extends Request {
	
	private int uid;
	private String name;
	private String address;
	private double lat;
	private double lon;
	
	public FavoriteAddressAddRequest(int uid, String name, String address, double lat, double lon) {
		this.uid = uid;
		this.name = name;
		this.address = address;
		this.lat = lat;
		this.lon = lon;
	}
	
	public void execute() throws IOException {
		String url = String.format("%s/V0.2/addfavadd/?UID=%d&NAME=%s&ADDRESS=%s&lat=%.7f&lon=%.7f",
				HOST, uid, URLEncoder.encode(name), URLEncoder.encode(address), lat, lon);
		
		executeHttpGetRequest(url);
	}
	
}
