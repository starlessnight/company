package com.smartrek.dialogs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.models.User;
import com.smartrek.requests.Request;
import com.smartrek.requests.Request.Page;
import com.smartrek.utils.Misc;
import com.smartrek.utils.SessionM;

public class FeedbackDialog extends Dialog {
    
    private static final String encoding = "utf-8";
    
    public static final String getUrl(Context ctx, String category, String message){
        String url = StringUtils.defaultString(Request.getPageUrl(Page.feedback))
            .replaceAll("\\{os\\}", "android")
            .replaceAll("\\{app_version\\}", ctx.getString(R.string.distribution_date));
        User user = User.getCurrentUser(ctx);
        if(user != null){
            url = url.replaceAll("\\{username\\}", StringUtils.defaultString(user.getUsername()))
            .replaceAll("\\{first_name\\}", StringUtils.defaultString(user.getFirstname()))
            .replaceAll("\\{email\\}", StringUtils.defaultString(user.getEmail()));
        }
        if(StringUtils.isNotBlank(category)){
            url = url.replaceAll("\\{category\\}", category);
        }
        if(StringUtils.isNotBlank(message)){
            try {
                url = url.replaceAll("\\{message\\}", URLEncoder.encode(truncate(message), encoding));
            }
            catch (UnsupportedEncodingException e) {}
        }
        return url;
    }
    
    private static String truncate(String val) throws UnsupportedEncodingException {
        byte[] src = val.getBytes(encoding);
        byte[] dest = new byte[Math.min(7168, src.length)];
        System.arraycopy(src, 0, dest, 0, dest.length);
        return new String(dest, encoding);
    }
    
	public interface ActionListener {
		void onClickPositiveButton();
		void onClickNegativeButton();
	}
	
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
		titleView.setText("Do you love Metropia?");
		
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
		settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);
		settings.setBuiltInZoomControls(true);
		webviewContent.loadUrl(getUrl(ctx, null, null));
		webviewContent.setVisibility(View.VISIBLE);
        webviewContent.requestFocus(View.FOCUS_DOWN);
        Misc.fadeIn(ctx, webviewContent);
		
		View closeIcon = dialogView.findViewById(R.id.close_icon);
		closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionM.logAction("feedback");
                dismiss();
            }
        });
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
		
	}
	
}
