package com.smartrek.requests;

import java.io.IOException;

import android.content.Context;

import com.smartrek.utils.Cache;


public abstract class FetchRequest<ReturnType> extends Request {
	
	public interface Listener<ReturnType> {
		void onFinish(ReturnType result);
	}
	
	protected FetchRequest(String url) {
		super(url);
	}

	protected Listener<ReturnType> listener;
	
	public void setListener(Listener<ReturnType> listener) {
		this.listener = listener;
	}
	
	public String getURL() {
		return url;
	}
	
	public abstract ReturnType execute(Context ctx) throws Exception;
	
	protected String executeFetchRequest(String url, Context ctx) throws IOException {
		Cache cache = Cache.getInstance(ctx);
		if (cache.has(url)) {
			return (String) cache.fetch(url);
		}
		else {
			String response = executeHttpGetRequest(url);
			cache.put(url, response);
			
			return response;
		}
	}
}