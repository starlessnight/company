package com.metropia.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.metropia.models.User;
import com.metropia.requests.IncidentRequest.Incident;

public class IncidentRequest extends FetchRequest<List<Incident>>{
	
	public static class Incident {
		public int type;
		public String shortDesc;
		public double lat;
		public double lon;
	}

	public IncidentRequest(User user, String url) {
		super(url);
		username = user.getUsername();
		password = user.getPassword();
	}

	@Override
	public List<Incident> execute(Context ctx) throws Exception {
		List<Incident> incidents = new ArrayList<Incident>();
		Exception e = null;
		String response;
		try {
			response = executeFetchRequest(getUrl(), ctx);
		}
		catch(IOException ioe) {
			e = ioe;
			response = ioe.getMessage();
		}
		if(e != null) {
			throw new Exception(new JSONObject(response).optString(ERROR_MESSAGE));
		}
		else {
			JSONArray responseArray = new JSONArray(response);
			for(int i = 0 ; i < responseArray.length() ; i++) {
				JSONObject incidentJSON = responseArray.optJSONObject(i);
				if("Y".equalsIgnoreCase(incidentJSON.optString("impacting"))) {
					Incident incident = new Incident();
					incident.type = incidentJSON.optInt("type");
					incident.lat = incidentJSON.optDouble("latitude");
					incident.lon = incidentJSON.optDouble("longitude");
					incident.shortDesc = incidentJSON.optString("ShortDesc");
					incidents.add(incident);
				}
			}
			return incidents;
		}
	}

}
