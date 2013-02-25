package com.smartrek.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.dialogs.CancelableProgressDialog;
import com.smartrek.exceptions.RouteNotFoundException;
import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.requests.RouteFetchRequest;
import com.smartrek.tasks.GeocodingTask;
import com.smartrek.tasks.GeocodingTaskCallback;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.ui.overlays.RouteInfoOverlay;
import com.smartrek.ui.overlays.RouteOverlayCallback;
import com.smartrek.ui.overlays.RoutePathOverlay;
import com.smartrek.ui.timelayout.ScrollableTimeLayout;
import com.smartrek.ui.timelayout.TimeButton;
import com.smartrek.ui.timelayout.TimeButton.DisplayMode;
import com.smartrek.ui.timelayout.TimeButton.State;
import com.smartrek.ui.timelayout.TimeColumn;
import com.smartrek.ui.timelayout.TimeLayout;
import com.smartrek.ui.timelayout.TimeLayout.TimeLayoutListener;
import com.smartrek.ui.timelayout.TimeLayout.TimeLayoutOnSelectListener;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Geocoding;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.SmartrekTileProvider;

/**
 * 
 *
 */
public final class RouteActivity extends ActionBarActivity {
	
	public static final String LOG_TAG = "RouteActivity";
	
	public static final String ORIGIN_ADDR = "originAddr";
	public static final String DEST_ADDR = "destAddr";
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
    private RouteInfoOverlay[] routeInfoOverlays = new RouteInfoOverlay[3];
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
    
    private List<RouteTask> routeTasks = new Vector<RouteTask>();
    
    private boolean debugMode;
    
