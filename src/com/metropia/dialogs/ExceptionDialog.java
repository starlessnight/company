package com.metropia.dialogs;

import com.metropia.activities.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

public class ExceptionDialog extends AlertDialog {
	
	private String message;

	public ExceptionDialog(Context context, String message) {
		super(context, R.style.PopUpDialog);
		this.message = message;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTitle("An error has occurred");
		setMessage(message);
		
		super.onCreate(savedInstanceState);
	}
}
