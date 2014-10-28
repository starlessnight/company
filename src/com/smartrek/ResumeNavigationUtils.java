package com.smartrek;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.util.TimeUtils;

import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.RouteNode;

public class ResumeNavigationUtils {
	
	public static final String DESTINATION_TIME = "destTime";
	public static final String DEST_LAT = "destLat";
	public static final String DEST_LON = "destLon";

	private static File getDir(Context ctx){
        return new File(ctx.getExternalFilesDir(null), "triplog");
    }

	private static final Float threshold = 804.672f; // 0.5 mile
	
    public static String getInterruptRId(Context ctx, final GeoPoint loc){
    	File dir = getDir(ctx);
    	if(dir.exists()) {
    		File[] files = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					try {
						JSONObject content = new JSONObject(FileUtils.readFileToString(pathname));
						long destTime = content.optLong(DESTINATION_TIME, 0);
						double destLat = content.optDouble(DEST_LAT, 0);
						double destLon = content.optDouble(DEST_LON, 0);
						Log.d("ResumeNavigationUtils", String.format("currentTime %s, destTime %s", String.valueOf(System.currentTimeMillis()), String.valueOf(destTime)));
						if(System.currentTimeMillis() < destTime && 
								RouteNode.distanceBetween(loc.getLatitude(), loc.getLongitude(), destLat, destLon) > threshold) {
							return true;
						}
					}
					catch(Exception ignore) {
						Log.d("ResumeNavigationUtils", Log.getStackTraceString(ignore));
					}
					return false;
				}
			}); 
    		
    		Arrays.sort(files, new Comparator<File>() {
    			public int compare(File lhs, File rhs){
    				Long lhsLong = Long.valueOf(lhs.getName());
    				Long rhsLong = Long.valueOf(rhs.getName());
    				return rhsLong.compareTo(lhsLong);
    			}
			});
    		
    		Log.d("ResumeNavigationUtils", String.format("Array is Empty %b", ArrayUtils.isEmpty(files)));
    		if(!ArrayUtils.isEmpty(files)) {
    			String rId = files[0].getName();
    			cleanTripLog(ctx);
    			return rId;
    		}
    	}
    	cleanTripLog(ctx);
        return null;
    }
    
    public static File getFile(Context ctx, long rId) {
    	return new File(getDir(ctx), String.valueOf(rId));
    }
    
    public static void cleanTripLog(Context ctx) {
    	File dir = getDir(ctx);
    	if(dir.exists()) {
	    	File[] all = getDir(ctx).listFiles();
			for(File f : all) {
				FileUtils.deleteQuietly(f);
			}
    	}
    }
    
}
