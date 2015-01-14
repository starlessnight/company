package com.metropia.dialogs;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
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

import com.metropia.models.Address;
import com.metropia.models.User;
import com.metropia.requests.AddressLinkRequest;
import com.metropia.requests.FavoriteAddressAddRequest;
import com.metropia.requests.FavoriteAddressFetchRequest;
import com.metropia.requests.FavoriteAddressUpdateRequest;
import com.metropia.tasks.GeocodingTask;
import com.metropia.tasks.GeocodingTaskCallback;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.GeoPoint;
import com.metropia.activities.R;

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
	private boolean fixedName;
	
	
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
        this(context, address, false);
    }
	
	public FavoriteAddressEditDialog(Context context, Address address, boolean fixedName) {
		super(context, R.style.PopUpDialog);
		this.address = address;
		this.fixedName = fixedName;
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
		editTextName.setEnabled(!fixedName);
		
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
	
	public String getAddress() {
		return editTextAddress.getText().toString().trim();
	}
	
	public void setAddress(Address address) {
		this.address = address;
	}
	
	public Address getAddressObject(){
	    return address;
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
		
		new GeocodingTask(getContext(), new GeoPoint(0, 0), ehs, new GeocodingTaskCallback() {
            @Override
            public void preCallback() {
                address.setLatitude(0);
                address.setLongitude(0);
            }
            @Override
            public void postCallback() {
                if (isEditMode()) {
                    new FavoriteAddressUpdateTask(address).execute();
                }
                else {
                    User currentUser = User.getCurrentUser(getContext());
                    new FavoriteAddressAddTask(getContext()).execute(currentUser, 
                        getName(), getAddress(), address.getLatitude(), 
                        address.getLongitude());
                }
            }
            @Override
            public void callback(List<com.metropia.utils.Geocoding.Address> addresses) {
                GeoPoint geoPoint = addresses.get(0).getGeoPoint();
                address.setLatitude(geoPoint.getLatitude());
                address.setLongitude(geoPoint.getLongitude());
            }
        }, false).execute(address.getAddress());
	}
	
	private class FavoriteAddressAddTask extends AsyncTask<Object, Object, Object> {

	    private Context ctx;
	    
	    public FavoriteAddressAddTask(Context ctx) {
	        this.ctx = ctx;
        }
	    
		@Override
		protected Object doInBackground(Object... params) {
			User user = (User) params[0];
			String name = (String) params[1];
			String address = (String) params[2];
			double lat = (Double) params[3];
			double lon = (Double) params[4];
			
			FavoriteAddressAddRequest request = new FavoriteAddressAddRequest(user, name, address, "star", lat, lon);
			try {
				request.execute(ctx);
				
				// clear cache
				new FavoriteAddressFetchRequest(user).invalidateCache(getContext());
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
				dismiss();
				
				Toast toast = Toast.makeText(
						getContext(),
						String.format("Address '%s' has been added.", address.getName()),
						Toast.LENGTH_SHORT);
				toast.show();
				
				if (listener != null) {
	                listener.onClickPositiveButton();
	            }
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
		    User user = User.getCurrentUser(getContext());
			try {
				FavoriteAddressUpdateRequest request = new FavoriteAddressUpdateRequest(
				        new AddressLinkRequest(user).execute(getContext()),
						address.getId(),
						user,
						address.getName(),
						address.getAddress(),
						address.getIconName(),
						address.getLatitude(),
						address.getLongitude());
				request.execute(getContext());
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
		    
		    if (listener != null) {
	            listener.onClickPositiveButton();
	        }
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
        if(editTextName != null && editTextAddress != null){
            Button btn = (Button) dialogView.findViewById(R.id.confirm_button);
            btn.setEnabled(StringUtils.isNotBlank(editTextName.getText()) && StringUtils.isNotBlank(editTextAddress.getText()));
        }
    }
	
}