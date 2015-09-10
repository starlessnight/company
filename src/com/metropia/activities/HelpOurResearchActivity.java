package com.metropia.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.LocalyticsUtils;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.ui.animation.ClickAnimation;
import com.metropia.ui.animation.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;

public class HelpOurResearchActivity extends FragmentActivity {
	
	private static final String HELP_OUR_RESEARCH = "HELP_OUR_RESEARCH";
	public static final String PAYDAYS = "PAYDAYS";
	public static final String DRIVE_SMART = "DRIVE_SMART";

	private Typeface boldFont;
	private Typeface lightFont;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_our_research);
		
		Localytics.integrate(this);
		
		AssetManager assets = getAssets();
		boldFont = Font.getBold(assets);
		lightFont = Font.getLight(assets);
		
		View backButton = findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(HelpOurResearchActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						finish();
					}
				});
			}
		});
		
		final SharedPreferences prefs = getSharedPreferences(HELP_OUR_RESEARCH,
				MODE_PRIVATE);
		
		boolean isPaydaysEnabled = isPaydaysEnabled(this);
		ToggleButton paydaysButton = (ToggleButton) findViewById(R.id.paydays);
		paydaysButton.setChecked(isPaydaysEnabled);
		paydaysButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button,
					boolean isChecked) {
				LocalyticsUtils.tagHelpOurResearchSettings(LocalyticsUtils.PAYDAYS, isChecked);
				prefs.edit().putBoolean(PAYDAYS, isChecked).commit();
			}
		});
		
		String paydayDesc = "Pay-As-You-Drive-And-You-Save:\n" + 
		                    "Insurance actuarial study promoting safer driving and " + 
				            "identifying opportunities to lower insurance premiums " + 
		                    "for drivers, improving driving habits.";
		
		TextView paydaysDescView = (TextView) findViewById(R.id.paydays_desc);
		paydaysDescView.setText(paydayDesc);
		
		boolean isDriveSmartEnabled = isDriveSmartEnabled(this);
		ToggleButton driveSmartButton = (ToggleButton) findViewById(R.id.drive_smart);
		driveSmartButton.setChecked(isDriveSmartEnabled);
		driveSmartButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button,
					boolean isChecked) {
				LocalyticsUtils.tagHelpOurResearchSettings(LocalyticsUtils.DRIVE_SMART, isChecked);
				prefs.edit().putBoolean(DRIVE_SMART, isChecked).commit();
			}
		});
		
		String driveSmartDesc = "Drive Smart Project:\n" + 
		                        "Examining time savings, travel pattern changes and " + 
				                "route choices to improve city mobility and driver safety.";
		
		TextView driveSmartDescView = (TextView) findViewById(R.id.drive_smart_desc);
		driveSmartDescView.setText(driveSmartDesc);
		
		Font.setTypeface(boldFont, (TextView) findViewById(R.id.header));
		Font.setTypeface(lightFont, (TextView) findViewById(R.id.paydays_text), 
				paydaysDescView, (TextView) findViewById(R.id.drive_smart_text), 
				driveSmartDescView);
		
		//init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	public static boolean isPaydaysEnabled(Context ctx) {
		return ctx.getSharedPreferences(HELP_OUR_RESEARCH, MODE_PRIVATE)
				.getBoolean(PAYDAYS, true);
	}
	
	public static boolean isDriveSmartEnabled(Context ctx) {
		return ctx.getSharedPreferences(HELP_OUR_RESEARCH, MODE_PRIVATE)
				.getBoolean(DRIVE_SMART, true);
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
