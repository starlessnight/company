package com.smartrek.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.smartrek.activities.R;

public class CancelableProgressDialog extends ProgressDialog {
	
	public interface ActionListener {
		/**
		 * Gets called when 'cancel' button is clicked.
		 */
		public void onClickNegativeButton();
	}
	
	private ActionListener actionListener;

	public CancelableProgressDialog(Context context, String message) {
		super(context);
		setMessage(message);
		setCanceledOnTouchOutside(false);
	}
	
	public void setActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.cancel), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (actionListener != null) {
					actionListener.onClickNegativeButton();
				}
			}
		});
		
		super.onCreate(savedInstanceState);
	}

}
