package com.smartrek.requests;

import java.io.IOException;

import org.json.JSONException;

import android.content.Context;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

public final class TripDeleteRequest extends DeleteRequest {

    private User user;
    
	/**
	 * Trip ID
	 */
	private int fid;
	
	private String link;
	
	public TripDeleteRequest(String link, int fid, User user) {
		this.fid = fid;
		this.user = user;
		this.link = link;
	}
	
	public void execute(Context ctx) throws IOException, JSONException, InterruptedException {
	    if(NEW_API){
            this.username = user.getUsername();
            this.password = user.getPassword();
            String url = link.replaceAll("\\{id\\}", String.valueOf(fid));
            executeHttpRequest(Method.DELETE, url, ctx);
        }else{
            String url = String.format("%s/favroutes-delete/?fid=%d", HOST, fid);
            executeDeleteRequest(url, ctx);
        }
	}

}
