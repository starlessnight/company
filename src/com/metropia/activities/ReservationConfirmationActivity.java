package com.metropia.activities;

import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.google.android.gms.analytics.GoogleAnalytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.activities.DebugOptionsActivity.FakeRoute;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.models.Reservation;
import com.metropia.models.Route;
import com.metropia.models.User;
import com.metropia.receivers.ReservationReceiver;
import com.metropia.requests.Request;
import com.metropia.requests.ReservationRequest;
import com.metropia.ui.menu.MainMenu;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.datetime.HumanReadableTime;
import com.metropia.activities.R;

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
        
        TimeZone timzone = TimeZone.getTimeZone(Request.getTimeZone(route.getTimezoneOffset()));
        
        textViewOrigin = (TextView) findViewById(R.id.textViewOrigin);
        textViewOrigin.setText(route.getOrigin());
        
        textViewDestination = (TextView) findViewById(R.id.textViewDestination);
        textViewDestination.setText(route.getDestination());
        
        textViewDepartureTime = (TextView) findViewById(R.id.textViewDepartureTime);
        // FIXME: Date/time format i18n
        textViewDepartureTime.setText(Reservation.formatTime(route.getDepartureTime(), timzone));
        
        textViewArrivalTime = (TextView) findViewById(R.id.textViewArrivalTime);
        // FIXME: Date/time format i18n
        textViewArrivalTime.setText(Reservation.formatTime(route.getArrivalTime(), timzone));
        
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
	}
	
	@Override
	protected void onPause() {
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
	
    /**
     * Creates a system alarm for a single route
     */
	public static void scheduleNotification(Context ctx, long reservationId, Route route) {
		
		long departureTime = route.getDepartureTime();
		
		Intent intent = new Intent(ctx, ReservationReceiver.class);
		
		// TODO: We need a reservation instance here...
		intent.putExtra("route", route);
		
		// NOTE: It appears custom Parcelable objects cannot passed across
		// different processes. Since a PendingIntent launched by AlarmManager
		// is on a separate process, we cannot pack a Route object.
		intent.putExtra(ReservationReceiver.RESERVATION_ID, reservationId);
		
		// In reality, you would want to have a static variable for the
		// request code instead of 192837
		PendingIntent pendingOperation = PendingIntent.getBroadcast(ctx, 192837,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, departureTime - Reservation.GRACE_INTERVAL, pendingOperation);
	}
	
	public static void cancelNotification(Context ctx){
        Intent intent = new Intent(ctx, ReservationReceiver.class);
        PendingIntent.getBroadcast(ctx, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT).cancel();
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(ReservationReceiver.ID);
	}
	
	private final class ReservationTask extends AsyncTask<Object, Object, Long> {
		
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
		protected Long doInBackground(Object... params) {
		    Long rs = null;
			ReservationRequest request = new ReservationRequest(User.getCurrentUser(ReservationConfirmationActivity.this), 
		        route, getString(R.string.distribution_date), 0);
			try {
			    rs = request.execute(ReservationConfirmationActivity.this);
			}
			catch (Exception e) {
				ehs.registerException(e);
			}
			
			return rs;
		}
		
		@Override
		protected void onPostExecute(Long result) {
			dialog.cancel();
			
		    if (ehs.hasExceptions()) {
		        ehs.reportExceptions();
		    }
		    else {
				scheduleNotification(ReservationConfirmationActivity.this, result, route);
				
				if(route.isFake()){
				    FakeRoute fakeRoute = new FakeRoute();
				    fakeRoute.id = route.getId();
				    fakeRoute.seq = route.getSeq();
				    DebugOptionsActivity.addFakeRoute(ReservationConfirmationActivity.this, fakeRoute);
				}
				
				NotificationDialog2 dialog = new NotificationDialog2(ReservationConfirmationActivity.this, "You have successfully reserved a route.");
				dialog.setTitle("Notification");
				dialog.setPositiveActionListener(new NotificationDialog2.ActionListener() {
                    
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(ReservationConfirmationActivity.this, LandingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
