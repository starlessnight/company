package smartrek.mappers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import smartrek.models.User;
import android.util.Log;

public class ContactsMapper extends Mapper {

	@Override
	protected String appendToUrl() {
		return "/getcontacts";
	}
	
	public List<User> getContacts(User currentUser) {
		return getContacts(currentUser.getId());
	}
	
	public List<User> getContacts(int uid) {
		List<User> contacts = new ArrayList<User>();
		
		String url = String.format("%s%s/%d", sturl, appendToUrl(), uid);
		String jsonString = downloadText(url);
		
		try {
			JSONArray array = new JSONArray(jsonString);
			for(int i = 0; i < array.length(); i++) {
				User user = User.parse((JSONObject) array.get(i));
				contacts.add(user);
			}
		}
		catch (JSONException e) {
			Log.d("ContactMapper", "JSONException", e);
		}
		
		return contacts;
	}

}
