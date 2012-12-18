package com.smartrek.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.datetime.HumanReadableTime;

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
	private TextView textViewHelp;
	private Button buttonStartTrip;
	
	/**
	 * @deprecated
	 */
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
            int duration = route.getDuration();
            
            String origin = route.getOrigin();
            String destination = route.getDestination();
            
            if (reservation != null) {
                arrivalTime = reservation.getArrivalTime();
                duration = reservation.getDuration();
                
                origin = reservation.getOriginAddress();
                destination = reservation.getDestinationAddress();
            }
            
            setTitle(String.format("Reservation #%d", route.getId()));
            
	        textViewOrigin = (TextView) findViewById(R.id.textViewOrigin);
	        textViewOrigin.setText(origin);
	        
	        textViewDestination = (TextView) findViewById(R.id.textViewDestination);
	        textViewDestination.setText(destination);
	        
	        textViewDepartureTime = (TextView) findViewById(R.id.textViewDepartureTime);
	        // FIXME: Date/time format i18n
	        Time dt = new Time();
	        dt.set(route.getDepartureTime());
	        textViewDepartureTime.setText(dt.format("%b %d, %G %l:%M%p"));
	        
	        textViewArrivalTime = (TextView) findViewById(R.id.textViewArrivalTime);
	        // FIXME: Date/time format i18n
	        Time at = new Time();
	        at.set(arrivalTime);
	        textViewArrivalTime.setText(String.format("%s (%s)", at.format("%b %d, %G %l:%M%p"), HumanReadableTime.formatDuration(duration)));
	        
	        textViewCredits = (TextView) findViewById(R.id.textViewCredits);
	        textViewCredits.setText(String.valueOf(route.getCredits()));
	        
	        textViewHelp = (TextView) findViewById(R.id.textViewHelp);
        
	        buttonStartTrip = (Button) findViewById(R.id.buttonStartTrip);
	        buttonStartTrip.setOnClickListener(new OnClickListener() {
	
	            @Override
	            public void onClick(View view) {
	            	Log.d("ReservationDetailsActivity", "buttonStartTrip.onClick()");
	            	
	                Intent intent = new Intent(ReservationDetailsActivity.this, ValidationActivity.class);
	                intent.putExtra("route", route);
	                intent.putExtra("reservation", reservation);
	                startActivity(intent);
	                finish();
	            }
	            
	        });
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if (reservation != null) {
            buttonStartTrip.setEnabled(reservation.isEligibleTrip());
            textViewHelp.setVisibility(reservation.isEligibleTrip() ? View.GONE : View.VISIBLE);
            
            if (reservation.hasExpired()) {
            	textViewHelp.setText(getResources().getString(R.string.trip_has_expired));
            }
            else if (reservation.isTooEarlyToStart()) {
            	textViewHelp.setText(getResources().getString(R.string.trip_too_early_to_start));
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        MainMenu.onMenuItemSelected(this, featureId, item);
        
        return super.onMenuItemSelected(featureId, item);
    }
}
