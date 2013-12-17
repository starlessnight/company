package com.smartrek.utils;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;
import com.smartrek.activities.GCMIntentService;

public class Misc {
    
    public static final String LOG_TAG = "Misc";
    
    private static final String addGoogleAccount = "addGoogleAccount";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void disableHardwareAcceleration(View v){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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
        helpBuilder.setMessage("Smartrek relies on a Google account,"
            + " would you like to configure one now?");
        helpBuilder.setPositiveButton("yes",
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ctx.startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT));
            }
        });
        helpBuilder.setNegativeButton("no", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
                // close the dialog, return to activity
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
    
}
