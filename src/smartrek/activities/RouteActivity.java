package smartrek.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import smartrek.AdjustableCouponDisplay.CouponLayout;
import smartrek.AdjustableTimeDisplay.ScrollableTimeLayout;
import smartrek.AdjustableTimeDisplay.TimeButton;
import smartrek.AdjustableTimeDisplay.TimeLayout;
import smartrek.AdjustableTimeDisplay.TimeButton.State;
import smartrek.AdjustableTimeDisplay.TimeLayout.TimeLayoutListener;
import smartrek.AdjustableTimeDisplay.TimeLayout.TimeLayoutOnSelectListener;
import smartrek.mappers.CouponMapper;
import smartrek.mappers.RouteMapper;
import smartrek.models.Coupon;
import smartrek.models.Route;
import smartrek.overlays.RouteOverlay;
import smartrek.overlays.RouteSegmentOverlay;
import smartrek.ui.ObservableScrollView;
import smartrek.ui.ScrollViewListener;
import smartrek.util.Geocoding;
import smartrek.util.RouteNode;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * 
 *
 */
public class RouteActivity extends MapActivity {
    
    public static final int DIALOG_ROUTE_NOT_FOUND = 1;
    
    private RouteOverlay routeoverlay1;
    private RouteOverlay routeoverlay2;
    private RouteOverlay routeoverlay3;
    
    private int uid;
//    private String user;
    private String origin;
    private String destination;
    private GeoPoint originCoord;
    private GeoPoint destCoord;
    
    private ProgressDialog dialog;
    private MapView mapView;
    
    private int GENMAP = 1;
    private int SATELLITE = 2;
    private int CURRENTMODE = GENMAP;
    
    /**
     * @deprecated
     */
    private int DEPARTONLY = 1;
    
    /**
     * @deprecated
     */
    private int DEPART_AND_ARRIVE = 2;
    
    /**
     * @deprecated
     */
    private int DEPART_AND_TRAVEL = 3;
    
    /**
     * @deprecated
     */
    private int DEPART_ARRIVE_TRAVEL = 4;
    
    /**
     * @deprecated
     */
    private int CURRENTDISPLAY = DEPARTONLY;
    
    private Time selectedTime;
    
    private TimeLayout timeLayout;
    
    private ArrayList<Coupon> coupons;
    private List<Route> routes;
    private HorizontalScrollView couponScroll;
    private CouponLayout couponLayout;
    private TextView coupTitleBar;
    private Context context;
    
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
        
        Log.d("RouteActivity", "Starting RouteActivity");

        SharedPreferences prefs = getSharedPreferences(LOGIN_PREFS, MODE_PRIVATE);
        
        mapView = (MapView) findViewById(R.id.mapview);
        
        mapView.setBuiltInZoomControls(true);
        if(CURRENTMODE == SATELLITE){
            mapView.setSatellite(true);
        } else if(CURRENTMODE == GENMAP){
            mapView.setSatellite(false);
        }
    
        context = this;
        
        /* Set the map view for a view of North America before zooming in on route */
        MapController mc = mapView.getController();
        int lat = (int) Math.round(38.27268853598097f*1E6);
        int lon = (int) Math.round(-99.1406250000000f*1E6);
        mc.setZoom(4); 
        mc.animateTo(new GeoPoint(lat, lon));
        
