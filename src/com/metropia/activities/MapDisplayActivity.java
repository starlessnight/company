package com.metropia.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

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
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.internal.nineoldandroids.animation.Animator;
import com.actionbarsherlock.internal.nineoldandroids.animation.Animator.AnimatorListener;
import com.actionbarsherlock.internal.nineoldandroids.animation.AnimatorSet;
import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.LocalyticsUtils;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.dialogs.ProfileSelectionDialog;
import com.metropia.models.ReservationTollHovInfo;
import com.metropia.models.User;
import com.metropia.ui.ClickAnimation;
import com.metropia.ui.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;

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
	
	public static final String OS_NAME = "android";

	private static final String CALENDAR_INTEGRATION = "CALENDAR_INTEGRATION";

	private static final String NAVIGATION_TTS = "NAVIGATION_TTS";

	private static final String LOCATION_BASED_SERVICE = "LOCATION_BASED_SERVICE";

	private static final String VALIDATED_TRIPS_COUNT = "VALIDATED_TRIPS_COUNT";

	private static final String HOME_ADDRESS = "HOME_ADDRESS";

	private static final String WORK_ADDRESS = "WORK_ADDRESS";

	private static final String PROFILE_SELECTION = "PROFILE_SELECTION";

	private static final String PREDICT_DESTINATION = "PREDICT_DESTINATION";
	
	private static final String INCLUDE_TOLL_ROADS = "INCLUDE_TOLL_ROADS";
	
	private ToggleButton calendarIntegration;

	private ToggleButton predictDest;
	
	private ToggleButton includeTollRoads;
	
	private Typeface boldFont;

	private Typeface lightFont;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapdisplayoptions);
		
		// Integrate Localytics
		Localytics.integrate(this);

		AssetManager assets = getAssets();
		boldFont = Font.getRobotoBold(assets);
		lightFont = Font.getMedium(assets);

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
		userNameView.setText(StringUtils.equals(user.getType(), User.FACEBOOK)? user.getName():user.getUsername());

		/*
		TextView emailView = (TextView) findViewById(R.id.user_email);
		emailView.setText(user.getEmail());
		*/

		boolean calIntEnabled = isCalendarIntegrationEnabled(this);
		calendarIntegration = (ToggleButton) findViewById(R.id.calendar_integration);
		calendarIntegration.setChecked(calIntEnabled);
		calendarIntegration.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						LocalyticsUtils.tagCalendarIntegrationSettings(isChecked);
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
		
		boolean includeTollRoadsEnabled = isIncludeTollRoadsEnabled(this);
		includeTollRoads = (ToggleButton) findViewById(R.id.include_toll_roads);
		includeTollRoads.setChecked(includeTollRoadsEnabled);
		includeTollRoads.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				setIncludeTollRoadsEnabled(MapDisplayActivity.this, isChecked);
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
		    veNum.setText("V. " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
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
			            Intent intent = new Intent(MapDisplayActivity.this, LandingActivity2.class);
			            intent.putExtra(LandingActivity2.LOGOUT, true);
			            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			            startActivity(intent);
			            finish();
					}
				});
			}
		});
		
		final View tutorialPanel = findViewById(R.id.tutorial_panel);
		final View firstPanel = findViewById(R.id.first_column);
		tutorialPanel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ObjectAnimator tutorialFlipAnimator = ObjectAnimator.ofFloat(tutorialPanel, "rotationY", 0f, -180f);
	    		tutorialFlipAnimator.setDuration(500);
	    		tutorialFlipAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator tutorialAlphaAnimator = ObjectAnimator.ofFloat(tutorialPanel, "alpha", 1f, 0f);
				tutorialAlphaAnimator.setStartDelay(250);
				tutorialAlphaAnimator.setDuration(1);
				tutorialAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator columnShowAnimator = ObjectAnimator.ofFloat(firstPanel, "alpha", 1f, 0f);
				columnShowAnimator.setDuration(0);
				columnShowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator columnFlipAnimator = ObjectAnimator.ofFloat(firstPanel, "rotationY", 180f, 0f);
				columnFlipAnimator.setDuration(500);
				columnFlipAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator columnAlphaAnimator = ObjectAnimator.ofFloat(firstPanel, "alpha", 0f, 1f);
				columnAlphaAnimator.setStartDelay(250);
				columnAlphaAnimator.setDuration(1);
				columnAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				AnimatorSet allAnimatorSet = new AnimatorSet();
				allAnimatorSet.playTogether(tutorialFlipAnimator, tutorialAlphaAnimator, columnFlipAnimator, columnAlphaAnimator, columnShowAnimator);
				allAnimatorSet.addListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {}

					@Override
					public void onAnimationEnd(Animator animation) {
						tutorialPanel.setVisibility(View.GONE);
					}

					@Override
					public void onAnimationCancel(Animator animation) {}

					@Override
					public void onAnimationRepeat(Animator animation) {}
					
				});
				allAnimatorSet.start();
			}
		});
		
		findViewById(R.id.predict_destination_tutorial_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LayoutParams tutorialPanelLp = tutorialPanel.getLayoutParams();
				tutorialPanelLp.height = firstPanel.getMeasuredHeight();
				tutorialPanelLp.width = LayoutParams.MATCH_PARENT;
				tutorialPanel.setLayoutParams(tutorialPanelLp);
				ObjectAnimator tutorialShowAnimator = ObjectAnimator.ofFloat(tutorialPanel, "alpha", 1f, 0f);
				tutorialShowAnimator.setDuration(0);
				tutorialShowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator tutorialFlipAnimator = ObjectAnimator.ofFloat(tutorialPanel, "rotationY", -180f, 0f);
	    		tutorialFlipAnimator.setDuration(500);
	    		tutorialFlipAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator tutorialAlphaAnimator = ObjectAnimator.ofFloat(tutorialPanel, "alpha", 0f, 1f);
				tutorialAlphaAnimator.setStartDelay(250);
				tutorialAlphaAnimator.setDuration(1);
				tutorialAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator columnFlipAnimator = ObjectAnimator.ofFloat(firstPanel, "rotationY", 0f, 180f);
				columnFlipAnimator.setDuration(500);
				columnFlipAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator columnAlphaAnimator = ObjectAnimator.ofFloat(firstPanel, "alpha", 1f, 0f);
				columnAlphaAnimator.setStartDelay(250);
				columnAlphaAnimator.setDuration(1);
				columnAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				AnimatorSet allAnimatorSet = new AnimatorSet();
				allAnimatorSet.playTogether(tutorialFlipAnimator, tutorialAlphaAnimator, tutorialShowAnimator, columnFlipAnimator, columnAlphaAnimator);
				allAnimatorSet.addListener(new AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
						tutorialPanel.setVisibility(View.VISIBLE);
					}

					@Override
					public void onAnimationEnd(Animator animation) {}

					@Override
					public void onAnimationCancel(Animator animation) {}

					@Override
					public void onAnimationRepeat(Animator animation) {}
					
				});
				allAnimatorSet.start();
			}
		});
		
		final View calendarIntegrationTutorialPanel = findViewById(R.id.calendar_integration_tutorial_panel);
		calendarIntegrationTutorialPanel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ObjectAnimator tutorialFlipAnimator = ObjectAnimator.ofFloat(calendarIntegrationTutorialPanel, "rotationY", 0f, -180f);
	    		tutorialFlipAnimator.setDuration(500);
	    		tutorialFlipAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator tutorialAlphaAnimator = ObjectAnimator.ofFloat(calendarIntegrationTutorialPanel, "alpha", 1f, 0f);
				tutorialAlphaAnimator.setStartDelay(250);
				tutorialAlphaAnimator.setDuration(1);
				tutorialAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator columnShowAnimator = ObjectAnimator.ofFloat(firstPanel, "alpha", 1f, 0f);
				columnShowAnimator.setDuration(0);
				columnShowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator columnFlipAnimator = ObjectAnimator.ofFloat(firstPanel, "rotationY", 180f, 0f);
				columnFlipAnimator.setDuration(500);
				columnFlipAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator columnAlphaAnimator = ObjectAnimator.ofFloat(firstPanel, "alpha", 0f, 1f);
				columnAlphaAnimator.setStartDelay(250);
				columnAlphaAnimator.setDuration(1);
				columnAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				AnimatorSet allAnimatorSet = new AnimatorSet();
				allAnimatorSet.playTogether(tutorialFlipAnimator, tutorialAlphaAnimator, columnFlipAnimator, columnAlphaAnimator, columnShowAnimator);
				allAnimatorSet.addListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {}

					@Override
					public void onAnimationEnd(Animator animation) {
						calendarIntegrationTutorialPanel.setVisibility(View.GONE);
					}

					@Override
					public void onAnimationCancel(Animator animation) {}

					@Override
					public void onAnimationRepeat(Animator animation) {}
					
				});
				allAnimatorSet.start();
			}
		});
		
		findViewById(R.id.calendar_integration_tutorial_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LayoutParams tutorialPanelLp = calendarIntegrationTutorialPanel.getLayoutParams();
				tutorialPanelLp.height = firstPanel.getMeasuredHeight();
				tutorialPanelLp.width = LayoutParams.MATCH_PARENT;
				calendarIntegrationTutorialPanel.setLayoutParams(tutorialPanelLp);
				ObjectAnimator tutorialShowAnimator = ObjectAnimator.ofFloat(calendarIntegrationTutorialPanel, "alpha", 1f, 0f);
				tutorialShowAnimator.setDuration(0);
				tutorialShowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator tutorialFlipAnimator = ObjectAnimator.ofFloat(calendarIntegrationTutorialPanel, "rotationY", -180f, 0f);
	    		tutorialFlipAnimator.setDuration(500);
	    		tutorialFlipAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator tutorialAlphaAnimator = ObjectAnimator.ofFloat(calendarIntegrationTutorialPanel, "alpha", 0f, 1f);
				tutorialAlphaAnimator.setStartDelay(250);
				tutorialAlphaAnimator.setDuration(1);
				tutorialAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator columnFlipAnimator = ObjectAnimator.ofFloat(firstPanel, "rotationY", 0f, 180f);
				columnFlipAnimator.setDuration(500);
				columnFlipAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				ObjectAnimator columnAlphaAnimator = ObjectAnimator.ofFloat(firstPanel, "alpha", 1f, 0f);
				columnAlphaAnimator.setStartDelay(250);
				columnAlphaAnimator.setDuration(1);
				columnAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				AnimatorSet allAnimatorSet = new AnimatorSet();
				allAnimatorSet.playTogether(tutorialFlipAnimator, tutorialAlphaAnimator, tutorialShowAnimator, columnFlipAnimator, columnAlphaAnimator);
				allAnimatorSet.addListener(new AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
						calendarIntegrationTutorialPanel.setVisibility(View.VISIBLE);
					}

					@Override
					public void onAnimationEnd(Animator animation) {}

					@Override
					public void onAnimationCancel(Animator animation) {}

					@Override
					public void onAnimationRepeat(Animator animation) {}
					
				});
				allAnimatorSet.start();
			}
		});
		
		Font.setTypeface(boldFont, logout, veNum, userNameView, 
				(TextView) findViewById(R.id.header),
				(TextView) findViewById(R.id.metropia_title),
				(TextView) findViewById(R.id.back_button), 
				(TextView) findViewById(R.id.tutorial_text), 
				(TextView) findViewById(R.id.calendar_integration_tutorial_text));
        Font.setTypeface(lightFont, 
				(TextView) findViewById(R.id.predict_destination_text),
				(TextView) findViewById(R.id.calendar_integration_text),
				(TextView) findViewById(R.id.tutorial),
				(TextView) findViewById(R.id.introduction_screens),
				(TextView) findViewById(R.id.terms_and_privacy),
				(TextView) findViewById(R.id.help_our_research), 
				(TextView) findViewById(R.id.include_toll_roads_text));
        
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
	
	public static boolean isIncludeTollRoadsEnabled(Context ctx) {
		return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE).getBoolean(INCLUDE_TOLL_ROADS, true);
	}
	
	public static void setIncludeTollRoadsEnabled(Context ctx, boolean includeToll) {
		ctx.getSharedPreferences(MAP_DISPLAY_PREFS,	MODE_PRIVATE).edit().putBoolean(INCLUDE_TOLL_ROADS, includeToll).commit();
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
	
	public static void addReservationTollHovInfo(Context ctx, ReservationTollHovInfo info) {
		try {
			File file = getReservationTollHovInfoFile(ctx);
			JSONObject infos;
			if(!file.exists()) {
				infos = new JSONObject();
			}
			else {
				infos = new JSONObject(FileUtils.readFileToString(file));
			}
			
			if(infos.has(info.getReservationId() + "")) {
				infos.remove(info.getReservationId() + "");
			}
			infos.put(info.getReservationId() + "", info.toJSONObject());
			FileUtils.writeStringToFile(getReservationTollHovInfoFile(ctx), infos.toString());
		}
		catch(Exception ignore){}
	}
	
	public static void cleanReservationTollHovInfoBeforeId(Context ctx, Long reservId) {
		try {
			File file = getReservationTollHovInfoFile(ctx);
			JSONObject infos = new JSONObject(FileUtils.readFileToString(file));
			Iterator<String> keys = infos.keys();
			List<String> removeKeys = new ArrayList<String>();
			while(keys.hasNext()) {
				String key = keys.next();
				if(Long.valueOf(key) < reservId) {
					removeKeys.add(key);
				}
			}
			for(String removeKey : removeKeys) {
				infos.remove(removeKey);
			}
			FileUtils.writeStringToFile(getReservationTollHovInfoFile(ctx), infos.toString());
		}
		catch(Exception ignore){}
	}
	
	public static ReservationTollHovInfo getReservationTollHovInfo(Context ctx, Long reservationId) {
		ReservationTollHovInfo info = new ReservationTollHovInfo(reservationId);
		info.setIncludeToll(isIncludeTollRoadsEnabled(ctx));
		try {
			JSONObject infos = new JSONObject(FileUtils.readFileToString(getReservationTollHovInfoFile(ctx)));
			if(infos.has(reservationId + "")) {
				ReservationTollHovInfo temp = ReservationTollHovInfo.parse(infos.optJSONObject(reservationId + ""));
				if(temp != null) {
					info = temp;
				}
			}
		}
		catch(Exception ignore) {}
		return info;
	}
	
	private static File getReservationTollHovInfoFile(Context ctx) {
		return new File(ctx.getExternalFilesDir(null), "reservation_toll_hov");
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
		Localytics.openSession();
		Localytics.tagScreen(this.getClass().getSimpleName());
	    Localytics.upload();
	    Localytics.setInAppMessageDisplayActivity(this);
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
	}

	@Override
	protected void onPause() {
	    Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
		super.onPause();
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();
	    Misc.tripInfoPanelOnActivityRestart(this);
	}

}
