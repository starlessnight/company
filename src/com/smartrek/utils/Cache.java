package com.smartrek.utils;

import java.util.Hashtable;

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
		public long expires;
		
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
	
	public boolean has(String key) {
		return storage.containsKey(key) && isValid(storage.get(key));
	}
	
	public boolean isValid(Data data) {
		return data != null && data.expires > System.currentTimeMillis();
	}
	
	public void put(String key, Object value) {
		Data data = new Data();
		data.expires = System.currentTimeMillis() + TTL*1000;
		data.userdata = value;
		storage.put(key, data);
	}
	
	/**
	 * Marks a cache entry as invalid (removes the entry)
	 * 
	 * @param key
	 */
	public void invalidate(String key) {
		storage.remove(key);
	}
	
	/**
	 * Clears all cache
	 */
	public void clear() {
		storage.clear();
	}
	
	public Object fetch(String key) {
		Log.d("Cache", "url = " + key);
		if(storage.containsKey(key)) {
			Data data = storage.get(key);
			
			if(isValid(data)) {
				Log.d("Cache", "Fetching from cache (valid)");
				return data.userdata;
			}
			else {
				Log.d("Cache", "Removing from cache");
				storage.remove(key);
			}
		}
		
		return null;
	}
}
