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
	
	private CharSequence message;
	private ViewGroup dialogView;
	private Typeface boldFont;
	private Typeface lightFont;

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
		
		TextView messageView = (TextView) dialogView.findViewById(R.id.message);
		messageView.setText(message);
		
		TextView dismissView = (TextView) dialogView.findViewById(R.id.dismiss);
		dismissView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		Font.setTypeface(boldFont, (TextView)dialogView.findViewById(R.id.title), dismissView);
		Font.setTypeface(lightFont, messageView);
		
		setContentView(dialogView);
		
		super.onCreate(savedInstanceState);
	}

}
