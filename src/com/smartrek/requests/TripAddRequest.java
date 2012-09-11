package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONException;

public final class TripAddRequest extends AddRequest {

	private int uid;
	private String name;
	private int oid;
	private int did;
	
	public TripAddRequest(int uid, String name, int oid, int did) {
		this.uid = uid;
		this.name = name;
		this.oid = oid;
		this.did = did;
	}
	
	public void execute() throws IOException, JSONException {
		String url = String.format("%s/favroutes-add/?uid=%d&name=%s&oid=%d&did=%d",
				HOST, uid, URLEncoder.encode(name), oid, did);
		executeAddRequest(url);
	}
}
