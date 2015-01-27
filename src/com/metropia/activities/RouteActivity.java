package com.metropia.activities;

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
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.CalendarService;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.activities.DebugOptionsActivity.FakeRoute;
import com.metropia.activities.LandingActivity2.PoiOverlayInfo;
import com.metropia.dialogs.CancelableProgressDialog;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.exceptions.RouteNotFoundException;
import com.metropia.models.IncidentIcon;
import com.metropia.models.Reservation;
import com.metropia.models.Route;
import com.metropia.models.Trajectory;
import com.metropia.models.User;
import com.metropia.requests.CityRequest;
import com.metropia.requests.CityRequest.City;
import com.metropia.requests.IncidentRequest;
import com.metropia.requests.IncidentRequest.Incident;
import com.metropia.requests.ReservationListFetchRequest;
import com.metropia.requests.ReservationRequest;
import com.metropia.requests.RouteFetchRequest;
import com.metropia.tasks.GeocodingTask;
import com.metropia.tasks.GeocodingTaskCallback;
import com.metropia.tasks.ShortcutNavigationTask;
import com.metropia.ui.ClickAnimation;
import com.metropia.ui.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.ui.EditAddress;
import com.metropia.ui.menu.MainMenu;
import com.metropia.ui.overlays.EventOverlay;
import com.metropia.ui.overlays.OverlayCallback;
import com.metropia.ui.overlays.POIOverlay;
import com.metropia.ui.overlays.PointOverlay;
import com.metropia.ui.overlays.RouteDestinationOverlay;
import com.metropia.ui.overlays.RoutePathOverlay;
import com.metropia.ui.timelayout.ScrollableTimeLayout;
import com.metropia.ui.timelayout.TimeButton;
import com.metropia.ui.timelayout.TimeButton.DisplayMode;
import com.metropia.ui.timelayout.TimeButton.State;
import com.metropia.ui.timelayout.TimeColumn;
import com.metropia.ui.timelayout.TimeLayout;
import com.metropia.ui.timelayout.TimeLayout.TimeLayoutListener;
import com.metropia.ui.timelayout.TimeLayout.TimeLayoutOnSelectListener;
import com.metropia.utils.CalendarContract.Instances;
import com.metropia.utils.Dimension;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.Geocoding;
import com.metropia.utils.Misc;
import com.metropia.utils.RouteNode;
import com.metropia.utils.RouteRect;
import com.metropia.utils.SmartrekTileProvider;
import com.metropia.utils.SystemService;

/**
 * 
 *
 */
public final class RouteActivity extends FragmentActivity {
	
	public static final int ON_MY_WAY = Integer.valueOf(100);
	
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
	public static final String ORIGIN_COORD_PROVIDER = "ORIGIN_COORD_PROVIDER";
	public static final String ORIGIN_COORD_TIME = "ORIGIN_COORD_TIME";
    public static final String DEST_COORD = "destCoord";
    
    public static final String CURRENT_LOCATION = "CURRENT_LOCATION";
    
    public static final String RESERVATION = "reservation";
    
    public static final String RESCHEDULE_RESERVATION_ID = "RESCHEDULE_RESERVATION_ID";
    public static final String RESCHEDULE_DEPARTURE_TIME = "RESCHEDULE_DEPARTURE_TIME";
    
    public static final String ORIGIN_OVERLAY_INFO = "originOverlayInfo";
    public static final String DEST_OVERLAY_INFO = "destOverlayInfo";
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
    private Typeface boldFont;
    
    private Typeface lightFont;
    
    private Typeface mediumFont;
    
    //private RouteInfoOverlay[] routeInfoOverlays = new RouteInfoOverlay[3];
    private RoutePathOverlay[] routePathOverlays = new RoutePathOverlay[3];
    private POIOverlay[] routeOriginOverlays = new POIOverlay[3];
    private POIOverlay[] routeDestOverlays = new POIOverlay[3];
    
    
    private String originAddr;
    private String destAddr;
    private GeoPoint originCoord;
    private String originCoordProvider;
    private long originCoordTime;
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
    
    private PoiOverlayInfo originOverlayInfo;
    private PoiOverlayInfo destOverlayInfo;
    
