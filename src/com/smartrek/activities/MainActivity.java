package com.smartrek.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity implements AnimationListener {
	
	public static final String LOG_TAG = "MainActivity";
	
	private ImageView logo;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		logo = (ImageView) findViewById(R.id.imageViewLogo);
		Animation fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade);
		fadeAnimation.setAnimationListener(this);
		logo.startAnimation(fadeAnimation);
		
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, GCMIntentService.GCM_SENDER_ID);
			Log.v(LOG_TAG, "Registered to GCM.");
			
			regId = GCMRegistrar.getRegistrationId(this);
			Log.v(LOG_TAG, "Registration ID: " + regId);
		}
		else {
			Log.v(LOG_TAG, "Already registered to GCM.");
		}
		
		
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		logo.setAlpha(0);
		
		SharedPreferences prefs = getSharedPreferences("Global", Context.MODE_PRIVATE);
		int licenseAgreement = prefs.getInt(LicenseAgreementActivity.LICENSE_AGREEMENT, LicenseAgreementActivity.DISAGREED);
		
		if (licenseAgreement == LicenseAgreementActivity.AGREED) {
			startLoginActivity();
		}
		else {
			Intent intent = new Intent(this, LicenseAgreementActivity.class);
			startActivityForResult(intent, LicenseAgreementActivity.LICENSE_AGREEMENT_ACTIVITY);
			
			Log.d("MainActivity", "asldkfjalskdfjlaskfdj");
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
        		startLoginActivity();
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
