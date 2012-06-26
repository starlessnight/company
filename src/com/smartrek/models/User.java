package com.smartrek.models;

import org.json.JSONException;
import org.json.JSONObject;

import com.smartrek.activities.LoginActivity;
import com.smartrek.mappers.UserMapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;


public final class User implements JSONModel, Parcelable {
	
	private static User currentUser;

	private int id;
	private String username;
	private String password;
	private String firstname;
	private String lastname;
	private String email;
	
	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			return new User[size];
		}
	};
	
	public User() {}
	
	public User(Parcel in) {
		id = in.readInt();
		username = in.readString();
		firstname = in.readString();
		lastname = in.readString();
		email = in.readString();
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
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	public String getName() {
		return String.format("%s %s", firstname, lastname);
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	@Override
	public String toJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put(UserMapper.UID, getId());
		obj.put(UserMapper.USERNAME, getUsername());
		obj.put(UserMapper.FIRSTNAME, getFirstname());
		obj.put(UserMapper.LASTNAME, getLastname());
		obj.put(UserMapper.EMAIL, getEmail());
		
		return obj.toString();
	}
	
	public static User parse(String jsonString) throws JSONException {
		return parse(new JSONObject(jsonString));
	}
	
	public static User parse(JSONObject object) throws JSONException {
		User user = new User();
		user.id = object.getInt(UserMapper.UID);
		user.username = object.getString(UserMapper.USERNAME);
		if(object.has(UserMapper.FIRSTNAME)) user.firstname = object.getString(UserMapper.FIRSTNAME);
		if(object.has(UserMapper.LASTNAME)) user.lastname = object.getString(UserMapper.LASTNAME);
		if(object.has(UserMapper.EMAIL)) user.email = object.getString(UserMapper.EMAIL);
		
		return user;
	}
	
	public static User getCurrentUser(Context context) {
		if(currentUser == null) {
			SharedPreferences prefs = context.getSharedPreferences(LoginActivity.LOGIN_PREFS, Context.MODE_PRIVATE);
	        String jsonString = prefs.getString("CurrentUser", "");
	        System.out.println(jsonString);
	        
	        try {
				currentUser = parse(jsonString);
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
			if(currentUser == null) {
				editor.putString("CurrentUser", "");
			}
			else {
				System.out.println(currentUser.toJSON());
				editor.putString("CurrentUser", currentUser.toJSON());
			}
			editor.commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(username);
		dest.writeString(firstname);
		dest.writeString(lastname);
		dest.writeString(email);
	}
}
