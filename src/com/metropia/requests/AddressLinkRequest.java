package com.metropia.requests;

import org.json.JSONObject;

import android.content.Context;

import com.metropia.models.User;

public final class AddressLinkRequest extends FetchRequest<String> {
    
	public AddressLinkRequest(User user) {
		super(getLinkUrl(Link.address));
		this.username = user.getUsername();
        this.password = user.getPassword();
	}
	
	@Override
	public String execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		JSONObject json  = new JSONObject(response);
        return json.getJSONObject("links").getString("element");
	}

}
