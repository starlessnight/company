package com.metropia.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.activities.R;

/****************************************************************************************************************
 * ************************************** Map_Mode_Activity *****************************************************
 * This Activity is used to give the user the option to switch between a regular map vie and a satellite map view.
 * This Activity uses the layout mapmodeoptions.xml in the layout folder.
 *
 *
 * @author Timothy Olivas
 ****************************************************************************************************************/
public final class MapModeActivity extends Activity implements OnClickListener {

	/****************************************************************************************************************
	 * ************************** onCreate(Bundle savedInstanceState) ***********************************************
	 * 
	 *
	 ****************************************************************************************************************/
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mapmodeoptions);
	    RadioButton rb1 = (RadioButton) findViewById(R.id.radio0);
	    rb1.setOnClickListener(this);
	    RadioButton rb2 = (RadioButton) findViewById(R.id.radio1);
	    rb2.setOnClickListener(this);
	    
	    Intent extras = getIntent();
	    int mode = extras.getIntExtra("mapmode", 0);
	    Log.d("Map_Mode_Activity","Got int from bundle " + mode);
	    if(mode == 1) {
	    	rb1.setChecked(true);
	    	rb2.setChecked(false);
	    } else if(mode == 2) {
	    	rb2.setChecked(true);
	    	rb1.setChecked(false);
	    }
	    //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
	
	/****************************************************************************************************************
	 * 
	 *
	 *
	 ****************************************************************************************************************/
	@Override
	public void onClick(View arg0) {
		RadioButton rb = (RadioButton) arg0;
		Log.d("Map_Mode_Activity","Selected View: " + rb.getText());
        int mapmode = -1;
       
        if(rb.getText().toString().equals(getString(R.string.map_view))){
        	mapmode = 1;
        } else if (rb.getText().toString().equals(getString(R.string.satellite_view))){
        	mapmode = 2;
        }
		//create a new intent...
        Intent intent = new Intent();
        Log.d("Map_Mode_Activity","Putting int into bundle " + mapmode);
        intent.putExtra("mapmode", mapmode);
        setResult(RESULT_OK,intent);
        //close this Activity...
        finish();
	}
	
	@Override
	public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED,intent);
        //close this Activity...
        finish();
	}
	
	
	
}
