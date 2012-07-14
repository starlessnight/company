package com.smartrek.overlays;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * Draws a point on a map
 * @author Sumin Byeon
 */
public class PointOverlay extends Overlay {
	public static final float RADIUS = 7.0f;
	
	private GeoPoint geoPoint;
	private int color = Color.GREEN;
	
	public PointOverlay(float lat, float lng) {
		setLocation(lat, lng);
	}
	
	public void setLocation(float lat, float lng) {
		geoPoint = new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6));
	}
	
	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public synchronized boolean draw (Canvas canvas, MapView mapView, boolean shadow, long when) {
		Projection projection = mapView.getProjection();
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);
		
		Point point = new Point();
		projection.toPixels(geoPoint, point);
		
		canvas.drawCircle(point.x, point.y, RADIUS, paint);
		
		return super.draw(canvas, mapView, shadow, when);
	}
}
