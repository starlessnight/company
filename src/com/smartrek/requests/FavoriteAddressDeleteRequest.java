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
	
	private String link;
	
	public FavoriteAddressDeleteRequest(String link, User user, int aid) {
		this.user = user;
		this.aid = aid;
		this.link = link;
	}
	
	public void execute() throws IOException, JSONException {
	    if(NEW_API){
	        this.username = user.getUsername();
            this.password = user.getPassword();
            String url = link.replaceAll("\\{id\\}", String.valueOf(aid));
            executeHttpRequest(Method.DELETE, url);
	    }else{
    		String url = String.format("%s/deletefavadd/%d%%20%d", HOST, aid, user.getId());
    		executeDeleteRequest(url);
	    }
	}

}
