package com.metropia.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import org.apache.commons.io.FileUtils;

import android.content.Context;
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
    	
	/**
	 * Time-to-live in terms of seconds
	 */
	public static final int TTL = 60*15;
	
	private Context ctx;
	
	/**
	 * Explicit call of this constructor outside this class is prohibited.
	 */
	private Cache() {
		
	}
	
	public static Cache getInstance(Context ctx) {
		Cache instance = new Cache();
		instance.ctx =ctx;
		return instance;
	}
	
	public boolean has(String key) {
		return containsKey(key) && isValid(get(key));
	}
	
	public void put(String key, String data) {
	    File file = getCacheFile(ctx, key);
        try{
            FileUtils.writeStringToFile(file, data);
        }catch(Throwable t){
            FileUtils.deleteQuietly(file);
        }
	}
	
	public void put(String key, InputStream data) {
        File file = getCacheFile(ctx, key);
        try{
            FileUtils.copyInputStreamToFile(data, file);
        }catch(Throwable t){
            FileUtils.deleteQuietly(file);
        }
    }
	
	/**
	 * Marks a cache entry as invalid (removes the entry)
	 * 
	 * @param key
	 */
	public void invalidate(String key) {
		remove(key);
	}
	
	/**
	 * Clears all cache
	 */
	public void clear() {
	    try {
            FileUtils.cleanDirectory(ctx.getCacheDir());
        }
        catch (IOException e) {}
	}
	
	public InputStream fetchStream(String key) {
        Log.d("Cache", "url = " + key);
        if(containsKey(key)) {
            File data = get(key);
            if(isValid(data)) {
                Log.d("Cache", "Fetching from cache (valid)");
                InputStream val = null;
                try{
                    val = FileUtils.openInputStream(data);
                }catch(IOException e){}
                return val;
            }
            else {
                Log.d("Cache", "Removing from cache");
                remove(key);
            }
        }
        
        return null;
    }
	
	public String fetch(String key) {
		Log.d("Cache", "url = " + key);
		if(containsKey(key)) {
			File data = get(key);
			if(isValid(data)) {
				Log.d("Cache", "Fetching from cache (valid)");
				String val = null;
                try{
                    val = FileUtils.readFileToString(data);
                }catch(IOException e){}
				return val;
			}
			else {
				Log.d("Cache", "Removing from cache");
				remove(key);
			}
		}
		
		return null;
	}
	
	private boolean isValid(File data) {
        return data != null && (data.lastModified() + TTL*1000) > System.currentTimeMillis();
    }
	
	private boolean containsKey(String key) {
        File file = getCacheFile(ctx, key);
        return file.exists() && file.length() != 0;
    }

    private File get(String key) {
        return getCacheFile(ctx, key);
    }

    private void remove(String key) {
        FileUtils.deleteQuietly(getCacheFile(ctx, key));
    }
    
    private static File getCacheFile(Context ctx, String key){
        return new File(ctx.getCacheDir(), md5(key));
    }
    
    private static String md5(String s) {
        String output = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes("UTF-8"));
            byte messageDigest[] = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            output = hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output;
    }
		
}
