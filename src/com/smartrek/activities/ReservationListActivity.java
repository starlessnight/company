package com.smartrek.activities;

import java.util.ArrayList;
import java.util.Collections;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.models.Reservation;
import com.smartrek.models.User;
import com.smartrek.requests.ReservationDeleteRequest;
import com.smartrek.requests.ReservationListFetchRequest;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;

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
        
//        listViewGeneric.setOnRefreshListener(new OnRefreshListener() {
//
//            @Override
//            public void onRefresh() {
//                requestRefresh(false);
//            }
//        });
        
        textViewGeneric.setText("You do not have any reserved route.");
        
        registerForContextMenu(getListView());
        
        reservations = new ArrayList<Reservation>();
        
        Font.setTypeface(lightFont, textViewGeneric);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
		requestRefresh(true);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
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
		MenuInflater mi = getSupportMenuInflater();
		mi.inflate(R.menu.list_activity, menu);
		return true;
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		MainMenu.onMenuItemSelected(this, featureId, item);
		
		switch (item.getItemId()) {
			case R.id.refresh:
				requestRefresh(false);
				break;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    android.view.MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.reservations_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.delete:
	            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	            int menuItemIndex = info.position;
	            Reservation reservation = reservations.get(menuItemIndex);
	            new ReservationDeleteTask(menuItemIndex).execute(
                    User.getCurrentUser(this), reservation.getRid());
	            return true;
	            
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
    private void requestRefresh(boolean useCache) {
        User currentUser = User.getCurrentUser(this);
        new ReservationRetrivalTask(useCache, currentUser).execute();
    }
	   
	/**
	 * Inner class for an asynchronous task.
	 */
	private class ReservationRetrivalTask extends AsyncTask<Object, Object, List<Reservation>> {
		
		private ProgressDialog dialog;
		
		private boolean useCache;
		
		private ReservationListFetchRequest request;
		
		public ReservationRetrivalTask(boolean useCache, User user) {
		    this.useCache = useCache;
		    this.request = new ReservationListFetchRequest(user);
		}

		@Override
		protected void onPreExecute() {
		    if (!useCache || !request.isCached(ReservationListActivity.this)) {
    			dialog = new ProgressDialog(ReservationListActivity.this);
    			dialog.setMessage("Loading reservations...");
    			dialog.setIndeterminate(true);
    			dialog.setCancelable(false);
    			dialog.setCanceledOnTouchOutside(false);
    			dialog.show();
		    }
		}
		
		@Override
		protected List<Reservation> doInBackground(Object... params) {
			//int uid = (Integer) params[0];
			
			//ReservationListFetchRequest request = new ReservationListFetchRequest(uid);
			try {
			    if (!useCache) {
			        request.invalidateCache(ReservationListActivity.this);
			    }
				reservations = request.execute(ReservationListActivity.this);
				
				if (debugPrefs.getBoolean(DebugOptionsActivity.DEBUG_MODE, false)) {
				}
				
				Collections.sort(reservations, Reservation.orderByDepartureTime());
            }
            catch (Exception e) {
                ehs.registerException(e);
            }
			
			return reservations;
		}
		
		@Override
		protected void onPostExecute(List<Reservation> result) {
		    if (dialog != null && dialog.isShowing()) {
		        dialog.cancel();
		    }
			//listViewGeneric.onRefreshComplete();
			
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
	
	private class ReservationItemAdapter extends ArrayAdapter<Reservation> {
		
		private int textViewResourceId;

		public ReservationItemAdapter(Context context, int textViewResourceId,
				List<Reservation> objects) {
			super(context, textViewResourceId, objects);
			
			this.textViewResourceId = textViewResourceId;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			
			if (view == null) {
				LayoutInflater inflater = getLayoutInflater();
				view = inflater.inflate(textViewResourceId, parent, false);
			}
			
			final Reservation r = getItem(position);
			
			TextView textView1 = (TextView)view.findViewById(R.id.textViewOrigin);
			textView1.setText(r.getOriginAddress());
			
			TextView textView2 = (TextView)view.findViewById(R.id.textViewDestination);
			textView2.setText(r.getDestinationAddress());
			
			TextView textViewDepartureTime = (TextView)view.findViewById(R.id.textViewDepartureTime);
			textViewDepartureTime.setText(Reservation.formatTime(r.getDepartureTime()));
			
			//TextView textViewCredits = (TextView) view.findViewById(R.id.textViewCredits);
			//textViewCredits.setText(String.format("%d", r.getCredits()));
			
			TextView itemNum = (TextView)view.findViewById(R.id.itemNum);
			itemNum.setText(String.format("No. %d", r.getRid()));
			
            Font.setTypeface(ReservationListActivity.this.boldFont, 
		        itemNum, (TextView)view.findViewById(R.id.textView0),
		        (TextView)view.findViewById(R.id.textView1), (TextView)view.findViewById(R.id.textView3)
			);
			Font.setTypeface(ReservationListActivity.this.lightFont, textView1, 
		        textView2, textViewDepartureTime);
			
			return view;
		}
	}
	
	private class ReservationDeleteTask extends AsyncTask<Object, Object, Object> {

	    private ProgressDialog dialog;
	    
        private int listItemIndex;
        
        public ReservationDeleteTask(int listItemIndex) {
            this.listItemIndex = listItemIndex;
        }
        
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ReservationListActivity.this);
            dialog.setMessage("Deleting reservation...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
        
        @Override
        protected Object doInBackground(Object... params) {
            User user = (User) params[0];
            long rid = (Long) params[1];
            
            ReservationDeleteRequest request = new ReservationDeleteRequest(user, rid);
            try {
                request.execute();
            }
            catch (Exception e) {
                ehs.registerException(e);
            }
            
            return null;
        }
        
        @Override
        protected void onPostExecute(Object result) {
            if (dialog != null && dialog.isShowing()) {
                dialog.cancel();
            }
            if (ehs.hasExceptions()) {
                ehs.reportExceptions();
            }
            else {
                requestRefresh(false);
            }
        }
    }
	
}