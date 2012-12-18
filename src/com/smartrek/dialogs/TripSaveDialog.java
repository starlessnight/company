package com.smartrek.dialogs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.smartrek.activities.R;
import com.smartrek.models.Address;
import com.smartrek.models.Trip;
import com.smartrek.models.User;
import com.smartrek.requests.TripAddRequest;
import com.smartrek.requests.TripListFetchRequest;
import com.smartrek.requests.TripUpdateRequest;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.datetime.RecurringTime;

public final class TripSaveDialog extends AlertDialog {
	
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
	public TripSaveDialog(Context context, Address origin, Address destination) {
		super(context);
		this.origin = origin;
		this.destination = destination;
	}
	
	/**
	 * When an instance of Trip is given, this dialog will triger an edit mode.
	 * 
	 * @param context
	 * @param trip
	 */
	public TripSaveDialog(Context context, Trip trip) {
		super(context);
		this.trip = trip;

		// FIXME: I think Trip should have a userID field.
		int uid = User.getCurrentUser(context).getId();
		this.origin = new Address(trip.getOriginID(), uid, "", trip.getOrigin());
		this.destination = new Address(trip.getDestinationID(), uid, "", trip.getDestination());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.trip_save, null);
		
		editTextName = (EditText) dialogView.findViewById(R.id.edit_text_name);
		editTextOrigin = (EditText) dialogView.findViewById(R.id.edit_text_origin);
		editTextDestination = (EditText) dialogView.findViewById(R.id.edit_text_destination);
		
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
		}
		
		ViewGroup layoutSetReminder = (ViewGroup) dialogView.findViewById(R.id.layout_set_reminder);
		layoutSetReminder.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				openSetReminderDialog();
			}
			
		});
		
		Resources res = getContext().getResources();
		
		setView(dialogView);
		setIcon(res.getDrawable(R.drawable.save_trip));
		setTitle(isEditMode() ? res.getString(R.string.edit_trip) : res.getString(R.string.save_trip));
		
		setButton(DialogInterface.BUTTON_POSITIVE,
				isEditMode() ? res.getString(R.string.ok) : res.getString(R.string.add),
				new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (actionListener != null) {
					actionListener.onClickPositiveButton(getName(), getOrigin(), getDestination());
				}
				
				User currentUser = User.getCurrentUser(getContext());
				if (isEditMode()) {
					new TripSaveTask(getContext(), trip.getId(), currentUser.getId(), getName(), getOrigin(), getDestination(), new RecurringTime((byte)hour, (byte)minute, weekdays)).execute();
				}
				else {
					// TODO: Should I pass 'trip'?
					new TripSaveTask(getContext(), 0, currentUser.getId(), getName(), getOrigin(), getDestination(), new RecurringTime((byte)hour, (byte)minute, weekdays)).execute();
				}
			}
		});
		
		setButton(DialogInterface.BUTTON_NEGATIVE, res.getString(R.string.cancel), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (actionListener != null) {
					actionListener.onClickNegativeButton();
				}
			}
		});
		
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
	
	/**
	 * Opens up a dialog to set a trip reminder.
	 */
	private void openSetReminderDialog() {
		SetReminderDialog dialog = new SetReminderDialog(getContext());
		dialog.setActionListener(new SetReminderDialog.ActionListener() {
			
			@Override
			public void onClickPositiveButton(int hour, int minute, byte weekdays) {
				TripSaveDialog.this.hour = hour;
				TripSaveDialog.this.minute = minute;
				TripSaveDialog.this.weekdays = weekdays;
			}
			
			@Override
			public void onClickNegativeButton() {
			}
		});
		dialog.show();
	}
	
	private class TripSaveTask extends AsyncTask<Object, Object, Object> {

		private ProgressDialog progressDialog;
		
		private Context context;
		
		/**
		 * Trip ID
		 */
		private int tid;
		
		/**
		 * User ID
		 */
		private int uid;
		
		private String name;
		private Address origin;
		private Address destination;
		private RecurringTime recurringTime;
		
		private ExceptionHandlingService ehs;
		
		public TripSaveTask(Context context, int tid, int uid, String name, Address origin, Address destination, RecurringTime recurringTime) {
			this.context = context;
			this.tid = tid;
			this.uid = uid;
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
			progressDialog.show();
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			try {
				if (tid == 0) {
					TripAddRequest request = new TripAddRequest(uid, name, origin.getId(), destination.getId(), recurringTime);
					request.execute();
				}
				else {
					TripUpdateRequest request = new TripUpdateRequest(tid, uid, name, origin.getId(), destination.getId(), recurringTime);
					request.execute();
				}
				
				new TripListFetchRequest(uid).invalidateCache();
			}
			catch (Exception e) {
				ehs.registerException(e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			progressDialog.cancel();
			
		    if (ehs.hasExceptions()) {
		        ehs.reportExceptions();
		    }
		    else {
		    	String message = (name != null && !name.equals("")) ?
		    			String.format("Trip '%s' has been saved.", name) : "Trip has been saved";
		    	NotificationDialog notificationDialog = new NotificationDialog(getContext(), message);
		    	notificationDialog.show();
		    }
		}
	}
}
