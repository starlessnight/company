package com.smartrek.dialogs;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
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
import com.smartrek.models.User;
import com.smartrek.requests.FavoriteAddressDeleteRequest;
import com.smartrek.requests.FavoriteAddressFetchRequest;
import com.smartrek.utils.ExceptionHandlingService;

public class FavoriteAddressListDialog extends GenericListDialog<Address> {
	
	private ExceptionHandlingService ehs = new ExceptionHandlingService(getContext());
	
	public interface ActionListener extends GenericListDialog.ActionListener<Address> {}

	public FavoriteAddressListDialog(Context context) {
		super(context, null);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Resources res = getContext().getResources();
		
		setTitle("Favorite addresses");
		setButton(DialogInterface.BUTTON_NEUTRAL, res.getString(R.string.add), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (actionListener != null) {
					actionListener.onClickNeutralButton();
				}
			}
			
		});
		
		super.onCreate(savedInstanceState);
		textViewGeneric.setText("You don't have any favorite address");
		
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
	    
	    Address listItem = listItems.get(menuItemIndex);
	    
	    switch (menuItem.getItemId()) {
	    	case R.id.edit:
	    		showAddressEditDialog(listItem);
	    		return true;
	    	
	        case R.id.delete:
	        	new FavoriteAddressDeleteTask(menuItemIndex).execute(listItem.getUid(), listItem.getId());
	            return true;
	            
	        default:
	            return super.onMenuItemSelected(featureId, menuItem);
	    }
	}
	
	private void requestRefresh() {
		User currentUser = User.getCurrentUser(getContext());
		
		// Invalidate cache
		new FavoriteAddressFetchRequest(currentUser.getId()).invalidateCache();
		
        new FavoriteAddressListFetchTask().execute(currentUser.getId());
	}
	
	private void showAddressEditDialog(Address address) {
		FavoriteAddressEditDialog dialog = new FavoriteAddressEditDialog(getContext(), address);
		dialog.setActionListener(new FavoriteAddressEditDialog.ActionListener() {
			
			@Override
			public void onClickPositiveButton() {
				requestRefresh();
			}
			
			@Override
			public void onClickNegativeButton() {
			}
		});
		dialog.show();
	}
	
	private class FavoriteAddressDeleteTask extends AsyncTask<Object, Object, Object> {

		private int listItemIndex;
		
		public FavoriteAddressDeleteTask(int listItemIndex) {
			this.listItemIndex = listItemIndex;
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			int uid = (Integer) params[0];
			int aid = (Integer) params[1];
			
			FavoriteAddressDeleteRequest request = new FavoriteAddressDeleteRequest(uid, aid);
			try {
				request.execute();
				
				// clear cache
				new FavoriteAddressFetchRequest(uid).invalidateCache();
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
		    	listViewGeneric.setAdapter(new FavoriteAddressListAdapter(getContext(), listItems));
		    	listViewGeneric.postInvalidate();
		    }
		}
	}
	
	private class FavoriteAddressListFetchTask extends AsyncTask<Object, Object, List<Address>> {
		
		private List<Address> favoriteAddresses;
		
		@Override
		protected List<Address> doInBackground(Object... params) {

			int uid = (Integer) params[0];

			FavoriteAddressFetchRequest request = new FavoriteAddressFetchRequest(uid);
			try {
			    request.invalidateCache();
				favoriteAddresses = request.execute();
			}
			catch (Exception e) {
				ehs.registerException(e);
			}

			return favoriteAddresses;
		}
		
		@Override
		protected void onPostExecute(List<Address> result) {
		    listViewGeneric.onRefreshComplete();
		    
			if (ehs.hasExceptions()) {
				ehs.reportExceptions();
			}
			else {
				setListItems(result);
				if (result != null && result.size() > 0) {
					setAdapter(new FavoriteAddressListAdapter(getContext(), result));
					initGenericList();
					setStatus(GenericListDialog.Status.GenericList);
				}
				else {
					initEmptyList();
					setStatus(GenericListDialog.Status.EmptyList);
				}
			}
		}
	}

	private class FavoriteAddressListAdapter extends ArrayAdapter<Address> {
		
		private LayoutInflater inflater;
		
		public FavoriteAddressListAdapter(Context context, List<Address> items) {
			super(context, R.layout.favorite_address_list, items);
			
			if(items != null) {
				notifyDataSetChanged();
			}
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if(view == null) {
				view = inflater.inflate(R.layout.favorite_address_list_item, parent, false);
			}
			Address item = getItem(position);
			
			TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
			textViewName.setText(item.getName());
			
			TextView textViewAddress = (TextView) view.findViewById(R.id.textViewAddress);
			textViewAddress.setText(item.getAddress());
			
			return view;
		}

	}
}
