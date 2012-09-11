package com.smartrek.requests;

import java.io.IOException;

import org.json.JSONException;

public final class TripDeleteRequest extends DeleteRequest {
	
	/**
	 * Trip ID
	 */
	private int fid;
	
	public TripDeleteRequest(int fid) {
		this.fid = fid;
	}
	
	public void execute() throws IOException, JSONException {
		String url = String.format("%s/favroutes-delete/?fid=%d", HOST, fid);
		
		executeDeleteRequest(url);
	}

}
