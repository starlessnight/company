package com.smartrek.activities;

import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.dialogs.ShareDialog;
import com.smartrek.models.Route;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.Misc;

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
                MainMenu.onMenuItemSelected(ValidationReportActivity.this, 0, R.id.dashboard);
            }
        });
        
        Button shareBtn = (Button) findViewById(R.id.share_button);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareDialog.newInstance("Share Smartrek", Misc.getGooglePlayAppUrl(ValidationReportActivity.this))
                    .show(getSupportFragmentManager(), null);
            }
        });
        
        Font.setTypeface(lightFont, textViewPoints);
        Font.setTypeface(boldFont, okBtn, shareBtn);
        
        AdView ad = (AdView) findViewById(R.id.adView);
        autoRefresh(ad);
	}
	
	private void autoRefresh(final AdView ad){
	    ad.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.loadAd(new AdRequest());
                autoRefresh(ad);
            }
        }, 10000);
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
        //mi.inflate(R.menu.main, menu);
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
	    
	    MainMenu.onMenuItemSelected(this, 0, R.id.dashboard);
	}
	
}
