package com.smartrek.utils;

import java.io.IOException;
import java.util.Hashtable;

import android.text.format.Time;
import android.util.Log;

/**
 * This class contains key-value pairs where key is a remote URL and a value
 * is a collection of the content and the metadata. Also, the caches are
 * intended to stay in memory (no use of persistent storage).
 *
 * TODO: We need some sort of garbage collection mechanism to clear off old
 *       data from 'storage'. 
 */
public final class Cache {
	
	public class Data {
		/**
		 * The local cache expires on this date/time
		 */
		// TODO: Use 'long' instead of 'Time'
		public Time expires;
		
		public Object userdata;
	}
	
	/**
	 * Time-to-live in terms of seconds
	 */
	public static final int TTL = 60*15;

	/**
	 * Singleton instance
	 */
	private static Cache instance;
	
	private Hashtable<String, Data> storage = new Hashtable<String, Data>();
	
	/**
	 * Explicit call of this constructor outside this class is prohibited.
	 */
	private Cache() {
		
	}
	
	/**
	 * Singleton
	 * 
	 * @return
	 */
	public static Cache getInstance() {
		if(instance == null) {
			instance = new Cache();
		}
		return instance;
	}
	
	public boolean isCacheAvailable(String url) {
		Data data = storage.get(url);
		
		return isValid(data);
	}
	
	public boolean isValid(Data data) {
		Time currentTime = new Time();
		currentTime.setToNow();
		
		return data != null && Time.compare(data.expires, currentTime) > 0;
	}
	
	public Object fetch(String url) throws IOException {
		Log.d("Cache", "url = " + url);
		if(storage.containsKey(url)) {
			Data data = storage.get(url);
			
			if(isValid(data)) {
				Log.d("Cache", "Fetching from cache (valid)");
				return data.userdata;
			}
			else {
				Log.d("Cache", "Removing from cache");
				storage.remove(url);
			}
		}
		
		Log.d("Cache", "Fetching from remote site");
		
		HTTP http = new HTTP(url);
		http.connect();
		
		int code = http.getResponseCode();
		if(code == 200) {
			// HTTP OK
			
			String body = http.getResponseBody();
			
			Data data = new Data();
			Time expire = new Time();
			expire.setToNow();
			expire.set(expire.toMillis(false) + TTL*1000);
			data.expires = expire;
			data.userdata = body;
			
			storage.put(url, data);
			
			return data.userdata;
		}
		else {
			throw new IOException(String.format("HTTP %d: %s", code, http.getResponseBody()));
		}
	}
}
