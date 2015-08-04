package com.metropia.activities;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import twitter4j.TwitterException;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.plus.PlusShare;
import com.localytics.android.Localytics;
import com.metropia.LocalyticsUtils;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.ui.ClickAnimation;
import com.metropia.ui.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;
import com.twitter.android.TwitterApp;
import com.twitter.android.TwitterApp.TwDialogListener;

public final class ShareActivity extends FragmentActivity {

    public static final String TITLE = "TITLE";
    
    public static final String SHARE_TEXT = "SHARE_TEXT";
    
	private static final String FB_PERMISSIONS = "publish_actions";
	private static final int GOOGLE_PLUS_REQ = 7;

	private Typeface boldFont;
	private Typeface lightFont;

	private boolean fbPending;
	private boolean fbClicked;

	private String title;
	private String shareText;
	private CallbackManager callbackManager;
	
	private TextView facebookView;
	private TextView googlePlusView;
	private TextView twitterView;
	private TextView textMessageView;
	private TextView emailView;
	private TextView shareButtonView;
	
	private enum ShareType {
		googlePlus, twitter, facebook, textMessage, email;
	}

	/*private Session.StatusCallback fbCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};*/

	private TwitterApp mTwitter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share);
		
		Localytics.integrate(this);

		AssetManager assets = getAssets();
		boldFont = Font.getBold(assets);
		lightFont = Font.getLight(assets);

//		User user = User.getCurrentUser(ShareActivity.this);

		Intent intent = getIntent();
		shareText = intent.getStringExtra(SHARE_TEXT);
		title = intent.getStringExtra(TITLE);

		TextView backButton = (TextView) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(ShareActivity.this, R.anim.click_animation));
				finish();
			}
		});

