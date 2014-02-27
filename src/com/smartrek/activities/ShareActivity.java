package com.smartrek.activities;

import java.util.Arrays;

import twitter4j.TwitterException;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.google.android.gms.plus.GooglePlusUtil;
import com.google.android.gms.plus.PlusShare;
import com.smartrek.models.User;
import com.smartrek.utils.Font;
import com.smartrek.utils.Misc;
import com.twitter.android.TwitterApp;
import com.twitter.android.TwitterApp.TwDialogListener;

public final class ShareActivity extends FragmentActivity {

	private static final String FB_PERMISSIONS = "publish_actions";
	private static final int GOOGLE_PLUS_REQ = 7;

	private Typeface boldFont;
	private Typeface lightFont;

	private boolean fbPending;
	private boolean fbClicked;

	private String title;
	private String shareText;
	private UiLifecycleHelper uiHelper;

	private Session.StatusCallback fbCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private TwitterApp mTwitter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share);

		AssetManager assets = getAssets();
		boldFont = Font.getBold(assets);
		lightFont = Font.getLight(assets);

		User user = User.getCurrentUser(ShareActivity.this);

		shareText = "I helped solve traffic congestion using Metropia Mobile!"
				+ "\n\n" + Misc.getGooglePlayAppUrl(ShareActivity.this);
		title = user.getFirstname() + " " + user.getLastname()
				+ " is on the way";

		TextView backButton = (TextView) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		TextView userNameView = (TextView) findViewById(R.id.user_name);
		userNameView.setText(user.getFirstname() + " " + user.getLastname());

		findViewById(R.id.facebook).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fbClicked = true;
				if (isNotLoading()) {
					Session session = Session.getActiveSession();
					if (session != null && session.isOpened()) {
						publishFB();
					} else {
						fbPending = true;
						fbLogin();
					}
				}
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
		findViewById(R.id.twitter).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isNotLoading()) {
					updateTwitterStatus();
				}
			}
		});

		findViewById(R.id.google_plus).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isNotLoading()) {
					int errorCode = GooglePlusUtil
							.checkGooglePlusApp(ShareActivity.this);
					if (errorCode != GooglePlusUtil.SUCCESS) {
						GooglePlusUtil.getErrorDialog(errorCode,
								ShareActivity.this, 0).show();
					} else {
						Intent shareIntent = new PlusShare.Builder(
								ShareActivity.this).setType("text/plain")
								.setText(shareText).getIntent();
						startActivityForResult(shareIntent, GOOGLE_PLUS_REQ);
					}
				}
			}
		});

		findViewById(R.id.text_message).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isNotLoading()) {
					Intent sendIntent = new Intent(Intent.ACTION_VIEW);
					sendIntent.putExtra("sms_body", shareText);
					sendIntent.setType("vnd.android-dir/mms-sms");
					startActivity(Intent.createChooser(sendIntent, title));
				}
			}
		});

		findViewById(R.id.email).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isNotLoading()) {
					Intent sendIntent = new Intent(Intent.ACTION_SEND);
					sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
					sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
					sendIntent.setType("message/rfc822");
					startActivity(Intent.createChooser(sendIntent, title));
				}
			}
		});

		Font.setTypeface(lightFont, userNameView, (TextView) findViewById(R.id.share_good_news),
				                    (TextView) findViewById(R.id.metropians_less_traffic), 
				                    (TextView) findViewById(R.id.facebook_text), 
				                    (TextView) findViewById(R.id.twitter_text),
				                    (TextView) findViewById(R.id.google_plus_text), 
				                    (TextView) findViewById(R.id.text_message_text), 
				                    (TextView) findViewById(R.id.email_text));

		uiHelper = new UiLifecycleHelper(ShareActivity.this, fbCallback);
		uiHelper.onCreate(savedInstanceState);
	}

	private void publishFB() {
		final Session session = Session.getActiveSession();
		if (session != null && ShareActivity.this != null) {
			final View loading = findViewById(R.id.loading);
			Request request = Request.newStatusUpdateRequest(session,
					shareText, new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							loading.setVisibility(View.GONE);
							FacebookRequestError error = response.getError();
							if (error != null && error.getErrorCode() != 506) {
								fbPending = true;
								session.closeAndClearTokenInformation();
							} else {
								displaySharedNotification();
							}
						}
					});
			request.executeAsync();
			loading.setVisibility(View.VISIBLE);
		}
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (fbClicked) {
			if (state == SessionState.OPENED_TOKEN_UPDATED) {
				if (hasPublishPermission()) {
					publishFB();
				} else {
					session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
							this, Arrays.asList(FB_PERMISSIONS)));
				}
			} else if (state == SessionState.OPENED	&& (fbPending || hasPublishPermission())) {
				fbPending = false;
				if (hasPublishPermission()) {
					publishFB();
				} else {
					session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
							this, Arrays.asList(FB_PERMISSIONS)));
				}
			} else if (state == SessionState.CLOSED && fbPending) {
				fbLogin();
			}
		}
	}

	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null && session.getPermissions().contains(FB_PERMISSIONS);
	}

	private boolean isNotLoading() {
		return findViewById(R.id.loading).getVisibility() != View.VISIBLE;
	}

	private void fbLogin() {
		try {
			Session.openActiveSession(ShareActivity.this, true, fbCallback);
		} catch (Throwable t) {
		}
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
			Toast.makeText(ShareActivity.this, "shared", Toast.LENGTH_SHORT)
					.show();
		}
	}
	
   @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GOOGLE_PLUS_REQ && resultCode == Activity.RESULT_OK){
            //finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

}
