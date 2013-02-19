package com.smartrek.activities;

import java.net.ConnectException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.models.User;
import com.smartrek.tasks.LoginTask;
import com.smartrek.utils.Preferences;

public final class LoginActivity extends Activity implements OnClickListener {
	
	private EditText editTextUsername;
	private EditText editTextPassword;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);
        
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
    
    @Override
    public void onBackPressed(){
    	finish();
    }

	@Override
	public void onClick(View v) {
		String username = editTextUsername.getText().toString();
		String password = editTextPassword.getText().toString();
		
		SharedPreferences globalPrefs = Preferences.getGlobalPreferences(this);
		String gcmRegistrationId = globalPrefs.getString(Preferences.Global.GCM_REG_ID, "");
		
		SharedPreferences loginPrefs = Preferences.getAuthPreferences(this);
		SharedPreferences.Editor loginPrefsEditor = loginPrefs.edit();
		loginPrefsEditor.putString(User.USERNAME, username);
		loginPrefsEditor.putString(User.PASSWORD, password);
		loginPrefsEditor.commit();
		
		new LoginTask(this, username, password, gcmRegistrationId){
            @Override
            protected void onPostLogin(User user) {
                if(user != null && user.getId() != -1) {
                    Log.d("Login_Activity","Successful Login");
                    Log.d("Login_Activity", "Saving Login Info to Shared Preferences");

                    User.setCurrentUser(LoginActivity.this, user);
                    
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    LoginActivity.this.startActivity(intent);
                }
                else {
//                  Log.d("Login_Activity", "Failed Login User: " + user.getUsername());
//                  TextView loginfail_text = (TextView) findViewById(R.id.failed_login);
//                  loginfail_text.setVisibility(View.VISIBLE);
                    editTextPassword.setText("");
                 
                    String msg;
                    if(ehs.hasExceptions() && ehs.popException().getException() instanceof ConnectException){
                        msg = "Can't connect. Check your network.";
                    }else{
                        msg = "The username or password you entered is not valid.";
                    }
                    NotificationDialog notificationDialog = new NotificationDialog(LoginActivity.this, msg);
                    notificationDialog.show();
                }                
            }
		}.execute();
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

}
