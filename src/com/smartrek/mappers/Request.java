package com.smartrek.mappers;

import java.io.IOException;
import java.util.Map;

import com.smartrek.utils.ExceptionHandlingService;
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
	
	public String executeGetRequest(String url) throws IOException {
		return executeGetRequest(url, null);
	}
	
	public String executeGetRequest(String url, Map<String, Object> params) throws IOException {
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
}
