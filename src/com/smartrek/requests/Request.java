package com.smartrek.requests;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.apache.http.client.HttpResponseException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.smartrek.utils.Cache;
import com.smartrek.utils.HTTP;
import com.smartrek.utils.HTTP.Method;

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
	
	public static final String NEW_HOST = "http://www.smartrekmobile.com/api";
	
	public static final String IMG_HOST = "http://www.smartrekmobile.com";
	
	public static final String REDEEM_URL = "http://www.smartrekmobile.com/dashboard/rewards";
	
	public static final String ENTRYPOINT_URL = "http://sandbox.smartrekmobile.com/v1/rest/index.json";

	public static final String TIME_ZONE = "PST8PDT";
	
	public static final boolean NEW_API = true;
	
	public enum Link { 
	    query_upcoming_reservation,
	    query_username,
	    commute,
	    favorite_trip,
	    auth_user,
	    reservation,
	    address,
	    query_route,
	    trip
	}
	
	private static EnumMap<Link, String> linkUrls = new EnumMap<Link, String>(Link.class);
	
	/**
	 * Defines what a request can do
	 */
	public enum Verb {
		Fetch, Add, Update, Delete
	}
	
	protected String url;
	
	protected int responseCode;
	
	protected String username;
    
	protected String password;
	
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
	
	protected String executeHttpGetRequest(String url, Map<String, String> params) throws IOException {
		return executeHttpRequest(Method.GET, url, params);
	}
	
	protected String executeHttpRequest(Method method, String url) throws IOException {
	    return executeHttpRequest(method, url, (Object) null);
	}
	
	protected String executeHttpRequest(Method method, String url, 
            Map<String, String> params) throws IOException {
	    return executeHttpRequest(method, url, (Object) params);
    }
	
	protected String executeHttpRequest(Method method, String url, 
            JSONObject json) throws IOException {
        return executeHttpRequest(method, url, (Object) json);
    }
	
	private String executeHttpRequest(Method method, String url, 
	        Object params) throws IOException {
	    Log.d(LOG_TAG, "executeHttpRequest(): method=" + method + ", url="+url 
            + ", params=" + params);
        
        HTTP http = new HTTP(url);
        if(username != null && password != null){
            http.setAuthorization(username, password);
        }
        http.setMethod(method);
        if(params instanceof Map){
            http.setFormData((Map)params);
        }else if(params instanceof JSONObject){
            http.set((JSONObject)params);
        }
        http.connect();
        
        responseCode = http.getResponseCode();
        String responseBody = http.getResponseBody();
        
        if (responseCode == 200 || responseCode == 201 || responseCode == 204) {
            return responseBody;
        }
        else if(responseCode == 500 || responseCode == 400){
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
	public boolean isCached(Context ctx) {
		return Cache.getInstance(ctx).has(url);
	}
	
	/**
	 * Marks cached entry as invalid so that it gets re-fetched from the server.
	 */
	public void invalidateCache(Context ctx) {
		Cache.getInstance(ctx).invalidate(url);
	}
	
	public static void setLinkUrls(EnumMap<Link, String> linkUrls){
	    Request.linkUrls = linkUrls;
	}
	
	protected static String getLinkUrl(Link link){
	    String url;
	    if(link == Link.commute){
	        url = linkUrls.get(Link.favorite_trip);
	        if(url == null){
	            url = linkUrls.get(Link.commute);
	        }
	    }else{
	        url = linkUrls.get(link);
	    }
	    return url;
	}
	
}
