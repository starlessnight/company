package com.smartrek.requests;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.smartrek.activities.DebugOptionsActivity;
import com.smartrek.models.User;
import com.smartrek.utils.Cache;
import com.smartrek.utils.HTTP;
import com.smartrek.utils.HTTP.Method;
import com.smartrek.utils.Misc;

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
	
	public static final String ENTRYPOINT_URL = "https://api.metropia.com";
	
	public static String getTimeZone(){
	    return TimeZone.getDefault().getID();
	}
	
	public static String getTimeZone(int offset){
	    return "GMT" + (offset < 0?"-":"+") + Math.abs(offset);
	}
	
	public static final String UTC_TIMEZONE = "UTC";
	
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
	    trip,
	    trajectory,
	    message,
	    achievement,
	    reward,
	    claim,
	    activity,
	    city,
	    where_to_go,
	    route, 
	    search, 
	    reverse_geocoding, 
	    issue
	}
	
	public enum Page { 
	    feedback,
	    reset_password,
	    eula,
	    rewards,
	    my_metropia,
	    privacy_policy, 
	    trips, 
	    co2_saving, 
	    time_saving
    }
	
	public enum Setting {
	    activity_distance_interval,
	    gps_accuracy,
	    tile,
	    reroute_after_N_deviated_samples,
	    reroute_trigger_distance_in_meter,
	    remaining_percentage_to_trigger_OMW_message,
	    intersection_radius_in_meter
	}
	
	private static EnumMap<Link, String> linkUrls = new EnumMap<Link, String>(Link.class);
	
	private static EnumMap<Page, String> pageUrls = new EnumMap<Page, String>(Page.class);
	
	private static EnumMap<Setting, Object> settings = new EnumMap<Setting, Object>(Setting.class);
	
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
	
	protected String executeHttpGetRequest(String url, Context ctx) throws IOException, InterruptedException {
		return executeHttpGetRequest(url, null, ctx);
	}
	
	protected String executeHttpGetRequest(String url, Map<String, String> params, Context ctx) throws IOException, InterruptedException {
		return executeHttpRequest(Method.GET, url, params, ctx);
	}
	
	protected String executeHttpRequest(Method method, String url, Context ctx) throws IOException, InterruptedException {
	    return executeHttpRequest(method, url, (Object) null, ctx);
	}
	
	protected String executeHttpRequest(Method method, String url, 
            Map<String, String> params, Context ctx) throws IOException, InterruptedException {
	    return executeHttpRequest(method, url, (Object) params, ctx);
    }
	
	protected String executeHttpRequest(Method method, String url, 
            JSONObject json, Context ctx) throws IOException, InterruptedException {
        return executeHttpRequest(method, url, (Object) json, ctx);
    }
	
	public static final int fifteenSecsTimeout = 15 * 1000;
	
	protected int timeout = HTTP.defaultTimeout;
	
	private String executeHttpRequest(Method method, String url, 
	        Object params, final Context ctx) throws IOException {
	    Log.d(LOG_TAG, "executeHttpRequest(): method=" + method + ", url="+url 
            + ", params=" + params);
        String responseBody = null;
	    try{
            HTTP http = new HTTP(url);
            http.setTimeout(timeout);
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
            responseBody = http.getResponseBody();
            
            if(DebugOptionsActivity.isHttp4xx5xxLogEnabled(ctx) && responseCode >= 400 && responseCode <= 599){
                FileUtils.writeStringToFile(getHttp4xx5xxLogFile(ctx), url + "\n\nHTTP " + responseCode + "\n\n" + responseBody);
            }
            
            if (responseCode == 200 || responseCode == 201 || responseCode == 204) {
                return responseBody;
            }
            else if(responseCode == 500 || responseCode == 400){
                throw new HttpResponseException(responseCode, responseBody);
            }
            else {
                throw new IOException(String.format("HTTP %d: %s", responseCode, responseBody));
            }
	    }catch(Throwable t){
	    	String detailMessage = url;
	    	if(StringUtils.isNotBlank(getLinkUrl(Link.issue)) && !StringUtils.equalsIgnoreCase(getLinkUrl(Link.issue), url)) {
	    		sendIssueReport(ctx, url, params, t.getMessage(), responseCode, responseBody);
	    		try {
					detailMessage = formatErrorMessage(url, params, t.getMessage(), responseCode, responseBody);
				} catch (JSONException e1) {
					detailMessage = responseBody;
				}
	    	}
	        IOException e = new IOException(detailMessage);
	        e.initCause(t);
	        throw e;
	    }
	}
	
	private static void sendIssueReport(final Context ctx, final String url, final Object reqParams, final String message,
			final int responseCode, final String responseBody) {
		Misc.parallelExecute(new AsyncTask<Void, Void, Void> () {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					IssueReportRequest issue = new IssueReportRequest(User.getCurrentUser(ctx), 
							message, url, reqParams, responseCode+"", responseBody);
					issue.execute(ctx);
				}
				catch(Exception ignore) {}
				return null;
			}
		});
	}
	
	public static final String RESPONSE = "response";
	public static final String ERROR_MESSAGE = "errorMessage";
	
	private String formatErrorMessage(String url, final Object reqParams, final String message,
			final int responseCode, final String responseBody) throws JSONException {
		JSONObject errorJson = new JSONObject();
		errorJson.put(RESPONSE, responseBody);
		StringBuffer detailMessage = new StringBuffer();
		detailMessage.append("Error Message=").append(message).append("\n");
		detailMessage.append("Request Url=").append(url).append("\n");
		detailMessage.append("Response Status=").append(responseCode + "\n");
		detailMessage.append("Response Content=").append(responseBody).append("\n");
		detailMessage.append("Request Parameter=").append(String.valueOf(reqParams)).append("\n");
		errorJson.put(ERROR_MESSAGE, detailMessage);
		return errorJson.toString();
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
	
	public static boolean hasLinkUrls(){
        return !Request.linkUrls.isEmpty();
    }
	
	public static boolean hasSettings() {
		return !Request.settings.isEmpty();
	}
	
	public static void setPageUrls(EnumMap<Page, String> pageUrls){
        Request.pageUrls = pageUrls;
    }
	
	public static String getPageUrl(Page page){
        return pageUrls.get(page);
	}
	
	public static void setSettings(EnumMap<Setting, Object> settings){
        Request.settings = settings;
    }
    
    public static Object getSetting(Setting setting){
        return settings.get(setting);
    }
    
    public static Long getActivityDistanceInterval(){
        Object interval = settings.get(Setting.activity_distance_interval);
        return interval == null?null:Long.valueOf(interval.toString());
    }

    public String getUrl() {
        return url;
    }
    
    private static File getHttp4xx5xxLogDir(Context ctx){
        return new File(ctx.getExternalFilesDir(null), "http_4xx_5xx_api_responses");
    }
    
    private static File getHttp4xx5xxLogFile(Context ctx){
        return new File(getHttp4xx5xxLogDir(ctx), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(new Date()));
    }
	
}
