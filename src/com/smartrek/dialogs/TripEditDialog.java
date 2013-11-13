package com.smartrek.dialogs;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.smartrek.activities.R;
import com.smartrek.models.Address;
import com.smartrek.models.Trip;
import com.smartrek.models.User;
import com.smartrek.requests.FavoriteAddressAddRequest;
import com.smartrek.requests.FavoriteAddressFetchRequest;
import com.smartrek.requests.TripAddRequest;
import com.smartrek.requests.TripLinkRequest;
import com.smartrek.requests.TripListFetchRequest;
import com.smartrek.requests.TripUpdateRequest;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.datetime.RecurringTime;

public final class TripEditDialog extends Dialog implements TextWatcher {
	
	/**
	 * Dialog action listener
	 *
	 */
	public interface ActionListener {
		/**
		 * 
		 * @param name Name of this trip
		 * @param origin A postal address of origin
		 * @param destination A postal address of destination
		 */
		void onClickPositiveButton(String name, Address origin, Address destination);
		void onClickNegativeButton();
	}
	
	private ActionListener actionListener;
	private ViewGroup dialogView;
	private EditText editTextName;
	private EditText editTextOrigin;
	private EditText editTextDestination;
	
	/**
	 * Unsaved origin address
	 */
	private Address origin;
	
	/**
	 * Unsaved destination address
	 */
	private Address destination;
	
	/**
	 * Saved trip. Will be used to pre-populate necessary fields.
	 */
	private Trip trip;
	
	private int hour;
	
	private int minute;
	
	private byte weekdays;
	
	private RecurringTime tmpTime;
	
	/**
	 * I decided to force the caller to set the origin and the destination
	 * addresses using this constructor, instead of providing setters, because
	 * it is difficult to guarantee that EditText instances are non-null when
	 * setters are called.
	 * 
	 * @param context
	 * @param origin
	 * @param destination
	 */
	public TripEditDialog(Context context, Address origin, Address destination) {
		super(context, R.style.PopUpDialog);
		this.origin = origin;
		this.destination = destination;
	}
	
	/**
	 * When an instance of Trip is given, this dialog will trigger an edit mode.
	 * 
	 * @param context
	 * @param trip
	 */
	public TripEditDialog(Context context, Trip trip) {
		super(context, R.style.PopUpDialog);
		this.trip = trip;

		if(trip != null){
		    tmpTime = trip.getRecurringTime();
		    if(tmpTime.getHour() == 0){
		        tmpTime.setHour(24);
		    }
		}
		
		// FIXME: I think Trip should have a userID field.
		int uid = User.getCurrentUser(context).getId();
		this.origin = new Address(trip.getOriginID(), uid, "", trip.getOrigin(), 0, 0);
		this.destination = new Address(trip.getDestinationID(), uid, "", trip.getDestination(), 0, 0);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.trip_save, null);
		
		TextView titleView = (TextView) dialogView.findViewById(R.id.title);
        titleView.setText((isEditMode()?"Edit":"Save") + " Trip");
		
		editTextName = (EditText) dialogView.findViewById(R.id.edit_text_name);
		editTextName.addTextChangedListener(this);
		editTextOrigin = (EditText) dialogView.findViewById(R.id.edit_text_origin);
		editTextDestination = (EditText) dialogView.findViewById(R.id.edit_text_destination);
		Button saveButton = (Button) dialogView.findViewById(R.id.save_button);
		
		if (origin != null) {
			editTextOrigin.setText(origin.getAddress());
		}
		if (destination != null) {
			editTextDestination.setText(destination.getAddress());
		}
		
		if (trip != null) {
			editTextName.setText(trip.getName());
			editTextOrigin.setText(trip.getOrigin());
			editTextDestination.setText(trip.getDestination());
		}else if (origin != null && origin.getId() > 0 
		        && destination != null && destination.getId() > 0){
		    editTextName.setText(origin.getName() + " to " + destination.getName());
		}else{
		    saveButton.setEnabled(false);
		}
		
