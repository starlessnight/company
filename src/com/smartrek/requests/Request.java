package com.smartrek.requests;

import java.io.IOException;
import java.util.Map;

import android.util.Log;

import com.smartrek.utils.HTTP;

/**
 * A request is a unit sent to the server to perform a certain task such as
 * fetch a single entity, fetch multiple entities, add, update, or delete an
 * entity.
 * 
 * @author Sumin Byeon
 * 
 */
public abstract class Request {
	
	public static final String HOST = "http://50.56.81.42:8080";

	/**
	 * Defines what a request can do
	 */
	public enum Verb {
		Fetch, Add, Update, Delete
	}
	
	public void setParameter(String key, Object value) {
		
	}
	
	protected String executeHttpGetRequest(String url) throws IOException {
		return executeHttpGetRequest(url, null);
	}
	
	protected String executeHttpGetRequest(String url, Map<String, Object> params) throws IOException {
		Log.d("Request", "executeHttpGetRequest(): url="+url);
		
		HTTP http = new HTTP(url);
		http.connect();
		
		int responseCode = http.getResponseCode();
		String responseBody = http.getResponseBody();
		
		if (responseCode == 200) {
			return responseBody;
		}
		else {
			throw new IOException(String.format("HTTP %d: %s", responseCode, responseBody));
		}
		
	}
	
	public String executeHttpPostRequest(String url, Map<String, Object> params) {
		return null;
	}
}
