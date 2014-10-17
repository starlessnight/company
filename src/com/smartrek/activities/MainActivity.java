package com.smartrek.activities;

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
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.smartrek.CrashlyticsUtils;
import com.smartrek.SkobblerUtils;
import com.smartrek.models.User;
import com.smartrek.requests.Request;
import com.smartrek.requests.ServiceDiscoveryRequest;
import com.smartrek.requests.ServiceDiscoveryRequest.Result;
import com.smartrek.requests.UserIdRequest;
import com.smartrek.tasks.LoginTask;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Misc;
import com.smartrek.utils.Preferences;

public class MainActivity extends Activity implements AnimationListener, SKPrepareMapTextureListener {
	
	public static final String LOG_TAG = "MainActivity";
	
	private ImageView logo;
	private ImageView logoMask;
	
	private boolean splashEnded;
	
	private boolean loginTaskEnded;
	
	private boolean loggedIn;
	
	private LoginTask loginTask;
	
	private ServiceDiscoveryTask sdTask;
	
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
                        finish();
                    }
                });
	        }else if(loginTask != null){
	            loginTask.execute();
	        }
	        
	        logoMask.startAnimation(slideUpAnimation);
		}
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
