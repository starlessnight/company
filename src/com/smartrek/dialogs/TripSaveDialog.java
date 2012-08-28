package com.smartrek.dialogs;

import java.io.IOException;

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
import com.smartrek.models.User;
import com.smartrek.requests.TripAddRequest;
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
		void onClickPositiveButton(String name, String origin, String destination);
		void onClickNegativeButton();
	}
	
	private ActionListener actionListener;
	private ViewGroup dialogView;
	private EditText editTextName;
	private EditText editTextOrigin;
	private EditText editTextDestination;
	
	private String origin;
	private String destination;
	
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
	public TripSaveDialog(Context context, String origin, String destination) {
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
			editTextOrigin.setText(origin);
		}
		if (destination != null) {
			editTextDestination.setText(destination);
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
	
	private String getOrigin() {
		return editTextOrigin.getText().toString().trim();
	}
	
	private String getDestination() {
		return editTextDestination.getText().toString().trim();
	}
	
	private class TripSaveTask extends AsyncTask<Object, Object, Object> {

		private ProgressDialog progressDialog;
		
		private Context context;
		private int uid;
		private String name;
		private String origin;
		private String destination;
		
		private ExceptionHandlingService ehs;
		
		public TripSaveTask(Context context, int uid, String name, String origin, String destination) {
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
			TripAddRequest request = new TripAddRequest(uid, name, origin, destination);
			try {
				request.execute();
			}
			catch (IOException e) {
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
