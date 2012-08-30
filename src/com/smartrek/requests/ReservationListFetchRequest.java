package com.smartrek.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartrek.models.Reservation;

public class ReservationListFetchRequest extends FetchRequest<List<Reservation>> {
	
	public ReservationListFetchRequest(int uid) {
		super(String.format("%s/getreservations/%d", FetchRequest.HOST, uid));
	}

	@Override
	public List<Reservation> execute() throws Exception {
		String response = executeFetchRequest(getURL());
		
		List<Reservation> reservations = new ArrayList<Reservation>();
        JSONArray array = new JSONArray(response);
        for (int i = 0; i < array.length(); i++) {
            Reservation r = Reservation.parse(new JSONObject(array.get(i).toString()));
            reservations.add(r);
        }
        
		return reservations;
	}

}
