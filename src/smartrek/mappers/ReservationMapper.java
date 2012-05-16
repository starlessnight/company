package smartrek.mappers;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import smartrek.models.Route;
import smartrek.util.HTTP;
import smartrek.util.RouteNode;
import android.util.Log;

public final class ReservationMapper extends Mapper {

	public List<Route> getReservations(int uid) {
		return null;
	}
	
	public void reserveRoute(Route route) throws IOException {
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
		
		String url = String.format("%s/addreservations/?rid=%d&credits=%d&uid=%d&start_datetime=%s&end_datetime=%s&origin_address=%s&destination_address=%s&route=%s",
				host,
				route.getId(), route.getCredits(), route.getUserId(),
				URLEncoder.encode(route.getDepartureTime().format("%Y-%m-%d %T")),
				URLEncoder.encode(route.getArrivalTime().format("%Y-%m-%d %T")),
				URLEncoder.encode(route.getOrigin()),
				URLEncoder.encode(route.getDestination()),
				URLEncoder.encode(new String(buf)));
		
		Log.d("ReservationMapper", url);
		
		HTTP http = new HTTP(url);
		http.connect();
		
		int responseCode = http.getResponseCode();
		if (responseCode == 200) {
			Log.d("ReservationMapper", "HTTP response: " + http.getResponseBody());
		}
		else {
			throw new IOException(String.format("HTTP %d - %s", responseCode, http.getResponseBody()));
		}
		
	}

}
