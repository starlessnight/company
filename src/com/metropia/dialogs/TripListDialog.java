package com.metropia.dialogs;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;
import com.metropia.models.Address;
import com.metropia.models.Trip;
import com.metropia.models.User;
import com.metropia.requests.FavoriteAddressFetchRequest;
import com.metropia.requests.Request;
import com.metropia.requests.TripDeleteRequest;
import com.metropia.requests.TripLinkRequest;
import com.metropia.requests.TripListFetchRequest;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.activities.R;

public class TripListDialog extends GenericListDialog<Trip> {
	
	public interface ActionListener extends GenericListDialog.ActionListener<Trip> {}
	
	private ExceptionHandlingService ehs = new ExceptionHandlingService(getContext());
	
	private DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (actionListener != null) {
				actionListener.onClickNegativeButton();
			}
		}
		
	};
	
	public TripListDialog(Context context) {
		super(context, null);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.trip_list, null);
		
		setContentView(dialogView);
		
		//setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", onClickListener);
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
		
		setTitle("Select Trip");
		textViewGeneric.setText("You don't have any saved trip");
		
		// enables context menu
		registerForContextMenu(listViewGeneric);
		listViewGeneric.setOnCreateContextMenuListener(this);
        listViewGeneric.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                requestRefresh();
            }
        });
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		requestRefresh();		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getOwnerActivity().getMenuInflater();
	    inflater.inflate(R.menu.context, menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem menuItem) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuItem.getMenuInfo();
	    
	    // For some reason info.position returns a value offset by +1
	    int menuItemIndex = info.position - 1;
	    
	    Trip listItem = listItems.get(menuItemIndex);
	    
	    switch (menuItem.getItemId()) {
	    	case R.id.edit:
	    		showTripEditDialog(listItem);
	    		return true;
	    		
	        case R.id.delete:
	        	new TripDeleteTask(menuItemIndex, getContext()).execute(listItem.getId());
	            return true;
	            
	        default:
	            return super.onMenuItemSelected(featureId, menuItem);
	    }
	}
	
	private void requestRefresh() {
		User currentUser = User.getCurrentUser(getContext());
		new TripListFetchRequest(currentUser).invalidateCache(getContext());
		new TripListFetchTask().execute(currentUser);
	}
	
	private TripEditDialog dialog;
	
	private void showTripEditDialog(Trip trip) {
		dialog = new TripEditDialog(getContext(), trip);
		dialog.setActionListener(new TripEditDialog.ActionListener() {
			
			@Override
			public void onClickPositiveButton(String name, Address origin, Address destination) {
				requestRefresh();
			}
			
			@Override
			public void onClickNegativeButton() {
			    dialog = null;
			}
		});
		dialog.show();
	}
	
	public void resizeButtonText(){
        if(dialog != null && dialog.isShowing()){
            dialog.resizeButtonText();
        }
    }
	
	private class TripDeleteTask extends AsyncTask<Object, Object, Object> {

		private int listItemIndex;
		
		private Context ctx;
		
		public TripDeleteTask(int listItemIndex, Context ctx) {
			this.listItemIndex = listItemIndex;
			this.ctx = ctx;
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			int fid = (Integer) params[0];
			
			User user = User.getCurrentUser(getContext());
			try {
			    
			    TripDeleteRequest request = new TripDeleteRequest(
		            new TripLinkRequest(user).execute(getContext()), fid, user);
				request.execute(ctx);
				
				// clear cache
				new TripListFetchRequest(User.getCurrentUser(getContext())).invalidateCache(getContext());
			}
			catch (Exception e) {
				ehs.registerException(e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
		    if (ehs.hasExceptions()) {
		        ehs.reportExceptions();
		    }
		    else {
		    	listItems.remove(listItemIndex);
		    	listViewGeneric.setAdapter(new TripListAdapter(getContext(), listItems));
		    	listViewGeneric.postInvalidate();
		    }
		}
	}
	
	private class TripListFetchTask extends AsyncTask<Object, Object, List<Trip>> {

		@Override
		protected List<Trip> doInBackground(Object... params) {
		    User user = (User) params[0];
			
			TripListFetchRequest request = new TripListFetchRequest(user);
			try {
			    //request.invalidateCache();
				listItems = request.execute(getContext());
			}
			catch (Exception e) {
				ehs.registerException(e);
			}

			return listItems;
		}
		
		@Override
		protected void onPostExecute(final List<Trip> result) {
			if (ehs.hasExceptions()) {
				ehs.reportExceptions();
				listViewGeneric.onRefreshComplete();
			}
			else {
			    if(Request.NEW_API){
    			    new AsyncTask<Void, Void, List<Address>>(){
                        @Override
                        protected List<Address> doInBackground(Void... params) {
                            User currentUser = User.getCurrentUser(getContext());
                            FavoriteAddressFetchRequest req = new FavoriteAddressFetchRequest(currentUser);
                            req.invalidateCache(getContext());
                            List<Address> addresses;
                            try {
                                addresses = req.execute(getContext());
                            }
                            catch (Exception e) {
                                ehs.registerException(e);
                                addresses = Collections.emptyList();
                            }
                            return addresses;
                        }
                        @Override
                        protected void onPostExecute(List<Address> addresses) {
                            for(Trip trip : result){
                                for (final Address address : addresses) {
                                    int id = address.getId();
                                    String addStr = address.getAddress();
                                    if(id == trip.getOriginID()){
                                        trip.setOrigin(addStr);
                                    }else if(id == trip.getDestinationID()){
                                        trip.setDestination(addStr);
                                    }
                                }
                            }
                            refreshListItems(result);
                        }
                    }.execute();
			    }else{
			        refreshListItems(result);
			    }
			}
			
			super.onPostExecute(result);
		}
		
		private void refreshListItems(List<Trip> result){
		    setListItems(result);
            if (result != null && result.size() > 0) {
                setAdapter(new TripListAdapter(getContext(), result));
                initGenericList();
                setStatus(GenericListDialog.Status.GenericList);
            }
            else {
                initEmptyList();
                setStatus(GenericListDialog.Status.EmptyList);
            }
            listViewGeneric.onRefreshComplete();
		}
		
	}

	private class TripListAdapter extends ArrayAdapter<Trip> {
		
		public TripListAdapter(Context context,	List<Trip> objects) {
			super(context, R.layout.trip_list_item, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			
			if (view == null) {
				LayoutInflater inflater = getLayoutInflater();
				view = inflater.inflate(R.layout.trip_list_item, parent, false);
			}
			
			Trip trip = getItem(position);
			
			TextView textView3 = (TextView) view.findViewById(R.id.text_view_name);
			textView3.setText(trip.getName());
			
			TextView textView1 = (TextView) view.findViewById(R.id.text_view_origin);
			textView1.setText(trip.getOrigin());
			
			TextView textView2 = (TextView) view.findViewById(R.id.text_view_destination);
			textView2.setText(trip.getDestination());
			
			Font.setTypeface(boldFont, textView3, 
		        (TextView)view.findViewById(R.id.text_view_origin_label),
		        (TextView)view.findViewById(R.id.text_view_destination_label)
	        );
			
			Font.setTypeface(lightFont, textView1, textView2);
			
			return view;
		}
	}
}