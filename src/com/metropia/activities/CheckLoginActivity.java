package com.metropia.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.localytics.android.Localytics;
import com.metropia.CrashlyticsUtils;
import com.metropia.models.User;
import com.metropia.requests.UserIdRequest;
import com.metropia.tasks.LoginTask;
import com.metropia.utils.Preferences;
import com.metropia.utils.Preferences.Global;

public class CheckLoginActivity extends FragmentActivity {

	private ImageView logo;
	private ImageView logoMask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Localytics.integrate(this);

		findViewById(R.id.progress).setVisibility(View.VISIBLE);
		
		logo = (ImageView) findViewById(R.id.imageViewLogo);
		logoMask = (ImageView) findViewById(R.id.logoMask);
		ViewTreeObserver vto = logo.getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				int logoHeight = Double
						.valueOf(logo.getMeasuredHeight() * 0.55).intValue();
				int logoWidth = logo.getMeasuredWidth();
				logoMask.setMaxHeight(logoHeight);
				logoMask.setMinimumHeight(logoHeight);
				logoMask.setMaxWidth(logoWidth);
				logoMask.setMinimumWidth(logoWidth);
				return true;
			}
		});
		Animation slideUpAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slideup);
		slideUpAnimation.setFillAfter(true);
		slideUpAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				Runnable loginAndDoCallback = new Runnable() {
					@Override
					public void run() {
						if (User.getCurrentUser(CheckLoginActivity.this) != null) {
							startLandingPage();
						} else {
							SharedPreferences loginPrefs = Preferences
									.getAuthPreferences(CheckLoginActivity.this);
							final String username = loginPrefs.getString(
									User.USERNAME, "");
							final String password = loginPrefs.getString(
									User.PASSWORD, "");
							if (!username.equals("") && !password.equals("")) {
								final String gcmRegistrationId = Preferences
										.getGlobalPreferences(
												CheckLoginActivity.this)
										.getString(Preferences.Global.GCM_REG_ID, "");
								final LoginTask loginTask = new LoginTask(
										CheckLoginActivity.this, username,
										password, gcmRegistrationId) {
									@Override
									protected void onPostLogin(final User user) {
										if (user != null && user.getId() != -1) {
											User.setCurrentUser(
													CheckLoginActivity.this,
													user);
											CrashlyticsUtils.initUserInfo(user);
											startLandingPage();
										} else {
											startLoginPage();
										}
									}
								}.setDialogEnabled(false);
								new AsyncTask<Void, Void, Integer>() {
									@Override
									protected Integer doInBackground(
											Void... params) {
										Integer id = null;
										try {
											UserIdRequest req = new UserIdRequest(
													username);
											req.invalidateCache(CheckLoginActivity.this);
											id = req.execute(CheckLoginActivity.this);
										} catch (Exception e) {
										}
										return id;
									}

									protected void onPostExecute(Integer userId) {
										if (userId != null) {
											loginTask.setUserId(userId)
													.execute();
										} else {
											startLoginPage();
										}
									}
								}.execute();
							} else {
								startLoginPage();
							}
						}
					}
				};
				MainActivity.initApiLinksIfNecessary(CheckLoginActivity.this,
						loginAndDoCallback);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		
		logoMask.startAnimation(slideUpAnimation);
	}

	private void startLandingPage() {
		Intent landing = new Intent(CheckLoginActivity.this,
				LandingActivity2.class);
		landing.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(landing);
		finish();
	}

	private void startLoginPage() {
		Intent landing = new Intent(CheckLoginActivity.this,
				LoginActivity.class);
		landing.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(landing);
		finish();
	}
	
	@Override
	public void onPause() {
	    Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
	    super.onPause();
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

}
