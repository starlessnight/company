package com.metropia.requests;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.metropia.requests.MyMetropiaRequest.MyMetropia;
import com.metropia.tasks.ICallback;

public final class MyMetropiaRequest extends FetchRequest<MyMetropia> {

	public static class MyMetropia {
		private int credit;
		private int timeSaving;
		private double co2Saving;
		
		private static final NumberFormat df = new DecimalFormat(".#");
		
		public MyMetropia() {}
		public MyMetropia(JSONObject obj) {
			credit = obj.optInt("credit");
			timeSaving = obj.optInt("timeSaving")/60;
			co2Saving = obj.optDouble("co2Saving");
		}
		public JSONObject toJSON() {
			JSONObject obj = new JSONObject();
			try {
				obj.put("credit", credit);
				obj.put("timeSaving", timeSaving*60);
				obj.put("co2Saving", co2Saving);
			} catch (JSONException e) {}
			return obj;
		}
		
		public int getCredit() {
			return credit;
		}
		
		public int getTimeSaving() {
			return timeSaving;
		}
		
		public double getCo2Saving() {
			return Double.valueOf(df.format(Math.round(co2Saving * 10) / 10f));
		}
		
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
			if(StringUtils.isNotBlank(balanceString)) {
				JSONObject balance = new JSONObject(balanceString);
				myMetropia = new MyMetropia(balance);
			}
		}
        return myMetropia;
	}
	
	public void executeAsyc(final Context ctx, final ICallback cb) {
		new AsyncTask<Void, Void, MyMetropia>() {
    		
			@Override
			protected MyMetropia doInBackground(Void... params) {
				MyMetropia info = null;
				try {
					info = MyMetropiaRequest.this.execute(ctx);
				}
				catch(Exception e) {Log.e("fetch MyMetropia failed", e.toString());}
				return info;
			}
			
			protected void onPostExecute(MyMetropia info) {
                if (cb!=null) cb.run(info);
			}
    		
    	}.execute();
	}

}
