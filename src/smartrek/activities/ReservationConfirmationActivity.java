package smartrek.activities;

import java.io.IOException;
import java.util.Stack;

import smartrek.mappers.ReservationMapper;
import smartrek.models.Route;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

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
	private Button buttonCancel;
	
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
        
        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ReservationConfirmationActivity.this.finish();
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
