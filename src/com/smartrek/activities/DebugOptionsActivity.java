package com.smartrek.activities;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
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
    
    private static final String fakeRoutes = "fakeRouteIds";
    
    private static final int fakeRouteSize = 10;
    
    private static final String osmdroidCacheDir = "osmdroid";
    
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
                
                FileUtils.deleteQuietly(
                    new File(Environment.getExternalStorageDirectory(), osmdroidCacheDir));
                
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
    
    private static JSONArray getFakeRoutes(Context ctx){
        JSONArray ids = null;
        try {
            ids = new JSONArray(getPrefs(ctx).getString(fakeRoutes, "[]"));
        }
        catch (JSONException e) {
            ids = new JSONArray();
        }
        return ids;
    }
    
    private static void saveFakeRoutes(Context ctx, JSONArray routes){
        SharedPreferences.Editor editor = getPrefs(ctx).edit();
        editor.putString(fakeRoutes, routes.toString());
        editor.commit();
    }
    
    public static void addFakeRoute(Context ctx, FakeRoute route){
        JSONArray newRoutes;
        JSONArray oldRoutes = getFakeRoutes(ctx);
        if(oldRoutes.length() > fakeRouteSize - 1){
            newRoutes = new JSONArray();
            for(int i=oldRoutes.length() - fakeRouteSize + 1; i<oldRoutes.length(); i++){
                newRoutes.put(oldRoutes.optString(i));
            }
        }else{
            newRoutes = oldRoutes;
        }
        saveFakeRoutes(ctx, newRoutes.put(route.toString()));
    }
    
    public static FakeRoute getFakeRoute(Context ctx, int id){
        FakeRoute route = null;
        JSONArray routes = getFakeRoutes(ctx);
        for(int i=0; i<routes.length(); i++){
            FakeRoute r = FakeRoute.fromString(routes.optString(i));
            if(r.id == id){
                route = r;
                break;
            }
        }
        return route;
    }
    
    public static class FakeRoute {
        
        int id;
        
        int seq;
        
        @Override
        public String toString() {
            return id + "," + seq;
        }
        
        public static FakeRoute fromString(String val){
            String[] vals = val.split(",");
            FakeRoute r = new FakeRoute();
            r.id = Integer.parseInt(vals[0]);
            r.seq = Integer.parseInt(vals[1]);
            return r;
        }
        
    }
    
}
