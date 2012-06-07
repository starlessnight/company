package com.smartrek.activities;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.overlays.RouteSegmentOverlay;
import com.smartrek.utils.RouteLink;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.ValidationService;

public class ValidationActivity extends MapActivity {

    private MapView mapView;
    private Route route;
    private List<Overlay> mapOverlays;
    
    // FIXME: Temporary
    private RouteNode nearestNode;
    
    // FIXME: Temporary
    private RouteLink nearestLink;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_reservation_map);
        
        Bundle extras = getIntent().getExtras();
        route = extras.getParcelable("route");
        
        mapView = (MapView) findViewById(R.id.mapview);
        drawRoute(mapView, route, 0);
        
        Log.d(getClass().toString(), String.format("route = %s", route));
        
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        // Define a listener that responds to location updates
        LocationListener locationListener = new ValidationLocationListener();

        // Register the listener with the Location Manager to receive location updates
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 1, locationListener);
        FakeLocationService fls = new FakeLocationService(locationListener);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return true;
    }
    
    public synchronized int[] drawRoute (MapView mapView, Route route, int routeNum) {
        mapOverlays = mapView.getOverlays();
        Log.d("ValidationActivity", String.format("mapOverlays has %d items", mapOverlays.size()));
        Drawable drawable;
        
        if(routeNum == 0)
            mapOverlays.clear();
        
        int latMax = (int)(-81 * 1E6);
        int lonMax = (int)(-181 * 1E6);
        int latMin = (int)(+81 * 1E6);
        int lonMin = (int)(+181 * 1E6);
         
        List<RouteNode> routeNodes = route.getNodes();
        
        int lat = 0;
        int lon = 0;
        
        for(int i = 0; i < routeNodes.size()-1; i++) {
            GeoPoint point = routeNodes.get(i).getPoint();
            
            int curLat = point.getLatitudeE6();
            int curLon = point.getLongitudeE6();
            
            if(i == routeNodes.size()/2){
                lat = curLat + 500;
                lon = curLon+ 150;
            }
            
            latMax = Math.max(latMax, curLat);
            lonMax = Math.max(lonMax, curLon);
            latMin = Math.min(latMin, curLat);
            lonMin = Math.min(lonMin, curLon);
            
            Overlay overlayitem = new RouteSegmentOverlay(point, routeNodes.get(i+1).getPoint(), routeNum);
            mapOverlays.add(overlayitem);
        }
        
        // FIXME:
        route.setUserId(User.getCurrentUser(this).getId());
        
        drawable = this.getResources().getDrawable(R.drawable.routetag);
        
        /* Log selected time to debug */
        //Log.d("RouteActivity", "In RouteActivity setting route time");
        //Log.d("RouteActivity", selectedTime.format3339(false));
        
        /* Add offset of 1000 to range so that map displays extra space around route. */
        int [] range = {latMax - latMin + 1500 ,lonMax - lonMin + 1500};
        
        /* Log range values to debug */
        //Log.d("RouteActivity", " Latitude Range:" + range[0]);
        //Log.d("RouteActivity", " Longitude Range:" + range[1]);
        
        
        //KdTree.Node root = KdTree.build(routeNodes, 0, routeNodes.size(), 0);

        /* Return the range to doRoute so that map can be adjusted to range settings */
        return range;
    }
    
    private void locationChanged(Location location) {
    	int nearestNodeIndex = -1;
    	List<RouteNode> routeNodes = route.getNodes();
        for(int i = 0; i < routeNodes.size(); i++) {
        	if (routeNodes.get(i).equals(nearestNode)) {
        		Log.d("ValidationActivity", "nearest node index = " + i);
        		nearestNodeIndex = i;
        	}
        }
        
        if (nearestNodeIndex == routeNodes.size() - 1) {
        	Log.d("ValidationActivity", "Arriving at the destination. Terminating validation process.");
        	
        	for (Overlay overlay : mapOverlays) {
        		((RouteSegmentOverlay) overlay).setColorNum(2);
        	}
        	mapView.postInvalidate();
        }
        else if (nearestNodeIndex >= 0) {
        	// FIXME: There's gotta be a better solution
        	for (Overlay overlay : mapOverlays) {
        		((RouteSegmentOverlay) overlay).setColorNum(0);
        	}
        	RouteSegmentOverlay overlay = (RouteSegmentOverlay) mapOverlays.get(nearestNodeIndex);
        	overlay.setColorNum(1);
        	mapView.postInvalidate();
        }
    }
    
    private class ValidationLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            Log.d(this.getClass().toString(), String.format("onLocationChanged: %s", location));
            
            float lat = (float) location.getLatitude();
            float lng = (float) location.getLongitude();
            nearestNode = ValidationService.getNearestNode(route.getNodes(), lat, lng);
            nearestLink = ValidationService.getNearestLink(nearestNode, lat, lng);
            
            Log.d("ValidationActivity", "nearest node = " + nearestNode);
            
            locationChanged(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(this.getClass().toString(), String.format("onStatusChanged: %s, %d, %s", provider, status, extras));
        }

        public void onProviderEnabled(String provider) {
            Log.d(this.getClass().toString(), String.format("onProviderEnabled: %s", provider));
        }

        public void onProviderDisabled(String provider) {
            Log.d(this.getClass().toString(), String.format("onProviderDisabled: %s", provider));
        }
    }
    
    private class FakeLocationService extends TimerTask {
    	private Timer timer;
    	private LocationListener listener;
    	private Queue<RouteNode> nodes;
    	
    	public FakeLocationService(LocationListener listener) {
    		this.listener = listener;
    		
    		timer = new Timer();
    		timer.schedule(this, 1000, 2000);
    		
    		nodes = new LinkedList<RouteNode>();
    		nodes.add(new RouteNode(32.2361f,-110.959468f, 0, 0));
    		nodes.add(new RouteNode(32.242997f,-110.959532f, 0, 0));
    		nodes.add(new RouteNode(32.248777f,-110.960712f, 0, 0));
    		nodes.add(new RouteNode(32.254039f,-110.958899f, 0, 0));
    		nodes.add(new RouteNode(32.257578f,-110.959811f, 0, 0));
    		nodes.add(new RouteNode(32.26128f,-110.960938f, 0, 0));
    		nodes.add(new RouteNode(32.264791f,-110.953245f, 0, 0));
    		//nodes.add(new RouteNode(, 0, 0));
    	}

		@Override
		public void run() {
			if (nodes.isEmpty()) {
				timer.cancel();
			}
			else {
				RouteNode node = nodes.poll();
				Location location = new Location("");
				location.setLatitude(node.getLatitude());
				location.setLongitude(node.getLongitude());
				listener.onLocationChanged(location);
			}
		}
    }
}
