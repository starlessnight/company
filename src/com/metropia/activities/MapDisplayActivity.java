package com.metropia.activities;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
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
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.dialogs.FloatingMenuDialog;
import com.metropia.dialogs.ProfileSelectionDialog;
import com.metropia.models.User;
import com.metropia.ui.ClickAnimation;
import com.metropia.ui.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;
import com.metropia.activities.R;

public final class MapDisplayActivity extends FragmentActivity {

	/**
	 * Name of the shared preference file
	 */
	public static final String MAP_DISPLAY_PREFS = "map_display";

	public static final String TIME_DISPLAY_MODE = "TimeDisplayMode";

	public static final int TIME_DISPLAY_TRAVEL = 2;
	public static final int TIME_DISPLAY_ARRIVAL = 4;
	public static final int TIME_DISPLAY_DEFAULT = TIME_DISPLAY_TRAVEL;

	public static final String TIME_INCREMENT = "TimeIncrement";
	public static final int TIME_INCREMENT_DEFAULT = 15;

	private static final String CALENDAR_INTEGRATION = "CALENDAR_INTEGRATION";

	private static final String NAVIGATION_TTS = "NAVIGATION_TTS";

	private static final String LOCATION_BASED_SERVICE = "LOCATION_BASED_SERVICE";

	private static final String VALIDATED_TRIPS_COUNT = "VALIDATED_TRIPS_COUNT";

	private static final String HOME_ADDRESS = "HOME_ADDRESS";

	private static final String WORK_ADDRESS = "WORK_ADDRESS";

	private static final String PROFILE_SELECTION = "PROFILE_SELECTION";

	private static final String PREDICT_DESTINATION = "PREDICT_DESTINATION";
	
	private ToggleButton calendarIntegration;

	private ToggleButton predictDest;
	
	private Typeface boldFont;

