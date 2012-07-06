package com.smartrek.activities;

import java.io.IOException;
import java.util.Stack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;

import com.smartrek.mappers.UserMapper;
import com.smartrek.models.User;

public class UserRegistrationActivity extends ExceptionSafeActivity {
	
	private EditText editTextUsername;
	private EditText editTextFirstname;
	private EditText editTextLastname;
	private EditText editTextEmail;
	private EditText editTextPassword;
	private EditText editTextPasswordConfirm;
	private Button buttonRegister;
	
	private Stack<Exception> exceptions = new Stack<Exception>();
	
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
        buttonRegister.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				checkUserInput();
				return false;
			}
        	
        });
    }
    
    /**
     * 
     * @return False if it fails, true otherwise.
     */
    private boolean checkUserInput() {
    	String username = editTextUsername.getText().toString().trim();
    	if (username.equals("")) {
    		return false;
    	}
    	
    	String firstname = editTextFirstname.getText().toString().trim();
    	String lastname = editTextLastname.getText().toString().trim();
    	// TODO: Check firstname and lastname
    	
    	String email = editTextEmail.getText().toString().trim();
    	// TODO: Validate email address
    	
    	String password = editTextPassword.getText().toString().trim();
    	String passwordConfirm = editTextPasswordConfirm.getText().toString().trim();
    	
    	if (!password.equals(passwordConfirm)) {
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
			UserMapper mapper = new UserMapper();
			try {
				mapper.register(user);
			}
			catch (IOException e) {
				registerException(e);
			}
			
			return user;
		}
    	
		@Override
		protected void onPostExecute(Object result) {
			if (exceptions.isEmpty()) {
				AlertDialog dialog = new AlertDialog.Builder(UserRegistrationActivity.this).create();
				// TODO: Text localization
				dialog.setTitle("Info");
				dialog.setMessage("Successfully registered.");
				dialog.setButton("Dismiss", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						UserRegistrationActivity.this.finish();
					}
					
				});
				dialog.show();
			}
			else {
				reportExceptions();
			}
		}
    }
}
