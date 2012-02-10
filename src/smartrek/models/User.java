package smartrek.models;

import org.json.JSONException;
import org.json.JSONObject;

import smartrek.mappers.UserMapper;

public class User {

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
	
	public static User parse(JSONObject object) throws JSONException {
		User user = new User();
		user.id = object.getInt(UserMapper.UID);
		user.username = object.getString(UserMapper.USERNAME);
		user.firstname = object.getString(UserMapper.FIRSTNAME);
		user.lastname = object.getString(UserMapper.LASTNAME);
		
		return user;
	}
}
