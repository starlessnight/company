package com.metropia.requests;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.metropia.models.User;
import com.metropia.requests.CityRequest.City;
import com.metropia.tasks.ICallback;
import com.metropia.utils.HTTP.Method;

public class DuoTripCheckRequest extends Request {
	
	public DuoTripCheckRequest(User user) {
		this.username = user.getUsername();
		this.password = user.getPassword();
		
		url = getLinkUrl(Link.query_DUO).replaceAll("\\{user_id\\}", String.valueOf(user.getId()));
	}
	
	public int execute(Context ctx) throws Exception {
        String str = executeHttpRequest(Method.GET, url, ctx);
        
        JSONObject json = new JSONObject(str);
        int timeToNext = json.getJSONObject("data").getInt("time_to_next");
        return timeToNext;
    }
	
	public void executeAsync(final Context ctx, final ICallback cb) {
		
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				
				Integer result = null;
				try {
					result = DuoTripCheckRequest.this.execute(ctx);
				} catch (Exception e) {}
				return result;
				
			}
			@Override
            protected void onPostExecute(Integer result) {
				if (cb!=null) cb.run(result);
			}
			
		}.execute();
		
	}
	
}
