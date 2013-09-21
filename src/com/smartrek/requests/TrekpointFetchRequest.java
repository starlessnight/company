package com.smartrek.requests;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.User;
import com.smartrek.requests.TrekpointFetchRequest.Trekpoint;

public final class TrekpointFetchRequest extends FetchRequest<Trekpoint> {

    public static class Trekpoint {
        
        public long credit;
        
        public long lifeTimeCredit;
        
    }
    
    private User user;
    
	public TrekpointFetchRequest(int uid) {
		super(String.format("%s/getusercredits/%d", HOST, uid));
	}
	
	public TrekpointFetchRequest(User user) {
	    super("");
	    this.user = user;
	}
	
	@Override
	public Trekpoint execute(Context ctx) throws Exception {
		Trekpoint point = new Trekpoint();
		if(NEW_API){
		    UserLoginRequest req = new UserLoginRequest(user.getId(), user.getUsername(), user.getPassword());
		    req.invalidateCache(ctx);
		    User user = req.execute(ctx);
		    point.credit = user.getCredit();
		}else{
    		String response = executeFetchRequest(getURL(), ctx);
            JSONObject json  = new JSONArray(response).getJSONObject(0);
    		point.credit = json.optLong("CREDIT");
    		point.lifeTimeCredit = json.optLong("LIFETIMECREDIT");
		}
		return point;
	}

}
