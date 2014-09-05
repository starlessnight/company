package com.smartrek;

import java.io.File;
import java.util.Calendar;

import android.content.Context;

import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.SKPrepareMapTextureThread;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.navigation.SKAdvisorSettings;

public class SkobblerUtils {
	
	private static final String API_KEY = "18dc78a75415e2e1f4260fd7e5990fd0f9a1ad42160171997d823bf79eb09d63";
	
	private static String mapResourcesDirPath;
	
	
	public static String getMapResourceDirPath() {
		return mapResourcesDirPath;
	}

	public static void setMapResourcesDirPath(String dirPath) {
		mapResourcesDirPath = dirPath;
	}
	
	public static void initSkobbler(Context ctx, SKPrepareMapTextureListener listener, Runnable checkLogin) {
    	String mapResourcesDirPath = "";
    	File externalDir = ctx.getExternalFilesDir(null);
        
        // determine path where map resources should be copied on the device
        if (externalDir != null) {
            mapResourcesDirPath = externalDir + "/" + "SKMaps/";
        } else {
            mapResourcesDirPath = ctx.getFilesDir() + "/" + "SKMaps/";
        }
        setMapResourcesDirPath(mapResourcesDirPath);

        
        if (!new File(mapResourcesDirPath).exists()) {
            // if map resources are not already present copy them to
            // mapResourcesDirPath in the following thread
            new SKPrepareMapTextureThread(ctx, mapResourcesDirPath, "SKMaps.zip", listener).start();
            // copy some other resource needed
        } else {
            // map resources have already been copied - start the map activity
        	initializeLibrary(ctx);
        	if(checkLogin != null) {
        		checkLogin.run();
        	}
        }
	}
	
	public static void initializeLibrary(Context ctx) {
        // get object holding map initialization settings
        SKMapsInitSettings initMapSettings = new SKMapsInitSettings();
        // set path to map resources and initial map style
        initMapSettings.setMapResourcesPaths(mapResourcesDirPath,
                new SKMapViewStyle(mapResourcesDirPath + "daystyle/", "daystyle.json"));
        
        SKAdvisorSettings advisorSettings = initMapSettings.getAdvisorSettings();
        advisorSettings.setLanguage("en");
        advisorSettings.setAdvisorVoice("en");
        advisorSettings.setPlayInitialAdvice(true);
        advisorSettings.setPlayAfterTurnInformalAdvice(true);
        advisorSettings.setPlayInitialVoiceNoRouteAdvice(true);
        initMapSettings.setAdvisorSettings(advisorSettings);
       
        SKMaps.getInstance().initializeSKMaps(ctx, initMapSettings, API_KEY);
    }
	
	
	public static SKMapViewStyle getMapVewStyle() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if(hour >=6 && hour <=18) {
			return new SKMapViewStyle(mapResourcesDirPath + "daystyle/", "daystyle.json");
		}
		return new SKMapViewStyle(mapResourcesDirPath + "nightstyle/", "nightstyle.json");
	}

}
