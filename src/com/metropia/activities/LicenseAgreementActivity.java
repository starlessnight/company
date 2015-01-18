package com.metropia.activities;

import org.apache.commons.lang3.StringUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.requests.Request;
import com.metropia.requests.Request.Page;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.HTTP;
import com.metropia.utils.Preferences;

public class LicenseAgreementActivity extends FragmentActivity {
    
    /**
     * Request code
     */
    public static final int LICENSE_AGREEMENT_ACTIVITY = 1;
    
    public static final int LICENSE_AGREEMENT_UPDATED = 2;
    
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
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
    private String eTag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.license_agreement);
        
        Localytics.integrate(this);
        
        buttonAgree = (Button) findViewById(R.id.button_agree);
        buttonAgree.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences prefs = Preferences.getGlobalPreferences(LicenseAgreementActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(Preferences.Global.LICENSE_AGREEMENT, AGREED);
                editor.commit();
                
                DebugOptionsActivity.setEulaEtag(LicenseAgreementActivity.this, eTag);
                
                setResult(AGREED);
                //finishActivity(LICENSE_AGREEMENT);
                finish();
            }
            
        });
        
        webviewContent = (WebView) findViewById(R.id.webview_content);
        webviewContent.setWebViewClient(new WebViewClient() {
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
        new AsyncTask<Void, Void, Result>() {
            @Override
            protected Result doInBackground(Void... params) {
                Result rs = new Result();
                try{
                    HTTP http = new HTTP(Request.getPageUrl(Page.eula));
                    http.connect();
                    rs.html = http.getResponseBody();
                    rs.eTag = http.getETag();
                }catch(Exception e){
                    ehs.registerException(e);
                }
                return rs;
            }
            @Override
            protected void onPostExecute(Result rs) {
                if (ehs.hasExceptions()) {
                    ehs.reportExceptions();
                }
                else {
                    webviewContent.loadDataWithBaseURL("file:///android_asset/", 
                        rs.html, "text/html", "utf-8", null);
                    eTag = rs.eTag;
                    buttonAgree.setEnabled(true);
                }
            }
        }.execute();        
        
        AssetManager assets = getAssets();
        Font.setTypeface(Font.getBold(assets), (TextView)findViewById(R.id.title),
            (TextView)findViewById(R.id.contine_notice), buttonAgree);
        Font.setTypeface(Font.getLight(assets));
        
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
    }
    
	@Override
	public void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
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

	@Override
	public void onPause() {
	    Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
	    super.onPause();
	}
	
	private static class Result {
        
        String eTag;
        
        String html;
        
    }
	
}
