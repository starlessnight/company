package com.metropia;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import android.content.Context;

import com.metropia.activities.DebugOptionsActivity;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.SKPrepareMapTextureThread;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.navigation.SKAdvisorSettings;

public class SkobblerUtils {
	
	private static final String API_KEY = "18dc78a75415e2e1f4260fd7e5990fd0f9a1ad42160171997d823bf79eb09d63";
	
	public static final String SDK_VERSION = "2.3";
	
	public static void initSkobbler(Context ctx, SKPrepareMapTextureListener listener, Runnable checkLogin) {
        String mapResourcesDirPath = getMapResourceDirPath(ctx);
        File skmapDir = new File(mapResourcesDirPath);
        if (skmapDir.exists() && DebugOptionsActivity.isSkobblerPatched(ctx)) {
        	// map resources have already been copied
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
        initMapSettings.setMapResourcesPaths(getMapResourceDirPath(ctx), getMapViewStyle(ctx, true));
        
        SKAdvisorSettings advisorSettings = initMapSettings.getAdvisorSettings();
        advisorSettings.setLanguage("en");
        advisorSettings.setAdvisorVoice("en");
        initMapSettings.setAdvisorSettings(advisorSettings);
       
        SKMaps.getInstance().initializeSKMaps(ctx, initMapSettings, API_KEY);
    }
	
	
	public static SKMapViewStyle getMapViewStyle(Context ctx, boolean dayMode) {
		if(dayMode) {
			return new SKMapViewStyle(getMapResourceDirPath(ctx) + "daystyle/", "daystyle.json");
		}
		return new SKMapViewStyle(getMapResourceDirPath(ctx) + "nightstyle/", "nightstyle.json");
	}
	
	private static final DateFormat HHmm = new SimpleDateFormat("HHmm");
	
	public static boolean isDayMode() {
		Integer time = Integer.valueOf(HHmm.format(new Date(System.currentTimeMillis())));
		return time >= 600 && time < 1800;
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
	
	public static Integer getUniqueId(double lat, double lon) {
		return new HashCodeBuilder().append(lat).append("+").append(lon).toHashCode();
	}
	
	/**
     * return RGBA Array (0~1)
     */
    public static float[] getRouteColorArray(String color) {
    	float[] skColor = {0.6f, 0.8f, 0.0f, 1.0f}; // GREEN
    	if(StringUtils.isNotBlank(color) && StringUtils.startsWith(color, "#")) {
    		String RR = color.substring(1, 3);
    		String GG = color.substring(3, 5);
    		String BB = color.substring(5, color.length());
    		skColor[0] = Float.valueOf(Integer.parseInt(RR, 16)) / 255.0f;
    		skColor[1] = Float.valueOf(Integer.parseInt(GG, 16)) / 255.0f;
    		skColor[2] = Float.valueOf(Integer.parseInt(BB, 16)) / 255.0f;
    	}
    	return skColor;
    }

}
