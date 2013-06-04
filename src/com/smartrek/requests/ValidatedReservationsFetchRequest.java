package com.smartrek.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.Reservation;
import com.smartrek.utils.Cache;

public class ValidatedReservationsFetchRequest extends FetchRequest<List<Reservation>> {
	
	public ValidatedReservationsFetchRequest(int uid) {
		super(String.format("%s/getreservationshistory/%d", FetchRequest.HOST, uid));
	}

	@Override
	public List<Reservation> execute(Context ctx) throws Exception {
	    // FIXME: Not going to use cache for now
	    Cache.getInstance(ctx).clear();
	    
		String response = executeFetchRequest(getURL(), ctx);
		
		List<Reservation> reservations = new ArrayList<Reservation>();
        JSONArray array = new JSONArray(response.replaceAll("\"DISTANCE\":,", "\"DISTANCE\":0,"));
        for (int i = 0; i < array.length(); i++) {
            Reservation r = Reservation.parse(new JSONObject(array.get(i).toString()));
            reservations.add(r);
        }
        
		return reservations;
	}

}
