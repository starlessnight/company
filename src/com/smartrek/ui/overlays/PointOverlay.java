package com.smartrek.ui.overlays;

import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.smartrek.utils.GeoPoint;

/**
 * Draws a point on a map
 * @author Sumin Byeon
 */
public class PointOverlay extends Overlay {
	public static final float RADIUS = 12.0f;
	
	private GeoPoint geoPoint;
	private int color = Color.GREEN;
	
	public PointOverlay(Context context, float lat, float lng) {
		super(context);
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

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);
		
		Point point = new Point();
		projection.toPixels(geoPoint, point);
		
		canvas.drawCircle(point.x, point.y, RADIUS, paint);
	}
}
