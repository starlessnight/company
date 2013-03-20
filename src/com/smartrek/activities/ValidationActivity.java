package com.smartrek.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.activities.DebugOptionsActivity.FakeRoute;
import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.models.Trajectory;
import com.smartrek.models.User;
import com.smartrek.requests.RouteFetchRequest;
import com.smartrek.requests.SendTrajectoryRequest;
import com.smartrek.ui.NavigationView;
import com.smartrek.ui.NavigationView.CheckPointListener;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.ui.overlays.PointOverlay;
import com.smartrek.ui.overlays.RouteDebugOverlay;
import com.smartrek.ui.overlays.RoutePathOverlay;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Misc;
import com.smartrek.utils.PrerecordedTrajectory;
import com.smartrek.utils.RouteLink;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.SmartrekTileProvider;
import com.smartrek.utils.SystemService;
import com.smartrek.utils.ValidationParameters;

public final class ValidationActivity extends ActionBarActivity implements OnInitListener {
    
    public static final int DEFAULT_ZOOM_LEVEL = 18;
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);

    private MapView mapView;
    private NavigationView navigationView;
    private ToggleButton buttonFollow;
    
    /**
     * @deprecated
     */
    private Route route;
    
    private Reservation reservation;
    
    private List<Overlay> mapOverlays;
    
    private PointOverlay pointOverlay;
    
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
    
    private AtomicBoolean arrived = new AtomicBoolean(false);
    
    private static final int ttsCheckCode = 1;
    
    private TextToSpeech mTts;
    
    private ListView dirListView;
    
    private ArrayAdapter<String> dirListadapter;
    
    private static String utteranceId = "utteranceId";

    private AtomicInteger utteringCnt = new AtomicInteger();
    
    private AtomicInteger utteredCnt = new AtomicInteger();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_reservation_map);
        
        Bundle extras = getIntent().getExtras();
        reservation = extras.getParcelable("reservation");
        
        route = extras.getParcelable("route");
        
        // Define a listener that responds to location updates
        locationListener = new ValidationLocationListener();
        
        // FIXME: Use long type
        startTime = new Time();
        startTime.setToNow();
        
        validationTimeoutNotifier = new ValidationTimeoutNotifier();
        validationTimeoutHandler = new Handler();
        validationTimeoutHandler.postDelayed(validationTimeoutNotifier, (900 + route.getDuration()*3) * 1000);
        
        Intent checkTtsIntent = new Intent();
        checkTtsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTtsIntent, ttsCheckCode);
        
        final FakeRoute fakeRoute = DebugOptionsActivity.getFakeRoute(
            ValidationActivity.this, route.getId()); 
        if(fakeRoute == null){
            route.preprocessNodes();
        }else{
            new AsyncTask<Void, Void, List<Route>>() {
                @Override
                protected List<Route> doInBackground(Void... params) {
                    List<Route> routes = null;
                    try {
                        RouteFetchRequest request = new RouteFetchRequest(route.getDepartureTime());
                        routes = request.execute();
                    }
                    catch(Exception e) {
                        ehs.registerException(e);
                    }                                
                    return routes;
                }
                protected void onPostExecute(java.util.List<Route> routes) {
                    if (ehs.hasExceptions()) {
                        ehs.reportExceptions(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    }else if(routes != null && routes.size() > 0) {
                        route = routes.get(fakeRoute.seq);
                        route.preprocessNodes();
                    }
                }
            }.execute();
        }
        
        dirListadapter = new ArrayAdapter<String>(this, R.layout.direction_list_item, R.id.direction_text){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Font.setTypeface(boldFont, (TextView)view.findViewById( R.id.direction_text));
                view.setBackgroundResource(position == 0?R.color.light_green:0);
                return view;
            }
        };
        
        initViews();
        
        MapController mc = mapView.getController();
        mc.setZoom(DEFAULT_ZOOM_LEVEL);
        
        if (route.getFirstNode() != null) {
            mc.setCenter(route.getFirstNode().getGeoPoint());
        }
        
        drawRoute(mapView, route, 0);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	EasyTracker.getInstance().activityStart(this);
        
    	if (reservation.hasExpired()) {
        	NotificationDialog dialog = new NotificationDialog(this, getResources().getString(R.string.trip_has_expired));
        	dialog.setActionListener(new NotificationDialog.ActionListener() {
				
				@Override
				public void onClickDismiss() {
					finish();
				}
			});
        	dialog.show();
        }
        else if (reservation.isTooEarlyToStart()) {
        	NotificationDialog dialog = new NotificationDialog(this, getResources().getString(R.string.trip_too_early_to_start));
        	dialog.setActionListener(new NotificationDialog.ActionListener() {
				
				@Override
				public void onClickDismiss() {
					finish();
				}
			});
        	dialog.show();
        }
    }
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
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
            if(fakeLocationService == null){
                fakeLocationService = new FakeLocationService(locationListener);
            }
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
        MenuInflater mi = getSupportMenuInflater();
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
        Misc.disableHardwareAcceleration(mapView);
        CloudmadeUtil.retrieveCloudmadeKey(this);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setTileSource(new SmartrekTileProvider());
        mapView.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                buttonFollow.setChecked(false);
                return false;
            }
        });
        
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setTypeface(boldFont);
        
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
        
        TextView dirSwitch = (TextView)findViewById(R.id.directions_switch);
        dirSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (View mView : getMapViews()) {
                    mView.setVisibility(View.INVISIBLE);
                }
                findViewById(R.id.directions_view).setVisibility(View.VISIBLE);
            }
        });
        
        TextView mapViewSwitch = (TextView)findViewById(R.id.mapview_switch);
        mapViewSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (View mView : getMapViews()) {
                    mView.setVisibility(View.VISIBLE);
                }
                findViewById(R.id.directions_view).setVisibility(View.INVISIBLE);
            }
        });
        
        dirListView = (ListView) findViewById(R.id.directions_list);
        dirListView.setAdapter(dirListadapter);
        
        Font.setTypeface(boldFont, dirSwitch, mapViewSwitch);
    }
    
    private View[] getMapViews(){
        return new View[]{findViewById(R.id.mapview), 
                findViewById(R.id.navigation_view), findViewById(R.id.mapview_options)};
    }
    
    private void prepareGPS() {
        // Acquire a reference to the system Location Manager
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            SystemService.alertNoGPS(this);
        }
        else {
            // TODO: Turn on GSP early
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 25, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, locationListener);
        }
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
    
    private void showNavigationInformation(final Location location, final RouteNode node) {
        Log.d("ValidationActivity", "showNavigationInformation()");
        runOnUiThread(new Runnable() {
            public void run() {
            	navigationView.update(route, location, node);
            	dirListadapter.clear();
            	RouteNode nextNode = node;
                double distance = 0;
            	do {
                    if (nextNode.getFlag() != 0) {
                        distance += nextNode == node? 
                            route.getDistanceToNextTurn(location.getLatitude(), 
                                location.getLongitude())
                            :nextNode.getDistance(); 
                        dirListadapter.add(NavigationView.getDirection(nextNode, distance));
                    }
            	} while ((nextNode = nextNode.getNextNode()) != null);
            }
        });
    }
    
    private synchronized void locationChanged(Location location) {
        trajectory.accumulate(location);
        
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        
        if (buttonFollow.isChecked()) {
            mapView.getController().animateTo(lat, lng);
        }
        
        pointOverlay.setLocation((float)lat, (float)lng);
        
        //nearestNode = route.getNearestNode(lat, lng);
        nearestLink = route.getNearestLink(lat, lng);
        nearestNode = nearestLink.getEndNode();
        
        ValidationParameters params = ValidationParameters.getInstance();
        
        double distanceToLink = nearestLink.distanceTo(lat, lng);
        if (distanceToLink <= params.getValidationDistanceThreshold()) {
            nearestLink.getStartNode().getMetadata().setValidated(true);
        }
        
        int numberOfValidatedNodes = 0;
        for (RouteNode node : route.getNodes()) {
        	if (node.getMetadata().isValidated()) {
        		numberOfValidatedNodes += 1;
        	}
        }
        Log.d("ValidationActivity", String.format("%d/%d", numberOfValidatedNodes, route.getNodes().size()));
        
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
        
        if (!arrived.get() && route.hasArrivedAtDestination(lat, lng)) {
            arrived.set(true);
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
        validationTimeoutHandler.removeCallbacks(validationTimeoutNotifier);
        
        endTime = new Time();
        endTime.setToNow();
        
        sendTrajectory();
        
        if(mTts == null){
            reportValidation();
        }else{
            final int oldCnt = utteredCnt.get();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    int newCnt = utteredCnt.get();
                    if(newCnt == oldCnt && newCnt == utteringCnt.get()){
                        reportValidation();
                    }
                }
            }, Math.round(Math.random() * 1000));
        }
    }
    
    private void reportValidation(){
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        
        deactivateLocationService();
        
        Intent intent = new Intent(this, ValidationReportActivity.class);
        intent.putExtra("route", route);
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

        @Override
        protected Object doInBackground(Object... params) {
            int seq = (Integer) params[0];
            int uid = (Integer) params[1];
            
            SendTrajectoryRequest request = new SendTrajectoryRequest();
            try {
            	request.execute(seq, uid, route.getId(), trajectory);
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
    
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (requestCode == ttsCheckCode) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                mTts = new TextToSpeech(this, this);
                navigationView.setListener(new CheckPointListener() {
                    @Override
                    public void onCheckPoint(final String navText) {
                        utteringCnt.incrementAndGet();
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
                        mTts.speak(navText, TextToSpeech.QUEUE_ADD, params);
                    }
                });
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                    TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    @Override
    public void onInit(int status) {
        if(mTts != null){
            mTts.setLanguage(Locale.US);            
            mTts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    if(utteredCnt.incrementAndGet() == utteringCnt.get() && arrived.get()){
                        reportValidation();
                    }
                }
            });
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        validationTimeoutHandler.removeCallbacks(validationTimeoutNotifier);
        if(mTts != null){
            mTts.shutdown();
        }
    }
    
}
