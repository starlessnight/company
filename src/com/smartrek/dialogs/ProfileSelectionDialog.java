package com.smartrek.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;

public class ProfileSelectionDialog extends Dialog {
	
    public enum Type { facebook, googlePlus }
    
	public interface ActionListener {
		void onClickPositiveButton(Type type);
		void onClickNegativeButton();
	}
	
	private ExceptionHandlingService ehs = new ExceptionHandlingService(getContext());
	
	private ActionListener listener;
	private ViewGroup dialogView;
	
	public ProfileSelectionDialog(Activity ctx) {
		super(ctx, R.style.PopUpDialog);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.profile_selection_dialog, null);
		
		setContentView(dialogView);
		
		TextView facebookButton = (TextView) dialogView.findViewById(R.id.facebook_button);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onClickPositiveButton(Type.facebook);
                }
            }
        });
        
        TextView googlePlusButton = (TextView) dialogView.findViewById(R.id.google_plus_button);
        googlePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onClickPositiveButton(Type.googlePlus);
                }
            }
        });
		
		View closeIcon = dialogView.findViewById(R.id.close_icon);
		closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onClickNegativeButton();
                }
            }
        });
		
		AssetManager assets = getContext().getAssets();
		Font.setTypeface(Font.getBold(assets), (TextView) dialogView.findViewById(R.id.title), 
	        facebookButton, googlePlusButton);
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
		
	}
	
	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}
	
}
