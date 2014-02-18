package com.smartrek.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.smartrek.models.User;
import com.smartrek.requests.UserLoginRequest;
import com.smartrek.utils.ExceptionHandlingService;

/**
 * Methods in this class will be executed asynchronously. 
 */
public abstract class LoginTask extends AsyncTask<String, Object, User> {
	
	private ProgressDialog dialog;
	
	private String username;
	private String password;
	private String gcmRegistrationId;
	private boolean dialogEnabled = true;
	protected ExceptionHandlingService ehs;
	private Integer userId;
	
	private Context ctx;
	
	public LoginTask(Context ctx, String username, String password, String gcmRegistrationId) {
		super();
		
		this.username = username;
		this.password = password;
		this.gcmRegistrationId = gcmRegistrationId;
		this.ctx = ctx;
		ehs = new ExceptionHandlingService(ctx);
		
		dialog = new ProgressDialog(ctx);
        dialog.setTitle("Metropia");
        dialog.setMessage(String.format("Logging in as '%s'...", username));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
	}
	
	public LoginTask setUserId(Integer userId){
	    this.userId = userId;
	    return this;
	}
	
	@Override
	protected void onPreExecute() {
	    if(dialogEnabled){
            dialog.show();
        }
	}

	@Override
	protected User doInBackground(String... params) {
		User user = null;
		try {
		    UserLoginRequest request = null; 
		    if(userId != null){
		        request = new UserLoginRequest(userId, username, password);
		    }else{
		        request = new UserLoginRequest(username, password, gcmRegistrationId);
		    }
		    request.invalidateCache(ctx);
			user = request.execute(ctx);
		}
		catch(Exception e) {
		    ehs.registerException(e);
		}
		
		if(user == null) {
			user = new User(-1, username);
		}
		
		return user;
	}
	
	@Override
	protected final void onPostExecute(User user) {
	    if(dialogEnabled){
	        dialog.cancel();
	    }
	    onPostLogin(user);
	}
	
	abstract protected void onPostLogin(User user);

    public boolean isDialogEnabled() {
        return dialogEnabled;
    }

    public LoginTask setDialogEnabled(boolean dialogEnabled) {
        this.dialogEnabled = dialogEnabled;
        if(getStatus() == Status.RUNNING && !dialog.isShowing()){
            dialog.show();
        }
        return this;
    }
    
    public void showDialog(){
        if(dialogEnabled){
            dialog.show();
        }
    }
    
    public void hideDialog(){
        dialog.cancel();
    }
    
	
}