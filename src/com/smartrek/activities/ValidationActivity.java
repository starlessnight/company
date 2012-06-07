package com.smartrek.activities;

import java.util.List;

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
import com.smartrek.overlays.RouteOverlay;
import com.smartrek.overlays.RouteSegmentOverlay;
import com.smartrek.utils.RouteNode;

public class ValidationActivity extends MapActivity {

    private MapView mapView;
    private Route route;
    private List<Overlay> mapOverlays;
    
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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5, 1, locationListener);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return true;
    }
    
    public synchronized int[] drawRoute (MapView mapView, Route route, int routeNum) {
        mapOverlays = mapView.getOverlays();
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
    
    private class ValidationLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            Log.d(this.getClass().toString(), String.format("onLocationChanged: %s", location));
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
}
