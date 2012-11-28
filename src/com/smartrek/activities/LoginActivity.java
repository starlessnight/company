package com.smartrek.activities;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smartrek.models.Reservation;
import com.smartrek.models.User;
import com.smartrek.receivers.ReservationReceiver;
import com.smartrek.requests.ReservationListFetchRequest;
import com.smartrek.requests.UserLoginRequest;
import com.smartrek.utils.Cache;
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
        User currentUser = User.getCurrentUser(this);
        
        if(currentUser != null) {
        	new NotificationTask().execute(currentUser.getId());
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
			Log.d("Attempting Login", "User: " + username + "    Password: " + password);
			
			new LoginTask().execute(username, password);
	}
	
	private void registerNotification(Reservation reservation) {
		
		Intent intent = new Intent(this, ReservationReceiver.class);
		
		intent.putExtra("reservation", reservation);
		intent.putExtra("route", reservation.getRoute());
		
		// In reality, you would want to have a static variable for the
		// request code instead of 192837
		PendingIntent pendingOperation = PendingIntent.getBroadcast(this, 192837,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, reservation.getDepartureTime() - 60000*5, pendingOperation); // 5 min earlier than departure time

		Cache cache = Cache.getInstance();
		if (cache.has("pendingAlarms")) {
			@SuppressWarnings("unchecked")
			List<PendingIntent> pendingAlarms = (List<PendingIntent>) cache.fetch("pendingAlarms");
			pendingAlarms.add(pendingOperation);
		}
		else {
			List<PendingIntent> pendingOperations = new LinkedList<PendingIntent>();
			pendingOperations.add(pendingOperation);
			
			cache.put("pendingAlarms", pendingOperations);
		}
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

		@Override
		protected User doInBackground(String... params) {
			String username = params[0];
			String password = params[1];
			
			User user = null;
			try {
				UserLoginRequest request = new UserLoginRequest(username, password);
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
			if(user != null && user.getId() != -1) {
				Log.d("Login_Activity","Successful Login");
				Log.d("Login_Activity", "Saving Login Info to Shared Preferences");
				
				User.setCurrentUser(LoginActivity.this, user);
				
				new NotificationTask().execute(user.getId());
			}
			else {
				Log.d("Login_Activity", "Failed Login User: " + user.getUsername());
				TextView loginfail_text = (TextView) findViewById(R.id.failed_login);
				loginfail_text.setVisibility(View.VISIBLE);
				editTextPassword.setText("");
			}
		}
	}
	
	private class NotificationTask extends AsyncTask<Object, Object, List<Reservation>> {
		private ProgressDialog dialog;
		
		public NotificationTask() {
			super();
			
			dialog = new ProgressDialog(LoginActivity.this);
			dialog.setTitle("Smartrek");
			dialog.setMessage("Fetching existing reservations...");
		}
		
		@Override
		protected void onPreExecute() {
			dialog.show();
		}
		
		@Override
		protected List<Reservation> doInBackground(Object... params) {
			int uid = (Integer) params[0];
			
			ReservationListFetchRequest request = new ReservationListFetchRequest(uid);
			List<Reservation> reservations = null;
			try {
				reservations = request.execute();
			}
			catch (Exception e) {
				ehs.registerException(e);
			}
			
			return reservations;
		}
		
		@Override
		protected void onPostExecute(List<Reservation> result) {
			if (dialog.isShowing()) {
				dialog.cancel();
			}
			
			for (Reservation r : result) {
				registerNotification(r);
			}
			
			Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
			
			startActivity(intent);
			finish();
		}
	}
}
