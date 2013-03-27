package com.smartrek.dialogs;

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
import com.smartrek.activities.R;
import com.smartrek.models.Address;
import com.smartrek.models.Trip;
import com.smartrek.models.User;
import com.smartrek.requests.TripDeleteRequest;
import com.smartrek.requests.TripListFetchRequest;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;

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
	        	new TripDeleteTask(menuItemIndex).execute(listItem.getId());
	            return true;
	            
	        default:
	            return super.onMenuItemSelected(featureId, menuItem);
	    }
	}
	
	private void requestRefresh() {
		User currentUser = User.getCurrentUser(getContext());
		new TripListFetchRequest(currentUser.getId()).invalidateCache();
		new TripListFetchTask().execute(currentUser.getId());
	}
	
	private void showTripEditDialog(Trip trip) {
		TripEditDialog dialog = new TripEditDialog(getContext(), trip);
		dialog.setActionListener(new TripEditDialog.ActionListener() {
			
			@Override
			public void onClickPositiveButton(String name, Address origin, Address destination) {
				requestRefresh();
			}
			
			@Override
			public void onClickNegativeButton() {
			}
		});
		dialog.show();
	}
	
	private class TripDeleteTask extends AsyncTask<Object, Object, Object> {

		private int listItemIndex;
		
		public TripDeleteTask(int listItemIndex) {
			this.listItemIndex = listItemIndex;
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			int fid = (Integer) params[0];
			
			TripDeleteRequest request = new TripDeleteRequest(fid);
			try {
				request.execute();
				
				// clear cache
				new TripListFetchRequest(User.getCurrentUser(getContext()).getId()).invalidateCache();
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
			int uid = (Integer) params[0];
			
			TripListFetchRequest request = new TripListFetchRequest(uid);
			try {
			    //request.invalidateCache();
				listItems = request.execute();
			}
			catch (Exception e) {
				ehs.registerException(e);
			}

			return listItems;
		}
		
		@Override
		protected void onPostExecute(List<Trip> result) {
			if (ehs.hasExceptions()) {
				ehs.reportExceptions();
			}
			else {
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
			}
			listViewGeneric.onRefreshComplete();
			
			super.onPostExecute(result);
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
