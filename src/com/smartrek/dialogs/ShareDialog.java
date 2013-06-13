package com.smartrek.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.utils.Font;

public class ShareDialog extends Dialog {
	
    private String title;
    
    private String shareText;
    
	private ViewGroup dialogView;
	
	/**
	 * When an instance of Address is given, this dialog will trigger an edit mode.
	 * 
	 * @param context
	 * @param address
	 */
	public ShareDialog(Context context, String title, String shareText) {
		super(context, R.style.PopUpDialog);
		this.title = title;
		this.shareText = shareText;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.share_dialog, null);
		
		TextView titleView = (TextView) dialogView.findViewById(R.id.title);
		titleView.setText(title);
		
		TextView facebookButton = (TextView) dialogView.findViewById(R.id.facebook_button);
		
		TextView twitterButton = (TextView) dialogView.findViewById(R.id.twitter_button);
		
		TextView googlePlusButton = (TextView) dialogView.findViewById(R.id.google_plus_button);
		
		setContentView(dialogView);
		
		View closeIcon = dialogView.findViewById(R.id.close_icon);
		closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
		
		AssetManager assets = getContext().getAssets();
		Font.setTypeface(Font.getBold(assets), titleView, facebookButton, 
	        twitterButton, googlePlusButton);
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
		
	}
	
}
