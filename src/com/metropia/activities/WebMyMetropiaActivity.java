package com.metropia.activities;

import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.LocalyticsUtils;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.models.User;
import com.metropia.requests.Request;
import com.metropia.requests.Request.Page;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;
import com.metropia.activities.R;

public class WebMyMetropiaActivity extends FragmentActivity{
	
	private WebView webviewContent;
	
	public static final String WHICH_PAGE = "pageNo";
	
	public static final int MY_METROPIA_PAGE = 1;
	public static final int TIME_SAVING_PAGE = 2;
	public static final int CO2_SAVING_PAGE = 3;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_my_metropia);
		
		Localytics.integrate(this);
		LocalyticsUtils.tagVisitMyMetropia();
		
		Bundle extras = getIntent().getExtras();
		final Integer page = extras.getInt(WHICH_PAGE);
		
		TextView backButton = (TextView) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		webviewContent = (WebView) findViewById(R.id.webview_content);
		webviewContent.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }  
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadUrl("file:///android_asset/error.html");
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
            	String host = "null";
            	try {
            		host = Uri.parse(getUrl(WebMyMetropiaActivity.this, page)).getHost();
            	}
            	catch(Exception ignore) {}
                if (url != null && !url.contains(host)) {
                    view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
        });
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
        webviewContent.clearCache(true);
        webviewContent.loadUrl(getUrl(this, page));
        webviewContent.setVisibility(View.VISIBLE);
        webviewContent.requestFocus(View.FOCUS_DOWN);
        Misc.fadeIn(this, webviewContent);
        
		AssetManager assets = getAssets();
		
		Font.setTypeface(Font.getBold(assets), (TextView) findViewById(R.id.header));
		Font.setTypeface(Font.getLight(assets), backButton);
		
		//init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	@Override
	protected void onStop() {
	    super.onStop();
	    Misc.tripInfoPanelOnActivityStop(this);
	    GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
	
	@Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
	
	@Override
	protected void onRestart() {
	    super.onRestart();
	    Misc.tripInfoPanelOnActivityRestart(this);
	}
	
	public static boolean hasMyMetropiaUrl(Context ctx){
	    return Request.getPageUrl(Page.my_metropia) != null;
	}
	
	public static boolean hasTimeSavingUrl(Context ctx){
	    return Request.getPageUrl(Page.time_saving) != null;
	}
	
	public static boolean hasCo2SavingUrl(Context ctx){
	    return Request.getPageUrl(Page.co2_saving) != null;
	}
	
	static String getUrl(Context ctx, Integer pageNo){
        User user = User.getCurrentUser(ctx);
        String url = "";
        switch(pageNo) {
        	case MY_METROPIA_PAGE:
        		url = StringUtils.defaultString(Request.getPageUrl(Page.my_metropia));
        		break;
        	case TIME_SAVING_PAGE:
        		url = StringUtils.defaultString(Request.getPageUrl(Page.time_saving));
        		break;
        	case CO2_SAVING_PAGE:
        		url = StringUtils.defaultString(Request.getPageUrl(Page.co2_saving));
        		break;
        	default:
        		url = StringUtils.defaultString(Request.getPageUrl(Page.my_metropia));
        }
        return url.replaceAll("\\{username\\}", user.getUsername())
            .replaceAll("\\{password\\}", URLEncoder.encode(user.getPassword()));
    }
	
	@Override
	public void onBackPressed() {
	    if(webviewContent.canGoBack()){
            webviewContent.goBack();
        }else{
            finish();
        }
	}
	
	@Override
	public void onResume() {
		super.onResume();
	    Localytics.openSession();
	    Localytics.tagScreen(this.getClass().getSimpleName());
	    Localytics.upload();
	    Localytics.setInAppMessageDisplayActivity(this);
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
	}

	@Override
	public void onPause() {
	    Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
	    super.onPause();
	}
	
}
