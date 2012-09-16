package com.smartrek.dialogs;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.smartrek.activities.R;
import com.smartrek.models.Address;
import com.smartrek.models.User;
import com.smartrek.requests.FavoriteAddressAddRequest;
import com.smartrek.requests.FavoriteAddressFetchRequest;
import com.smartrek.utils.ExceptionHandlingService;

/**
 * The purpose of this dialog is to provide an interface to add a favorite
 * address.
 * 
 * @author Sumin Byeon
 * 
 */
public class FavoriteAddressAddDialog extends AlertDialog {
	
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
	
	public FavoriteAddressAddDialog(Context context) {
		super(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.favorite_address_add, null);
		
		editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
		editTextAddress = (EditText) dialogView.findViewById(R.id.editTextAddress);
		editTextAddress.setText(address.getAddress());
		progressBar = (ProgressBar) dialogView.findViewById(R.id.progressBar);
		
		setView(dialogView);
		setTitle("Would you like to add as favorite address?");
		
		setButton(DialogInterface.BUTTON_POSITIVE, "Add", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
			
		});
		
		setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

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
	
	private void onClickPositiveButton() {
		editTextName.setEnabled(false);
		editTextAddress.setEnabled(false);
		progressBar.setVisibility(View.VISIBLE);
		
		User currentUser = User.getCurrentUser(getContext());
		new FavoriteAddressAddTask().execute(currentUser.getId(), getName(), getAddress());
		
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
			
			FavoriteAddressAddRequest request = new FavoriteAddressAddRequest(uid, name, address);
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
			}
		}
		
	}
}
