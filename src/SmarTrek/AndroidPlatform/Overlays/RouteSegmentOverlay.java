package SmarTrek.AndroidPlatform.Overlays;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/******************************************************************************************************************************
 * **************************************** RouteSegmentOverlay ***************************************************************
 * 
 * 
 * @author Tim Olivas
 *
 *******************************************************************************************************************************/
public class RouteSegmentOverlay extends Overlay { 

	private GeoPoint gp1;
	private GeoPoint gp2;
	private int colorNum;

	/***************************************************************************************************************************
	 * ********************** RouteSegmentOverlay(GeoPoint gp1,GeoPoint gp2, int colorNum) *************************************
	 * 
	 * 
	 *
	 ***************************************************************************************************************************/
	public RouteSegmentOverlay(GeoPoint gp1,GeoPoint gp2, int colorNum) { // GeoPoint is a int. (6E)
    	this.gp1 = gp1;
    	this.gp2 = gp2;
    	this.colorNum = colorNum;
	}


	/***************************************************************************************************************************
	 * ********************** draw (Canvas canvas, MapView mapView, boolean shadow, long when) *********************************
	 * 
	 * 
	 * 
	 * @param Canvas canvas
	 * 
	 * @param MapView mapView
	 * 
	 * @param boolean shadow
	 * 
	 * @param long when
	 * 
	 * @return True if you need to be drawn again right away; false otherwise. Default implementation returns false.
	 *
	 ***************************************************************************************************************************/
	@Override
	public boolean draw (Canvas canvas, MapView mapView, boolean shadow, long when) {
		Projection projection = mapView.getProjection();
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		Point point = new Point();
		projection.toPixels(gp1, point);
		if(colorNum == 0){
			paint.setColor(Color.RED);
		} else if (colorNum == 1){
			paint.setColor(Color.BLUE);
		} else {
			paint.setColor(Color.BLACK);
		}
		Point point2 = new Point();
		projection.toPixels(gp2, point2);
		paint.setStrokeWidth(7);
		paint.setAlpha(90);
		canvas.drawLine(point.x, point.y, point2.x,point2.y, paint);
		return super.draw(canvas, mapView, shadow, when);
	}
}