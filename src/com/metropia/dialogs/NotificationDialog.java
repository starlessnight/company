package com.metropia.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.metropia.activities.R;

public class NotificationDialog extends AlertDialog implements DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {
    
    public interface ActionListener {
        void onClickDismiss();
    }
    
    private ActionListener actionListener;
	
	private CharSequence message;

	public NotificationDialog(Context context, CharSequence message) {
		super(context, R.style.PopUpDialog);
		setCanceledOnTouchOutside(false);
		this.message = message;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setTitle("Notification");
		setMessage(message);
		setOnCancelListener(this);
		setOnDismissListener(this);
		
		setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getResources().getString(R.string.close), new OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	dismiss();
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

	@Override
	public void onCancel(DialogInterface dialog) {
		dismiss();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (actionListener != null) actionListener.onClickDismiss();
	}
	
	
}
