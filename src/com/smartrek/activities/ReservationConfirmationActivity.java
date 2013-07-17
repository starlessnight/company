package com.smartrek.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.activities.DebugOptionsActivity.FakeRoute;
import com.smartrek.activities.DebugOptionsActivity.NavigationLink;
import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.receivers.ReservationReceiver;
import com.smartrek.requests.ReservationRequest;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.datetime.HumanReadableTime;

/**
 * This will popup before a user makes a reservation for a route
 *
 */
public final class ReservationConfirmationActivity extends ActionBarActivity {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private Route route;
	
	private TextView textViewOrigin;
	private TextView textViewDestination;
	private TextView textViewDepartureTime;
	private TextView textViewArrivalTime;
	private TextView textViewDuration;
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
        textViewDepartureTime.setText(Reservation.formatTime(route.getDepartureTime()));
        
        textViewArrivalTime = (TextView) findViewById(R.id.textViewArrivalTime);
        // FIXME: Date/time format i18n
        textViewArrivalTime.setText(Reservation.formatTime(route.getArrivalTime()));
        
        textViewDuration = (TextView) findViewById(R.id.textViewDuration);
        textViewDuration.setText(HumanReadableTime.formatDuration(route.getDuration()));

        textViewCredits = (TextView) findViewById(R.id.textViewCredits);
        textViewCredits.setText(String.valueOf(route.getCredits()));
        
        buttonReserve = (Button) findViewById(R.id.button_reserve);
        buttonReserve.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new ReservationTask().execute();
			}
        });
        
        Font.setTypeface(boldFont, (TextView)findViewById(R.id.textView1),
            (TextView)findViewById(R.id.textView2), (TextView)findViewById(R.id.textView3), 
            (TextView)findViewById(R.id.textView4), (TextView)findViewById(R.id.textView5), 
            (TextView)findViewById(R.id.textView6),
            buttonReserve);
        Font.setTypeface(lightFont, textViewArrivalTime, textViewCredits,
            textViewDepartureTime, textViewDestination, textViewDuration, 
            textViewOrigin);
        
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
		intent.putExtra(ReservationReceiver.RESERVATION_ID, route.getId());
		
		// In reality, you would want to have a static variable for the
		// request code instead of 192837
		PendingIntent pendingOperation = PendingIntent.getBroadcast(this, 192837,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, departureTime - 60000*5, pendingOperation); // 5 min earlier than departure time
	}
	
	private final class ReservationTask extends AsyncTask<Object, Object, Integer> {
		
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(ReservationConfirmationActivity.this);
			dialog.setMessage("Making reservation...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}

		@Override
		protected Integer doInBackground(Object... params) {
		    Integer rs = null;
			ReservationRequest request = new ReservationRequest(User.getCurrentUser(ReservationConfirmationActivity.this), 
		        route, getString(R.string.distribution_date));
			try {
			    rs = request.execute();
			}
			catch (Exception e) {
				ehs.registerException(e);
			}
			
			return rs;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			dialog.cancel();
			
		    if (ehs.hasExceptions()) {
		        ehs.reportExceptions();
		    }
		    else {
				scheduleNotification(route);
				
				if(route.isFake()){
				    FakeRoute fakeRoute = new FakeRoute();
				    fakeRoute.id = route.getId();
				    fakeRoute.seq = route.getSeq();
				    DebugOptionsActivity.addFakeRoute(ReservationConfirmationActivity.this, fakeRoute);
				}
				
				NavigationLink link = route.getLink();
				if(link != null && result != null){
				    link.id = result;
				    DebugOptionsActivity.addNavLink(ReservationConfirmationActivity.this, link);
				}
				
				NotificationDialog dialog = new NotificationDialog(ReservationConfirmationActivity.this, "You have successfully reserved a route.");
				dialog.setActionListener(new NotificationDialog.ActionListener() {
                    
                    @Override
                    public void onClickDismiss() {
                        Intent intent = new Intent(ReservationConfirmationActivity.this, ReservationListActivity.class);
                        startActivity(intent);
                        
                        setResult(RouteActivity.RESERVATION_CONFIRM_ENDED);
                        finish();
                        
                    }
                });
				dialog.show();
			}
		}
	}
}