        dialog = new ProgressDialog(RouteActivity.this);
        dialog.setMessage("Geocoding...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        
        
        //
        // Set up time layout
        //        
    	timeLayout = (TimeLayout) findViewById(R.id.timelayout);
        timeLayout.setOnSelectListener(new TimeLayoutOnSelectListener() {
			@Override
			public void onSelect(int column, TimeButton timeButton1, TimeButton timeButton2) {
				Time departureTime = timeButton1.getTime();
				//doRoute(originCoord, destCoord, departureTime);
				dialog.show();
		        new RouteTask().execute(originCoord, destCoord, departureTime, column, true);
			}
		});
        timeLayout.setTimeLayoutListener(new TimeLayoutListener() {
			@Override
			public void updateTimeLayout(TimeLayout timeLayout, int column) {
				Time departureTime = timeLayout.getDepartureTime(column);
				new RouteTask().execute(originCoord, destCoord, departureTime, column, false);
			}
        });

        ScrollableTimeLayout scrollView = (ScrollableTimeLayout) findViewById(R.id.scrollTime);
        scrollView.setTimeLayout(timeLayout);
        
        Display display = getWindowManager().getDefaultDisplay();

        int displayMode = prefs.getInt(MapDisplayActivity.TIME_DISPLAY_MODE, MapDisplayActivity.TIME_DISPLAY_DEFAULT);
        
        // FIXME: Sloppy
        timeLayout.setDisplayMode((displayMode & MapDisplayActivity.TIME_DISPLAY_TRAVEL) != 0 ? TimeLayout.DisplayMode.TravelTime : TimeLayout.DisplayMode.ArrivalTime);
        
        // FIXME: Temporary solution
        selectedTime = new Time();
        selectedTime.setToNow();
        
        //
        //
             
        /* Get the extras from the bundle */
        Bundle extras = getIntent().getExtras();
        
        uid = prefs.getInt("uid", -1);
        
        Log.d("RouteActivity","Got user id " + uid);
     
        origin = extras.getString("origin");
        Log.d("RouteActivity","Got origin " + origin);
        
        destination = extras.getString("destination");
        Log.d("RouteActivity","Got destination " + destination);
        
        // Workflow:
        //   1. Geocoding (address to coordinate)
        //   2. Request for routes
        //   3. Draw routes on the map view
        GeocodingTaskCallback callback = new GeocodingTaskCallback() {
			@Override
			public void preCallback() {
	            dialog.show();
			}
        	
        	@Override
			public void callback(GeoPoint origin, GeoPoint destination) {
				originCoord = origin;
				destCoord = destination;

				for(int i = 0; i < 3; i++) {
					Time departureTime = timeLayout.getDepartureTime(i);
					
					// `i` is going to be `selectedColumn` for the time layout
					// Only updates maps for `i = 0`
					new RouteTask(i).execute(origin, destination, departureTime, i, (i == 0));
				}
			}
			
			@Override
			public void postCallback() {
				dialog.setMessage("Computing routes...");
			}
		};
        new GeocodingTask(callback).execute(origin, destination);
        
        couponScroll = (HorizontalScrollView) findViewById(R.id.scrollCoupon);
        couponScroll.setScrollContainer(true);
        
        couponLayout = (CouponLayout)couponScroll.getChildAt(0);
        coupTitleBar = (TextView) findViewById(R.id.adjustableCouponLable);
        coupTitleBar.setVisibility(View.GONE);
        //Log.d("RouteActivity", "current selected time is " + selectedTime.toString());

    }

    /**
     * 
     * @param origin
     * @param destination
     * @param time
     */
    public void doRoute(GeoPoint origin, GeoPoint destination, Time time) {
        dialog.show();
        new RouteTask().execute(origin, destination, time);
    }
    
