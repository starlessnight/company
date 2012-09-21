package com.smartrek.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

public class NotificationDialog extends AlertDialog {
    
    public interface ActionListener {
        void onClickDismiss();
    }
    
    private ActionListener actionListener;
	
	private String message;

	public NotificationDialog(Context context, String message) {
		super(context);
		this.message = message;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTitle("Notification");
		setMessage(message);
		
		setButton(DialogInterface.BUTTON_NEGATIVE, "Dismiss", new OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (actionListener != null) {
                    actionListener.onClickDismiss();
                }
            }
        });
		
		super.onCreate(savedInstanceState);
	}
	
	public void setActionListener(ActionListener listener) {
	    this.actionListener = listener;
	}
}
