package com.metropia.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.models.User;
import com.metropia.activities.R;

public final class ContactDetailsActivity extends Activity {
	/**
	 * Model instance
	 */
	private User user;
	
	private TextView textViewName;
	
	private TextView textViewUsername;
	
	private TextView textViewEmail;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_details);
		
		Localytics.integrate(this);
		
		Bundle extras = getIntent().getExtras();
		user = (User) extras.getParcelable("user");
		
		textViewName = (TextView) findViewById(R.id.textViewName);
		textViewName.setText(user.getName());
		
		textViewUsername = (TextView) findViewById(R.id.textViewUsername);
		textViewUsername.setText(user.getUsername());
		
		textViewEmail = (TextView) findViewById(R.id.textViewEmail);
		textViewEmail.setText(user.getEmail());
		
		//init Tracker
		((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        Localytics.openSession();
	    Localytics.upload();
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
    }
    
    @Override
    protected void onPause() {
	    Localytics.closeSession();
	    Localytics.upload();
        super.onPause();
    }
}
