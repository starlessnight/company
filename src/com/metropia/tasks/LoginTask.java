package com.metropia.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.metropia.activities.R;
import com.metropia.models.User;
import com.metropia.requests.UserLoginRequest;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Misc;

/**
 * Methods in this class will be executed asynchronously. 
 */
public abstract class LoginTask extends AsyncTask<String, Object, User> {
	
	protected ProgressDialog dialog;
	
	protected String username;
	protected String password;
	private String gcmRegistrationId;
	private boolean dialogEnabled = true;
	protected ExceptionHandlingService ehs;
	private Integer userId;
	
	protected Context ctx;
	
	public LoginTask(Context ctx, String username, String password, String gcmRegistrationId) {
		super();
		
		this.username = username;
		this.password = password;
		this.gcmRegistrationId = gcmRegistrationId;
		this.ctx = ctx;
		ehs = new ExceptionHandlingService(ctx);
		
		dialog = new ProgressDialog(ctx, R.style.PopUpDialog);
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
	        Misc.doQuietly(new Runnable() {
	            @Override
	            public void run() {
	                dialog.show();
	            }
	        });
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
	        Misc.doQuietly(new Runnable() {
	            @Override
	            public void run() {
	                dialog.cancel();
	            }
	        });
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
            Misc.doQuietly(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });
        }
        return this;
    }
    
    public void showDialog(){
        if(dialogEnabled){
            Misc.doQuietly(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });
        }
    }
    
    public void hideDialog(){
        Misc.doQuietly(new Runnable() {
            @Override
            public void run() {
                dialog.cancel();
            }
        });
    }
	
}