package com.metropia.dialogs;

import org.apache.commons.lang3.StringUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.metropia.ui.animation.ClickAnimation;
import com.metropia.ui.animation.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.utils.Font;
import com.metropia.activities.R;

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
	private Typeface mediumFont;
	private CharSequence buttonText = "Dismiss";
	private CharSequence negativeButtonText = "Cancel";
	
	private boolean buttonVerticalOrientation = true;
	
	private int messageTextSize = 0;
	
	private String detailMessage;
	private boolean postiveClickDismiss = true;
	AlertDialog dialog;

	public NotificationDialog2(Context context, CharSequence message) {
		super(context, R.style.PopUpDialog);
		this.message = message;
		setCanceledOnTouchOutside(false);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		AssetManager assets = getContext().getAssets();
		boldFont = Font.getBold(assets);
		mediumFont = Font.getMedium(assets);
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.notification_dialog, null);
		
		TextView titleView = (TextView)dialogView.findViewById(R.id.title);
		titleView.setText(title);
		
		TextView messageView = (TextView) dialogView.findViewById(R.id.message);
		messageView.setText(message);
		if(StringUtils.isBlank(title)) {
			titleView.setVisibility(View.GONE);
			messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
		}
		else if(messageTextSize > 0) {
			messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, messageTextSize);
		}
		
		try{
		    messageView.setMovementMethod(LinkMovementMethod.getInstance());
        }catch(Throwable t){}
		
		if(!buttonVerticalOrientation) {
			dialogView.findViewById(R.id.vertical_button_panel).setVisibility(View.GONE);
			dialogView.findViewById(R.id.horizontal_button_panel).setVisibility(View.VISIBLE);
		}
		
		int dismissId = buttonVerticalOrientation?R.id.dismiss:R.id.h_dismiss;
		int seperator2Id = buttonVerticalOrientation?R.id.seperator2:R.id.h_seperator2;
		int reportProblemId = buttonVerticalOrientation?R.id.reportProblem:R.id.h_reportProblem;
		int seperator3Id = buttonVerticalOrientation?R.id.seperator3:R.id.h_seperator3;
		int cancelId = buttonVerticalOrientation?R.id.cancel:R.id.h_cancel;
		
		TextView dismissView = (TextView) dialogView.findViewById(dismissId);
		dismissView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(getContext(), v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						if(positiveActionListener != null) {
							positiveActionListener.onClick();
						}
						
						if(postiveClickDismiss) {
							dismiss();
						}
					}
				});
			}
		});
		dismissView.setText(buttonText);
		
		if(reportProblemActionListener != null) {
			dialogView.findViewById(seperator2Id).setVisibility(View.VISIBLE);
			TextView reportProblemView = (TextView) dialogView.findViewById(reportProblemId);
			reportProblemView.setVisibility(View.VISIBLE);
			reportProblemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
			    	ClickAnimation clickAnimation = new ClickAnimation(getContext(), v);
			    	clickAnimation.startAnimation(new ClickAnimationEndCallback(){
						@Override
						public void onAnimationEnd() {
							reportProblemActionListener.onClick();
							dismiss();
						}
					});
				}
		   });
			Font.setTypeface(boldFont, reportProblemView);
		}
		
		if(negativeActionListener != null) {
		   dialogView.findViewById(seperator3Id).setVisibility(View.VISIBLE);
		   TextView cancelView = (TextView) dialogView.findViewById(cancelId);
		   cancelView.setVisibility(View.VISIBLE);
		   cancelView.setOnClickListener(new View.OnClickListener() {
		       @Override
			   public void onClick(View v) {
		    	   ClickAnimation clickAnimation = new ClickAnimation(getContext(), v);
		    	   clickAnimation.startAnimation(new ClickAnimationEndCallback(){
					@Override
					public void onAnimationEnd() {
						negativeActionListener.onClick();
					    dismiss();
					}
				});
			  }
		   });
		   cancelView.setText(negativeButtonText);
		   Font.setTypeface(boldFont, cancelView);
		}
		
		Font.setTypeface(boldFont, titleView, dismissView);
		Font.setTypeface(mediumFont, messageView);
		
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

	public void setReportProblemActionListener(ActionListener reportProblemActionListener) {
		this.reportProblemActionListener = reportProblemActionListener;
	}
	
	public void setNegativeButtonText(CharSequence negativeButtonText) {
		this.negativeButtonText = negativeButtonText;
	}

	public void setNegativeActionListener(ActionListener negativeActionListener) {
		this.negativeActionListener = negativeActionListener;
	}
	
	public void setVerticalOrientation(boolean verticalOrientation) {
		this.buttonVerticalOrientation = verticalOrientation;
	}

	public void setMessageTextSize(int messageTextSize) {
		this.messageTextSize = messageTextSize;
	}

	public void setDetailMessage(String detailMessage) {
		this.detailMessage = detailMessage;
	}
	
	public void hideNegativeButtonAndShowDetail() {
		TextView titleView = (TextView)dialogView.findViewById(R.id.title);
		titleView.setText("Error Message");
		int seperator3Id = buttonVerticalOrientation?R.id.seperator3:R.id.h_seperator3;
		int dismissId = buttonVerticalOrientation?R.id.dismiss:R.id.h_dismiss;
		dialogView.findViewById(seperator3Id).setVisibility(View.GONE);
		TextView dismissView = (TextView) dialogView.findViewById(dismissId);
		dismissView.setVisibility(View.GONE);
		TextView messageView = (TextView) dialogView.findViewById(R.id.message);
		messageView.setText(detailMessage);
		messageView.setGravity(Gravity.LEFT);
	}

	public void setPostiveClickDismiss(boolean postiveClickDismiss) {
		this.postiveClickDismiss = postiveClickDismiss;
	}
	
	@Override
	public void show() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext()).setTitle(title).setMessage(message);
		
		builder.setPositiveButton(buttonText, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (positiveActionListener!=null) positiveActionListener.onClick();
				if (postiveClickDismiss) dialog.dismiss();
			}
		});
		
		if (negativeActionListener!=null)
		builder.setNegativeButton(negativeButtonText, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {negativeActionListener.onClick();}
		});
		
		dialog = builder.create();
		dialog.show();
	}

}
