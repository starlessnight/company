package com.smartrek.activities;

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

import com.smartrek.ui.ClickAnimation;
import com.smartrek.ui.ClickAnimation.ClickAnimationEndCallback;
import com.smartrek.utils.Font;
import com.smartrek.utils.Misc;

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
		
		AssetManager assets = getAssets();
		boldFont = Font.getBold(assets);
		lightFont = Font.getLight(assets);
		
		TextView backButton = (TextView) findViewById(R.id.back_button);
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
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();
	    Misc.tripInfoPanelOnActivityRestart(this);
	}
	
}
