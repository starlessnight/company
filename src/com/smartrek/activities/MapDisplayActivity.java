package com.smartrek.activities;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.dialogs.FeedbackDialog;
import com.smartrek.dialogs.FloatingMenuDialog;
import com.smartrek.dialogs.ProfileSelectionDialog;
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
    
    private static final String CALENDAR_INTEGRATION = "CALENDAR_INTEGRATION";
    
    private static final String NAVIGATION_TTS = "NAVIGATION_TTS";
    
    private static final String LOCATION_BASED_SERVICE = "LOCATION_BASED_SERVICE";
    
    private static final String VALIDATED_TRIPS_COUNT = "VALIDATED_TRIPS_COUNT";
    
    private static final String HOME_ADDRESS = "HOME_ADDRESS";
    
    private static final String WORK_ADDRESS = "WORK_ADDRESS";
    
    private static final String PROFILE_SELECTION = "PROFILE_SELECTION";

    private RadioButton displayTravel;
    private RadioButton displayArrival;
    private RadioButton timeIncrement5;
    private RadioButton timeIncrement15;
    private RadioButton timeIncrement60;
    
    private CheckBox calendarIntegration;
    
    private CheckBox navigationTts;
    
    private CheckBox lbs;
    
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
        
        boolean calIntEnabled = isCalendarIntegrationEnabled(this);
        calendarIntegration = (CheckBox) findViewById(R.id.calendar_integration);
        calendarIntegration.setChecked(calIntEnabled);
        calendarIntegration.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit()
                    .putBoolean(CALENDAR_INTEGRATION, isChecked)
                    .commit();
            }
        });
        
        boolean navTtsEnabled = isNavigationTtsEnabled(this);
        navigationTts = (CheckBox) findViewById(R.id.navigation_tts);
        navigationTts.setChecked(navTtsEnabled);
        navigationTts.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit()
                    .putBoolean(NAVIGATION_TTS, isChecked)
                    .commit();
            }
        });
        
        boolean lbsEnabled = isLocBasedServiceEnabled(this);
        lbs = (CheckBox) findViewById(R.id.lbs);
        lbs.setChecked(lbsEnabled);
        lbs.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit()
                    .putBoolean(LOCATION_BASED_SERVICE, isChecked)
                    .commit();
            }
        });
        
        findViewById(R.id.floating_menu_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingMenuDialog dialog = new FloatingMenuDialog(MapDisplayActivity.this);
                dialog.show();
            }
        });
        
        TextView feedback = (TextView) findViewById(R.id.feedback);
        feedback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedbackDialog d = new FeedbackDialog(MapDisplayActivity.this);
                d.show();
            }
        });
        
        Font.setTypeface(boldFont, (TextView)findViewById(R.id.time_heading),
            (TextView)findViewById(R.id.calendar_integration_heading),
            (TextView)findViewById(R.id.navigation_tts_heading),
            (TextView)findViewById(R.id.lbs_heading),
            (TextView)findViewById(R.id.distribution_heading),
            feedback);
        Font.setTypeface(lightFont, displayTravel, displayArrival,
            (TextView)findViewById(R.id.calendar_integration_text),
            (TextView)findViewById(R.id.navigation_tts_text),
            (TextView)findViewById(R.id.lbs_text),
            (TextView)findViewById(R.id.distribution_date));
    }
    
    public static boolean isCalendarIntegrationEnabled(Context ctx){
        return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
            .getBoolean(CALENDAR_INTEGRATION, true);
    }
    
    public static boolean isNavigationTtsEnabled(Context ctx){
        return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
            .getBoolean(NAVIGATION_TTS, true);
    }
    
    public static boolean isLocBasedServiceEnabled(Context ctx){
        return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
            .getBoolean(LOCATION_BASED_SERVICE, true);
    }
    
    public static int getValidatedTripsCount(Context ctx){
        return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
            .getInt(VALIDATED_TRIPS_COUNT, 0);
    }
    
    public static void setValidatedTripsCount(Context ctx, int count){
        ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
            .edit()
            .putInt(VALIDATED_TRIPS_COUNT, count)
            .commit();
    }
    
    public static String getHomeAddress(Context ctx){
        return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
            .getString(HOME_ADDRESS, "");
    }
    
    public static void setHomeAddress(Context ctx, String address){
        ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
            .edit()
            .putString(HOME_ADDRESS, address)
            .commit();
    }
    
    public static String getWorkAddress(Context ctx){
        return ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
            .getString(WORK_ADDRESS, "");
    }
    
    public static void setWorkAddress(Context ctx, String address){
        ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
            .edit()
            .putString(WORK_ADDRESS, address)
            .commit();
    }
    
    public static ProfileSelectionDialog.Type getProfileSelection(Context ctx){
        ProfileSelectionDialog.Type type = null;
        String typeStr  = ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
            .getString(PROFILE_SELECTION, "");
        if(StringUtils.isNotBlank(typeStr)){
            try{
                type = ProfileSelectionDialog.Type.valueOf(typeStr);
            }catch(Throwable t){}
        }
        return type;
    }
    
    public static void setProfileSelection(Context ctx, ProfileSelectionDialog.Type type){
        ctx.getSharedPreferences(MAP_DISPLAY_PREFS, MODE_PRIVATE)
            .edit()
            .putString(PROFILE_SELECTION, type==null?null:type.name())
            .commit();
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
