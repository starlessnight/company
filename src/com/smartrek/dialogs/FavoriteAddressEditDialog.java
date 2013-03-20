package com.smartrek.dialogs;

import java.io.IOException;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.smartrek.activities.R;
import com.smartrek.models.Address;
import com.smartrek.models.User;
import com.smartrek.requests.FavoriteAddressAddRequest;
import com.smartrek.requests.FavoriteAddressFetchRequest;
import com.smartrek.requests.FavoriteAddressUpdateRequest;
import com.smartrek.tasks.GeocodingTask;
import com.smartrek.tasks.GeocodingTaskCallback;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.GeoPoint;

/**
 * The purpose of this dialog is to provide an interface to add a favorite
 * address.
 * 
 * @author Sumin Byeon
 * 
 */
public class FavoriteAddressEditDialog extends AlertDialog {
	
	public interface ActionListener {
		void onClickPositiveButton();
		void onClickNegativeButton();
	}
	
	private ExceptionHandlingService ehs = new ExceptionHandlingService(getContext());
	
	private ActionListener listener;
	private Address address;
	private ViewGroup dialogView;
	private EditText editTextName;
	private EditText editTextAddress;
	private ProgressBar progressBar;
	
	public FavoriteAddressEditDialog(Context context) {
		super(context);
	}
	
	/**
	 * When an instance of Address is given, this dialog will trigger an edit mode.
	 * 
	 * @param context
	 * @param address
	 */
	public FavoriteAddressEditDialog(Context context, Address address) {
		super(context);
		this.address = address;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.favorite_address_add, null);
		
		editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
		editTextName.setText(address.getName());
		
		editTextAddress = (EditText) dialogView.findViewById(R.id.editTextAddress);
		editTextAddress.setText(address.getAddress());
		
		progressBar = (ProgressBar) dialogView.findViewById(R.id.progressBar);
		
		Resources res = getContext().getResources();
		
		setView(dialogView);
		setTitle("Would you like to add as favorite address?");
		
		setButton(DialogInterface.BUTTON_POSITIVE,
				res.getString(isEditMode() ? R.string.ok : R.string.add),
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
			
		});
		
		setButton(DialogInterface.BUTTON_NEGATIVE, res.getString(R.string.cancel), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.onClickNegativeButton();
				}
			}
			
		});
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
		
		// Replace the default onClickListener to prevent this dialog closing.
		Button positiveButton = getButton(DialogInterface.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickPositiveButton();
			}
			
		});
	}
	
	private String getName() {
		return editTextName.getText().toString().trim();
	}
	
	private String getAddress() {
		return editTextAddress.getText().toString().trim();
	}
	
	public void setAddress(Address address) {
		this.address = address;
	}
	
	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}
	
	public boolean isEditMode() {
		return address != null && address.getId() != 0;
	}
	
	private void onClickPositiveButton() {
		editTextName.setEnabled(false);
		editTextAddress.setEnabled(false);
		progressBar.setVisibility(View.VISIBLE);
		
		address.setName(getName());
		address.setAddress(getAddress());
		
		new GeocodingTask(ehs, new GeocodingTaskCallback() {
            @Override
            public void preCallback() {}
            @Override
            public void postCallback() {
                if (isEditMode()) {
                    new FavoriteAddressUpdateTask(address).execute();
                }
                else {
                    User currentUser = User.getCurrentUser(getContext());
                    new FavoriteAddressAddTask().execute(currentUser.getId(), 
                        getName(), getAddress(), address.getLatitude(), 
                        address.getLongitude());
                }
            }
            @Override
            public void callback(List<com.smartrek.utils.Geocoding.Address> addresses) {
                GeoPoint geoPoint = addresses.get(0).getGeoPoint();
                address.setLatitude(geoPoint.getLatitude());
                address.setLongitude(geoPoint.getLongitude());
            }
        }, false).execute(address.getAddress());
		
		if (listener != null) {
			listener.onClickPositiveButton();
		}
	}
	
	private class FavoriteAddressAddTask extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			int uid = (Integer) params[0];
			String name = (String) params[1];
			String address = (String) params[2];
			double lat = (Double) params[3];
			double lon = (Double) params[4];
			
			FavoriteAddressAddRequest request = new FavoriteAddressAddRequest(uid, name, address, lat, lon);
			try {
				request.execute();
				
				// clear cache
				new FavoriteAddressFetchRequest(uid).invalidateCache();
			}
			catch (IOException e) {
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
				dismiss();
				
				Toast toast = Toast.makeText(
						getContext(),
						String.format("Address '%s' has been added.", address.getName()),
						Toast.LENGTH_SHORT);
				toast.show();
			}
		}
		
	}
	
	private class FavoriteAddressUpdateTask extends AsyncTask<Object, Object, Object> {
		
		private Address address;
		
		private boolean showMsg;
		
		public FavoriteAddressUpdateTask(Address address, boolean showMsg) {
			this.address = address;
			this.showMsg = showMsg;
		}
		
		public FavoriteAddressUpdateTask(Address address) {
            this(address, true);
        }
		
		@Override
		protected Object doInBackground(Object... params) {
			try {
				FavoriteAddressUpdateRequest request = new FavoriteAddressUpdateRequest(
						address.getId(),
						address.getUid(),
						address.getName(),
						address.getAddress(),
						address.getLatitude(),
						address.getLongitude());
				request.execute();
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
		    else if (showMsg){
				Toast toast = Toast.makeText(
						getContext(),
						String.format("Address '%s' has been updated.", address.getName()),
						Toast.LENGTH_SHORT);
				toast.show();
		    }
		    
		    dismiss();
		}
	}
}
