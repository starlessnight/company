package com.smartrek.utils;

import com.google.android.gcm.GCMRegistrar;
import com.smartrek.activities.GCMIntentService;
import com.smartrek.activities.R;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
        v.setText(Html.fromHtml(ctx.getString(R.string.osm_contributors)));
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.startActivity(new Intent(Intent.ACTION_VIEW, 
                    Uri.parse("http://www.openstreetmap.org/")));
            }
        });
    }
    
}
