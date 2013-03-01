package com.smartrek.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.datetime.HumanReadableTime;

/**
 * Shows detailed information about a reservation.
 *
 */
public final class ReservationDetailsActivity extends ActionBarActivity {
	private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private TextView textViewName;
	private TextView textViewOrigin;
	private TextView textViewDestination;
	private TextView textViewDepartureTime;
	private TextView textViewArrivalTime;
	private TextView textViewDuration;
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
            
            setTitle(String.format("no. %d", route.getId()));
            
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
	        textViewArrivalTime.setText(at.format("%b %d, %G %l:%M%p"));
	        
	        textViewDuration = (TextView) findViewById(R.id.textViewDuration);
	        textViewDuration.setText(HumanReadableTime.formatDuration(duration));
	        
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
	        
	        Font.setTypeface(boldFont, (TextView)findViewById(R.id.textView1),
                (TextView)findViewById(R.id.textView2), (TextView)findViewById(R.id.textView3),
                (TextView)findViewById(R.id.textView4), (TextView)findViewById(R.id.textView5),
                (TextView)findViewById(R.id.textView6), buttonStartTrip);
	        Font.setTypeface(lightFont, textViewArrivalTime, textViewCredits,
                textViewDepartureTime, textViewDestination, textViewDuration, 
                textViewOrigin);
        }
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
        } else{
            textViewHelp.setVisibility(View.GONE);
        }
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
}
