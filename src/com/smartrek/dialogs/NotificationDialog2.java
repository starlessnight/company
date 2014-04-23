package com.smartrek.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.utils.Font;

public class NotificationDialog2 extends Dialog {
	
	public interface ActionListener {
        void onClick();
    }
    
    private ActionListener positiveActionListener;
    private ActionListener reportProblemActionListener;
    private ActionListener negativeActionListener;
	
	private CharSequence message;
	private CharSequence title = "Oops!";
	private ViewGroup dialogView;
	private Typeface boldFont;
	private Typeface lightFont;
	private CharSequence buttonText = "Dismiss";
	private CharSequence negativeButtonText = "Cancel";

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
		
		try{
		    messageView.setMovementMethod(LinkMovementMethod.getInstance());
        }catch(Throwable t){}
		
		TextView dismissView = (TextView) dialogView.findViewById(R.id.dismiss);
		dismissView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(positiveActionListener != null) {
					positiveActionListener.onClick();
				}
				dismiss();
			}
		});
		dismissView.setText(buttonText);
		
		if(reportProblemActionListener != null) {
			dialogView.findViewById(R.id.seperator2).setVisibility(View.VISIBLE);
			TextView reportProblemView = (TextView) dialogView.findViewById(R.id.reportProblem);
			reportProblemView.setVisibility(View.VISIBLE);
			reportProblemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					reportProblemActionListener.onClick();
					dismiss();
				}
		   });
			Font.setTypeface(boldFont, reportProblemView);
		}
		
		if(negativeActionListener != null) {
		   dialogView.findViewById(R.id.seperator3).setVisibility(View.VISIBLE);
		   TextView cancelView = (TextView) dialogView.findViewById(R.id.cancel);
		   cancelView.setVisibility(View.VISIBLE);
		   cancelView.setOnClickListener(new View.OnClickListener() {
		       @Override
			   public void onClick(View v) {
		    	   negativeActionListener.onClick();
			       dismiss();
			   }
		   });
		   cancelView.setText(negativeButtonText);
		   Font.setTypeface(boldFont, cancelView);
		}
		
		Font.setTypeface(boldFont, titleView, dismissView);
		Font.setTypeface(lightFont, messageView);
		
		setContentView(dialogView);
		
		super.onCreate(savedInstanceState);
	}
	
	public void setPositiveActionListener(ActionListener listener) {
	    this.positiveActionListener = listener;
	}
	
	public void setTitle(CharSequence title) {
		this.title = title;
	}
	
	public void setPositiveButtonText(CharSequence buttonText) {
		this.buttonText = buttonText;
	}

	public void setReportProblemActionListener(
			ActionListener reportProblemActionListener) {
		this.reportProblemActionListener = reportProblemActionListener;
	}
	
	public void setNegativeButtonText(CharSequence negativeButtonText) {
		this.negativeButtonText = negativeButtonText;
	}

	public void setNegativeActionListener(ActionListener negativeActionListener) {
		this.negativeActionListener = negativeActionListener;
	}

}
