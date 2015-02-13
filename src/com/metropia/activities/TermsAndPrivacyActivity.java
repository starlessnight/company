package com.metropia.activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.ui.ClickAnimation;
import com.metropia.ui.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;
import com.metropia.activities.R;

public class TermsAndPrivacyActivity extends FragmentActivity{
	
	private Typeface boldFont;
	private Typeface lightFont;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.terms_and_privacy);
		
		Localytics.integrate(this);

		AssetManager assets = getAssets();
		boldFont = Font.getBold(assets);
		lightFont = Font.getLight(assets);
		
		View backButton = findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(TermsAndPrivacyActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
					    finish();
					}
				});
			}
		});
		
		TextView termOfUseView = (TextView) findViewById(R.id.terms_of_use);
		termOfUseView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(TermsAndPrivacyActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Misc.suppressTripInfoPanel(TermsAndPrivacyActivity.this);
						Intent intent = new Intent(TermsAndPrivacyActivity.this, TermOfUseActivity.class);
						startActivity(intent);
					}
				});
			}
		});
		
		TextView privacyPolicyView = (TextView) findViewById(R.id.privacy_policy);
		privacyPolicyView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(TermsAndPrivacyActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Misc.suppressTripInfoPanel(TermsAndPrivacyActivity.this);
						Intent intent = new Intent(TermsAndPrivacyActivity.this, PrivacyPolicyActivity.class);
						startActivity(intent);
					}
				});
			}
		});
		
		Font.setTypeface(boldFont, (TextView)findViewById(R.id.header));
		Font.setTypeface(lightFont, termOfUseView, privacyPolicyView);
		//init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	@Override
	protected void onStop() {
	    super.onStop();
        Misc.tripInfoPanelOnActivityStop(this);
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();
	    Misc.tripInfoPanelOnActivityRestart(this);
	}
	
	@Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
	
	@Override
	public void onResume() {
		super.onResume();
	    Localytics.openSession();
	    Localytics.tagScreen(this.getClass().getSimpleName());
	    Localytics.upload();
	    Localytics.setInAppMessageDisplayActivity(this);
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
	}

	@Override
	public void onPause() {
	    Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
	    super.onPause();
	}


}
