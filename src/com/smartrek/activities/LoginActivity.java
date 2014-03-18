package com.smartrek.activities;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smartrek.activities.R.color;
import com.smartrek.dialogs.NotificationDialog2;
import com.smartrek.models.User;
import com.smartrek.requests.Request;
import com.smartrek.requests.Request.Page;
import com.smartrek.requests.UserIdRequest;
import com.smartrek.tasks.LoginTask;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.Misc;
import com.smartrek.utils.Preferences;

public final class LoginActivity extends Activity implements OnClickListener,
        TextWatcher {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
	private EditText editTextUsername;
	private EditText editTextPassword;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);
        
        /* If it hasn't set up the login screen */
        
        TextView login = (TextView) findViewById(R.id.login_button);
        login.setOnClickListener(this);
        
//        TextView new_user = (TextView) findViewById(R.id.new_user_button);
//        new_user.setOnClickListener(registerButtonClickListener);
        
        TextView forgetPwd = (TextView) findViewById(R.id.forget_pwd);
        forgetPwd.setText(Html.fromHtml("<u>Forgot password?</u>"));
        forgetPwd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Request.getPageUrl(Page.reset_password))));
            }
        });
        
        TextView terms = (TextView) findViewById(R.id.terms);
//        terms.setText(Html.fromHtml("By signing up, you agree to the <u>Terms & Conditions</u>"));
//        terms.setText("By using this application, you agree to the ");;
//        terms.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(LoginActivity.this, TermsAndPrivacyActivity.class);
//                startActivity(intent);
//            }
//        });
        terms.setText(getTermsDescription(LoginActivity.this));
        terms.setMovementMethod(LinkMovementMethod.getInstance());
        terms.setLinkTextColor(Color.BLACK);
        
        TextView newUser = (TextView) findViewById(R.id.new_user);
        SpannableString createAccount = new SpannableString(Html.fromHtml("Or Create Account"));
        ClickableSpan create = new ClickableSpan() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(LoginActivity.this, UserRegistrationActivity.class);
				startActivity(intent);
			}
        };
        createAccount.setSpan(create, 3, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        newUser.setText(createAccount);
        newUser.setMovementMethod(LinkMovementMethod.getInstance());
        newUser.setLinkTextColor(Color.BLACK);
        
        editTextUsername = (EditText) findViewById(R.id.username_box);
        editTextUsername.addTextChangedListener(this);
        editTextPassword = (EditText) findViewById(R.id.pwd_box);
        editTextPassword.addTextChangedListener(this);
        
        AssetManager assets = getAssets();
        //Font.setTypeface(Font.getBold(assets));
        Font.setTypeface(Font.getLight(assets), editTextUsername, 
        		editTextPassword, login, terms, newUser, 
        		(TextView) findViewById(R.id.forget_pwd));
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Misc.initGCM(this);
    }
    
    @Override
    public void onBackPressed(){
    	finish();
    }

	@Override
	public void onClick(View v) {
	    if(Misc.isAddGoogleAccount(this)){
            Misc.showGoogleAccountDialog(this);
        }else{
    		final String username = editTextUsername.getText().toString();
    		String password = editTextPassword.getText().toString();
    		
    		SharedPreferences globalPrefs = Preferences.getGlobalPreferences(this);
    		String gcmRegistrationId = globalPrefs.getString(Preferences.Global.GCM_REG_ID, "");
    		
    		SharedPreferences loginPrefs = Preferences.getAuthPreferences(this);
    		SharedPreferences.Editor loginPrefsEditor = loginPrefs.edit();
    		loginPrefsEditor.putString(User.USERNAME, username);
    		loginPrefsEditor.putString(User.PASSWORD, password);
    		loginPrefsEditor.commit();
    		
    		final LoginTask loginTask = new LoginTask(this, username, password, gcmRegistrationId){
                @Override
                protected void onPostLogin(User user) {
                    if(user != null && user.getId() != -1) {
                        Log.d("Login_Activity","Successful Login");
                        Log.d("Login_Activity", "Saving Login Info to Shared Preferences");
    
                        User.setCurrentUser(LoginActivity.this, user);
                        
                        Intent intent = new Intent(LoginActivity.this, LandingActivity2.ENABLED?LandingActivity2.class:LandingActivity.class);
                        LoginActivity.this.startActivity(intent);
                        finish();
                    }
                    else {
    //                  Log.d("Login_Activity", "Failed Login User: " + user.getUsername());
    //                  TextView loginfail_text = (TextView) findViewById(R.id.failed_login);
    //                  loginfail_text.setVisibility(View.VISIBLE);
                        editTextPassword.setText("");
                     
                        CharSequence msg;
                        Exception exc = ehs.hasExceptions()?ehs.popException().getException():null;
                        if(exc instanceof ConnectException || exc instanceof UnknownHostException){
                            msg = getString(R.string.no_connection);
                        }else if(exc instanceof SocketTimeoutException){
                            msg = getString(R.string.connection_timeout);
                        }
                        else if(exc instanceof HttpResponseException && ((HttpResponseException)exc).getStatusCode() == 500){
                            msg = "The server encountered an unexpected condition which prevented it from fulfilling the request.";
                        }else{
                            msg = Html.fromHtml(getAccountPwdErrorMsg());
                        }
                        NotificationDialog2 notificationDialog = new NotificationDialog2(LoginActivity.this, msg);
                        notificationDialog.show();
                    }                
                }
    		};
    		if(Request.NEW_API){
    		    new AsyncTask<Void, Void, Integer>() {
    		        protected void onPreExecute() {
    		            loginTask.showDialog();
    		        }
                    @Override
                    protected Integer doInBackground(Void... params) {
                        Integer id = null;
                        try {
                            UserIdRequest req = new UserIdRequest(username); 
                            req.invalidateCache(LoginActivity.this);
                            id = req.execute(LoginActivity.this);
                        }
                        catch(Exception e) {
                            ehs.registerException(e);
                        }
                        return id;
                    }
                    protected void onPostExecute(Integer userId) {
                        if(userId == null){
                            loginTask.hideDialog();
                            NotificationDialog2 notificationDialog = new NotificationDialog2(LoginActivity.this, 
                                Html.fromHtml(getAccountPwdErrorMsg()));
                            notificationDialog.show();
                        }else{
                            loginTask.setUserId(userId)
                                .execute();
                        }
                    }
                }.execute();
    		}else{
    		    loginTask.execute();
    		}
        }
	}
	
	public static SpannableString getTermsDescription(final Context ctx) {
		SpannableString termsString = new SpannableString("By using this application, you agree to the Terms & Conditions and Privacy Policy");
        ClickableSpan termsAndCondition = new ClickableSpan() {
			@Override
			public void onClick(View view) {
				Log.d("LoginActivity", "click terms and condition");
				Intent intent = new Intent(ctx, TermOfUseActivity.class);
				ctx.startActivity(intent);
			}
        };
        termsString.setSpan(termsAndCondition, 44, 62, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        ClickableSpan privacyPolicy = new ClickableSpan() {
			@Override
			public void onClick(View view) {
				Log.d("LoginActivity", "click privacy policy");
				Intent intent = new Intent(ctx, PrivacyPolicyActivity.class);
				ctx.startActivity(intent);
			}
        };
        termsString.setSpan(privacyPolicy, 67, 81, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return termsString;
	}
	
	private static String getAccountPwdErrorMsg(){
	    return "The username or password you entered is not valid.&nbsp;"
                + "<a href=\"" + Request.getPageUrl(Page.reset_password) + "\">Forgot your password?</a>";
	}
	
	Button.OnClickListener registerButtonClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.d("Login_Activity","Register New user clicked");
			Log.d("Login_Activity","Starting Register Activity");
			Intent intent = new Intent(LoginActivity.this, UserRegistrationActivity.class);
			startActivity(intent);
		}
	};

    @Override
    public void afterTextChanged(Editable s) {
        
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        TextView login = (TextView) findViewById(R.id.login_button);
        boolean enabled = StringUtils.isNotBlank(editTextUsername.getText()) && StringUtils.isNotBlank(editTextPassword.getText());
        login.setEnabled(enabled);
        login.setTextColor(getResources().getColor(enabled?android.R.color.white:R.color.lighter_gray));
    }

}
