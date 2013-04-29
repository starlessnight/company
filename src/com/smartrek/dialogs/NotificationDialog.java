package com.smartrek.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.smartrek.activities.R;

public class NotificationDialog extends AlertDialog {
    
    public interface ActionListener {
        void onClickDismiss();
    }
    
    private ActionListener actionListener;
	
	private CharSequence message;

	public NotificationDialog(Context context, CharSequence message) {
		super(context);
		this.message = message;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTitle("Notification");
		setMessage(message);
		
		setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getResources().getString(R.string.close), new OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (actionListener != null) {
                    actionListener.onClickDismiss();
                }
            }
        });
		
		super.onCreate(savedInstanceState);
		
		try{
		    ((TextView)findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
		}catch(Throwable t){}
	}
	
	public void setActionListener(ActionListener listener) {
	    this.actionListener = listener;
	}
}
