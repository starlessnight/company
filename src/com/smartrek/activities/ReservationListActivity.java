package com.smartrek.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

import com.smartrek.models.Reservation;
import com.smartrek.models.User;
import com.smartrek.requests.ReservationListFetchRequest;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;

/**
 * Shows a list of reserved routes
 *
 */
public final class ReservationListActivity extends ListActivity {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private List<Reservation> reservations;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.reservation_list);
        
        setTitle("Reservations");
        
        getListView().setBackgroundDrawable(getResources().getDrawable(R.drawable.background_gradient));
        
        //registerForContextMenu(getListView());
        
        reservations = new ArrayList<Reservation>();
        
        User currentUser = User.getCurrentUser(this);
        
        new ReservationRetrivalTask().execute(currentUser.getId());
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(ReservationListActivity.this, ReservationDetailsActivity.class);
		
		Bundle extras = new Bundle();
		// FIXME: Reservation.getRoute() is a temporary solution
		extras.putParcelable("route", reservations.get(position).getRoute());
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
				reservations = new ArrayList<Reservation>();
                for (Reservation resv : request.execute()) {
                	if (!resv.isPast()) {
                		reservations.add(resv);
                	}
                }
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
		    	}
		    	else {
		    		
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
			
			TextView textViewCredits = (TextView) view.findViewById(R.id.textViewCredits);
			textViewCredits.setText(String.format("%d", r.getCredits()));
			
			return view;
		}
	}
}