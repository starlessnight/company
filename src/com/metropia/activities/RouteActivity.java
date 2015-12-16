package com.metropia.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.CalendarService;
import com.metropia.LocalyticsUtils;
import com.metropia.SkobblerUtils;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.activities.DebugOptionsActivity.FakeRoute;
import com.metropia.dialogs.CancelableProgressDialog;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.exceptions.RouteNotFoundException;
import com.metropia.models.IncidentIcon;
import com.metropia.models.PoiOverlayInfo;
import com.metropia.models.Reservation;
import com.metropia.models.ReservationTollHovInfo;
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
import com.metropia.tasks.ICallback;
import com.metropia.tasks.ImageLoader;
import com.metropia.tasks.ShortcutNavigationTask;
import com.metropia.ui.EditAddress;
import com.metropia.ui.SkobblerImageView;
import com.metropia.ui.animation.ClickAnimation;
import com.metropia.ui.animation.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.ui.menu.MainMenu;
import com.metropia.ui.overlays.PointOverlay;
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
import com.metropia.utils.Preferences;
import com.metropia.utils.RouteNode;
import com.metropia.utils.RouteRect;
import com.metropia.utils.SystemService;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.map.SKAnimationSettings;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKAnnotationView;
import com.skobbler.ngx.map.SKCoordinateRegion;
import com.skobbler.ngx.map.SKMapCustomPOI;
import com.skobbler.ngx.map.SKMapPOI;
import com.skobbler.ngx.map.SKMapSettings.SKMapDisplayMode;
import com.skobbler.ngx.map.SKMapSettings.SKMapFollowerMode;
import com.skobbler.ngx.map.SKMapSurfaceListener;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKMapViewHolder;
import com.skobbler.ngx.map.SKPOICluster;
import com.skobbler.ngx.map.SKPolyline;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.util.SKLogging;

/**
 * 
 *
 */
public final class RouteActivity extends FragmentActivity implements SKMapSurfaceListener{
	
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
//    private RoutePathOverlay[] routePathOverlays = new RoutePathOverlay[3];
//    private POIOverlay[] routeOriginOverlays = new POIOverlay[3];
//    private POIOverlay[] routeDestOverlays = new POIOverlay[3];
    
    
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
    
//    private MapView mapView;
    
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
    
    private Map<Integer, Incident> idIncidentMap = new HashMap<Integer, Incident>();
    
    private String versionNumber = "";
    
    private ImageView hovButton;
    
    private Queue<Runnable> mapActionQueue = new LinkedList<Runnable>();
    
    private Runnable goBackToWhereTo = new Runnable() {
        @Override
        public void run() {
            if(!isFinishing()){
            	Intent i = new Intent(RouteActivity.this, LandingActivity2.class);
            	startActivity(i);
                finish();
            }
        }
    };
    @Override
    public void finish() {
    	if (mapView!=null) {
    		mapView.clearAllOverlays();
    		mapView.deleteAllAnnotationsAndCustomPOIs();
    	}
		super.finish();
    }
    
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
			    RouteTask routeTask = new RouteTask(originCoord, destCoord, timeLayout.getDepartureTime(0), 0, true, versionNumber, getIncludeToll(), getHov());
	            routeTasks.add(routeTask);
	            if(mapView != null) {
	            	routeTask.execute();
	            }
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
				
