package com.smartrek.activities;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.smartrek.ui.ClickAnimation;
import com.smartrek.ui.ClickAnimation.ClickAnimationEndCallback;
import com.smartrek.utils.Font;

public class TermOfUseActivity extends FragmentActivity {
	
	private static final String URL = "http://www.metropia.com/terms";
	
	private Typeface boldFont;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.term_of_use);
		
		AssetManager assets = getAssets();
		boldFont = Font.getBold(assets);
		
		TextView backButton = (TextView) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(TermOfUseActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {

					@Override
					public void onAnimationEnd() {
						finish();
					}
					
				});
			}
		});
		
		final WebView webviewContent = (WebView) findViewById(R.id.webview_content);
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
		webviewContent.loadUrl(URL);
		webviewContent.setVisibility(View.VISIBLE);
        webviewContent.requestFocus(View.FOCUS_DOWN);
        
        Font.setTypeface(boldFont, (TextView) findViewById(R.id.header));
        
	}

}
