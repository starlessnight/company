package com.smartrek.activities;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import com.smartrek.utils.Cache;

public final class DebugOptionsActivity extends Activity {
    
    /**
     * Name of the shared preference file
     */
    public static final String DEBUG_PREFS = "debug_prefs";
    
    public static final String DEBUG_MODE = "DebugMode";
    public static final String GPS_MODE = "GPSMode";
    
    public static final int GPS_MODE_REAL = 1;
    public static final int GPS_MODE_PRERECORDED = 2;
    public static final int GPS_MODE_LONG_PRESS = 4;
    public static final int GPS_MODE_DEFAULT = GPS_MODE_REAL;
    
    private static final String fakeRouteIds = "fakeRouteIds";
    
    private static final int fakeRouteIdSize = 10;
    
    private SharedPreferences prefs;
    
    private CheckBox checkboxDebugMode;
    
    private RadioButton radioRealGPS;
    private RadioButton radioPrerecordedGPS;
    private RadioButton radioLongPress;
    
    private Button buttonClearCache;
    private Button buttonCrash;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_options);
        
        prefs = getSharedPreferences(DEBUG_PREFS, MODE_PRIVATE);
        
        checkboxDebugMode = (CheckBox) findViewById(R.id.checkbox_debug_mode);
        checkboxDebugMode.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(DEBUG_MODE, true);
                editor.commit();
            }
        });
        
        radioRealGPS = (RadioButton) findViewById(R.id.radio_real_gps);
        radioPrerecordedGPS = (RadioButton) findViewById(R.id.radio_prerecorded_gps);
        radioLongPress = (RadioButton) findViewById(R.id.radio_long_press);
        
        radioRealGPS.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(GPS_MODE, GPS_MODE_REAL);
                editor.commit();
            }
        });
        
        radioPrerecordedGPS.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(GPS_MODE, GPS_MODE_PRERECORDED);
                editor.commit();
            }
        });
        
        radioLongPress.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(GPS_MODE, GPS_MODE_LONG_PRESS);
                editor.commit();
            }
        });
        
        buttonClearCache = (Button) findViewById(R.id.button_clear_cache);
        buttonCrash = (Button) findViewById(R.id.button_crash);
        
        buttonClearCache.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Cache.getInstance().clear();
                
                Toast toast = Toast.makeText(
                		DebugOptionsActivity.this,
                		"Cache has been cleared.",
                		Toast.LENGTH_SHORT);
                toast.show();
            }
            
        });
        
        buttonCrash.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ((String) null).length();
            }
            
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        int gpsMode = prefs.getInt(GPS_MODE, GPS_MODE_DEFAULT);
        
        switch (gpsMode) {
        case GPS_MODE_REAL:
            radioRealGPS.setChecked(true);
            break;
            
        case GPS_MODE_PRERECORDED:
            radioPrerecordedGPS.setChecked(true);
            break;
            
        default:
            Log.e("DebugOptionsActivity", "Should not reach here.");
        }
    }
    
    private static SharedPreferences getPrefs(Context ctx){
        return ctx.getSharedPreferences(DebugOptionsActivity.DEBUG_PREFS, 
                MODE_PRIVATE);
    }
    
    private static JSONArray getFakeRouteIds(Context ctx){
        JSONArray ids = null;
        try {
            ids = new JSONArray(getPrefs(ctx).getString(fakeRouteIds, "[]"));
        }
        catch (JSONException e) {
            ids = new JSONArray();
        }
        return ids;
    }
    
    private static void saveFakeRouteIds(Context ctx, JSONArray ids){
        SharedPreferences.Editor editor = getPrefs(ctx).edit();
        editor.putString(fakeRouteIds, ids.toString());
        editor.commit();
    }
    
    public static void addFakeRouteId(Context ctx, int id){
        JSONArray newIds;
        JSONArray oldIds = getFakeRouteIds(ctx);
        if(oldIds.length() > fakeRouteIdSize - 1){
            newIds = new JSONArray();
            for(int i=oldIds.length() - fakeRouteIdSize + 1; i<oldIds.length(); i++){
                newIds.put(oldIds.optInt(i));
            }
        }else{
            newIds = oldIds;
        }
        saveFakeRouteIds(ctx, newIds.put(id));
    }
    
    public static boolean isFakeRouteId(Context ctx, int id){
        boolean fake = false;
        JSONArray ids = getFakeRouteIds(ctx);
        for(int i=0; i<ids.length(); i++){
            if(ids.optInt(i) == id){
                fake = true;
                break;
            }
        }
        return fake;
    }
    
}
