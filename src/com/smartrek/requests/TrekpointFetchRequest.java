package com.smartrek.requests;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

public final class TrekpointFetchRequest extends FetchRequest<Long> {

	public TrekpointFetchRequest(int uid) {
		super(String.format("%s/getusercredits/%d", HOST, uid));
	}
	
	@Override
	public Long execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		JSONObject json  = new JSONArray(response).getJSONObject(0);
		return json.optLong("CREDIT");
	}

}
