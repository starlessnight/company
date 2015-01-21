package com.metropia.activities;

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
import com.metropia.requests.UserIdRequest;
import com.metropia.tasks.LoginTask;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Misc;
import com.metropia.utils.Preferences;
import com.skobbler.ngx.SKPrepareMapTextureListener;

public class MainActivity extends FragmentActivity implements AnimationListener, SKPrepareMapTextureListener {
	
	public static final String LOG_TAG = "MainActivity";
	
	private ImageView logo;
	private ImageView logoMask;
	
	private boolean splashEnded;
	
	private boolean loginTaskEnded;
	
	private boolean loggedIn;
	
	private LoginTask loginTask;
	
	private ServiceDiscoveryTask sdTask;
	
	private AtomicBoolean showWaitOrCancelDialog = new AtomicBoolean(true);
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
            if (!username.equals("") && !password.equals("")) {
                loginTask = newLoginTask(username, password);
            }
	        
	        if(Request.NEW_API){
	        	final NotificationDialog2 waitOrCancelDialog = new NotificationDialog2(MainActivity.this, "The network seems to be very slow.\nKeep on trying?");
	        	waitOrCancelDialog.setVerticalOrientation(false);
	        	waitOrCancelDialog.setMessageTextSize(12);
				waitOrCancelDialog.setTitle("Just a little bit longer...");
				waitOrCancelDialog.setNegativeButtonText("I'll wait");
				waitOrCancelDialog.setNegativeActionListener(new ActionListener() {
					@Override
					public void onClick() {
						waitOrCancelDialog.dismiss();
					}
				});
				waitOrCancelDialog.setPositiveButtonText("Cancel");
				waitOrCancelDialog.setPositiveActionListener(new ActionListener() {
					@Override
					public void onClick() {
						MainActivity.this.finish();
					}
				});
	        	
	            final Runnable onSuccess = new Runnable() {
                    @Override
                    public void run() {
                    	showWaitOrCancelDialog.set(false);
                    	if(waitOrCancelDialog.isShowing()) {
                    		waitOrCancelDialog.dismiss();
                    	}
                    	
                    	findViewById(R.id.progress).setVisibility(View.GONE);
                    	
                        if(loginTask != null){
                            loginTask.setDialogEnabled(splashEnded);
                            loginTask.showDialog();
                            Misc.parallelExecute(new AsyncTask<Void, Void, Integer>() {
                                @Override
                                protected Integer doInBackground(Void... params) {
                                    Integer id = null;
                                    try {
                                        UserIdRequest req = new UserIdRequest(username); 
                                        req.invalidateCache(MainActivity.this);
                                        id = req.execute(MainActivity.this);
                                    }
                                    catch(Exception e) {}
                                    return id;
                                }
                                protected void onPostExecute(Integer userId) {
                                    if(userId == null){
                                        loginTaskEnded = true;
                                        loggedIn = false;
                                        if(splashEnded){
                                            startLoginActivity();
                                        }
                                    }else{
                                        loginTask.setUserId(userId);
                                        Misc.parallelExecute(loginTask);
                                    }
                                }
                            });
                        }
                        DebugOptionsActivity.setActivityDistanceInterval(MainActivity.this, Request.getActivityDistanceInterval());
                    }
                };
                sdTask = initApiLinks(this, getEntrypoint(MainActivity.this), onSuccess, new Runnable() {
                    @Override
                    public void run() {
                    	showWaitOrCancelDialog.set(false);
                    	if(waitOrCancelDialog.isShowing()) {
                    		waitOrCancelDialog.dismiss();
                    	}
                    	findViewById(R.id.progress).setVisibility(View.GONE);
                        finish();
                    }
                });
                
                new Handler().postDelayed(new Runnable() {
        			@Override
        			public void run() {
        				if(showWaitOrCancelDialog.getAndSet(false) && !isFinishing()) {
        					waitOrCancelDialog.show();
        				}
        			}
                }, 10000);
                
	        }else if(loginTask != null){
	            loginTask.execute();
	        }
	        
	        logoMask.startAnimation(slideUpAnimation);
		}
		
		//init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
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
                ehs.registerException(e);
            }
            return rs;
        }
	    
	    @Override
        protected void onPostExecute(Result result) {
            if (ehs.hasExceptions()) {
                failed = true;
                if(onError != null){
                    ehs.reportExceptions(onError);
                }
            }
            else {
                failed = false;
                Request.setLinkUrls(result.links);
                Request.setPageUrls(result.pages);
                Request.setSettings(result.settings);
                if(onSuccess != null){
                    onSuccess.run();
                }
            }
        }
	    
	    public boolean isFailed(){
	        return failed;
	    }
	    
	}
	
	public static ServiceDiscoveryTask initApiLinks(final Context ctx, final String entrypoint, 
	        final Runnable onSuccess, final Runnable onError){
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
	
	private LoginTask newLoginTask(String username, String password){
	    final String gcmRegistrationId = Preferences.getGlobalPreferences(this)
            .getString("GCMRegistrationID", "");
	    return new LoginTask(this, username, password, gcmRegistrationId) {
            @Override
            protected void onPostLogin(final User user) {
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
        }.setDialogEnabled(false);
	}
	
	private void startLandingActivity(){
	    Intent intent = new Intent(this, LandingActivity2.class);        
	    startActivity(intent);
        finish();
	}
	
	public void onResume(){
	    super.onResume();
	    Localytics.openSession();
	    Localytics.upload();
	    
	    if (MainActivity.this instanceof FragmentActivity) {
	        Localytics.setInAppMessageDisplayActivity((FragmentActivity) MainActivity.this);
	    }

	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
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
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void onAnimationStart(Animation animation) {

	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Log.d("MainActivity", String.format("onActivityResult: %d, %d", requestCode, resultCode));
        super.onActivityResult(requestCode, resultCode, intent);
       
    	if (resultCode == IntroActivity.INTRO_FINISH) {
    	    if(requestCode == IntroActivity.INTRO_ACTIVITY){
    	    	Intent signUpIntent = new Intent(this, UserRegistrationActivity.class);
                startActivity(signUpIntent);
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
		checkLoginStatus();
	}
    
}
