package com.smartrek.requests;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.requests.MyMetropiaRequest.MyMetropia;

public final class MyMetropiaRequest extends FetchRequest<MyMetropia> {

	public static class MyMetropia {
		private int credit;
		private int timeSaving;
		private double co2Saving;
		
		private static final NumberFormat df = new DecimalFormat(".#");
		
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
				myMetropia.credit = balance.optInt("credit", 0);
				myMetropia.timeSaving = balance.optInt("time_saving", 0) / 60;
				myMetropia.co2Saving = balance.optDouble("co2_saving", 0);
			}
		}
        return myMetropia;
	}

}
