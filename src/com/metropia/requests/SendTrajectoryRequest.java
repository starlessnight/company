package com.metropia.requests;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.metropia.activities.DebugOptionsActivity;
import com.metropia.activities.PassengerActivity;
import com.metropia.exceptions.SmarTrekException;
import com.metropia.models.Passenger;
import com.metropia.models.Trajectory;
import com.metropia.models.User;
import com.metropia.utils.HTTP.Method;

public class SendTrajectoryRequest extends Request {
	
	public SendTrajectoryRequest(boolean quickTimeout) {
		super();
		if(quickTimeout) {
			timeout = fifteenSecsTimeout;
		}
	}
	
    public void execute(User user, Trajectory trajectory, Context ctx) throws JSONException, ClientProtocolException, IOException, InterruptedException {
        JSONObject params = new JSONObject();
        params.put("trajectory", trajectory.toJSON());
        this.username = user.getUsername();
        this.password = user.getPassword();
        String link = Request.getLinkUrl(Link.activity);
        executeHttpRequest(Method.POST, link, params, ctx);
    }
    
    public ArrayList<Passenger> execute(User user, long rid, Trajectory trajectory, Context ctx, String mode) throws Exception {
        JSONObject params = new JSONObject();
        params.put("trajectory", trajectory.toJSON());
        this.username = user.getUsername();
        this.password = user.getPassword();
        Link link = mode.equals(PassengerActivity.PASSENGER_TRIP_VALIDATOR)? Link.passenger_trajectory:Link.trajectory;
        String url = Request.getLinkUrl(link).replaceAll("\\{reservation_id\\}", String.valueOf(rid));

        ArrayList<Passenger> passengers = new ArrayList<Passenger>(); 
        
        try{
        	String entryPoint = DebugOptionsActivity.getDebugEntrypoint(ctx);
        	boolean compress = entryPoint.contains("dev4_v1");
            String str = executeHttpRequest(Method.POST, url, params, compress, ctx);

            if (str==null) return passengers;
            JSONObject json = new JSONObject(str);
            JSONArray names = json.getJSONObject("data").getJSONArray("o_users_names");
            JSONArray photos = json.getJSONObject("data").getJSONArray("o_users_pic");
            for (int i=0 ; i<names.length() ; i++) {
            	passengers.add(new Passenger(names.getString(i), photos.getString(i)));
            }
            
            return passengers;
            
        }catch(Exception e){
            if(responseCode >= 400 && responseCode <= 499){
                throw new SmarTrekException(responseCode);
            }else{
                throw e;
            }
        }
    }
    @Deprecated
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
