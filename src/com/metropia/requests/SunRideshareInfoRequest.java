package com.metropia.requests;


import org.json.JSONObject;

/**
 * for Sun Rideshare Activity
 * **/


import android.content.Context;
import android.os.AsyncTask;

import com.metropia.models.User;
import com.metropia.requests.CityRequest.City;
import com.metropia.tasks.ICallback;
import com.metropia.utils.HTTP.Method;

public class SunRideshareInfoRequest extends Request {
	
	public SunRideshareInfoRequest(User user) {
		this.username = user.getUsername();
		this.password = user.getPassword();
		this.url = getLinkUrl(Link.optin_sunrideshare);
	}
	
	public JSONObject execute(Context ctx, City city, String campaign) throws Exception {
		JSONObject params= new JSONObject();
		params.put("city", city.name);
		params.put("campaign", campaign);
		
		String str = executeHttpRequest(Method.POST, url, ctx);
		JSONObject jsonObject = new JSONObject(str);
		return jsonObject.getJSONObject("data");
	}
	
	public void executeAsync(final Context ctx, final City city, final String campaign, final ICallback cb) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				
				JSONObject jsonObject = null;
				try {
					jsonObject = SunRideshareInfoRequest.this.execute(ctx, city, campaign);
				} catch (Exception e) {}
				if (cb!=null) cb.run(jsonObject);
				return null;
			}
		}.execute();
	}
}
