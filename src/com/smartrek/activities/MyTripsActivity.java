package com.smartrek.activities;

import org.apache.commons.lang3.StringUtils;

import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.models.User;
import com.smartrek.requests.Request;
import com.smartrek.requests.Request.Page;
import com.smartrek.utils.Font;
import com.smartrek.utils.Misc;

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

public class MyTripsActivity extends FragmentActivity {
	
	private WebView webviewContent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_trips);
		
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
        webviewContent.loadUrl(getUrl(this));
        webviewContent.setVisibility(View.VISIBLE);
        webviewContent.requestFocus(View.FOCUS_DOWN);
        Misc.fadeIn(this, webviewContent);
        
		AssetManager assets = getAssets();
		
		Font.setTypeface(Font.getBold(assets), (TextView) findViewById(R.id.header));
		Font.setTypeface(Font.getLight(assets), backButton);
	}
	
	@Override
	protected void onStop() {
	    super.onStop();
	    Misc.tripInfoPanelOnActivityStop(this);
	    EasyTracker.getInstance().activityStop(this);
	}
	
	@Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }
	
	@Override
	protected void onRestart() {
	    super.onRestart();
	    Misc.tripInfoPanelOnActivityRestart(this);
	}
	
	public static boolean hasUrl(Context ctx){
	    return Request.getPageUrl(Page.trips) != null;
	}
	
	static String getUrl(Context ctx){
        User user = User.getCurrentUser(ctx);
        return StringUtils.defaultString(Request.getPageUrl(Page.trips))
            .replaceAll("\\{username\\}", user.getUsername())
            .replaceAll("\\{password\\}", user.getPassword())
            .replaceAll("\\{user_id\\}", String.valueOf(user.getId()));
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
