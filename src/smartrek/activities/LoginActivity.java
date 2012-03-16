package smartrek.activities;

import smartrek.mappers.UserMapper;
import smartrek.models.User;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/******************************************************************************************************************
 * 
 *
 ******************************************************************************************************************/
public class LoginActivity extends FragmentActivity implements OnClickListener{
	
	private EditText uname;
	private EditText pwd;
	public static final String LOGIN_PREFS = "login_file";
	
	private Context context;
	
	/******************************************************************************************************************
	 * 
	 *
	 ******************************************************************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginscreen);
        
        context = this;
        
        /* Check Shared memory to see if login info has already been entered on this phone */
        checkSharedPreferences();
        
        /* If it hasn't set up the login screen */
        
        Button login = (Button) findViewById(R.id.login_button);
        login.setId(1);
        login.setOnClickListener(this);
        
        Button new_user = (Button) findViewById(R.id.new_user_button);
        new_user.setId(2);
        new_user.setOnClickListener(registerButtonClickListener);
        
        uname = (EditText) findViewById(R.id.username_box);
        pwd = (EditText) findViewById(R.id.pwd_box);
       
    }
    
	/******************************************************************************************************************
	 * 
	 *
	 ******************************************************************************************************************/
    private void checkSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_PREFS,MODE_PRIVATE);
        String user = sharedPreferences.getString(UserMapper.USERNAME, "");
        int uid = sharedPreferences.getInt(UserMapper.UID, -1);
        
        if(!user.equals("") && uid !=- 1){
        	Log.d("Login_Activity","Got Login info from Shared Preferences");
        	Log.d("Login_Activity","Finishing Login_Activity, Staring Home_Activity");
        	Intent intent = new Intent(this,HomeActivity.class);
			startActivity(intent);
			finish();
        }
    	
    }
    
	/******************************************************************************************************************
	 * 
	 *
	 ******************************************************************************************************************/
    @Override
    public void onBackPressed(){
    	finish();
    }

	/******************************************************************************************************************
	 * 
	 *
	 ******************************************************************************************************************/
	@Override
	public void onClick(View v) {
		
			String username = uname.getText().toString();
			String password = pwd.getText().toString();
			Log.d("Attempting Login", "User: " + username + "    Password: " + password);
			
			new LoginTask().execute(username, password);
	}

	Button.OnClickListener registerButtonClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.d("Login_Activity","Register New user clicked");
			Log.d("Login_Activity","Starting Register Activity");
			Intent intent = new Intent(context,RegisterActivity.class);
			startActivity(intent);
		}
	};
	
	/**
	 * Methods in this class will be executed asynchronously. 
	 */
	private class LoginTask extends AsyncTask<String, Object, User> {

		@Override
		protected User doInBackground(String... params) {
			String username = params[0];
			String password = params[1];
			
			User user = null;
			try {
				user = new UserMapper().login(username, password);
			}
			catch(Exception e) {
				user = new User(-1, username);
			}
			
			if(user == null) {
				user = new User(-1, username);
			}

			return user;
		}
		
		@Override
		protected void onPostExecute(User user) {
			if(user != null && user.getId() != -1) {
				Log.d("Login_Activity","Successful Login");
				Log.d("Login_Activity", "Saving Login Info to Shared Preferences");
				
				SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_PREFS, MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(UserMapper.USERNAME, user.getUsername());
				editor.putInt(UserMapper.UID, user.getId());
				editor.commit();
				
				Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
				
				Log.d("Login_Activity","Finishing Login_Activity, Staring Home_Activity");
				
				startActivity(intent);
				finish();
				
			}
			else {
				Log.d("Login_Activity", "Failed Login User: " + user.getUsername());
				TextView loginfail_text = (TextView) findViewById(R.id.failed_login);
				loginfail_text.setVisibility(View.VISIBLE);
				pwd.setText("");
			}
		}
		
	}
}
