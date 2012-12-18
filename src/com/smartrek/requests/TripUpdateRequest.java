package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONException;

import com.smartrek.utils.RecurringTime;


public class TripUpdateRequest extends UpdateRequest {
	
	/**
	 * Trip ID
	 */
	private int tid;
	
	/**
	 * User ID
	 */
	private int uid;
	
	/**
	 * Trip name
	 */
	private String name;
	
	/**
	 * Origin address ID
	 */
	private int oid;
	
	/**
	 * Destination address ID
	 */
	private int did;
	
	private RecurringTime recurringTime;
	
	public TripUpdateRequest(int tid, int uid, String name, int oid, int did, RecurringTime recurringTime) {
		this.tid = tid;
		this.uid = uid;
		this.name = name;
		this.oid = oid;
		this.did = did;
		this.recurringTime = recurringTime;
	}

	public void execute() throws IOException, JSONException {
		String url = String.format("%s/favroutes-update/?rid=%d&uid=%d&name=%s&oid=%d&did=%d&arrivaltime=%d:%d:00&datetype=%d",
				HOST, tid, uid, URLEncoder.encode(name), oid, did, recurringTime.getHour(), recurringTime.getMinute(), recurringTime.getWeekdays());
		executeUpdateRequest(url);
	}
}
