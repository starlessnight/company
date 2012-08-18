package com.smartrek.requests;

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
	
	public ReturnType execute() {
		return null;
	}
	
	protected String executeFetchRequest(String url) {
		Cache cache = Cache.getInstance();
		if (cache.isCacheAvailable(url)) {
			
		}
		else {
			
		}
		
		return null;
	}
	
	/**
	 * Indicates whether the data is available in the local cache
	 * 
	 * @return
	 */
	public boolean isCached() {
		return false;
	}
}