    /**
     * This function will be called when BackgroundDownloadTask().execute()
     * succeeds.
     * 
     * @param possibleRoutes
     * @param time
     */
    private void updateMap(List<Route> possibleRoutes, Time time) {
        
        if(possibleRoutes != null && possibleRoutes.size() > 0) {
            /* Get a midpoint to center the view of  the routes */
            GeoPoint mid = getMidPoint(possibleRoutes.get(0).getPoints());
            
            /* range holds 2 points consisting of the lat/lon range to be displayed */
            int[] range = null;
            
            /* Iterate through the routes to draw each to the screen */
            for (int i = 0; i < possibleRoutes.size(); i++) {
                Route route = possibleRoutes.get(i);
            
                /* Get all coupons associated with the route */
                ArrayList<Coupon> coupons = route.getAllCoupons();
                
                /* If its the first route display that routes coupons */
                if(i == 0) {
                    this.coupons = coupons;
                }
                
                /* Draw the route to the screen and hold on to the range */
                range = drawRoute(mapView, route, i);
            }
            
            /* Start the Thread to download the coupon images */
            //new BackgroundDownloadImageTask().execute();
            
            /* Get the MapController set the midpoint and range */
            MapController mc = mapView.getController();
            mc.animateTo(mid);
            mc.zoomToSpan(range[0], range[1]);
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return true;
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
        Drawable drawable;
        
        if(routeNum == 0)
            mapOverlays.clear();
        
        int latMax = (int)(-81 * 1E6);
        int lonMax = (int)(-181 * 1E6);
        int latMin = (int)(+81 * 1E6);
        int lonMin = (int)(+181 * 1E6);
         
        ArrayList<RouteNode> route_nodes = route.getPoints();
        
        int lat = 0;
        int lon = 0;
        
        for(int i = 0; i < route_nodes.size()-1; i++) {    
            GeoPoint point = route_nodes.get(i).getPoint();
            
            int curLat = point.getLatitudeE6();
            int curLon = point.getLongitudeE6();
            
            if(i == route_nodes.size()/2){
                lat = curLat + 500;
                lon = curLon+ 150;
            }
            
            latMax = Math.max(latMax, curLat);
            lonMax = Math.max(lonMax, curLon);
            latMin = Math.min(latMin, curLat);
            lonMin = Math.min(lonMin, curLon);
            
            Overlay overlayitem = new RouteSegmentOverlay(point, route_nodes.get(i+1).getPoint(), routeNum);
            mapOverlays.add(overlayitem);
        }
        
        /* Set values into route to be passed to next Activity */
        route.setOD(origin, destination);
        route.setUserId(uid);
        
        drawable = this.getResources().getDrawable(R.drawable.routetag);
        
        if(routeNum == 0) {
            routeoverlay1 = new RouteOverlay(drawable,mapView);
            OverlayItem oi = new OverlayItem(new GeoPoint(lat,lon),
                                             "Route " + (routeNum + 1),
                                             "Origin: \n" + route.getOrigin()  + " \n\n" +
                                             "Destination: \n" + route.getDestination() + "\n\n" + 
                                             "Estimated Travel Time: \n" + route.getTimeString());
            routeoverlay1.addOverlay(oi);
            mapOverlays.add(routeoverlay1);
        } else if(routeNum == 1) {
            routeoverlay2 = new RouteOverlay(drawable,mapView);
            OverlayItem oi = new OverlayItem(new GeoPoint(lat,lon),
                                             "Route " + (routeNum + 1),
                                             "Origin: \n" + route.getOrigin()  + " \n\n" +
                                             "Destination: \n" + route.getDestination() + "\n\n" +
                                             "Estimated Travel Time: \n" + route.getTimeString());
            routeoverlay2.addOverlay(oi);
            mapOverlays.add(routeoverlay2);
        } else {
            routeoverlay3 = new RouteOverlay(drawable,mapView);
            OverlayItem oi = new OverlayItem(new GeoPoint(lat,lon), 
                                             "Route " + (routeNum + 1),
                                             "Origin: \n" + route.getOrigin()  + " \n\n" +
                                             "Destination: \n" + route.getDestination() + "\n\n" +
                                             "Estimated Travel Time: \n" + route.getTimeString());
            routeoverlay3.addOverlay(oi);
            mapOverlays.add(routeoverlay3);
        }
        
        /* Log selected time to debug */
        Log.d("RouteActivity", "In RouteActivity setting route time");
        Log.d("RouteActivity", selectedTime.format3339(false));
        
        /* Add offset of 1000 to range so that map displays extra space around route. */
        int [] range = {latMax - latMin + 1500 ,lonMax - lonMin + 1500};
        
        /* Log range values to debug */
        Log.d("RouteActivity", " Latitude Range:" + range[0]);
        Log.d("RouteActivity", " Longitude Range:" + range[1]);  
        
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
    private GeoPoint getMidPoint (ArrayList<RouteNode> nodes) {
        GeoPoint p1 = nodes.get(0).getPoint(); 
        GeoPoint p2 = nodes.get(nodes.size()-1).getPoint();
        int midLat = (p1.getLatitudeE6() + p2.getLatitudeE6())/2;
        int midLong = (p1.getLongitudeE6() + p2.getLongitudeE6())/2;
        return new GeoPoint(midLat,midLong);
    }
    
    /****************************************************************************************************************
     * 
     *
     *
     ****************************************************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        Log.d("RouteActivity","Menu Opened from RouteActivity");
         MenuInflater mi = getMenuInflater();
         mi.inflate(R.menu.mapoptions, menu);
        return true;
    }
    
    /****************************************************************************************************************
     * 
     *
     *
     ****************************************************************************************************************/
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item){
        Log.d("RouteActivity", "Menu Open: Entering Map Mode Options");
        Intent intent = null;
        switch(item.getItemId()){
        case R.id.menusettings:
//            Log.d("RouteActivity", "Menu Open: Settings Selected");
//            Intent intent = new Intent(this,Map_Menu_Activity.class);
//            startActivity(intent);
            return true;
            
        case R.id.map_display_options:
            intent = new Intent(this,MapDisplayActivity.class);
            int displayed = 0;
            intent.putExtra("mapmode", CURRENTDISPLAY);
            startActivityForResult(intent, displayed);
            
            Log.d("RouteActivity","Returned " + displayed + "from map display options");
            return true;
            
        case R.id.map_mode:
            intent = new Intent(this,MapModeActivity.class);
            int val = 0;
            intent.putExtra("mapmode", CURRENTMODE);
            startActivityForResult(intent, val);
            Log.d("RouteActivity","Returned " + val + "from map mode options");
            return true;
            
        case R.id.mycoupons:
            intent = new Intent(this,MyCouponsActivity.class);
            startActivity(intent);
            return true;
            
        case R.id.logout_option:
            SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_PREFS,MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("user", "");
            editor.putInt("uid", -1);
            editor.commit();
            finish();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
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
                if (mapmode != CURRENTMODE) {
                    if (mapmode == SATELLITE) {
                        mapView.setSatellite(true);
                    } else if (mapmode == GENMAP) {
                        mapView.setSatellite(false);
                    }
                    CURRENTMODE = mapmode;
                    mapView.invalidate();
                }
            } else if (0 != extras.getInt("display")) {
                int displayMode = prefs.getInt(MapDisplayActivity.TIME_DISPLAY_MODE, MapDisplayActivity.TIME_DISPLAY_DEFAULT);
                
                // FIXME: Sloppy
                timeLayout.setDisplayMode((displayMode & MapDisplayActivity.TIME_DISPLAY_TRAVEL) != 0 ? TimeLayout.DisplayMode.TravelTime : TimeLayout.DisplayMode.ArrivalTime);
            }
        } 
    }
    
