package com.smartrek.ui.overlays;

import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.smartrek.utils.GeoPoint;

public class EventOverlay extends Overlay {
	
	public interface ActionListener {
		void onLongPress(double latitude, double longitude);
		void onSingleTap();
	}
	
	private ActionListener actionListener;
	
	private GeoPoint lastTouchedPoint;
	
	public EventOverlay(Context context) {
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
		return false;
	}
	
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
	    if (actionListener != null) {
            actionListener.onSingleTap();
        }
	    return false;
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		
	}
}
