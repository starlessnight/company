package com.smartrek.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.models.Reservation;
import com.smartrek.models.User;

public class ValidatedReservationsFetchRequest extends FetchRequest<List<Reservation>> {
	
	public ValidatedReservationsFetchRequest(int uid) {
		super(String.format("%s/getreservationshistory/%d", FetchRequest.HOST, uid));
	}
	
	public ValidatedReservationsFetchRequest(User user) {
	    super(getLinkUrl(Link.trip).replaceAll("\\{user_id\\}", String.valueOf(user.getId())));
	    this.username = user.getUsername();
	    this.password = user.getPassword();
	}

	@Override
	public List<Reservation> execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx)
	        .replaceAll("\"DISTANCE\":,", "\"DISTANCE\":0,");
		JSONArray array;
        if(NEW_API){
            array = new JSONObject(response).getJSONArray("data");
        }else{
            array = new JSONArray(response);
        }
		List<Reservation> reservations = new ArrayList<Reservation>();
        for (int i = 0; i < array.length(); i++) {
            Reservation r = Reservation.parse(new JSONObject(array.get(i).toString()));
            reservations.add(r);
        }
        
		return reservations;
	}

}
