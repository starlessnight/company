package com.smartrek.activities;

import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.utils.Cache;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;

public final class DebugOptionsActivity extends Activity {
    
    /**
     * Name of the shared preference file
     */
    public static final String DEBUG_PREFS = "debug_prefs";
    
    public static final String GPS_MODE = "GPSMode";
    
    public static final int GPS_MODE_REAL = 1;
    public static final int GPS_MODE_PRERECORDED = 2;
    public static final int GPS_MODE_LONG_PRESS = 4;
    public static final int GPS_MODE_DEFAULT = GPS_MODE_REAL;
    
    private SharedPreferences prefs;
    
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
        
        radioRealGPS = (RadioButton) findViewById(R.id.radio_real_gps);
        radioPrerecordedGPS = (RadioButton) findViewById(R.id.radio_prerecorded_gps);
        radioLongPress = (RadioButton) findViewById(R.id.radio_long_press);
        
        buttonClearCache = (Button) findViewById(R.id.button_clear_cache);
        buttonCrash = (Button) findViewById(R.id.button_crash);
        
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
        
        buttonClearCache.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Cache.getInstance().clear();
                
                NotificationDialog dialog = new NotificationDialog(DebugOptionsActivity.this, "Cache has been cleared.");
                dialog.show();
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
}
