package com.smartrek.activities;

import java.io.IOException;

import android.content.Intent;
import android.os.AsyncTask;
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
import com.smartrek.models.User;
import com.smartrek.requests.RouteValidationRequest;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.ValidationParameters;

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
        
        boolean validated = false;
        final int uid = User.getCurrentUser(this).getId();
        
        String msg;
        if (extras.getBoolean("timedout")) {
            msg ="Timed out!";
        }
        else {
        	double score = route.getValidatedDistance() / route.getLength();
        	
            //float score = numberOfInRoute / (float)numberOfLocationChanges;
            ValidationParameters params = ValidationParameters.getInstance();
            validated = score >= params.getScoreThreshold();
            
            if (validated) {
                new ValidationReportTask().execute(uid, route.getId());
            }
            msg = String.format("You just earned %d Trekpoints!", validated ? route.getCredits() : 0);
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
	
	private class ValidationReportTask extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			int uid = (Integer) params[0];
			int rid = (Integer) params[1];
			
			RouteValidationRequest request = new RouteValidationRequest(uid, rid);
			try {
				request.execute();
			}
			catch (IOException e) {
				ehs.registerException(e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
		    if (ehs.hasExceptions()) {
		        ehs.reportExceptions();
		    }
		}
		
	}
}
