package com.smartrek;

import java.io.File;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.util.Log;

import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.SKPrepareMapTextureThread;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.navigation.SKAdvisorSettings;
import com.smartrek.activities.DebugOptionsActivity;

public class SkobblerUtils {
	
	private static final String API_KEY = "18dc78a75415e2e1f4260fd7e5990fd0f9a1ad42160171997d823bf79eb09d63";
	
	public static final String SDK_VERSION = "2.2";
	
	public static void initSkobbler(Context ctx, SKPrepareMapTextureListener listener, Runnable checkLogin) {
        String mapResourcesDirPath = getMapResourceDirPath(ctx);
        File skmapDir = new File(mapResourcesDirPath);
        if (skmapDir.exists() && DebugOptionsActivity.isSkobblerPatched(ctx)) {
        	// map resources have already been copied - start the map activity
        	initializeLibrary(ctx);
        	if(checkLogin != null) {
        		checkLogin.run();
        	}
        } else {
        	// if mapResourcesDirPath existed then clean first
        	FileUtils.deleteQuietly(skmapDir);
        	// if map resources are not already present copy them to
            // mapResourcesDirPath in the following thread
            new SKPrepareMapTextureThread(ctx, mapResourcesDirPath, "SKMaps.zip", listener).start();
            // copy some other resource needed
        }
	}
	
	public static void initializeLibrary(Context ctx) {
        // get object holding map initialization settings
        SKMapsInitSettings initMapSettings = new SKMapsInitSettings();
        // set path to map resources and initial map style
        initMapSettings.setMapResourcesPaths(getMapResourceDirPath(ctx), getMapVewStyle(ctx));
        
        SKAdvisorSettings advisorSettings = initMapSettings.getAdvisorSettings();
        advisorSettings.setLanguage("en");
        advisorSettings.setAdvisorVoice("en");
        initMapSettings.setAdvisorSettings(advisorSettings);
       
        SKMaps.getInstance().initializeSKMaps(ctx, initMapSettings, API_KEY);
    }
	
	
	public static SKMapViewStyle getMapVewStyle(Context ctx) {
		if(isDayMode()) {
			return new SKMapViewStyle(getMapResourceDirPath(ctx) + "daystyle/", "daystyle.json");
		}
		return new SKMapViewStyle(getMapResourceDirPath(ctx) + "nightstyle/", "nightstyle.json");
	}
	
	public static boolean isDayMode() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		return hour >= 6 && hour < 18;
	}
	
	private static String getMapResourceDirPath(Context ctx) {
		String mapResourcesDirPath = "";
    	File externalDir = ctx.getExternalFilesDir(null);
        
        // determine path where map resources should be copied on the device
        if (externalDir != null) {
            mapResourcesDirPath = externalDir + "/" + "SKMaps/";
        } else {
            mapResourcesDirPath = ctx.getFilesDir() + "/" + "SKMaps/";
        }
        return mapResourcesDirPath;
	}

}
