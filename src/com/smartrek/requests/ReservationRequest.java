package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Time;

import com.smartrek.models.Route;
import com.smartrek.utils.RouteNode;

public class ReservationRequest extends Request {
	
	public ReservationRequest(Route route) {
		super();
		
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

		Time t = new Time(TIME_ZONE);
        t.set(route.getDepartureTime());
		
		url = String.format("%s/addreservations/?rid=%d&credits=%d&uid=%d&start_datetime=%s&estimatedTT=%d&origin_address=%s&destination_address=%s&route=%s",
				HOST,
				route.getId(), route.getCredits(), route.getUserId(),
				URLEncoder.encode(t.format("%Y-%m-%d %T")),
				route.getDuration() / 60, // Server requires this value in terms of minutes
				URLEncoder.encode(route.getOrigin()),
				URLEncoder.encode(route.getDestination()),
				URLEncoder.encode(new String(buf)));
	}
	
	public void execute() throws IOException, JSONException {
		String responseBody = executeHttpGetRequest(url);
		
		// FIXME: The following code is inherited from the old code so it might not comply with the new conventions
		// FIXME: This won't be necessary as long as server returns sensible HTTP status code
		if (responseBody.startsWith("[") && responseBody.endsWith("]")) {
		    JSONObject obj = new JSONObject(responseBody.substring(1, responseBody.length()-1));
		    String status = obj.getString("STATUS");
		    
		    if (status.equals("fail")) {
		        throw new IOException("Server side error (db989d9f)");
		    }
		}
	}
}
