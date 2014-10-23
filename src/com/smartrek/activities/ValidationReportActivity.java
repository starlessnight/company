package com.smartrek.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.smartrek.SmarTrekApplication;
import com.smartrek.SmarTrekApplication.TrackerName;
import com.smartrek.dialogs.ShareDialog;
import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.Font;
import com.smartrek.utils.Misc;

public final class ValidationReportActivity extends ActionBarActivity {
    
	private Route route;
    
    private TextView textViewPoints;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.validation_report);
        
        Bundle extras = getIntent().getExtras();
        route = extras.getParcelable("route");
        
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
                User user = User.getCurrentUser(ValidationReportActivity.this);
                
                ShareDialog.newInstance(user.getFirstname() + " " + user.getLastname() + " is on the way",
                     "I earned " + route.getCredits() + " points for traveling at " 
                     + Reservation.formatTime(route.getDepartureTime(), true) + " to help solve traffic congestion "
                     + "using Metropia Mobile!"
                     + "\n\n" + Misc.APP_DOWNLOAD_LINK)
                    .show(getSupportFragmentManager(), null);
            }
        });
        
        Font.setTypeface(lightFont, textViewPoints);
        Font.setTypeface(boldFont, okBtn, shareBtn);
        
        AdView ad = (AdView) findViewById(R.id.adView);
        autoRefresh(ad);
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	private void autoRefresh(final AdView ad){
	    ad.postDelayed(new Runnable() {
            @Override
            public void run() {
                AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
                adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
                ad.loadAd(adRequestBuilder.build());
                autoRefresh(ad);
            }
        }, 10000);
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
