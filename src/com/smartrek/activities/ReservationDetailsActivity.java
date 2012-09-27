package com.smartrek.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.utils.ExceptionHandlingService;

/**
 * Shows detailed information about a reservation.
 *
 */
public final class ReservationDetailsActivity extends Activity {
	private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private TextView textViewName;
	private TextView textViewOrigin;
	private TextView textViewDestination;
	private TextView textViewDepartureTime;
	private TextView textViewArrivalTime;
	private TextView textViewCredits;
	private Button buttonStartTrip;
	
	private Route route;
	private Reservation reservation;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reservation_details);
        
        Bundle extras = getIntent().getExtras();
        route = extras.getParcelable("route");
        reservation = extras.getParcelable("reservation");
        
        if (route == null) {
        	ehs.reportException("Could not load route (e73d67fc)");
     	
        }
        else {
            long arrivalTime = route.getArrivalTime();
            if (reservation != null) {
                arrivalTime = reservation.getArrivalTime();
            }
            
            setTitle(String.format("Reservation #%d", route.getId()));
            
	        textViewOrigin = (TextView) findViewById(R.id.textViewOrigin);
	        textViewOrigin.setText(route.getOrigin());
	        
	        textViewDestination = (TextView) findViewById(R.id.textViewDestination);
	        textViewDestination.setText(route.getDestination());
	        
	        textViewDepartureTime = (TextView) findViewById(R.id.textViewDepartureTime);
	        // FIXME: Date/time format i18n
	        Time dt = new Time();
	        dt.set(route.getDepartureTime());
	        textViewDepartureTime.setText(dt.format("%b %d, %G %l:%M%p"));
	        
	        textViewArrivalTime = (TextView) findViewById(R.id.textViewArrivalTime);
	        // FIXME: Date/time format i18n
	        Time at = new Time();
	        at.set(arrivalTime);
	        textViewArrivalTime.setText(at.format("%b %d, %G %l:%M%p"));
	        
	        textViewCredits = (TextView) findViewById(R.id.textViewCredits);
	        textViewCredits.setText(String.valueOf(route.getCredits()));
        
	        buttonStartTrip = (Button) findViewById(R.id.buttonStartTrip);
	        buttonStartTrip.setOnClickListener(new OnClickListener() {
	
	            @Override
	            public void onClick(View view) {
	                Intent intent = new Intent(ReservationDetailsActivity.this, ValidationActivity.class);
	                intent.putExtra("route", route);
	                startActivity(intent);
	                finish();
	            }
	            
	        });
        }
    }
}
