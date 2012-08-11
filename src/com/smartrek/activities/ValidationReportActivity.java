package com.smartrek.activities;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.TextView;

import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.requests.ReservationMapper;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.ValidationParameters;

public class ValidationReportActivity extends Activity {
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
	private Route route;
	
	private int numberOfLocationChanges;
    private int numberOfInRoute;
    
    private Time startTime;
    private Time endTime;
    
    private TextView textViewScore;
    private TextView textViewDuration;
    private TextView textViewPoints;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.validation_report);
        
        Bundle extras = getIntent().getExtras();
        route = extras.getParcelable("route");
        numberOfLocationChanges = extras.getInt("numberOfLocationChanges");
        numberOfInRoute = extras.getInt("numberOfInRoute");
        startTime = new Time();
        startTime.set(extras.getLong("startTime"));
        endTime = new Time();
        endTime.set(extras.getLong("endTime"));
        
        boolean validated = false;
        if (extras.getBoolean("timedout")) {
            textViewScore = (TextView) findViewById(R.id.textViewValidationScore);
            textViewScore.setText("Timed out");
        }
        else {
            float score = numberOfInRoute / (float)numberOfLocationChanges;
            ValidationParameters params = ValidationParameters.getInstance();
            validated = score >= params.getScoreThreshold();
            
            if (validated) {
            	new ValidationReportTask().execute(User.getCurrentUser(this).getId(), route.getId());
            }
            
            textViewScore = (TextView) findViewById(R.id.textViewValidationScore);
            textViewScore.setText(String.format("%d/%d = %.01f%%", numberOfInRoute, numberOfLocationChanges, score*100));
            textViewScore.setTextColor(validated ? Color.GREEN : Color.RED);
        }
        
        long duration = (endTime.toMillis(false) - startTime.toMillis(false)) / 1000;
        textViewDuration = (TextView) findViewById(R.id.textViewTripDuration);
        textViewDuration.setText(String.format("%d sec", duration));
        
        textViewPoints = (TextView) findViewById(R.id.textViewPoints);
        textViewPoints.setText(String.format("%d", validated ? route.getCredits() : 0));
	}
	
	private class ValidationReportTask extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			int uid = (Integer) params[0];
			int rid = (Integer) params[1];
			
			ReservationMapper mapper = new ReservationMapper();
			try {
				mapper.reportValidation(uid, rid);
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