		Button layoutSetReminder = (Button) dialogView.findViewById(R.id.layout_set_reminder);
		layoutSetReminder.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				openSetReminderDialog();
			}
			
		});
		
		saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = getName();
                if(name.length() == 0){
                    Toast.makeText(getContext(), R.string.enter_trip_name, Toast.LENGTH_LONG).show();
                }else{
                    User currentUser = User.getCurrentUser(getContext());
                    if (isEditMode()) {
                        new TripSaveTask(getContext(), trip.getId(), currentUser, name, getOrigin(), getDestination(), new RecurringTime((byte)hour, (byte)minute, (byte)0, weekdays)).execute();
                    }
                    else {
                        new TripSaveTask(getContext(), 0, currentUser, name, getOrigin(), getDestination(), new RecurringTime((byte)hour, (byte)minute, (byte)0, weekdays)).execute();
                    }
                }
            }
        });
		
		View closeIcon = dialogView.findViewById(R.id.close_icon);
        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (actionListener != null) {
                    actionListener.onClickNegativeButton();
                }
            }
        });
        
        AssetManager assets = getContext().getAssets();
        Font.setTypeface(Font.getBold(assets), titleView, saveButton, layoutSetReminder);
        Font.setTypeface(Font.getLight(assets), editTextName, editTextOrigin, editTextDestination);
		
		setContentView(dialogView);
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);

	}
	
	public void setActionListener(ActionListener listener) {
		this.actionListener = listener;
	}
	
	private String getName() {
		return editTextName.getText().toString().trim();
	}
	
	private Address getOrigin() {
		//return new Address(0, User.getCurrentUser(getContext()).getId(), "", editTextOrigin.getText().toString().trim());
		return origin;
	}
	
	private Address getDestination() {
		//return new Address(0, User.getCurrentUser(getContext()).getId(), "", editTextDestination.getText().toString().trim());
		return destination;
	}
	
	public boolean isEditMode() {
		return trip != null;
	}
	
	private SetReminderDialog dialog;
	
	/**
	 * Opens up a dialog to set a trip reminder.
	 */
	private void openSetReminderDialog() {
		dialog = new SetReminderDialog(getContext(), tmpTime);
		dialog.setActionListener(new SetReminderDialog.ActionListener() {
			
			@Override
			public void onClickPositiveButton(RecurringTime recurringTime) {
				TripEditDialog.this.hour = recurringTime.getHour();
				TripEditDialog.this.minute = recurringTime.getMinute();
				TripEditDialog.this.weekdays = recurringTime.getWeekdays();
				tmpTime = recurringTime;
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
	
    @Override
    public void afterTextChanged(Editable s) {
        
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(editTextName != null){
            Button btn = (Button) dialogView.findViewById(R.id.save_button);
            btn.setEnabled(StringUtils.isNotBlank(editTextName.getText()));
        }
    }
	
	private class TripSaveTask extends AsyncTask<Object, Object, String> {

		private ProgressDialog progressDialog;
		
		private Context context;
		
		/**
		 * Trip ID
		 */
		private int tid;
		
		private User user;
		
		private String name;
		private Address origin;
		private Address destination;
		private RecurringTime recurringTime;
		
		private ExceptionHandlingService ehs;
		
		public TripSaveTask(Context context, int tid, User user, String name, Address origin, Address destination, RecurringTime recurringTime) {
			this.context = context;
			this.tid = tid;
			this.user = user;
			this.name = name;
			this.origin = origin;
			this.destination = destination;
			this.recurringTime = recurringTime;
			this.ehs = new ExceptionHandlingService(context);
		}
		
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Saving trip...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
		}
		
		@Override
		protected String doInBackground(Object... params) {
			try {
				if (tid == 0) {
				    FavoriteAddressFetchRequest listReq = new FavoriteAddressFetchRequest(user);
				    listReq.invalidateCache(getContext());
				    List<Address> addrList = listReq.execute(getContext());
				    int originId = 0;
				    int destId = 0;
				    for(Address addr : addrList){
				        int id = addr.getId();
				        String addrStr = addr.getAddress().trim();
				        if(addrStr.equals(origin.getAddress().trim())){
				            originId = id;
				        }
				        if(addrStr.equals(destination.getAddress().trim())){
				            destId = id;
                        }
				    }
				    
				    if(originId > 0 && destId > 0){
				        TripListFetchRequest tripListReq = new TripListFetchRequest(user);
				        tripListReq.invalidateCache(getContext());
				        for (Trip trip : tripListReq.execute(getContext())) {
                            if(trip.getOriginID() == originId && trip.getDestinationID() == destId){
                                return "Trip already in list";
                            }
                        }
				    }
				    
				    if(originId == 0){
				        String addr = origin.getAddress();
				        originId = new FavoriteAddressAddRequest(user, truncateName(addr), addr, 0, 0).execute(context);
				    }
				    
				    if(destId == 0){
				        String addr = destination.getAddress();
				        destId = new FavoriteAddressAddRequest(user, truncateName(addr), addr, 0, 0).execute(context);
				    }
				    
			        TripAddRequest request = new TripAddRequest(user, name, originId, destId, recurringTime);
                    request.execute(context);
				}
				else {
					TripUpdateRequest request = new TripUpdateRequest(
				        new TripLinkRequest(user).execute(getContext()), tid, 
				        user, name, origin.getId(), destination.getId(), recurringTime);
					request.execute(context);
				}
				
				new TripListFetchRequest(user).invalidateCache(getContext());
			}
			catch (Exception e) {
				ehs.registerException(e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			progressDialog.cancel();
			
			String message = null;
			
		    if (ehs.hasExceptions()) {
		        ehs.reportExceptions();
		    }
		    else if (result != null){
		        message = result;
		    }else {
		    	message = (name != null && !name.equals("")) ?
		    			String.format("Trip '%s' has been saved.", name) : "Trip has been saved.";
		    }
		    
		    if(message != null){
		        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
                toast.show();
		    }
		    
		    dismiss();
		    
		    if (actionListener != null) {
                actionListener.onClickPositiveButton(name, getOrigin(), getDestination());
            }
		}
		
		private String truncateName(String addr){
		    return StringUtils.abbreviate(addr, 30);
		}
	}
}
