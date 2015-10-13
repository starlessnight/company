package com.metropia.requests;

import android.content.Context;
import android.os.AsyncTask;

import com.metropia.models.User;
import com.metropia.requests.CityRequest.City;
import com.metropia.tasks.ICallback;
import com.metropia.utils.HTTP.Method;

public class SendMailRequest extends Request {
	
	public SendMailRequest(User user) {
		this.username = user.getUsername();
		this.password = user.getPassword();
		this.url = getLinkUrl(Link.send_user_email);
	}
	
	public void execute(Context ctx, City city, String campaign) throws Exception {
		url = url.replaceAll("\\{city\\}", city.name).replaceAll("\\{campaign\\}", campaign);
		executeHttpRequest(Method.GET, url, ctx);
	}
	
	public void executeAsync(final Context ctx, final City city, final String campaign, final ICallback cb) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				
				try {
					SendMailRequest.this.execute(ctx, city, campaign);
				} catch (Exception e) {}
				if (cb!=null) cb.run();
				return null;
			}
		}.execute();
	}
}
