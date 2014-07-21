package com.smartrek.activities;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.analytics.tracking.android.EasyTracker;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.smartrek.UserLocationService;
import com.smartrek.models.User;
import com.smartrek.requests.Request;
import com.smartrek.requests.ServiceDiscoveryRequest;
import com.smartrek.requests.ServiceDiscoveryRequest.Result;
import com.smartrek.requests.UserIdRequest;
import com.smartrek.tasks.LoginTask;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Misc;
import com.smartrek.utils.Preferences;

public class MainActivity extends Activity implements AnimationListener {
	
	public static final String LOG_TAG = "MainActivity";
	
	private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private ImageView logo;
	private ImageView logoMask;
	
	private boolean splashEnded;
	
	private boolean loginTaskEnded;
	
	private boolean loggedIn;
	
	private LoginTask loginTask;
	
	private static AtomicBoolean initApiLinksFailed = new AtomicBoolean();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
	        logoMask.startAnimation(slideUpAnimation);
	        
	        /* Check Shared memory to see if login info has already been entered on this phone */
            SharedPreferences loginPrefs = Preferences.getAuthPreferences(this);
            final String username = loginPrefs.getString(User.USERNAME, "");
            final String password = loginPrefs.getString(User.PASSWORD, "");
            if (!username.equals("") && !password.equals("")) {
                loginTask = newLoginTask(username, password);
            }
	        
	        if(Request.NEW_API){
	            final Runnable onSuccess = new Runnable() {
                    @Override
                    public void run() {
                        Long interval = UserLocationService.getInterval();
                        LocationLibrary.initialiseLibrary(getBaseContext(), interval, 
                            interval.intValue(), true, "com.smartrek.activities");
                        UserLocationService.schedule(getBaseContext());
                        if(loginTask != null){
                            loginTask.setDialogEnabled(splashEnded);
                            loginTask.showDialog();
                            new AsyncTask<Void, Void, Integer>() {
                                @Override
                                protected Integer doInBackground(Void... params) {
                                    Integer id = null;
                                    try {
                                        UserIdRequest req = new UserIdRequest(username); 
                                        req.invalidateCache(MainActivity.this);
                                        id = req.execute(MainActivity.this);
                                    }
                                    catch(Exception e) {
                                        ehs.registerException(e);
                                    }
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
                                        loginTask.setUserId(userId)
                                            .execute();
                                    }
                                }
                            }.execute();
                        }
                    }
                };
	            initApiLinks(this, getEntrypoint(MainActivity.this), onSuccess, new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
	        }else if(loginTask != null){
	            loginTask.execute();
	        }
		}
	}
	
	public static String getEntrypoint(Context ctx){
	    String url = DebugOptionsActivity.getDebugEntrypoint(ctx);
        if(StringUtils.isBlank(url)){
            url = Request.ENTRYPOINT_URL;
        }
        return url;
	}
	
	public static AsyncTask<Void, Void, Result> initApiLinks(final Context ctx, final String entrypoint, 
	        final Runnable onSuccess, final Runnable onError){
	    final ExceptionHandlingService eh = new ExceptionHandlingService(ctx);
	    AsyncTask<Void, Void, Result> task = new AsyncTask<Void, Void, Result>() {
            @Override
            protected Result doInBackground(Void... params) {
                Result rs = null;
                try {
                    ServiceDiscoveryRequest req = new ServiceDiscoveryRequest(entrypoint);  
                    req.invalidateCache(ctx);
                    rs = req.execute(ctx);
                }
                catch(Exception e) {
                    eh.registerException(e);
                }
                return rs;
            }
            @Override
            protected void onPostExecute(Result result) {
                if (eh.hasExceptions()) {
                    initApiLinksFailed.set(true);
                    if(onError != null){
                        eh.reportExceptions(onError);
                    }
                }
                else {
                    initApiLinksFailed.set(false);
                    Request.setLinkUrls(result.links);
                    Request.setPageUrls(result.pages);
                    Request.setSettings(result.settings);
                    if(onSuccess != null){
                        onSuccess.run();
                    }
                }
            }
        }.execute();
        return task;
	}
	
	public static void initApiLinksIfNecessary(Context ctx, Runnable onSuccess){
	    if(Request.hasLinkUrls()){
	        onSuccess.run();
        }else{
            MainActivity.initApiLinks(ctx, MainActivity.getEntrypoint(ctx), onSuccess, null);
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
	    Intent intent = new Intent(this, LandingActivity2.ENABLED?LandingActivity2.class:LandingActivity.class);        
	    startActivity(intent);
        finish();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		EasyTracker.getInstance().activityStart(this);
		
		Misc.initGCM(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
	    splashEnded = true;
		logoMask.setVisibility(View.GONE);
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
		    }else if(!initApiLinksFailed.get()){
		        loginTask.showDialog();
	            loginTask.setDialogEnabled(true);
		    }
        }
	}
	
	/*
	private void checkEulaAndProceedToNextScreen(){
	    new AsyncTask<Void, Void, Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                boolean updated = false;
                try{
                    HTTP http = new HTTP(Request.getPageUrl(Page.eula))
                        .setIfNoneMatch(DebugOptionsActivity.getEulaEtag(MainActivity.this));
                    http.connect();
                    updated = http.getResponseCode() == 200;
                }catch(Throwable t){}
                return updated;
            }
            protected void onPostExecute(Boolean updated) {
                if(updated){
                    Intent intent = new Intent(MainActivity.this, LicenseAgreementActivity.class);
                    startActivityForResult(intent, LicenseAgreementActivity.LICENSE_AGREEMENT_UPDATED);
                }else{
                    proceedToNextScreen();
                }
            }
        }.execute();
	}
	*/
	
	private void proceedToNextScreen(){
	    if(loggedIn){
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
    	        proceedToNextScreen();
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
    
}
