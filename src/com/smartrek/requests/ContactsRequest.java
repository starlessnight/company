package com.smartrek.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.User;


public final class ContactsRequest extends FetchRequest<List<User>> {
	
	public ContactsRequest(int uid) {
		super(String.format("%s/getcontacts/%d", HOST, uid));
	}

	@Override
	public List<User> execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		
		List<User> contacts = new ArrayList<User>();
		JSONArray array = new JSONArray(response);
		for(int i = 0; i < array.length(); i++) {
			User user = User.parse((JSONObject) array.get(i));
			contacts.add(user);
		}
		
		return contacts;
	}

}
