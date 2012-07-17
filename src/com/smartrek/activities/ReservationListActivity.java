package com.smartrek.activities;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.smartrek.mappers.ReservationMapper;
import com.smartrek.models.Reservation;
import com.smartrek.models.User;
import com.smartrek.ui.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;

/**
 * Shows a list of reserved routes
 *
 */
public final class ReservationListActivity extends Activity {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private List<Reservation> reservations;
	
	private ListView listViewReservation;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reservation_list);
        
        reservations = new ArrayList<Reservation>();
        listViewReservation = (ListView) findViewById(R.id.listViewReservation);
        listViewReservation.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(ReservationListActivity.this, ReservationDetailsActivity.class);
				
				Bundle extras = new Bundle();
				// FIXME: Reservation.getRoute() is a temporary solution
				extras.putParcelable("route", reservations.get(position).getRoute());
				intent.putExtras(extras);
				startActivity(intent);
			}
        });
        
        User currentUser = User.getCurrentUser(this);
        
        new ReservationRetrivalTask().execute(currentUser.getId());
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
	 * Inner class for an asynchronous task.
	 */
	private class ReservationRetrivalTask extends AsyncTask<Object, Object, String> {
		
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(ReservationListActivity.this);
			dialog.setMessage("Loading reservations...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();
		}
		
		@Override
		protected String doInBackground(Object... params) {
			int uid = (Integer) params[0];
			
			ReservationMapper mapper = new ReservationMapper();
			try {
                reservations = mapper.getReservations(uid);
            }
            catch (IOException e) {
                ehs.registerException(e);
            }
            catch (JSONException e) {
                ehs.registerException(e);
            }
            catch (ParseException e) {
                ehs.registerException(e);
            }
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			dialog.cancel();
			
		    if (ehs.hasExceptions()) {
		        ehs.reportExceptions();
		    }
		    else {
		        listViewReservation.setAdapter(new ReservationItemAdapter(ReservationListActivity.this, R.layout.reservation_list_item, reservations));
		    }
	    }
	}
	
	private class ReservationItemAdapter extends ArrayAdapter<Reservation> {
		
		private int textViewResourceId;

		public ReservationItemAdapter(Context context, int textViewResourceId,
				List<Reservation> objects) {
			super(context, textViewResourceId, objects);
			
			this.textViewResourceId = textViewResourceId;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			
			if (view == null) {
				LayoutInflater inflater = getLayoutInflater();
				view = inflater.inflate(textViewResourceId, parent, false);
			}
			
			Reservation r = getItem(position);
			
			TextView textView1 = (TextView)view.findViewById(R.id.textViewOrigin);
			textView1.setText(r.getOriginAddress());
			
			TextView textView2 = (TextView)view.findViewById(R.id.textViewDestination);
			textView2.setText(r.getDestinationAddress());
			
			TextView textViewCredits = (TextView) view.findViewById(R.id.textViewCredits);
			textViewCredits.setText(String.format("%d", r.getCredits()));
			
			return view;
		}
	}
}