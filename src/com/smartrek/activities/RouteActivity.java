package com.smartrek.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.CalendarService;
import com.smartrek.activities.DebugOptionsActivity.FakeRoute;
import com.smartrek.activities.LandingActivity.ShortcutNavigationTask;
import com.smartrek.dialogs.CancelableProgressDialog;
import com.smartrek.dialogs.ContactsDialog;
import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.exceptions.RouteNotFoundException;
import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.requests.ReservationRequest;
import com.smartrek.requests.RouteFetchRequest;
import com.smartrek.tasks.GeocodingTask;
import com.smartrek.tasks.GeocodingTaskCallback;
import com.smartrek.ui.EditAddress;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.ui.overlays.OverlayCallback;
import com.smartrek.ui.overlays.RoutePathOverlay;
import com.smartrek.ui.timelayout.ScrollableTimeLayout;
import com.smartrek.ui.timelayout.TimeButton;
import com.smartrek.ui.timelayout.TimeButton.DisplayMode;
import com.smartrek.ui.timelayout.TimeButton.State;
import com.smartrek.ui.timelayout.TimeColumn;
import com.smartrek.ui.timelayout.TimeLayout;
import com.smartrek.ui.timelayout.TimeLayout.TimeLayoutListener;
import com.smartrek.ui.timelayout.TimeLayout.TimeLayoutOnSelectListener;
import com.smartrek.utils.CalendarContract.Instances;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Geocoding;
import com.smartrek.utils.Misc;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.RouteRect;
import com.smartrek.utils.SessionM;
import com.smartrek.utils.SmartrekTileProvider;
import com.smartrek.utils.SystemService;

/**
 * 
 *
 */
public final class RouteActivity extends FragmentActivity {

    public static final String EVENT_ID = "event_id";
    
    public static final int RESERVATION_CONFIRM = 3;
    
    public static final int RESERVATION_CONFIRM_ENDED = 3;
    
	public static final String LOG_TAG = "RouteActivity";
	
	public static final String ORIGIN_ADDR = "originAddr";
	public static final String DEST_ADDR = "destAddr";
	
	public static final String ORIGIN_COORD = "originCoord";
    public static final String DEST_COORD = "destCoord";
    
    public static final String CURRENT_LOCATION = "CURRENT_LOCATION";
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
    private Typeface boldFont;
    
    private Typeface lightFont;
    
    //private RouteInfoOverlay[] routeInfoOverlays = new RouteInfoOverlay[3];
    private RoutePathOverlay[] routePathOverlays = new RoutePathOverlay[3];
    
    private String originAddr;
    private String destAddr;
    private GeoPoint originCoord;
    private GeoPoint destCoord;
    
    // TODO: 'dialog' isn't really meaningful. Rename this variable.
    private ProgressDialog dialog;
    
    private MapView mapView;
    
    private Time selectedTime;
    
    private TimeLayout timeLayout;
    
    private ScrollableTimeLayout scrollableTimeLayout;
    
    private List<RouteTask> routeTasks = new Vector<RouteTask>();
    
    private List<GeocodingTask> geocodingTasks = new ArrayList<GeocodingTask>();
    
    private boolean debugMode;
    
    private Runnable goBackToWhereTo = new Runnable() {
        @Override
        public void run() {
            if(!isFinishing()){
                finish();
            }
        }
    };
    
