package com.smartrek.activities;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.smartrek.models.User;
import com.smartrek.requests.UserRegistrationRequest;
import com.smartrek.utils.ExceptionHandlingService;

public final class UserRegistrationActivity extends Activity {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private EditText editTextUsername;
	private EditText editTextFirstname;
	private EditText editTextLastname;
	private EditText editTextEmail;
	private EditText editTextPassword;
	private EditText editTextPasswordConfirm;
	private Button buttonRegister;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_registration);
        
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextFirstname = (EditText) findViewById(R.id.editTextFirstname);
        editTextLastname = (EditText) findViewById(R.id.editTextLastname);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPasswordConfirm = (EditText) findViewById(R.id.editTextPasswordConfirm);
        
        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				checkUserInput();
			}

        });
    }
    
    /**
     * 
     * @return False if it fails, true otherwise.
     */
    private boolean checkUserInput() {
    	// TODO: Define a common interface to validate your input
    	
    	String username = editTextUsername.getText().toString().trim();
    	if (username.equals("")) {
    		ehs.reportException("Please enter your username.");
    		return false;
    	}
    	
    	String firstname = editTextFirstname.getText().toString().trim();
    	String lastname = editTextLastname.getText().toString().trim();
    	// TODO: Check firstname and lastname
    	
    	if (firstname.equals("")) {
    		ehs.reportException("Please enter your first name.");
    		return false;
    	}
    	if (lastname.equals("")) {
    		ehs.reportException("Please enter your last name.");
    		return false;
    	}
    	
    	String email = editTextEmail.getText().toString().trim();
    	if (email.equals("")) {
    		ehs.reportException("Please enter your email address.");
    		return false;
    	}
    	// TODO: Validate email address
    	
    	String password = editTextPassword.getText().toString().trim();
    	String passwordConfirm = editTextPasswordConfirm.getText().toString().trim();
    	
    	if (!password.equals(passwordConfirm)) {
    		ehs.reportException("The two passwords you entered do not match.");
    		return false;
    	}
    	
    	User user = new User();
    	user.setUsername(username);
    	user.setFirstname(firstname);
    	user.setLastname(lastname);
    	user.setEmail(email);
    	user.setPassword(password);
    	
		new UserRegistrationTask().execute(user);
		
		return true;
    }

    private class UserRegistrationTask extends AsyncTask<Object, Object, Object> {
    	
		@Override
		protected Object doInBackground(Object... params) {
			
			User user = (User) params[0];
			UserRegistrationRequest request = new UserRegistrationRequest();
			try {
				request.execute(user);
			}
			catch (IOException e) {
				ehs.registerException(e);
			}
			
			return user;
		}
    	
		@Override
		protected void onPostExecute(Object result) {
		    if (ehs.hasExceptions()) {
		        ehs.reportExceptions();
		    }
		    else {
				AlertDialog dialog = new AlertDialog.Builder(UserRegistrationActivity.this).create();
				// TODO: Text localization
				dialog.setTitle("Info");
				dialog.setMessage("Successfully registered.");
				dialog.setButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						UserRegistrationActivity.this.finish();
					}
					
				});
				dialog.show();
			}
		}
    }
}
