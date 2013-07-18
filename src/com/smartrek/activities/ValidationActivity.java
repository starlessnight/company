package com.smartrek.activities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.SendTrajectoryService;
import com.smartrek.ValidationService;
import com.smartrek.activities.DebugOptionsActivity.FakeRoute;
import com.smartrek.activities.DebugOptionsActivity.NavigationLink;
import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.models.Trajectory;
import com.smartrek.models.User;
import com.smartrek.requests.Request;
import com.smartrek.requests.RouteFetchRequest;
import com.smartrek.ui.NavigationView;
import com.smartrek.ui.NavigationView.CheckPointListener;
import com.smartrek.ui.NavigationView.DirectionItem;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.ui.overlays.PointOverlay;
import com.smartrek.ui.overlays.RouteDebugOverlay;
import com.smartrek.ui.overlays.RoutePathOverlay;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Misc;
import com.smartrek.utils.PrerecordedTrajectory;
import com.smartrek.utils.RouteLink;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.RouteRect;
import com.smartrek.utils.SmartrekTileProvider;
import com.smartrek.utils.StringUtil;
import com.smartrek.utils.SystemService;
import com.smartrek.utils.ValidationParameters;

public final class ValidationActivity extends Activity implements OnInitListener {
    
    public static final int DEFAULT_ZOOM_LEVEL = 18;
    
    private static final String RESERVATION = "reservation";
    
    private static final String ROUTE = "route";
    
    private static final String START_TIME = "startTime";
    
    private static final String POLL_CNT = "pollCnt";
    
    private static final String GEO_POINT = "geoPoint";
    
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
    
    private long startTime;
    
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
    
    private TextToSpeech mTts;
    
    private ListView dirListView;
    
    private ArrayAdapter<DirectionItem> dirListadapter;
    
    private static String utteranceId = "utteranceId";

    private AtomicInteger utteringCnt = new AtomicInteger();
    
    private AtomicInteger utteredCnt = new AtomicInteger();
    
    private AtomicBoolean reported = new AtomicBoolean(false);
    
    private boolean isDebugging;
    
    private long lastLocChanged;
    
    private Handler animator;
    
    private Typeface boldFont;
    
    private Typeface lightFont;
    
    private int savedPollCnt;
    
