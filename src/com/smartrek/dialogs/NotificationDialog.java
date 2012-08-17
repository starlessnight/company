package com.smartrek.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

public class NotificationDialog extends AlertDialog {
	
	private String message;

	public NotificationDialog(Context context, String message) {
		super(context);
		this.message = message;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTitle("Notification");
		setMessage(message);
		
		super.onCreate(savedInstanceState);
	}
}
