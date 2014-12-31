package com.metropia.activities;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.dialogs.NotificationDialog2.ActionListener;
import com.metropia.models.User;
import com.metropia.requests.UserRegistrationRequest;
import com.metropia.utils.Dimension;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;
import com.metropia.utils.Preferences;
import com.metropia.activities.R;

public final class UserRegistrationActivity extends FragmentActivity
        implements TextWatcher {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private EditText editTextFirstname;
	private EditText editTextLastname;
	private EditText editTextZipCode;
	private EditText editTextEmail;
	private EditText editTextPassword;
	private EditText editTextPasswordConfirm;
	private TextView buttonRegister;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_registration);
        
        TextView backButton = (TextView) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(UserRegistrationActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		});
        
        editTextFirstname = (EditText) findViewById(R.id.editTextFirstname);
        editTextFirstname.addTextChangedListener(this);
        editTextLastname = (EditText) findViewById(R.id.editTextLastname);
        editTextLastname.addTextChangedListener(this);
        editTextZipCode = (EditText) findViewById(R.id.editTextZipCode);
        editTextZipCode.addTextChangedListener(this);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextEmail.addTextChangedListener(this);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPassword.addTextChangedListener(this);
        editTextPasswordConfirm = (EditText) findViewById(R.id.editTextPasswordConfirm);
        editTextPasswordConfirm.addTextChangedListener(this);
        
        buttonRegister = (TextView) findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				resetEditText();
			    if(Misc.isAddGoogleAccount(UserRegistrationActivity.this)){
		            Misc.showGoogleAccountDialog(UserRegistrationActivity.this);
		        }else{
		            checkUserInput();
		        }
			}

        });
        
        TextView terms = (TextView) findViewById(R.id.terms);
        terms.setText(getTermsDescription(UserRegistrationActivity.this));
        terms.setMovementMethod(LinkMovementMethod.getInstance());
        terms.setLinkTextColor(Color.BLACK);
        
        queryUserInfo();
        
        AssetManager assets = getAssets();
        
        Font.setTypeface(Font.getLight(assets), editTextEmail, editTextFirstname, editTextLastname, 
        		editTextPassword, editTextPasswordConfirm, editTextZipCode, terms);
        Font.setTypeface(Font.getBold(assets), buttonRegister, (TextView) findViewById(R.id.header));
        
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
    }
    
    private void queryUserInfo(){
        final AccountManager manager = AccountManager.get(this);
        final Account[] accounts = manager.getAccountsByType("com.google");
        if (accounts[0].name != null) {
            String accountName = accounts[0].name;

            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Email.DATA + " = ?",
                    new String[] { accountName }, null);
            while (emailCur.moveToNext()) {
                String email = emailCur
                        .getString(emailCur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                editTextEmail.setText(email);
                
                String newName = emailCur
                        .getString(emailCur
                                .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String name = null;
                if (name == null || newName.length() > name.length())
                    name = newName;
                
                String[] nameToks = StringUtils.split(name, " ");
                String firstname;
                String lastname = null;
                if(nameToks.length > 1){
                   firstname = StringUtils.join(ArrayUtils.subarray(nameToks, 0, nameToks.length - 1), " ");
                   lastname = nameToks[nameToks.length - 1];
                }else{
                   firstname = nameToks[0];
                }
                editTextFirstname.setText(firstname);
                editTextLastname.setText(lastname);
            }

            emailCur.close();
        }
    }
    
	@Override
	public void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
		Misc.initGCM(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
	
    /**
     * 
     * @return False if it fails, true otherwise.
     */
    private boolean checkUserInput() {
    	// TODO: Define a common interface to validate your input
    	
    	String firstname = editTextFirstname.getText().toString().trim();
    	String lastname = editTextLastname.getText().toString().trim();
    	List<EditText> missInfoEditText = new ArrayList<EditText>();
    	// TODO: Check firstname and lastname
    	if (firstname.equals("")) {
    		missInfoEditText.add(editTextFirstname);
    	}
    	if (lastname.equals("")) {
    		missInfoEditText.add(editTextLastname);
    	}
    	
    	String zipCode = editTextZipCode.getText().toString().trim();
    	if(zipCode.equals("")) {
    		missInfoEditText.add(editTextZipCode);
    	}
    	
    	String email = editTextEmail.getText().toString().trim();
    	if (email.equals("")) {
    		missInfoEditText.add(editTextEmail);
    	}
    	// TODO: Validate email address
    	
    	String password = editTextPassword.getText().toString().trim();
    	String passwordConfirm = editTextPasswordConfirm.getText().toString().trim();
    	
    	if(password.equals("")) {
    		missInfoEditText.add(editTextPassword);
    	}
    	
    	if (!password.equals(passwordConfirm)) {
    		if(missInfoEditText.isEmpty()) {
    			List<EditText> pswAlert = new ArrayList<EditText>();
    			pswAlert.add(editTextPassword);
    			pswAlert.add(editTextPasswordConfirm);
    			setEditTextAlert(pswAlert);
    			showPasswordNotMatchMessage();
    			return false;
    		}
    		else {
    			missInfoEditText.add(editTextPasswordConfirm);
    		}
    	}
    	
    	if(missInfoEditText.isEmpty()) {
	    	User user = new User();
	    	user.setUsername(email);
	    	user.setFirstname(firstname);
	    	user.setLastname(lastname);
	    	user.setEmail(email);
	    	user.setPassword(password);
	    	user.setZipCode(zipCode);
			new UserRegistrationTask(this).execute(user);
			return true;
    	}
    	else {
    		showMissingInfoMessage();
    		setEditTextAlert(missInfoEditText);
    		return false;
    	}
    }
    
    private void setEditTextAlert(List<EditText> edits) {
    	for(EditText edit : edits) {
	    	edit.setBackgroundResource(R.drawable.alert_registration_text_field);
	    	edit.setPadding(Dimension.dpToPx(15, getResources().getDisplayMetrics()), 
	    			Dimension.dpToPx(10, getResources().getDisplayMetrics()), 
	    			Dimension.dpToPx(15, getResources().getDisplayMetrics()), 
	    			Dimension.dpToPx(10, getResources().getDisplayMetrics()));
    	}
    }
    
    private void resetEditText() {
    	EditText[] edits = new EditText[] { editTextFirstname, editTextLastname, editTextZipCode, 
    			editTextEmail, editTextPassword, editTextPasswordConfirm};
    	for(EditText edit : edits) {
	    	edit.setBackgroundResource(R.drawable.registration_text_field);
	    	edit.setPadding(Dimension.dpToPx(15, getResources().getDisplayMetrics()), 
	    			Dimension.dpToPx(10, getResources().getDisplayMetrics()), 
	    			Dimension.dpToPx(15, getResources().getDisplayMetrics()), 
	    			Dimension.dpToPx(10, getResources().getDisplayMetrics()));
    	}
    }
    
    private SpannableString getTermsDescription(final Context ctx) {
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
    
    private void showMissingInfoMessage() {
    	NotificationDialog2 dialog = new NotificationDialog2(UserRegistrationActivity.this, "Required fields are marked in red");
    	dialog.setTitle("Missing some stuff");
    	dialog.setPositiveButtonText("OK");
    	dialog.show();
    }
    
    private void showPasswordNotMatchMessage() {
    	NotificationDialog2 dialog = new NotificationDialog2(UserRegistrationActivity.this, "Your passwords don't match!");
    	dialog.setPositiveButtonText("OK");
    	dialog.show();
    }

    private class UserRegistrationTask extends AsyncTask<Object, Object, User> {
    	
        private ProgressDialog dialog;
        
        private Context ctx;
        
        public UserRegistrationTask(Context ctx){
            this.ctx = ctx;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ctx);
            dialog.setTitle("Metropia");
            dialog.setMessage("Signing up ...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }
        
		@Override
		protected User doInBackground(Object... params) {
			
			User user = (User) params[0];
			UserRegistrationRequest request = new UserRegistrationRequest();
			try {
				request.execute(user, ctx);
			}
			catch (Exception e) {
				ehs.registerException(e, e.getMessage());
			}
			
			return user;
		}
    	
		@Override
		protected void onPostExecute(final User result) {
		    dialog.cancel();
		    if (ehs.hasExceptions()) {
		        ehs.reportExceptions();
		    }
		    else {
		    	final NotificationDialog2 dialog = new NotificationDialog2(UserRegistrationActivity.this, "Now check your mailbox for a verification email!");
		    	dialog.setTitle("New Account Created");
		    	dialog.setPositiveButtonText("OK");
		    	dialog.setPositiveActionListener(new ActionListener() {
					@Override
					public void onClick() {
						SharedPreferences loginPrefs = Preferences.getAuthPreferences(UserRegistrationActivity.this);
                        SharedPreferences.Editor loginPrefsEditor = loginPrefs.edit();
                        loginPrefsEditor.putString(User.USERNAME, result.getUsername());
                        loginPrefsEditor.putString(User.NEW_USER, result.getUsername());
                        loginPrefsEditor.commit();
						
                        Intent openIntent = getOpenEmailAppIntent();
    				    if(openIntent != null) {
    					    startActivity(openIntent);
    				    	finish();
    				    }
    				    else {
    				    	if(dialog.isShowing()) {
    				    		dialog.dismiss();
    				    	}
    				    	
    				    	NotificationDialog2 manualOpenDialog = new NotificationDialog2(UserRegistrationActivity.this, "No default email app. Please launch email app manually.");
    				    	manualOpenDialog.setPositiveButtonText("Dismiss");
    				    	manualOpenDialog.setPositiveActionListener(new ActionListener() {
								@Override
								public void onClick() {
									Intent loginIntent = new Intent(UserRegistrationActivity.this, LoginActivity.class);
									startActivity(loginIntent);
									finish();
								}
    				    	});
    				    	manualOpenDialog.show();
    				    }
					}
		    	});
		    	dialog.show();
			}
		}
    }
    
    private Intent getOpenEmailAppIntent() {
    	Intent getEmailAppIntent = new Intent(Intent.ACTION_SENDTO);
	    getEmailAppIntent.setData(Uri.parse("mailto:"));
	    PackageManager pm = getPackageManager();
	    if(getEmailAppIntent.resolveActivity(pm) != null) {
	    	try {
	    		ActivityInfo info = pm.getActivityInfo(getEmailAppIntent.resolveActivity(pm), 0);
		    	Intent openIntent = pm.getLaunchIntentForPackage(info.applicationInfo.packageName);
		    	return openIntent;
	    	}
	    	catch(Exception ignore) {
//	    		ExceptionHandlingService ehs = new ExceptionHandlingService(UserRegistrationActivity.this);
//	    		ehs.reportException(ignore);
	    	}
	    }
	    return null;
    }

    @Override
    public void afterTextChanged(Editable s) {
        
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
//        boolean enabled = true;
//        EditText[] inputs = {editTextEmail, editTextFirstname, editTextLastname, 
//            editTextPassword, editTextPasswordConfirm, editTextZipCode};
//        for (EditText input : inputs) {
//            enabled &= StringUtils.isNotBlank(input.getText());
//        }
//        buttonRegister.setEnabled(enabled);
    }
    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
    
}
