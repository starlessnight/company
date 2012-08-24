package com.smartrek.requests;

import java.io.IOException;

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
		String url = String.format("%s/favroutes-add/?uid=%d&name=%sorigin_address=%s&destination_address=%s", HOST, uid, name, origin, destination);
		executeHttpGetRequest(url);
	}
}
