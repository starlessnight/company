package com.metropia.activities;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.littlefluffytoys.littlefluffylocationlibrary.PassiveLocationChangedReceiver;
import com.localytics.android.Localytics;
import com.metropia.CrashlyticsUtils;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.dialogs.CancelableProgressDialog;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.models.User;
import com.metropia.requests.CityRequest;
import com.metropia.requests.CityRequest.City;
import com.metropia.requests.Request;
import com.metropia.requests.UserIdRequest;
import com.metropia.tasks.LoginFBTask;
import com.metropia.tasks.LoginTask;
import com.metropia.tasks.LoginTaskNew;
import com.metropia.ui.LoginPager;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.HTTP;
import com.metropia.utils.LocationService;
import com.metropia.utils.Misc;
import com.metropia.utils.Preferences;

public final class LoginActivity extends FragmentActivity implements OnClickListener,
        TextWatcher {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	private CallbackManager callbackManager;
    
	private LoginPager loginPager;
	private EditText editTextUsername;
	private EditText editTextPassword;
	
	private static final Integer TWENTY_SECONDS = 20 * 1000; 
	private AtomicBoolean cancelWait = new AtomicBoolean(false);
	
	int[] clickable = {R.id.fb_login_button, R.id.login_button, R.id.formTrigger, R.id.new_user, R.id.forget_pwd, R.id.login_back};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);
        
        Localytics.integrate(this);
        User.logout(this);
        
        loginPager = (LoginPager) findViewById(R.id.LoginLayout);
        loginPager.setAdapter(R.id.page1, R.id.page2);
        
        /* If it hasn't set up the login screen */
        //TextView login = (TextView) findViewById(R.id.login_button);
        for (int i=0 ; i<clickable.length; i++) findViewById(clickable[i]).setOnClickListener(this);
        
        
        TextView newUser = (TextView) findViewById(R.id.new_user);
        /*SpannableString createAccount = new SpannableString("Or Create Account");
        ClickableSpan create = new ClickableSpan() {
			@Override
			public void onClick(View view) {
				
			}
        };
        createAccount.setSpan(create, 3, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //newUser.setText(createAccount);
        newUser.setMovementMethod(LinkMovementMethod.getInstance());
        newUser.setLinkTextColor(Color.BLACK);*/
        
        editTextUsername = (EditText) findViewById(R.id.username_box);
        editTextUsername.addTextChangedListener(this);
        editTextPassword = (EditText) findViewById(R.id.pwd_box);
        editTextPassword.addTextChangedListener(this);
        
        SharedPreferences loginPrefs = Preferences.getAuthPreferences(this);
        String username = loginPrefs.getString(User.USERNAME, "");
        String type = loginPrefs.getString(User.TYPE, "");
        if(StringUtils.isNotBlank(username) && StringUtils.equals(type, "")) {
        	editTextUsername.setText(username);
        }
        
        AssetManager assets = getAssets();
        Font.setTypeface(Font.getBold(assets));
        //Font.setTypeface(Font.getLight(assets), editTextUsername, editTextPassword, login, newUser, (TextView) findViewById(R.id.forget_pwd));
        
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
      	
      	FacebookSdk.sdkInitialize(this);
        callbackManager = CallbackManager.Factory.create();
      	locationService.init(this);
    }
    
    private AtomicBoolean cityChecked = new AtomicBoolean(false);
    private AtomicBoolean serviceArea = new AtomicBoolean(false);
    private String registerUrl;
    
    
    boolean restrictedMode = false;
    private void checkCity(final double lat, final double lon) {
    	AsyncTask<Void, Void, City> checkCityAvailability = new AsyncTask<Void, Void, City>(){
            @Override
            protected City doInBackground(Void... params) {
                City result;
                try{
                    CityRequest req = new CityRequest(lat, lon, HTTP.defaultTimeout);
                    req.invalidateCache(LoginActivity.this);
                    result = req.execute(LoginActivity.this);
                }catch(Throwable t){
                	Log.d("LoginActivity", Log.getStackTraceString(t));
                    result = null;
                }
                return result;
            }
            @Override
            protected void onPostExecute(City result) {
            	if (result!=null && StringUtils.equals(result.signUp, "http://www.metropia.com/elpasolite")) restrictedMode = true;
                if (result != null && StringUtils.isNotBlank(result.html)) {
                	serviceArea.set(false);
                	registerUrl = result.link;
                }else{
                	serviceArea.set(result != null ? true : false);
                	registerUrl = result != null ? result.signUp : "";
                }
                cityChecked.set(true);
            }
        };
        Misc.parallelExecute(checkCityAvailability);
	}

	@Override
	public void onClick(View v) {
		
		
		
		switch(v.getId()) {
			case R.id.fb_login_button:
				
			break;
			case R.id.login_button:
				
			break;
			case R.id.formTrigger:
				loginPager.setCurrentItem(1);
				TextView account = (TextView)findViewById(R.id.username_box);
				hsowKeyboard(account);
				if (!account.getText().toString().equals("")) findViewById(R.id.pwd_box).requestFocus();
				
				return;
			case R.id.login_back:
				onBackPressed();
				return;
			case R.id.new_user:
				
				Misc.parallelExecute(new AsyncTask<Void, Void, Void>() {
					CancelableProgressDialog dialog;
					
					@Override
					protected void onPreExecute() {
						cancelWait.set(false);
						dialog = new CancelableProgressDialog(LoginActivity.this, "Preparing...");
			            dialog.setActionListener(new CancelableProgressDialog.ActionListener() {
			                @Override
			                public void onClickNegativeButton() {
			                	if(dialog.isShowing()) {
			                		Misc.doQuietly(new Runnable() {
										@Override
										public void run() {
											dialog.dismiss();
										}
									});
			                		cancelWait.set(true);
			                	}
			                }
			            });
			            dialog.show();
					}
					
					@Override
					protected Void doInBackground(Void... params) {
						long startTime = System.currentTimeMillis();
						try {
							while(!cancelWait.get() && !cityChecked.get() && System.currentTimeMillis() - startTime <= TWENTY_SECONDS) {
								Thread.sleep(1000);
							}
						}
						catch(Exception ignore) {}
						return null;
					}
					
					@Override
    				protected void onPostExecute(Void result) {
						if(dialog.isShowing()) {
							Misc.doQuietly(new Runnable() {
								@Override
								public void run() {dialog.dismiss();}
							});
						}
						if(!cancelWait.get()) {
							if(StringUtils.isBlank(registerUrl)) {
								Intent intent = new Intent(LoginActivity.this, UserRegistrationActivity.class);
								startActivity(intent);
								finish();
							}
							else {
								Intent intent = new Intent(LoginActivity.this, WebViewRegistrationActivity.class);
								intent.putExtra(WebViewRegistrationActivity.PAGE_URL, registerUrl);
								startActivity(intent);
							}
						}
					}
				});
				
				return;
			case R.id.forget_pwd:
				Intent intent = new Intent(LoginActivity.this, ForgotPwdActivity.class);
                startActivity(intent);
				return;
		}
		
		
		class onPostLogin implements Runnable {
			User user;
			public Runnable setUser(User user) {this.user=user;return this;}
			public void run() {
				if(user != null && user.getId() != -1) {
                    Log.d("Login_Activity","Successful Login");
                    Log.d("Login_Activity", "Saving Login Info to Shared Preferences");

                    User.setCurrentUser(LoginActivity.this, user);
                    CrashlyticsUtils.initUserInfo(user);
                    
                    SharedPreferences loginPrefs = Preferences.getAuthPreferences(LoginActivity.this);
                    SharedPreferences.Editor loginPrefsEditor = loginPrefs.edit();
                    loginPrefsEditor.putString(User.USERNAME, user.getUsername());
                    loginPrefsEditor.putString(User.PASSWORD, user.getPassword());
                    loginPrefsEditor.putString(User.TYPE, user.getType());
                    //boolean toOnBoard =  StringUtils.equalsIgnoreCase(user.getUsername(), loginPrefs.getString(User.NEW_USER, ""));
                    boolean toOnBoard = StringUtils.isBlank(user.getDeviceId());
                    
                    toOnBoard&=!restrictedMode;
                    if (toOnBoard) loginPrefsEditor.putString(User.PLAY_SCORE_ANIMATION, user.getUsername());
                    loginPrefsEditor.remove(User.NEW_USER);
                    loginPrefsEditor.commit();
                    
                    Intent intent = new Intent(LoginActivity.this, toOnBoard? OnBoardActivity.class : LandingActivity2.class);
                    LoginActivity.this.startActivity(intent);
                    finish();
                }
                else {
                    
                    editTextPassword.setText("");
                 
                    CharSequence msg;
                    Exception exc = ehs.hasExceptions()?ehs.popException().getException():null;
                    if(exc instanceof ConnectException || exc instanceof UnknownHostException){
                        msg = getString(R.string.no_connection);
                    }else if(exc instanceof SocketTimeoutException){
                        msg = getString(R.string.connection_timeout);
                    }
                    else if(exc instanceof HttpResponseException && ((HttpResponseException)exc).getStatusCode() == 500){
                        msg = "The server encountered an unexpected condition which prevented it from fulfilling the request.";
                    }else{
                        msg = Html.fromHtml(getAccountPwdErrorMsg());
                    }
                    NotificationDialog2 notificationDialog = new NotificationDialog2(LoginActivity.this, msg);
                    notificationDialog.show();
                }
			}
		};
		
		
		
		
		if (v.getId()==R.id.fb_login_button) {
			
			final Runnable fbLogin = new Runnable() {
				public void run() {
					new LoginFBTask(LoginActivity.this) {
						@Override
						protected void onPostLogin(User user) {
							new onPostLogin().setUser(user).run();
						}
					}.execute();
				}
			};
			
			if (AccessToken.getCurrentAccessToken()==null || AccessToken.getCurrentAccessToken().isExpired()) {
				LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"));
				LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
					@Override
					public void onSuccess(LoginResult result) {fbLogin.run();}
					public void onCancel() {}
					public void onError(FacebookException error) {}
				});
			}
			else {fbLogin.run();}
			return;
		}
		
	    if(Misc.isAddGoogleAccount(this)){
            Misc.showGoogleAccountDialog(this);
        }else{
    		final String username = editTextUsername.getText().toString();
    		final String password = editTextPassword.getText().toString();
    		
    		SharedPreferences globalPrefs = Preferences.getGlobalPreferences(this);
    		String gcmRegistrationId = globalPrefs.getString(Preferences.Global.GCM_REG_ID, "");
    		
    		LoginTaskNew loginTask = new LoginTaskNew(this, username, password, gcmRegistrationId){
                @Override
                protected void onPostLogin(User user) {
                	new onPostLogin().setUser(user).run();
                }
    		};
    		
    		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password))
    		loginTask.execute();
        }
	}
	
	private static String getAccountPwdErrorMsg(){
	    return "The username or password you entered is not valid.";
	}
	
    public void afterTextChanged(Editable s) {}
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    
    private void hsowKeyboard(View view) {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, 0);
    }
    private void hideKeyboard() {
    	View view = this.getCurrentFocus();
    	if (view != null) {
    	    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    	}
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        TextView loginButton = (TextView) findViewById(R.id.login_button);
        boolean enabled = StringUtils.isNotBlank(editTextUsername.getText()) && StringUtils.isNotBlank(editTextPassword.getText());
        loginButton.setEnabled(enabled);
        loginButton.setTextColor(getResources().getColor(enabled?android.R.color.white:R.color.lighter_gray));
    }
    
    @Override
    public void onBackPressed(){
    	if (loginPager.getCurrentItem()==0) finish();
    	else {
    		hideKeyboard();
    		loginPager.setCurrentItem(0);
    	}
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Misc.initGCM(this);
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        locationService.connect();
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
	    locationService.prepareGPS();
	}
	@Override
	public void onPause() {
	    Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
	    super.onPause();
	    locationService.closeGPS();
	}
    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        locationService.disconnect();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationService.closeGPS();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        callbackManager.onActivityResult(requestCode, resultCode, intent);
        locationService.onActivityResult(requestCode, resultCode, intent);
    }
    
    

	LocationService locationService = new LocationService() {

		@Override
		public void onLocationChanged(Location location) {
			GeoPoint curLoc = DebugOptionsActivity.getCurrentLocationLatLon(LoginActivity.this);
			if(curLoc != null) {
				location.setLatitude(curLoc.getLatitude());
				location.setLongitude(curLoc.getLongitude());
			}
			
			final Location _loc = location; 
			Misc.parallelExecute(new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					try {
			        	PassiveLocationChangedReceiver.processLocation(getApplicationContext(), _loc);
			        }catch(Exception ignore){}
					return null;
				}
	       	});
			checkCity(location.getLatitude(), location.getLongitude());
			closeGPS();
		}

		@Override
		public void onResult(LocationSettingsResult locationSettingsResult) {
			final Status status = locationSettingsResult.getStatus();
	        switch (status.getStatusCode()) {
	            case LocationSettingsStatusCodes.SUCCESS:
	            	startLocationUpdates();
	                break;
	            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
	                try {
	                    status.startResolutionForResult(LoginActivity.this, REQUEST_CHECK_SETTINGS);
	                } catch (IntentSender.SendIntentException e) {}
	                break;
	            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
	                startLocationUpdates();
	                break;
	        }
		}
		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	};
}
