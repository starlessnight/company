package com.smartrek.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;
import com.smartrek.models.Reservation;
import com.smartrek.models.User;
import com.smartrek.requests.ReservationListFetchRequest;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;

/**
 * Shows a list of reserved routes
 *
 */
public final class ReservationListActivity extends GenericListActivity<Reservation> {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
    private SharedPreferences debugPrefs;
	
	private List<Reservation> reservations;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        listViewGeneric.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
            }
        });
        
        textViewGeneric.setText("You do not have any reserved route.");
        
        //registerForContextMenu(getListView());
        
        reservations = new ArrayList<Reservation>();
        
        User currentUser = User.getCurrentUser(this);
        
        new ReservationRetrivalTask().execute(currentUser.getId());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		debugPrefs = getSharedPreferences(DebugOptionsActivity.DEBUG_PREFS, MODE_PRIVATE);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(ReservationListActivity.this, ReservationDetailsActivity.class);
		
		Bundle extras = new Bundle();
		// FIXME: Reservation.getRoute() is a temporary solution
		extras.putParcelable("route", reservations.get(position).getRoute());
		extras.putParcelable("reservation", reservations.get(position));
		intent.putExtras(extras);
		startActivity(intent);
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
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    //AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case R.id.delete:
	            return true;
	            
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	/**
	 * Inner class for an asynchronous task.
	 */
	private class ReservationRetrivalTask extends AsyncTask<Object, Object, List<Reservation>> {
		
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
		protected List<Reservation> doInBackground(Object... params) {
			int uid = (Integer) params[0];
			
			ReservationListFetchRequest request = new ReservationListFetchRequest(uid);
			try {
				reservations = request.execute();
				
				if (debugPrefs.getBoolean(DebugOptionsActivity.DEBUG_MODE, false)) {
				}
				
				Collections.reverse(reservations);
            }
            catch (Exception e) {
                ehs.registerException(e);
            }
			
			return reservations;
		}
		
		@Override
		protected void onPostExecute(List<Reservation> result) {
			dialog.cancel();
			
		    if (ehs.hasExceptions()) {
		        ehs.reportExceptions();
		    }
		    else {
		    	if (reservations != null && reservations.size() > 0) {
		    		setListAdapter(new ReservationItemAdapter(ReservationListActivity.this, R.layout.reservation_list_item, reservations));
		    		setStatus(GenericListActivity.Status.GenericList);
		    	}
		    	else {
		    		setStatus(GenericListActivity.Status.EmptyList);
		    	}
		    }
	    }
	}
	
	private class ReservationDeleteTask extends AsyncTask<Object, Object, String> {
		@Override
		protected String doInBackground(Object... params) {
			
			
			return null;
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
			
			TextView textViewDepartureTime = (TextView)view.findViewById(R.id.textViewDepartureTime);
			SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
			textViewDepartureTime.setText(formatter.format(new Date(r.getDepartureTime())));
			
			TextView textViewCredits = (TextView) view.findViewById(R.id.textViewCredits);
			textViewCredits.setText(String.format("%d", r.getCredits()));
			
			return view;
		}
	}
}