            	destOverlayInfo = PoiOverlayInfo.fromCurrentLocation(destCoord);
            	destOverlayInfo.markerWithShadow = R.drawable.pin_destination1;
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
				MainActivity.initApiLinksIfNecessary(RouteActivity.this, new Runnable() {
					@Override
					public void run() {
						retriveIncident(new Runnable() {
							@Override
							public void run() {
								RouteTask routeTask = new RouteTask(originCoord, destCoord, timeLayout.getDepartureTime(0), 0, true, versionNumber, getIncludeToll(), getHov());
						        routeTasks.add(routeTask);
						        if(mapView != null) {
						        	routeTask.execute();
						        }
							}
						});
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
        // init skmap
    	SkobblerUtils.initializeLibrary(RouteActivity.this);
        setContentView(R.layout.pre_reservation_map);
        
        restrictedMode(restrictedMode);
        mapViewHolder = (SKMapViewHolder) findViewById(R.id.mapview_holder);
		mapViewHolder.hideAllAttributionTextViews();
		mapViewHolder.setMapSurfaceListener(this);
        
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
        
        try {
			versionNumber = MapDisplayActivity.OS_NAME + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		}catch(NameNotFoundException ignore) {}
        
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
        
//        mapView = (MapView) findViewById(R.id.mapview);
//        Misc.disableHardwareAcceleration(mapView);
//        mapView.setBuiltInZoomControls(false);
//        mapView.setMultiTouchControls(true);
//        mapView.setTileSource(new SmartrekTileProvider());
//        TextView osmCredit = (TextView) findViewById(R.id.osm_credit);
//        Font.setTypeface(lightFont, osmCredit);
        
        /* Set the map view for a view of North America before zooming in on route */
//        setViewToNorthAmerica(mapView);
        
        dialog = new ProgressDialog(RouteActivity.this, R.style.PopUpDialog) {
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
        scrollableTimeLayout.setTimeLayout(timeLayout);
        
        rescheduleReservId = extras.getLong(RESCHEDULE_RESERVATION_ID);
        
        ReservationTollHovInfo info = MapDisplayActivity.getReservationTollHovInfo(RouteActivity.this, rescheduleReservId);
        int hovResourceId;
        hovButton = (ImageView) findViewById(R.id.hov_button);
        if(rescheduleReservId > 0) {
        	currentHov.set(info.isHov());
        	hovResourceId = info.isHov() ? R.drawable.hov_active : R.drawable.hov_inactive;
        }
        else {
        	currentHov.set(false);
        	hovResourceId = R.drawable.hov_inactive;
        }
        hovButton.setImageBitmap(Misc.getBitmap(RouteActivity.this, hovResourceId, 1));
        
        hovButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation clickAnimation = new ClickAnimation(RouteActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						boolean newValue = !currentHov.get();
						currentHov.set(newValue);
						hovButton.setImageBitmap(Misc.getBitmap(RouteActivity.this, (newValue ? R.drawable.hov_active : R.drawable.hov_inactive), 1));
						cancelAllTask();
						timeLayout.refreshAllColumns();
						scrollableTimeLayout.notifyScrollChanged();
						v.setClickable(true);
					}
				});
			}
		});
        
        boolean includeToll = rescheduleReservId > 0 ? info.isIncludeToll() : MapDisplayActivity.isIncludeTollRoadsEnabled(RouteActivity.this);
        updateTollRoadView(includeToll);
        TextView includeTollButton = (TextView) findViewById(R.id.include_toll);
        includeTollButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if(!currentIncludeToll.get()) {
					v.setClickable(false);
					ClickAnimation clickAnimation = new ClickAnimation(RouteActivity.this, v);
					clickAnimation.startAnimation(new ClickAnimationEndCallback() {
						@Override
						public void onAnimationEnd() {
							MapDisplayActivity.setIncludeTollRoadsEnabled(RouteActivity.this, true);
							updateTollRoadView(true);
							cancelAllTask();
							timeLayout.refreshAllColumns();
							scrollableTimeLayout.notifyScrollChanged();
							v.setClickable(true);
						}
					});
				}
			}
        });
        
        TextView noTollButton = (TextView) findViewById(R.id.no_toll);
        noTollButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if(currentIncludeToll.get()) {
					v.setClickable(false);
					ClickAnimation clickAnimation = new ClickAnimation(RouteActivity.this, v);
					clickAnimation.startAnimation(new ClickAnimationEndCallback() {
						@Override
						public void onAnimationEnd() {
							MapDisplayActivity.setIncludeTollRoadsEnabled(RouteActivity.this, false);
							updateTollRoadView(false);
							cancelAllTask();
							timeLayout.refreshAllColumns();
							scrollableTimeLayout.notifyScrollChanged();
							v.setClickable(true);
						}
					});
				}
			}
        });
        
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
                    	if (isFinishing()) return;
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
                                	goBackToWhereTo.run();
                                }
                            });
                        }else if(routes != null && routes.size() > 0) {
                        	if (isFinishing()) return;
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
                                	if (isFinishing()) return;
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
            handleImComing(imComingMsg, extras.getDouble(LAT, 0), extras.getDouble(LON, 0));
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
                            
                            RouteTask routeTask = new RouteTask(originCoord, destCoord, departureTime, column, timeLayout.getSelectedColumn() == column, versionNumber, getIncludeToll(), getHov());
                            routeTasks.add(routeTask);
                            loadingTasks.put(column, routeTask);
                            routeTask.execute();
                        }
                    }else{
                        if(State.InProgress.equals(columnState) && (task = loadingTasks.remove(column)) != null){
                        	task.cancelTask(true);
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
            
            GeoPoint pOriginCoord = extras.getParcelable(ORIGIN_COORD);
            if(pOriginCoord != null){
                originCoord = pOriginCoord;
            }
            originCoordProvider = extras.getString(ORIGIN_COORD_PROVIDER);
            originCoordTime = extras.getLong(ORIGIN_COORD_TIME);
            GeoPoint pDestCoord = extras.getParcelable(DEST_COORD);
            if(pOriginCoord != null){
                destCoord = pDestCoord;
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
	                                    GeoPoint curLoc = DebugOptionsActivity.getCurrentLocationLatLon(RouteActivity.this);
	                                    if(curLoc != null){
	                                    	originCoord = curLoc;
	                                    }else{
	                                    	originCoord = new GeoPoint(location.getLatitude(), location.getLongitude(), location.getBearing());
	                                    	originCoordProvider = location.getProvider();
	                                        originCoordTime = location.getTime();
	                                    }
                                    	originOverlayInfo = PoiOverlayInfo.fromCurrentLocation(originCoord);
                                        doRouteTask(originCoord);
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
        
        rescheduleDepartureTime = extras.getLong(RESCHEDULE_DEPARTURE_TIME);
        final View reservePanelView = findViewById(R.id.reserve);
        final TextView reserveView = (TextView)findViewById(R.id.reserve);
        if(rescheduleReservId > 0) {
        	reserveView.setText("Reschedule Trip");
        	reservePanelView.setBackgroundColor(getResources().getColor(R.color.metropia_red));
        }
        reservePanelView.setOnClickListener(new View.OnClickListener() {
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
		                    	if (isFinishing()) return;
		                    	mapView.clearAllOverlays();
		                        if (ehs.hasExceptions()) {
		                            ehs.reportExceptions();
		                            scrollableTimeLayout.notifyScrollChanged();
		                            v.setClickable(true);
		                        }
		                        else {
		                        	ReservationTollHovInfo reservInfo = new ReservationTollHovInfo(result);
		                        	reservInfo.setIncludeToll(getIncludeToll());
		                        	reservInfo.setHov(getHov());
		                        	MapDisplayActivity.addReservationTollHovInfo(RouteActivity.this, reservInfo);
		                            deleteRescheduledReservation();
		                            Reservation.scheduleNotification(RouteActivity.this, result, route);
		                            
		                            if(rescheduleReservId > 0) {
		                            	LocalyticsUtils.tagReschedule();
		                            }
		                            else {
		                            	LocalyticsUtils.tagMakeAReservation(timeLayout.getSelectedColumn());
		                            }
		                            
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
     
        final View letsGoView = findViewById(R.id.lets_go);
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
							mapView.clearAllOverlays();
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
	                        final ReservationTollHovInfo reservInfo = new ReservationTollHovInfo(0L);
	                        reservInfo.setIncludeToll(getIncludeToll());
	                        reservInfo.setHov(getHov());
	                        ShortcutNavigationTask task = new ShortcutNavigationTask(RouteActivity.this, route, ehs, rescheduleReservId, versionNumber, reservInfo);
	                        task.callback = new ShortcutNavigationTask.Callback() {
	                            @Override
	                            public void run(Reservation reservation) {
	                            	reservInfo.setReservationId(reservation.getRid());
	                            	MapDisplayActivity.addReservationTollHovInfo(RouteActivity.this, reservInfo);
	                            	if(rescheduleReservId > 0) {
	                            		LocalyticsUtils.tagReschedule();
	                            	}
	                            	else {
	                            		LocalyticsUtils.tagMakeAReservation(timeLayout.getSelectedColumn());
	                            	}
	                            	
	                                if(reservation.isEligibleTrip()){
	                                	mapView.clearAllOverlays();
	                                    deleteRescheduledReservation();
	                                    Misc.suppressTripInfoPanel(RouteActivity.this);
	                                    Intent intent = new Intent(RouteActivity.this, ValidationActivity.class);
	                                    intent.putExtra("route", reservation.getRoute());
	                                    intent.putExtra("reservation", reservation);
	                                    put_cur_location_if_GPS_provider_and_less_than_1_min_old(intent);
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
        
        findViewById(R.id.tutorial).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
        });
        
        TextView skipTutorial = (TextView) findViewById(R.id.skip_tutorial);
        skipTutorial.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation click = new ClickAnimation(RouteActivity.this, v);
				click.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						findViewById(R.id.tutorial).setVisibility(View.GONE);
						SharedPreferences prefs = Preferences.getGlobalPreferences(RouteActivity.this);
		                SharedPreferences.Editor editor = prefs.edit();
		                editor.putInt(Preferences.Global.ROUTE_TUTORIAL_FINISH, TutorialActivity.TUTORIAL_FINISH);
		                editor.commit();
		                v.setClickable(true);
					}
				});
			}
        });
        
        Font.setTypeface(mediumFont, skipTutorial, durationRow, reserveView, includeTollButton, noTollButton,
        		(TextView)findViewById(R.id.lets_go),
        		(TextView)findViewById(R.id.arrive_row),
                (TextView)findViewById(R.id.mpoint_row), 
                (TextView)findViewById(R.id.departure_row));
        
        showTutorialIfNessary();
        
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
    }
    
    private AtomicBoolean currentIncludeToll = new AtomicBoolean();
    private AtomicBoolean currentHov = new AtomicBoolean();
    public static final Integer TOLL_PANEL_HEIGHT = Integer.valueOf(30);
    
    private boolean getIncludeToll() {
    	return currentIncludeToll.get();
    }
    
    private boolean getHov() {
    	return currentHov.get();
    }
    
    private void cancelAllTask() {
    	for(RouteTask task : routeTasks) {
    		task.cancelTask(true);
    	}
    	routeTasks.clear();
    }
    
    private void updateTollRoadView(boolean includeToll) {
    	currentIncludeToll.set(includeToll);
    	TextView includeTollButton = (TextView) findViewById(R.id.include_toll);
    	RelativeLayout.LayoutParams includeTollLp = (RelativeLayout.LayoutParams) includeTollButton.getLayoutParams();
    	includeTollLp.width = LayoutParams.MATCH_PARENT;
    	includeTollLp.height = includeToll ? LayoutParams.MATCH_PARENT : (Double.valueOf(0.8 * Dimension.dpToPx(TOLL_PANEL_HEIGHT, getResources().getDisplayMetrics())).intValue());
    	includeTollButton.setLayoutParams(includeTollLp);
    	includeTollButton.setBackgroundColor(getResources().getColor(includeToll ? R.color.transparent_metropia_blue : R.color.transparent_metropia_light_blue));
    	includeTollButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, includeToll ? 18 : 14);
    	
    	TextView noTollButton = (TextView) findViewById(R.id.no_toll);
    	RelativeLayout.LayoutParams noTollLp = (RelativeLayout.LayoutParams) noTollButton.getLayoutParams();
    	noTollLp.width = LayoutParams.MATCH_PARENT;
    	noTollLp.height = includeToll ? (Double.valueOf(0.8 * Dimension.dpToPx(TOLL_PANEL_HEIGHT, getResources().getDisplayMetrics())).intValue()) : LayoutParams.MATCH_PARENT;
    	noTollButton.setLayoutParams(noTollLp);
    	noTollButton.setBackgroundColor(getResources().getColor(includeToll ? R.color.transparent_metropia_light_blue : R.color.transparent_metropia_blue));
    	noTollButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, includeToll ? 14 : 18);
    }
    
	private SKMapViewHolder mapViewHolder;
	private SKMapSurfaceView mapView;
    
    private void initSKMaps(SKMapViewHolder mapViewHolder) {
		SKLogging.enableLogs(true);
		
		mapView = mapViewHolder.getMapSurfaceView();
		
		mapView.getMapSettings().setCurrentPositionShown(false);
		mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.NONE);
		mapView.getMapSettings().setMapDisplayMode(SKMapDisplayMode.MODE_2D);
		mapView.getMapSettings().setMapRotationEnabled(false);
        mapView.getMapSettings().setMapZoomingEnabled(true);
        mapView.getMapSettings().setMapPanningEnabled(true);
        mapView.getMapSettings().setZoomWithAnchorEnabled(true);
        mapView.getMapSettings().setInertiaRotatingEnabled(false);
        mapView.getMapSettings().setInertiaZoomingEnabled(true);
        mapView.getMapSettings().setInertiaPanningEnabled(true);
        mapView.getMapSettings().setMapStyle(SkobblerUtils.getMapViewStyle(RouteActivity.this, true));
	}
    
    private String incidentUrl;
    
    private void retriveIncident(final Runnable callback) {
    	if(callback != null) {
    		callback.run();
    	}
    	if(DebugOptionsActivity.isIncidentEnabled(RouteActivity.this)) {
	    	final AsyncTask<Void, Void, Void> getIncidentTask = new AsyncTask<Void, Void, Void>() {
	    		@Override
				protected Void doInBackground(Void... params) {
	    			if(originCoord != null) {
	    				CityRequest cityReq = new CityRequest(originCoord.getLatitude(), originCoord.getLongitude(), 10000); // timeout 10 secs.
		                try {
							City city = cityReq.execute(RouteActivity.this);
							if(city != null && StringUtils.isBlank(city.html)) {
								incidentUrl = city.incidents;
							}
						} catch (Exception ignore) {}
		    			refreshIncident();
		    		}
	    			return null;
				}
	    		
				@Override
		        protected void onPostExecute(Void result) {
					if (isFinishing()) return;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if(timeLayout.getSelectedTimeButton() != null) {
								showIncidentOverlays(timeLayout.getSelectedDepartureTime());
							}
						}
					});
				}
	    	};
	    	
	    	if(mapView != null) {
	    		Misc.parallelExecute(getIncidentTask);
	    	}
	    	else {
	    		mapActionQueue.add(new Runnable() {
					@Override
					public void run() {
						Misc.parallelExecute(getIncidentTask);
					}
	    		});
	    	}
    	}
    }
    
    private Object mutex = new Object();
    
    private void refreshIncident() {
        if(StringUtils.isNotBlank(incidentUrl)) {
            IncidentRequest incidentReq = new IncidentRequest(User.getCurrentUser(RouteActivity.this), incidentUrl, 10000); // timeout 10 secs.
            incidentReq.invalidateCache(RouteActivity.this);
            try {
                List<Incident> allIncident = incidentReq.execute(RouteActivity.this);
                synchronized(mutex) {
                    idIncidentMap.clear();
                     for(Incident inc : allIncident) {
                         idIncidentMap.put(SkobblerUtils.getUniqueId(inc.lat, inc.lon), inc);
                     }
                }
            } catch (Exception ignore) {
                Log.d("RouteActivity", Log.getStackTraceString(ignore));
            }
        }
    }
    
    private void showIncidentOverlays(long depTimeInMillis) {
	    removeIncidentOverlays();
	    sizeRatio.set(0); // init for zoom action
	    List<Incident> incidentOfDepTime = getIncidentOfDepartureTime(depTimeInMillis);
    	for(Incident incident : incidentOfDepTime) {
    		SKAnnotation incAnn = new SKAnnotation(SkobblerUtils.getUniqueId(incident.lat, incident.lon));
//    		incAnn.setUniqueID(SkobblerUtils.getUniqueId(incident.lat, incident.lon));
			incAnn.setLocation(new SKCoordinate(incident.lon, incident.lat));
			incAnn.setMininumZoomLevel(incident.getMinimalDisplayZoomLevel());
			SKAnnotationView iconView = new SKAnnotationView();
			SkobblerImageView incImage = new SkobblerImageView(RouteActivity.this, IncidentIcon.fromType(incident.type).getResourceId(RouteActivity.this), getSizeRatioByZoomLevel());
			incImage.setLat(incident.lat);
			incImage.setLon(incident.lon);
			incImage.setMinimumHeight(annSize.get() / getSizeRatioByZoomLevel());
			incImage.setMinimumWidth(annSize.get() / getSizeRatioByZoomLevel());
			incImage.setImageBitmap(Misc.getBitmap(RouteActivity.this, IncidentIcon.fromType(incident.type).getResourceId(RouteActivity.this), getSizeRatioByZoomLevel()));
			iconView.setView(incImage);
			incAnn.setAnnotationView(iconView);
			mapView.addAnnotation(incAnn, SKAnimationSettings.ANIMATION_NONE);
    	}
    }
    
    private static final Integer FROM_BALLOON_ID = Integer.valueOf(2456);
    private static final Integer FROM_DETAIL_BALLOON_ID = Integer.valueOf(2455);
    private static final Integer TO_BALLOON_ID = Integer.valueOf(1357);
    private static final Integer TO_DETAIL_BALLOON_ID = Integer.valueOf(1356);
    private static final Integer FROM_OVERLAY_ID = Integer.valueOf(2457);
    private static final Integer TO_OVERLAY_ID = Integer.valueOf(1368);
    
    private void removeIncidentOverlays() {
    	synchronized(mutex) {
    		for(Integer incId : idIncidentMap.keySet()) {
        		mapView.deleteAnnotation(incId);
        	}
    	}
    }
    
    private AtomicBoolean OD_DRAWED = new AtomicBoolean(false);
    private SKAnnotation toBalloonAnn;
    private SKAnnotation fromBalloonAnn;
    private SKAnnotation toDetailBalloonAnn;
    private SKAnnotation fromDetailBalloonAnn; 
    private RouteNode routeFirstNode;
    private RouteNode routeLastNode;
    
    private void drawODOverlayAndBalloon(Route _route) {
    	if(!OD_DRAWED.getAndSet(true)) {
	    	
	    	int destResourceId = destOverlayInfo != null ? (destOverlayInfo.markerWithShadow == R.drawable.poi_pin_with_shadow ? R.drawable.pin_destination1 : destOverlayInfo.markerWithShadow) : R.drawable.pin_destination1;
	    	boolean isFlag = destResourceId == R.drawable.pin_destination1;

	    	routeFirstNode = _route.getFirstNode();
	    	routeLastNode = _route.getLastNode();
	    	odSizeRatio.set(0);
	    	drawODAnnotation(2);
	    	
	    	double originLat = (originOverlayInfo != null && originOverlayInfo.geopoint != null) ? originOverlayInfo.geopoint.getLatitude() : routeFirstNode.getLatitude();
	    	double originLon = (originOverlayInfo != null && originOverlayInfo.geopoint != null) ? originOverlayInfo.geopoint.getLongitude() : routeFirstNode.getLongitude();
	    	double destLat = (destOverlayInfo != null && destOverlayInfo.geopoint != null) ? destOverlayInfo.geopoint.getLatitude() : routeLastNode.getLatitude();
	    	double destLon = (destOverlayInfo != null && destOverlayInfo.geopoint != null) ? destOverlayInfo.geopoint.getLongitude() : routeLastNode.getLongitude();
	    	
	    	toBalloonAnn = new SKAnnotation(TO_BALLOON_ID);
	    	toBalloonAnn.setUniqueID(TO_BALLOON_ID);
	    	
	    	toBalloonAnn.setLocation(new SKCoordinate(destLon, destLat));
	    	int toOffset = isFlag ? 38 : 34;
	    	toBalloonAnn.setOffset(new SKScreenPoint(0, Dimension.dpToPx(toOffset, getResources().getDisplayMetrics())));
	    	SKAnnotationView toBalloonView = new SKAnnotationView();
	        SkobblerImageView toBalloonImage = new SkobblerImageView(RouteActivity.this, 0, 0);
	        toBalloonImage.setLat(destLat);
	        toBalloonImage.setLon(destLon);
	        toBalloonImage.setDesc("TO");
	        toBalloonImage.setImageBitmap(loadBitmapOfFromToBalloon(RouteActivity.this, false));
	        toBalloonView.setView(toBalloonImage);
	        toBalloonAnn.setAnnotationView(toBalloonView);
	        mapView.addAnnotation(toBalloonAnn, SKAnimationSettings.ANIMATION_POP_OUT);
	        
	        toDetailBalloonAnn = new SKAnnotation(TO_DETAIL_BALLOON_ID);
	        toDetailBalloonAnn.setUniqueID(TO_DETAIL_BALLOON_ID);
	        toDetailBalloonAnn.setLocation(new SKCoordinate(destLon, destLat));
	        toDetailBalloonAnn.setOffset(new SKScreenPoint(0, Dimension.dpToPx(toOffset, getResources().getDisplayMetrics())));
	    	SKAnnotationView toDetailBalloonView = new SKAnnotationView();
	    	SkobblerImageView toDetailBalloonImage = new SkobblerImageView(RouteActivity.this, 0, 0);
	    	toDetailBalloonImage.setLat(destLat);
	    	toDetailBalloonImage.setLon(destLon);
	    	toDetailBalloonImage.setDesc(destAddr);
	        toDetailBalloonImage.setImageBitmap(loadBitmapOfFromToDetailBalloon(RouteActivity.this, destAddr, false));
	        toDetailBalloonView.setView(toDetailBalloonImage);
	        toDetailBalloonAnn.setAnnotationView(toDetailBalloonView);
	        
	        fromBalloonAnn = new SKAnnotation(FROM_BALLOON_ID);
	        fromBalloonAnn.setUniqueID(FROM_BALLOON_ID);
	        fromBalloonAnn.setLocation(new SKCoordinate(originLon, originLat));
	        fromBalloonAnn.setOffset(new SKScreenPoint(0, Dimension.dpToPx(28, getResources().getDisplayMetrics())));
	    	SKAnnotationView fromBalloonView = new SKAnnotationView();
	    	SkobblerImageView balloon = new SkobblerImageView(RouteActivity.this, 0, 0);
	    	balloon.setLat(originLat);
	    	balloon.setLon(originLon);
	    	balloon.setDesc("FROM");
	        balloon.setImageBitmap(loadBitmapOfFromToBalloon(RouteActivity.this, true));
	        fromBalloonView.setView(balloon);
	        fromBalloonAnn.setAnnotationView(fromBalloonView);
	        mapView.addAnnotation(fromBalloonAnn, SKAnimationSettings.ANIMATION_POP_OUT);
	        
	        fromDetailBalloonAnn = new SKAnnotation(FROM_DETAIL_BALLOON_ID);
	        fromDetailBalloonAnn.setUniqueID(FROM_DETAIL_BALLOON_ID);
	        fromDetailBalloonAnn.setLocation(new SKCoordinate(originLon, originLat));
	        fromDetailBalloonAnn.setOffset(new SKScreenPoint(0, Dimension.dpToPx(36, getResources().getDisplayMetrics())));
	    	SKAnnotationView fromDetailBalloonView = new SKAnnotationView();
	    	SkobblerImageView detailBalloon = new SkobblerImageView(RouteActivity.this, 0, 0);
	    	detailBalloon.setLat(originLat);
	    	detailBalloon.setLon(originLon);
	    	detailBalloon.setDesc(originAddr);
	        detailBalloon.setImageBitmap(loadBitmapOfFromToDetailBalloon(RouteActivity.this, originAddr, true));
	        fromDetailBalloonView.setView(detailBalloon);
	        fromDetailBalloonAnn.setAnnotationView(fromDetailBalloonView);
    	}
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
    
    public static void setViewToNorthAmerica(SKMapSurfaceView mapView){
        mapView.setZoom(3);
        mapView.centerMapOnPosition(new SKCoordinate(-99.1406250000000f*1E6, 38.27268853598097f*1E6));
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
        findViewById(R.id.lets_go).setVisibility(View.GONE);
        
        Runnable r = new Runnable() {
			public void run() {
				mapView.deleteAllAnnotationsAndCustomPOIs();
		        drawDestinationAnnotation(lat, lon);
		        mapView.setZoom(ValidationActivity.DEFAULT_ZOOM_LEVEL);
		        mapView.centerMapOnPosition(new SKCoordinate(lon, lat));
			}
    	};
        if(mapView == null) mapActionQueue.add(r);
        else r.run();
    }
    
    private static final Integer DEST_ANNOTATION_ID = Integer.valueOf(1010);
    
    private void drawDestinationAnnotation(double lat, double lon) {
    	SKAnnotation destAnn = new SKAnnotation(DEST_ANNOTATION_ID);
		destAnn.setUniqueID(DEST_ANNOTATION_ID);
		destAnn.setLocation(new SKCoordinate(lon, lat));
		destAnn.setMininumZoomLevel(5);
		SKAnnotationView destAnnView = new SKAnnotationView();
        SkobblerImageView destImage = new SkobblerImageView(RouteActivity.this, R.drawable.pin_destination1, 2);
        destImage.setLat(lat);
        destImage.setLon(lon);
        destImage.setImageBitmap(Misc.getBitmap(RouteActivity.this, R.drawable.pin_destination1, 2));
        destAnnView.setView(destImage);
        destAnn.setAnnotationView(destAnnView);
        //destAnn.setOffset(new SKScreenPoint(0, Dimension.dpToPx(20, getResources().getDisplayMetrics())));
        
        destAnn.setOffset(new SKScreenPoint(Dimension.dpToPx(-8, getResources().getDisplayMetrics()), Dimension.dpToPx(10, getResources().getDisplayMetrics())));

		mapView.addAnnotation(destAnn, SKAnimationSettings.ANIMATION_NONE);
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
        	MainActivity.initApiLinksIfNecessary(RouteActivity.this, new Runnable() {
    			@Override
    			public void run() {
		        	retriveIncident(new Runnable() {
						@Override
						public void run() {
							RouteTask routeTask = new RouteTask(originCoord, destCoord, timeLayout.getDepartureTime(0), 0, true, versionNumber, getIncludeToll(), getHov());
				            routeTasks.add(routeTask);
				            if(mapView != null) {
				            	routeTask.execute();
				            }
						}
		        	});
    			}
        	});
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Localytics.openSession();
        Localytics.tagScreen(this.getClass().getSimpleName());
	    Localytics.upload();
	    Localytics.setInAppMessageDisplayActivity(this);
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
	    
	    annSize.set(Dimension.dpToPx(Misc.ANNOTATION_MINIMUM_SIZE_IN_DP, getResources().getDisplayMetrics()));
	    
	    mapViewHolder.onResume();
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
	    //if (mapView!=null) mapView.deleteAllAnnotationsAndCustomPOIs();
	    super.onPause();
	    mapViewHolder.onPause();
    } 
    
    @Override
    public void onBackPressed() {
        goBackToWhereTo.run();
    }
    
    private RouteRect routeRect;
    
    private void fitRouteToMap(boolean zoomToSpan){
        if(routeRect != null && zoomToSpan){
//            GeoPoint topLeft = routeRect.getTopLeftPoint();
//            GeoPoint bottomRight = routeRect.getBottomRightPoint();
//			SKBoundingBox boundingBox = new SKBoundingBox(topLeft.getLatitude(), topLeft.getLongitude(), bottomRight.getLatitude(), bottomRight.getLongitude());
//			mapView.fitBoundingBox(boundingBox, 100, 100);
//        	mapView.setZoom(SKMapSurfaceView.DEFAULT_ZOOM_LEVEL);
        	int[] range = routeRect.getRange();
			float newZoomLevel = getSpanZoomLevel(range[0], range[1]);
			SKCoordinateRegion region = new SKCoordinateRegion();
			region.setCenter(new SKCoordinate(routeRect.getMidPoint().getLongitude(), routeRect.getMidPoint().getLatitude()));
			region.setZoomLevel(newZoomLevel);
			mapView.changeMapVisibleRegion(region, false);
        }
    }
    
    private float getSpanZoomLevel(int reqLatSpan, int reqLonSpan) {
		if (reqLatSpan <= 0 || reqLonSpan <= 0) {
			return mapView.getZoomLevel();
		}

		// zoom level : 14.9101 , BoundingBox topLat:24.968921851853025, bottomLat:24.96617906512892, topLon:121.53976321220398, bottomLon:121.5416944026947
		float curZoomLevel = 14.9101f;
		int curLatSpan = Math.abs((int)(24.968921851853025 * 1E6) - (int)(24.96617906512892 * 1E6));
		int curLonSpan = Math.abs((int)(121.53976321220398 * 1E6) - (int)(121.5416944026947 * 1E6));

		float diffNeededLat = (float) reqLatSpan / curLatSpan; // i.e. 600/500 = 1,2
		float diffNeededLon = (float) reqLonSpan / curLonSpan; // i.e. 300/400 = 0,75

		float diffNeeded = Math.max(diffNeededLat, diffNeededLon); // i.e. 1,2

		if (diffNeeded > 1) { // Zoom Out
			return curZoomLevel - Misc.getNextSquareNumberAbove(diffNeeded) + 2.7f;
		} else if (diffNeeded < 0.5) { // Can Zoom in
			return curZoomLevel + Misc.getNextSquareNumberAbove(1 / diffNeeded) - 1 ;
		}
		return curZoomLevel;
	}
    
    private static final double mapZoomVerticalOffset = -0.3;
    
    /**
     * This function will be called when BackgroundDownloadTask().execute()
     * succeeds.
     * 
     * @param possibleRoutes
     */
    private void updateMap(List<Route> possibleRoutes, boolean zoomToSpan) {
    	if (isFinishing()) return;
        if(possibleRoutes != null && possibleRoutes.size() > 0) {
            
            List<RouteNode> nodes = new ArrayList<RouteNode>(); 
            
            /* Iterate through the routes to draw each to the screen */
            for (int i = 0; i < possibleRoutes.size(); i++) {
                Route route = possibleRoutes.get(i);
                /* Draw the route to the screen and hold on to the range */
                drawRoute(mapView, route, i);
                drawODOverlayAndBalloon(route);
                nodes.addAll(route.getNodes());
            }
            
            routeRect = new RouteRect(nodes, mapZoomVerticalOffset);
            
            fitRouteToMap(zoomToSpan);
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
            origin, destination, departureTime, speed, course, getOriginAddrRouteReqParam(), destAddr, 
            getIncludeToll(), versionNumber, getHov());
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
//        	AsyncTask<Void, Void, List<Route>> fetchRoute = new AsyncTask<Void, Void, List<Route>>() {
//				@Override
//				protected List<Route> doInBackground(Void... params) {
//					try {
//						return request.execute(RouteActivity.this);
//					} catch (Exception e) {
//						ehs.registerException(e);
//					} 
//					return null;
//				}
//				
//				@Override
//				protected void onPostExecute(List<Route> routes) {
//					if(ehs.hasExceptions()) {
//						ehs.reportExceptions();
//					}
//					else {
//			        	updateMap(routes, false);
//			        	timeLayout.setColumnState(column, TimeButton.State.Selected);
//					}
//				}
//        		
//        	};
//        	Misc.parallelExecute(fetchRoute);
            for (RouteTask task : routeTasks) {
                task.cancelTask(true);
            }
            timeLayout.refresh();
            RouteTask routeTask = new RouteTask(originCoord, destCoord, timeLayout.getDepartureTime(0), 0, true, versionNumber, getIncludeToll(), getHov());
            routeTasks.add(routeTask);
            routeTask.execute();
            letsGoPanelVis = View.VISIBLE;
            reservePanelVis = View.GONE;
        }
        
        findViewById(R.id.lets_go).setVisibility(letsGoPanelVis);
        findViewById(R.id.reserve).setVisibility(reservePanelVis);
    }

