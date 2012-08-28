package com.smartrek.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;

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
		
		setButton(DialogInterface.BUTTON_NEGATIVE, "Dismiss", (Message) null);
		
		super.onCreate(savedInstanceState);
	}
}
