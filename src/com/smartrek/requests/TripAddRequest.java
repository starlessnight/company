package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

public final class TripAddRequest extends Request {

	private int uid;
	private String name;
	private String origin;
	private String destination;
	
	public TripAddRequest(int uid, String name, String origin, String destination) {
		this.uid = uid;
		this.name = name;
		this.origin = origin;
		this.destination = destination;
	}
	
	public void execute() throws IOException {
		String url = String.format("%s/favroutes-add/?uid=%d&name=%s&origin_address=%s&destination_address=%s",
				HOST, uid, URLEncoder.encode(name), URLEncoder.encode(origin), URLEncoder.encode(destination));
		executeHttpGetRequest(url);
	}
}
