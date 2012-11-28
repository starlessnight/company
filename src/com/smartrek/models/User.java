package com.smartrek.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.smartrek.activities.LoginActivity;

public final class User implements JSONModel, Parcelable {
	
	/* JSON keys */
	public static final String UID = "UID";
	public static final String USERNAME = "USERNAME";
	public static final String FIRSTNAME = "FIRSTNAME";
	public static final String LASTNAME = "LASTNAME";
	public static final String EMAIL = "EMAIL";
	public static final String PASSWORD = "PASSWORD";
	
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
		obj.put(UID, getId());
		obj.put(USERNAME, getUsername());
		obj.put(FIRSTNAME, getFirstname());
		obj.put(LASTNAME, getLastname());
		obj.put(EMAIL, getEmail());
		obj.put(PASSWORD, getPassword());
		
		return obj.toString();
	}
	
	public static User parse(String jsonString) throws JSONException {
		return parse(new JSONObject(jsonString));
	}
	
	public static User parse(JSONObject object) throws JSONException {
		User user = new User();
		user.id = object.getInt(UID);
		user.username = object.getString(USERNAME);
		if (object.has(FIRSTNAME)) user.firstname = object.getString(FIRSTNAME);
		if (object.has(LASTNAME)) user.lastname = object.getString(LASTNAME);
		if (object.has(EMAIL)) user.email = object.getString(EMAIL);
		if (object.has(PASSWORD)) user.password = object.getString(PASSWORD);
		
		return user;
	}
	
	/**
	 * @param context
	 * @return JSON object of the current user (if exists)
	 * @throws JSONException 
	 */
	public static JSONObject getCurrentUserData(Context context) throws JSONException {
		SharedPreferences prefs = context.getSharedPreferences(LoginActivity.LOGIN_PREFS, Context.MODE_PRIVATE);
		return new JSONObject(prefs.getString("CurrentUser", ""));
	}
	
	public static User getCurrentUser(Context context) {
		if(currentUser == null) {
	        try {
				currentUser = parse(getCurrentUserData(context));
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
				editor.putString("CurrentUser", currentUser.toJSON());
			}
			editor.commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static void logout(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(LoginActivity.LOGIN_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(USERNAME);
		editor.remove(PASSWORD);
		editor.commit();
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
