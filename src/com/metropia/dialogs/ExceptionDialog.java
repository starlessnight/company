package com.metropia.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

public class ExceptionDialog extends AlertDialog {
	
	private String message;

	public ExceptionDialog(Context context, String message) {
		super(context);
		this.message = message;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTitle("An error has occurred");
		setMessage(message);
		
		super.onCreate(savedInstanceState);
	}
}