	private GeocodingTaskCallback originGeocodingTaskCallback = new GeocodingTaskCallback() {
		
		private ProgressDialog dialog;

		@Override
		public void preCallback() {
			dialog = new CancelableProgressDialog(RouteActivity.this, "Geocoding origin address...");
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
			    ehs.reportExceptions();
			}
			else {
				new GeocodingTask(ehs, destGeocodingTaskCallback).execute(destAddr);
			}
		}
		
	};
	
	private GeocodingTaskCallback destGeocodingTaskCallback = new GeocodingTaskCallback() {

		private ProgressDialog dialog;
		
		@Override
		public void preCallback() {
			dialog = new CancelableProgressDialog(RouteActivity.this, "Geocoding destination address...");
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
			    ehs.reportExceptions();
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
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setTileSource(new SmartrekTileProvider());
        
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
        
        // What happens when user selects a specific time
        timeLayout.setOnSelectListener(new TimeLayoutOnSelectListener() {
            @Override
            public void onSelect(int column, TimeColumn timeButton) {
                Log.d(LOG_TAG, "Column state: " + timeLayout.getColumnState(column));
                
                // FIXME: Refactor this. (Close all route info overlays)
                for (int i = 0; i < routeInfoOverlays.length; i++) {
                	RouteInfoOverlay routeInfoOverlay = routeInfoOverlays[i];
                	
                	if (routeInfoOverlay != null) {
                		routeInfoOverlay.hide();
                	}
                }
                
                if (!timeLayout.getColumnState(column).equals(State.InProgress)) {
                    
//                  if (timeLayout.getColumnState(column).equals(State.Unknown)) {
                        timeLayout.setColumnState(column, State.InProgress);
                        long departureTime = timeButton.getDepartureTime();
                        
                        updateRoute(originCoord, destCoord, departureTime, column);
                        
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
            @Override
            public void updateTimeLayout(TimeLayout timeLayout, int column) {
                if (timeLayout.getColumnState(column).equals(State.Unknown)) {
                    timeLayout.setColumnState(column, State.InProgress);
                    long departureTime = timeLayout.getDepartureTime(column);
                    
                    RouteTask routeTask = new RouteTask(originCoord, destCoord, departureTime, column, false);
                    routeTasks.add(routeTask);
                    routeTask.execute();
                }
            }
        });

        ScrollableTimeLayout scrollableTimeLayout = (ScrollableTimeLayout) findViewById(R.id.scrollTime);
        scrollableTimeLayout.setTimeLayout(timeLayout);
        
        Display display = getWindowManager().getDefaultDisplay();
        scrollableTimeLayout.setScreenWidth(Math.max(display.getWidth(), display.getHeight()));

        // FIXME: Should store values in a different preference file
        int timeDisplayMode = prefs.getInt(MapDisplayActivity.TIME_DISPLAY_MODE, MapDisplayActivity.TIME_DISPLAY_DEFAULT);
        
        // FIXME: Sloppy
        timeLayout.setDisplayMode((timeDisplayMode & MapDisplayActivity.TIME_DISPLAY_TRAVEL) != 0 ? DisplayMode.Duration : DisplayMode.Time);
        
        // FIXME: Temporary solution
        selectedTime = new Time();
        selectedTime.setToNow();
             
        /* Get the extras from the bundle */
        Bundle extras = getIntent().getExtras();
        
        debugMode = extras.getBoolean("debugMode");

        originAddr = extras.getString(ORIGIN_ADDR);
        destAddr = extras.getString(DEST_ADDR);
        
        new GeocodingTask(ehs, originGeocodingTaskCallback).execute(originAddr);
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
    	
        // Ask the user if they want to quit
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to go back to previous screen?")
                .setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        
                        // Cancel all pending tasks
                        for (RouteTask task : routeTasks) {
                            task.cancel(true);
                        }
                        
                        // Stop the activity
                        RouteActivity.this.finish();
                    }

                }).setNegativeButton(res.getString(R.string.no), null).show();
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
            
            RouteRect routeRect = new RouteRect(nodes);
            
            /* Get a midpoint to center the view of  the routes */
            GeoPoint mid = routeRect.getMidPoint();
            
            /* range holds 2 points consisting of the lat/lon range to be displayed */
            int[] range = routeRect.getRange();
            
            // Overlays must be drawn in orders
            for (int i = 0; i < possibleRoutes.size(); i++) {
            	mapOverlays.add(routePathOverlays[i]);
            }
            for (int i = 0; i < possibleRoutes.size(); i++) {
            	mapOverlays.add(routeInfoOverlays[i]);
            }
            
            /* Get the MapController set the midpoint and range */
            MapController mc = mapView.getController();
            mc.zoomToSpan(range[0], range[1]);
            mc.setCenter(mid); // setCenter only works properly after zoomToSpan
            
            mapView.postInvalidate();
        }
        else {
        	Log.d(LOG_TAG, "updateMap(): no route available.");
        }
    }
    
    /**
     * Updates the map by fetching a route from a local cache
     * 
     * @param origin
     * @param destination
     * @param departureTime
     * @param column
     * @throws JSONException 
     * @throws IOException 
     * @throws RouteNotFoundException 
     */
    private void updateRoute(GeoPoint origin, GeoPoint destination, long departureTime, int column) {
    	RouteFetchRequest request = new RouteFetchRequest(origin, destination, departureTime);
    	if (request.isCached()) {
	    	try {
				List<Route> routes = request.execute();
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
    	}
    }

    private List<Overlay> mapOverlays;
    
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
        
        routeInfoOverlays[routeNum] = new RouteInfoOverlay(mapView, route, routeNum, new GeoPoint(lat, lon));
        routeInfoOverlays[routeNum].setCallback(new RouteOverlayCallbackImpl(route, routeNum));
        //mapOverlays.add(routeOverlays[routeNum]);
        
        /* Add offset of 1000 to range so that map displays extra space around route. */
        int [] range = {latMax - latMin + 1500 ,lonMax - lonMin + 1500};
        
        /* Return the range to doRoute so that map can be adjusted to range settings */
        return range;
    }
    
    private static class RouteRect {
    	
    	int latMax;
    	int lonMax;
    	int latMin;
    	int lonMin;
    	
    	RouteRect(List<RouteNode> nodes) {
            int latMax = (int) (-81 * 1E6);
            int lonMax = (int) (-181 * 1E6);
            int latMin = (int) (+81 * 1E6);
            int lonMin = (int) (+181 * 1E6);
            for (int i = 0; i < nodes.size() - 1; i++) {
                GeoPoint point = nodes.get(i).getGeoPoint();
                int curLat = point.getLatitudeE6();
                int curLon = point.getLongitudeE6();
                latMax = Math.max(latMax, curLat);
                lonMax = Math.max(lonMax, curLon);
                latMin = Math.min(latMin, curLat);
                lonMin = Math.min(lonMin, curLon);
            }
            this.latMax = latMax;
            this.lonMax = lonMax;
            this.latMin = latMin;
            this.lonMin = lonMin;
        }
        
        GeoPoint getMidPoint(){
        	return new GeoPoint((latMax + latMin) / 2, (lonMax + lonMin) / 2);
        }
        
        int[] getRange(){
        	return new int[]{latMax - latMin, lonMax - lonMin};
        }
    	
    }
    
    private void setHighlightedRoutePathOverlays(boolean highlighted) {
        for (int i = 0; i < routeInfoOverlays.length; i++) {
            RoutePathOverlay overlay = routePathOverlays[i];
            
            if (overlay != null) {
                overlay.setHighlighted(highlighted);
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater mi = getSupportMenuInflater();
        mi.inflate(R.menu.main, menu);
        return true;
    }
    
    /**
     * 
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Bundle extras = intent.getExtras();
        
        if(requestCode == -1) {
            finish();
        }
        
        SharedPreferences prefs = getSharedPreferences(MapDisplayActivity.MAP_DISPLAY_PREFS, MODE_PRIVATE);
        
        if (extras != null) {
            if (extras.getInt("display") != 0) {
                int displayMode = prefs.getInt(MapDisplayActivity.TIME_DISPLAY_MODE, MapDisplayActivity.TIME_DISPLAY_DEFAULT);
                
                // FIXME: Sloppy
                timeLayout.setDisplayMode((displayMode & MapDisplayActivity.TIME_DISPLAY_TRAVEL) != 0 ? DisplayMode.Duration : DisplayMode.Time);
            }
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
        
        private ProgressDialog dialog;
        
        public RouteTask(GeoPoint origin, GeoPoint destination, long departureTime, int column, boolean updateMap) {
        	super();
        	
        	this.origin = origin;
        	this.destination = destination;
        	this.departureTime = departureTime;
        	this.selectedColumn = column;
        	this.updateMap = updateMap;
        }
        
        public boolean isCached() {
        	RouteFetchRequest request = new RouteFetchRequest(origin, destination, departureTime);
        	return request.isCached();
        }
        
        public List<Route> getData() throws RouteNotFoundException, IOException, JSONException {
        	RouteFetchRequest request = new RouteFetchRequest(origin, destination, departureTime);
        	return request.execute();
        }
        
        @Override
        protected void onPreExecute() {
            // FIXME: Should this be here?
            timeLayout.setColumnState(selectedColumn, TimeButton.State.InProgress);
            
            dialog = new CancelableProgressDialog(RouteActivity.this, "Finding routes...");
	        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
	        		getResources().getString(R.string.cancel),
	        		new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
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
            		routes = request.execute();
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
                ehs.reportExceptions();
            }
            else {
            	// FIXME: Temporary
            	if(routes != null && routes.size() > 0) {
            		Route firstRoute = routes.get(0);
            		timeLayout.setModelForColumn(selectedColumn, firstRoute);
            	}
            	
                if(routes != null && updateMap) {
                    updateMap(routes);
                }
                
                // FIXME: Relying on updateMap is kind of hack-ish. Need to come up with more sophisticated way.
                timeLayout.setColumnState(selectedColumn, updateMap ? TimeButton.State.Selected : TimeButton.State.None);
                //timeLayout.setColumnState(selectedColumn, State.None);
                
                if (selectedColumn == 0) {
                    for (int i = 1; i < Math.min(9, timeLayout.getColumnCount()); i++) {
                        long departureTime = timeLayout.getDepartureTime(i);
                        new RouteTask(originCoord, destCoord, departureTime, i, false).execute();
                    }
                }
            }
        }
    }
    
    private class RouteOverlayCallbackImpl implements RouteOverlayCallback {

        private Route route;
        private int routeNum;
        
        public RouteOverlayCallbackImpl(Route route, int routeNum) {
            this.route = route;
            this.routeNum = routeNum;
        }
        
        @Override
        public boolean onBalloonTap(int index, OverlayItem item) {
        	Log.d("RouteActivity.RouteOverlayCallbackImpl", "onBalloonTap()");
            Intent intent = new Intent(RouteActivity.this, ReservationConfirmationActivity.class);
            Bundle extras = new Bundle();
            extras.putParcelable("route", route);
            intent.putExtras(extras);
            startActivity(intent);
            
            return true;
        }

        @Override
        public boolean onTap(int index) {
        	Log.d("RouteActivity.RouteOverlayCallbackImpl", "onTap()");
            // Highlight selected route path
        	setHighlightedRoutePathOverlays(false);
            routePathOverlays[routeNum].setHighlighted(true);
            mapView.getController().setCenter(routeInfoOverlays[routeNum].getGeoPoint());
            
            return true;
        }

		@Override
		public boolean onClose() {
			Log.d("RouteActivity.RouteOverlayCallbackImpl", "onClose()");
			setHighlightedRoutePathOverlays(true);
			mapView.invalidate();
			
			return true;
		}
    }
}