	private GeocodingTaskCallback originGeocodingTaskCallback = new GeocodingTaskCallback() {
		
		private CancelableProgressDialog dialog;

		@Override
		public void preCallback() {
			dialog = new CancelableProgressDialog(RouteActivity.this, "Geocoding origin address...");
			dialog.setActionListener(new CancelableProgressDialog.ActionListener() {
                @Override
                public void onClickNegativeButton() {
                    goBackToWhereTo.run();
                }
            });
	        dialog.show();
		}

		@Override
		public void callback(List<Geocoding.Address> addresses) {
			if (addresses.size() == 1) {
				originCoord = addresses.get(0).getGeoPoint();
			}
			else {
				// TODO: Popup a dialog to pick an address
				originCoord = addresses.get(0).getGeoPoint();
			}
		}

		@Override
		public void postCallback() {
			dialog.cancel();
			
			if (ehs.hasExceptions()) {
			    ehs.reportExceptions(goBackToWhereTo);
			}
			else if(destCoord == null || destCoord.isEmpty()){
				GeocodingTask task = new GeocodingTask(ehs, destGeocodingTaskCallback);
				task.execute(destAddr);
                geocodingTasks.add(task);
			}else{
			    RouteTask routeTask = new RouteTask(originCoord, destCoord, timeLayout.getDepartureTime(0), 0, true);
	            routeTasks.add(routeTask);
	            routeTask.execute();
			}
		}
		
	};
	
	private GeocodingTaskCallback destGeocodingTaskCallback = new GeocodingTaskCallback() {

		private CancelableProgressDialog dialog;
		
		@Override
		public void preCallback() {
			dialog = new CancelableProgressDialog(RouteActivity.this, "Geocoding destination address...");
			dialog.setActionListener(new CancelableProgressDialog.ActionListener() {
                @Override
                public void onClickNegativeButton() {
                    goBackToWhereTo.run();
                }
            });
	        dialog.show();
		}

		@Override
		public void callback(List<Geocoding.Address> addresses) {
			if (addresses.size() == 1) {
				destCoord = addresses.get(0).getGeoPoint();
			}
			else {
				// TODO: Popup a dialog to pick an address
				destCoord = addresses.get(0).getGeoPoint();
			}
		}

		@Override
		public void postCallback() {
			dialog.cancel();
			
			if (ehs.hasExceptions()) {
			    ehs.reportExceptions(goBackToWhereTo);
			}
			else {
		        RouteTask routeTask = new RouteTask(originCoord, destCoord, timeLayout.getDepartureTime(0), 0, true);
		        routeTasks.add(routeTask);
		        routeTask.execute();
			}
		}
	};
    
    public GeoPoint getOriginCoord() {
        return originCoord;
    }
    
