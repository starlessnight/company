package com.smartrek.mappers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP;


public final class ContactsMapper extends Mapper {

	@Override
	protected String appendToUrl() {
		return "/getcontacts";
	}
	
	public List<User> getContacts(User currentUser) throws JSONException, IOException {
		return getContacts(currentUser.getId());
	}
	
	public List<User> getContacts(int uid) throws JSONException, IOException {
		List<User> contacts = new ArrayList<User>();
		
		String url = String.format("%s%s/%d", sturl, appendToUrl(), uid);
		
		HTTP http = new HTTP(url);
		http.connect();
		
		int responseCode = http.getResponseCode();
		if (responseCode == 200) {
			String jsonString = http.getResponseBody();
			JSONArray array = new JSONArray(jsonString);
			for(int i = 0; i < array.length(); i++) {
				User user = User.parse((JSONObject) array.get(i));
				contacts.add(user);
			}
		}
		else {
			throw new IOException(String.format("HTTP %d: %s", responseCode, http.getResponseBody()));
		}
	
		
		return contacts;
	}

}
