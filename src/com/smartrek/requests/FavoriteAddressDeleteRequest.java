package com.smartrek.requests;

import java.io.IOException;

import org.json.JSONException;

public final class FavoriteAddressDeleteRequest extends DeleteRequest {
	
	/**
	 * User ID
	 */
	private int uid;
	
	/**
	 * Favorite address ID
	 */
	private int aid;
	
	public FavoriteAddressDeleteRequest(int uid, int aid) {
		this.uid = uid;
		this.aid = aid;
	}
	
	public void execute() throws IOException, JSONException {
		String url = String.format("%s/deletefavadd/%d%%20%d", HOST, aid, uid);
		
		executeDeleteRequest(url);
	}

}
