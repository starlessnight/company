package com.smartrek.requests;

import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.User;

public final class TripLinkRequest extends FetchRequest<String> {
    
	public TripLinkRequest(User user, long reservationId) {
		super(getLinkUrl(Link.reservation) + "/" + reservationId);
		this.username = user.getUsername();
        this.password = user.getPassword();
	}
	
	@Override
	public String execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		JSONObject json  = new JSONObject(response);
        return json.getJSONObject("links").getString("trajectory");
	}

}
