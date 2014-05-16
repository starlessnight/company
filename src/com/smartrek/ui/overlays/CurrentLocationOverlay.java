package com.smartrek.ui.overlays;

import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;

import com.smartrek.utils.GeoPoint;

/**
 * Draws a point on a map
 * @author Sumin Byeon
 */
public class CurrentLocationOverlay extends Overlay {
	private static final int MAX_RADIUS = 100;
    
	private int MIN_RAIDUS = 32;
	
    private int MAX_OPACITY = 225;
	
	private GeoPoint geoPoint;
	
	private Bitmap icon;
	
	private float currentDegrees;
	
	private float degrees;
	
	public CurrentLocationOverlay(Context context, float lat, float lng, int drawableId) {
		super(context);
		setLocation(lat, lng);
		icon = BitmapFactory.decodeResource(context.getResources(), drawableId);
	}
	
	public void setLocation(float lat, float lng) {
		geoPoint = new GeoPoint(lat, lng);
	}
	
	public GeoPoint getLocation(){
	    return geoPoint;
	}
	
	private int radius = MIN_RAIDUS;
	
	private int opacity = MAX_OPACITY;
	
	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		
		Point point = new Point();
		projection.toPixels(geoPoint, point);
		
		Paint circlePaint = new Paint();
		circlePaint.setAntiAlias(true);
		circlePaint.setColor(Color.parseColor("#" + Integer.toHexString(opacity)  + "d4ebf5"));
        canvas.drawCircle(point.x, point.y, radius, circlePaint);
		
		if(currentDegrees != degrees){
		    float diff = degrees - currentDegrees;
		    if(Math.abs(diff) > 180){
		        diff += diff > 0?-360:360;
		    }
		    float step = diff / 5;
            currentDegrees = currentDegrees + step;
            if(Math.abs(currentDegrees) > 180){
                currentDegrees += currentDegrees > 0?-360:360;
            }
		}
		
		Matrix matrix = new Matrix();
        matrix.setRotate(round(currentDegrees, 9));
        Bitmap rotatedIcon = Bitmap.createBitmap(icon, 0, 0, icon.getWidth(), icon.getHeight(), matrix, true);
        canvas.drawBitmap(rotatedIcon, point.x - rotatedIcon.getWidth()/2.0f, 
            point.y - rotatedIcon.getHeight()/2.0f, paint);
        
        radius++;
        opacity -= 3;
        if(radius > MAX_RADIUS){
            radius = MIN_RAIDUS;
            opacity = MAX_OPACITY;
        }
	}
	
	public static int round(float input, int step) 
	{
	  return ((Math.round(input / step)) * step);
	}

    public float getDegrees() {
        return degrees;
    }

    public void setDegrees(float degrees) {
        this.degrees = degrees;
    }
}
