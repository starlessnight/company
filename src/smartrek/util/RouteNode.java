package smartrek.util;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Formatter;

import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/***************************************************************************************************************************
 * 
 *
 *
 ***************************************************************************************************************************/
public class RouteNode extends OverlayItem {
	
	private String lat;
	private String lon;
	
	/***************************************************************************************************************************
	 * 
	 *
	 *
	 ***************************************************************************************************************************/
	public RouteNode(float latitude, float longitude, int routeNum, int nodeNum) {
		super(new GeoPoint(
				  (int)Math.round(1E6*latitude),
				  (int)Math.round(1E6*longitude)),
				  "Route " + routeNum,
				  "Node " + nodeNum );
		
		
		lat = latitude + "";
		lon = longitude + "";
		
		Log.d("RouteNode","Formatted Latitude " + getFloatLat());
		Log.d("RouteNode","Formatted Longitude " + getFloatLon());
//		Formatter myFormatter = new Formatter();
//		lat = myFormatter.format("%.6f", latitude);
//		Log.d("RouteNode","Formatted Latitude " + latitude%.6f);
//		Log.d("RouteNode","Formatted Longitude " + longitude%.6f);
	}
	
	public RouteNode(Bundle  bundle, int routeNum, int nodeNum) {
		super(new GeoPoint(
				  (int)Math.round(1E6*(Float.parseFloat(bundle.getString("latitude" + nodeNum)))),
				  (int)Math.round(1E6*(Float.parseFloat(bundle.getString("longitude" + nodeNum))))),
				  "Route " + routeNum,
				  "Node " + nodeNum );
		
		
		lat = bundle.getString("latitude" + nodeNum);
		lon = bundle.getString("longitude" + nodeNum);
		
		Log.d("RouteNode","Formatted Latitude " + getFloatLat());
		Log.d("RouteNode","Formatted Longitude " + getFloatLon());
		
	}
	
	public String getFloatLat(){
		return lat;
	}
	
	public String getFloatLon(){
		return lon;
	}
	
	public void adjustLatLonFormat(){
		Log.d("RouteNode","Adjusting Format From " + lat);
		Log.d("RouteNode","Adjusting Format From " + lon);
		Log.d("RouteNode",lat.substring(lat.length()-2));
		Log.d("RouteNode",lon.substring(lon.length()-2));	
	}
}