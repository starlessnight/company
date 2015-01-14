package com.metropia.activities;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
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
		
		Bundle extras = getIntent().getExtras();
		Integer page = extras.getInt(WHICH_PAGE);
		
		TextView backButton = (TextView) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		webviewContent = (WebView) findViewById(R.id.webview_content);
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
            .replaceAll("\\{password\\}", user.getPassword());
    }
	
	@Override
	public void onBackPressed() {
	    if(webviewContent.canGoBack()){
            webviewContent.goBack();
        }else{
            finish();
        }
	}
	
}