package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

public class FavoriteAddressAddRequest extends Request {
	
	public void execute(int uid, String name, String address) throws IOException {
		String url = String.format("%s/addfavadd/?UID=%d&NAME=%s&ADDRESS=%s",
				HOST, uid, URLEncoder.encode(name), URLEncoder.encode(address));
		
		executeHttpGetRequest(url);
	}
	
}
