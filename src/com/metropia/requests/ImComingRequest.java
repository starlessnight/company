package com.metropia.requests;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.metropia.models.User;
import com.metropia.ui.timelayout.TimeColumn;
import com.metropia.utils.HTTP.Method;

public final class ImComingRequest extends AddRequest {

    private User user;
	
    private String to;
    private double lat;
    private double lon;
    private long eta;
    private double mile;
    private String destination;
    private int timzoneOffset;
    
	public ImComingRequest(User user, String to, double lat, double lon, long eta, double mile, 
	        String destination, int timzoneOffset) {
		this.user = user;
		this.to = to;
		this.lat = lat;
		this.lon = lon;
		this.eta = eta;
		this.mile = mile;
		this.destination = destination;
		this.timzoneOffset = timzoneOffset;
	}
	
	public void execute(Context ctx) throws IOException, InterruptedException {
        this.username = user.getUsername();
        this.password = user.getPassword();
        String url = getLinkUrl(Link.message);
        Map<String, String> params = new HashMap<String, String>();
        params.put("to", to);
        params.put("lat", String.valueOf(lat));
        params.put("lon", String.valueOf(lon));
        params.put("eta", TimeColumn.formatTime(eta, timzoneOffset));
        params.put("mile", new DecimalFormat("0.#").format(mile));
        params.put("destination", destination); 
        executeHttpRequest(Method.POST, url, params, ctx);
	}
}
