package com.smartrek.activities;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.smartrek.models.Route;
import com.smartrek.overlays.RouteOverlay;
import com.smartrek.overlays.RouteSegmentOverlay;
import com.smartrek.utils.RouteNode;

/******************************************************************************************************************
 * 
 * @author Tim Olivas
 *
 ******************************************************************************************************************/
public class ConfirmedMapActivity extends MapActivity {

	private Route route;
	
	private RouteOverlay routeoverlay;
	
	private MapView mapView;
	
	private int GENMAP = 1;
	private int SATELLITE = 2;
	private int CURRENTMODE = GENMAP;
	
	private int selectedRoute;
	
	/******************************************************************************************************************
	 * 
	 *
	 ******************************************************************************************************************/
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.post_reservation_map); 
	    

	    
	    Bundle bundle = getIntent().getExtras();
        
        route = new Route(bundle);
        selectedRoute = bundle.getInt("selected route");
        
        
        
	    mapView = (MapView) findViewById(R.id.mapview);
	    
	    mapView.setBuiltInZoomControls(true);
    	if(CURRENTMODE == SATELLITE){
    		mapView.setSatellite(true);
    	} else if(CURRENTMODE == GENMAP){
    		mapView.setSatellite(false);
    	}
    	
	    List<Overlay> mapOverlays = mapView.getOverlays();
	    
	    int latMax = (int)(-81 * 1E6);
	    int lonMax = (int)(-181 * 1E6);
	    int latMin = (int)(+81 * 1E6);
	    int lonMin = (int)(+181 * 1E6);
	    
	    long latitude = 0;
	    long longitude = 0;
	     
	    List<RouteNode> route_nodes = route.getNodes();
    	
	    for(int i = 0; i < route_nodes.size()-1; i++) {	
	    	GeoPoint point = route_nodes.get(i).getPoint();
	    	
	    	int curLat = point.getLatitudeE6();
	    	int curLon = point.getLongitudeE6();
	    	
	    	latitude += curLat;
	    	longitude += curLon;
	    	
		    latMax = Math.max(latMax, curLat);
		    lonMax = Math.max(lonMax, curLon);
		    latMin = Math.min(latMin, curLat);
		    lonMin = Math.min(lonMin, curLon);
	    	
		    Overlay overlayitem = new RouteSegmentOverlay(point, route_nodes.get(i+1).getPoint(), selectedRoute);
		    mapOverlays.add(overlayitem);
	    }
    	
	}
	
	/******************************************************************************************************************
	 * 
	 *
	 ******************************************************************************************************************/
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	/******************************************************************************************************************
	 * 
	 *
	 ******************************************************************************************************************/
    @Override
    public void onBackPressed(){
    	finish();
    	
		Intent intent = new Intent(this, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Bundle extras = new Bundle();
		extras.putInt("uid",0);
		extras.putString("user", "user");
		intent.putExtras(extras);
		startActivity(intent);
    	
    }
}