package com.smartrek.requests;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import com.smartrek.models.Trajectory;
import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

public class SendTrajectoryRequest extends Request {
	
    public void execute(User user, String link, long rid, Trajectory trajectory) throws JSONException, ClientProtocolException, IOException {
        SimpleDateFormat idFmt = new SimpleDateFormat("yyyyMMdd/HH/mmss");
        idFmt.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        String url = link.replaceAll("\\{id\\}", idFmt.format(new Date(rid)));
        SimpleDateFormat fieldFmt = new SimpleDateFormat("yyyyMMddHHmmss");
        fieldFmt.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        Map<String, String> params = new HashMap<String, String>();
        params.put("trajectorybatch_" + fieldFmt.format(new Date()), trajectory.toJSON().toString());
        this.username = user.getUsername();
        this.password = user.getPassword();
        executeHttpRequest(Method.PUT, url, params);
    }
    
	public void execute(int seq, int uid, long rid, Trajectory trajectory) throws JSONException, ClientProtocolException, IOException {
		String url = String.format("%s/sendtrajectory/", HOST);
		//String url = "http://192.168.0.21:7787/";
		// GPS Points (Lat/ Lon / Altitude (ft) / Heading / Timestamp / Speed (mph)
		
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>(4);
		params.add(new BasicNameValuePair("uid", String.valueOf(uid)));
		params.add(new BasicNameValuePair("rid", String.valueOf(rid)));
		params.add(new BasicNameValuePair("GPSPoints", trajectory.toJSON().toString()));
		
		// FIXME: This is a temporary solution. Web service won't accept encoded string.
		String entity = String.format("seq=%d&uid=%d&rid=%d&GPSPoints=%s", seq, uid, rid, trajectory.toJSON().toString());
		
		httpPost.setEntity(new StringEntity(entity));
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpPost);
		StatusLine responseLine = response.getStatusLine();
		int statusCode = responseLine.getStatusCode();
		if (statusCode == 200) {
			trajectory.clear();
		}
		else {
			throw new IOException(String.format("HTTP %d: %s", statusCode, responseLine.getReasonPhrase()));
		}
	}
	
}
