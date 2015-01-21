package com.metropia.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import com.metropia.CrashlyticsUtils;
import com.metropia.activities.MainActivity;
import com.metropia.activities.MapDisplayActivity;
import com.metropia.activities.R;
import com.metropia.requests.UserIdRequest;
import com.metropia.tasks.LoginTask;
import com.metropia.ui.menu.MainMenu;
import com.metropia.utils.Preferences;

public final class User implements JSONModel, Parcelable {
	
	/* JSON keys */
	public static final String UID = "UID";
	public static final String USERNAME = "USERNAME";
	public static final String FIRSTNAME = "FIRSTNAME";
	public static final String LASTNAME = "LASTNAME";
	public static final String EMAIL = "EMAIL";
	public static final String PASSWORD = "PASSWORD";
	public static final String CREDIT = "CREDIT";
	public static final String TRIP = "TRIP";
	public static final String ZIP_CODE = "ZIP_CODE";
	public static final String NEW_USER = "NEW_USER";
	
	private static User currentUser;

	private int id;
	private String username;
	private String password;
	private String firstname;
	private String lastname;
	private String email;
	private String deviceId;
	private int credit;
	private int trip;
	private String zipCode;
	
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
		deviceId = in.readString();
		credit = in.readInt();
		trip = in.readInt();
		zipCode = in.readString();
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
		obj.put(CREDIT, getCredit());
		obj.put(TRIP, getTrip());
		obj.put(ZIP_CODE, getZipCode());
		
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
		if (object.has(CREDIT)) user.credit = object.getInt(CREDIT);
		if (object.has(TRIP)) user.trip = object.getInt(TRIP);
		if (object.has(ZIP_CODE)) user.zipCode = object.getString(ZIP_CODE);
		
		return user;
	}
	
	/**
	 * @param context
	 * @return JSON object of the current user (if exists)
	 * @throws JSONException 
	 */
	public static JSONObject getCurrentUserData(Context context) throws JSONException {
		SharedPreferences prefs = Preferences.getAuthPreferences(context);
		return new JSONObject(prefs.getString("CurrentUser", ""));
	}
	
	public static User getCurrentUser(Context context) {
		if(currentUser == null) {
	        try {
				currentUser = parse(getCurrentUserData(context));
			} catch (JSONException e) {
			}
		}
		
		return currentUser;
	}
	
	public static User getCurrentUserWithoutCache(Context context) {
       User user = null;
       try {
           user = parse(getCurrentUserData(context));
       } catch (JSONException e) {
       }
       return user;
    }
	
	public static void setCurrentUser(Context context, User currentUser) {
		User.currentUser = currentUser;
		
		SharedPreferences prefs = Preferences.getAuthPreferences(context);
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
		SharedPreferences prefs = Preferences.getAuthPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
//		editor.remove(USERNAME);
		editor.remove(PASSWORD);
		editor.putString("CurrentUser", "");
		editor.commit();
		MapDisplayActivity.setProfileSelection(context, null);
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
		dest.writeString(deviceId);
		dest.writeInt(credit);
		dest.writeInt(trip);
		dest.writeString(zipCode);
	}

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public int getTrip() {
        return trip;
    }

    public void setTrip(int trip) {
        this.trip = trip;
    }
    
    public String getZipCode() {
    	return zipCode;
    }
    
    public void setZipCode(String zipCode) {
    	this.zipCode = zipCode;
    }
    
    public static void initializeIfNeccessary(final Context ctx, final Runnable callback){
        Runnable loginAndDoCallback = new Runnable() {
            @Override
            public void run() {
                if(User.getCurrentUser(ctx) != null){
                    callback.run();
                }else{
                    SharedPreferences loginPrefs = Preferences.getAuthPreferences(ctx);
                    final String username = loginPrefs.getString(User.USERNAME, "");
                    final String password = loginPrefs.getString(User.PASSWORD, "");
                    if (!username.equals("") && !password.equals("")) {
                        final String gcmRegistrationId = Preferences.getGlobalPreferences(ctx)
                                .getString("GCMRegistrationID", "");
                        final LoginTask loginTask = new LoginTask(ctx, username, password, gcmRegistrationId) {
                            @Override
                            protected void onPostLogin(final User user) {
                                if(user != null && user.getId() != -1){
                                    User.setCurrentUser(ctx, user);
                                    CrashlyticsUtils.initUserInfo(user);
                                    callback.run();
                                }else if(ctx instanceof Activity){
                                    MainMenu.onMenuItemSelected((Activity) ctx, 0, R.id.logout_option);
                                }
                           }
                        }.setDialogEnabled(false);
                        new AsyncTask<Void, Void, Integer>() {
                            @Override
                            protected Integer doInBackground(Void... params) {
                                Integer id = null;
                                try {
                                    UserIdRequest req = new UserIdRequest(username); 
                                    req.invalidateCache(ctx);
                                    id = req.execute(ctx);
                                }
                                catch(Exception e) {
                                }
                                return id;
                            }
                            protected void onPostExecute(Integer userId) {
                                if(userId != null){
                                    loginTask.setUserId(userId)
                                        .execute();
                                }else if(ctx instanceof Activity){
                                    MainMenu.onMenuItemSelected((Activity)ctx, 0, R.id.logout_option);
                                }
                            }
                        }.execute();
                    }else if(ctx instanceof Activity){
                        MainMenu.onMenuItemSelected((Activity) ctx, 0, R.id.logout_option);
                    }
                }
            }
        };
        MainActivity.initApiLinksIfNecessary(ctx, loginAndDoCallback);
    }
    
}
