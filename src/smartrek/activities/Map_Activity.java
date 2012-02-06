package smartrek.activities;

import java.util.ArrayList;
import java.util.List;

import smartrek.AdjustableCouponDisplay.CouponLayout;
import smartrek.AdjustableTimeDisplay.TimeButton;
import smartrek.AdjustableTimeDisplay.TimeLayout;
import smartrek.SeverCommunicator.Coupon_Communicator;
import smartrek.SeverCommunicator.Route_Communicator;
import smartrek.overlays.RouteOverlay;
import smartrek.overlays.RouteSegmentOverlay;
import smartrek.util.Coupon;
import smartrek.util.Route;
import smartrek.util.RouteNode;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
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

/******************************************************************************************************************
 * 
 * @author Tim Olivas
 *
 *******************************************************************************************************************/
public class Map_Activity extends MapActivity {
	
	private RouteOverlay routeoverlay1;
	private RouteOverlay routeoverlay2;
	private RouteOverlay routeoverlay3;
	
	private int uid;
//	private String user;
	private String origin;
	private String destination;
	private ProgressDialog dialog;
	private MapView mapView;
	
	private int GENMAP = 1;
	private int SATELLITE = 2;
	private int CURRENTMODE = GENMAP;
	
	private int DEPARTONLY = 1;
	private int DEPART_AND_ARRIVE = 2;
	private int DEPART_AND_TRAVEL = 3;
	private int DEPART_ARRIVE_TRAVEL = 4;
	private int CURRENTDISPLAY = DEPARTONLY;
	
	private Time selectedTime;
	
	private TimeLayout arriveTime;
	private TimeLayout travelTime;
	
	private Route selectedRoute;
	
    private ArrayList<Coupon> coupons;
	private ArrayList<Route> routes;
	private HorizontalScrollView couponScroll;
	private CouponLayout couponLayout;
	private TextView coupTitleBar;
	private Context context;
	
	private final String LOGIN_PREFS = "login_file";
	
