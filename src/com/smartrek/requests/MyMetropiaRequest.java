package com.smartrek.requests;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.smartrek.requests.MyMetropiaRequest.MyMetropia;

public final class MyMetropiaRequest extends FetchRequest<MyMetropia> {

	public static class MyMetropia {
		public int reward;
		public int timeSaving;
		public int co2Saving;
	}

	public MyMetropiaRequest(String username) {
		super(getLinkUrl(Link.query_username).replaceAll("\\{username\\}", username));
	}
	
	@Override
	public MyMetropia execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		JSONObject json  = new JSONObject(response);
		JSONArray data = json.getJSONArray("data");
		MyMetropia myMetropia = new MyMetropia();
		if(data.length() > 0){
			String balanceString = data.getJSONObject(0).optString("balance", "");
			Log.d("MyMetropiaRequest", "balance : " + balanceString);
			if(balanceString != null) {
				JSONObject balance = new JSONObject(balanceString);
				myMetropia.reward = balance.optInt("credit", 0);
				myMetropia.timeSaving = balance.optInt("time_saving", 0);
				myMetropia.co2Saving = balance.optInt("co2_saving", 0);
			}
		}
        return myMetropia;
	}

}
