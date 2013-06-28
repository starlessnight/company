package com.smartrek.requests;

import java.io.IOException;

import org.json.JSONException;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

public final class TripDeleteRequest extends DeleteRequest {

    private User user;
    
	/**
	 * Trip ID
	 */
	private int fid;
	
	public TripDeleteRequest(int fid, User user) {
		this.fid = fid;
		this.user = user;
	}
	
	public void execute() throws IOException, JSONException {
	    if(NEW_API){
            this.username = user.getUsername();
            this.password = user.getPassword();
            String url = getLinkUrl(Link.commute) + "/" + fid;
            executeHttpRequest(Method.DELETE, url);
        }else{
            String url = String.format("%s/favroutes-delete/?fid=%d", HOST, fid);
            executeDeleteRequest(url);
        }
	}

}