	/****************************************************************************************************************
	 * ************************** onCreate(Bundle savedInstanceState) ***********************************************
	 * 
	 *
	 ****************************************************************************************************************/
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.pre_reservation_map);	
	   
	    Log.d("Map_Activity", "Starting Map_Activity");
	    
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
	    
  	   	dialog = new ProgressDialog(Map_Activity.this);
  	   	dialog.setMessage("Computing Routes...");
  	   	dialog.setIndeterminate(true);
  	   	dialog.setCancelable(false);
  	   	dialog.show();	
  	   	
    	/* Get the extras from the bundle */
	    Bundle extras = getIntent().getExtras();
	    
	 //   uid = extras.getInt("uid");
	    
		SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_PREFS,MODE_PRIVATE);
		uid = sharedPreferences.getInt("uid", -1);
	    
	    
	    Log.d("Map_Activity","Got user id " + uid);
	 
	    origin = extras.getString("origin");
	    Log.d("Map_Activity","Got origin " + origin);
	    
	    destination = extras.getString("destination");
	    Log.d("Map_Activity","Got destination " + destination);
	    
	    /* Begin download of the route information */
	    new BackgroundDownloadTask().execute();
	    
	    setupScrollTime(); 
	    
	    TimeLayout tl = (TimeLayout) findViewById(R.id.timelayout);
	    TimeButton tb = ((TimeButton) tl.getChildAt(0));
	    selectedTime = tb.getTime();
	    
	    couponScroll = (HorizontalScrollView) findViewById(R.id.scrollCoupon);
	    couponScroll.setScrollContainer(true);
	    
	    couponLayout = (CouponLayout)couponScroll.getChildAt(0);
	    coupTitleBar = (TextView) findViewById(R.id.adjustableCouponLable);
	    coupTitleBar.setVisibility(View.GONE);
  	   	Log.d("Map_Activity", "current selected time is " + selectedTime.toString());
	    
	}
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	private void setupScrollTime(){
	    
		arriveTime = (TimeLayout) findViewById(R.id.arrivaltimelayout);
		travelTime = (TimeLayout) findViewById(R.id.traveltimelayout);
		
		travelTime.setVisibility(View.GONE);
		arriveTime.setVisibility(View.GONE);
	    
	    TimeLayout timelayout = (TimeLayout) findViewById(R.id.timelayout);
	    timelayout.setMapActivity(this); 
	    
	    timelayout.setDependents(arriveTime,travelTime);
	}
		
	/****************************************************************************************************************
	 * 
	 *
	 *
	 ****************************************************************************************************************/
	public void doRoute(Time time) {
	   
		/* Create an instance of Route_Communicator to handle route doownload */
		Route_Communicator rcomm = new Route_Communicator();
		
		/* Get the possible routes from the server */
		ArrayList<Route> possible_Routes = rcomm.getPossibleRoutes(origin, destination, time);
		
		// FIXME:
		if(possible_Routes == null) {
			return;
		}
		
		Log.d("Map_Activity","Got " + possible_Routes.size() + "Possible Routes from server");
		
		routes = possible_Routes;
		
		/* Get a midpoint to center the view of  the routes */
		GeoPoint mid = getMidPoint(possible_Routes.get(0).getPoints());
		
		/* range holds 2 points consisting of the lat/lon range to be displayed */
		int[] range = null;
		
		/* Iterate through the routes to draw each to the screen */
		for (int i = 0; i < possible_Routes.size(); i++) {
			Route route = possible_Routes.get(i);
		
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
        new BackgroundDownloadImageTask().execute();
		
        /* Get the MapController set the midpoint and range */
		MapController mc = mapView.getController();
		mc.animateTo(mid);
		mc.zoomToSpan(range[0], range[1]);
	}

	/****************************************************************************************************************
	 * 
	 *
	 *
	 ****************************************************************************************************************/
	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}
	
	List<Overlay> mapOverlays;
	
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
	    route.setTime(selectedTime);
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
	    Log.d("Map_Activity", "In Map_Activity setting route time");
	    Log.d("Map_Activity", selectedTime.format3339(false));
	    
	    /* Add offset of 1000 to range so that map displays extra space around route. */
	    int [] range = {latMax - latMin + 1500 ,lonMax - lonMin + 1500};
	    
	    /* Log range values to debug */
	    Log.d("Map_Activity", " Latitude Range:" + range[0]);
	    Log.d("Map_Activity", " Longitude Range:" + range[1]);  
	    
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
    	Log.d("Map_Activity","Menu Opened from Map_Activity");
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
		Log.d("Map_Activity", "Menu Open: Entering Map Mode Options");
		Intent intent = null;
		switch(item.getItemId()){
    	case R.id.menusettings:
//    		Log.d("Map_Activity", "Menu Open: Settings Selected");
//    		Intent intent = new Intent(this,Map_Menu_Activity.class);
//    		startActivity(intent);
    		return true;
    	case R.id.map_display_options:
    		intent = new Intent(this,MapDisplayActivity.class);
    		int displayed = 0;
    		intent.putExtra("mapmode", CURRENTDISPLAY);
    		startActivityForResult(intent, displayed);
    		
    		Log.d("Map_Activity","Returned " + displayed + "from map display options");
    		return true;
    	case R.id.map_mode:
    		intent = new Intent(this,MapModeActivity.class);
    		int val = 0;
    		intent.putExtra("mapmode", CURRENTMODE);
    		startActivityForResult(intent, val);
    		Log.d("Map_Activity","Returned " + val + "from map mode options");
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
	
	/****************************************************************************************************************
	 * 
	 *
	 *
	 ****************************************************************************************************************/
	@Override
	protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Bundle extras = intent.getExtras();
		
		if(requestCode == -1) {
			finish();
		}
		
		if (extras != null) {
			if (0 != extras.getInt("mapmode")) {
				int mapmode = extras.getInt("mapmode");
				Log.d("Map_Activity", "Got result from menu " + mapmode);
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
				int display = extras.getInt("display");
				Log.d("Map_Activity", "Got result from menu " + display);
				if (display != CURRENTDISPLAY) {				
					if(display == DEPARTONLY){
						travelTime.setVisibility(View.GONE);
						arriveTime.setVisibility(View.GONE);
					}
					if (display == DEPART_AND_ARRIVE){				
						travelTime.setVisibility(View.GONE);
						arriveTime.setVisibility(View.VISIBLE);
					}
					if (display == DEPART_AND_TRAVEL){
						travelTime.setVisibility(View.VISIBLE);
						arriveTime.setVisibility(View.GONE);
					} 
					if (display == DEPART_ARRIVE_TRAVEL){
						travelTime.setVisibility(View.VISIBLE);
						arriveTime.setVisibility(View.VISIBLE);
					}
					CURRENTDISPLAY = display;
				}
			}
		} 
	}

/*=====================================================================================================================*/
	
	/****************************************************************************************************************
	 * 
	 *
	 *
	 ****************************************************************************************************************/ 
    protected class BackgroundDownloadTask extends AsyncTask<Void,Void,Void > {    	 
    	
    	/****************************************************************************************************************
    	 * 
    	 *
    	 *
    	 ****************************************************************************************************************/ 
    	protected Void doInBackground(Void... url) {  
    		
//   		    Coupon_Communicator cpc = new Coupon_Communicator();
//		    coupons = cpc.discount();
    		
    		final Time time = new Time();
    		
    		time.setToNow();
    		doRoute(time);
    		
    		
        	try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            return null;
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
        	
        	Log.d("Map_Activity", "Starting AsyncTask BackgroundDownloadImageTask");
        	
        	Coupon_Communicator ccom = new Coupon_Communicator();
        	
        	for (int i = 0; i < routes.size(); i++) {
        		ccom.doCouponBitmapDownloads(routes.get(i).getAllCoupons(), context);
			}
        	
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
			Log.d("Map_Activity","Background Image Download complete, setting coupon bitmaps");

			couponLayout.setRoutes(routes);

			routeoverlay1.setRoute(routes.get(0), 0);
			routeoverlay1.setCouponLayout(couponLayout,coupTitleBar);
			
			routeoverlay2.setRoute(routes.get(1), 1);
			routeoverlay2.setCouponLayout(couponLayout,coupTitleBar);
			
			routeoverlay3.setRoute(routes.get(2), 2);
			routeoverlay3.setCouponLayout(couponLayout,coupTitleBar);
			
			dialog.dismiss();
		}
	}
/*=====================================================================================================================*/
}