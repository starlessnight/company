package com.smartrek.requests;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public abstract class DeleteRequest extends Request {
	protected String executeDeleteRequest(String url, Context ctx) throws IOException, JSONException, InterruptedException {
		String response = executeHttpGetRequest(url, ctx).trim();
		
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
