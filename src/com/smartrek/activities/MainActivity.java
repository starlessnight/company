package com.smartrek.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.SendTrajectoryService;
import com.smartrek.models.User;
import com.smartrek.tasks.LoginTask;
import com.smartrek.utils.Misc;
import com.smartrek.utils.Preferences;

public class MainActivity extends Activity implements AnimationListener {
	
	public static final String LOG_TAG = "MainActivity";
	
	private ImageView logo;
	
	private boolean splashEnded;
	
	private boolean loginTaskEnded;
	
	private boolean loggedIn;
	
	private LoginTask loginTask;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		logo = (ImageView) findViewById(R.id.imageViewLogo);
		Animation fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade);
		fadeAnimation.setAnimationListener(this);
		logo.startAnimation(fadeAnimation);
		
		/* Check Shared memory to see if login info has already been entered on this phone */
        SharedPreferences loginPrefs = Preferences.getAuthPreferences(this);
        String username = loginPrefs.getString(User.USERNAME, "");
        String password = loginPrefs.getString(User.PASSWORD, "");
        if (!username.equals("") && !password.equals("")) {
            String gcmRegistrationId = Preferences.getGlobalPreferences(this).getString("GCMRegistrationID", "");
            
            loginTask = new LoginTask(this, username, password, gcmRegistrationId) {
                @Override
                protected void onPostLogin(final User user) {
                    loggedIn = user != null && user.getId() != -1;
                    if(loggedIn){
                        User.setCurrentUser(MainActivity.this, user);
                        Log.d(LOG_TAG,"Successful Login");
                        Log.d(LOG_TAG, "Saving Login Info to Shared Preferences");
                    }
                    loginTaskEnded = true;
                    if(splashEnded){
                        if(loggedIn){
                            startHomeActivity();
                        }else{
                            startLoginActivity();
                        }
                    }
               }
            }.setDialogEnabled(false);
            loginTask.execute();
        }
        SendTrajectoryService.schedule(this);
        //CalendarService.schedule(this);
	}
	
	private void startHomeActivity(){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(HomeActivity.INIT, true);
        startActivity(intent);
        finish();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		EasyTracker.getInstance().activityStart(this);
		
		Misc.initGCM(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
	    splashEnded = true;
		logo.setAlpha(0);
		
		if(loginTask == null){
		    SharedPreferences prefs = Preferences.getGlobalPreferences(this);
            int licenseAgreement = prefs.getInt(Preferences.Global.LICENSE_AGREEMENT, LicenseAgreementActivity.DISAGREED);
            
            if (licenseAgreement == LicenseAgreementActivity.AGREED) {
                startLoginActivity();
            }
            else {
                Intent intent = new Intent(this, LicenseAgreementActivity.class);
                startActivityForResult(intent, LicenseAgreementActivity.LICENSE_AGREEMENT_ACTIVITY);
            }
		}else{
		    if(loginTaskEnded){
		        if(loggedIn){
		            startHomeActivity();
	            }else{
	                startLoginActivity();
	            }
		    }else{
		        loginTask.setDialogEnabled(true);
		    }
        }
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void onAnimationStart(Animation animation) {

	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Log.d("MainActivity", String.format("onActivityResult: %d, %d", requestCode, resultCode));
        super.onActivityResult(requestCode, resultCode, intent);
        
        switch (requestCode) {
        case LicenseAgreementActivity.LICENSE_AGREEMENT_ACTIVITY:
        	if (resultCode == LicenseAgreementActivity.AGREED) {
                startActivity(new Intent(this, TutorialActivity.class));
                
                finish();
        	}
        	else {
        		finish();
        	}
        	break;
        }
    }
    
    private void startLoginActivity() {
    	Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		
		finish();
    }
    
}
