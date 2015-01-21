package com.metropia.activities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.models.User;
import com.metropia.requests.Request;
import com.metropia.requests.Request.Page;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;

public class FeedbackActivity extends FragmentActivity{
	
    public static final String CATEGORY = "CATEGORY";
    
    public static final String MESSAGE = "MESSAGE";
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		
		Localytics.integrate(this);
		
		TextView backButton = (TextView) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		final WebView webviewContent = (WebView) findViewById(R.id.webview_content);
		webviewContent.setWebViewClient(Misc.getSSLTolerentWebViewClient());
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
        Intent intent = getIntent();
        webviewContent.loadUrl(getUrl(this, 
            intent.getStringExtra(CATEGORY), intent.getStringExtra(MESSAGE)));
        webviewContent.setVisibility(View.VISIBLE);
        webviewContent.requestFocus(View.FOCUS_DOWN);
        Misc.fadeIn(this, webviewContent);
			
		AssetManager assets = getAssets();
		
		Font.setTypeface(Font.getBold(assets), (TextView) findViewById(R.id.header));
		Font.setTypeface(Font.getLight(assets), backButton);
		//init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
    private static final String encoding = "utf-8";
    
    private static String truncate(String val) throws UnsupportedEncodingException {
        byte[] src = val.getBytes(encoding);
        byte[] dest = new byte[Math.min(7168, src.length)];
        System.arraycopy(src, 0, dest, 0, dest.length);
        return new String(dest, encoding);
    }
	
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

	
	@Override
	protected void onStop() {
	    super.onStop();
	    GoogleAnalytics.getInstance(this).reportActivityStop(this);
	    Misc.tripInfoPanelOnActivityStop(this);
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();
	    Misc.tripInfoPanelOnActivityRestart(this);
	}
	
	@Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
	
	@Override
	public void onPause() {
	    Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
	    super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	    Localytics.openSession();
	    Localytics.upload();
	    Localytics.setInAppMessageDisplayActivity(this);
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
	}
	
}
