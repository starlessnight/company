package com.smartrek.activities;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.models.Route;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;

public final class ValidationReportActivity extends ActionBarActivity {
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
	private Route route;
	
    private Time startTime;
    private Time endTime;
    
    private TextView textViewPoints;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.validation_report);
        
        Bundle extras = getIntent().getExtras();
        route = extras.getParcelable("route");
        startTime = new Time();
        startTime.set(extras.getLong("startTime"));
        endTime = new Time();
        endTime.set(extras.getLong("endTime"));
        
        String msg = null;
        if (extras.getBoolean("timedout")) {
            msg ="Timed out!";
        }
        else {
            msg = String.format("You just earned %d Trekpoints!", route.getCredits());
        }
        
        textViewPoints = (TextView) findViewById(R.id.textViewPoints);
        textViewPoints.setText(msg);
        
        Button okBtn = (Button) findViewById(R.id.ok_button);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        Font.setTypeface(lightFont, textViewPoints);
        Font.setTypeface(boldFont, okBtn);
        
        MediaPlayer validationMusicPlayer = new MediaPlayer();
        validationMusicPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
        try{
            validationMusicPlayer.setDataSource(this,
                Uri.parse("android.resource://" + getPackageName() + "/"+R.raw.validation_music));
            validationMusicPlayer.prepare();
        }catch (Throwable t) {
        }
        validationMusicPlayer.start();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater mi = getSupportMenuInflater();
        mi.inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        MainMenu.onMenuItemSelected(this, featureId, item);
        
        return super.onMenuItemSelected(featureId, item);
    }
	
	@Override
	public void onBackPressed() {
	    finish();
	    
		// TODO: Is this okay to do this?
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
	}
	
}
