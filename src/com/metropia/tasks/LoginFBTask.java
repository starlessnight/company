package com.metropia.tasks;

import android.content.Context;

import com.facebook.AccessToken;
import com.metropia.models.User;
import com.metropia.requests.UserFBLoginRequest;
import com.metropia.requests.UserIdRequest;

public abstract class LoginFBTask extends LoginTask {

	public LoginFBTask(Context ctx) {
		super(ctx, null, null, "");
		dialog.setMessage("Logging in with Facebook...");
	}
	public LoginFBTask(Context ctx, String username, String password) {
		this(ctx);
		this.username = username;
		this.password = password;
	}
	
	@Override
	protected User doInBackground(String... params) {
		
		User user = null;
		try {
			if (username==null) {
				UserFBLoginRequest request = new UserFBLoginRequest(AccessToken.getCurrentAccessToken().getToken());
				request.invalidateCache(ctx);
				user = request.execute(ctx);
				username = user.getUsername();
				password = user.getPassword();
			}
			
			UserIdRequest req = new UserIdRequest(username); 
            req.invalidateCache(ctx);
            Integer id = req.execute(ctx);
            setUserId(id);
            
            user = super.doInBackground(params);
            user.setType(User.FACEBOOK);
		} catch (Exception e) {}
		
		return user;
	}


}
