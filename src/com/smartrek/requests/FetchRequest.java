package com.smartrek.requests;

import java.io.IOException;

import com.smartrek.utils.Cache;


public abstract class FetchRequest<ReturnType> extends Request {
	
	public interface Listener<ReturnType> {
		void onFinish(ReturnType result);
	}

	protected Listener<ReturnType> listener;
	
	protected String url;
	
	/**
	 * @deprecated
	 */
	public FetchRequest() {
		
	}
	
	public FetchRequest(String url) {
		this.url = url;
	}
	
	public void setListener(Listener<ReturnType> listener) {
		this.listener = listener;
	}
	
	public String getURL() {
		return url;
	}
	
	public abstract ReturnType execute() throws Exception;
	
	protected String executeFetchRequest(String url) throws IOException {
		Cache cache = Cache.getInstance();
		if (cache.isCacheAvailable(url)) {
			return (String) cache.fetch(url);
		}
		else {
			String response = executeHttpGetRequest(url);
			cache.put(url, response);
			
			return response;
		}
	}
	
	/**
	 * Indicates whether the data is available in the local cache
	 * 
	 * @return
	 */
	public boolean isCached() {
		return Cache.getInstance().isCacheAvailable(url);
	}
	
	/**
	 * Marks cached entry as invalid so that it gets re-fetched from the server.
	 */
	public void invalidateCache() {
		Cache.getInstance().invalidate(url);
	}
}