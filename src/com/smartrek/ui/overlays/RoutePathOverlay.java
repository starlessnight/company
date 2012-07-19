package com.smartrek.ui.overlays;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.smartrek.models.Route;
import com.smartrek.utils.RouteNode;

public class RoutePathOverlay extends Overlay {
	
	private Route route;
	private int color;
	private boolean highlighted = true;
	
	public RoutePathOverlay(Route route, int color) {
		this.route = route;
		this.color = color;
	}
	
	public boolean isHighlighted() {
		return highlighted;
	}
	
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
		Projection projection = mapView.getProjection();
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(6);
		//paint.setAlpha(90);
		
		// Seems like Paint.setAlpha has no effect
		int alphaMask = highlighted ? 0x6F000000 : 0x2F000000;
		paint.setColor((color & 0x00FFFFFF) | alphaMask);
		
		Point point = new Point();
		Path path = new Path();
		
		List<RouteNode> routeNodes = route.getNodes();
		
		RouteNode firstNode = routeNodes.get(0);
		projection.toPixels(firstNode.getGeoPoint(), point);
		path.moveTo(point.x, point.y);

		for (int i = 1; i < routeNodes.size(); i++) {
			RouteNode node = routeNodes.get(i);
			
			projection.toPixels(node.getGeoPoint(), point);
			path.lineTo(point.x, point.y);
		}
		
		canvas.drawPath(path, paint);
		canvas.drawCircle(point.x, point.y, 10, paint);
		
		return super.draw(canvas, mapView, shadow, when);
	}
}
