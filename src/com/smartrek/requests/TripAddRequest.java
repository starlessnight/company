package com.smartrek.requests;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;
import com.smartrek.utils.datetime.RecurringTime;

public final class TripAddRequest extends AddRequest {

    private User user;
	private String name;
	private int oid;
	private int did;
	private RecurringTime recurringTime;
	
	public TripAddRequest(User user, String name, int oid, int did, RecurringTime recurringTime) {
		this.user = user;
		this.name = name;
		this.oid = oid;
		this.did = did;
		this.recurringTime = recurringTime;
	}
	
	public void execute() throws IOException, JSONException {
	    if(NEW_API){
	        this.username = user.getUsername();
            this.password = user.getPassword();
            String url = getLinkUrl(Link.commute);
            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", String.valueOf(user.getId()));
            params.put("name", name);
            params.put("origin_id", String.valueOf(oid));
            params.put("destination_id", String.valueOf(did));
            params.put("arrival_time", String.format("%02d:%02d:00", recurringTime.getHour() - 1, recurringTime.getMinute()));
            params.put("datetype", String.format("%d", recurringTime.getWeekdays()));
            executeHttpRequest(Method.POST, url, params);
	    }else{
    		String url = String.format("%s/V0.2/favroutes-add/?uid=%d&name=%s&oid=%d&did=%d&arrivaltime=%d:%d:00&datetype=%d",
    				HOST, user.getId(), URLEncoder.encode(name), oid, did, recurringTime.getHour(), recurringTime.getMinute(), recurringTime.getWeekdays());
    		executeAddRequest(url);
	    }
	}
}
