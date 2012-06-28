package com.smartrek.activities;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.smartrek.mappers.RouteMapper;
import com.smartrek.models.Route;
import com.smartrek.models.Trajectory;
import com.smartrek.models.User;
import com.smartrek.overlays.PointOverlay;
import com.smartrek.overlays.RouteSegmentOverlay;
import com.smartrek.utils.RouteLink;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.ValidationParameters;
import com.smartrek.utils.ValidationService;

public class ValidationActivity extends MapActivity {

    private MapView mapView;
    private Route route;
    private List<Overlay> mapOverlays;
    private int mapOverlayOffset = 1;
    
    private PointOverlay pointOverlay;
    
    private PointOverlay startNodeOverlay;
    private PointOverlay endNodeOverlay;
    
    private int numberOfLocationChanges = 0;
    private int numberOfInRoute = 0;
    
    private Time startTime;
    private Time endTime;
    
    // FIXME: Temporary
    private RouteNode nearestNode;
    
    // FIXME: Temporary
    private RouteLink nearestLink;
    
    private Trajectory trajectory = new Trajectory();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_reservation_map);
        
        Bundle extras = getIntent().getExtras();
        route = extras.getParcelable("route");
        
        RouteMapper.buildRouteNodeReferenceChain(route.getNodes());
        
        mapView = (MapView) findViewById(R.id.mapview);
        drawRoute(mapView, route, 0);
        
        Log.d(getClass().toString(), String.format("route = %s", route));
        
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        // Define a listener that responds to location updates
        LocationListener locationListener = new ValidationLocationListener();

        // Register the listener with the Location Manager to receive location updates
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5, 25, locationListener);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 1, locationListener);
        FakeLocationService faceLocationService = new FakeLocationService(locationListener);

        startTime = new Time();
        startTime.setToNow();
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
            
            RouteSegmentOverlay overlay = new RouteSegmentOverlay(point, routeNodes.get(i+1).getPoint(), routeNum);
            overlay.setColor(Color.DKGRAY);
            mapOverlays.add(overlay);
        }
        
        pointOverlay = new PointOverlay(0, 0);
        pointOverlay.setColor(Color.BLUE);
        mapOverlays.add(pointOverlay);
        
        startNodeOverlay = new PointOverlay(0, 0);
        startNodeOverlay.setColor(Color.GRAY);
        mapOverlays.add(startNodeOverlay);
        
        endNodeOverlay = new PointOverlay(0, 0);
        endNodeOverlay.setColor(Color.MAGENTA);
        mapOverlays.add(endNodeOverlay);
        
        route.setUserId(User.getCurrentUser(this).getId());
        
        drawable = this.getResources().getDrawable(R.drawable.routetag);
        
        /* Add offset of 1000 to range so that map displays extra space around route. */
        int [] range = {latMax - latMin + 1500 ,lonMax - lonMin + 1500};
        
        /* Return the range to doRoute so that map can be adjusted to range settings */
        return range;
    }
    
    // FIXME: This function must be called asynchronously
    private void sendTrajectory() {
    	Log.d("ValidationActivity", "sendTrajectory()");
    	RouteMapper mapper = new RouteMapper();
    	try {
			mapper.sendTrajectory(User.getCurrentUser(this).getId(), route.getId(), trajectory);
		}
    	catch (ClientProtocolException e) {
			e.printStackTrace();
		}
    	catch (JSONException e) {
			e.printStackTrace();
		}
    	catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private synchronized void locationChanged(Location location) {
    	numberOfLocationChanges += 1;
    	
    	trajectory.accumulate(location);
    	
        float lat = (float) location.getLatitude();
        float lng = (float) location.getLongitude();
        
        pointOverlay.setLocation(lat, lng);
        
        nearestNode = ValidationService.getNearestNode(route.getNodes(), lat, lng);
        nearestLink = ValidationService.getNearestLink(nearestNode, lat, lng);
        
        //Log.d("ValidationActivity", "nearest node = " + nearestNode);
    	
    	List<RouteNode> routeNodes = route.getNodes();
        
    	// FIXME: There's gotta be a better solution
    	for (int i = 0; i < routeNodes.size() - mapOverlayOffset; i++) {
    		RouteSegmentOverlay overlay = (RouteSegmentOverlay) mapOverlays.get(i);
    		overlay.setColor(Color.DKGRAY);
    	}
    	RouteSegmentOverlay overlay = (RouteSegmentOverlay) mapOverlays.get(nearestLink.getStartNode().getNodeIndex());
    	
//    	RouteNode startNode = nearestLink.getStartNode();
//    	startNodeOverlay.setLocation(startNode.getLatitude(), startNode.getLongitude());
//    	
//    	RouteNode endNode = nearestLink.getEndNode();
//    	endNodeOverlay.setLocation(endNode.getLatitude(), endNode.getLongitude());
    	
    	ValidationParameters params = ValidationParameters.getInstance();
        
        float distanceToLink = nearestLink.distanceTo(lat, lng);
        if (distanceToLink <= params.getDistanceThreshold()) {
        	numberOfInRoute += 1;
        	overlay.setColor(Color.GREEN);
        	//Log.d("ValidationActivity", String.format("In route, score = %d/%d = %.2f", numberOfInRoute, numberOfLocationChanges, numberOfInRoute/(float)numberOfLocationChanges));
        }
        else {
        	overlay.setColor(Color.RED);
        	//Log.d("ValidationActivity", String.format("Out of route, score = %d/%d = %.2f", numberOfInRoute, numberOfLocationChanges, numberOfInRoute/(float)numberOfLocationChanges));
        }
    	
    	mapView.postInvalidate();
    	
    	if (trajectory.size() >= 3) {
    		sendTrajectory();
    	}
        
        if (route.hasArrivedAtDestination(lat, lng)) {
        	deactivateLocationService();
        	arriveAtDestination();
        	Log.d("ValidationActivity", "Arriving at destination");
        	
        	try {
				Log.d("ValidationActivity", "trajectory = " + trajectory.toJSON().toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
    }
    
    private void arriveAtDestination() {
    	endTime = new Time();
    	endTime.setToNow();
    	
    	sendTrajectory();
    	
    	Intent intent = new Intent(this, ValidationReportActivity.class);
    	intent.putExtra("route", route);
    	intent.putExtra("numberOfLocationChanges", numberOfLocationChanges);
    	intent.putExtra("numberOfInRoute", numberOfInRoute);
    	intent.putExtra("startTime", startTime.toMillis(false));
    	intent.putExtra("endTime", endTime.toMillis(false));
    	startActivity(intent);
    }
    
    private void deactivateLocationService() {
    	
    }
    
    private class ValidationLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            Log.d(this.getClass().toString(), String.format("onLocationChanged: %s", location));
            
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
    
    /**
     * Fake data player
     */
    private class FakeLocationService extends TimerTask {
    	private Timer timer;
    	private LocationListener listener;
    	private Queue<RouteNode> nodes;
    	
    	public FakeLocationService(LocationListener listener) {
    		this.listener = listener;
    		
    		timer = new Timer();
    		timer.schedule(this, 1000, 100);
    		
    		nodes = new LinkedList<RouteNode>();
    		nodes.add(new RouteNode(32.234294f, -110.956807f, 0, 0));
    		nodes.add(new RouteNode(32.2361f,-110.959468f, 0, 0));
    		nodes.add(new RouteNode(32.240356f, -110.959425f, 0, 0));
    		nodes.add(new RouteNode(32.242997f,-110.959532f, 0, 0));
    		nodes.add(new RouteNode(32.248777f,-110.960712f, 0, 0));
    		nodes.add(new RouteNode(32.254039f,-110.958899f, 0, 0));
    		nodes.add(new RouteNode(32.259411f, -110.961571f, 0, 0));
    		nodes.add(new RouteNode(32.257578f,-110.959811f, 0, 0));
    		nodes.add(new RouteNode(32.26128f,-110.960938f, 0, 0));
    		nodes.add(new RouteNode(32.264927f, -110.962343f, 0, 0));
    		nodes.add(new RouteNode(32.26489f, -110.958095f, 0, 0));
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
				location.setTime(System.currentTimeMillis());
				listener.onLocationChanged(location);
			}
		}
    }
}
