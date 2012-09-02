package com.smartrek.ui.overlays;

import java.util.List;

import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.smartrek.models.Route;
import com.smartrek.utils.RouteNode;

public class RoutePathOverlay extends Overlay {
	
	public static final int COLORS[] = {0xFF79BA14, 0xFF1E8BD9, 0xFF232A2E};
	
	private Route route;
	private int color;
	private boolean highlighted = true;
	
	public RoutePathOverlay(Context context, Route route, int color) {
		super(context);
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
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(6);
		
		// Seems like Paint.setAlpha has no effect
		int alphaMask = highlighted ? 0xCF000000 : 0x7F000000;
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
		
		int zoom = mapView.getZoomLevel();
		int radius = 2 + zoom/3;
		
		canvas.drawPath(path, paint);
		canvas.drawCircle(point.x, point.y, radius, paint);
	}
}
