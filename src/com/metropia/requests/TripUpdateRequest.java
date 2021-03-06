package com.metropia.requests;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import android.content.Context;

import com.metropia.models.User;
import com.metropia.utils.HTTP.Method;
import com.metropia.utils.datetime.RecurringTime;


public class TripUpdateRequest extends UpdateRequest {
	
	/**
	 * Trip ID
	 */
	private int tid;
	
	private User user;
	
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
	
	private String link;
	
	public TripUpdateRequest(String link, int tid, User user, String name, int oid, int did, RecurringTime recurringTime) {
		this.tid = tid;
		this.user = user;
		this.name = name;
		this.oid = oid;
		this.did = did;
		this.recurringTime = recurringTime;
		this.link = link;
	}

	public void execute(Context ctx) throws IOException, JSONException, InterruptedException {
	    if(NEW_API){
	        this.username = user.getUsername();
            this.password = user.getPassword();
            url = link.replaceAll("\\{id\\}", String.valueOf(tid));
            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", String.valueOf(user.getId()));
            params.put("name", name);
            params.put("origin_id", String.valueOf(oid));
            params.put("destination_id", String.valueOf(did));
            params.put("arrival_time", String.format("%02d:%2d:00", recurringTime.getHour() % 24, recurringTime.getMinute()));
            params.put("datetype", String.format("%d", recurringTime.getWeekdays()));
            executeHttpRequest(Method.PUT, url, params, ctx);
	    }else{
    		String url = String.format("%s/favroutes-update/?rid=%d&uid=%d&name=%s&oid=%d&did=%d&arrivaltime=%d:%d:00&datetype=%d",
    				HOST, tid, user.getId(), URLEncoder.encode(name), oid, did, recurringTime.getHour(), recurringTime.getMinute(), recurringTime.getWeekdays());
    		executeUpdateRequest(url, ctx);
	    }
	}
}
