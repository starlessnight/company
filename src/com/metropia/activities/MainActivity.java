package com.metropia.activities;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.CrashlyticsUtils;
import com.metropia.SkobblerUtils;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.dialogs.NotificationDialog2.ActionListener;
import com.metropia.models.User;
import com.metropia.requests.Request;
import com.metropia.requests.ServiceDiscoveryRequest;
import com.metropia.requests.ServiceDiscoveryRequest.Result;
import com.metropia.tasks.LoginFBTask;
import com.metropia.tasks.LoginTaskNew;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Misc;
import com.metropia.utils.Preferences;
import com.skobbler.ngx.SKPrepareMapTextureListener;

public class MainActivity extends FragmentActivity implements AnimationListener, SKPrepareMapTextureListener {
	
	public static final String LOG_TAG = "MainActivity";

	private static Boolean setting_request=false;
	
	
	private ImageView logo;
	private ImageView logoMask;
	
	private boolean splashEnded;
	
	private boolean loginTaskEnded;
	
	private boolean loggedIn;
	
	private LoginTaskNew loginTask;
	
	private ServiceDiscoveryTask sdTask;
	
	private   NotificationDialog2 waitOrCancelDialog;
	
	private AtomicBoolean showWaitOrCancelDialog = new AtomicBoolean(true);
	
