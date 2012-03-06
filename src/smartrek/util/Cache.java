package smartrek.util;

import java.util.Hashtable;

import android.text.format.Time;

/**
 * This class contains key-value pairs where key is a remote URL and a value
 * is a collection of the content and the metadata.
 *
 * TODO: We need some sort of garbage collection mechanism to clear off old
 *       data from 'storage'. 
 */
public final class Cache {
	
	public class Data {
		/**
		 * The local cache expires on this date/time
		 */
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
	
	public boolean isValid(Data data) {
		Time currentTime = new Time();
		currentTime.setToNow();
		
		return data != null && Time.compare(currentTime, data.expires) > 0;
	}
	
	public Object fetch(String url) {
		if(storage.containsKey(url)) {
			Data data = storage.get(url);
			
			if(isValid(data)) {
				return data.userdata;
			}
		}
		
		// TODO: Fetch and cache
		return null;
	}
}
