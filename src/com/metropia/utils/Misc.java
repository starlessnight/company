package com.metropia.utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;
import com.metropia.activities.GCMIntentService;
import com.metropia.activities.LandingActivity2;
import com.metropia.activities.LandingActivity2.ReservationListTask;
import com.metropia.activities.R;
import com.metropia.models.Reservation;
import com.metropia.ui.timelayout.ScrollableTimeLayout;
import com.metropia.ui.timelayout.TimeButton;

public class Misc {

    public static final String APP_DOWNLOAD_LINK = "https://play.google.com/store/apps/details?id=com.metropia.activities";
    
    public static final String LOG_TAG = "Misc";
    
    private static final String addGoogleAccount = "addGoogleAccount";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void disableHardwareAcceleration(View v){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void setFastScrollAlwaysVisible(ListView v){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            v.setFastScrollAlwaysVisible(true);
        }
    }
    
    public static void setAddGoogleAccount(Context ctx, boolean flag){
        Preferences.getGlobalPreferences(ctx)
            .edit()
            .putBoolean(addGoogleAccount, flag)
            .commit();
    }
    
    public static boolean isAddGoogleAccount(Context ctx){
        return Preferences.getGlobalPreferences(ctx).getBoolean(addGoogleAccount, false);
    }
    
    public static void showGoogleAccountDialog(final Context ctx){
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(ctx);
        helpBuilder.setTitle("Add Google account");
        helpBuilder.setMessage("Metropia relies on a Google account,"
            + " would you like to configure one now?");
        helpBuilder.setPositiveButton("No",
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // close the dialog, return to activity
            }
        });
        helpBuilder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
                ctx.startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT));
            }
        });
        helpBuilder.setCancelable(false);
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();
    }
    
    public static void initGCM(Context ctx){
        GCMRegistrar.checkDevice(ctx);
        GCMRegistrar.checkManifest(ctx);
        final String regId = GCMRegistrar.getRegistrationId(ctx);
        if (regId.equals("")) {
            GCMRegistrar.register(ctx, GCMIntentService.GCM_SENDER_ID);
            Log.v(LOG_TAG, "Registered to GCM.");
        }
        else {
            Log.v(LOG_TAG, "Already registered to GCM.");
        }
    }
    
    public static void initOsmCredit(TextView v){
        final Context ctx = v.getContext();
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.startActivity(new Intent(Intent.ACTION_VIEW, 
                    Uri.parse("http://www.openstreetmap.org/")));
            }
        });
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static <Params, Progress, Result> AsyncTask<Params, Progress, Result> parallelExecute(AsyncTask<Params, Progress, Result> task, 
            Params... params){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }else{
            task.execute(params);
        }
        return task;
    }
    
    public static void fadeIn(Context ctx, View v){
        Animation anim = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_in);
        v.startAnimation(anim);
    }
    
    public static String getGooglePlayAppUrl(Context ctx){
        return "https://play.google.com/store/apps/details?id=" + ctx.getApplicationContext().getPackageName();
    }
    
    public static void wakeUpScreen(Context ctx, String tag){
        PowerManager pm = (PowerManager)ctx.getSystemService(Context.POWER_SERVICE);
        if(!pm.isScreenOn()){
           WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE, 
               tag + "Lock");
           wl.acquire(10000);
           WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
               tag + "CpuLock");
           wl_cpu.acquire(10000);
        }
    }
    
    public static void playDefaultNotificationSound(Context ctx){
        playCustomSound(ctx, R.raw.notification);
    }
    
    public static void playAndroidNotificationSound(Context ctx) {
    	try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(ctx, notification);
            r.play();
        } catch (Throwable e) {}
    }
    
    public static void playUnmuteSound(Context ctx) {
    	playCustomSound(ctx, R.raw.unmute);
    }
    
    public static void playOnMyWaySound(Context ctx) {
    	playCustomSound(ctx, R.raw.omw);
    }
    
    private static void playCustomSound(Context ctx, int rawResourceId) {
    	try {
	    	Uri ding = Uri.parse("android.resource://" + ctx.getPackageName() + "/" + rawResourceId);
	    	Ringtone r = RingtoneManager.getRingtone(ctx, ding);
	    	r.play();
    	} catch (Throwable e) {}
    }
    
    private static final String INIT_TRIP_INFO_PANEL = "INIT_TRIP_INFO_PANEL";
    
    private static final String REMOVE_TRIP_INFO_PANEL = "REMOVE_TRIP_INFO_PANEL";
    
    public static void suppressTripInfoPanel(Activity activity) {
        activity.getPreferences(Context.MODE_PRIVATE).edit()
            .putBoolean(REMOVE_TRIP_INFO_PANEL, true)
            .commit();
    }
    
    public static void tripInfoPanelOnActivityStop(final Activity activity){
        final SharedPreferences pref = activity.getPreferences(Context.MODE_PRIVATE);
        pref.edit()
            .putBoolean(INIT_TRIP_INFO_PANEL, false)
            .commit();
        if(!pref.getBoolean(REMOVE_TRIP_INFO_PANEL, false)){
            ReservationListTask task = new ReservationListTask(activity, true){
                @Override
                protected void onPostExecute(List<Reservation> reservations) {
                    if (reservations != null && !reservations.isEmpty()) {
                        pref.edit()
                            .putBoolean(INIT_TRIP_INFO_PANEL, true)
                            .commit();
                        activity.sendBroadcast(new Intent(LandingActivity2.TRIP_INFO_CACHED_UPDATES));
                    } 
                }
            };
            Misc.parallelExecute(task);
        }
        pref.edit()
            .putBoolean(REMOVE_TRIP_INFO_PANEL, false)
            .commit();
    }
    
    public static void tripInfoPanelOnActivityRestart(final Activity activity){
        SharedPreferences pref = activity.getPreferences(Context.MODE_PRIVATE);
        if(pref.getBoolean(INIT_TRIP_INFO_PANEL, false)){
            pref.edit()
                .putBoolean(REMOVE_TRIP_INFO_PANEL, true)
                .commit();
            Intent landing= new Intent(activity, LandingActivity2.class);
            landing.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(landing);
            activity.finish();
        }
    }
    
    public static WebViewClient getSSLTolerentWebViewClient(){
        return new WebViewClient(){
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }  
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadUrl("file:///android_asset/error.html");
            }
        };
    }
    
    public static void initTimeButtonDimension(Activity activity) {
    	DisplayMetrics dm = activity.getResources().getDisplayMetrics();
	    Display display = activity.getWindowManager().getDefaultDisplay();
	    TimeButton.initButtonDimension(dm, display);
	    ScrollableTimeLayout.initScreenWidth(dm, display);
    }
    
    public static void doQuietly(Runnable callback){
        try{
            callback.run();
        }catch(Throwable t){}
    }
    
    public static Bitmap getBitmap(Context ctx, int resourceId, int inSampleSize) {
    	InputStream is = ctx.getResources().openRawResource(resourceId);
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeStream(is, null, options);
    }
    
    public static Map<String, String> processQueryString(String queryString) {
    	Map<String, String> paramValueMap = new HashMap<String, String>();
    	for(String paramValuePair : StringUtils.split(queryString, "&")) {
    		String[] paramValueArray = StringUtils.split(paramValuePair, "=");
    		paramValueMap.put(paramValueArray[0], paramValueArray[1]);
    	}
    	return paramValueMap;
    }
    
}
