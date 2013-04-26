package com.smartrek.requests;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.HttpResponseException;

import android.util.Log;

import com.smartrek.utils.Cache;
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
	
	public static final String LOG_TAG = "Request";
	
	public static final String HOST = "http://portal.smartrekmobile.com:8080";

	public static final String TIME_ZONE = "PST8PDT";
	
	/**
	 * Defines what a request can do
	 */
	public enum Verb {
		Fetch, Add, Update, Delete
	}
	
	protected String url;
	
	protected int responseCode;
	
	protected Request() {
		
	}
	
	protected Request(String url) {
		this.url = url;
	}
	
	public void setParameter(String key, Object value) {
		
	}
	
	protected String executeHttpGetRequest(String url) throws IOException {
		return executeHttpGetRequest(url, null);
	}
	
	protected String executeHttpGetRequest(String url, Map<String, Object> params) throws IOException {
		Log.d(LOG_TAG, "executeHttpGetRequest(): url="+url);
		
		HTTP http = new HTTP(url);
		http.connect();
		
		responseCode = http.getResponseCode();
		String responseBody = http.getResponseBody();
		
		if (responseCode == 200) {
			return responseBody;
		}
		else if(responseCode == 500){
		    throw new HttpResponseException(responseCode, responseBody);
		}
		else {
			throw new IOException(String.format("HTTP %d: %s", responseCode, responseBody));
		}
		
	}
	
	public String executeHttpPostRequest(String url, Map<String, Object> params) {
		return null;
	}
	
	/**
	 * Indicates whether the data is available in the local cache
	 * 
	 * @return
	 */
	public boolean isCached() {
		return Cache.getInstance().has(url);
	}
	
	/**
	 * Marks cached entry as invalid so that it gets re-fetched from the server.
	 */
	public void invalidateCache() {
		Cache.getInstance().invalidate(url);
	}
	
}
