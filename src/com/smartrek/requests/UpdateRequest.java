package com.smartrek.requests;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateRequest extends Request {
	protected String executeUpdateRequest(String url) throws IOException, JSONException {
		String response = executeHttpGetRequest(url).trim();
		
		// Since the server returns a JSON array for no apparent reason...
		response = response.substring(1, response.length()-1);
		
		JSONObject jsonObject = new JSONObject(response);
		String status = jsonObject.getString("STATUS");
		
		if (!status.equals("OK")) {
			if (jsonObject.has("MESSAGE")) {
				throw new IOException(jsonObject.getString("MESSAGE"));
			}
			else {
				throw new IOException("Invalid response: " + response);
			}
		}
		
		return response;
	}
}