//    private List<Overlay> mapOverlays;

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
    public void drawRoute (SKMapSurfaceView mapView, Route _route, int routeNum) {
    	mapView.clearAllOverlays();
    	List<SKCoordinate> routeCoors = new ArrayList<SKCoordinate>();
		for(RouteNode node : _route.getNodes()) {
			routeCoors.add(new SKCoordinate(node.getLongitude(), node.getLatitude()));
		}
		
		SKPolyline routeLine = new SKPolyline();
		routeLine.setNodes(routeCoors);
		routeLine.setColor(SkobblerUtils.getRouteColorArray(_route.getColor())); //RGBA
		routeLine.setLineSize(10);
		
		//outline properties, otherwise map crash
		routeLine.setOutlineColor(SkobblerUtils.getRouteColorArray(_route.getColor()));
		routeLine.setOutlineSize(10);
		routeLine.setOutlineDottedPixelsSolid(0);
		routeLine.setOutlineDottedPixelsSkip(0);
		//
		mapView.addPolyline(routeLine);
		
        /* Set values into route to be passed to next Activity */
        _route.setAddresses(originAddr, destAddr);
        
        // FIXME:
        _route.setUserId(User.getCurrentUser(this).getId());
        findViewById(R.id.reserve).setTag(_route);
    }
    
    private AtomicInteger sizeRatio = new AtomicInteger(0);
    
    private void updateIncidentAnnotationSize(int ratio, long departureTime) {
    	synchronized(mutex) {
	    	if(DebugOptionsActivity.isIncidentEnabled(RouteActivity.this) && sizeRatio.get() != ratio && departureTime > 0) {
	    		sizeRatio.set(ratio);
		    	Set<Integer> incidentIds = idIncidentMap.keySet();
		    	for(Integer uniqueId : incidentIds) {
		    		mapView.deleteAnnotation(uniqueId);
		    	}
		    	List<Incident> incidentOfDepTime = new ArrayList<Incident>();
		    	if(idIncidentMap.size() > 0) {
		    		for(Incident incident : idIncidentMap.values()) {
		    			if(incident.severity > 0 && incident.isInTimeRange(departureTime)) {
		    				incidentOfDepTime.add(incident);
		    			}
		    		}
		    	}
		    	for(Incident incident : incidentOfDepTime) {
		    		SKAnnotation incAnn = new SKAnnotation(SkobblerUtils.getUniqueId(incident.lat, incident.lon));
//		    		incAnn.setUniqueID();
					incAnn.setLocation(new SKCoordinate(incident.lon, incident.lat));
					incAnn.setMininumZoomLevel(incident.getMinimalDisplayZoomLevel());
					SKAnnotationView iconView = new SKAnnotationView();
					SkobblerImageView incImage = new SkobblerImageView(RouteActivity.this, IncidentIcon.fromType(incident.type).getResourceId(RouteActivity.this), ratio);
					incImage.setLat(incident.lat);
					incImage.setLon(incident.lon);
					incImage.setMinimumHeight(annSize.get() / ratio);
					incImage.setMinimumWidth(annSize.get() / ratio);
					incImage.setImageBitmap(Misc.getBitmap(RouteActivity.this, IncidentIcon.fromType(incident.type).getResourceId(RouteActivity.this), ratio));
					iconView.setView(incImage);
					incAnn.setAnnotationView(iconView);
					mapView.addAnnotation(incAnn, SKAnimationSettings.ANIMATION_NONE);
		    	}
	    	}
    	}
    }
    
    private AtomicInteger odSizeRatio = new AtomicInteger(0);
    private AtomicInteger annSize = new AtomicInteger(0);
    private SKAnnotation fromOverlay;
    private SKAnnotation toOverlay;
    
    private void drawODAnnotation(int ratio) {
    	if(odSizeRatio.get() != ratio) {
    		odSizeRatio.set(ratio);
	    	if(routeFirstNode != null && originOverlayInfo != null) {
		    	fromOverlay = new SKAnnotation(FROM_OVERLAY_ID);
		    	fromOverlay.setUniqueID(FROM_OVERLAY_ID);
		    	fromOverlay.setMininumZoomLevel(MINIAL_ZOOM_LEVEL + 1);
		    	double originLat = originOverlayInfo.geopoint != null ? originOverlayInfo.geopoint.getLatitude() : routeFirstNode.getLatitude();
		    	double originLon = originOverlayInfo.geopoint != null ? originOverlayInfo.geopoint.getLongitude() : routeFirstNode.getLongitude();
		    	fromOverlay.setLocation(new SKCoordinate(originLon, originLat));
		    	SKAnnotationView fromOverlayView = new SKAnnotationView();
		    	SkobblerImageView fromOverlayImageView = new SkobblerImageView(RouteActivity.this, originOverlayInfo.markerWithShadow, ratio);
		    	fromOverlayImageView.setLat(originLat);
		    	fromOverlayImageView.setLon(originLon);
		    	fromOverlayImageView.setMinimumHeight(annSize.get() / ratio);
		    	fromOverlayImageView.setMinimumWidth(annSize.get() / ratio);
		    	fromOverlayImageView.setImageBitmap(Misc.getBitmap(RouteActivity.this, originOverlayInfo.markerWithShadow, ratio));
		    	fromOverlayView.setView(fromOverlayImageView);
		    	fromOverlay.setAnnotationView(fromOverlayView);
		    	mapView.addAnnotation(fromOverlay, SKAnimationSettings.ANIMATION_POP_OUT);
	    	}
	    	
	    	if(routeLastNode != null && destOverlayInfo != null) {
	    		toOverlay = new SKAnnotation(TO_OVERLAY_ID);
	    		toOverlay.setUniqueID(TO_OVERLAY_ID);
	    		toOverlay.setMininumZoomLevel(MINIAL_ZOOM_LEVEL + 1);
	    		double destLat = destOverlayInfo.geopoint != null ? destOverlayInfo.geopoint.getLatitude() : routeLastNode.getLatitude();
		    	double destLon = destOverlayInfo.geopoint != null ? destOverlayInfo.geopoint.getLongitude() : routeLastNode.getLongitude();
	    		toOverlay.setLocation(new SKCoordinate(destLon, destLat));
	    		final SKAnnotationView toOverlayView = new SKAnnotationView();
	    		int destResourceId = destOverlayInfo != null ? (destOverlayInfo.markerWithShadow == R.drawable.poi_pin_with_shadow ? R.drawable.pin_destination1 : destOverlayInfo.markerWithShadow) : R.drawable.pin_destination1;
	    		final SkobblerImageView toOverlayImageView = new SkobblerImageView(RouteActivity.this, destResourceId, ratio);
	    		toOverlayImageView.setLat(destLat);
	    		toOverlayImageView.setLon(destLon);
	    		boolean isFlag = destResourceId == R.drawable.pin_destination1;
	    		toOverlayImageView.setMinimumHeight(annSize.get() / ratio);
	    		toOverlayImageView.setMinimumWidth(annSize.get() / ratio);
	    		
	    		if (destOverlayInfo.markerWithShadow==R.drawable.transparent_poi && destOverlayInfo.markerURL!=null) {
	    			new ImageLoader(RouteActivity.this, destOverlayInfo.markerURL, new ICallback() {
	    				@Override
	    				public void run(Object... obj) {
	    					if (obj[0]==null) return;
	    					Drawable drawable = (Drawable) obj[0];
	    					
	    					destOverlayInfo.drawable = drawable;
	    					toOverlayImageView.setImageDrawable(drawable);
	    					toOverlayView.setView(toOverlayImageView);
	    					toOverlay.setAnnotationView(toOverlayView);
	    			   		mapView.addAnnotation(toOverlay, SKAnimationSettings.ANIMATION_NONE);
	    				}
	    			}).execute(true);
	    		}
	    		else {
	    			toOverlayImageView.setImageBitmap(Misc.getBitmap(RouteActivity.this, destResourceId, ratio));
	    			toOverlayView.setView(toOverlayImageView);
	    			toOverlay.setAnnotationView(toOverlayView);
	    			mapView.addAnnotation(toOverlay, SKAnimationSettings.ANIMATION_POP_OUT);
	    		}
	    	}
    	}
    }
    
    private static final int MINIAL_ZOOM_LEVEL = 5;
    
    private int getSizeRatioByZoomLevel() {
    	float zoomLevel = mapView.getZoomLevel();
    	if(zoomLevel >= 13) {
    		return 1;
    	}
    	else if(zoomLevel >= MINIAL_ZOOM_LEVEL){
    		return 2;
    	}
    	return 1;
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
            goBackToWhereTo.run();
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
                final ReservationTollHovInfo reservInfo = new ReservationTollHovInfo(0L);
                reservInfo.setIncludeToll(getIncludeToll());
                reservInfo.setHov(getHov());
                ShortcutNavigationTask task = new ShortcutNavigationTask(RouteActivity.this, route, ehs, rescheduleReservId, versionNumber, reservInfo);
                task.callback = new ShortcutNavigationTask.Callback() {
                    @Override
                    public void run(Reservation reservation) {
                    	reservInfo.setReservationId(reservation.getRid());
                    	MapDisplayActivity.addReservationTollHovInfo(RouteActivity.this, reservInfo);
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
//        SKMaps.getInstance().destroySKMaps();
        for(GeocodingTask t:geocodingTasks){
            t.cancel(true);
        }
        for (RouteTask task : routeTasks) {
            task.cancelTask(true);
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
        
        private String versionNumber;
        private boolean includeToll;
        private boolean hov;
        
        public RouteTask(GeoPoint origin, GeoPoint destination, long departureTime, int column, boolean updateMap, String versionNumber, boolean includeToll, boolean hov) {
        	super();
        	
        	this.origin = origin;
        	this.destination = destination;
        	this.departureTime = departureTime;
        	this.selectedColumn = column;
        	this.updateMap = updateMap;
        	this.versionNumber = versionNumber;
        	this.includeToll = includeToll;
        	this.hov = hov;
        }
        
        public boolean isCached() {
        	RouteFetchRequest request = new RouteFetchRequest(User.getCurrentUser(RouteActivity.this), 
    	        origin, destination, departureTime, speed, course, getOriginAddrRouteReqParam(), destAddr, 
    	        includeToll, versionNumber, hov);
        	return request.isCached(RouteActivity.this);
        }
        
        public List<Route> getData() throws RouteNotFoundException, IOException, JSONException, InterruptedException {
        	RouteFetchRequest request = new RouteFetchRequest(User.getCurrentUser(RouteActivity.this), 
    	        origin, destination, departureTime, speed, course, getOriginAddrRouteReqParam(), destAddr, 
    	        includeToll, versionNumber, hov);
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
            	if(selectedColumn == 0) { 
            		ehs.registerException(e, e.getMessage());
            	}
            }
            
            return routes;
        }
        
        /**
         * Dialogs must be handled in onPostExecute() because they have to
         * reside in the main loop.
         */
        @Override
        protected void onPostExecute(List<Route> routes) {
        	if (isFinishing()) return;
            if (dialog.isShowing()) {
                Misc.doQuietly(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }
            
            setHighlightedRoutePathOverlays(true);
            
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
            		RouteActivity.this.findViewById(R.id.lets_go).setEnabled(true);
                    scrollableTimeLayout.notifyScrollChanged();
                    scrollableTimeLayout.updateVisibleColumns();
                    scrollToRescheduleDepartureTime();
                }
                
                timeLayout.notifySelectColumn(selectedColumn);
            }
        }
        
        public boolean cancelTask(boolean mayInterruptIfRunning) {
        	try {
        		return this.cancel(mayInterruptIfRunning);
        	}catch(Throwable ignore) {}
        	return false;
        }
    }
    
    private void scrollToRescheduleDepartureTime() {
    	if(rescheduleReservId > 0) {
    		scrollableTimeLayout.scrollToDepartureTime(rescheduleDepartureTime);
    	}
    }
    
    private List<Incident> getIncidentOfDepartureTime(long departureUTCTime) {
    	synchronized(mutex) {
	    	List<Incident> incidentOfDepTime = new ArrayList<Incident>();
	    	if(idIncidentMap.size() > 0) {
	    		for(Incident incident : idIncidentMap.values()) {
	    			if(incident.severity > 0 && incident.isInTimeRange(departureUTCTime)) {
	    				incidentOfDepTime.add(incident);
	    			}
	    		}
	    	}
	    	return incidentOfDepTime;
    	}
    }
    
    private void showTutorialIfNessary() {
    	SharedPreferences prefs = Preferences.getGlobalPreferences(this);
    	int routeTutorialFinish = prefs.getInt(Preferences.Global.ROUTE_TUTORIAL_FINISH, 0);
    	// hide tutorial page
    	if(false && routeTutorialFinish != TutorialActivity.TUTORIAL_FINISH) {
    		findViewById(R.id.tutorial).setVisibility(View.VISIBLE);
    	}
    }
    
    private Incident getIncident(Integer uniqueId) {
    	synchronized(mutex) {
    		return idIncidentMap.get(uniqueId);
    	}
    }

	@Override
	public void onActionPan() {}

	@Override
	public void onActionZoom() {}
	
	private static final Integer INCIDENT_BALLOON_ID = Integer.valueOf(1234);
	private View incidentBalloon;
	private SKAnnotation incBalloonAnn;

	@Override
	public void onAnnotationSelected(SKAnnotation annotation) {
		int selectedAnnotationId = annotation.getUniqueID();
		Incident selectedInc = getIncident(selectedAnnotationId);
		if(selectedInc != null && mapView.getZoomLevel() >= selectedInc.getMinimalDisplayZoomLevel()) {
			DisplayMetrics dm = getResources().getDisplayMetrics();
            incBalloonAnn = new SKAnnotation(INCIDENT_BALLOON_ID);
            incBalloonAnn.setUniqueID(INCIDENT_BALLOON_ID);
            SKAnnotationView incBalloonView = new SKAnnotationView();
            incBalloonAnn.setLocation(annotation.getLocation());
            incBalloonAnn.setOffset(new SKScreenPoint(0, Dimension.dpToPx(60, dm)));
            SkobblerImageView balloon = new SkobblerImageView(RouteActivity.this, 0, 0);
            balloon.setLat(annotation.getLocation().getLatitude());
            balloon.setLon(annotation.getLocation().getLongitude());
            balloon.setDesc(selectedInc.shortDesc);
            balloon.setImageBitmap(loadBitmapFromView(RouteActivity.this, selectedInc));
            incBalloonView.setView(balloon);
            incBalloonAnn.setAnnotationView(incBalloonView);
            mapView.addAnnotation(incBalloonAnn, SKAnimationSettings.ANIMATION_POP_OUT);
            mapView.centerMapOnPositionSmooth(annotation.getLocation(), 500);
		}
		else if(selectedAnnotationId == FROM_BALLOON_ID) {
			mapView.deleteAnnotation(FROM_BALLOON_ID);
			mapView.addAnnotation(fromDetailBalloonAnn, SKAnimationSettings.ANIMATION_POP_OUT);
		}
		else if(selectedAnnotationId == FROM_DETAIL_BALLOON_ID) {
			mapView.deleteAnnotation(FROM_DETAIL_BALLOON_ID);
			mapView.addAnnotation(fromBalloonAnn, SKAnimationSettings.ANIMATION_POP_OUT);
		}
		else if(selectedAnnotationId == TO_BALLOON_ID) {
			mapView.deleteAnnotation(TO_BALLOON_ID);
			mapView.addAnnotation(toDetailBalloonAnn, SKAnimationSettings.ANIMATION_POP_OUT);
		}
		else if(selectedAnnotationId == TO_DETAIL_BALLOON_ID) {
			mapView.deleteAnnotation(TO_DETAIL_BALLOON_ID);
			mapView.addAnnotation(toBalloonAnn, SKAnimationSettings.ANIMATION_POP_OUT);
		}
	}
	
	public Bitmap loadBitmapFromView(Context ctx, Incident selectedInc) {
		if(incidentBalloon == null) {
			FrameLayout layout = new FrameLayout(ctx);
			ViewGroup.LayoutParams layoutLp = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(layoutLp);
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			incidentBalloon = inflater.inflate(R.layout.incident_balloon, layout);
		}
        TextView textView = (TextView) incidentBalloon.findViewById(R.id.text);
        textView.setText(selectedInc.shortDesc);
        Font.setTypeface(Font.getRegular(ctx.getAssets()), textView);
        incidentBalloon.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        incidentBalloon.layout(0, 0, incidentBalloon.getMeasuredWidth(), incidentBalloon.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(incidentBalloon.getMeasuredWidth(), incidentBalloon.getMeasuredHeight(), Bitmap.Config.ARGB_8888);                
        Canvas canvas = new Canvas(bitmap);
        incidentBalloon.draw(canvas);
        return bitmap;
	}
	
	private View fromBalloon;
	private View toBalloon;
	
	private Bitmap loadBitmapOfFromToBalloon(Context ctx, boolean from) {
		if(from && fromBalloon == null) {
			FrameLayout layout = new FrameLayout(ctx);
			ViewGroup.LayoutParams layoutLp = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(layoutLp);
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			fromBalloon = inflater.inflate(R.layout.from_to_balloon, layout);
			fromBalloon.findViewById(R.id.poi_content_mini).setBackgroundResource(R.drawable.from_to_pin);
		}
		else if(!from && toBalloon == null) {
			FrameLayout layout = new FrameLayout(ctx);
			ViewGroup.LayoutParams layoutLp = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(layoutLp);
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			toBalloon = inflater.inflate(R.layout.from_to_balloon, layout);
			toBalloon.findViewById(R.id.poi_content_mini).setBackgroundResource(R.drawable.departure_to_pin);
		}
		View balloon = from ? fromBalloon : toBalloon;
        TextView textView = (TextView) balloon.findViewById(R.id.poi_mini_title);
        textView.setText(from ? "FROM" : "TO");
        textView.setTextColor(ctx.getResources().getColor(from ? R.color.metropia_blue : android.R.color.white));
        Font.setTypeface(Font.getRegular(ctx.getAssets()), textView);
        balloon.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        balloon.layout(0, 0, balloon.getMeasuredWidth(), balloon.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(balloon.getMeasuredWidth(), balloon.getMeasuredHeight(), Bitmap.Config.ARGB_8888);                
        Canvas canvas = new Canvas(bitmap);
        balloon.draw(canvas);
        return bitmap;
	}
	
	private View fromDetailBalloon;
	private View toDetailBalloon;
	
	private Bitmap loadBitmapOfFromToDetailBalloon(Context ctx, String address, boolean from) {
		if(from && fromDetailBalloon == null) {
			FrameLayout layout = new FrameLayout(ctx);
			ViewGroup.LayoutParams layoutLp = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(layoutLp);
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			fromDetailBalloon = inflater.inflate(R.layout.from_to_detail_balloon, layout);
			fromDetailBalloon.findViewById(R.id.poi_content_detail).setBackgroundResource(R.drawable.from_departure_page_detail);
		}
		else if(!from && toDetailBalloon == null) {
			FrameLayout layout = new FrameLayout(ctx);
			ViewGroup.LayoutParams layoutLp = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(layoutLp);
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			toDetailBalloon = inflater.inflate(R.layout.from_to_detail_balloon, layout);
			toDetailBalloon.findViewById(R.id.poi_content_detail).setBackgroundResource(R.drawable.to_departure_page_detail);
		}
		View balloon = from ? fromDetailBalloon : toDetailBalloon;
        TextView titleView = (TextView) balloon.findViewById(R.id.poi_detail_title);
        titleView.setText(from ? "FROM" : "TO");
        titleView.setTextColor(ctx.getResources().getColor(from ? R.color.metropia_blue : android.R.color.white));
        TextView addressView = (TextView) balloon.findViewById(R.id.detail_address);
        addressView.setText(address);
        Font.setTypeface(Font.getRegular(ctx.getAssets()), titleView, addressView);
        balloon.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        balloon.layout(0, 0, balloon.getMeasuredWidth(), balloon.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(balloon.getMeasuredWidth(), balloon.getMeasuredHeight(), Bitmap.Config.ARGB_8888);                
        Canvas canvas = new Canvas(bitmap);
        balloon.draw(canvas);
        return bitmap;
	}

	@Override
	public void onCompassSelected() {}

	@Override
	public void onCurrentPositionSelected() {}

	@Override
	public void onCustomPOISelected(SKMapCustomPOI arg0) {}

	@Override
	public void onDoubleTap(SKScreenPoint arg0) {}

	@Override
	public void onInternationalisationCalled(int arg0) {}

	@Override
	public void onInternetConnectionNeeded() {}

	@Override
	public void onLongPress(SKScreenPoint arg0) {}

	@Override
	public void onMapActionDown(SKScreenPoint arg0) {}

	@Override
	public void onMapActionUp(SKScreenPoint arg0) {}

	@Override
	public void onMapPOISelected(SKMapPOI arg0) {}

	@Override
	public void onMapRegionChangeEnded(SKCoordinateRegion arg0) {}

	@Override
	public void onMapRegionChangeStarted(SKCoordinateRegion arg0) {}

	@Override
	public void onMapRegionChanged(SKCoordinateRegion arg0) {
		if(timeLayout != null && timeLayout.getSelectedTimeButton() != null) {
			updateIncidentAnnotationSize(getSizeRatioByZoomLevel(), timeLayout.getSelectedDepartureTime());
		}
	}

	@Override
	public void onObjectSelected(int arg0) {}

	@Override
	public void onPOIClusterSelected(SKPOICluster arg0) {}

	@Override
	public void onRotateMap() {}

	@Override
	public void onSingleTap(SKScreenPoint arg0) {
		if (mapView==null) return;
		mapView.deleteAnnotation(INCIDENT_BALLOON_ID);
		incBalloonAnn = null;
	}
	
	@Override
	public void onBoundingBoxImageRendered(int arg0) {}

	@Override
	public void onGLInitializationError(String arg0) {}

	@Override
	public void onSurfaceCreated(SKMapViewHolder mapViewHolder) {
		initSKMaps(mapViewHolder);
		Route route = (Route) findViewById(R.id.reserve).getTag();
		if(route != null) {
			List<Route> possibleRoutes = new ArrayList<Route>();
			possibleRoutes.add(route);
			updateMap(possibleRoutes, timeLayout.getSelectedColumn() == 0);
		}
		
		// refresh incident
	    MainActivity.initApiLinksIfNecessary(RouteActivity.this, new Runnable() {
			@Override
			public void run() {
				retriveIncident(null);
			}
	    });
		
		for(RouteTask task : routeTasks) {
			if(task.getStatus() != Status.RUNNING && task.getStatus() != Status.FINISHED && task.getStatus() != Status.RUNNING) {
				task.execute();
			}
		}
		
		Runnable action = mapActionQueue.poll();
		while(action != null) {
			action.run();
			action = mapActionQueue.poll();
		}
	}

	@Override
	public void onDebugInfo(double arg0, float arg1, double arg2) {}
	
	public static boolean restrictedMode = false;
	public void restrictedMode(boolean mode) {
		int visibility = mode? View.GONE:View.VISIBLE;
		findViewById(R.id.buttonLayout).setVisibility(visibility);
		findViewById(R.id.tollLayout).setVisibility(visibility);
		findViewById(R.id.hov_button).setVisibility(visibility);
	}

}
