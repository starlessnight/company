package com.smartrek.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.utils.Font;

public class NotificationDialog2 extends Dialog {
	
	public interface ActionListener {
        void onClickDismiss();
    }
    
    private ActionListener actionListener;
	
	private CharSequence message;
	private CharSequence title = "Oops!";
	private ViewGroup dialogView;
	private Typeface boldFont;
	private Typeface lightFont;
	private CharSequence buttonText = "Dismiss";

	public NotificationDialog2(Context context, CharSequence message) {
		super(context, R.style.PopUpDialog);
		this.message = message;
		setCanceledOnTouchOutside(false);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		AssetManager assets = getContext().getAssets();
		boldFont = Font.getBold(assets);
		lightFont = Font.getLight(assets);
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.notification_dialog, null);
		
		TextView titleView = (TextView)dialogView.findViewById(R.id.title);
		titleView.setText(title);
		
		TextView messageView = (TextView) dialogView.findViewById(R.id.message);
		messageView.setText(message);
		
		TextView dismissView = (TextView) dialogView.findViewById(R.id.dismiss);
		dismissView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(actionListener != null) {
					actionListener.onClickDismiss();
				}
				dismiss();
			}
		});
		dismissView.setText(buttonText);
		
		Font.setTypeface(boldFont, titleView, dismissView);
		Font.setTypeface(lightFont, messageView);
		
		setContentView(dialogView);
		
		super.onCreate(savedInstanceState);
	}
	
	public void setActionListener(ActionListener listener) {
	    this.actionListener = listener;
	}
	
	public void setTitle(CharSequence title) {
		this.title = title;
	}
	
	public void setButtonText(CharSequence buttonText) {
		this.buttonText = buttonText;
	}

}
