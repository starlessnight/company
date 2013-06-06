package com.smartrek.requests;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.requests.TrekpointFetchRequest.Trekpoint;

public final class TrekpointFetchRequest extends FetchRequest<Trekpoint> {

    public static class Trekpoint {
        
        public long credit;
        
        public long lifeTimeCredit;
        
    }
    
	public TrekpointFetchRequest(int uid) {
		super(String.format("%s/getusercredits/%d", HOST, uid));
	}
	
	@Override
	public Trekpoint execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		JSONObject json  = new JSONArray(response).getJSONObject(0);
		Trekpoint point = new Trekpoint();
		point.credit = json.optLong("CREDIT");
		point.lifeTimeCredit = json.optLong("LIFETIMECREDIT");
		return point;
	}

}
