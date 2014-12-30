package com.metropia.requests;

import org.json.JSONObject;

import android.content.Context;

import com.metropia.models.Reservation;
import com.metropia.models.User;
import com.metropia.utils.Cache;

public class ReservationFetchRequest extends FetchRequest<Reservation> {
    
	public ReservationFetchRequest(User user, long rid) {
		this(user, buildUrl(rid));
	}
	
	public ReservationFetchRequest(User user, String url) {
        super(url);
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
