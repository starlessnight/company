package com.smartrek.requests;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

public class IssueReportRequest extends Request {
	
	private String message;
	private String requestUrl;
	private String responseStatus;
	private String responseContent;
	
	public IssueReportRequest(User user, String message, String requestUrl, 
			String responseStatus, String responseContent) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.message = message;
        this.requestUrl = requestUrl;
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
            executeHttpRequest(Method.POST, getLinkUrl(Link.issue), params, ctx);
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
