package com.smartrek.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.utils.Font;

public final class MapDisplayActivity extends ActionBarActivity {
    
    /**
     * Name of the shared preference file
     */
    public static final String MAP_DISPLAY_PREFS = "map_display";
    
    public static final String TIME_DISPLAY_MODE = "TimeDisplayMode";
    
    public static final int TIME_DISPLAY_TRAVEL = 2;
    public static final int TIME_DISPLAY_ARRIVAL = 4;
    public static final int TIME_DISPLAY_DEFAULT = TIME_DISPLAY_TRAVEL;
    
    public static final String TIME_INCREMENT = "TimeIncrement";
    public static final int TIME_INCREMENT_DEFAULT = 15;

    private RadioButton displayTravel;
    private RadioButton displayArrival;
    private RadioButton timeIncrement5;
    private RadioButton timeIncrement15;
    private RadioButton timeIncrement60;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapdisplayoptions);
        
        displayTravel = (RadioButton) findViewById(R.id.mdoradio1);
        displayArrival = (RadioButton) findViewById(R.id.mdoradio2);

        timeIncrement5 = (RadioButton) findViewById(R.id.time_increment_5min);
        timeIncrement15 = (RadioButton) findViewById(R.id.time_increment_15min);
        timeIncrement60 = (RadioButton) findViewById(R.id.time_increment_60min);
        
        final SharedPreferences prefs = getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE);
        int timeDisplayMode = prefs.getInt(TIME_DISPLAY_MODE, TIME_DISPLAY_DEFAULT);
        
        displayTravel.setChecked((timeDisplayMode & TIME_DISPLAY_TRAVEL) != 0);
        displayArrival.setChecked(!displayTravel.isChecked());
        
        displayTravel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(TIME_DISPLAY_MODE, TIME_DISPLAY_TRAVEL);
                editor.commit();
            }
        });
        
        displayArrival.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(TIME_DISPLAY_MODE, TIME_DISPLAY_ARRIVAL);
                editor.commit();
            }
        });
        
        int timeIncrement = prefs.getInt(TIME_INCREMENT, TIME_INCREMENT_DEFAULT);
        switch(timeIncrement) {
        case 5:
            timeIncrement5.setChecked(true);
            break;
            
        case 60:
            timeIncrement60.setChecked(true);
            break;
            
        default:
            timeIncrement15.setChecked(true);
        }
        
        OnClickListener timeIncrementListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                int tag = Integer.valueOf((String)v.getTag());
                editor.putInt(TIME_INCREMENT, tag);
                editor.commit();
            }
        };
        timeIncrement5.setOnClickListener(timeIncrementListener);
        timeIncrement15.setOnClickListener(timeIncrementListener);
        timeIncrement60.setOnClickListener(timeIncrementListener);
        
        Font.setTypeface(boldFont, (TextView)findViewById(R.id.time_heading));
        Font.setTypeface(lightFont, displayTravel, displayArrival);
    }
    
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("display", 1);
        setResult(RESULT_OK, intent);
        
        finish();
    }
}
