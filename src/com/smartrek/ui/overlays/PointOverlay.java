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
public class PointOverlay extends Overlay {
	public static final float RADIUS = 12.0f;
	
	private GeoPoint geoPoint;
	private int color = Color.GREEN;
	
	private Bitmap icon;
	
	private float currentDegrees;
	
	private float degrees;
	
	public PointOverlay(Context context, float lat, float lng, int drawableId) {
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
	
	public void setColor(int color) {
		this.color = color;
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		
		Point point = new Point();
		projection.toPixels(geoPoint, point);
		
		//paint.setColor(color);
		//canvas.drawCircle(point.x, point.y, RADIUS, paint);
		
		if(currentDegrees != degrees){
		    float diff = degrees - currentDegrees;
		    if(Math.abs(diff) > 180){
		        diff += diff > 0?-360:360;
		    }
		    float step = diff / 3;
		    if(Math.abs(diff) <= 6){
                currentDegrees = degrees; 
            }else{
                currentDegrees = currentDegrees + step;
                if(Math.abs(currentDegrees) > 180){
                    currentDegrees += currentDegrees > 0?-360:360;
                }
            }
		}
		
		Matrix matrix = new Matrix();
        matrix.setRotate(Math.round(currentDegrees));
        Bitmap rotatedIcon = Bitmap.createBitmap(icon, 0, 0, icon.getWidth(), icon.getHeight(), matrix, true);
        canvas.drawBitmap(rotatedIcon, point.x - (rotatedIcon.getWidth()/2), point.y - rotatedIcon.getHeight()/2, paint);
	}

    public float getDegrees() {
        return degrees;
    }

    public void setDegrees(float degrees) {
        this.degrees = degrees;
    }
}
