package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONException;

import com.smartrek.utils.datetime.RecurringTime;

public final class TripAddRequest extends AddRequest {

	private int uid;
	private String name;
	private int oid;
	private int did;
	private RecurringTime recurringTime;
	
	public TripAddRequest(int uid, String name, int oid, int did, RecurringTime recurringTime) {
		this.uid = uid;
		this.name = name;
		this.oid = oid;
		this.did = did;
		this.recurringTime = recurringTime;
	}
	
	public void execute() throws IOException, JSONException {
		String url = String.format("%s/V0.2/favroutes-add/?uid=%d&name=%s&oid=%d&did=%d&arrivaltime=%d:%d:00&datetype=%d",
				HOST, uid, URLEncoder.encode(name), oid, did, recurringTime.getHour(), recurringTime.getMinute(), recurringTime.getWeekdays());
		executeAddRequest(url);
	}
}