	private Typeface lightFont;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapdisplayoptions);

		AssetManager assets = getAssets();
		boldFont = Font.getBold(assets);
		lightFont = Font.getLight(assets);

		View backButton = findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(MapDisplayActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						finish();
					}
				});
			}
		});

		final SharedPreferences prefs = getSharedPreferences(MAP_DISPLAY_PREFS,
				MODE_PRIVATE);
		
		User user = User.getCurrentUser(MapDisplayActivity.this);

		TextView userNameView = (TextView) findViewById(R.id.user_name);
		userNameView.setText(user.getFirstname() + " " + user.getLastname());

		TextView emailView = (TextView) findViewById(R.id.user_email);
		emailView.setText(user.getEmail());

		boolean calIntEnabled = isCalendarIntegrationEnabled(this);
		calendarIntegration = (ToggleButton) findViewById(R.id.calendar_integration);
		calendarIntegration.setChecked(calIntEnabled);
		calendarIntegration.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						prefs.edit()
								.putBoolean(CALENDAR_INTEGRATION, isChecked)
								.commit();
					}
				});

		boolean predictDestEnabled = isPredictDestEnabled(this);
		predictDest = (ToggleButton) findViewById(R.id.predict_destination);
		predictDest.setChecked(predictDestEnabled);
		predictDest.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				prefs.edit().putBoolean(PREDICT_DESTINATION, isChecked)
						.commit();
			}
		});
		
		findViewById(R.id.floating_menu_button).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						FloatingMenuDialog dialog = new FloatingMenuDialog(
								MapDisplayActivity.this);
						dialog.show();
					}
				});

		View tutorial = findViewById(R.id.tutorial);
		tutorial.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(MapDisplayActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
					    Misc.suppressTripInfoPanel(MapDisplayActivity.this);
						Intent tutorialActivity = new Intent(MapDisplayActivity.this,
								TutorialActivity.class);
						startActivity(tutorialActivity);
					}
				});
			}
		});
		
		View intro = findViewById(R.id.introduction_screens);
		intro.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(MapDisplayActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
					    Misc.suppressTripInfoPanel(MapDisplayActivity.this);
						Intent tutorialActivity = new Intent(MapDisplayActivity.this,
								IntroActivity.class);
						startActivity(tutorialActivity);
					}
				});
			}
		});

		View termsAndConditions = findViewById(R.id.terms_and_privacy);
		termsAndConditions.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(MapDisplayActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
					    Misc.suppressTripInfoPanel(MapDisplayActivity.this);
						Intent intent = new Intent(MapDisplayActivity.this, TermsAndPrivacyActivity.class);
						startActivity(intent);
					}
				});
			}
		});

		View helpOurResearch = findViewById(R.id.help_our_research);
		helpOurResearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(MapDisplayActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
					    Misc.suppressTripInfoPanel(MapDisplayActivity.this);
						Intent intent = new Intent(MapDisplayActivity.this, HelpOurResearchActivity.class);
						startActivity(intent);
					}
				});
			}
		});

		TextView veNum = (TextView) findViewById(R.id.version_number);
		try{
		    veNum.setText("Version " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		}catch(NameNotFoundException e){}
		
		TextView logout = (TextView) findViewById(R.id.logout);
		logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(MapDisplayActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
					    Misc.suppressTripInfoPanel(MapDisplayActivity.this);
					    User.logout(MapDisplayActivity.this);
			            Intent intent = new Intent(MapDisplayActivity.this, LandingActivity2.ENABLED?LandingActivity2.class:LandingActivity.class);
			            intent.putExtra(LandingActivity.LOGOUT, true);
			            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			            startActivity(intent);
			            finish();
					}
				});
			}
		});
		
		Font.setTypeface(boldFont, (TextView) findViewById(R.id.header), logout);
        Font.setTypeface(lightFont, userNameView, emailView, veNum,
				(TextView) findViewById(R.id.predict_destination_text),
				(TextView) findViewById(R.id.calendar_integration_text),
				(TextView) findViewById(R.id.tutorial),
				(TextView) findViewById(R.id.introduction_screens),
				(TextView) findViewById(R.id.terms_and_privacy),
				(TextView) findViewById(R.id.help_our_research));
        
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}

	public static boolean isCalendarIntegrationEnabled(Context ctx) {
		return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
				.getBoolean(CALENDAR_INTEGRATION, true);
	}

	public static boolean isNavigationTtsEnabled(Context ctx) {
		return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
				.getBoolean(NAVIGATION_TTS, true);
	}

	public static void setNavigationTts(Context ctx, boolean isEnabled) {
		ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE).edit()
				.putBoolean(NAVIGATION_TTS, isEnabled).commit();
	}

	public static boolean isPredictDestEnabled(Context ctx) {
		return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
				.getBoolean(PREDICT_DESTINATION, true);
	}

	public static boolean isLocBasedServiceEnabled(Context ctx) {
		return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
				.getBoolean(LOCATION_BASED_SERVICE, true);
	}
	
	public static int getValidatedTripsCount(Context ctx) {
		return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
				.getInt(VALIDATED_TRIPS_COUNT, 0);
	}

	public static void setValidatedTripsCount(Context ctx, int count) {
		ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE).edit()
				.putInt(VALIDATED_TRIPS_COUNT, count).commit();
	}

	public static String getHomeAddress(Context ctx) {
		return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
				.getString(HOME_ADDRESS, "");
	}

	public static void setHomeAddress(Context ctx, String address) {
		ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE).edit()
				.putString(HOME_ADDRESS, address).commit();
	}

	public static String getWorkAddress(Context ctx) {
		return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
				.getString(WORK_ADDRESS, "");
	}

	public static void setWorkAddress(Context ctx, String address) {
		ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE).edit()
				.putString(WORK_ADDRESS, address).commit();
	}

	public static ProfileSelectionDialog.Type getProfileSelection(Context ctx) {
		ProfileSelectionDialog.Type type = null;
		String typeStr = ctx.getSharedPreferences(MAP_DISPLAY_PREFS,
				MODE_PRIVATE).getString(PROFILE_SELECTION, "");
		if (StringUtils.isNotBlank(typeStr)) {
			try {
				type = ProfileSelectionDialog.Type.valueOf(typeStr);
			} catch (Throwable t) {
			}
		}
		return type;
	}

	public static void setProfileSelection(Context ctx,
			ProfileSelectionDialog.Type type) {
		ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
				.edit()
				.putString(PROFILE_SELECTION, type == null ? null : type.name())
				.commit();
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
        Misc.tripInfoPanelOnActivityStop(this);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra("display", 1);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();
	    Misc.tripInfoPanelOnActivityRestart(this);
	}

}