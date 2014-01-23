package com.smartrek.ui.overlays;

import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.smartrek.utils.GeoPoint;

public class POIOverlay extends Overlay {
	
	private Bitmap icon;
	
	private GeoPoint gPoint;
	
	public POIOverlay(Context context, GeoPoint gPoint, int drawable) {
		super(context);
		this.gPoint = gPoint;
		this.icon = BitmapFactory.decodeResource(context.getResources(), drawable);
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		Paint paint = new Paint();
        paint.setAntiAlias(true);
		Point point = new Point();		
		projection.toPixels(gPoint, point);		
		Point poiPiont = new Point(point);
		canvas.drawBitmap(icon, poiPiont.x - (icon.getWidth()/2), poiPiont.y - icon.getHeight() * 85 / 100, paint);
	}
	
}
