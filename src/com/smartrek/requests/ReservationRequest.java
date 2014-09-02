package com.smartrek.requests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.activities.DebugOptionsActivity.NavigationLink;
import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;
import com.smartrek.utils.RouteNode;

public class ReservationRequest extends Request {
    
    private SimpleDateFormat dateFormat;
    private User user;
    private Date now;
    private long rid;
    private int credits;
    private int uid;
    private int duration;
    private String origin;
    private String destination;
    private String routeBuf;
    private String version;
    private String navUrl;
    private String routeJSON;
    
    private long rescheduleId;
    
	public ReservationRequest(User user, Route route, String version, long rescheduleId) {
		super();
		
		this.rescheduleId = rescheduleId;
		
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

		this.user = user;
        now = new Date(route.getDepartureTime());
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone(getTimeZone()));
        
        rid = route.getId();
        credits = route.getCredits();
        uid = route.getUserId();
        duration = route.getDuration() / 60;
        origin = route.getOrigin();
        destination = route.getDestination();
        routeBuf = new String(buf);
        this.version = version;
        NavigationLink navLink = route.getLink();
        navUrl = navLink == null?null:navLink.url;
        if(NEW_API){
            url = getLinkUrl(Link.reservation) + (rescheduleId > 0?("/" + rescheduleId):"");
            this.routeJSON = route.getRawJSON();
        }else{
            url = String.format("%s/V0.2/addreservations/?rid=%d&credits=%d&uid=%d&start_datetime=%s&estimatedTT=%d&origin_address=%s&destination_address=%s&route=%s&version=%s",
    				HOST,
    				rid, credits, uid,
    				URLEncoder.encode(dateFormat.format(now)),
    				duration, // Server requires this value in terms of minutes
    				URLEncoder.encode(origin),
    				URLEncoder.encode(destination),
    				URLEncoder.encode(routeBuf),
    				version);
        }
	}
	
	public Long execute(Context ctx) throws Exception {
	    Long id = null;
	    if(NEW_API){
	        this.username = user.getUsername();
	        this.password = user.getPassword();
	        JSONObject params = new JSONObject(routeJSON);
	        String idStr = params.getString("id");
            String separator = "_";
	        params.put("id", StringUtils.contains(idStr, separator)?
                StringUtils.substringAfter(idStr, separator):idStr);
	        String originEncoded = StringUtils.defaultString(origin, "");
	        String destEncoded = StringUtils.defaultString(destination, "");
	        try {
	        	originEncoded = URLEncoder.encode(originEncoded, "utf-8");
	        	destEncoded = URLEncoder.encode(destEncoded, "utf-8");
	        }
	        catch(UnsupportedEncodingException ignore){}
            params.put("origin", originEncoded);
            params.put("destination", destEncoded);
            params.put("trajectory_fields", "lat,lon,altitude,heading,timestamp,speed,link,accuracy");
            String res = null;
            try {
                res = executeHttpRequest(rescheduleId > 0?Method.PUT:Method.POST, url, params, ctx);
            } catch (Exception e){
                res = e.getMessage();
            }
            if(rescheduleId!=0) {
            	id = rescheduleId;
            }
            else {
	            JSONObject json = new JSONObject(res);
	            JSONObject data = json.getJSONObject("data");
	            if("fail".equals(json.getString("status"))){
	                String msg = "";
	                Iterator keys = data.keys();
	                while(keys.hasNext()){
	                    Object attr = keys.next();
	                    msg += (msg.length() == 0?"":".\n") + attr +  ": " + data.getString(attr.toString());
	                }
	                throw new Exception(msg);
	            }else{
	                id = data.getLong("id");
	            }
            }
	    }else{
    		String responseBody = executeHttpGetRequest(url, ctx);
    		
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
	    return id;
	}
}
