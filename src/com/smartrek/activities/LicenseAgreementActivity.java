package com.smartrek.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

public class LicenseAgreementActivity extends Activity {
    
    /**
     * Request code
     */
    public static final int LICENSE_AGREEMENT_ACTIVITY = 1;
    
    /**
     * Preference key
     */
    public static final String LICENSE_AGREEMENT = "LicenseAgreement";
    
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
    private Button buttonDisagree;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.license_agreement);
        
        buttonAgree = (Button) findViewById(R.id.button_agree);
        buttonAgree.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("Global", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(LICENSE_AGREEMENT, AGREED);
                editor.commit();
                
                setResult(AGREED);
                //finishActivity(LICENSE_AGREEMENT);
                finish();
            }
            
        });
        
        buttonDisagree = (Button) findViewById(R.id.button_disagree);
        buttonDisagree.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setResult(DISAGREED);
                //finishActivity(LICENSE_AGREEMENT);
                finish();
            }
            
        });
        
        webviewContent = (WebView) findViewById(R.id.webview_content);
        webviewContent.loadUrl("file:///android_asset/license.html");
    }
}
