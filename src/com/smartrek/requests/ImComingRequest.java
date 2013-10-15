package com.smartrek.requests;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

public final class ImComingRequest extends AddRequest {

    private User user;
	
    private String to;
    private double lat;
    private double lon;
    private long eta;
    private double mile;
    private String destination;
    
	public ImComingRequest(User user, String to, double lat, double lon, long eta, double mile, String destination) {
		this.user = user;
		this.to = to;
		this.lat = lat;
		this.lon = lon;
		this.eta = eta;
		this.mile = mile;
		this.destination = destination;
	}
	
	public void execute() throws IOException {
        this.username = user.getUsername();
        this.password = user.getPassword();
        String url = getLinkUrl(Link.message);
        SimpleDateFormat dateFmt = new SimpleDateFormat("HH:mm");
        dateFmt.setTimeZone(TimeZone.getTimeZone(getTimeZone()));
        Map<String, String> params = new HashMap<String, String>();
        params.put("to", to);
        params.put("lat", String.valueOf(lat));
        params.put("lon", String.valueOf(lon));
        params.put("eta", dateFmt.format(new Date(eta)));
        params.put("mile", String.valueOf(Double.valueOf(mile).longValue()));
        params.put("destination", destination); 
        executeHttpRequest(Method.POST, url, params);
	}
}
