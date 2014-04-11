package com.smartrek.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.smartrek.ContactListService;
import com.smartrek.activities.DebugOptionsActivity.FakeRoute;
import com.smartrek.activities.LandingActivity.ShortcutNavigationTask;
import com.smartrek.dialogs.CancelableProgressDialog;
import com.smartrek.dialogs.NotificationDialog2;
import com.smartrek.exceptions.RouteNotFoundException;
import com.smartrek.models.Contact;
import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.models.Trajectory;
import com.smartrek.models.User;
import com.smartrek.requests.ReservationDeleteRequest;
import com.smartrek.requests.ReservationListFetchRequest;
import com.smartrek.requests.ReservationRequest;
import com.smartrek.requests.RouteFetchRequest;
import com.smartrek.tasks.GeocodingTask;
import com.smartrek.tasks.GeocodingTaskCallback;
import com.smartrek.ui.EditAddress;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.ui.overlays.OverlayCallback;
import com.smartrek.ui.overlays.PointOverlay;
import com.smartrek.ui.overlays.RouteDestinationOverlay;
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
	
	public static final int ON_MY_WAY = 100;

    public static final String RESERVATION_ID = "reservationId";
    
    public static final String LAT = "lat";
    
    public static final String LON = "lon";
    
    public static final String MSG = "msg";
    
    public static final String EVENT_ID = "event_id";
    
    public static final int RESERVATION_CONFIRM = 3;
    
    public static final int RESERVATION_CONFIRM_ENDED = 3;
    
	public static final String LOG_TAG = "RouteActivity";
	
	public static final String ORIGIN_ADDR = "originAddr";
	public static final String DEST_ADDR = "destAddr";
	
	public static final String ORIGIN_COORD = "originCoord";
    public static final String DEST_COORD = "destCoord";
    
    public static final String CURRENT_LOCATION = "CURRENT_LOCATION";
    
    public static final String RESERVATION = "reservation";
    
    public static final String RESCHEDULE_RESERVATION_ID = "RESCHEDULE_RESERVATION_ID";
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
    private Typeface boldFont;
    
    private Typeface lightFont;
    
    //private RouteInfoOverlay[] routeInfoOverlays = new RouteInfoOverlay[3];
    private RoutePathOverlay[] routePathOverlays = new RoutePathOverlay[3];
    
    private RouteDestinationOverlay[] routeDestOverlays = new RouteDestinationOverlay[3];
    
    private String originAddr;
    private String destAddr;
    private GeoPoint originCoord;
    private GeoPoint destCoord;
    
    private double speed;
    private float course;
    
    private Reservation reservation;
    
    // TODO: 'dialog' isn't really meaningful. Rename this variable.
    private ProgressDialog dialog;
    
    private MapView mapView;
    
    private Time selectedTime;
    
    private TimeLayout timeLayout;
    
    private ScrollableTimeLayout scrollableTimeLayout;
    
    private List<RouteTask> routeTasks = new Vector<RouteTask>();
    
    private List<GeocodingTask> geocodingTasks = new ArrayList<GeocodingTask>();
    
    private boolean debugMode;
    
    private long reservId;
    private boolean hasReservId;
    private boolean hasReserv;
    
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
				GeocodingTask task = new GeocodingTask(getBaseContext(), ehs, destGeocodingTaskCallback);
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
        
        othersPointOverlay = new PointOverlay(this, 0, 0, R.drawable.landing_page_current_location);
        othersPointOverlay.setColor(0xCC2020DF);
        
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
        
        /* Set the map view for a view of North America before zooming in on route */
        setViewToNorthAmerica(mapView);
        
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
        
        TextView header = (TextView) findViewById(R.id.header);
        
        final String imComingMsg = extras.getString(MSG);
        final boolean hasImComingMsg = StringUtils.isNotBlank(imComingMsg);
        reservation = extras.getParcelable(RESERVATION);
        reservId = extras.getLong(RESERVATION_ID, 0);
        hasReservId = reservId > 0;
        hasReserv = reservation != null || hasReservId;
        if(hasReserv){
            header.setText("Let's Go");
            findViewById(R.id.time_layout).setVisibility(View.GONE);
            if(hasReserv){
                final CancelableProgressDialog progressDialog = new CancelableProgressDialog(RouteActivity.this, "Loading...");
                final AsyncTask<Void, Void, List<Route>> routeTask = new AsyncTask<Void, Void, List<Route>>() {
                    
                    @Override
                    protected void onPreExecute() {
                        if(!progressDialog.isShowing()){
                            progressDialog.show();
                        }
                        destAddr = reservation.getDestinationAddress();
                    }
                    
                    @Override
                    protected List<Route> doInBackground(Void... params) {
                        List<Route> routes = null;
                        try {
                            RouteFetchRequest request = new RouteFetchRequest(
                                reservation.getNavLink(),
                                reservation.getDepartureTime(), 
                                reservation.getDuration(),
                                0,
                                0);
                            routes = request.execute(RouteActivity.this);
                        }
                        catch(Exception e) {
                            ehs.registerException(e);
                        }                                
                        return routes;
                    }
                    protected void onPostExecute(java.util.List<Route> routes) {
                        progressDialog.cancel();
                        if (ehs.hasExceptions()) {
                            ehs.reportExceptions(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            });
                        }else if(routes != null && routes.size() > 0) {
                            Route route = routes.get(0);
                            route.setCredits(reservation.getCredits());
                            route.preprocessNodes();
                            updateMap(routes);
                        }
                    }
                };
                if(hasReservId){
                    LandingActivity.initializeIfNeccessary(this, new Runnable() {
                        @Override
                        public void run() {
                            AsyncTask<Void, Void, List<Reservation>> tripTask = new AsyncTask<Void, Void, List<Reservation>>(){
                                
                                @Override
                                protected void onPreExecute() {
                                    progressDialog.show();
                                }
                                
                                @Override
                                protected List<Reservation> doInBackground(Void... params) {
                                    User user = User.getCurrentUser(RouteActivity.this);
                                    List<Reservation> reservations= Collections.emptyList();
                                    try {
                                        ReservationListFetchRequest resReq = new ReservationListFetchRequest(user);
                                        resReq.invalidateCache(RouteActivity.this);
                                        reservations = resReq.execute(RouteActivity.this);
                                    }
                                    catch (NullPointerException e){}
                                    catch (Exception e) {
                                        ehs.registerException(e);
                                    }
                                    return reservations;
                                }
                                @Override
                                protected void onPostExecute(List<Reservation> reservations) {
                                    if (ehs.hasExceptions()) { 
                                        progressDialog.cancel();
                                        ehs.reportExceptions();
                                    } 
                                    else{
                                        for(Reservation r:reservations){
                                            if(r.getRid() == reservId){
                                                reservation = r;
                                                Misc.parallelExecute(routeTask);
                                                break;
                                            }
                                        }
                                    }
                                }
                            };
                            Misc.parallelExecute(tripTask);
                        }
                    });
                }else{
                    Misc.parallelExecute(routeTask);
                }
            }
        }else if(hasImComingMsg){
            handleImComing(imComingMsg, extras.getDouble(LAT, 0), 
                extras.getDouble(LON, 0));
        }else{
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
            timeLayout.setDisplayMode((timeDisplayMode & MapDisplayActivity.TIME_DISPLAY_TRAVEL) != 0 ? DisplayMode.Duration : DisplayMode.Arrival);
            
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
                        final String curLoc = DebugOptionsActivity.getCurrentLocation(RouteActivity.this);
                        if(StringUtils.isNotBlank(curLoc)){
                            AsyncTask<Void, Void, GeoPoint> task = new AsyncTask<Void, Void, GeoPoint>(){
                                @Override
                                protected GeoPoint doInBackground(Void... params) {
                                    GeoPoint rs = null;
                                    try{
                                        rs = Geocoding.lookup(getBaseContext(), curLoc).get(0).getGeoPoint();
                                    }catch(Throwable t){}
                                    return rs;
                                }
                                @Override
                                protected void onPostExecute(GeoPoint result) {
                                    if(result != null){
                                        originCoord = result;
                                        doRouteTask();
                                    }
                                }
                            };
                            Misc.parallelExecute(task);
                        }else{
                            final CancelableProgressDialog currentLocDialog = new CancelableProgressDialog(RouteActivity.this, "Getting current location...");
                            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            locationListener = new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    try{
                                        locationChanged.set(true);
                                        locationManager.removeUpdates(this);
                                        currentLocDialog.dismiss();
                                        originCoord = new GeoPoint(location.getLatitude(), location.getLongitude());
                                        speed = Trajectory.msToMph(location.getSpeed());
                                        course = location.getBearing();
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
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    requestNetworkLocation();
                                }
                            }, 10000);
                            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                SystemService.alertNoGPS(RouteActivity.this, true, new SystemService.Callback(){
                                    @Override
                                    public void onNo() {
                                        requestNetworkLocation();
                                    }
                                });
                            }
                        }
                    }else{
                        doRouteTask();
                    }
                }
            });
        }
        
        AssetManager assets = getAssets();
        boldFont = Font.getBold(assets);
        lightFont = Font.getLight(assets);
        
        rescheduleReservId = extras.getLong(RESCHEDULE_RESERVATION_ID);
        
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
                            deleteRescheduledReservation();
                            ReservationConfirmationActivity.scheduleNotification(RouteActivity.this, result, route);
                            
                            if(route.isFake()){
                                FakeRoute fakeRoute = new FakeRoute();
                                fakeRoute.id = route.getId();
                                fakeRoute.seq = route.getSeq();
                                DebugOptionsActivity.addFakeRoute(RouteActivity.this, fakeRoute);
                            }
                            
                            SessionM.logAction("make_reservation");
                            
                            NotificationDialog2 dialog = new NotificationDialog2(RouteActivity.this, "You have successfully reserved a route.");
                            dialog.setTitle("Notification");
                            dialog.setPositiveActionListener(new NotificationDialog2.ActionListener() {
                                
                                @Override
                                public void onClick() {
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
            	Intent contactSelect = new Intent(RouteActivity.this, ContactsSelectActivity.class);
            	contactSelect.putParcelableArrayListExtra(ContactsSelectActivity.CONTACT_LIST, contactList);
            	startActivityForResult(contactSelect, ON_MY_WAY);
            	/*
                ContactsDialog d = new ContactsDialog(RouteActivity.this);
                d.setActionListener(new ContactsDialog.ActionListener() {
                    @Override
                    public void onClickPositiveButton(final List<String> emails) {
                        if(hasReserv){
                            Intent intent = new Intent(RouteActivity.this, ValidationActivity.class);
                            intent.putExtra("route", reservation.getRoute());
                            intent.putExtra("reservation", reservation);
                            startActivity(intent);
                            finish();
                        }else{
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
                        SessionM.logAction("on_my_way");
                    }
                    @Override
                    public void onClickNegativeButton() {}
                });
                d.show();
            	 */
            }
        });
        TextView letsGoView = (TextView) findViewById(R.id.lets_go);
        letsGoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasReserv){
                    deleteRescheduledReservation();
                    Intent intent = new Intent(RouteActivity.this, ValidationActivity.class);
                    intent.putExtra("route", reservation.getRoute());
                    intent.putExtra("reservation", reservation);
                    startActivity(intent);
                    finish();
                }else{
                    final Route route = (Route) reserveView.getTag();
                    ShortcutNavigationTask task = new ShortcutNavigationTask(RouteActivity.this, route, ehs);
                    task.callback = new ShortcutNavigationTask.Callback() {
                        @Override
                        public void run(Reservation reservation) {
                            if(reservation.isEligibleTrip()){
                                deleteRescheduledReservation();
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
                                    NotificationDialog2 dialog = new NotificationDialog2(RouteActivity.this, msg);
                                    dialog.show();
                                }
                            }
                        }
                    };
                    Misc.parallelExecute(task);
                }
            }
        });
        
        TextView backButton = (TextView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        final TextView durationRow = (TextView)findViewById(R.id.duration_row);
        durationRow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayMode curDisplayMode = (DisplayMode) durationRow.getTag();
                DisplayMode newDisplayMode = (curDisplayMode == null || curDisplayMode.equals(DisplayMode.Duration)) 
                    ? DisplayMode.Arrival:DisplayMode.Duration;
                durationRow.setTag(newDisplayMode);
                timeLayout.setDisplayMode(newDisplayMode);
                durationRow.setText(newDisplayMode.name());
            }
        });
        
        if (MapDisplayActivity.isDisplayDuration(this)) {
            durationRow.setTag(DisplayMode.Duration);
            timeLayout.setDisplayMode(DisplayMode.Duration);
            durationRow.setText(DisplayMode.Duration.name());
        }
        else {
        	durationRow.setTag(DisplayMode.Arrival);
            timeLayout.setDisplayMode(DisplayMode.Arrival);
            durationRow.setText(DisplayMode.Arrival.name());
        }
        
        preloadContactList();
        
        Font.setTypeface(boldFont, header);
        Font.setTypeface(lightFont, onMyWayView, letsGoView, reserveView,
            backButton, (TextView)findViewById(R.id.departure_row), durationRow,
            (TextView)findViewById(R.id.mpoint_row));
    }
    
    private ArrayList<Contact> contactList = new ArrayList<Contact>();
    
    private void preloadContactList(){
        AsyncTask<Void, Void, ArrayList<Contact>> task = new AsyncTask<Void, Void, ArrayList<Contact>>(){
            @Override
            protected ArrayList<Contact> doInBackground(Void... params) {
                return ContactListService.getSyncedContactList(RouteActivity.this);
            }
            @Override
            protected void onPostExecute(ArrayList<Contact> result) {
                contactList = result;
            }
        };
        Misc.parallelExecute(task);
    }
    
    private long rescheduleReservId;
    
    private void deleteRescheduledReservation(){
        if(rescheduleReservId > 0){
            AsyncTask<Void, Void, Void> delTask = new AsyncTask<Void, Void, Void>(){
                @Override
                protected Void doInBackground(Void... params) {
                    ReservationDeleteRequest request = new ReservationDeleteRequest(
                        User.getCurrentUser(RouteActivity.this), rescheduleReservId);
                    try {
                        request.execute(RouteActivity.this);
                    }
                    catch (Exception e) {
                    }
                    return null;
                }
            };
            Misc.parallelExecute(delTask);
        }
    }
    
    public static void setViewToNorthAmerica(MapView mapView){
        IMapController mc = mapView.getController();
        int lat = (int) Math.round(38.27268853598097f*1E6);
        int lon = (int) Math.round(-99.1406250000000f*1E6);
        mc.setZoom(3); 
        mc.setCenter(new GeoPoint(lat, lon));
    }
    
    private AtomicBoolean locationChanged = new AtomicBoolean();
    
    private void requestNetworkLocation(){
        if(locationManager != null && locationListener != null){
            if(!locationChanged.get()){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String imComingMsg = intent.getStringExtra(MSG);
        if(StringUtils.isNotBlank(imComingMsg)){
            handleImComing(imComingMsg, intent.getDoubleExtra(LAT, 0), 
                intent.getDoubleExtra(LON, 0));
        }
    }
    
    PointOverlay othersPointOverlay;
    
    private void handleImComing(String msg, final double lat, final double lon){
        TextView header = (TextView) findViewById(R.id.header);
        header.setText("On The Way");
        findViewById(R.id.time_layout).setVisibility(View.GONE);
        findViewById(R.id.lets_go_panel).setVisibility(View.GONE);
        final MapView mapView = (MapView) findViewById(R.id.mapview);
        final List<Overlay> mapOverlays = mapView.getOverlays();
        mapOverlays.clear();
        othersPointOverlay.setLocation((float) lat, (float)lon);
        mapOverlays.add(othersPointOverlay);
        RouteDestinationOverlay msgView = new RouteDestinationOverlay(mapView, 
            new GeoPoint(lat, lon), lightFont, msg, android.R.color.transparent);
        msgView.setBalloonOffsetY(0);
        mapOverlays.add(msgView);
        msgView.showBalloonOverlay();
        mapView.postInvalidate();
        IMapController mc = mapView.getController();
        mc.setZoom(ValidationActivity.DEFAULT_ZOOM_LEVEL);
        mc.setCenter(new GeoPoint(lat, lon));
    }
    
    private void updateTimetableScreenWidth(){
        if(scrollableTimeLayout != null){
            scrollableTimeLayout.setScreenWidth(getWindowManager().getDefaultDisplay().getWidth());
            scrollableTimeLayout.notifyScrollChanged();
        }
    }
    
    private void doRouteTask(){
        if(originCoord == null || originCoord.isEmpty()){
            GeocodingTask task = new GeocodingTask(getBaseContext(), ehs, originGeocodingTaskCallback);
            task.execute(originAddr);
            geocodingTasks.add(task);
        }else if(destCoord == null || destCoord.isEmpty()){
            GeocodingTask task = new GeocodingTask(getBaseContext(), ehs, destGeocodingTaskCallback);
            task.execute(destAddr);
            geocodingTasks.add(task);
        }else{
            RouteTask routeTask = new RouteTask(originCoord, destCoord, timeLayout.getDepartureTime(0), 0, true);
            routeTasks.add(routeTask);
            routeTask.execute();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        SessionM.onActivityResume(this);
    }
    
	@Override
	public void onStart() {
		super.onStart();
		SessionM.onActivityStart(this);
		EasyTracker.getInstance().activityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		SessionM.onActivityStop(this);
		EasyTracker.getInstance().activityStop(this);
	}
	
	@Override
    protected void onPause() {
      SessionM.onActivityPause(this);
      super.onPause();
    } 
    
    @Override
    public void onBackPressed() {
        goBackToWhereTo.run();
    }
    
    private RouteRect routeRect;
    
    private void fitRouteToMap(){
        if(routeRect != null){
            /* Get a midpoint to center the view of  the routes */
            GeoPoint mid = routeRect.getMidPoint();
            /* range holds 2 points consisting of the lat/lon range to be displayed */
            int[] range = routeRect.getRange();
            /* Get the MapController set the midpoint and range */
            IMapController mc = mapView.getController();
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
            	mapOverlays.add(routeDestOverlays[i]);
            	final int _i = i;
            	routeDestOverlays[i].setCallback(new OverlayCallback() {
                    @Override
                    public boolean onTap(int index) {
                        if(routeDestOverlays[_i].isBalloonVisible()){
                            routeDestOverlays[_i].hideBalloon();
                        }else{
                            routeDestOverlays[_i].showBalloonOverlay();
                        }
                        return false;
                    }
                    @Override
                    public boolean onLongPress(int index, OverlayItem item) {
                        return false;
                    }
                    
                    @Override
                    public boolean onClose() {
                        return false;
                    }
                    @Override
                    public void onChange() {
                    }
                    @Override
                    public boolean onBalloonTap(int index, OverlayItem item) {
                        return false;
                    }
                });
            	routeDestOverlays[i].showBalloonOverlay();
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
        RouteFetchRequest request = new RouteFetchRequest(User.getCurrentUser(this), 
            origin, destination, departureTime, speed, course, getOriginAddrRouteReqParam(), destAddr);
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
    public void drawRoute (MapView mapView, Route route, int routeNum) {
        mapOverlays = mapView.getOverlays();
        
        if(routeNum == 0)
            mapOverlays.clear();
        
        routePathOverlays[routeNum] = new RoutePathOverlay(this, route, RoutePathOverlay.COLORS[routeNum]);
        //mapOverlays.add(routePathOverlays[routeNum]);
        
        routeDestOverlays[routeNum] = new RouteDestinationOverlay(mapView, route.getLastNode().getGeoPoint(), 
            lightFont, destAddr, R.drawable.pin_destination);
        
        /* Set values into route to be passed to next Activity */
        route.setAddresses(originAddr, destAddr);
        
        // FIXME:
        route.setUserId(User.getCurrentUser(this).getId());
        
        findViewById(R.id.reserve).setTag(route);
//        routeInfoOverlays[routeNum] = new RouteInfoOverlay(mapView, route, routeNum, new GeoPoint(lat, lon), boldFont, lightFont);
//        routeInfoOverlays[routeNum].setCallback(new RouteOverlayCallbackImpl(route, routeNum));
        //mapOverlays.add(routeOverlays[routeNum]);
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
                timeLayout.setDisplayMode((displayMode & MapDisplayActivity.TIME_DISPLAY_TRAVEL) != 0 ? DisplayMode.Duration : DisplayMode.Arrival);
            }
        } 
        
        if(requestCode == RESERVATION_CONFIRM){
            if(resultCode == RESERVATION_CONFIRM_ENDED){
                goBackToWhereTo.run();
            }
        }
        
        Log.d("RouteActivityResultCode", resultCode + "");
        if(requestCode == ON_MY_WAY && resultCode == Activity.RESULT_OK) {
        	final String emails = extras.getString(ValidationActivity.EMAILS);
        	if(hasReserv){
        	    deleteRescheduledReservation();
                Intent validationActivity = new Intent(RouteActivity.this, ValidationActivity.class);
                validationActivity.putExtra("route", reservation.getRoute());
                validationActivity.putExtra("reservation", reservation);
                validationActivity.putExtra(ValidationActivity.EMAILS, emails);
                startActivity(validationActivity);
                finish();
            }else{
            	final TextView reserveView = (TextView) findViewById(R.id.reserve);
                final Route route = (Route) reserveView.getTag();
                ShortcutNavigationTask task = new ShortcutNavigationTask(RouteActivity.this, route, ehs);
                task.callback = new ShortcutNavigationTask.Callback() {
                    @Override
                    public void run(Reservation reservation) {
                        if(reservation.isEligibleTrip()){
                            deleteRescheduledReservation();
                            Intent intent = new Intent(RouteActivity.this, ValidationActivity.class);
                            intent.putExtra("route", reservation.getRoute());
                            intent.putExtra("reservation", reservation);
                            intent.putExtra(ValidationActivity.EMAILS, emails);
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
                                NotificationDialog2 dialog = new NotificationDialog2(RouteActivity.this, msg);
                                dialog.show();
                            }
                        }
                    }
                };
                Misc.parallelExecute(task);
            }
            SessionM.logAction("on_my_way");
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
    
    private String getOriginAddrRouteReqParam(){
        return EditAddress.CURRENT_LOCATION.equals(originAddr)?null:originAddr;
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
        	RouteFetchRequest request = new RouteFetchRequest(User.getCurrentUser(RouteActivity.this), 
    	        origin, destination, departureTime, speed, course, getOriginAddrRouteReqParam(), destAddr);
        	return request.isCached(RouteActivity.this);
        }
        
        public List<Route> getData() throws RouteNotFoundException, IOException, JSONException, InterruptedException {
        	RouteFetchRequest request = new RouteFetchRequest(User.getCurrentUser(RouteActivity.this), 
    	        origin, destination, departureTime, speed, course, getOriginAddrRouteReqParam(), destAddr);
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
