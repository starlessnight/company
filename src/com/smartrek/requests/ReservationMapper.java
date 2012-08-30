package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.SimpleFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Time;
import android.util.Log;
import android.util.TimeFormatException;

import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.utils.HTTP;
import com.smartrek.utils.RouteNode;

public final class ReservationMapper extends FetchRequest {
	
	public void reserveRoute(Route route) throws IOException, JSONException {
		// TODO: Better way to handle this?
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		
		List<RouteNode> nodes = route.getNodes();
		for (RouteNode node : nodes) {
			buf.append(node.toJSON());
			buf.append(",");
		}
		buf.deleteCharAt(buf.length()-1);
		buf.append("]");

		Time t = new Time();
		t.set(route.getDepartureTime());
		
		String url = String.format("%s/addreservations/?rid=%d&credits=%d&uid=%d&start_datetime=%s&estimatedTT=%d&origin_address=%s&destination_address=%s&route=%s",
				HOST,
				route.getId(), route.getCredits(), route.getUserId(),
				URLEncoder.encode(t.format("%Y-%m-%d %T")),
				route.getDuration(),
				URLEncoder.encode(route.getOrigin()),
				URLEncoder.encode(route.getDestination()),
				URLEncoder.encode(new String(buf)));
		
		Log.d("ReservationMapper", "reserveRoute: " + url);
		
		HTTP http = new HTTP(url);
		http.connect();
		
		int responseCode = http.getResponseCode();
		String responseBody = http.getResponseBody();
		if (responseCode == 200) {
			Log.d("ReservationMapper", "reserveRoute: HTTP response: " + responseBody);
			
			// FIXME: This won't be necessary as long as server returns sensible HTTP status code
			if (responseBody.startsWith("[") && responseBody.endsWith("]")) {
			    JSONObject obj = new JSONObject(responseBody.substring(1, responseBody.length()-1));
			    String status = obj.getString("STATUS");
			    
			    if (status.equals("fail")) {
			        throw new IOException("Server side error (db989d9f)");
			    }
			}
			
		}
		else {
			throw new IOException(String.format("HTTP %d - %s", responseCode, responseBody));
		}
		
	}
	
	public void reportValidation(int uid, int rid) throws IOException {
		String url = String.format("%s/validationdone/?uid=%d&rid=%d", HOST, uid, rid);
		
		HTTP http = new HTTP(url);
		http.connect();
		
		int responseCode = http.getResponseCode();
		if (responseCode == 200) {
			Log.d("ReservationMapper", "finishValidation: HTTP response: " + http.getResponseBody());
		}
		else {
			throw new IOException(String.format("HTTP %d - %s", responseCode, http.getResponseBody()));
		}
	}

	@Override
	public Object execute() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
