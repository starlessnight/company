package com.smartrek.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.Misc;

public class FeedbackDialog extends Dialog {
	
	public interface ActionListener {
		void onClickPositiveButton();
		void onClickNegativeButton();
	}
	
	private ExceptionHandlingService ehs = new ExceptionHandlingService(getContext());
	
	private ActionListener listener;
	private ViewGroup dialogView;
	private Activity ctx;
	
	public FeedbackDialog(Activity ctx) {
		super(ctx, R.style.PopUpDialog);
		this.ctx = ctx;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.feedback_dialog, null);
		
		TextView titleView = (TextView) dialogView.findViewById(R.id.title);
		titleView.setText("Do you love Smartrek?");
		
		setContentView(dialogView);
		
		final WebView webviewContent = (WebView) dialogView.findViewById(R.id.webview_content);
		webviewContent.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        switch (event.getAction()) {
		            case MotionEvent.ACTION_DOWN:
		            case MotionEvent.ACTION_UP:
		                if (!v.hasFocus()) {
		                    v.requestFocus();
		                }
		                break;
		        }
		        return false;
		    }
		});
		WebSettings settings = webviewContent.getSettings();
        settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);
		settings.setBuiltInZoomControls(true);
		webviewContent.loadUrl("http://www.smartrekmobile.com/support/contact");
		
		final Button yesButton = (Button) dialogView.findViewById(R.id.yes_button);
		yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, 
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getContext().getApplicationContext().getPackageName())));
                if (listener != null) {
                    listener.onClickPositiveButton();
                }
                dismiss();
            }
        });
		
		final Button noButton = (Button) dialogView.findViewById(R.id.no_button);
		noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClickNegativeButton();
                }
                findViewById(R.id.scrollview).setVisibility(View.GONE);
                webviewContent.setVisibility(View.VISIBLE);
                webviewContent.requestFocus(View.FOCUS_DOWN);
                Misc.fadeIn(ctx, webviewContent);
            }
        });
		
		View closeIcon = dialogView.findViewById(R.id.close_icon);
		closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
		
		AssetManager assets = getContext().getAssets();
		Font.setTypeface(Font.getBold(assets), titleView, yesButton, noButton);
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
		
	}
	
	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}
	
}