//		TextView userNameView = (TextView) findViewById(R.id.user_name);
//		userNameView.setText(user.getFirstname() + " " + user.getLastname());
		
		shareButtonView = (TextView) findViewById(R.id.share_button);
		shareButtonView.setTag(ShareType.facebook);
		shareButtonView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(ShareActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						if(shareButtonView.getTag() != null) {
							share((ShareType)shareButtonView.getTag());
						}
					}
				});
			}
		});
		
		facebookView = (TextView) findViewById(R.id.facebook_text);
		facebookView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(ShareActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						deselectAll();
						facebookView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.share_fasebook_select, 0, 0);
						shareButtonView.setTag(ShareType.facebook);
					}
				});
			}
		});

		mTwitter = new TwitterApp(ShareActivity.this, getString(R.string.twitter_key),
				getString(R.string.twitter_secret));
		mTwitter.setListener(new TwDialogListener() {
			@Override
			public void onError(String value) {
				if (!"Error getting access token".equals(value)) {
					mTwitter.resetAccessToken();
					mTwitter.authorize();
					Toast.makeText(ShareActivity.this, "wrong username and/or password",
							Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onComplete(String value) {
				updateTwitterStatus();
			}
		});
		
		twitterView = (TextView) findViewById(R.id.twitter_text); 
		twitterView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(ShareActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						deselectAll();
						twitterView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.share_twitter_select, 0, 0);
						shareButtonView.setTag(ShareType.twitter);
					}
				});
			}
		});

		googlePlusView = (TextView) findViewById(R.id.google_plus_text); 
		googlePlusView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(ShareActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						deselectAll();
						googlePlusView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.share_google_plus_select, 0, 0);
						shareButtonView.setTag(ShareType.googlePlus);
					}
				});
			}
		});

		textMessageView = (TextView) findViewById(R.id.text_message_text); 
		textMessageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			    ClickAnimation clickAnimation = new ClickAnimation(ShareActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						deselectAll();
						textMessageView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.share_sms_select, 0, 0);
						shareButtonView.setTag(ShareType.textMessage);
					}
				});
			}
		});

		emailView = (TextView) findViewById(R.id.email_text);
		emailView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(ShareActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						deselectAll();
						emailView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.share_email_select, 0, 0);
						shareButtonView.setTag(ShareType.email);
					}
				});
			}
		});

		Font.setTypeface(lightFont, (TextView) findViewById(R.id.share_good_news),
				    facebookView, twitterView, googlePlusView, textMessageView, emailView, shareButtonView);
		
		FacebookSdk.sdkInitialize(this);
		callbackManager = CallbackManager.Factory.create();
		
		//init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}

	private void publishFB() {
		Bundle parameters = new Bundle();
		parameters.putString("link", "https://dl.dropboxusercontent.com/u/22414157/appLink.html");
		parameters.putString("message", shareText);
		
		GraphRequest request = GraphRequest.newPostRequest(AccessToken.getCurrentAccessToken(), "me/feed", new JSONObject(), new GraphRequest.Callback() {
			public void onCompleted(GraphResponse response) {
				findViewById(R.id.loading).setVisibility(View.GONE);
				if (response.getError()==null) {
					displaySharedNotification();
				}
				else {
					Log.e("log", response.toString());}
				}
		});
		request.setParameters(parameters);
		request.executeAsync();
		findViewById(R.id.loading).setVisibility(View.VISIBLE);
	}

	private void checkPermission() {
		if (!AccessToken.getCurrentAccessToken().getPermissions().contains(FB_PERMISSIONS))
			LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList(FB_PERMISSIONS));
		else publishFB();
	}
	
	private void fbLogin() {
		LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult result) {Log.d("log", "success");checkPermission();}
			@Override
			public void onCancel() {Log.d("log", "cancel");}
			@Override
			public void onError(FacebookException error) {Log.d("log", "error");}
		});
		
		if (AccessToken.getCurrentAccessToken()==null || AccessToken.getCurrentAccessToken().isExpired())
			LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"));
		else checkPermission();
	}

	private boolean isNotLoading() {
		return findViewById(R.id.loading).getVisibility() != View.VISIBLE;
	}

	private void updateTwitterStatus() {
		if (mTwitter.hasAccessToken() == true) {
			final View loading = findViewById(R.id.loading);
			AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(Void... params) {
					boolean success;
					try {
						mTwitter.updateStatus(shareText);
						success = true;
					} catch (Exception e) {
						success = false;
						if (e instanceof TwitterException) {
							TwitterException te = (TwitterException) e;
							if (te.getErrorCode() == 187) {
								success = true;
							}
						}
					}
					return success;
				}

				@Override
				protected void onPostExecute(Boolean success) {
					loading.setVisibility(View.GONE);
					if (success) {
						displaySharedNotification();
					} else {
						mTwitter.resetAccessToken();
						mTwitter.authorize();
					}
				}
			};
			Misc.parallelExecute(task);
			loading.setVisibility(View.VISIBLE);
		} else {
			mTwitter.authorize();
		}
	}

	private void displaySharedNotification() {
		if (ShareActivity.this != null) {
			NotificationDialog2 dialog = new NotificationDialog2(ShareActivity.this, "A message has been posted to your wall. Thanks for sharing Metropia!");
			dialog.setTitle("");
			dialog.setPositiveButtonText("OK");
			dialog.show();
		}
	}
	
	private void deselectAll() {
		facebookView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.share_fasebook_unselect, 0, 0);
		googlePlusView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.share_google_plus_unselect, 0, 0);
		twitterView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.share_twitter_unselect, 0, 0);
		textMessageView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.share_sms_unselect, 0, 0);
		emailView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.share_email_unselect, 0, 0);
		shareButtonView.setTag(null);
	}
	
	private void share(ShareType type) {
		switch(type) {
			case googlePlus:
				if (isNotLoading()) {
					LocalyticsUtils.tagSocialSharing(LocalyticsUtils.GOOGLE_PLUS);
					int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ShareActivity.this);
					if (!StringUtils.equalsIgnoreCase(GooglePlayServicesUtil.getErrorString(errorCode), "success")) {
						GooglePlayServicesUtil.getErrorDialog(errorCode,
								ShareActivity.this, 0).show();
					} else {
					    Misc.suppressTripInfoPanel(ShareActivity.this);
						Intent shareIntent = new PlusShare.Builder(
								ShareActivity.this).setType("text/plain")
								.setText(shareText).getIntent();
						startActivityForResult(shareIntent, GOOGLE_PLUS_REQ);
					}
				}
				break;
			case twitter:
				if (isNotLoading()) {
					LocalyticsUtils.tagSocialSharing(LocalyticsUtils.TWITTER);
					updateTwitterStatus();
				}
				break;
			case facebook:
				fbClicked = true;
				if (isNotLoading()) {
					LocalyticsUtils.tagSocialSharing(LocalyticsUtils.FACEBOOK);
					fbLogin();
					/*Session session = Session.getActiveSession();
					if (session != null && session.isOpened()) {
						publishFB();
					} else {
					    Misc.suppressTripInfoPanel(ShareActivity.this);
						fbPending = true;
						fbLogin();
					}*/
				}
				break;
			case textMessage:
				if (isNotLoading()) {
					LocalyticsUtils.tagSocialSharing(LocalyticsUtils.TEXT_MESSAGE);
				    Misc.suppressTripInfoPanel(ShareActivity.this);
					Intent sendIntent = new Intent(Intent.ACTION_VIEW);
					sendIntent.putExtra("sms_body", shareText);
					sendIntent.setType("vnd.android-dir/mms-sms");
					sendIntent.setData(Uri.parse("smsto:"));
					startActivity(Intent.createChooser(sendIntent, title));
				}
				break;
			case email:
				if (isNotLoading()) {
					LocalyticsUtils.tagSocialSharing(LocalyticsUtils.EMAIL);
				    Misc.suppressTripInfoPanel(ShareActivity.this);
					Intent sendIntent = new Intent(Intent.ACTION_SEND);
					sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
					sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
					sendIntent.setType("message/rfc822");
					startActivity(Intent.createChooser(sendIntent, title));
				}
				break;
		}
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GOOGLE_PLUS_REQ && resultCode == Activity.RESULT_OK){
            //finish();
        }
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
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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

}
