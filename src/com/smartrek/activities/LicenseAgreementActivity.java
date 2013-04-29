package com.smartrek.activities;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.utils.Font;
import com.smartrek.utils.Preferences;

public class LicenseAgreementActivity extends Activity {
    
    /**
     * Request code
     */
    public static final int LICENSE_AGREEMENT_ACTIVITY = 1;
    
    /**
     * Preference value
     */
    public static final int AGREED = 1;
    
    /**
     * Preference value
     */
    public static final int DISAGREED = 0;
    
    private WebView webviewContent;
    private Button buttonAgree;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.license_agreement);
        
        buttonAgree = (Button) findViewById(R.id.button_agree);
        buttonAgree.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences prefs = Preferences.getGlobalPreferences(LicenseAgreementActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(Preferences.Global.LICENSE_AGREEMENT, AGREED);
                editor.commit();
                
                setResult(AGREED);
                //finishActivity(LICENSE_AGREEMENT);
                finish();
            }
            
        });
        
        InputStream is = null;
        try {
            is = getResources().getAssets().open("license.html");
            webviewContent = (WebView) findViewById(R.id.webview_content);
            webviewContent.setWebViewClient(new WebViewClient() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
				public void onPageFinished(WebView view, String url) {
                    view.setBackgroundColor(0x00000000);
                    if (Build.VERSION.SDK_INT >= 11) {
                        view.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
                    }
                    webviewContent.setVisibility(View.VISIBLE);
                }
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    String emailProtocol = "mailto:";
                    if (url != null && url.startsWith("http://")) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } else if(url != null && url.startsWith(emailProtocol)){
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", StringUtils.substringAfter(url, emailProtocol), null));
                        startActivity(emailIntent);
                    }
                    return true;
                }
             });
            webviewContent.loadDataWithBaseURL("file:///android_asset/", 
                IOUtils.toString(is), "text/html", "utf-8", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        
        AssetManager assets = getAssets();
        Font.setTypeface(Font.getBold(assets), (TextView)findViewById(R.id.title),
            (TextView)findViewById(R.id.contine_notice), buttonAgree);
        Font.setTypeface(Font.getLight(assets));
    }
    
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
}
