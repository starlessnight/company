package com.smartrek.activities;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.models.Route;
import com.smartrek.receivers.ReservationReceiver;
import com.smartrek.requests.ReservationMapper;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.Cache;
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
	
    /**
     * Creates a system alarm for a single route
     */
	private void scheduleNotification(Route route) {
		
		long departureTime = route.getDepartureTime();
		
		Intent intent = new Intent(this, ReservationReceiver.class);
		
		// TODO: We need a reservation instance here...
		intent.putExtra("route", route);
		
		// NOTE: It appears custom Parcelable objects cannot passed across
		// different processes. Since a PendingIntent launched by AlarmManager
		// is on a separate process, we cannot pack a Route object.
		intent.putExtra("reservationId", route.getId());
		
		// In reality, you would want to have a static variable for the
		// request code instead of 192837
		PendingIntent pendingOperation = PendingIntent.getBroadcast(this, 192837,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, departureTime - 60000*5, pendingOperation); // 5 min earlier than departure time

		Cache cache = Cache.getInstance();
		if (cache.has("pendingAlarms")) {
			@SuppressWarnings("unchecked")
			List<PendingIntent> pendingAlarms = (List<PendingIntent>) cache.fetch("pendingAlarms");
			pendingAlarms.add(pendingOperation);
		}
		else {
			List<PendingIntent> pendingOperations = new LinkedList<PendingIntent>();
			pendingOperations.add(pendingOperation);
			
			cache.put("pendingAlarms", pendingOperations);
		}
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
