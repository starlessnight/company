package com.smartrek.activities;

import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.smartrek.mappers.ReservationMapper;
import com.smartrek.models.Route;
import com.smartrek.receivers.ReservationReceiver;
import com.smartrek.utils.ExceptionHandlingService;

/**
 * This will popup before a user makes a reservation for a route
 *
 */
public final class ReservationConfirmationActivity extends Activity {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private Route route;
	
	private TextView textViewOrigin;
	private TextView textViewDestination;
	private TextView textViewDepartureTime;
	private TextView textViewArrivalTime;
	private TextView textViewCredits;
	
	private Button buttonReserve;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reservation_confirmation);

        Bundle extras = getIntent().getExtras();
        route = extras.getParcelable("route");
        
        textViewOrigin = (TextView) findViewById(R.id.textViewOrigin);
        textViewOrigin.setText(route.getOrigin());
        
        textViewDestination = (TextView) findViewById(R.id.textViewDestination);
        textViewDestination.setText(route.getDestination());
        
        textViewDepartureTime = (TextView) findViewById(R.id.textViewDepartureTime);
        textViewDepartureTime.setText(route.getDepartureTime().format2445());
        
        textViewArrivalTime = (TextView) findViewById(R.id.textViewArrivalTime);
        textViewArrivalTime.setText(route.getArrivalTime().format2445());

        textViewCredits = (TextView) findViewById(R.id.textViewCredits);
        
        buttonReserve = (Button) findViewById(R.id.buttonReserve);
        buttonReserve.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new ReservationTask().execute();
			}
        });
        
	}
	
	private void scheduleEvent(Route route) {
		
		long departureTime = route.getDepartureTime().toMillis(false);
		
		Intent intent = new Intent(ReservationConfirmationActivity.this, ReservationReceiver.class);
		intent.putExtra("route", route);
		intent.putExtra("alarm_message", "O'Doyle Rules!");
		// In reality, you would want to have a static variable for the
		// request code instead of 192837
		PendingIntent sender = PendingIntent.getBroadcast(ReservationConfirmationActivity.this, 192837,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, departureTime, sender);
		
		Log.d("ReservationConfirmationActivity", "Event has been scheduled. " + departureTime);
	}
	
	private final class ReservationTask extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			ReservationMapper mapper = new ReservationMapper();
			
			try {
				mapper.reserveRoute(route);
			}
			catch (IOException e) {
				ehs.registerException(e);
			}
            catch (JSONException e) {
                ehs.registerException(e);
            }
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
		    if (ehs.hasExceptions()) {
		        ehs.reportExceptions();
		    }
		    else {
				scheduleEvent(route);
				
				Intent intent = new Intent(ReservationConfirmationActivity.this, ReservationListActivity.class);
//				Bundle extras = new Bundle();
//				extras.putParcelable("route", route);
//				intent.putExtras(extras);
				startActivity(intent);
				
				finish();
			}
		}
	}
}
