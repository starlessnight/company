package com.smartrek.requests;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

public class IssueReportRequest extends Request {
	
	private String message;
	private String requestUrl;
	private String responseStatus;
	private String responseContent;
	private Object reqParams;
	
	public IssueReportRequest(User user, String message, String requestUrl, Object params,
			String responseStatus, String responseContent) {
	    if(user != null){
            this.username = user.getUsername();
            this.password = user.getPassword();
	    }
        this.message = message;
        this.requestUrl = requestUrl;
        this.reqParams = params;
        this.responseStatus = responseStatus;
        this.responseContent = responseContent;
	}
	
	public void execute(Context ctx) {
		try{
	        Map<String, String> params = new HashMap<String, String>();
	        params.put("username", this.username);
	        params.put("os", "android");
	        params.put("app_version", ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName);
	        params.put("message", message==null?"":message);
	        params.put("request_url", requestUrl==null?"":requestUrl);
	        params.put("response_status", responseStatus==null?"":responseStatus);
	        params.put("response_content", responseContent==null?"":responseContent);
	        JSONObject jsonParam = null;
	        if(reqParams instanceof Map) {
	        	jsonParam = new JSONObject((Map)reqParams);
	        }
	        else if(reqParams instanceof JSONObject) {
	        	jsonParam = (JSONObject)reqParams;
	        }
	        if(jsonParam != null) {
	        	params.put("request_parameters", jsonParam.toString());
	        }
            executeHttpRequest(Method.POST, StringUtils.defaultString(getLinkUrl(Link.issue)), params, ctx);
        }catch(Exception e){
            Log.w("IssueReportRequest", Log.getStackTraceString(e));
        }
	}
	
	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		toString.append("username : ").append(this.username);
		toString.append(", os : android").append(", app_version : 0.10.9");
		toString.append(", message : ").append(message);
		toString.append(", request_url : ").append(requestUrl);
		toString.append(", response_status : ").append(responseStatus);
		toString.append(", response_content : ").append(responseContent);
		return toString.toString();
	}
}