    public GeoPoint getDestCoord() {
        return destCoord;
    }
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_reservation_map);    
        
        SharedPreferences prefs = getSharedPreferences(MapDisplayActivity.MAP_DISPLAY_PREFS, MODE_PRIVATE);
        
        /* Get the extras from the bundle */
        Bundle extras = getIntent().getExtras();
        
        boolean currentLocation = extras.getBoolean(CURRENT_LOCATION);
        originAddr = extras.getString(ORIGIN_ADDR);
        destAddr = extras.getString(DEST_ADDR);
        
        int eventId = extras.getInt(EVENT_ID, 0);
        if(eventId > 0){
            NotificationManager nMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nMgr.cancel(eventId);
            JSONObject event = CalendarService.getEvent(this, eventId);
            originAddr = EditAddress.CURRENT_LOCATION;
            destAddr = event.optString(Instances.EVENT_LOCATION);
            currentLocation = true;
        }
        
        debugMode = extras.getBoolean("debugMode");
        
        mapView = (MapView) findViewById(R.id.mapview);
        Misc.disableHardwareAcceleration(mapView);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setTileSource(new SmartrekTileProvider());
        
        TextView osmCredit = (TextView) findViewById(R.id.osm_credit);
        Font.setTypeface(lightFont, osmCredit);
        Misc.initOsmCredit(osmCredit);
        
        /* Set the map view for a view of North America before zooming in on route */
        MapController mc = mapView.getController();
        int lat = (int) Math.round(38.27268853598097f*1E6);
        int lon = (int) Math.round(-99.1406250000000f*1E6);
        mc.setZoom(4); 
        mc.setCenter(new GeoPoint(lat, lon));
        
        dialog = new ProgressDialog(RouteActivity.this) {
            @Override
            public void onBackPressed() {
                RouteActivity.this.onBackPressed();
            }
        };
        dialog.setCancelable(false);
        
        //
        // Set up time layout
        //        
        timeLayout = (TimeLayout) findViewById(R.id.timelayout);
        scrollableTimeLayout = (ScrollableTimeLayout) findViewById(R.id.scrollTime);
        
        // What happens when user selects a specific time
        timeLayout.setOnSelectListener(new TimeLayoutOnSelectListener() {
            @Override
            public void onSelect(int column, TimeColumn timeButton) {
                Log.d(LOG_TAG, "Column state: " + timeLayout.getColumnState(column));
                
                // FIXME: Refactor this. (Close all route info overlays)
                /*for (int i = 0; i < routeInfoOverlays.length; i++) {
                	RouteInfoOverlay routeInfoOverlay = routeInfoOverlays[i];
                	
                	if (routeInfoOverlay != null) {
                		routeInfoOverlay.hide();
                	}
                }*/
                
                if (!timeLayout.getColumnState(column).equals(State.InProgress)) {
                    
//                  if (timeLayout.getColumnState(column).equals(State.Unknown)) {
                        timeLayout.setColumnState(column, State.InProgress);
                        long departureTime = timeButton.getDepartureTime();
                        
                        try {
                            updateRoute(originCoord, destCoord, departureTime, column);
                        }
                        catch (InterruptedException e) {
                        }
                        
//                        RouteTask routeTask = new RouteTask(originCoord, destCoord, departureTime, column, true);
//                        routeTasks.add(routeTask);
//                        routeTask.execute();
//                  }
//                  else {
//                      timeLayout.setColumnState(column, State.Selected);
//                  }
                }
            }
        });
        
        // What happens when user scrolls time layout
        timeLayout.setTimeLayoutListener(new TimeLayoutListener() {
            
            private Map<Integer, RouteTask> loadingTasks = new HashMap<Integer, RouteTask>();
            
            @Override
            public void updateTimeLayout(TimeLayout timeLayout, int column, boolean visible) {
                State columnState = timeLayout.getColumnState(column);
                RouteTask task;
                if(visible){
                    if (originCoord != null && !originCoord.isEmpty() 
                            && destCoord != null && !destCoord.isEmpty() 
                            && (State.Unknown.equals(columnState))) {
                        timeLayout.setColumnState(column, State.InProgress);
                        long departureTime = timeLayout.getDepartureTime(column);
                        
                        RouteTask routeTask = new RouteTask(originCoord, destCoord, departureTime, column, false);
                        routeTasks.add(routeTask);
                        loadingTasks.put(column, routeTask);
                        routeTask.execute();
                    }
                }else{
                    if(State.InProgress.equals(columnState) && (task = loadingTasks.remove(column)) != null){
                        task.cancel(true);
                        timeLayout.setColumnState(column, State.Unknown);
                    }
                }
            }
        });

        ScrollableTimeLayout scrollableTimeLayout = (ScrollableTimeLayout) findViewById(R.id.scrollTime);
        scrollableTimeLayout.setTimeLayout(timeLayout);
        
        updateTimetableScreenWidth();

        // FIXME: Should store values in a different preference file
        int timeDisplayMode = prefs.getInt(MapDisplayActivity.TIME_DISPLAY_MODE, MapDisplayActivity.TIME_DISPLAY_DEFAULT);
        
        // FIXME: Sloppy
        timeLayout.setDisplayMode((timeDisplayMode & MapDisplayActivity.TIME_DISPLAY_TRAVEL) != 0 ? DisplayMode.Duration : DisplayMode.Time);
        
        // FIXME: Temporary solution
        selectedTime = new Time();
        selectedTime.setToNow();
        
        org.osmdroid.util.GeoPoint pOriginCoord = extras.getParcelable(ORIGIN_COORD);
        if(pOriginCoord != null){
            originCoord = new GeoPoint(pOriginCoord);
        }
        org.osmdroid.util.GeoPoint pDestCoord = extras.getParcelable(DEST_COORD);
        if(pOriginCoord != null){
            destCoord = new GeoPoint(pDestCoord);
        }
        
        final boolean _currentLocation = currentLocation;
        LandingActivity.initializeIfNeccessary(this, new Runnable() {
            @Override
            public void run() {
                if(_currentLocation){
                    final CancelableProgressDialog currentLocDialog = new CancelableProgressDialog(RouteActivity.this, "Getting current location...");
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            try{
                                locationManager.removeUpdates(this);
                                currentLocDialog.dismiss();
                                originCoord = new GeoPoint(location.getLatitude(), location.getLongitude());
                                doRouteTask();
                            }catch(Throwable t){}
                        }
                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {}
                        @Override
                        public void onProviderEnabled(String provider) {}
                        @Override
                        public void onProviderDisabled(String provider) {}
                    };
                    currentLocDialog.setActionListener(new CancelableProgressDialog.ActionListener() {
                        @Override
                        public void onClickNegativeButton() {
                            locationManager.removeUpdates(locationListener);
                            goBackToWhereTo.run();
                        }
                    });
                    currentLocDialog.show();
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, locationListener);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        SystemService.alertNoGPS(RouteActivity.this);
                    }
                }else{
                    doRouteTask();
                }
            }
        });
        
        AssetManager assets = getAssets();
        boldFont = Font.getBold(assets);
        lightFont = Font.getLight(assets);
        
        TextView destView = (TextView) findViewById(R.id.destination);
        destView.setText(destAddr);
        
        final TextView reserveView = (TextView) findViewById(R.id.reserve);
        reserveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Route route = (Route) reserveView.getTag();
                AsyncTask<Void, Void, Long> task = new AsyncTask<Void, Void, Long>(){
                    
                    private ProgressDialog dialog;

                    @Override
                    protected void onPreExecute() {
                        dialog = new ProgressDialog(RouteActivity.this);
                        dialog.setMessage("Making reservation...");
                        dialog.setIndeterminate(true);
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    }
                    
                    @Override
                    protected Long doInBackground(Void... params) {
                        Long rs = null;
                        ReservationRequest request = new ReservationRequest(User.getCurrentUser(RouteActivity.this), 
                            route, getString(R.string.distribution_date));
                        try {
                            rs = request.execute(RouteActivity.this);
                        }
                        catch (Exception e) {
                            ehs.registerException(e);
                        }
                        return rs;
                    }
                    
                    @Override
                    protected void onPostExecute(Long result) {
                        dialog.cancel();
                        
                        if (ehs.hasExceptions()) {
                            ehs.reportExceptions();
                        }
                        else {
                            ReservationConfirmationActivity.scheduleNotification(RouteActivity.this, result, route);
                            
                            if(route.isFake()){
                                FakeRoute fakeRoute = new FakeRoute();
                                fakeRoute.id = route.getId();
                                fakeRoute.seq = route.getSeq();
                                DebugOptionsActivity.addFakeRoute(RouteActivity.this, fakeRoute);
                            }
                            
                            SessionM.logAction("make_reservation");
                            
                            NotificationDialog dialog = new NotificationDialog(RouteActivity.this, "You have successfully reserved a route.");
                            dialog.setActionListener(new NotificationDialog.ActionListener() {
                                
                                @Override
                                public void onClickDismiss() {
                                    Intent intent = new Intent(RouteActivity.this, 
                                        LandingActivity2.ENABLED?LandingActivity2.class:LandingActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            dialog.show();
                        }
                    }
                    
                };
                Misc.parallelExecute(task);
            }
        });
        
        TextView onMyWayView = (TextView) findViewById(R.id.on_my_way);
        onMyWayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactsDialog d = new ContactsDialog(RouteActivity.this);
                d.setActionListener(new ContactsDialog.ActionListener() {
                    @Override
                    public void onClickPositiveButton(final List<String> emails) {
                        final Route route = (Route) reserveView.getTag();
                        ShortcutNavigationTask task = new ShortcutNavigationTask(RouteActivity.this, route, ehs);
                        task.callback = new ShortcutNavigationTask.Callback() {
                            @Override
                            public void run(Reservation reservation) {
                                if(reservation.isEligibleTrip()){
                                    Intent intent = new Intent(RouteActivity.this, ValidationActivity.class);
                                    intent.putExtra("route", reservation.getRoute());
                                    intent.putExtra("reservation", reservation);
                                    intent.putExtra(ValidationActivity.EMAILS, StringUtils.join(emails, ","));
                                    startActivity(intent);
                                    finish();
                                }else{
                                    String msg = null;
                                    if (reservation.hasExpired()) {
                                        msg = getString(R.string.trip_has_expired);
                                    }
                                    else if (reservation.isTooEarlyToStart()) {
                                        long minutes = (reservation.getDepartureTimeUtc() - System.currentTimeMillis()) / 60000;
                                        msg = getString(R.string.trip_too_early_to_start, minutes);
                                        if(minutes != 1){
                                            msg += "s";
                                        }
                                    }
                                    if(msg != null){
                                        NotificationDialog dialog = new NotificationDialog(RouteActivity.this, msg);
                                        dialog.show();
                                    }
                                }
                            }
                        };
                        Misc.parallelExecute(task);
                    }
                    @Override
                    public void onClickNegativeButton() {}
                });
                d.show();
            }
        });
        TextView letsGoView = (TextView) findViewById(R.id.lets_go);
        letsGoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Route route = (Route) reserveView.getTag();
                ShortcutNavigationTask task = new ShortcutNavigationTask(RouteActivity.this, route, ehs);
                task.callback = new ShortcutNavigationTask.Callback() {
                    @Override
                    public void run(Reservation reservation) {
                        if(reservation.isEligibleTrip()){
                            Intent intent = new Intent(RouteActivity.this, ValidationActivity.class);
                            intent.putExtra("route", reservation.getRoute());
                            intent.putExtra("reservation", reservation);
                            startActivity(intent);
                            finish();
                        }else{
                            String msg = null;
                            if (reservation.hasExpired()) {
                                msg = getString(R.string.trip_has_expired);
                            }
                            else if (reservation.isTooEarlyToStart()) {
                                long minutes = (reservation.getDepartureTimeUtc() - System.currentTimeMillis()) / 60000;
                                msg = getString(R.string.trip_too_early_to_start, minutes);
                                if(minutes != 1){
                                    msg += "s";
                                }
                            }
                            if(msg != null){
                                NotificationDialog dialog = new NotificationDialog(RouteActivity.this, msg);
                                dialog.show();
                            }
                        }
                    }
                };
                Misc.parallelExecute(task);
            }
        });
        
        Font.setTypeface(lightFont, destView, onMyWayView, letsGoView, reserveView);
    }
    
    private void updateTimetableScreenWidth(){
        if(scrollableTimeLayout != null){
            scrollableTimeLayout.setScreenWidth(getWindowManager().getDefaultDisplay().getWidth());
            scrollableTimeLayout.notifyScrollChanged();
        }
    }
    
    private void doRouteTask(){
        if(originCoord == null || originCoord.isEmpty()){
            GeocodingTask task = new GeocodingTask(ehs, originGeocodingTaskCallback);
            task.execute(originAddr);
            geocodingTasks.add(task);
        }else if(destCoord == null || destCoord.isEmpty()){
            GeocodingTask task = new GeocodingTask(ehs, destGeocodingTaskCallback);
            task.execute(destAddr);
            geocodingTasks.add(task);
        }else{
            RouteTask routeTask = new RouteTask(originCoord, destCoord, timeLayout.getDepartureTime(0), 0, true);
            routeTasks.add(routeTask);
            routeTask.execute();
        }
    }
    
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
    
    @Override
    public void onBackPressed() {
    	Resources res = getResources();
    	
    	boolean reverseButtons = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
    	String rightButtonText = res.getString(R.string.yes);
    	String leftButtonText = res.getString(R.string.no); 
    	DialogInterface.OnClickListener rightButtonOnClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                goBackToWhereTo.run();
            }
        }; 
        // Ask the user if they want to quit
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to go back to previous screen?")
                .setPositiveButton(reverseButtons?leftButtonText:rightButtonText, 
                    reverseButtons?null:rightButtonOnClick)
                .setNegativeButton(reverseButtons?rightButtonText:leftButtonText, 
                    reverseButtons?rightButtonOnClick:null)
                .show();
    }
    
    private RouteRect routeRect;
    
    private void fitRouteToMap(){
        if(routeRect != null){
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
    
    /**
     * This function will be called when BackgroundDownloadTask().execute()
     * succeeds.
     * 
     * @param possibleRoutes
     */
    private void updateMap(List<Route> possibleRoutes) {
    	
        if(possibleRoutes != null && possibleRoutes.size() > 0) {
            
            List<RouteNode> nodes = new ArrayList<RouteNode>(); 
            
            /* Iterate through the routes to draw each to the screen */
            for (int i = 0; i < possibleRoutes.size(); i++) {
                Route route = possibleRoutes.get(i);
            
                /* Draw the route to the screen and hold on to the range */
                drawRoute(mapView, route, i);
                
                nodes.addAll(route.getNodes());
            }
            
            routeRect = new RouteRect(nodes);
            
            // Overlays must be drawn in orders
            for (int i = 0; i < possibleRoutes.size(); i++) {
            	mapOverlays.add(routePathOverlays[i]);
            }
            /*for (int i = 0; i < possibleRoutes.size(); i++) {
            	mapOverlays.add(routeInfoOverlays[i]);
            }*/
            
            fitRouteToMap();
            
            mapView.postInvalidate();
        }
        else {
        	Log.d(LOG_TAG, "updateMap(): no route available.");
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mapView.postDelayed(new Runnable() {
            public void run() {
                updateTimetableScreenWidth();
                fitRouteToMap();
            }
        }, 500);
    }
    
    /**
     * Updates the map by fetching a route from a local cache
     * 
     * @param origin
     * @param destination
     * @param departureTime
     * @param column
     * @throws InterruptedException 
     * @throws JSONException 
     * @throws IOException 
     * @throws RouteNotFoundException 
     */
    private void updateRoute(GeoPoint origin, GeoPoint destination, long departureTime, int column) throws InterruptedException {
        int letsGoPanelVis = column == 0?View.VISIBLE:View.GONE;
        int reservePanelVis = column == 0?View.GONE:View.VISIBLE;
        RouteFetchRequest request = new RouteFetchRequest(User.getCurrentUser(this), origin, destination, departureTime);
        if (request.isCached(this)) {
            try {
                List<Route> routes = request.execute(this);
                updateMap(routes);
                timeLayout.setColumnState(column, TimeButton.State.Selected);
            }
            catch (RouteNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            for (RouteTask task : routeTasks) {
                task.cancel(true);
            }
            timeLayout.refresh();
            RouteTask routeTask = new RouteTask(originCoord, destCoord, timeLayout.getDepartureTime(0), 0, true);
            routeTasks.add(routeTask);
            routeTask.execute();
            letsGoPanelVis = View.VISIBLE;
            reservePanelVis = View.GONE;
        }
        findViewById(R.id.lets_go_panel).setVisibility(letsGoPanelVis);
        findViewById(R.id.reserve_panel).setVisibility(reservePanelVis);
    }

    private List<Overlay> mapOverlays;

    private LocationManager locationManager;

    private LocationListener locationListener;
    
    /*****************************************************************************************************************
     * ************* public void drawRoute (MapView mapView, ArrayList<Parcelable> route, int routeNum) **************
     * 
     * This method is called once for each route displayed to the screen
     *
     * @param mapView - The MapView that this route is drwn to.
     * 
     * @param route - 
     * 
     * @param routeNum - 
     *
     ****************************************************************************************************************/
    public int[] drawRoute (MapView mapView, Route route, int routeNum) {
        mapOverlays = mapView.getOverlays();
        
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
        
        routePathOverlays[routeNum] = new RoutePathOverlay(this, route, RoutePathOverlay.COLORS[routeNum]);
        //mapOverlays.add(routePathOverlays[routeNum]);
        
        /* Set values into route to be passed to next Activity */
        route.setAddresses(originAddr, destAddr);
        
        // FIXME:
        route.setUserId(User.getCurrentUser(this).getId());
        
        findViewById(R.id.reserve).setTag(route);
//        routeInfoOverlays[routeNum] = new RouteInfoOverlay(mapView, route, routeNum, new GeoPoint(lat, lon), boldFont, lightFont);
//        routeInfoOverlays[routeNum].setCallback(new RouteOverlayCallbackImpl(route, routeNum));
        //mapOverlays.add(routeOverlays[routeNum]);
        
        /* Add offset of 1000 to range so that map displays extra space around route. */
        int [] range = {latMax - latMin + 1500 ,lonMax - lonMin + 1500};
        
        /* Return the range to doRoute so that map can be adjusted to range settings */
        return range;
    }
    
    private void setHighlightedRoutePathOverlays(boolean highlighted) {
        /*for (int i = 0; i < routeInfoOverlays.length; i++) {
            RoutePathOverlay overlay = routePathOverlays[i];
            
            if (overlay != null) {
                overlay.setHighlighted(highlighted);
            }
        }*/
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater mi = getMenuInflater();
        //mi.inflate(R.menu.main, menu);
        return true;
    }
    
    /**
     * 
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        if(requestCode == -1) {
            finish();
        }
        
        Bundle extras = intent == null?null:intent.getExtras();
        
        SharedPreferences prefs = getSharedPreferences(MapDisplayActivity.MAP_DISPLAY_PREFS, MODE_PRIVATE);
        
        if (extras != null) {
            if (extras.getInt("display") != 0) {
                int displayMode = prefs.getInt(MapDisplayActivity.TIME_DISPLAY_MODE, MapDisplayActivity.TIME_DISPLAY_DEFAULT);
                
                // FIXME: Sloppy
                timeLayout.setDisplayMode((displayMode & MapDisplayActivity.TIME_DISPLAY_TRAVEL) != 0 ? DisplayMode.Duration : DisplayMode.Time);
            }
        } 
        
        if(requestCode == RESERVATION_CONFIRM){
            if(resultCode == RESERVATION_CONFIRM_ENDED){
                goBackToWhereTo.run();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        for(GeocodingTask t:geocodingTasks){
            t.cancel(true);
        }
        for (RouteTask task : routeTasks) {
            task.cancel(true);
        }
        if(locationManager != null && locationListener != null){
            locationManager.removeUpdates(locationListener); 
        }
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        MainMenu.onMenuItemSelected(this, featureId, item);
        
        return super.onMenuItemSelected(featureId, item);
    }
    
    // FIXME: This should be an inner interface of RouteTask. Probably want to name it 'Listener'.
    public interface RouteTaskCallback {
        public void preCallback();
        public void callback(List<Route> routes);
        public void postCallback();
    }

    /**
     * Asynchronous task to request for a route from the server. 
     */
    protected class RouteTask extends AsyncTask<Object, Void, List<Route>> {
        
        private int selectedColumn;
        private boolean updateMap;
        private GeoPoint origin;
        private GeoPoint destination;
        private long departureTime;
        
        private CancelableProgressDialog dialog;
        
        public RouteTask(GeoPoint origin, GeoPoint destination, long departureTime, int column, boolean updateMap) {
        	super();
        	
        	this.origin = origin;
        	this.destination = destination;
        	this.departureTime = departureTime;
        	this.selectedColumn = column;
        	this.updateMap = updateMap;
        }
        
        public boolean isCached() {
        	RouteFetchRequest request = new RouteFetchRequest(User.getCurrentUser(RouteActivity.this), origin, destination, departureTime);
        	return request.isCached(RouteActivity.this);
        }
        
        public List<Route> getData() throws RouteNotFoundException, IOException, JSONException, InterruptedException {
        	RouteFetchRequest request = new RouteFetchRequest(User.getCurrentUser(RouteActivity.this), origin, destination, departureTime);
        	return request.execute(RouteActivity.this);
        }
        
        @Override
        protected void onPreExecute() {
            // FIXME: Should this be here?
            timeLayout.setColumnState(selectedColumn, TimeButton.State.InProgress);
            
            dialog = new CancelableProgressDialog(RouteActivity.this, "Finding routes...");
            dialog.setActionListener(new CancelableProgressDialog.ActionListener() {
                @Override
                public void onClickNegativeButton() {
                    goBackToWhereTo.run();
                }
            });
        	
        	if (isCached()) {
        		
        	}
        	else {
        		if (updateMap) {
        			dialog.show();
        		}
        	}
        }
        
        @Override
        protected List<Route> doInBackground(Object... args) {  
            /* Get the possible routes from the server */
            List<Route> routes = null;
            try {
            	if (debugMode) {
            		RouteFetchRequest request = new RouteFetchRequest(departureTime);
            		routes = request.execute(RouteActivity.this);
            	}
            	else {
            		routes = getData();
            	}
            }
            catch(Exception e) {
                ehs.registerException(e);
            }
            
            return routes;
        }
        
        /**
         * Dialogs must be handled in onPostExecute() because they have to
         * reside in the main loop.
         */
        @Override
        protected void onPostExecute(List<Route> routes) {
            if (dialog.isShowing()) {
            	dialog.dismiss();
            }
            
            setHighlightedRoutePathOverlays(true);
            mapView.postInvalidate();
            
            if (ehs.hasExceptions()) {
                ehs.reportExceptions(goBackToWhereTo);
            }
            else {
            	// FIXME: Temporary
            	if(routes != null && routes.size() > 0) {
            		Route firstRoute = routes.get(0);
            		timeLayout.setTimzoneOffset(firstRoute.getTimezoneOffset());
            		timeLayout.setModelForColumn(selectedColumn, firstRoute);
            	}
            	
                if(routes != null && updateMap) {
                    updateMap(routes);
                }
                
                // FIXME: Relying on updateMap is kind of hack-ish. Need to come up with more sophisticated way.
                timeLayout.setColumnState(selectedColumn, updateMap ? TimeButton.State.Selected : TimeButton.State.None);
                //timeLayout.setColumnState(selectedColumn, State.None);

                if (selectedColumn == 0) {
                    scrollableTimeLayout.notifyScrollChanged();
                }
            }
        }
    }
    
    private class RouteOverlayCallbackImpl implements OverlayCallback {

        private Route route;
        private int routeNum;
        
        public RouteOverlayCallbackImpl(Route route, int routeNum) {
            this.route = route;
            this.routeNum = routeNum;
        }
        
        @Override
        public boolean onBalloonTap(int index, OverlayItem item) {
        	Log.d("RouteActivity.RouteOverlayCallbackImpl", "onBalloonTap()");
        	for (RouteTask task : routeTasks) {
                task.cancel(true);
            }
            Intent intent = new Intent(RouteActivity.this, ReservationConfirmationActivity.class);
            Bundle extras = new Bundle();
            extras.putParcelable("route", route);
            intent.putExtras(extras);
            startActivityForResult(intent, RESERVATION_CONFIRM);
            
            return true;
        }

        @Override
        public void onChange() {
            /*for (int i = 0; i < routeInfoOverlays.length; i++) {
                RouteInfoOverlay routeInfoOverlay = routeInfoOverlays[i];
                if (routeInfoOverlay != null) {
                    routeInfoOverlay.showOverlay();
                }
            }*/
        }
        
        @Override
        public boolean onTap(int index) {
        	Log.d("RouteActivity.RouteOverlayCallbackImpl", "onTap()");
            // Highlight selected route path
        	setHighlightedRoutePathOverlays(false);
            routePathOverlays[routeNum].setHighlighted(true);
            //mapView.getController().setCenter(routeInfoOverlays[routeNum].getGeoPoint());
            
            return true;
        }

		@Override
		public boolean onClose() {
			Log.d("RouteActivity.RouteOverlayCallbackImpl", "onClose()");
			setHighlightedRoutePathOverlays(true);
			mapView.invalidate();
			
			return true;
		}

        @Override
        public boolean onLongPress(int index, OverlayItem item) {
            return false;
        }
    }
}
