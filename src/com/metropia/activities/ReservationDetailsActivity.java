package com.metropia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.models.Reservation;
import com.metropia.models.Route;
import com.metropia.requests.Request;
import com.metropia.ui.menu.MainMenu;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.datetime.HumanReadableTime;

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
        
        Localytics.integrate(this);
        
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
            
            setTitle(String.format("No. %d", reservation.getRid()));
            
	        textViewOrigin = (TextView) findViewById(R.id.textViewOrigin);
	        textViewOrigin.setText(origin);
	        
	        textViewDestination = (TextView) findViewById(R.id.textViewDestination);
	        textViewDestination.setText(destination);
	        
	        textViewDepartureTime = (TextView) findViewById(R.id.textViewDepartureTime);
	        // FIXME: Date/time format i18n
	        textViewDepartureTime.setText(Reservation.formatTime(route.getDepartureTime()));
	        
	        textViewArrivalTime = (TextView) findViewById(R.id.textViewArrivalTime);
	        // FIXME: Date/time format i18n
	        textViewArrivalTime.setText(Reservation.formatTime(arrivalTime));
	        
	        textViewDuration = (TextView) findViewById(R.id.textViewDuration);
	        textViewDuration.setText(HumanReadableTime.formatDuration(duration));
	        
	        textViewCredits = (TextView) findViewById(R.id.textViewCredits);
	        textViewCredits.setText(String.valueOf(Request.NEW_API?reservation.getCredits():route.getCredits()));
	        
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
                textViewOrigin, textViewHelp);
        }
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
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
    protected void onResume() {
        super.onResume();
        
        if (reservation != null) {
            buttonStartTrip.setEnabled(reservation.isEligibleTrip());
            textViewHelp.setVisibility(reservation.isEligibleTrip() ? View.GONE : View.VISIBLE);
            
            if (reservation.hasExpired()) {
            	textViewHelp.setText(getResources().getString(R.string.trip_has_expired));
            }
            else if (reservation.isTooEarlyToStart()) {
                long minutes = (reservation.getDepartureTime() - System.currentTimeMillis()) / 60000;
            	String msg = getResources().getString(R.string.trip_too_early_to_start, minutes);
            	if(minutes != 1){
            	    msg += "s";
            	}
                textViewHelp.setText(msg);
            }
        } else{
            textViewHelp.setVisibility(View.GONE);
        }
        
        Localytics.openSession();
	    Localytics.upload();
	    if(this instanceof FragmentActivity) {
	    	Localytics.setInAppMessageDisplayActivity(this);
	    }
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
    }
    
    @Override
    protected void onPause() {
    	if(this instanceof FragmentActivity) {
	    	Localytics.dismissCurrentInAppMessage();
		    Localytics.clearInAppMessageDisplayActivity();
    	}
	    Localytics.closeSession();
	    Localytics.upload();
        super.onPause();
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
}
