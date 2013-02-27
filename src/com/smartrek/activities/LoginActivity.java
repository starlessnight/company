package com.smartrek.activities;

import java.net.ConnectException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.models.User;
import com.smartrek.tasks.LoginTask;
import com.smartrek.utils.Font;
import com.smartrek.utils.Preferences;

public final class LoginActivity extends Activity implements OnClickListener,
        TextWatcher {
	
	private EditText editTextUsername;
	private EditText editTextPassword;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);
        
        /* If it hasn't set up the login screen */
        
        Button login = (Button) findViewById(R.id.login_button);
        login.setOnClickListener(this);
        
        TextView new_user = (TextView) findViewById(R.id.new_user_button);
        new_user.setText(Html.fromHtml(getString(R.string.create_account_link_text)));
        new_user.setOnClickListener(registerButtonClickListener);
        
        editTextUsername = (EditText) findViewById(R.id.username_box);
        editTextUsername.addTextChangedListener(this);
        editTextPassword = (EditText) findViewById(R.id.pwd_box);
        editTextPassword.addTextChangedListener(this);
        
        AssetManager assets = getAssets();
        Font.setTypeface(Font.getBold(assets), (TextView)findViewById(R.id.subtitle),
            login, new_user);
        Font.setTypeface(Font.getLight(assets), editTextUsername, editTextPassword);
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

    @Override
    public void afterTextChanged(Editable s) {
        
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Button login = (Button) findViewById(R.id.login_button);
        login.setEnabled(editTextUsername.getText().length() > 0 && editTextPassword.getText().length() > 0);
    }

}
