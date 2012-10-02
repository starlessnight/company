package com.smartrek.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.smartrek.models.Route;
import com.smartrek.models.Trajectory;
import com.smartrek.models.User;
import com.smartrek.requests.RouteMapper;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.ui.overlays.PointOverlay;
import com.smartrek.ui.overlays.RouteDebugOverlay;
import com.smartrek.ui.overlays.RoutePathOverlay;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.PrerecordedTrajectory;
import com.smartrek.utils.RouteLink;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.StringUtil;
import com.smartrek.utils.ValidationParameters;

public class ValidationActivity extends Activity {
    
    public static final int DEFAULT_ZOOM_LEVEL = 18;
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);

    private MapView mapView;
    private TextView textViewMessage;
    private TextView textViewDistance;
    private TextView textViewRoadname;
    private ToggleButton buttonFollow;
    
    private Route route;
    private List<Overlay> mapOverlays;
    private int mapOverlayOffset = 1;
    
    private PointOverlay pointOverlay;
    
    private int numberOfLocationChanges = 0;
    private int numberOfInRoute = 0;
    
    private Time startTime;
    private Time endTime;
    
    // FIXME: Temporary
    private RouteNode nearestNode;
    
    // FIXME: Temporary
    private RouteLink nearestLink;
    
    private Trajectory trajectory = new Trajectory();
    
    private LocationManager locationManager;

    private LocationListener locationListener;
    
    private Location lastKnownLocation;
    
    private ValidationTimeoutNotifier validationTimeoutNotifier;
    
    private Handler validationTimeoutHandler;
    
    private FakeLocationService fakeLocationService;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_reservation_map);
        
        Bundle extras = getIntent().getExtras();
        route = extras.getParcelable("route");
        route.preprocessNodes();
        
        initViews();
        
        MapController mc = mapView.getController();
        mc.setZoom(DEFAULT_ZOOM_LEVEL);
        
        if (route.getFirstNode() != null) {
            mc.setCenter(route.getFirstNode().getGeoPoint());
        }
        
        preparePingSound();
        
        drawRoute(mapView, route, 0);
        
        // Define a listener that responds to location updates
        locationListener = new ValidationLocationListener();
        
        // FIXME: Use long type
        startTime = new Time();
        startTime.setToNow();
        
        validationTimeoutNotifier = new ValidationTimeoutNotifier();
        validationTimeoutHandler = new Handler();
        validationTimeoutHandler.postDelayed(validationTimeoutNotifier, (900 + route.getDuration()*3) * 1000);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        SharedPreferences debugPrefs = getSharedPreferences(DebugOptionsActivity.DEBUG_PREFS, MODE_PRIVATE);

        // Register the listener with the Location Manager to receive location updates
        int gpsMode = debugPrefs.getInt(DebugOptionsActivity.GPS_MODE, DebugOptionsActivity.GPS_MODE_DEFAULT);
        if (gpsMode == DebugOptionsActivity.GPS_MODE_REAL) {
            prepareGPS();
        }
        else if (gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED) {
            fakeLocationService = new FakeLocationService(locationListener);
        }
        else {
            
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // TODO: Pause location service
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.validation, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        MainMenu.onMenuItemSelected(this, featureId, item);
        
        switch (item.getItemId()) {
        case R.id.stop:
            cancelValidation();
            break;
        }
        
        return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle the back button
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            cancelValidation();

            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }

    }
    
    private void initViews() {
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(false);
        mapView.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                buttonFollow.setChecked(false);
                return false;
            }
        });
        
        /* Create a ImageView with a zoomIn-Icon. */
        final ImageView imageViewZoomIn = (ImageView) findViewById(R.id.image_view_zoom_in);
        imageViewZoomIn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                        mapView.getController().zoomIn();
                }
        });
        
        final ImageView imageViewZoomOut = (ImageView) findViewById(R.id.image_view_zoom_out);
        imageViewZoomOut.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                        mapView.getController().zoomOut();
                }
        });
        
        textViewMessage = (TextView) findViewById(R.id.text_view_message);
        textViewDistance = (TextView) findViewById(R.id.text_view_distance);
        textViewRoadname = (TextView) findViewById(R.id.text_view_roadname);
        
        buttonFollow = (ToggleButton) findViewById(R.id.button_follow);
        buttonFollow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (buttonFollow.isChecked()) {
                    if (lastKnownLocation != null) {
                    	double latitude = lastKnownLocation.getLatitude();
                    	double longitude = lastKnownLocation.getLongitude();
                    	MapController mc = mapView.getController();
                    	mc.animateTo(latitude, longitude);
                    	mc.setZoom(DEFAULT_ZOOM_LEVEL);
                    }
                }
                else {
                    
                }
            }
        });
        
        ((View) findViewById(R.id.layout_navigation)).getBackground().setAlpha(220);
    }
    
    private void prepareGPS() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertNoGPS();
        }
        else {
            // TODO: Turn on GSP early
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 25, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, locationListener);
        }
    }
    
    /**
     * Directly copied from http://stackoverflow.com/questions/843675/how-do-i-find-out-if-the-gps-of-an-android-device-is-enabled
     */
    private void alertNoGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Yout GPS seems to be disabled. Do you want to enable it?")
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                       startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                   }
               });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public synchronized int[] drawRoute (MapView mapView, Route route, int routeNum) {
        mapOverlays = mapView.getOverlays();
        Log.d("ValidationActivity", String.format("mapOverlays has %d items", mapOverlays.size()));
        
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
            GeoPoint point = routeNodes.get(i).getGeoPoint();
            
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
        }
        
        RoutePathOverlay pathOverlay = new RoutePathOverlay(this, route, Color.DKGRAY);
        mapOverlays.add(pathOverlay);
        
        pointOverlay = new PointOverlay(this, 0, 0);
        pointOverlay.setColor(0xCC2020DF);
        mapOverlays.add(pointOverlay);
        
        RouteDebugOverlay debugOverlay = new RouteDebugOverlay(this);
        debugOverlay.setActionListener(new RouteDebugOverlay.ActionListener() {
			
			@Override
			public void onLongPress(double latitude, double longitude) {
                Location location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setTime(System.currentTimeMillis());
				locationChanged(location);
			}
			
		});
        mapOverlays.add(debugOverlay);
        
        route.setUserId(User.getCurrentUser(this).getId());
        
        /* Add offset of 1000 to range so that map displays extra space around route. */
        int [] range = {latMax - latMin + 1500 ,lonMax - lonMin + 1500};
        
        /* Return the range to doRoute so that map can be adjusted to range settings */
        return range;
    }
    
    private int seq = 1;
    
    private void sendTrajectory() {
        new SendTrajectoryTask().execute(seq++, User.getCurrentUser(this).getId());
    }
    
    private MediaPlayer mediaPlayer;
    
    private void preparePingSound() {
    	Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mediaPlayer = MediaPlayer.create(ValidationActivity.this, alert);
        
        try {
			mediaPlayer.prepare();
		}
		catch (IllegalStateException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // FIXME: Temporary
    private void playPingSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }
    
    private void showNavigationInformation(final Location location, final RouteNode node) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        
        runOnUiThread(new Runnable() {
            public void run() {
                double distance = route.getDistanceToNextTurn(latitude, longitude);
                double distanceInMile = distance * 0.000621371;
                double distanceInFoot = distance * 3.28084;
                
                String distancePresentation = null;
                if (distanceInFoot < 1000) {
                    distancePresentation = String.format("%.0f ft", distanceInFoot);
                }
                else {
                    distancePresentation = String.format("%.1f mi", distanceInMile);
                }
                
                //String message = String.format("%s in %s", StringUtil.capitalizeFirstLetter(node.getMessage()), distancePresentation);
                textViewMessage.setText(StringUtil.capitalizeFirstLetter(node.getMessage()));
                textViewDistance.setText(distancePresentation);
                textViewRoadname.setText(node.getRoadName());
                
                // FIXME: Temporary
                if (node.hasMetadata()) {
                    RouteNode.Metadata metadata = node.getMetadata();
                
                    if (!metadata.pingFlags[0] && distanceInFoot <= 500) {
                        metadata.pingFlags[0] = true;
                        playPingSound();
                    }
                    else if (metadata.pingFlags[1] && distanceInMile <= 1.0) {
                        metadata.pingFlags[1] = true;
                        playPingSound();
                    }
                    else if (!metadata.pingFlags[2] && distanceInMile <= 2.0) {
                        metadata.pingFlags[2] = true;
                        playPingSound();
                    }
                }
            }
        });
    }
    
    private synchronized void locationChanged(Location location) {
        numberOfLocationChanges += 1;
        
        trajectory.accumulate(location);
        
        // FIXME: Need to refactor
        ViewGroup textViewNavigation = (ViewGroup) findViewById(R.id.text_view_navigation);
        if (textViewNavigation.getVisibility() == View.INVISIBLE) {
            textViewNavigation.setVisibility(View.VISIBLE);
            
            TextView textViewWaiting = (TextView) findViewById(R.id.text_view_waiting);
            textViewWaiting.setVisibility(View.INVISIBLE);
        }
        
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        
        if (buttonFollow.isChecked()) {
            mapView.getController().animateTo(lat, lng);
        }
        
        pointOverlay.setLocation((float)lat, (float)lng);
        
        //nearestNode = route.getNearestNode(lat, lng);
        nearestLink = route.getNearestLink(lat, lng);
        nearestNode = nearestLink.getEndNode();
        
        List<RouteNode> routeNodes = route.getNodes();
        
        ValidationParameters params = ValidationParameters.getInstance();
        
        double distanceToLink = nearestLink.distanceTo(lat, lng);
        if (distanceToLink <= params.getDistanceThreshold()) {
            numberOfInRoute += 1;
        }
        
        if (nearestNode.getFlag() != 0) {
            showNavigationInformation(location, nearestNode);
        }
        else {
            // find the closest RouteNode with a non-zero flag
            RouteNode node = nearestNode;
            while (node.getNextNode() != null) {
                node = node.getNextNode();
                if (node.getFlag() != 0) {
                    showNavigationInformation(location, node);
                    break;
                }
            }
        }
        
        
        mapView.postInvalidate();
        
        if (trajectory.size() >= 8) {
            sendTrajectory();
        }
        
        if (route.hasArrivedAtDestination(lat, lng)) {
            deactivateLocationService();
            arriveAtDestination();
            Log.d("ValidationActivity", "Arriving at destination");
            
            try {
                Log.d("ValidationActivity", "trajectory = " + trajectory.toJSON().toString());
            }
            catch (JSONException e) {
                ehs.registerException(e);
            }
        }
        lastKnownLocation = location;
    }
    
    private void arriveAtDestination() {
        locationManager.removeUpdates(locationListener);
        validationTimeoutHandler.removeCallbacks(validationTimeoutNotifier);
        
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
        
        finish();
    }
    
    private void deactivateLocationService() {
        if (fakeLocationService != null) {
            fakeLocationService.cancel();
        }
    }
    
    private void cancelValidation() {
        // Ask the user if they want to quit
        new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("Confirm")
        .setMessage("Are you sure you want to stop this trip?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                deactivateLocationService();
                
                //Stop the activity
                ValidationActivity.this.finish();    
            }

        })
        .setNegativeButton("No", null)
        .show();
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
    
    private class ValidationTimeoutNotifier extends Thread {
        @Override
        public void run() {
            if (endTime == null) {
                endTime = new Time();
                endTime.setToNow();
            }
            
            Intent intent = new Intent(ValidationActivity.this, ValidationReportActivity.class);
            intent.putExtra("route", route);
            intent.putExtra("timedout", true);
            intent.putExtra("numberOfLocationChanges", numberOfLocationChanges);
            intent.putExtra("numberOfInRoute", numberOfInRoute);
            intent.putExtra("startTime", startTime.toMillis(false));
            intent.putExtra("endTime", endTime.toMillis(false));
            startActivity(intent);
            
            finish();
        }
    }
    
    /**
     * Fake data player
     */
    private class FakeLocationService extends TimerTask {
        private Timer timer;
        private LocationListener listener;
        //private Queue<RouteNode> nodes;
        
        private Queue<GeoPoint> trajectory;
        
        @SuppressWarnings("unchecked")
        public FakeLocationService(LocationListener listener) {
            this.listener = listener;
            
            try {
                InputStream in = getResources().getAssets().open("trajectory.csv");
                trajectory = (Queue<GeoPoint>) PrerecordedTrajectory.read(in);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            
            timer = new Timer();
            timer.schedule(this, 1000, 500);
        }

        @Override
        public void run() {
            if (trajectory == null || trajectory.isEmpty()) {
                timer.cancel();
            }
            else {
                GeoPoint geoPoint = trajectory.poll();
                Location location = new Location("");
                location.setLatitude(geoPoint.getLatitude());
                location.setLongitude(geoPoint.getLongitude());
                location.setTime(System.currentTimeMillis());
                listener.onLocationChanged(location);
            }
        }
    }
    
    private class SendTrajectoryTask extends AsyncTask<Object, Object, Object> {

        private RouteMapper mapper = new RouteMapper();
        
        @Override
        protected Object doInBackground(Object... params) {
            int seq = (Integer) params[0];
            int uid = (Integer) params[1];
            
            Log.d("ValidationActivity", "sendTrajectory()");
            try {
                mapper.sendTrajectory(seq, uid, route.getId(), trajectory);
            }
            catch (Exception e) {
                ehs.registerException(e);
            }
            
            return null;
        }
        
        @Override
        protected void onPostExecute(Object result) {
            // Silently report exceptions
            while (ehs.hasExceptions()) {
                System.err.print(ehs.popException());
            }
        }
    }
}
