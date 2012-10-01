package com.smartrek.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.models.Route;
import com.smartrek.receivers.ReservationReceiver;
import com.smartrek.requests.ReservationMapper;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.HumanReadableTime;

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
	private Button buttonSaveTrip;
	
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
        // FIXME: Date/time format i18n
        Time dt = new Time();
        dt.set(route.getDepartureTime());
        textViewDepartureTime.setText(dt.format("%b %d, %G %l:%M%p"));
        
        textViewArrivalTime = (TextView) findViewById(R.id.textViewArrivalTime);
        // FIXME: Date/time format i18n
        Time at = new Time();
        at.set(route.getArrivalTime());
        textViewArrivalTime.setText(String.format("%s (%s)", at.format("%b %d, %G %l:%M%p"), HumanReadableTime.formatDuration(route.getDuration())));

        textViewCredits = (TextView) findViewById(R.id.textViewCredits);
        textViewCredits.setText(String.valueOf(route.getCredits()));
        
        buttonReserve = (Button) findViewById(R.id.button_reserve);
        buttonReserve.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new ReservationTask().execute();
			}
        });
        
	}
	
	private void scheduleNotification(Route route) {
		
		long departureTime = route.getDepartureTime();
		
		Intent intent = new Intent(this, ReservationReceiver.class);
		intent.putExtra("route", route);
		//intent.putExtra("reservation", reservation);
		// In reality, you would want to have a static variable for the
		// request code instead of 192837
		PendingIntent sender = PendingIntent.getBroadcast(this, 192837,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, departureTime - 60000*5, sender); // 5 min earlier than departure time
		
	}
	
	private final class ReservationTask extends AsyncTask<Object, Object, Object> {
		
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(ReservationConfirmationActivity.this);
			dialog.setMessage("Making reservation...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected Object doInBackground(Object... params) {
			ReservationMapper mapper = new ReservationMapper();
			
			try {
				mapper.reserveRoute(route);
			}
			catch (Exception e) {
				ehs.registerException(e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			dialog.cancel();
			
		    if (ehs.hasExceptions()) {
		        ehs.reportExceptions();
		    }
		    else {
				scheduleNotification(route);
				
				NotificationDialog dialog = new NotificationDialog(ReservationConfirmationActivity.this, "You have successfully reserved a route.");
				dialog.setActionListener(new NotificationDialog.ActionListener() {
                    
                    @Override
                    public void onClickDismiss() {
                        Intent intent = new Intent(ReservationConfirmationActivity.this, ReservationListActivity.class);
                        startActivity(intent);
                        
                        finish();
                        
                    }
                });
				dialog.show();
			}
		}
	}
}
