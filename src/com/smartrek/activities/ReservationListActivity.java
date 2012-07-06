package com.smartrek.activities;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

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

/**
 * Shows a list of reserved routes
 *
 */
public final class ReservationListActivity extends ExceptionSafeActivity {
	
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
				extras.putParcelable("reservation", reservations.get(position));
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
		@Override
		protected String doInBackground(Object... params) {
			int uid = (Integer) params[0];
			
			ReservationMapper mapper = new ReservationMapper();
			try {
                reservations = mapper.getReservations(uid);
            }
            catch (IOException e) {
                registerException(e);
            }
            catch (JSONException e) {
                registerException(e);
            }
            catch (ParseException e) {
                registerException(e);
            }
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
		    if (exceptions.isEmpty()) {
		        listViewReservation.setAdapter(new ReservationItemAdapter(ReservationListActivity.this, R.layout.reservation_list_item, reservations));
		    }
		    else {
		        reportExceptions();
		    }
	    }
	}
	
	private class ReservationItemAdapter extends ArrayAdapter<Reservation> {
		
		private int textViewResourceId;
		private List<Reservation> objects;

		public ReservationItemAdapter(Context context, int textViewResourceId,
				List<Reservation> objects) {
			super(context, textViewResourceId, objects);
			
			this.textViewResourceId = textViewResourceId;
			this.objects = objects;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Reservation r = objects.get(position);
			
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(textViewResourceId, parent, false);
			
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