package com.metropia.utils;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

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
import com.skobbler.ngx.map.SKScreenPoint;

public class MapOperations {
	
	public static final Integer POIOVERLAY_HIDE_ZOOM_LEVEL = 9;
	
	public static AtomicInteger sizeRatio = new AtomicInteger(2);
    public static AtomicInteger annSize = new AtomicInteger();

	public MapOperations() {
		
	}

	
	
	
	public static void addAnnotationFromPoiInfo(Context context, final SKMapSurfaceView mapView, POIContainer poiContainer, final PoiOverlayInfo poiInfo) {
		if (((Activity)context).isFinishing()) return;
   		final SKAnnotation incAnn = new SKAnnotation(poiContainer.addPOIToMap(poiInfo));
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
	
	public static final Integer FROM_BALLOON_ID = 0;
	public static final Integer TO_BALLOON_ID = FROM_BALLOON_ID + 100;  //100
	
	public static void drawODBalloon(Context context, SKMapSurfaceView mapView, PoiOverlayInfo info, boolean from) {
    	SKAnnotation balloonAnn = new SKAnnotation(from ? FROM_BALLOON_ID : TO_BALLOON_ID);
    	balloonAnn.setUniqueID(from ? FROM_BALLOON_ID : TO_BALLOON_ID);
    	balloonAnn.setOffset(new SKScreenPoint(0, Dimension.dpToPx(20, context.getResources().getDisplayMetrics())));
    	balloonAnn.setLocation(new SKCoordinate(info.lon, info.lat));
    	SKAnnotationView balloonView = new SKAnnotationView();
    	SkobblerImageView balloonImage = new SkobblerImageView(context, 0, 0);
    	balloonImage.setLat(info.lat);
    	balloonImage.setLon(info.lon);
    	balloonImage.setDesc(from ? "FROM" : "TO");
    	balloonImage.setImageBitmap(loadBitmapOfFromToBalloon(context, fromToBalloon, from));
    	balloonView.setView(balloonImage);
    	balloonAnn.setAnnotationView(balloonView);
    	mapView.addAnnotation(balloonAnn, SKAnimationSettings.ANIMATION_POP_OUT);
    }
	
	private static View fromToBalloon;
	public static Bitmap loadBitmapOfFromToBalloon(Context context, View fromToBalloon, boolean from) {
		if(fromToBalloon == null) {
			FrameLayout layout = new FrameLayout(context);
			ViewGroup.LayoutParams layoutLp = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(layoutLp);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			fromToBalloon = inflater.inflate(R.layout.from_to_balloon, layout);
		}
        TextView textView = (TextView) fromToBalloon.findViewById(R.id.poi_mini_title);
        textView.setText(from ? "FROM" : "TO");
        Font.setTypeface(Font.getRegular(context.getAssets()), textView);
        fromToBalloon.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        fromToBalloon.layout(0, 0, fromToBalloon.getMeasuredWidth(), fromToBalloon.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(fromToBalloon.getMeasuredWidth(), fromToBalloon.getMeasuredHeight(), Bitmap.Config.ARGB_8888);                
        Canvas canvas = new Canvas(bitmap);
        fromToBalloon.draw(canvas);
        return bitmap;
	}
}
