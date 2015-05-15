package com.metropia.ui.overlays;

import android.content.Context;
import android.graphics.Color;

import com.metropia.utils.GeoPoint;

/**
 * Draws a point on a map
 * @author Sumin Byeon
 */
public class PointOverlay {
	public static final float RADIUS = 12.0f;
	
	private Context ctx;
	private GeoPoint geoPoint;
	private int color = Color.GREEN;
	
	public PointOverlay(Context context, float lat, float lng) {
		this.ctx = context;
		setLocation(lat, lng);
	}
	
	public void setLocation(float lat, float lng) {
		geoPoint = new GeoPoint(lat, lng);
	}
	
	public GeoPoint getLocation(){
	    return geoPoint;
	}
	
	public void setColor(int color) {
		this.color = color;
	}

}
