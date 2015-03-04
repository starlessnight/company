package com.metropia.requests;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.metropia.models.User;
import com.metropia.requests.IncidentRequest.Incident;

public class IncidentRequest extends FetchRequest<List<Incident>> {

	public static class Incident {
		public int type;
		public String shortDesc;
		public double lat;
		public double lon;
		public Date startTime;
		public Date endTime;
		public int severity;
		
		public boolean isInTimeRange(long departureUTCTime) {
			return departureUTCTime >= startTime.getTime() && departureUTCTime <= endTime.getTime();
		}
		
		public int getMinimalDisplayZoomLevel() {
			switch(severity) {
				case 2 :
					return 10;
				case 1 : 
					return 13;
				default : 
					return 9;
			}
		}
	}

	public IncidentRequest(User user, String url) {
		super(url);
		username = user.getUsername();
		password = user.getPassword();
	}
	
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	@Override
	public List<Incident> execute(Context ctx) throws Exception {
		List<Incident> incidents = new ArrayList<Incident>();
		Exception e = null;
		String response;
		try {
			response = executeFetchRequest(getUrl(), ctx);
		} 
		catch (IOException ioe) {
			e = ioe;
			response = ioe.getMessage();
		}
		
		if (e != null) {
			throw new Exception(new JSONObject(response).optString(ERROR_MESSAGE));
		} 
		else {
			JSONArray responseArray = new JSONArray(response);
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			for (int i = 0; i < responseArray.length(); i++) {
				try {
					JSONObject incidentJSON = responseArray.optJSONObject(i);
					if ("Y".equalsIgnoreCase(incidentJSON.optString("impacting"))) {
						Incident incident = new Incident();
						incident.type = incidentJSON.optInt("metropia_type");
						incident.lat = incidentJSON.optDouble("latitude");
						incident.lon = incidentJSON.optDouble("longitude");
						incident.shortDesc = incidentJSON.optString("ShortDesc");
						incident.startTime = df.parse(incidentJSON.optString("startTime"));
						incident.endTime = df.parse(incidentJSON.optString("endTime"));
						incident.severity = incidentJSON.optInt("severity");
						incidents.add(incident);
					}
				}
				catch(Exception ignore) {
					Log.d("IncidentRequest", ignore.getMessage());
				}
			}
			return incidents;
		}
	}

}