	private static MainActivity M_this;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());
		
		M_this=MainActivity.this;
		// Integrate Localytics
		Localytics.integrate(this);
		
		//init Time button dimension
		Misc.initTimeButtonDimension(MainActivity.this);
		
		// Possible work around for market launches. See http://code.google.com/p/android/issues/detail?id=2373
		// for more details. Essentially, the market launches the main activity on top of other activities.
		// we never want this to happen. Instead, we check if we are the root and if not, we finish.
		if (!isTaskRoot()) {
		    final Intent intent = getIntent();
		    final String intentAction = intent.getAction(); 
		    if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
		        Log.w(LOG_TAG, "Main Activity is not the root.  Finishing Main Activity instead of launching.");
		        finish();
		    }
		}else{
		    setContentView(R.layout.main);
		    
	        logo = (ImageView) findViewById(R.id.imageViewLogo);
	        logoMask = (ImageView) findViewById(R.id.logoMask);
	        ViewTreeObserver vto = logo.getViewTreeObserver();
	        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					int logoHeight = Double.valueOf(logo.getMeasuredHeight() * 0.55).intValue();
					int logoWidth = logo.getMeasuredWidth();
					logoMask.setMaxHeight(logoHeight);
					logoMask.setMinimumHeight(logoHeight);
					logoMask.setMaxWidth(logoWidth);
					logoMask.setMinimumWidth(logoWidth);
					return true;
				}
			});
	        Animation slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slideup);
	        slideUpAnimation.setAnimationListener(this);
	        
	        /* Check Shared memory to see if login info has already been entered on this phone */
            SharedPreferences loginPrefs = Preferences.getAuthPreferences(this);
            final String username = loginPrefs.getString(User.USERNAME, "");
            final String password = loginPrefs.getString(User.PASSWORD, "");
            final String type = loginPrefs.getString(User.TYPE, "");
            if (type.equals(User.FACEBOOK) && !username.equals("") && !password.equals("")) {
            	loginTask = newLoginFBTask(username, password);
            }
            else if (!username.equals("") && !password.equals("") && DebugOptionsActivity.isSkobblerPatched(MainActivity.this)) {
                loginTask = newLoginTask(username, password);
            }
            
            waitOrCancelDialog = new NotificationDialog2(MainActivity.this, "Your connection to our server is very slow. Would you like to keep trying to log on?");
        	waitOrCancelDialog.setVerticalOrientation(false);
        	waitOrCancelDialog.setMessageTextSize(12);
			waitOrCancelDialog.setTitle("Sorry for the delay");
			waitOrCancelDialog.setNegativeButtonText("No");
			waitOrCancelDialog.setPositiveButtonText("Yes");
			waitOrCancelDialog.setCanceledOnTouchOutside(false);
			
			waitOrCancelDialog.setNegativeActionListener(new ActionListener() {
				@Override
				public void onClick() {
					MainActivity.this.finish();
				}
			});
			waitOrCancelDialog.setPositiveActionListener(new ActionListener() {
				@Override
				public void onClick() {
					sdTask.cancel(true);
					sdTask = initApiLinks(MainActivity.this, getEntrypoint(MainActivity.this), onSuccess, onError);
					delay();
				}
			});
	       	
            sdTask = initApiLinks(this, getEntrypoint(MainActivity.this), onSuccess, onError);
            delay();
	        
	        logoMask.startAnimation(slideUpAnimation);
		}
		
		//init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	final Runnable onSuccess = new Runnable() {
        @Override
        public void run() {
        	showWaitOrCancelDialog.set(false);
        	if(waitOrCancelDialog.isShowing()) {
        		Misc.doQuietly(new Runnable() {
					@Override
					public void run() {
						waitOrCancelDialog.dismiss();
					}
        		});
        	}
        	
        	findViewById(R.id.progress).setVisibility(View.GONE);
        	
        	if(loginTask != null){
                loginTask.setDialogEnabled(splashEnded);
                loginTask.showDialog();
                loginTask.execute();
            }
            DebugOptionsActivity.setActivityDistanceInterval(MainActivity.this, Request.getActivityDistanceInterval());
        }
    };
    
    final Runnable onError = new Runnable() {
        @Override
        public void run() {
        	showWaitOrCancelDialog.set(false);
        }
    };
    
	public void delay(){
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(showWaitOrCancelDialog.get() && !isFinishing()) {
					Misc.doQuietly(new Runnable() {
						@Override
						public void run() {
							waitOrCancelDialog.show();
						}
					});
				}
			}
        }, 10000);
		
	}
	
	
	public static String getEntrypoint(Context ctx){
	    String url = DebugOptionsActivity.getDebugEntrypoint(ctx);
        if(StringUtils.isBlank(url)){
            url = Request.ENTRYPOINT_URL;
        }
        return url;
	}
	
	public static class ServiceDiscoveryTask extends AsyncTask<Void, Void, Result> {
	    
	    private boolean failed;
	    
	    private Context ctx;
	    
	    private ExceptionHandlingService ehs;
	    
	    private String entrypoint;
	    
	    private Runnable onSuccess;
	    
	    private Runnable onError;
	    
	    public ServiceDiscoveryTask(Context ctx, String entrypoint, Runnable onSuccess, 
	            Runnable onError){
	        this.ctx = ctx;
	        ehs = new ExceptionHandlingService(ctx);
	        this.entrypoint = entrypoint;
	        this.onSuccess = onSuccess;
	        this.onError = onError;
	    }
	    
	    @Override
        protected Result doInBackground(Void... params) {
            Result rs = null;
            try {
                ServiceDiscoveryRequest req = new ServiceDiscoveryRequest(entrypoint);  
                req.invalidateCache(ctx);
                rs = req.execute(ctx);
            }
            catch(Exception e) {
            	if(StringUtils.equals(entrypoint, Request.ENTRYPOINT_URL)) {
            		Log.d("MainActivity", "Use Secondary EntryPoint Url");
            		try {
	            		ServiceDiscoveryRequest req = new ServiceDiscoveryRequest(Request.SECONDARY_ENTRYPOINT_URL);  
	                    req.invalidateCache(ctx);
	                    rs = req.execute(ctx);
            		}
            		catch(Exception secEx) {
            			ehs.registerException(secEx);
            		}
            	}
            	else {
            		ehs.registerException(e);
            	}
            }
            return rs;
        }
	    
	    @Override
        protected void onPostExecute(Result result) {
	    	if (isCancelled()) return;
            if (ehs.hasExceptions()) {
	            failed = true;
	            if(onError != null){
	                //ehs.reportExceptions(onError);
	            	final NotificationDialog2 noconnection = new NotificationDialog2(M_this, "Please check your network settings and try again!");
					noconnection.setVerticalOrientation(false);
					noconnection.setMessageTextSize(12);
					noconnection.setTitle("Signal is too weak!");
					noconnection.setNegativeButtonText("Settings");
					noconnection.setPositiveButtonText("Exit");
					noconnection.setNegativeActionListener(new ActionListener() {
						@Override
						public void onClick() {
							Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
							M_this.startActivity(intent);
							setting_request=true;
						}
					});
					noconnection.setPositiveActionListener(new ActionListener(){

						@Override
						public void onClick() {
							M_this.finish();
						}
					});
					noconnection.show();
					noconnection.dialog.setCanceledOnTouchOutside(false);
	            }
	            if (onError!=null) onError.run();
            }
            else {
                failed = false;
                Request.setLinkUrls(result.links);
                Request.setPageUrls(result.pages);
                Request.setSettings(result.settings);
                if(onSuccess != null) onSuccess.run();
            }
        }
	    
	    public boolean isFailed(){
	        return failed;
	    }
	    
	}
	
    public static ServiceDiscoveryTask initApiLinks(final Context ctx, final String entrypoint, final Runnable onSuccess, final Runnable onError) {
	    ServiceDiscoveryTask task = new ServiceDiscoveryTask(ctx, entrypoint, onSuccess, onError);
	    Misc.parallelExecute(task);
        return task;
	}
	
	public static void initApiLinksIfNecessary(Context ctx, Runnable onSuccess){
	    if(Request.hasLinkUrls()){
	        onSuccess.run();
        }else{
            MainActivity.initApiLinks(ctx, MainActivity.getEntrypoint(ctx), onSuccess, null);
        }
	}
	
	public static void initSettingsIfNecessary(Context ctx, Runnable onSuccess){
	    if(Request.hasSettings()){
	        onSuccess.run();
        }else{
            MainActivity.initApiLinks(ctx, MainActivity.getEntrypoint(ctx), onSuccess, new Runnable() {
				@Override
				public void run() {
					// report execption and do nothing
				}
			});
        }
	}
	
	private LoginTaskNew newLoginTask(String username, String password){
	    final String gcmRegistrationId = Preferences.getGlobalPreferences(this).getString(Preferences.Global.GCM_REG_ID, "");
	    return new LoginTaskNew(this, username, password, gcmRegistrationId) {
            @Override
            protected void onPostLogin(final User user) {
                loginTaskEnded = true;
                loggedIn = user != null && user.getId() != -1;
                if(loggedIn){
                    User.setCurrentUser(MainActivity.this, user);
                    Log.d(LOG_TAG,"Successful Login");
                    Log.d(LOG_TAG, "Saving Login Info to Shared Preferences");
                }
                if(splashEnded){
                    if (loggedIn) proceedToNextScreen();
                    else startLoginActivity();
                }
           }
        }.setDialogEnabled(false);
	}
	private LoginTaskNew newLoginFBTask(String username, String password) {
		
		return new LoginFBTask(this, username, password) {
			@Override
			protected void onPostLogin(User user) {
				loggedIn = user != null && user.getId() != -1;
                if(loggedIn){
                    User.setCurrentUser(MainActivity.this, user);
                    Log.d(LOG_TAG,"Successful Login");
                    Log.d(LOG_TAG, "Saving Login Info to Shared Preferences");
                }
                loginTaskEnded = true;
                if(splashEnded){
                    proceedToNextScreen();
                }
			}
		};
	}
	
	private void startLandingActivity(){
	    Intent intent = new Intent(this, LandingActivity2.class);        
	    startActivity(intent);
        finish();
	}
	
	public void onResume(){
	    super.onResume();
	    Localytics.openSession();
	    Localytics.tagScreen(this.getClass().getSimpleName());
	    Localytics.upload();
	    
	    if (MainActivity.this instanceof FragmentActivity) {
	        Localytics.setInAppMessageDisplayActivity((FragmentActivity) MainActivity.this);
	    }

	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
	    if(setting_request){
	    	sdTask = initApiLinks(this, getEntrypoint(MainActivity.this), onSuccess, onError);
	        delay();
	        setting_request=false;
	    }
	    
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
		Misc.initGCM(this);
	}
	
	@Override
	public void onPause() {
	    if (MainActivity.this instanceof FragmentActivity) {
	        Localytics.dismissCurrentInAppMessage();
	        Localytics.clearInAppMessageDisplayActivity();
	    }
	    Localytics.closeSession();
	    Localytics.upload();
	    super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	   super.onNewIntent(intent);
	   setIntent(intent);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
	    splashEnded = true;
		logoMask.setVisibility(View.GONE);
		findViewById(R.id.progress).setVisibility(View.VISIBLE);
		SkobblerUtils.initSkobbler(MainActivity.this, MainActivity.this, new Runnable() {
			@Override
			public void run() {
				SkobblerUtils.initializeLibrary(MainActivity.this);
				checkLoginStatus();
			}
		});
	}
	
	private void proceedToNextScreen(){
	    if(loggedIn){
	    	User user = User.getCurrentUser(MainActivity.this);
	    	if(user != null) {
		    	CrashlyticsUtils.initUserInfo(user);
	    	}
            startLandingActivity();
        }else{
            startLoginActivity();
        }
	}

	@Override
	public void onAnimationRepeat(Animation animation) {}

	@Override
	public void onAnimationStart(Animation animation) {}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Log.d("MainActivity", String.format("onActivityResult: %d, %d", requestCode, resultCode));
        super.onActivityResult(requestCode, resultCode, intent);
       
    	if (resultCode == IntroActivity.INTRO_FINISH) {
    	    if(requestCode == IntroActivity.INTRO_ACTIVITY){
    	    	Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
    	    }
    	}
    	else {
    		finish();
    	}
    }
    
    private void startLoginActivity() {
    	Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
    }
    
    private void checkLoginStatus() {
    	if(loginTask == null){
		    SharedPreferences prefs = Preferences.getGlobalPreferences(this);
            int introFinish = prefs.getInt(Preferences.Global.INTRO_FINISH, 0);
            
            if (introFinish == IntroActivity.INTRO_FINISH) {
                proceedToNextScreen();
            }
            else {
                Intent intent = new Intent(this, IntroActivity.class);
                startActivityForResult(intent, IntroActivity.INTRO_ACTIVITY);
            }
		}else{   
		    if(loginTaskEnded){
		        proceedToNextScreen();
		    }else if(!sdTask.isFailed()){
		        loginTask.showDialog();
	            loginTask.setDialogEnabled(true);
		    }
        }
    }
    
	@Override
	public void onMapTexturesPrepared(boolean success) {
		DebugOptionsActivity.setSkobblerPatched(MainActivity.this, success);
		SkobblerUtils.initializeLibrary(MainActivity.this);
		checkLoginStatus();
	}
    
}
