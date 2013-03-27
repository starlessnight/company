package com.smartrek.dialogs;

import java.io.IOException;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.smartrek.utils.Font;
import com.smartrek.utils.GeoPoint;

/**
 * The purpose of this dialog is to provide an interface to add a favorite
 * address.
 * 
 * @author Sumin Byeon
 * 
 */
public class FavoriteAddressEditDialog extends Dialog implements TextWatcher {
	
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
		this(context, null);
	}
	
	/**
	 * When an instance of Address is given, this dialog will trigger an edit mode.
	 * 
	 * @param context
	 * @param address
	 */
	public FavoriteAddressEditDialog(Context context, Address address) {
		super(context, R.style.PopUpDialog);
		this.address = address;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.favorite_address_add, null);
		
		boolean isAdd = address == null || address.getId() == 0;
		
		TextView titleView = (TextView) dialogView.findViewById(R.id.title);
		titleView.setText((isAdd?"Add New":"Edit") + " Location");
		
		editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
		editTextName.addTextChangedListener(this);
		editTextName.setText(address.getName());
		
		editTextAddress = (EditText) dialogView.findViewById(R.id.editTextAddress);
		editTextAddress.addTextChangedListener(this);
		editTextAddress.setText(address.getAddress());
		
		progressBar = (ProgressBar) dialogView.findViewById(R.id.progressBar);
		
		setContentView(dialogView);
		
		Button confirmButton = (Button) dialogView.findViewById(R.id.confirm_button);
		confirmButton.setEnabled(!isAdd);
		confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPositiveButton();
            }
        });
		
		View closeIcon = dialogView.findViewById(R.id.close_icon);
		closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onClickNegativeButton();
                }
            }
        });
		
		AssetManager assets = getContext().getAssets();
		Font.setTypeface(Font.getBold(assets), titleView, confirmButton);
		Font.setTypeface(Font.getLight(assets), editTextName, editTextAddress);
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
		
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
	
    @Override
    public void afterTextChanged(Editable s) {
        
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Button btn = (Button) dialogView.findViewById(R.id.confirm_button);
        btn.setEnabled(editTextName.getText().length() > 0 && editTextName.getText().length() > 0);
    }
	
}
