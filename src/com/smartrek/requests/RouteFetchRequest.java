package com.smartrek.requests;

import java.util.List;

import android.text.format.Time;

import com.smartrek.models.Route;
import com.smartrek.utils.GeoPoint;

public class RouteFetchRequest extends FetchRequest<List<Route>> {

	public RouteFetchRequest(GeoPoint origin, GeoPoint destination, Time time) {
		super(String.format("%s/getroutes/startlat=%f%%20startlon=%f%%20endlat=%f%%20endlon=%f%%20departtime=%d:%02d",
				HOST, origin.getLatitude(), origin.getLongitude(),
				destination.getLatitude(), destination.getLongitude(),
				time.hour, time.minute));
	}
	
	public List<Route> execute() {
		return null;
	}
}
