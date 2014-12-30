package com.metropia.ui.overlays;

import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;

import com.metropia.utils.GeoPoint;

public class RouteDebugOverlay extends Overlay {
	
	public interface ActionListener {
		void onLongPress(double latitude, double longitude);
	}
	
	private ActionListener actionListener;
	
	private GeoPoint lastTouchedPoint;
	
	public RouteDebugOverlay(Context context) {
		super(context);
	}
	
	public void setActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}
	
	@Override
	public boolean onLongPress(MotionEvent evt, MapView mapView) {
		Projection projection = mapView.getProjection();
		lastTouchedPoint = new GeoPoint(projection.fromPixels(evt.getX(), evt.getY()));
		
		if (actionListener != null) {
			actionListener.onLongPress(lastTouchedPoint.getLatitude(), lastTouchedPoint.getLongitude());
		}
		
		mapView.postInvalidate();
		
		return false;
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		
		if (lastTouchedPoint != null) {
		
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(0x33F0C020);
			
			Projection projection = mapView.getProjection();
			Point point = projection.toMapPixels(lastTouchedPoint, null);
			
			canvas.drawCircle(point.x, point.y, 7, paint);
		}
		
	}
}
