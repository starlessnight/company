package com.smartrek.activities;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.requests.Request;
import com.smartrek.requests.Request.Page;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.HTTP;
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
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);

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
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String html = null;
                try{
                    HTTP http = new HTTP(Request.getPageUrl(Page.eula));
                    http.connect();
                    html = http.getResponseBody(); 
                }catch(Exception e){
                    ehs.registerException(e);
                }
                return html;
            }
            @Override
            protected void onPostExecute(String html) {
                if (ehs.hasExceptions()) {
                    ehs.reportExceptions();
                }
                else {
                    webviewContent.loadDataWithBaseURL("file:///android_asset/", 
                        html, "text/html", "utf-8", null);
                }
            }
        }.execute();        
        
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
