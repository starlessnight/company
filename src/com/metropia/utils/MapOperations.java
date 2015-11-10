package com.metropia.utils;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.util.Log;

import com.google.android.maps.MapView;
import com.metropia.activities.LandingActivity2;
import com.metropia.activities.R;
import com.metropia.models.POIContainer;
import com.metropia.models.PoiOverlayInfo;
import com.metropia.tasks.ICallback;
import com.metropia.tasks.ImageLoader;
import com.metropia.ui.SkobblerImageView;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.map.SKAnimationSettings;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKAnnotationView;
import com.skobbler.ngx.map.SKMapSurfaceView;

public class MapOperations {
	
	public static final Integer POIOVERLAY_HIDE_ZOOM_LEVEL = 9;
	
	public static AtomicInteger sizeRatio = new AtomicInteger(2);
    public static AtomicInteger annSize = new AtomicInteger();

	public MapOperations() {
		
	}

	
	
	
	public static void addAnnotationFromPoiInfo(Context context, final SKMapSurfaceView mapView, POIContainer poiContainer, final PoiOverlayInfo poiInfo) {
		if (((Activity)context).isFinishing()) return;
   		final SKAnnotation incAnn = new SKAnnotation(poiContainer.addPOIToMap(poiInfo));
//   		incAnn.setUniqueID();
   		incAnn.setLocation(new SKCoordinate(poiInfo.lon, poiInfo.lat));
   		incAnn.setMininumZoomLevel(POIOVERLAY_HIDE_ZOOM_LEVEL);
   		final SKAnnotationView iconView = new SKAnnotationView();
   		final SkobblerImageView incImage = new SkobblerImageView(context, poiInfo.markerWithShadow, sizeRatio.get());
   		incImage.setLat(poiInfo.lat);
   		incImage.setLon(poiInfo.lon);
   		incImage.setDesc(poiInfo.address);
   		incImage.setMinimumHeight(annSize.get() / sizeRatio.get());
		incImage.setMinimumWidth(annSize.get() / sizeRatio.get());
		incImage.setMaxHeight(annSize.get() / sizeRatio.get());
		incImage.setMaxWidth(annSize.get() / sizeRatio.get());
		

		if (poiInfo.markerWithShadow==R.drawable.transparent_poi && poiInfo.markerURL!=null) {
			new ImageLoader(context, poiInfo.markerURL, new ICallback() {
				@Override
				public void run(Object... obj) {
					if (obj[0]==null) return;
					Drawable drawable = (Drawable) obj[0];
					
					poiInfo.drawable = drawable;
					incImage.setImageDrawable(drawable);
			   		iconView.setView(incImage);
			   		incAnn.setAnnotationView(iconView);
			   		mapView.addAnnotation(incAnn, SKAnimationSettings.ANIMATION_NONE);
				}
			}).execute(true);
		}
		else {
			incImage.setImageBitmap(Misc.getBitmap(context, poiInfo.markerWithShadow, sizeRatio.get()));
	   		iconView.setView(incImage);
	   		incAnn.setAnnotationView(iconView);
	   		mapView.addAnnotation(incAnn, SKAnimationSettings.ANIMATION_NONE);
		}
	}
	
	
	public static boolean firstUpdate = true;
	public static void updateAnnotationSize(Context context, final SKMapSurfaceView mapView, POIContainer poiContainer, int newRatio) {
		if (((Activity)context).isFinishing()) return;
		if (firstUpdate) {
			newRatio = 2;
			firstUpdate = false;
		}
    	if(sizeRatio.get() != newRatio) {
    		sizeRatio.set(newRatio);
	    	Set<Integer> starIds = poiContainer.getStarUniqueIdSet();
	    	for(Integer uniqueId : starIds) {
	    		PoiOverlayInfo poiInfo = poiContainer.getExistedPOIByUniqueId(uniqueId);
	    		if(poiInfo != null) {
	    			SKAnnotation incAnn = new SKAnnotation(uniqueId);
	    			incAnn.setUniqueID(uniqueId);
	    			incAnn.setLocation(new SKCoordinate(poiInfo.lon, poiInfo.lat));
	    			incAnn.setMininumZoomLevel(POIOVERLAY_HIDE_ZOOM_LEVEL);
	    			SKAnnotationView iconView = new SKAnnotationView();
	    			SkobblerImageView incImage = new SkobblerImageView(context, poiInfo.markerWithShadow, newRatio);
	    			incImage.setLat(poiInfo.lat);
	    			incImage.setLon(poiInfo.lon);
	    			incImage.setDesc(poiInfo.address);
	    			incImage.setMinimumHeight(annSize.get() / newRatio);
	    			incImage.setMinimumWidth(annSize.get() / newRatio);
	    			incImage.setMaxHeight(annSize.get() / newRatio);
	    			incImage.setMaxWidth(annSize.get() / newRatio);
	    			if (poiInfo.drawable!=null) {
	    				incImage.setImageDrawable(poiInfo.drawable);
	    			}
	    			else incImage.setImageBitmap(Misc.getBitmap(context, poiInfo.markerWithShadow, newRatio));
	    			iconView.setView(incImage);
	    			incAnn.setAnnotationView(iconView);
	    			mapView.addAnnotation(incAnn, SKAnimationSettings.ANIMATION_NONE);
//	    			mapView.updateAnnotation(incAnn);
	    		}
	    	}
	    	Set<Integer> bulbIds = poiContainer.getBulbUniqueIdSet();
	    	for(Integer uniqueId : bulbIds) {
	    		PoiOverlayInfo poiInfo = poiContainer.getExistedPOIByUniqueId(uniqueId);
	    		if(poiInfo != null) {
	    			SKAnnotation incAnn = new SKAnnotation(uniqueId);
	    			incAnn.setUniqueID(uniqueId);
	    			incAnn.setLocation(new SKCoordinate(poiInfo.lon, poiInfo.lat));
	    			incAnn.setMininumZoomLevel(POIOVERLAY_HIDE_ZOOM_LEVEL);
	    			SKAnnotationView iconView = new SKAnnotationView();
	    			SkobblerImageView incImage = new SkobblerImageView(context, poiInfo.markerWithShadow, newRatio);
	    			incImage.setLat(poiInfo.lat);
	    			incImage.setLon(poiInfo.lon);
	    			incImage.setDesc(poiInfo.address);
	    			incImage.setMinimumHeight(annSize.get() / newRatio);
	    			incImage.setMinimumWidth(annSize.get() / newRatio);
	    			incImage.setMaxHeight(annSize.get() / newRatio);
	    			incImage.setMaxWidth(annSize.get() / newRatio);
//	    			incImage.setImageResource(poiInfo.markerWithShadow);
	    			incImage.setImageBitmap(Misc.getBitmap(context, poiInfo.markerWithShadow, newRatio));
	    			iconView.setView(incImage);
	    			incAnn.setAnnotationView(iconView);
	    			mapView.addAnnotation(incAnn, SKAnimationSettings.ANIMATION_NONE);
//	    			mapView.updateAnnotation(incAnn);
	    		}
	    	}
    	}
    }
}
