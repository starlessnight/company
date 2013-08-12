package com.smartrek.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.json.JSONObject;

import com.smartrek.models.Trajectory;
import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

public class SendTrajectoryRequest extends Request {
	
    public void execute(User user, long rid, Trajectory trajectory) throws JSONException, ClientProtocolException, IOException {
        JSONObject params = new JSONObject();
        params.put("trajectory", trajectory.toJSON());
        this.username = user.getUsername();
        this.password = user.getPassword();
        String link = Request.getLinkUrl(Link.trajectory)
            .replaceAll("\\{reservation_id\\}", String.valueOf(rid));
        executeHttpRequest(Method.POST, link, params);
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
