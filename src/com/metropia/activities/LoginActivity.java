package com.metropia.activities;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import android.widget.Button;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
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
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.HTTP;
import com.metropia.utils.Misc;
import com.metropia.utils.Preferences;
import com.metropia.utils.SystemService;

public final class LoginActivity extends FragmentActivity implements OnClickListener,
        TextWatcher, ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<LocationSettingsResult> {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	private CallbackManager callbackManager;
    
	private EditText editTextUsername;
	private EditText editTextPassword;
	
	private static final Integer TWENTY_SECONDS = 20 * 1000; 
	private AtomicBoolean cancelWait = new AtomicBoolean(false);
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);
        
        Localytics.integrate(this);
        
        User.logout(this);
        
        /* If it hasn't set up the login screen */
        
        TextView login = (TextView) findViewById(R.id.login_button);
        login.setOnClickListener(this);
        TextView fb_login = (TextView) findViewById(R.id.fb_login_button);
        fb_login.setOnClickListener(this);
        
        
        TextView forgetPwd = (TextView) findViewById(R.id.forget_pwd);
        forgetPwd.setText(Html.fromHtml("<u>Forgot Password?</u>"));
        forgetPwd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPwdActivity.class);
                startActivity(intent);
            }
        });
        
        TextView newUser = (TextView) findViewById(R.id.new_user);
        SpannableString createAccount = new SpannableString("Or Create Account");
        ClickableSpan create = new ClickableSpan() {
			@Override
			public void onClick(View view) {
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
								public void run() {
									dialog.dismiss();
								}
								
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
			}
        };
        createAccount.setSpan(create, 3, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        newUser.setText(createAccount);
        newUser.setMovementMethod(LinkMovementMethod.getInstance());
        newUser.setLinkTextColor(Color.BLACK);
        
        editTextUsername = (EditText) findViewById(R.id.username_box);
        editTextUsername.addTextChangedListener(this);
        editTextPassword = (EditText) findViewById(R.id.pwd_box);
        editTextPassword.addTextChangedListener(this);
        SharedPreferences loginPrefs = Preferences.getAuthPreferences(this);
        String username = loginPrefs.getString(User.USERNAME, "");
        if(StringUtils.isNotBlank(username)) {
        	editTextUsername.setText(username);
        }
        
        locationListener = new LocationListener(){
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
        };
        
        systemLocationListener = new android.location.LocationListener() {

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
			public void onStatusChanged(String provider, int status, Bundle extras) {}

			@Override
			public void onProviderEnabled(String provider) {}

			@Override
			public void onProviderDisabled(String provider) {}
        	
        };
        
        FacebookSdk.sdkInitialize(this);
        callbackManager = CallbackManager.Factory.create();
        
        AssetManager assets = getAssets();
        //Font.setTypeface(Font.getBold(assets));
        Font.setTypeface(Font.getLight(assets), editTextUsername, 
        		editTextPassword, login, newUser, 
        		(TextView) findViewById(R.id.forget_pwd));
        
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
      	
      	if(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this) == ConnectionResult.SUCCESS) {
	        requestingLocationUpdates = true;
	        createGoogleApiClient();
	        createLocationRequest();
	        buildLocationSettingsRequest();
        }
      	
    }
    
    private AtomicBoolean cityChecked = new AtomicBoolean(false);
    private AtomicBoolean serviceArea = new AtomicBoolean(false);
    private String registerUrl;
    
    
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
                if(result != null && StringUtils.isNotBlank(result.html)){
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
    protected void onStart() {
        super.onStart();
        Misc.initGCM(this);
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        if(googleApiClient != null) {
        	googleApiClient.connect();
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        if(googleApiClient != null) {
        	googleApiClient.disconnect();
        }
    }
    
    @Override
    public void onBackPressed(){
    	finish();
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
	    prepareGPS();
	}

	@Override
	public void onPause() {
	    Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
	    super.onPause();
	    closeGPS();
	}

	@Override
	public void onClick(View v) {
		
		class onPostLogin implements Runnable {
			User user;
			public Runnable setUser(User user) {this.user = user;return this;}
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
                    boolean toOnBoard =  StringUtils.equalsIgnoreCase(user.getUsername(), loginPrefs.getString(User.NEW_USER, ""));
                    if(toOnBoard) {
                    	loginPrefsEditor.remove(User.NEW_USER);
                    	loginPrefsEditor.putString(User.PLAY_SCORE_ANIMATION, user.getUsername());
                    }
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
    		
    		final LoginTask loginTask = new LoginTask(this, username, password, gcmRegistrationId){
                @Override
                protected void onPostLogin(User user) {
                	new onPostLogin().setUser(user).run();
                }
    		};
    		
    		if(Request.NEW_API){
    		    new AsyncTask<Void, Void, Integer>() {
    		        protected void onPreExecute() {
    		            loginTask.showDialog();
    		        }
                    @Override
                    protected Integer doInBackground(Void... params) {
                        Integer id = null;
                        try {
                            UserIdRequest req = new UserIdRequest(username); 
                            req.invalidateCache(LoginActivity.this);
                            id = req.execute(LoginActivity.this);
                        }
                        catch(Exception e) {
                            ehs.registerException(e);
                        }
                        return id;
                    }
                    protected void onPostExecute(Integer userId) {
                        if(userId == null){
                            loginTask.hideDialog();
                            NotificationDialog2 notificationDialog = new NotificationDialog2(LoginActivity.this, 
                                Html.fromHtml(getAccountPwdErrorMsg()));
                            notificationDialog.show();
                        }else{
                            loginTask.setUserId(userId).execute();
                        }
                    }
                }.execute();
    		}else{
    		    loginTask.execute();
    		}
        }
	}
	
	private static String getAccountPwdErrorMsg(){
	    return "The username or password you entered is not valid.";
	}
	
	Button.OnClickListener registerButtonClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.d("Login_Activity","Register New user clicked");
			Log.d("Login_Activity","Starting Register Activity");
			Intent intent = new Intent(LoginActivity.this, UserRegistrationActivity.class);
			startActivity(intent);
		}
	};

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        TextView login = (TextView) findViewById(R.id.login_button);
        boolean enabled = StringUtils.isNotBlank(editTextUsername.getText()) && StringUtils.isNotBlank(editTextPassword.getText());
        login.setEnabled(enabled);
        login.setTextColor(getResources().getColor(enabled?android.R.color.white:R.color.lighter_gray));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeGPS();
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        callbackManager.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == REQUEST_CHECK_SETTINGS) {
        	if(resultCode == Activity.RESULT_OK) {
        		startLocationUpdates();
        	}
        	else {
        		requestingLocationUpdates = false;
        		startLocationUpdates();
        	}
        }
    }
    
    private GoogleApiClient googleApiClient;
    private LocationRequest highAccuracyLocationRequest;
    private boolean requestingLocationUpdates = false;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private android.location.LocationListener systemLocationListener;
    private Integer REQUEST_CHECK_SETTINGS = Integer.valueOf(1111);
    
    private void createGoogleApiClient() {
    	googleApiClient = new GoogleApiClient.Builder(LoginActivity.this).addApi(LocationServices.API)
    			.addConnectionCallbacks(LoginActivity.this).addOnConnectionFailedListener(LoginActivity.this).build();
    }
    
    private void createLocationRequest() {
    	highAccuracyLocationRequest = new LocationRequest();
    	highAccuracyLocationRequest.setInterval(5000);
    	highAccuracyLocationRequest.setFastestInterval(2500);
    	highAccuracyLocationRequest.setSmallestDisplacement(5);
    	highAccuracyLocationRequest.setNumUpdates(1);
    	highAccuracyLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(highAccuracyLocationRequest).setAlwaysShow(true);
        locationSettingsRequest = builder.build();
    }
    
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);
        result.setResultCallback(LoginActivity.this);
    }
    
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, highAccuracyLocationRequest, locationListener);
    }
    
    private void prepareGPS(){
    	if(googleApiClient != null && requestingLocationUpdates) {
    		checkLocationSettings();
    	}
    	else if(googleApiClient == null){
    		closeGPS();
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && 
            		locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, 2, systemLocationListener);
            }else{
                SystemService.alertNoGPS(this, true);
            }
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, systemLocationListener);
    	}
    }
    
    private void closeGPS(){
    	if(googleApiClient != null && googleApiClient.isConnected()) {
	    	LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener).setResultCallback(new ResultCallback<Status>() {
	            @Override
	            public void onResult(Status status) {
	                requestingLocationUpdates = true;
	            }
	        });
    	}
    	else if(locationManager != null){
    		locationManager.removeUpdates(systemLocationListener);
    	}
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
                } catch (IntentSender.SendIntentException e) {
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                startLocationUpdates();
                break;
        }
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {}

	@Override
	public void onConnected(Bundle arg0) {}

	@Override
	public void onConnectionSuspended(int arg0) {}

}
