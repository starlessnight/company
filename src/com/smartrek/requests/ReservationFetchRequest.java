package com.smartrek.requests;

import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.Reservation;
import com.smartrek.models.User;
import com.smartrek.utils.Cache;

public class ReservationFetchRequest extends FetchRequest<Reservation> {
    
	public ReservationFetchRequest(User user, long rid) {
		super(buildUrl(rid));
	    username = user.getUsername();
	    password = user.getPassword();
	}

	@Override
	public Reservation execute(Context ctx) throws Exception {
	    // FIXME: Not going to use cache for now
	    Cache.getInstance(ctx).clear();
	    
		String response = executeFetchRequest(getURL(), ctx);
        JSONObject object = new JSONObject(response).getJSONObject("data");
        Reservation r = ReservationListFetchRequest.parse(object);
                
		return r;
	}
	
	private static String buildUrl(long rid){
	    return getLinkUrl(Link.reservation) + "/" +rid;
	}

}
