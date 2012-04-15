package smartrek.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;

public class MapDisplayActivity extends Activity {
    
    /**
     * Name of the shared preference file
     */
    public static final String MAP_DISPLAY_PREFS = "map_display";
    
    public static final String TIME_DISPLAY_MODE = "TimeDisplayMode";
    public static final int TIME_DISPLAY_DEPARTURE = 1;
    public static final int TIME_DISPLAY_TRAVEL = 2;
    public static final int TIME_DISPLAY_ARRIVAL = 4;
    public static final int TIME_DISPLAY_DEFAULT = TIME_DISPLAY_DEPARTURE | TIME_DISPLAY_TRAVEL;
    
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
        int defaultTimeDisplayMode = TIME_DISPLAY_DEFAULT;
        int timeDisplayMode = prefs.getInt(TIME_DISPLAY_MODE, defaultTimeDisplayMode);

        //displayDeparture.setChecked((timeDisplayMode & TIME_DISPLAY_DEPARTURE) != 0);
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
    }
    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("display", getRetVal());
        setResult(RESULT_OK,intent);
        
        //close this Activity...
        finish();
    }

    private int getRetVal(){
        int retVal = 1;
        if(displayArrival.isChecked() && !displayTravel.isChecked()){
            retVal = 2;
        }
        if(displayTravel.isChecked() && !displayArrival.isChecked()){
            retVal = 3;
        }
        if(displayTravel.isChecked() && displayArrival.isChecked()){
            retVal = 4;
        }
        return retVal;
    }
    
}
