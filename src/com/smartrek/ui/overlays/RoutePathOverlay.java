package com.smartrek.ui.overlays;

import java.util.List;

import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.smartrek.activities.R;
import com.smartrek.models.Route;
import com.smartrek.utils.RouteNode;

public class RoutePathOverlay extends Overlay {
	
	/**
	 * Green, blue, dark gray
	 */
	public static final int COLORS[] = {0xFF6CBD44, 0xFF0089E8, 0xFF232A2E};
	
	private Route route;
	private int color;
	private boolean highlighted = true;
	
	private Bitmap originFlag;
	private Bitmap destinationFlag;
	
	public RoutePathOverlay(Context context, Route route, int color) {
		super(context);
		this.route = route;
		this.color = color;
		this.originFlag = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin_origin);
		this.destinationFlag = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin_destination);
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
		int zoom = mapView.getZoomLevel();
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		
		int thickness = 5 + zoom/2 + (highlighted ? 3 : -3);
		paint.setStrokeWidth(thickness);
		
		// Seems like Paint.setAlpha has no effect
		int alphaMask = highlighted ? 0xCF000000 : 0x4F000000;
		paint.setColor((color & 0x00FFFFFF) | alphaMask);
		
		Point point = new Point();
		Path path = new Path();
		
		List<RouteNode> routeNodes = route.getNodes();
		
		RouteNode firstNode = routeNodes.get(0);
		projection.toPixels(firstNode.getGeoPoint(), point);
		path.moveTo(point.x, point.y);
		
        // Copy the initial point because the variable 'point' is not
        // referentially transparent and path has to be drawn before the origin
        // and the destination flags are drawn.
		Point originPiont = new Point(point);
		
		for (int i = 1; i < routeNodes.size(); i++) {
			RouteNode node = routeNodes.get(i);
			
			projection.toPixels(node.getGeoPoint(), point);
			path.lineTo(point.x, point.y);
		}
		
		canvas.drawPath(path, paint);
		canvas.drawBitmap(originFlag, originPiont.x - (originFlag.getWidth()/2), originPiont.y - originFlag.getHeight(), paint);
		canvas.drawBitmap(destinationFlag, point.x - (originFlag.getWidth()/2), point.y - destinationFlag.getHeight(), paint);
	}
}
