package com.smartrek.dialogs;

import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.ui.ClickAnimation;
import com.smartrek.ui.ClickAnimation.ClickAnimationEndCallback;
import com.smartrek.utils.Font;

public class NotifyResumeDialog extends Dialog{
	
	public interface ActionListener {
        void onClick();
    }
	
	private Typeface mediumFont;
	private ViewGroup dialogView;
	private ActionListener listener;
	
	private AtomicBoolean trigger = new AtomicBoolean(true);

	public NotifyResumeDialog(Context context) {
		super(context, R.style.PopUpDialog);
		setCanceledOnTouchOutside(false);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		AssetManager assets = getContext().getAssets();
		mediumFont = Font.getMedium(assets);
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.resume_navigation_dialog, null);
		
		TextView titleView = (TextView)dialogView.findViewById(R.id.title);
		titleView.setText("Resume Navigation");
		
		TextView messageView = (TextView) dialogView.findViewById(R.id.message);
		messageView.setText("Navigation was discontinued before reaching destination. Would you like to resume last route?");
		
		final View yesPanel = dialogView.findViewById(R.id.yes_panel);
		yesPanel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				trigger.set(false);
				ClickAnimation clickAnimation = new ClickAnimation(getContext(), v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						if(listener != null) {
							listener.onClick();
						}
						dismiss();
					}
					
				});
			}
		});
		
		TextView noView = (TextView) dialogView.findViewById(R.id.no);
		noView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				trigger.set(false);
				ClickAnimation clickAnimation = new ClickAnimation(getContext(), v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						dismiss();
					}
				});
			}
		});
		
		final TextView countDownView = (TextView) dialogView.findViewById(R.id.count_down);
		
		new CountDownTimer(5000, 1000) {
		     public void onTick(long millisUntilFinished) {
		    	 countDownView.setText(String.valueOf(millisUntilFinished / 1000));
		     }

		     public void onFinish() {
		    	 if(trigger.get()) {
		    		 yesPanel.performClick();
		    	 }
		     }
		 }.start();

		Font.setTypeface(mediumFont, messageView, titleView, noView, 
				countDownView, (TextView) dialogView.findViewById(R.id.yes));
		setContentView(dialogView);
			
		super.onCreate(savedInstanceState);
	}
	
	public void setYesListener(ActionListener listener) {
		this.listener = listener;
	}

}
