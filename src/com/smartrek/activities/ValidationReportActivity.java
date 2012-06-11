package com.smartrek.activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.TextView;

import com.smartrek.models.Route;
import com.smartrek.utils.ValidationParameters;

public class ValidationReportActivity extends Activity {
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

        float score = numberOfInRoute / (float)numberOfLocationChanges;
        ValidationParameters params = ValidationParameters.getInstance();
        boolean validated = score >= params.getScoreThreshold();
        
        textViewScore = (TextView) findViewById(R.id.textViewValidationScore);
        textViewScore.setText(String.format("%d/%d = %.01f%%", numberOfInRoute, numberOfLocationChanges, score*100));
        textViewScore.setTextColor(validated ? Color.GREEN : Color.RED);
        
        long duration = (endTime.toMillis(false) - startTime.toMillis(false)) / 1000;
        textViewDuration = (TextView) findViewById(R.id.textViewTripDuration);
        textViewDuration.setText(String.format("%d sec", duration));
        
        textViewPoints = (TextView) findViewById(R.id.textViewPoints);
        textViewPoints.setText(String.format("%d", validated ? route.getCredits() : 0));
	}
}
