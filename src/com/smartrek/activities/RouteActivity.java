package com.smartrek.activities;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.smartrek.dialogs.TripSaveDialog;
import com.smartrek.exceptions.RouteNotFoundException;
import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.requests.RouteFetchRequest;
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
import com.smartrek.utils.RouteNode;

/**
 * 
 *
 */
public final class RouteActivity extends Activity {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
    private RouteInfoOverlay[] routeOverlays = new RouteInfoOverlay[3];
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
    
    public static final String LOGIN_PREFS = "login_file";
    
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
        
        SharedPreferences prefs = getSharedPreferences(LOGIN_PREFS, MODE_PRIVATE);
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(false);
        
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
                Log.d("RouteActivity", "Column state: " + timeLayout.getColumnState(column));
                if (!timeLayout.getColumnState(column).equals(State.InProgress)) {
                    
//                  if (timeLayout.getColumnState(column).equals(State.Unknown)) {
                        timeLayout.setColumnState(column, State.InProgress);
                        Time departureTime = timeButton.getDepartureTime();
                        
                        RouteTask routeTask = new RouteTask(originCoord, destCoord, departureTime.toMillis(false), column, true);
                        routeTasks.add(routeTask);
                        routeTask.execute();
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
                    Time departureTime = timeLayout.getDepartureTime(column);
                    
                    RouteTask routeTask = new RouteTask(originCoord, destCoord, departureTime.toMillis(false), column, false);
                    routeTasks.add(routeTask);
                    routeTask.execute();
                }
            }
        });

        ScrollableTimeLayout scrollView = (ScrollableTimeLayout) findViewById(R.id.scrollTime);
        scrollView.setTimeLayout(timeLayout);
        
        Display display = getWindowManager().getDefaultDisplay();

        // FIXME: Should store values in a different preference file
        int displayMode = prefs.getInt(MapDisplayActivity.TIME_DISPLAY_MODE, MapDisplayActivity.TIME_DISPLAY_DEFAULT);
        
        // FIXME: Sloppy
        timeLayout.setDisplayMode((displayMode & MapDisplayActivity.TIME_DISPLAY_TRAVEL) != 0 ? DisplayMode.Duration : DisplayMode.Time);
        
        // FIXME: Temporary solution
        selectedTime = new Time();
        selectedTime.setToNow();
             
        /* Get the extras from the bundle */
        Bundle extras = getIntent().getExtras();
        
        debugMode = extras.getBoolean("debugMode");

        originAddr = extras.getString("originAddr");
        destAddr = extras.getString("destAddr");
        
        originCoord = new GeoPoint(extras.getInt("originLat"), extras.getInt("originLng"));
        destCoord = new GeoPoint(extras.getInt("destLat"), extras.getInt("destLng"));

        dialog.setMessage("Finding routes...");
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
        
        });
        dialog.show();
        
        RouteTask routeTask = new RouteTask(originCoord, destCoord, timeLayout.getDepartureTime(0).toMillis(false), 0, true);
        routeTasks.add(routeTask);
        routeTask.execute();
    }
    
    @Override
    public void onBackPressed() {
        // Ask the user if they want to quit
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to go back to main screen?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        
                        // Cancel all pending tasks
                        for (RouteTask task : routeTasks) {
                            task.cancel(true);
                        }
                        
                        // Stop the activity
                        RouteActivity.this.finish();
                    }

                }).setNegativeButton("No", null).show();
    }
    
    /**
     * This function will be called when BackgroundDownloadTask().execute()
     * succeeds.
     * 
     * @param possibleRoutes
     */
    private void updateMap(List<Route> possibleRoutes) {
    	
    	Log.d("RouteActivity", "updateMap()");
        
        if(possibleRoutes != null && possibleRoutes.size() > 0) {
            /* Get a midpoint to center the view of  the routes */
            GeoPoint mid = getMidPoint(possibleRoutes.get(0).getNodes());
            
            /* range holds 2 points consisting of the lat/lon range to be displayed */
            int[] range = null;
            
            /* Iterate through the routes to draw each to the screen */
            for (int i = 0; i < possibleRoutes.size(); i++) {
                Route route = possibleRoutes.get(i);
            
                /* Draw the route to the screen and hold on to the range */
                range = drawRoute(mapView, route, i);
            }
            
            /* Get the MapController set the midpoint and range */
            MapController mc = mapView.getController();
            //mc.animateTo(mid);
            mc.setCenter(mid);
            mc.zoomToSpan(range[0], range[1]);
            
            mapView.invalidate();
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
        
        int pathColors[] = {Color.RED, Color.BLUE, Color.BLACK};
        
        routePathOverlays[routeNum] = new RoutePathOverlay(this, route, pathColors[routeNum]);
        mapOverlays.add(routePathOverlays[routeNum]);
        
        /* Set values into route to be passed to next Activity */
        route.setAddresses(originAddr, destAddr);
        
        // FIXME:
        route.setUserId(User.getCurrentUser(this).getId());
        
        routeOverlays[routeNum] = new RouteInfoOverlay(mapView, route, new GeoPoint(lat, lon));
        routeOverlays[routeNum].setCallback(new RouteOverlayCallbackImpl(route, routeNum));
        mapOverlays.add(routeOverlays[routeNum]);
        
        /* Add offset of 1000 to range so that map displays extra space around route. */
        int [] range = {latMax - latMin + 1500 ,lonMax - lonMin + 1500};
        
        /* Return the range to doRoute so that map can be adjusted to range settings */
        return range;
    }
    
    /***************************************************************************************************************
     * ****************** private GeoPoint getMidPoint (RouteNode r1, RouteNode r2) ********************************
     *
     * This is a private helper method to obtain a mid point between the first and last nodes in the list of nodes. 
     * This value is used as a center point point for the map view. 
     *
     * @param r1    The first RouteNode in the list of RouteNodes 
     * 
     * @param r2    The last RouteNode in the list of RouteNodes 
     *
     * @return A GeoPoint representing the mid point between the first and last node in the route.
     *
     ***************************************************************************************************************/
    private GeoPoint getMidPoint (List<RouteNode> nodes) {
        GeoPoint p1 = nodes.get(0).getGeoPoint(); 
        GeoPoint p2 = nodes.get(nodes.size()-1).getGeoPoint();
        int midLat = (p1.getLatitudeE6() + p2.getLatitudeE6())/2;
        int midLong = (p1.getLongitudeE6() + p2.getLongitudeE6())/2;
        return new GeoPoint(midLat,midLong);
    }
    
    private void setHighlightedRoutePathOverlays(boolean highlighted) {
        for (int i = 0; i < routeOverlays.length; i++) {
            RoutePathOverlay overlay = routePathOverlays[i];
            
            if (overlay != null) {
                overlay.setHighlighted(highlighted);
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.route, menu);
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
            if (0 != extras.getInt("mapmode")) {
                int mapmode = extras.getInt("mapmode");
                Log.d("RouteActivity", "Got result from menu " + mapmode);
            } else if (0 != extras.getInt("display")) {
                int displayMode = prefs.getInt(MapDisplayActivity.TIME_DISPLAY_MODE, MapDisplayActivity.TIME_DISPLAY_DEFAULT);
                
                // FIXME: Sloppy
                timeLayout.setDisplayMode((displayMode & MapDisplayActivity.TIME_DISPLAY_TRAVEL) != 0 ? DisplayMode.Duration : DisplayMode.Time);
            }
        } 
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        MainMenu.onMenuItemSelected(this, featureId, item);
        
		switch (item.getItemId()) {
		case R.id.menu_save_trip:
			onMenuItemSaveTrip();
			break;
		}
        
        return super.onMenuItemSelected(featureId, item);
    }
    
    private void onMenuItemSaveTrip() {
    	TripSaveDialog dialog = new TripSaveDialog(this, originAddr, destAddr);
    	dialog.setActionListener(new TripSaveDialog.ActionListener() {
			
			@Override
			public void onClickPositiveButton(String name, String origin,
					String destination) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onClickNegativeButton() {
				// TODO Auto-generated method stub
				
			}
		});
    	dialog.show();
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
            //timeLayout.setColumnState(selectedColumn, TimeButton.State.InProgress);
        	
        	setHighlightedRoutePathOverlays(false);
        	
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
            List<Route> possibleRoutes = null;
            try {
            	if (debugMode) {
            		//possibleRoutes = mapper.getFakeRoutes(departureTime);
            	}
            	else {
            		possibleRoutes = getData();
            	}
            }
            catch(Exception e) {
                ehs.registerException(e);
            }
            
            return possibleRoutes;
        }
        
        /**
         * Dialogs must be handled in onPostExecute() because they have to
         * reside in the main loop.
         */
        @Override
        protected void onPostExecute(List<Route> possibleRoutes) {
            dialog.dismiss();
            
            if (ehs.hasExceptions()) {
                ehs.reportExceptions();
            }
            else {
                if(possibleRoutes != null && updateMap) {
                    updateMap(possibleRoutes);
                }
                
                // FIXME: Temporary
                if(possibleRoutes != null && possibleRoutes.size() > 0) {
                    Route firstRoute = possibleRoutes.get(0);
                    timeLayout.setModelForColumn(selectedColumn, firstRoute);
                }
                
                // FIXME: Relying on updateMap is kind of hack-ish. Need to come up with more sophisticated way.
                timeLayout.setColumnState(selectedColumn, updateMap ? TimeButton.State.Selected : TimeButton.State.None);
                //timeLayout.setColumnState(selectedColumn, State.None);
                
                if (selectedColumn == 0) {
                    for (int i = 1; i < 4; i++) {
                        Time departureTime = timeLayout.getDepartureTime(i);
                        new RouteTask(originCoord, destCoord, departureTime.toMillis(false), i, false).execute();
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
            Intent intent = new Intent(RouteActivity.this, ReservationConfirmationActivity.class);
            Bundle extras = new Bundle();
            extras.putParcelable("route", route);
            intent.putExtras(extras);
            startActivity(intent);
            
            return true;
        }

        @Override
        public boolean onTap(int index) {
            // Highlight selected route path
        	setHighlightedRoutePathOverlays(false);
            routePathOverlays[routeNum].setHighlighted(true);
            mapView.getController().setCenter(routeOverlays[routeNum].getGeoPoint());
            
            return true;
        }
    }
}