package smartrek.models;

import org.json.JSONException;
import org.json.JSONObject;

import smartrek.activities.LoginActivity;
import smartrek.mappers.UserMapper;
import android.content.Context;
import android.content.SharedPreferences;


public final class User {
	
	private static User currentUser;

	private int id;
	private String username;
	private String firstname;
	private String lastname;
	
	public User() {
		
	}
	
	public User(int id, String username) {
		this.id = id;
		this.username = username;
	}
	
	public int getId() {
		return id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	public String toJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put(UserMapper.UID, getId());
		obj.put(UserMapper.USERNAME, getUsername());
		obj.put(UserMapper.FIRSTNAME, getFirstname());
		obj.put(UserMapper.LASTNAME, getLastname());
		
		return obj.toString();
	}
	
	public static User parse(String jsonString) throws JSONException {
		return parse(new JSONObject(jsonString));
	}
	
	public static User parse(JSONObject object) throws JSONException {
		User user = new User();
		user.id = object.getInt(UserMapper.UID);
		user.username = object.getString(UserMapper.USERNAME);
		user.firstname = object.getString(UserMapper.FIRSTNAME);
		user.lastname = object.getString(UserMapper.LASTNAME);
		
		return user;
	}
	
	public static User getCurrentUser(Context context) {
		if(currentUser == null) {
			SharedPreferences prefs = context.getSharedPreferences(LoginActivity.LOGIN_PREFS, Context.MODE_PRIVATE);
	        String jsonString = prefs.getString("CurrentUser", "{}");
	        
	        try {
				User.currentUser = parse(jsonString);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return currentUser;
	}
	
	public static void setCurrentUser(Context context, User currentUser) {
		User.currentUser = currentUser;
		
		SharedPreferences prefs = context.getSharedPreferences(LoginActivity.LOGIN_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		try {
			editor.putString("CurrentUser", currentUser.toJSON());
			editor.commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
