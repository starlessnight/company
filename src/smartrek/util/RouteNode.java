package smartrek.util;

import android.os.Bundle;
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
		

		// FIXME: Really???
		lat = latitude + "";
		lon = longitude + "";
	}
	
	public RouteNode(Bundle  bundle, int routeNum, int nodeNum) {
		super(new GeoPoint(
				  (int)Math.round(1E6*(Float.parseFloat(bundle.getString("latitude" + nodeNum)))),
				  (int)Math.round(1E6*(Float.parseFloat(bundle.getString("longitude" + nodeNum))))),
				  "Route " + routeNum,
				  "Node " + nodeNum );
		
		
		lat = bundle.getString("latitude" + nodeNum);
		lon = bundle.getString("longitude" + nodeNum);
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