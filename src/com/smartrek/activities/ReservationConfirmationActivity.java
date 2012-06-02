package com.smartrek.activities;

import java.io.IOException;
import java.util.Stack;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
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

/**
 * This will popup before a user makes a reservation for a route
 *
 */
public final class ReservationConfirmationActivity extends Activity {
	
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
	
	private Stack<Exception> exceptions = new Stack<Exception>();
	
	private void reportException(String message) {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle("Exception");
        dialog.setMessage(message);
        dialog.setButton("Dismiss", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
	}
	
	private void reportExceptions() {
		while (!exceptions.isEmpty()) {
			Exception e = exceptions.pop();
			
            reportException(e.getMessage());
		}
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
				e.printStackTrace();
				exceptions.push(e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			if (exceptions.isEmpty()) {
				
				scheduleEvent(route);
				
				Intent intent = new Intent(ReservationConfirmationActivity.this, ReservationListActivity.class);
//				Bundle extras = new Bundle();
//				extras.putParcelable("route", route);
//				intent.putExtras(extras);
				startActivity(intent);
				
				finish();
			}
			else {
				reportExceptions();
			}
		}
	}
}
