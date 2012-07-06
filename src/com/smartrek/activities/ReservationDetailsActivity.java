package com.smartrek.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.smartrek.models.Reservation;

/**
 * Shows detailed information about a reservation.
 *
 */
public final class ReservationDetailsActivity extends Activity {
	private TextView textViewName;
	private TextView textViewOrigin;
	private TextView textViewDestination;
	private TextView textViewDepartureTime;
	private TextView textViewArrivalTime;
	private TextView textViewCredits;
	private Button buttonStartTrip;
	
	private Reservation reservation;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reservation_details);
        
        Bundle extras = getIntent().getExtras();
        reservation = extras.getParcelable("reservation");
        
        textViewName = (TextView) findViewById(R.id.textViewReservationName);
        textViewName.setText(String.format("Reservation #%d", reservation.getRid()));
        
        textViewOrigin = (TextView) findViewById(R.id.textViewOrigin);
        textViewOrigin.setText(reservation.getOriginAddress());
        
        textViewDestination = (TextView) findViewById(R.id.textViewDestination);
        textViewDestination.setText(reservation.getDestinationAddress());
        
        textViewDepartureTime = (TextView) findViewById(R.id.textViewDepartureTime);
        // FIXME: Date/time format i18n
        textViewDepartureTime.setText(reservation.getDepartureTime().format("%b %d, %G %l:%M%p"));
        
        textViewArrivalTime = (TextView) findViewById(R.id.textViewArrivalTime);
        // FIXME: Date/time format i18n
        textViewArrivalTime.setText(reservation.getArrivalTime().format("%b %d, %G %l:%M%p"));
        
        textViewCredits = (TextView) findViewById(R.id.textViewCredits);
        textViewCredits.setText(String.valueOf(reservation.getCredits()));
        
        buttonStartTrip = (Button) findViewById(R.id.buttonStartTrip);
        buttonStartTrip.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ReservationDetailsActivity.this, ValidationActivity.class);
                intent.putExtra("route", reservation.getRoute());
                startActivity(intent);
            }
            
        });
    }
}