    private MediaPlayer validationMusicPlayer;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_reservation_map);
        
        AssetManager assets = getAssets();
        boldFont = Font.getBold(assets);
        lightFont = Font.getLight(assets);
        
        animator = new Handler(Looper.getMainLooper());
        
        boolean isOnRecreate = savedInstanceState != null;
        
        Bundle extras = getIntent().getExtras();
        
        reservation = extras.getParcelable(RESERVATION);
        
        route = (isOnRecreate?savedInstanceState:extras).getParcelable(ROUTE);
        reservation.setRoute(route);
        
        // Define a listener that responds to location updates
        locationListener = new ValidationLocationListener();
        
        if(isOnRecreate){
            startTime = savedInstanceState.getLong(START_TIME);
            savedPollCnt = savedInstanceState.getInt(POLL_CNT);
        }else{
            Time now = new Time();
            now.setToNow();
            startTime = now.toMillis(false);
        }
        
        validationTimeoutNotifier = new ValidationTimeoutNotifier();
        validationTimeoutHandler = new Handler();
        validationTimeoutHandler.postDelayed(validationTimeoutNotifier, (900 + route.getDuration()*3) * 1000);
        
        dirListadapter = new ArrayAdapter<DirectionItem>(this, R.layout.direction_list_item, R.id.text_view_road){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView vRoad = (TextView)view.findViewById(R.id.text_view_road);
                TextView vDistance = (TextView)view.findViewById(R.id.text_view_distance);
                Font.setTypeface(boldFont, vRoad, vDistance);
                for(View v: new View[]{view.findViewById(R.id.left_panel), vRoad}){
                    v.setBackgroundResource(position == 0?R.color.light_green:0);
                }
                DirectionItem item = getItem(position);
                ImageView vDirection = (ImageView)view.findViewById(R.id.img_view_direction);
                if(item.drawableId == 0){
                    vDirection.setVisibility(View.INVISIBLE);
                }else{
                    vDirection.setImageResource(item.drawableId);
                    vDirection.setVisibility(View.VISIBLE);
                }
                vDistance.setText(StringUtil.formatImperialDistance(item.distance, true));
                vDistance.requestLayout();
                vRoad.setText((StringUtils.isBlank(item.roadName) 
                    || StringUtils.equalsIgnoreCase(item.roadName, "null"))
                    ?"":item.roadName);
                vRoad.requestLayout();
                return view;
            }
        };
        
        final NavigationLink navLink = DebugOptionsActivity.getNavLink(this, reservation.getRid());
        boolean hasNavLink = navLink != null;
        final FakeRoute fakeRoute = DebugOptionsActivity.getFakeRoute(
            ValidationActivity.this, route.getId());
        isDebugging = fakeRoute != null;
        if(!isOnRecreate && (isDebugging || hasNavLink)){
            new AsyncTask<Void, Void, List<Route>>() {
                @Override
                protected List<Route> doInBackground(Void... params) {
                    List<Route> routes = null;
                    try {
                        RouteFetchRequest request;
                        if(isDebugging){
                            request = new RouteFetchRequest(route.getDepartureTime());
                        }else{
                            request = new RouteFetchRequest(navLink.url, 
                                reservation.getDepartureTime(), reservation.getDuration());
                        }
                        routes = request.execute(ValidationActivity.this);
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
                        Route oldRoute = route; 
                        route = routes.get(isDebugging?fakeRoute.seq:0);
                        route.setId(oldRoute.getId());
                        reservation.setRoute(route);
                        route.preprocessNodes();
                        routeRect = initRouteRect(route);
                        updateDirectionsList();
                    }
                }
            }.execute();
        }else{
            route.preprocessNodes();
            routeRect = initRouteRect(route);
            updateDirectionsList();
        }
        
        initViews();
        
        MapController mc = mapView.getController();
        mc.setZoom(DEFAULT_ZOOM_LEVEL);
        
        GeoPoint center = null;
        if(isOnRecreate){
            center = new GeoPoint((IGeoPoint)savedInstanceState.getParcelable(GEO_POINT));
        }else if (route.getFirstNode() != null) {
            center = route.getFirstNode().getGeoPoint();
        }
        
        if(center != null){
            mc.setCenter(center);
        }
        
        drawRoute(mapView, route, 0);
        
        try{
            mTts = new TextToSpeech(this, this);
        }catch(Throwable t){}
        
        lastLocChanged = SystemClock.elapsedRealtime();
        
        setVolumeControlStream(AudioManager.STREAM_NOTIFICATION);
        
        if(!isOnRecreate){
            if (reservation.hasExpired()) {
                NotificationDialog dialog = new NotificationDialog(this, getResources().getString(R.string.trip_has_expired));
                dialog.setActionListener(new NotificationDialog.ActionListener() {
                    
                    @Override
                    public void onClickDismiss() {
                        if(!isFinishing()){
                            finish();
                        }
                    }
                });
                dialog.show();
            }
            else if (reservation.isTooEarlyToStart()) {
                NotificationDialog dialog = new NotificationDialog(this, getResources().getString(R.string.trip_too_early_to_start));
                dialog.setActionListener(new NotificationDialog.ActionListener() {
                    
                    @Override
                    public void onClickDismiss() {
                        if(!isFinishing()){
                            finish();
                        }
                    }
                });
                dialog.show();
            }
        }
        
        validationMusicPlayer = new MediaPlayer();
        validationMusicPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
        try{
            validationMusicPlayer.setDataSource(this,
                Uri.parse("android.resource://" + getPackageName() + "/"+R.raw.validation_music));
            validationMusicPlayer.prepare();
        }catch (Throwable t) {
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
        startActivity(intent);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ROUTE, route);
        outState.putLong(START_TIME, startTime);
        outState.putInt(POLL_CNT, fakeLocationService == null?0:fakeLocationService.pollCnt);
        GeoPoint geoPoint = null;
        RouteNode firstNode = route.getFirstNode();
        if(pointOverlay != null) {
            geoPoint = pointOverlay.getLocation();
        }else if(firstNode != null){
            geoPoint = firstNode.getGeoPoint();
        }
        if(geoPoint != null){
            outState.putParcelable(GEO_POINT, geoPoint);
        }
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	EasyTracker.getInstance().activityStart(this);
    	
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
        else if (gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED 
                || gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA
                || gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA2
                || gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA3
                || gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA4) {
            int interval = DebugOptionsActivity.getGpsUpdateInterval(this);
            if(fakeLocationService == null){
                fakeLocationService = new FakeLocationService(locationListener, interval, gpsMode);
            }else{
                fakeLocationService = fakeLocationService.setInterval(interval);
            }
            if(savedPollCnt > 0){
                fakeLocationService.skip(savedPollCnt);
                savedPollCnt = 0;
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
    
    private RouteRect routeRect;
    
    private static RouteRect initRouteRect(Route r){
        return new RouteRect(r.getNodes());
    }
    
    private void initViews() {
        mapView = (MapView) findViewById(R.id.mapview);
        Misc.disableHardwareAcceleration(mapView);
        CloudmadeUtil.retrieveCloudmadeKey(this);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setTileSource(new SmartrekTileProvider(isDebugging));
        mapView.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                buttonFollow.setChecked(false);
                return false;
            }
        });
        
        TextView osmCredit = (TextView) findViewById(R.id.osm_credit);
        Misc.initOsmCredit(osmCredit);
        RelativeLayout.LayoutParams osmCreditLp = (RelativeLayout.LayoutParams) osmCredit.getLayoutParams();
        osmCreditLp.bottomMargin += Dimension.dpToPx(52, getResources().getDisplayMetrics());
        
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
                    	mc.setZoom(DEFAULT_ZOOM_LEVEL);
                    	mc.animateTo(latitude, longitude);
                    }
                }
                else if(routeRect != null){
                    /* Get a midpoint to center the view of  the routes */
                    GeoPoint mid = routeRect.getMidPoint();
                    /* range holds 2 points consisting of the lat/lon range to be displayed */
                    int[] range = routeRect.getRange();
                    /* Get the MapController set the midpoint and range */
                    MapController mc = mapView.getController();
                    mc.zoomToSpan(range[0], range[1]);
                    mc.setCenter(mid); // setCenter only works properly after zoomToSpan
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
        
        View mapViewEndTripBtn = findViewById(R.id.map_view_end_trip_btn);
        mapViewEndTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelValidation();
            }
        });
        
        View dirViewEndTripBtn = findViewById(R.id.directions_view_end_trip_btn);
        dirViewEndTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelValidation();
            }
        });
        
        Font.setTypeface(lightFont, osmCredit);
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
        }else{
            // TODO: Turn on GSP early
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
                DebugOptionsActivity.getGpsUpdateInterval(this), 5, locationListener);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
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
        
        RoutePathOverlay pathOverlay = new RoutePathOverlay(this, route, RoutePathOverlay.GREEN);
        mapOverlays.add(pathOverlay);
        
        pointOverlay = new PointOverlay(this, 0, 0);
        pointOverlay.setColor(0xCC2020DF);
        mapOverlays.add(pointOverlay);
        
        RouteDebugOverlay debugOverlay = new RouteDebugOverlay(this);
        debugOverlay.setActionListener(new RouteDebugOverlay.ActionListener() {
			
			@Override
			public void onLongPress(double latitude, double longitude) {
			    SharedPreferences debugPrefs = getSharedPreferences(DebugOptionsActivity.DEBUG_PREFS, MODE_PRIVATE);
		        int gpsMode = debugPrefs.getInt(DebugOptionsActivity.GPS_MODE, DebugOptionsActivity.GPS_MODE_DEFAULT);
		        if(gpsMode == DebugOptionsActivity.GPS_MODE_LONG_PRESS){
		            Location location = new Location("");
	                location.setLatitude(latitude);
	                location.setLongitude(longitude);
	                location.setTime(System.currentTimeMillis());
	                locationChanged(location);
		        }
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
    
    private void saveTrajectory(){
        if(!Request.NEW_API){
            final File tFile = SendTrajectoryService.getInFile(this, reservation.getDisplayId(), seq++);
            final JSONArray tJson;
            try {
                tJson = trajectory.toJSON();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            new AsyncTask<Void, Void, Void>(){
                                @Override
                                protected Void doInBackground(Void... params) {
                                    try {
                                        FileUtils.write(tFile, tJson.toString());
                                    }
                                    catch (IOException e) {
                                    }
                                    return null;
                                }
                            }.execute();
                        }catch(Throwable t){}
                    }
                });
            }
            catch (JSONException e) {
            }
        }
        trajectory.clear();
    }
    
    private void saveValidation(){
        final File tFile = ValidationService.getFile(this, reservation.getDisplayId());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                FileUtils.write(tFile, "");
                            }
                            catch (IOException e) {
                            }
                            return null;
                        }
                    }.execute();
                }catch(Throwable t){}
            }
        });
    }
    
    private void showNavigationInformation(final Location location, final RouteNode node) {
        Log.d("ValidationActivity", "showNavigationInformation()");
        runOnUiThread(new Runnable() {
            public void run() {
                List<DirectionItem> items = updateDirectionsList(node, location);
            	navigationView.update(route, location, node, items);
            }
        });
    }
    
    private void updateDirectionsList(){
        updateDirectionsList(null, null);
    }
    
    private List<DirectionItem> updateDirectionsList(RouteNode node, Location location){
        List<DirectionItem> items = new ArrayList<DirectionItem>(); 
        dirListadapter.clear();
        RouteNode nextNode = node;
        if(nextNode == null){
            nextNode = route.getFirstNode();
        }
        if(nextNode != null){
            double distance = 0;
            do {
                if (nextNode.getFlag() != 0) {
                    if(nextNode == node && location != null){
                        distance = route.getDistanceToNextTurn(location.getLatitude(), 
                            location.getLongitude());
                    }
                    DirectionItem item = new DirectionItem(
                        NavigationView.getDirectionDrawableId(nextNode.getDirection()),
                        distance, nextNode.getRoadName());
                    dirListadapter.add(item);
                    items.add(item);
                    distance = 0;
                }
                distance += nextNode.getDistance();
            } while ((nextNode = nextNode.getNextNode()) != null);
        }
        return items;
    }
    
    private synchronized void locationChanged(Location location) {
        trajectory.accumulate(location);
        
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        
        GeoPoint oldLoc = pointOverlay.getLocation();
        long now = SystemClock.elapsedRealtime();
        if(oldLoc.isEmpty()){
            if (buttonFollow.isChecked()) {
                mapView.getController().animateTo(lat, lng);
            }
            pointOverlay.setLocation((float) lat, (float)lng);
            mapView.postInvalidate();
        }else{
            animator.removeCallbacksAndMessages(null);
            final double oldLat = oldLoc.getLatitude();
            double y = lat - oldLat;
            final double oldLng = oldLoc.getLongitude();
            double x = lng - oldLng;
            final double slop = y/x;
            double timeInterval = 1000 / 30;
            long numOfSteps = Math.round((now - lastLocChanged) / timeInterval);
            final double stepSize = x / numOfSteps;
            long startTimeMillis = SystemClock.uptimeMillis();
            for(int i=1; i<=numOfSteps; i++){
                final int seq = i;
                animator.postAtTime(new Runnable() {
                    @Override
                    public void run() {
                        double deltaX = seq * stepSize;
                        double newLng = oldLng + deltaX;
                        double newLat = oldLat + deltaX * slop; 
                        pointOverlay.setLocation((float) newLat, (float)newLng);
                        mapView.postInvalidate();
                        if (buttonFollow.isChecked()) {
                            mapView.getController().setCenter(new GeoPoint(newLat, newLng));
                        }
                    }
                }, startTimeMillis + Math.round(i * timeInterval));
            }
        }
        
        lastLocChanged = now;
        
        //nearestNode = route.getNearestNode(lat, lng);
        nearestLink = route.getNearestLink(lat, lng);
        nearestNode = nearestLink.getEndNode();
        
        ValidationParameters params = ValidationParameters.getInstance();
        
        boolean alreadyValidated = isTripValidated(); 
        
        double distanceToLink = nearestLink.distanceTo(lat, lng);
        if (distanceToLink <= params.getValidationDistanceThreshold()) {
            Log.i("validated node", nearestLink.getStartNode().getNodeIndex() + "");
            nearestLink.getStartNode().getMetadata().setValidated(true);
        }
        
        if(!alreadyValidated && isTripValidated()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    validationMusicPlayer.start();
                    saveValidation();
                }
            });
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
        
        if (trajectory.size() >= 8) {
            saveTrajectory();
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
        saveTrajectory();
        
        reportValidation();
        if(mTts == null){
            finish();
        }else{
            final int oldCnt = utteredCnt.get();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    int newCnt = utteredCnt.get();
                    if(newCnt == oldCnt && newCnt == utteringCnt.get()){
                        finish();
                    }
                }
            }, 1000 + Math.round(Math.random() * 500));
        }
    }
    
    private void reportValidation(){
        if(!reported.get()){
            reported.set(true);
            
            if(isTripValidated()){
                Time now = new Time();
                now.setToNow();
                Intent intent = new Intent(this, ValidationReportActivity.class);
                intent.putExtra(ROUTE, route);
                intent.putExtra(START_TIME, startTime);
                intent.putExtra("endTime", now.toMillis(false));
                startActivity(intent);
            }
        }
    }
    
    private boolean isTripValidated(){
        double validatedDistance = route.getValidatedDistance();
        double length = route.getLength();
        double score = validatedDistance / length;
        Log.i("isTripValidated", validatedDistance + " / " + length + " = " + score);
        ValidationParameters params = ValidationParameters.getInstance();
        return score >= params.getScoreThreshold();
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

                reportValidation();
                
                //Stop the activity
                ValidationActivity.this.finish();    
            }

        })
        .setNegativeButton("No", null)
        .show();
    }
    
    private class ValidationLocationListener implements LocationListener {
        
        Location lastLocation;
        
        public void onLocationChanged(Location location) {
            Log.d(this.getClass().toString(), String.format("onLocationChanged: %s", location));
            SharedPreferences debugPrefs = getSharedPreferences(DebugOptionsActivity.DEBUG_PREFS, MODE_PRIVATE);
            int gpsMode = debugPrefs.getInt(DebugOptionsActivity.GPS_MODE, DebugOptionsActivity.GPS_MODE_DEFAULT);
            if (gpsMode == DebugOptionsActivity.GPS_MODE_REAL) {
                if(isBetterLocation(location, lastLocation)){
                    lastLocation = location;
                    locationChanged(location);
                }
            }else{
                locationChanged(location);
            }
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
    
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    
    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    private static boolean isBetterLocation(Location location, Location currentBestLocation) {
       if (currentBestLocation == null) {
           // A new location is always better than no location
           return true;
       }

       // Check whether the new location fix is newer or older
       long timeDelta = location.getTime() - currentBestLocation.getTime();
       boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
       boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
       boolean isNewer = timeDelta > 0;

       // If it's been more than two minutes since the current location, use the new location
       // because the user has likely moved
       if (isSignificantlyNewer) {
           return true;
       // If the new location is more than two minutes older, it must be worse
       } else if (isSignificantlyOlder) {
           return false;
       }

       // Check whether the new location fix is more or less accurate
       int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
       boolean isLessAccurate = accuracyDelta > 0;
       boolean isMoreAccurate = accuracyDelta < 0;
       boolean isSignificantlyLessAccurate = accuracyDelta > 200;

       // Check if the old and new location are from the same provider
       boolean isFromSameProvider = isSameProvider(location.getProvider(),
               currentBestLocation.getProvider());

       // Determine location quality using a combination of timeliness and accuracy
       if (isMoreAccurate) {
           return true;
       } else if (isNewer && !isLessAccurate) {
           return true;
       } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
           return true;
       }
       return false;
   }
    
    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
    
    private class ValidationTimeoutNotifier extends Thread {
        @Override
        public void run() {
            if(isTripValidated()){
                Intent intent = new Intent(ValidationActivity.this, ValidationReportActivity.class);
                intent.putExtra(ROUTE, route);
                intent.putExtra(START_TIME, startTime);
                Time now = new Time();
                now.setToNow();
                intent.putExtra("endTime", now.toMillis(false));
                startActivity(intent);
                finish();
            }else{
                NotificationDialog dialog = new NotificationDialog(ValidationActivity.this, "Timed out!");
                dialog.setActionListener(new NotificationDialog.ActionListener() {
                    @Override
                    public void onClickDismiss() {
                        if(!isFinishing()){
                            finish();
                        }
                    }
                });
                dialog.show();
            }
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
        
        private int interval;
        
        private int gpsMode;
        
        int pollCnt;
        
        public FakeLocationService(LocationListener listener, int interva, int gpsMode) {
            this(listener, interva, null, gpsMode);
        }
        
        @SuppressWarnings("unchecked")
        public FakeLocationService(LocationListener listener, int interval, Queue<GeoPoint> trajectory, int gpsMode) {
            this.listener = listener;
            this.interval = interval;
            this.gpsMode = gpsMode;
            
            if(trajectory == null){
                try {
                    String tFile;
                    if(gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED){
                        tFile = "trajectory.csv";
                    }else if(gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA){
                        tFile = "trajectory-la.csv";
                    }else if(gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA2){
                        tFile = "trajectory-la-2.csv";
                    }else if(gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA3){
                        tFile = "trajectory-la-3.csv";
                    }else{
                        tFile = "trajectory-la-4.csv";
                    }
                    InputStream in = getResources().getAssets().open(tFile);
                    this.trajectory = (Queue<GeoPoint>) PrerecordedTrajectory.read(in, gpsMode);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                this.trajectory = trajectory;
            }
            
            timer = new Timer();
            timer.schedule(this, 1000, interval);
        }
        
        FakeLocationService setInterval(int millisecond){
            FakeLocationService rtn;
            if(interval != millisecond){
                cancel();
                rtn = new FakeLocationService(listener, millisecond, trajectory, gpsMode);
            }else{
                rtn = this;
            }
            return rtn;
        }

        @Override
        public void run() {
            if (trajectory == null || trajectory.isEmpty()) {
                timer.cancel();
            }
            else {
                Location location = new Location("");
                location.setTime(System.currentTimeMillis());
                GeoPoint geoPoint = trajectory.poll();
                pollCnt++;
                location.setLatitude(geoPoint.getLatitude());
                location.setLongitude(geoPoint.getLongitude());
                listener.onLocationChanged(location);
            }
        }
        
        void skip(int cnt){
            for(int i=0; i<cnt; i++){
                trajectory.poll();
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
                    unmuteMusic();
                    if(utteredCnt.incrementAndGet() == utteringCnt.get() && arrived.get()){
                        finish();
                    }
                }
            });
            navigationView.setListener(new CheckPointListener() {
                @Override
                public void onCheckPoint(final String navText) {
                    if(MapDisplayActivity.isNavigationTtsEnabled(ValidationActivity.this)){
                        utteringCnt.incrementAndGet();
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
                        params.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                            String.valueOf(AudioManager.STREAM_NOTIFICATION));
                        AudioManager am = (AudioManager)ValidationActivity.this.getSystemService(Context.AUDIO_SERVICE);
                        am.setStreamMute(AudioManager.STREAM_MUSIC, true);
                        mTts.speak(navText, TextToSpeech.QUEUE_ADD, params);
                    }
                }
            });
        }
    }
    
    private void unmuteMusic(){
        AudioManager am = (AudioManager)ValidationActivity.this.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamMute(AudioManager.STREAM_MUSIC, false);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unmuteMusic();
        validationTimeoutHandler.removeCallbacks(validationTimeoutNotifier);
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        deactivateLocationService();
        if(mTts != null){
            mTts.shutdown();
        }
    }
    
}
