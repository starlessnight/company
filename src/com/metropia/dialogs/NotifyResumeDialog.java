package com.metropia.dialogs;

import java.util.concurrent.atomic.AtomicBoolean;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.metropia.ui.ClickAnimation;
import com.metropia.ui.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.utils.Font;
import com.metropia.activities.R;

public class NotifyResumeDialog extends Dialog implements android.view.View.OnClickListener{
	
	public interface ActionListener {
        void onClick();
    }
	
	TextView countDownView;
	
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
		
		countDownView = (TextView) dialogView.findViewById(R.id.count_down);
		
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

		Font.setTypeface(mediumFont, messageView, titleView, noView, countDownView, (TextView) dialogView.findViewById(R.id.yes));
		//setContentView(dialogView);
			
		super.onCreate(savedInstanceState);
	}
	
	public void setYesListener(ActionListener listener) {
		this.listener = listener;
	}
	
	
	AlertDialog dialog;
	CountDownTimer counter;
	
	@Override
	public void show() {
		String title = "Resume Navigation";
		String message = "Navigation was discontinued before reaching destination. Would you like to resume last route?";
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext()).setTitle(title).setMessage(message);
		
		
		dialog = builder.create();
		ViewGroup pane = (ViewGroup) LayoutInflater.from(this.getContext()).inflate(R.layout.resume_navigation_buttons, null);
		
		dialog.setView(pane, 0, 0, 0, 0);
		dialog.show();
		
		dialog.findViewById(R.id.yes_panel).setOnClickListener(this);
		dialog.findViewById(R.id.no).setOnClickListener(this);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		
		counter = new CountDownTimer(5000, 1000) {
		     public void onTick(long millisUntilFinished) {
		    	 ((TextView) dialog.findViewById(R.id.count_down)).setText(String.valueOf(millisUntilFinished / 1000));
		     }

		     public void onFinish() {
		    	 dialog.findViewById(R.id.yes_panel).performClick();
		     }
		 }.start();
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
			case R.id.yes_panel:
				if (listener!=null) listener.onClick();
			break;
		}

		counter.cancel();
		dialog.dismiss();
	}

}