    private List<Incident> incidents = new ArrayList<Incident>();
    
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
			Misc.doQuietly(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });
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
		    Misc.doQuietly(new Runnable() {
                @Override
                public void run() {
                    dialog.cancel();
                }
            });
			
			if (ehs.hasExceptions()) {
			    ehs.reportExceptions(goBackToWhereTo);
			}
			else if(destCoord == null || destCoord.isEmpty()){
				GeocodingTask task = new GeocodingTask(getBaseContext(), originCoord, ehs, destGeocodingTaskCallback);
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
			Misc.doQuietly(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });
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
		    Misc.doQuietly(new Runnable() {
                @Override
                public void run() {
                    dialog.cancel();
                }
            });
			
			if (ehs.hasExceptions()) {
			    ehs.reportExceptions(goBackToWhereTo);
			}
			else {
				retriveIncident(new Runnable() {
					@Override
					public void run() {
						RouteTask routeTask = new RouteTask(originCoord, destCoord, timeLayout.getDepartureTime(0), 0, true);
				        routeTasks.add(routeTask);
				        routeTask.execute();
					}
				});
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
        
        Localytics.integrate(this);
        
        initTimeTableDimension();
        
        othersPointOverlay = new PointOverlay(this, 0, 0);
        othersPointOverlay.setColor(0xCC2020DF);
        
        SharedPreferences prefs = getSharedPreferences(MapDisplayActivity.MAP_DISPLAY_PREFS, MODE_PRIVATE);
        
        /* Get the extras from the bundle */
        Bundle extras = getIntent().getExtras();
        
        originAddr = extras.getString(ORIGIN_ADDR);
        destAddr = extras.getString(DEST_ADDR);
        
        boolean currentLocation = extras.getBoolean(CURRENT_LOCATION);
        
        originOverlayInfo = extras.getParcelable(ORIGIN_OVERLAY_INFO);
        destOverlayInfo = extras.getParcelable(DEST_OVERLAY_INFO);
        
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
        
        bindMapFunctions();
        
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
        
        final String imComingMsg = extras.getString(MSG);
        final boolean hasImComingMsg = StringUtils.isNotBlank(imComingMsg);
        reservation = extras.getParcelable(RESERVATION);
        reservId = extras.getLong(RESERVATION_ID, 0);
        hasReservId = reservId > 0;
        hasReserv = reservation != null || hasReservId;
        if(hasReserv){
            findViewById(R.id.time_layout).setVisibility(View.GONE);
            if(hasReserv){
                final CancelableProgressDialog progressDialog = new CancelableProgressDialog(RouteActivity.this, "Loading...");
                final AsyncTask<Void, Void, List<Route>> routeTask = new AsyncTask<Void, Void, List<Route>>() {
                    
                    @Override
                    protected void onPreExecute() {
                        if(!progressDialog.isShowing()){
                            Misc.doQuietly(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.show();
                                }
                            });
                        }
                        destAddr = reservation.getDestinationAddress();
                        originAddr = reservation.getOriginAddress();
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
                        Misc.doQuietly(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.cancel();
                            }
                        });
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
                            updateMap(routes, true);
                        }
                    }
                };
                if(hasReservId){
                    User.initializeIfNeccessary(this, new Runnable() {
                        @Override
                        public void run() {
                            AsyncTask<Void, Void, List<Reservation>> tripTask = new AsyncTask<Void, Void, List<Reservation>>(){
                                
                                @Override
                                protected void onPreExecute() {
                                    Misc.doQuietly(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.show();
                                        }
                                    });
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
                                        Misc.doQuietly(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.cancel();
                                            }
                                        });
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
                    Log.d(LOG_TAG, "Column state: " + timeLayout.getColumnState(column) + " colume : " + column);
                    
                    showIncidentOverlays(timeButton.getDepartureTime());
                    
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

//				@Override
//				public void cancelOtherRouteTask(final TimeLayout timeLayout, final int selectedColumn) {
//					runOnUiThread(new Runnable() {
//						@Override
//						public void run() {
//							Set<Integer> columns = new HashSet<Integer>();
//							columns.addAll(loadingTasks.keySet());
//							for(Integer column : columns) {
//								State columnState = timeLayout.getColumnState(column);
//								if(!Integer.valueOf(selectedColumn).equals(column) && State.InProgress.equals(columnState)) {
//									RouteTask task = loadingTasks.remove(column);
//									task.cancel(true);
//									timeLayout.setColumnState(column, State.Unknown);
//								}
//							}
//						}
//					});
//				}
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
            originCoordProvider = extras.getString(ORIGIN_COORD_PROVIDER);
            originCoordTime = extras.getLong(ORIGIN_COORD_TIME);
            org.osmdroid.util.GeoPoint pDestCoord = extras.getParcelable(DEST_COORD);
            if(pOriginCoord != null){
                destCoord = new GeoPoint(pDestCoord);
            }
            
            final boolean _currentLocation = currentLocation;
            User.initializeIfNeccessary(this, new Runnable() {
                @Override
                public void run() {
                	if(originCoord != null && destCoord != null) {
                		doRouteTask(originCoord);
                	}
                	else {
	                	final CancelableProgressDialog currentLocDialog = new CancelableProgressDialog(RouteActivity.this, "Getting current location...");
	                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	                    locationListener = new LocationListener() {
	                        @Override
	                        public void onLocationChanged(final Location location) {
	                            try{
	                                locationChanged.set(true);
	                                locationManager.removeUpdates(this);
	                                Misc.doQuietly(new Runnable() {
	                                    @Override
	                                    public void run() {
	                                        currentLocDialog.dismiss();
	                                    }
	                                });
	                                speed = Trajectory.msToMph(location.getSpeed());
	                                course = location.getBearing();
	                                if(_currentLocation){
	                                    final String curLoc = DebugOptionsActivity.getCurrentLocation(RouteActivity.this);
	                                    if(StringUtils.isNotBlank(curLoc)){
	                                        AsyncTask<Void, Void, GeoPoint> task = new AsyncTask<Void, Void, GeoPoint>(){
	                                            @Override
	                                            protected GeoPoint doInBackground(Void... params) {
	                                                GeoPoint rs = null;
	                                                try{
	                                                    rs = Geocoding.lookup(getBaseContext(), curLoc, location.getLatitude(), location.getLongitude()).get(0).getGeoPoint();
	                                                }catch(Throwable t){}
	                                                return rs;
	                                            }
	                                            @Override
	                                            protected void onPostExecute(GeoPoint result) {
	                                                if(result != null){
	                                                    originCoord = result;
	                                                    doRouteTask(originCoord);
	                                                }
	                                            }
	                                        };
	                                        Misc.parallelExecute(task);
	                                    }else{
	                                    	originCoord = new GeoPoint(location.getLatitude(), location.getLongitude());
	                                    	originCoordProvider = location.getProvider();
	                                        originCoordTime = location.getTime();
	                                        doRouteTask(originCoord);
	                                    }
	                                }else{
	                                    doRouteTask(new GeoPoint(location.getLatitude(), location.getLongitude()));
	                                }
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
	                    Misc.doQuietly(new Runnable() {
	                        @Override
	                        public void run() {
	                            currentLocDialog.show();
	                        }
	                    });
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
	            }
            });
        }
        
        AssetManager assets = getAssets();
        boldFont = Font.getBold(assets);
        lightFont = Font.getLight(assets);
        mediumFont = Font.getMedium(assets);
        
        rescheduleReservId = extras.getLong(RESCHEDULE_RESERVATION_ID);
        rescheduleDepartureTime = extras.getLong(RESCHEDULE_DEPARTURE_TIME);
        final TextView reserveView = (TextView) findViewById(R.id.reserve);
        if(rescheduleReservId > 0) {
        	reserveView.setText("Reschedule Trip");
        	findViewById(R.id.reserve_panel).setBackgroundColor(getResources().getColor(R.color.metropia_red));
        }
        reserveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
            	v.setClickable(false);
            	ClickAnimation clickAnimation = new ClickAnimation(RouteActivity.this, v);
            	clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						v.setClickable(false);
						final Route route = (Route) reserveView.getTag();
		                AsyncTask<Void, Void, Long> task = new AsyncTask<Void, Void, Long>(){
		                    @Override
		                    protected Long doInBackground(Void... params) {
//		                    	timeLayout.cancelOtherRouteTask();
		                        Long rs = null;
		                        ReservationRequest request = new ReservationRequest(User.getCurrentUser(RouteActivity.this), 
		                            route, getString(R.string.distribution_date), rescheduleReservId);
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
		                        if (ehs.hasExceptions()) {
		                            ehs.reportExceptions();
		                            scrollableTimeLayout.notifyScrollChanged();
		                            v.setClickable(true);
		                        }
		                        else {
		                            deleteRescheduledReservation();
		                            Reservation.scheduleNotification(RouteActivity.this, result, route);
		                            
		                            if(route.isFake()){
		                                FakeRoute fakeRoute = new FakeRoute();
		                                fakeRoute.id = route.getId();
		                                fakeRoute.seq = route.getSeq();
		                                DebugOptionsActivity.addFakeRoute(RouteActivity.this, fakeRoute);
		                            }
		                            
		                            removeTerminateReservationId(result);
		                            Misc.suppressTripInfoPanel(RouteActivity.this);
		                            Intent intent = new Intent(RouteActivity.this, LandingActivity2.class);
		                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		                            startActivity(intent);
		                            finish();
		                        }
		                    }
		                    
		                };
		                Misc.parallelExecute(task);
					}
				});
            }
        });
        
        TextView onMyWayView = (TextView) findViewById(R.id.on_my_way);
        onMyWayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	ClickAnimation clickAnimation = new ClickAnimation(RouteActivity.this, v);
            	clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
					    Misc.suppressTripInfoPanel(RouteActivity.this);
						Intent contactSelect = new Intent(RouteActivity.this, ContactsSelectActivity.class);
		            	startActivityForResult(contactSelect, ON_MY_WAY);
					}
				});
            }
        });
        final TextView letsGoView = (TextView) findViewById(R.id.lets_go);
        letsGoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
            	v.setClickable(false);
            	ClickAnimation clickAnimation = new ClickAnimation(RouteActivity.this, v);
            	clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						v.setClickable(false);
						if(hasReserv){
	                        deleteRescheduledReservation();
	                        Misc.suppressTripInfoPanel(RouteActivity.this);
	                        Intent intent = new Intent(RouteActivity.this, ValidationActivity.class);
	                        intent.putExtra("route", reservation.getRoute());
	                        intent.putExtra("reservation", reservation);
	                        put_cur_location_if_GPS_provider_and_less_than_1_min_old(intent);
	                        startActivity(intent);
	                        finish();
	                    }else{
//	                    	timeLayout.cancelOtherRouteTask();
	                        final Route route = (Route) reserveView.getTag();
	                        ShortcutNavigationTask task = new ShortcutNavigationTask(RouteActivity.this, route, ehs, rescheduleReservId);
	                        task.callback = new ShortcutNavigationTask.Callback() {
	                            @Override
	                            public void run(Reservation reservation) {
	                                if(reservation.isEligibleTrip()){
	                                    deleteRescheduledReservation();
	                                    Misc.suppressTripInfoPanel(RouteActivity.this);
//	                                    Intent intent = new Intent(RouteActivity.this, ValidationActivity.class);
//	                                    intent.putExtra("route", reservation.getRoute());
//	                                    intent.putExtra("reservation", reservation);
//	                                    put_cur_location_if_GPS_provider_and_less_than_1_min_old(intent);
//	                                    startActivity(intent);
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
	                                        final NotificationDialog2 dialog = new NotificationDialog2(RouteActivity.this, msg);
	                                        Misc.doQuietly(new Runnable() {
	                                            @Override
	                                            public void run() {
	                                                dialog.show();
	                                            }
	                                        });
	                                    }
	                                    v.setClickable(true);
	                                }
	                            }

								@Override
								public void runOnFail() {
									scrollableTimeLayout.notifyScrollChanged();
									v.setClickable(true);
								}
	                        };
	                        Misc.parallelExecute(task);
	                    }
					}
            	});
            }
        });
        
        View backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	ClickAnimation clickAnimation = new ClickAnimation(RouteActivity.this, v);
            	clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						onBackPressed();
					}
				});
            }
        });
        
        TextView durationRow = (TextView)findViewById(R.id.duration_row);
        durationRow.setTag(DisplayMode.Duration);
        timeLayout.setDisplayMode(DisplayMode.Duration);
        durationRow.setText(DisplayMode.Duration.name());
        
        TextView arriveRow = (TextView)findViewById(R.id.arrive_row);
        
        Font.setTypeface(mediumFont, (TextView)findViewById(R.id.departure_row), arriveRow, 
                durationRow, (TextView)findViewById(R.id.mpoint_row), onMyWayView, letsGoView, reserveView);
        
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
    }
    
    private String incidentUrl;
    
    private void retriveIncident(final Runnable callback) {
    	AsyncTask<Void, Void, Void> getIncidentTask = new AsyncTask<Void, Void, Void>() {
    		
    		private CancelableProgressDialog dialog;
    		
    		@Override
            protected void onPreExecute() {
    			dialog = new CancelableProgressDialog(RouteActivity.this, "Get incident informations...");
                dialog.setActionListener(new CancelableProgressDialog.ActionListener() {
                    @Override
                    public void onClickNegativeButton() {
                        goBackToWhereTo.run();
                    }
                });
    		}
    		
    		@Override
			protected Void doInBackground(Void... params) {
    			CityRequest cityReq = new CityRequest(originCoord.getLatitude(), originCoord.getLongitude());
                try {
					City city = cityReq.execute(RouteActivity.this);
					if(city != null && StringUtils.isBlank(city.html)) {
						incidentUrl = city.incidents;
					}
				} catch (Exception ignore) {}
    			refreshIncident();
    			return null;
			}
    		
			@Override
	        protected void onPostExecute(Void result) {
				if (dialog.isShowing()) {
	                Misc.doQuietly(new Runnable() {
	                    @Override
	                    public void run() {
	                        dialog.dismiss();
	                    }
	                });
	            }
				if(callback != null) {
					callback.run();
				}
			}
    	};
    	Misc.parallelExecute(getIncidentTask);
    }
    
    private void refreshIncident() {
    	if(StringUtils.isNotBlank(incidentUrl)) {
    		incidents.clear();
			IncidentRequest incidentReq = new IncidentRequest(User.getCurrentUser(RouteActivity.this), incidentUrl);
			incidentReq.invalidateCache(RouteActivity.this);
			try {
				incidents.addAll(incidentReq.execute(RouteActivity.this));
			} catch (Exception ignore) {}
			List<Overlay> oldIncidentOverlay = new ArrayList<Overlay>();
			List<Overlay> all = mapView.getOverlays();
			for(final Overlay overlay : all) {
				if(overlay instanceof RouteDestinationOverlay) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							((RouteDestinationOverlay) overlay).hideBalloon();
						}
					});
					oldIncidentOverlay.add(overlay);
				}
			}
			all.removeAll(oldIncidentOverlay);
			showIncidentOverlays(timeLayout.getSelectedDepartureTime());
			mapView.postInvalidate();
		}
    }
    
    private void addIncidentIcons(Incident incident) {
		IncidentIcon icon = IncidentIcon.fromType(incident.type);
		final RouteDestinationOverlay inc = new RouteDestinationOverlay(mapView, new GeoPoint(incident.lat, incident.lon), mediumFont, incident.shortDesc, icon.getResourceId(RouteActivity.this));
		inc.setCallback(new OverlayCallback() {
			@Override
			public boolean onBalloonTap(int index, OverlayItem item) {
				return false;
			}
					
			@Override
			public boolean onTap(int index) {
				if(inc.isBalloonVisible()) {
					inc.hideBalloon();
				}
				else {
					List<Overlay> overlays = mapView.getOverlays();
					for(Overlay overlay : overlays) {
						if(overlay instanceof RouteDestinationOverlay) {
							((RouteDestinationOverlay) overlay).hideBalloon();
						}
					}
					inc.showBalloonOverlay();
				}
				return true;
			}
					
			@Override
			public boolean onClose() {
				return false;
			}

			@Override
			public void onChange() {
			}

			@Override
			public boolean onLongPress(int index, OverlayItem item) {
				return false;
			}
		});
		mapView.getOverlays().add(inc);
		mapView.postInvalidate();
    }
    
    private void showIncidentOverlays(long depTimeInMillis) {
    	hideIncidentOverlays();
    	List<Incident> incidentOfDepTime = getIncidentOfDepartureTime(depTimeInMillis);
    	Log.d("RouteActivity", "incidentOfDeptime size : " + incidentOfDepTime.size());
    	for(Incident incident : incidentOfDepTime) {
    		Overlay incidentOverlay = getOverlayOfGeoPoint(new GeoPoint(incident.lat, incident.lon));
    		if(incidentOverlay == null) {
    			addIncidentIcons(incident);
    		}
    		else {
    			incidentOverlay.setEnabled(true);
    		}
    	}
    }
    
    private Overlay getOverlayOfGeoPoint(GeoPoint point) {
    	List<Overlay> mapOverlays = mapView.getOverlays();
    	for(Overlay overlay : mapOverlays) {
    		if(overlay instanceof RouteDestinationOverlay) {
    			if(((RouteDestinationOverlay)overlay).getGeoPoint().equals(point)) {
    				return overlay;
    			}
    		}
    	}
    	return null;
    }
    
    private void hideIncidentOverlays() {
    	List<Overlay> overlays = mapView.getOverlays();
    	for(Overlay overlay : overlays) {
    		if(overlay instanceof RouteDestinationOverlay) {
    			((RouteDestinationOverlay) overlay).hideBalloon();
    			overlay.setEnabled(false);
    		}
    	}
    }
    
    private void bindMapFunctions() {
    	EventOverlay eventOverlay = new EventOverlay(this);
        eventOverlay.setActionListener(new EventOverlay.ActionListener() {
            
            @Override
            public void onSingleTap() {
            	List<Overlay> overlays = mapView.getOverlays();
            	for(Overlay overlay : overlays) {
            		if(overlay instanceof RouteDestinationOverlay) {
            			if(((RouteDestinationOverlay) overlay).isBalloonVisible()) {
            				((RouteDestinationOverlay) overlay).hideBalloon();
            			}
            		}
            	}
            }

			@Override
			public void onLongPress(double latitude, double longitude) {
			}
        });
        mapView.getOverlays().add(eventOverlay);
        mapView.postInvalidate();
    }
    
    private void removeTerminateReservationId(Long result) {
    	DebugOptionsActivity.removeTerminatedReservIds(RouteActivity.this, result);
    	sendBroadcast(new Intent(LandingActivity2.TRIP_INFO_CACHED_UPDATES));
    }
    
    private void initTimeTableDimension() {
	    DisplayMetrics dm = getResources().getDisplayMetrics();
	    Misc.initTimeButtonDimension(RouteActivity.this);
    	LayoutParams timeTicketPanelLp = findViewById(R.id.time_ticket_panel).getLayoutParams();
    	timeTicketPanelLp.height = TimeButton.FIRST_ROW_HEIGHT + TimeButton.HEIGHT * 3 + Dimension.dpToPx(2, dm);
    	LayoutParams departureRowLp = findViewById(R.id.departure_row).getLayoutParams();
    	departureRowLp.height = TimeButton.FIRST_ROW_HEIGHT;
    	LayoutParams arriveRowLp = findViewById(R.id.arrive_row).getLayoutParams();
    	arriveRowLp.height = TimeButton.HEIGHT;
    	LayoutParams durationRowLp = findViewById(R.id.duration_row).getLayoutParams();
    	durationRowLp.height = TimeButton.HEIGHT;
    	LayoutParams mpointRowLp = findViewById(R.id.mpoint_row).getLayoutParams();
    	mpointRowLp.height = TimeButton.HEIGHT;
    }
    
    private void put_cur_location_if_GPS_provider_and_less_than_1_min_old(Intent intent){
        if(LocationManager.GPS_PROVIDER.equals(originCoordProvider) 
                && (System.currentTimeMillis() - originCoordTime) < 60000){
            intent.putExtra(ValidationActivity.CURRENT_LOCATION, (Parcelable)originCoord);
        }
    }
    
    private long rescheduleReservId;
    private long rescheduleDepartureTime;
    
    private void deleteRescheduledReservation(){
        /*
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
        */
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
            scrollableTimeLayout.updateVisibleColumns();
        }
    }
    
    private void doRouteTask(GeoPoint currentLocation){
        if(originCoord == null || originCoord.isEmpty()){
            GeocodingTask task = new GeocodingTask(getBaseContext(), currentLocation, ehs, originGeocodingTaskCallback);
            task.execute(originAddr);
            geocodingTasks.add(task);
        }else if(destCoord == null || destCoord.isEmpty()){
            GeocodingTask task = new GeocodingTask(getBaseContext(), currentLocation, ehs, destGeocodingTaskCallback);
            task.execute(destAddr);
            geocodingTasks.add(task);
        }else{
        	retriveIncident(new Runnable() {
				@Override
				public void run() {
					RouteTask routeTask = new RouteTask(originCoord, destCoord, timeLayout.getDepartureTime(0), 0, true);
		            routeTasks.add(routeTask);
		            routeTask.execute();
				}
        	});
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Localytics.openSession();
	    Localytics.upload();
	    Localytics.setInAppMessageDisplayActivity(this);
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
	    // refresh incident
		retriveIncident(null);
    }
    
	@Override
	public void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
        Misc.tripInfoPanelOnActivityStop(this);
	}
	
	@Override
    protected void onRestart() {
        super.onRestart();
        Misc.tripInfoPanelOnActivityRestart(this);
    }
	
	@Override
    protected void onPause() {
		Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
	    super.onPause();
    } 
    
    @Override
    public void onBackPressed() {
        goBackToWhereTo.run();
    }
    
    private RouteRect routeRect;
    
    private void fitRouteToMap(boolean zoomToSpan){
        if(routeRect != null){
            /* Get a midpoint to center the view of  the routes */
            GeoPoint mid = routeRect.getMidPoint();
            /* range holds 2 points consisting of the lat/lon range to be displayed */
            int[] range = routeRect.getRange();
            /* Get the MapController set the midpoint and range */
            IMapController mc = mapView.getController();
            if(zoomToSpan) {
            	mc.zoomToSpan(range[0], range[1]);
            }
            mc.setCenter(mid); // setCenter only works properly after zoomToSpan
        }
    }
    
    private static final double mapZoomVerticalOffset = -0.3;
    
    /**
     * This function will be called when BackgroundDownloadTask().execute()
     * succeeds.
     * 
     * @param possibleRoutes
     */
    private void updateMap(List<Route> possibleRoutes, boolean zoomToSpan) {
    	
        if(possibleRoutes != null && possibleRoutes.size() > 0) {
            
            List<RouteNode> nodes = new ArrayList<RouteNode>(); 
            
            /* Iterate through the routes to draw each to the screen */
            for (int i = 0; i < possibleRoutes.size(); i++) {
                Route route = possibleRoutes.get(i);
            
                /* Draw the route to the screen and hold on to the range */
                drawRoute(mapView, route, i);
                
                nodes.addAll(route.getNodes());
            }
            
            routeRect = new RouteRect(nodes, mapZoomVerticalOffset);
            
            // Overlays must be drawn in orders
            for (int i = 0; i < possibleRoutes.size(); i++) {
            	final int _i = i;
            	mapOverlays.add(routePathOverlays[i]);
            	if(!EditAddress.CURRENT_LOCATION.equals(originAddr)) {
            		mapOverlays.add(routeOriginOverlays[i]);
            		routeOriginOverlays[i].setCallback(new OverlayCallback() {
            			@Override
            			public boolean onBalloonTap(int index, OverlayItem item) {
            				routeOriginOverlays[_i].switchBalloon();
            				return true;
            			}
            			@Override
            			public boolean onTap(int index) {
            				routeOriginOverlays[_i].switchBalloon();
            				return true;
            			}
            			@Override
            			public boolean onClose() {
            				return false;
            			}
            			@Override
            			public void onChange() {
            			}
            			@Override
            			public boolean onLongPress(int index, OverlayItem item) {
            				return false;
            			}
            		});
            		routeOriginOverlays[i].showBalloonOverlay();
            	}
            	mapOverlays.add(routeDestOverlays[i]);
            	
            	routeDestOverlays[i].setCallback(new OverlayCallback() {
                    @Override
                    public boolean onTap(int index) {
                        routeDestOverlays[_i].switchBalloon();
                        return true;
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
                    	routeDestOverlays[_i].switchBalloon();
                        return true;
                    }
                });
            	routeDestOverlays[i].showBalloonOverlay();
            }
            /*for (int i = 0; i < possibleRoutes.size(); i++) {
            	mapOverlays.add(routeInfoOverlays[i]);
            }*/
            
            fitRouteToMap(zoomToSpan);
            
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
                fitRouteToMap(true);
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
    private void updateRoute(GeoPoint origin, GeoPoint destination, long departureTime, final int column) throws InterruptedException {
        int letsGoPanelVis = column == 0?View.VISIBLE:View.GONE;
        int reservePanelVis = column == 0?View.GONE:View.VISIBLE;
        final RouteFetchRequest request = new RouteFetchRequest(User.getCurrentUser(this), 
            origin, destination, departureTime, speed, course, getOriginAddrRouteReqParam(), destAddr);
        if (request.isCached(this)) {
            try {
                List<Route> routes = request.execute(this);
                updateMap(routes, false);
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
        	AsyncTask<Void, Void, List<Route>> fetchRoute = new AsyncTask<Void, Void, List<Route>>() {
				@Override
				protected List<Route> doInBackground(Void... params) {
					try {
						return request.execute(RouteActivity.this);
					} catch (Exception e) {
						ehs.registerException(e);
					} 
					return null;
				}
				
				@Override
				protected void onPostExecute(List<Route> routes) {
					if(ehs.hasExceptions()) {
						ehs.reportExceptions();
					}
					else {
			        	updateMap(routes, false);
			        	timeLayout.setColumnState(column, TimeButton.State.Selected);
					}
				}
        		
        	};
        	Misc.parallelExecute(fetchRoute);
//            for (RouteTask task : routeTasks) {
//                task.cancel(true);
//            }
//            timeLayout.refresh();
//            RouteTask routeTask = new RouteTask(originCoord, destCoord, timeLayout.getDepartureTime(0), 0, true);
//            routeTasks.add(routeTask);
//            routeTask.execute();
//            letsGoPanelVis = View.VISIBLE;
//            reservePanelVis = View.GONE;
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
        
        List<Overlay> resetOverlays = new ArrayList<Overlay>();
        for (Overlay o : mapOverlays) {
            if(o instanceof POIOverlay){
            	resetOverlays.add(o);
                ((POIOverlay) o).hideBalloon();
            }
            else if(o instanceof RouteDestinationOverlay) {
            	((RouteDestinationOverlay) o).hideBalloon();
            }
            else if(!(o instanceof EventOverlay)){
            	resetOverlays.add(o);
            }
        }
        
        if(routeNum == 0) {
            mapOverlays.removeAll(resetOverlays);
        }
        
        int routeColor = route.getColor()!=null?Color.parseColor(route.getColor()):RoutePathOverlay.COLORS[routeNum];
        
        int originDrawableId = 0;
        if(EditAddress.CURRENT_LOCATION.equals(originAddr)) {
        	originDrawableId = R.drawable.landing_page_current_location;
        }
        routePathOverlays[routeNum] = new RoutePathOverlay(this, route, routeColor, originDrawableId);
        //mapOverlays.add(routePathOverlays[routeNum]);
        if(!EditAddress.CURRENT_LOCATION.equals(originAddr)) {
	        routeOriginOverlays[routeNum] = new POIOverlay(mapView, lightFont, 
	        		getPoiOverlayInfo(originOverlayInfo, route.getFirstNode().getGeoPoint(), originAddr), 
	        		HotspotPlace.CENTER, null);
	        routeOriginOverlays[routeNum].inRoutePage();
	        routeOriginOverlays[routeNum].setIsFromPoi(true);
        }
        routeDestOverlays[routeNum] = new POIOverlay(mapView, lightFont, 
        		getPoiOverlayInfo(destOverlayInfo, route.getLastNode().getGeoPoint(), destAddr), 
        		HotspotPlace.CENTER, null);
        routeDestOverlays[routeNum].inRoutePage();
        routeDestOverlays[routeNum].setIsFromPoi(false);
        
        /* Set values into route to be passed to next Activity */
        route.setAddresses(originAddr, destAddr);
        
        // FIXME:
        route.setUserId(User.getCurrentUser(this).getId());
        findViewById(R.id.reserve).setTag(route);
//        routeInfoOverlays[routeNum] = new RouteInfoOverlay(mapView, route, routeNum, new GeoPoint(lat, lon), boldFont, lightFont);
//        routeInfoOverlays[routeNum].setCallback(new RouteOverlayCallbackImpl(route, routeNum));
        //mapOverlays.add(routeOverlays[routeNum]);
    }
    
    private PoiOverlayInfo getPoiOverlayInfo(PoiOverlayInfo poiInfo, GeoPoint geoPoint, String address) {
    	if(poiInfo == null) {
    		poiInfo = new PoiOverlayInfo();
    		poiInfo.address = address;
    		poiInfo.geopoint = geoPoint;
    	}
    	return poiInfo;
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
        
        if(requestCode == ON_MY_WAY && resultCode == Activity.RESULT_OK) {
        	final String emails = extras.getString(ValidationActivity.EMAILS);
        	final String phones = extras.getString(ValidationActivity.PHONES);
        	if(hasReserv){
        	    deleteRescheduledReservation();
        	    Misc.suppressTripInfoPanel(RouteActivity.this);
                Intent validationActivity = new Intent(RouteActivity.this, ValidationActivity.class);
                validationActivity.putExtra("route", reservation.getRoute());
                validationActivity.putExtra("reservation", reservation);
                put_cur_location_if_GPS_provider_and_less_than_1_min_old(validationActivity);
                validationActivity.putExtra(ValidationActivity.EMAILS, emails);
                validationActivity.putExtra(ValidationActivity.PHONES, phones);
                startActivity(validationActivity);
                finish();
            }else{
            	final TextView reserveView = (TextView) findViewById(R.id.reserve);
                final Route route = (Route) reserveView.getTag();
                ShortcutNavigationTask task = new ShortcutNavigationTask(RouteActivity.this, route, ehs, rescheduleReservId);
                task.callback = new ShortcutNavigationTask.Callback() {
                    @Override
                    public void run(Reservation reservation) {
                        if(reservation.isEligibleTrip()){
                            deleteRescheduledReservation();
                            Misc.suppressTripInfoPanel(RouteActivity.this);
                            Intent intent = new Intent(RouteActivity.this, ValidationActivity.class);
                            intent.putExtra("route", reservation.getRoute());
                            intent.putExtra("reservation", reservation);
                            put_cur_location_if_GPS_provider_and_less_than_1_min_old(intent);
                            intent.putExtra(ValidationActivity.EMAILS, emails);
                            intent.putExtra(ValidationActivity.PHONES, phones);
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
                                final NotificationDialog2 dialog = new NotificationDialog2(RouteActivity.this, msg);
                                Misc.doQuietly(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.show();
                                    }
                                });
                            }
                        }
                    }

					@Override
					public void runOnFail() {
						// do nothing
					}
                };
                Misc.parallelExecute(task);
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
        		    Misc.doQuietly(new Runnable() {
                        @Override
                        public void run() {
                            dialog.show();
                        }
        		    });
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
            	ehs.registerException(e, e.getMessage());
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
                Misc.doQuietly(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }
            
            setHighlightedRoutePathOverlays(true);
            mapView.postInvalidate();
            
            if (ehs.hasExceptions() && selectedColumn == 0) {
                ehs.reportExceptions(goBackToWhereTo);
            }
            else {
            	// FIXME: Temporary
            	if(routes != null && routes.size() > 0) {
            		Route firstRoute = routes.get(0);
            		timeLayout.setTimzoneOffset(firstRoute.getTimezoneOffset());
            		timeLayout.setModelForColumn(selectedColumn, firstRoute);
            	}
            	
            	if(updateMap) {
            		showIncidentOverlays(timeLayout.getSelectedDepartureTime());
            	}
            	
                if(routes != null && updateMap) {
                    updateMap(routes, true);
                }
                
                // FIXME: Relying on updateMap is kind of hack-ish. Need to come up with more sophisticated way.
                timeLayout.setColumnState(selectedColumn, updateMap ? TimeButton.State.Selected : TimeButton.State.None);
                //timeLayout.setColumnState(selectedColumn, State.None);

                if (selectedColumn == 0) {
                    scrollableTimeLayout.notifyScrollChanged();
                    scrollableTimeLayout.updateVisibleColumns();
                    scrollToRescheduleDepartureTime();
                }
                
                timeLayout.notifySelectColumn(selectedColumn);
            }
        }
    }
    
    private void scrollToRescheduleDepartureTime() {
    	if(rescheduleReservId > 0) {
    		scrollableTimeLayout.scrollToDepartureTime(rescheduleDepartureTime);
    	}
    }
    
    private List<Incident> getIncidentOfDepartureTime(long departureTime) {
    	List<Incident> incidentOfDepTime = new ArrayList<Incident>();
    	if(incidents != null && incidents.size() > 0) {
    		long timezoneOffsetInMillis = timeLayout.getTimzoneOffset() * 60 * 60 * 1000;
    		for(Incident incident : incidents) {
    			if((incident.startTime.getTime() + timezoneOffsetInMillis) <= departureTime && 
    					(incident.endTime.getTime() + timezoneOffsetInMillis) >= departureTime) {
    				incidentOfDepTime.add(incident);
    			}
    		}
    	}
    	return incidentOfDepTime;
    }
    

}
