package com.smartrek.dialogs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;

import com.smartrek.activities.R;
import com.smartrek.models.Address;
import com.smartrek.models.User;
import com.smartrek.requests.TripAddRequest;
import com.smartrek.requests.TripListFetchRequest;
import com.smartrek.utils.ExceptionHandlingService;

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
	
	private Address origin;
	private Address destination;
	
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
		
		setView(dialogView);
		setTitle("Save Trip");
		
		setButton(DialogInterface.BUTTON_POSITIVE, "Add", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (actionListener != null) {
					actionListener.onClickPositiveButton(getName(), getOrigin(), getDestination());
				}
				
				User currentUser = User.getCurrentUser(getContext());
				new TripSaveTask(getContext(), currentUser.getId(), getName(), getOrigin(), getDestination()).execute();
			}
		});
		
		setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
			
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
	
	private class TripSaveTask extends AsyncTask<Object, Object, Object> {

		private ProgressDialog progressDialog;
		
		private Context context;
		private int uid;
		private String name;
		private Address origin;
		private Address destination;
		
		private ExceptionHandlingService ehs;
		
		public TripSaveTask(Context context, int uid, String name, Address origin, Address destination) {
			this.context = context;
			this.uid = uid;
			this.name = name;
			this.origin = origin;
			this.destination = destination;
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
			TripAddRequest request = new TripAddRequest(uid, name, origin.getId(), destination.getId());
			try {
				request.execute();
				
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
