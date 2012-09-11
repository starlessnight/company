package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

public class FavoriteAddressAddRequest extends Request {
	
	private int uid;
	private String name;
	private String address;
	
	public FavoriteAddressAddRequest(int uid, String name, String address) {
		this.uid = uid;
		this.name = name;
		this.address = address;
	}
	
	public void execute() throws IOException {
		String url = String.format("%s/addfavadd/?UID=%d&NAME=%s&ADDRESS=%s",
				HOST, uid, URLEncoder.encode(name), URLEncoder.encode(address));
		
		executeHttpGetRequest(url);
	}
	
}
