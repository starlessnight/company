package com.smartrek.requests;

import java.io.IOException;

public class FavoriteAddressDeleteRequest extends Request {
	
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
	
	public void execute() throws IOException {
		String url = String.format("%s/deletefavadd/%d%%20%d", HOST, aid, uid);
		
		executeHttpGetRequest(url);
	}

}
