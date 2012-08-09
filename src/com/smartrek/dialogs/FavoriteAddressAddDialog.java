package com.smartrek.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.smartrek.activities.R;

/**
 * The purpose of this dialog is to provide an interface to add a favorite
 * address.
 * 
 * @author Sumin Byeon
 * 
 */
public class FavoriteAddressAddDialog extends AlertDialog {
	
	private OnClickListener listener;
	private String address;
	private ViewGroup dialogView;
	private EditText editTextName;
	private EditText editTextAddress;
	private ProgressBar progressBar;
	
	public FavoriteAddressAddDialog(Context context, String address) {
		super(context);
		this.address = address;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.favorite_address_add, null);
		
		editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
		editTextAddress = (EditText) dialogView.findViewById(R.id.editTextAddress);
		editTextAddress.setText(address);
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
	
	private void onClickPositiveButton() {
		editTextName.setEnabled(false);
		editTextAddress.setEnabled(false);
		progressBar.setVisibility(View.VISIBLE);
	}
}