    private Stack<Exception> exceptions = new Stack<Exception>();
    
    private void reportExceptions() {
        while(!exceptions.isEmpty()) {
            Exception e = exceptions.pop();
            
            AlertDialog dialog = new AlertDialog.Builder(RouteActivity.this).create();
            dialog.setTitle("Exception");
            dialog.setMessage(e.getMessage());
            dialog.setButton("Dismiss", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialog.show();
        }
    }

    /**
     * Defines an interface that is going to be called when GeocodingTask.execute()
     * is completed.
     */
	interface GeocodingTaskCallback {
		public void preCallback();
		public void callback(GeoPoint origin, GeoPoint destination);
		public void postCallback();
	}

	/**
	 * Asynchronous task that converts a postal address to a geographic coordinate.
	 */
    private class GeocodingTask extends AsyncTask<String, Void, Void> {
    	
    	GeocodingTaskCallback callback;
    	
    	public GeocodingTask(GeocodingTaskCallback callback) {
    		super();
    		this.callback = callback;
    	}
        
        @Override
        protected void onPreExecute () {
        	if(callback != null) {
        		callback.preCallback();
        	}
        }
        
        @Override
        protected Void doInBackground(String... args) {
            String origin = args[0];
            String destination = args[1];
            
            GeoPoint originCoord = Geocoding.lookup(origin);
            GeoPoint destCoord = Geocoding.lookup(destination);
            

            boolean coordNotFound = false;
            if(originCoord.getLatitudeE6() == 0 && originCoord.getLongitudeE6() == 0) {
                coordNotFound = true;
                exceptions.push(new Exception("Could not find a coordinate of the origin address."));
            }
            if(destCoord.getLatitudeE6() == 0 && destCoord.getLongitudeE6() == 0) {
                coordNotFound = true;
                exceptions.push(new Exception("Could not find a coordinate of the destination address."));
            }
            
            if(!coordNotFound && callback != null) {
            	callback.callback(originCoord, destCoord);
                //onGeoLocationFound(originCoord, destCoord);
            }
            
            return null;
        }
        
        @Override
        protected void onPostExecute(Void v) {
            reportExceptions();
            if(callback != null) {
            	callback.postCallback();
            }
        }

    }
    
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
    	
    	public RouteTask() {
    		super();
    	}
    	
    	/**
    	 * 
    	 * @param column Indicates a column in TimeLayout that this class is bound to
    	 */
    	public RouteTask(int column) {
    		this.selectedColumn = column;
    	}
    	
        @Override
        protected void onPreExecute () {
            // FIXME: Should this be here?
            timeLayout.setColumnState(selectedColumn, TimeButton.State.InProgress);
        }
        
        @Override
        protected List<Route> doInBackground(Object... args) {  
            
        	// FIXME: Potential array out of boundary issues
            GeoPoint origin = (GeoPoint)args[0];
            GeoPoint destination = (GeoPoint)args[1];
            Time time = (Time)args[2];
            selectedColumn = (Integer)args[3];
            updateMap = (Boolean)args[4];
            
            RouteMapper mapper = new RouteMapper();
            
            /* Get the possible routes from the server */
            List<Route> possibleRoutes = null;
            try {
                possibleRoutes = mapper.getPossibleRoutes(origin, destination, time);
                
                if(possibleRoutes == null || possibleRoutes.size() == 0) {
                    exceptions.push(new Exception("Could not find a route."));
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                exceptions.push(e);
            }
            
            if(possibleRoutes != null && updateMap) {
                updateMap(possibleRoutes, time);
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
            
            reportExceptions();
            
            // FIXME: Temporary
            if(possibleRoutes != null && possibleRoutes.size() > 0) {
            	Route firstRoute = possibleRoutes.get(0);
            	timeLayout.setModelForColumn(selectedColumn, firstRoute);
            }
            
            // FIXME: Relying on updateMap is kind of hack-ish. Need to come up with more sophiscated way.
            timeLayout.setColumnState(selectedColumn, updateMap ? TimeButton.State.Selected : TimeButton.State.None);
        }

    }

/*=====================================================================================================================*/
    
/*=====================================================================================================================*/
    
    /****************************************************************************************************************
     * 
     *
     *
     ****************************************************************************************************************/ 
    private class BackgroundDownloadImageTask extends AsyncTask<Void, Void, Void> {
        
        /****************************************************************************************************************
         * 
         *
         *
         ****************************************************************************************************************/ 
        protected Void doInBackground(Void... v) {   
            
            Log.d("RouteActivity", "Starting AsyncTask BackgroundDownloadImageTask");
            
            CouponMapper ccom = new CouponMapper();
            
            for (int i = 0; i < routes.size(); i++) {
                ccom.doCouponBitmapDownloads(routes.get(i).getAllCoupons(), context);
            }
            
            // FIXME: Fuck me...
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            return null;
        }       
        
        /****************************************************************************************************************
         * 
         *
         *
         ****************************************************************************************************************/ 
        protected void onPostExecute(Void v) {
            Log.d("RouteActivity", "Background Image Download complete, setting coupon bitmaps");

            couponLayout.setRoutes(routes);

            dialog.dismiss();
        }
    }
/*=====================================================================================================================*/
}