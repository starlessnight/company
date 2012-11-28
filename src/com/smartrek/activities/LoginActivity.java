package com.smartrek.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.models.User;
import com.smartrek.requests.UserLoginRequest;
import com.smartrek.utils.ExceptionHandlingService;

public final class LoginActivity extends Activity implements OnClickListener {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private EditText editTextUsername;
	private EditText editTextPassword;
	public static final String LOGIN_PREFS = "login_file";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);
        
        /* Check Shared memory to see if login info has already been entered on this phone */
        checkSharedPreferences();
        
        /* If it hasn't set up the login screen */
        
        Button login = (Button) findViewById(R.id.login_button);
        login.setId(1);
        login.setOnClickListener(this);
        
        Button new_user = (Button) findViewById(R.id.new_user_button);
        new_user.setId(2);
        new_user.setOnClickListener(registerButtonClickListener);
        
        editTextUsername = (EditText) findViewById(R.id.username_box);
        editTextPassword = (EditText) findViewById(R.id.pwd_box);
       
    }
    
    private void checkSharedPreferences() {
    	SharedPreferences loginPrefs = getSharedPreferences(LOGIN_PREFS, Context.MODE_PRIVATE);
    	String username = loginPrefs.getString(User.USERNAME, "");
    	String password = loginPrefs.getString(User.PASSWORD, "");
    	
    	if (!username.equals("") && !password.equals("")) {
    		SharedPreferences prefs = getSharedPreferences("Global", Context.MODE_PRIVATE);
    		String gcmRegistrationId = prefs.getString("GCMRegistrationID", "");
    		
    		new LoginTask(username, password, gcmRegistrationId).execute();
    	}
    }
    
    @Override
    public void onBackPressed(){
    	finish();
    }

	@Override
	public void onClick(View v) {
			String username = editTextUsername.getText().toString();
			String password = editTextPassword.getText().toString();
			
			SharedPreferences globalPrefs = getSharedPreferences("Global", Context.MODE_PRIVATE);
    		String gcmRegistrationId = globalPrefs.getString("GCMRegistrationID", "");
    		
    		SharedPreferences loginPrefs = getSharedPreferences(LOGIN_PREFS, Context.MODE_PRIVATE);
    		SharedPreferences.Editor loginPrefsEditor = loginPrefs.edit();
    		loginPrefsEditor.putString(User.USERNAME, username);
    		loginPrefsEditor.putString(User.PASSWORD, password);
    		loginPrefsEditor.commit();
			
			new LoginTask(username, password, gcmRegistrationId).execute();
	}
	
	Button.OnClickListener registerButtonClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.d("Login_Activity","Register New user clicked");
			Log.d("Login_Activity","Starting Register Activity");
			Intent intent = new Intent(LoginActivity.this, UserRegistrationActivity.class);
			startActivity(intent);
		}
	};
	
	/**
	 * Methods in this class will be executed asynchronously. 
	 */
	private class LoginTask extends AsyncTask<String, Object, User> {
		
		private ProgressDialog dialog;
		
		private String username;
		private String password;
		private String gcmRegistrationId;
		
		public LoginTask(String username, String password, String gcmRegistrationId) {
			super();
			
			this.username = username;
			this.password = password;
			this.gcmRegistrationId = gcmRegistrationId;

			dialog = new ProgressDialog(LoginActivity.this);
			dialog.setTitle("Smartrek");
			dialog.setMessage(String.format("Logging in as '%s'...", username));
		}
		
		@Override
		protected void onPreExecute() {
			dialog.show();
		}

		@Override
		protected User doInBackground(String... params) {
			User user = null;
			try {
				UserLoginRequest request = new UserLoginRequest(username, password, gcmRegistrationId);
				user = request.execute();
			}
			catch(Exception e) {
				ehs.registerException(e);
			}
			
			if(user == null) {
				user = new User(-1, username);
			}

			return user;
		}
		
		@Override
		protected void onPostExecute(User user) {
		    dialog.cancel();
		    
			if(user != null && user.getId() != -1) {
				Log.d("Login_Activity","Successful Login");
				Log.d("Login_Activity", "Saving Login Info to Shared Preferences");

				User.setCurrentUser(LoginActivity.this, user);
				
				Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
				startActivity(intent);
			}
			else {
//				Log.d("Login_Activity", "Failed Login User: " + user.getUsername());
//				TextView loginfail_text = (TextView) findViewById(R.id.failed_login);
//				loginfail_text.setVisibility(View.VISIBLE);
				editTextPassword.setText("");
				
				NotificationDialog notificationDialog = new NotificationDialog(LoginActivity.this, "The username or password you entered is not valid.");
				notificationDialog.show();
			}
		}
	}

}
