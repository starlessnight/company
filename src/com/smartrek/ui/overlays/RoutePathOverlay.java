package com.smartrek.ui.overlays;

import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.smartrek.activities.R;
import com.smartrek.models.Route;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.RouteLink;
import com.smartrek.utils.RouteNode;

public class RoutePathOverlay extends Overlay {
	
    public static final int GREEN = 0xFF99CC00; 
    
    public static final int BLUE = 0xFF0089E8;
    
	/**
	 * Green, blue, dark gray
	 */
	public static final int COLORS[] = {GREEN, BLUE, 0xFF232A2E};
	
	private Route route;
	private int color;
	private boolean highlighted = true;
	
	private Bitmap originFlag;
	private Bitmap destinationFlag;
	
	private boolean dashEffect;
	
	private RoutePathCallback callback;
	
	public interface RoutePathCallback {
		public void onTap();
	}
	
	public RoutePathOverlay(Context context, Route route, int color, int marker) {
		super(context);
		this.route = route;
		this.color = color;
		if(marker > 0) {
			this.originFlag = BitmapFactory.decodeResource(context.getResources(), marker);
		}
		this.destinationFlag = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin_destination);
	}
	
	public boolean isHighlighted() {
		return highlighted;
	}
	
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}
	
	public void setDashEffect() {
		this.dashEffect = true;
	}
	
	public void setCallback(RoutePathCallback callback) {
		this.callback = callback;
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		List<RouteNode> routeNodes = route.getNodes();
		
		if(routeNodes.isEmpty()){
		    return;
		}
		
		Projection projection = mapView.getProjection();
		int zoom = mapView.getZoomLevel();
		DisplayMetrics dm = mapView.getResources().getDisplayMetrics();
        float thickness = Dimension.dpToPx(1 + zoom/2 + (highlighted ? 1 : -1), dm);
        
        if(!dashEffect){
            drawPath(canvas, new Paint(), projection, routeNodes, 
                thickness, getDarkerColor(color));
        }
        
		Paint path = new Paint();
		Point originPoint = drawPath(canvas, path, projection, routeNodes, 
	        thickness * 0.7f, color);
		
		if(originFlag!=null) {
			canvas.drawBitmap(originFlag, originPoint.x - (originFlag.getWidth()/2), originPoint.y - originFlag.getHeight() * 85 / 100, path);
		}
		//canvas.drawBitmap(destinationFlag, point.x - (destinationFlag.getWidth()/2), point.y - destinationFlag.getHeight() * 85 / 100, paint);
	}
	
	private Point drawPath(Canvas canvas, Paint paint, Projection projection, 
	        List<RouteNode> routeNodes, float width, int pathColor){	    
	    paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
	    
        if(dashEffect) {
            paint.setPathEffect(new DashPathEffect(new float[] {30, 27}, 0));
        }
        
        paint.setStrokeWidth(width);
        
        // Seems like Paint.setAlpha has no effect
        int alphaMask = highlighted ? 0x66000000 : 0x4F000000;
        paint.setColor((pathColor & 0x00FFFFFF) | alphaMask);
	    
	    Point point = new Point();
        Path path = new Path();
	    
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
        
        return originPiont;
	}
	
	private Integer distanceToPathThreshold = 100; //meter
	
	@Override
	public boolean onSingleTapConfirmed(final MotionEvent e, final MapView mapView) {
		if(callback != null) {
			IGeoPoint fromPixels = mapView.getProjection().fromPixels(e.getX(), e.getY());
			RouteLink nearestLink = route.getNearestLink(fromPixels.getLatitude(), fromPixels.getLongitude());
			double dist = nearestLink.distanceTo(fromPixels.getLatitude(), fromPixels.getLongitude());
			if(dist < distanceToPathThreshold) {
				callback.onTap();
				return true;
			}
		}
		return super.onSingleTapConfirmed(e, mapView);
	}
	
	private int getDarkerColor(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.5f; // value component
		return Color.HSVToColor(hsv);
	}
}
