package com.smartrek.requests;

import java.io.IOException;

import org.json.JSONException;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

public final class FavoriteAddressDeleteRequest extends DeleteRequest {
	
	private User user;
	
	/**
	 * Favorite address ID
	 */
	private int aid;
	
	public FavoriteAddressDeleteRequest(User user, int aid) {
		this.user = user;
		this.aid = aid;
	}
	
	public void execute() throws IOException, JSONException {
	    if(NEW_API){
	        this.username = user.getUsername();
            this.password = user.getPassword();
            String url = getLinkUrl(Link.address) + "/" + aid;
            executeHttpRequest(Method.DELETE, url);
	    }else{
    		String url = String.format("%s/deletefavadd/%d%%20%d", HOST, aid, user.getId());
    		executeDeleteRequest(url);
	    }
	}

}
