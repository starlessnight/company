package com.smartrek.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartrek.dialogs.FeedbackDialog;
import com.smartrek.dialogs.NotificationDialog2;
import com.smartrek.models.Reservation;
import com.smartrek.models.User;
import com.smartrek.requests.AddressLinkRequest;
import com.smartrek.requests.CityRequest;
import com.smartrek.requests.CityRequest.City;
import com.smartrek.requests.FavoriteAddressAddRequest;
import com.smartrek.requests.FavoriteAddressDeleteRequest;
import com.smartrek.requests.FavoriteAddressFetchRequest;
import com.smartrek.requests.FavoriteAddressUpdateRequest;
import com.smartrek.requests.Request;
import com.smartrek.requests.ReservationListFetchRequest;
import com.smartrek.requests.UpdateDeviceIdRequest;
import com.smartrek.requests.WhereToGoRequest;
import com.smartrek.ui.EditAddress;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.ui.overlays.EventOverlay;
import com.smartrek.ui.overlays.OverlayCallback;
import com.smartrek.ui.overlays.POIActionOverlay;
import com.smartrek.ui.overlays.PointOverlay;
import com.smartrek.ui.timelayout.TimeColumn;
import com.smartrek.utils.Cache;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Geocoding;
import com.smartrek.utils.Geocoding.Address;
import com.smartrek.utils.HTTP;
import com.smartrek.utils.Misc;
import com.smartrek.utils.Preferences;
import com.smartrek.utils.RouteRect;
import com.smartrek.utils.SessionM;
import com.smartrek.utils.SmartrekTileProvider;

public final class LandingActivity2 extends FragmentActivity implements SensorEventListener { 
    
    private static final double mapZoomVerticalOffset = 0.25;

    public static final String LAT = "lat";
    
    public static final String LON = "lon";
    
    public static final String MSG = "msg";
    
    public static final boolean ENABLED = true;
    
    public static final String NO_TRIPS = "No Upcoming Trip";
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
    PointOverlay myPointOverlay;
    
    private LocationManager locationManager;

    private LocationListener locationListener;
    
    private Location lastLocation;
    
    private AtomicBoolean mapRezoom = new AtomicBoolean(true);
    
    private AtomicBoolean mapRecenter = new AtomicBoolean();
    
    private AtomicBoolean mapRefresh = new AtomicBoolean(true);
    
    private AtomicBoolean mapAlertAvailability = new AtomicBoolean(true);
    
    private AtomicInteger mapCenterLat = new AtomicInteger();
    
    private AtomicInteger mapCenterLon = new AtomicInteger();
    
    private SensorManager mSensorManager;
    
    public static Sensor accelerometer;
    public static Sensor magnetometer;
    
    private List<String> searchAddresses = new ArrayList<String>();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing2);
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
        final MapView mapView = (MapView) findViewById(R.id.mapview);
        Misc.disableHardwareAcceleration(mapView);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setTileSource(new SmartrekTileProvider());
        bindMapFunctions(mapView);
        RouteActivity.setViewToNorthAmerica(mapView);
        
        final AutoCompleteTextView searchBox = (AutoCompleteTextView) findViewById(R.id.search_box);
        refreshSearchAutoCompleteData();
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                final String addrInput = v.getText().toString();
                boolean handled = StringUtils.isNotBlank(addrInput);
                if(handled){
                    searchAddress(addrInput, false);
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return handled;
            }
        });
        final View searchBoxClear = findViewById(R.id.search_box_clear);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchBoxClear.setVisibility(StringUtils.isBlank(s)?View.GONE:View.VISIBLE); 
                final String addrInput = s.toString();
                if(StringUtils.isNotBlank(addrInput)) {
                	AsyncTask<Void, Void, List<String>> searchPoiTask = new AsyncTask<Void, Void, List<String>>(){
        				@Override
        				protected List<String> doInBackground(Void... params) {
        					List<String> addresses = new ArrayList<String>();
        					try {
        						addresses = Geocoding.searchPoi(addrInput, true);
        					}
        					catch(Exception e) {
        						Log.e("LandingActivity2", "search error!");
        					}
        					return addresses;
        				}
        				
        				@Override
        				protected void onPostExecute(List<String> addresses) {
        					searchAddresses.clear();
        					searchAddresses.addAll(addresses);
        					refreshSearchAutoCompleteData();
        				}
                	};
                	Misc.parallelExecute(searchPoiTask); 
                }
                else {
                	searchAddresses.clear();
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        searchBoxClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBox.setText("");
            }
        });
        String intentAddress = getIntentAddress(getIntent());
        boolean hasIntentAddr = StringUtils.isNotBlank(intentAddress); 
        mapRezoom.set(!hasIntentAddr);
        if(hasIntentAddr){
            searchBox.setText(intentAddress);
            searchAddress(intentAddress, true);
        }
        
        LandingActivity.initializeIfNeccessary(this, new Runnable() {
            @Override
            public void run() {
                updateDeviceId();
            }
        });
        
        final IMapController mc = mapView.getController();
        
        locationListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location location) {
//              LA lat-lon
//                location = new Location(location.getProvider());
//                location.setLatitude(34.0291747);
//                location.setLongitude(-118.2734106);
                if (ValidationActivity.isBetterLocation(location, lastLocation)) {
                    lastLocation = location;
                    final boolean refresh = mapRefresh.getAndSet(false);
                    final boolean alertAvailability = mapAlertAvailability.getAndSet(false);
                    final boolean rezoom = mapRezoom.getAndSet(false);
                    final double lat = location.getLatitude();
                    final double lon = location.getLongitude();
                    refreshMyLocation(lat, lon);
                    if(mapRecenter.getAndSet(false)){
                        if(myPointOverlay != null){
                            GeoPoint loc = myPointOverlay.getLocation();
                            int latE6 = loc.getLatitudeE6();
                            int lonE6 = loc.getLongitudeE6();
                            mc.animateTo(new GeoPoint(latE6, lonE6));
                            mapCenterLat.set(latE6);
                            mapCenterLon.set(lonE6);
                        }
                    }
                    LandingActivity.initializeIfNeccessary(LandingActivity2.this, new Runnable() {
                        @Override
                        public void run() {
                            if(refresh){
                                refreshBulbPOIs(lat , lon, rezoom);
                                refreshStarredPOIs();
                                refreshCobranding(lat, lon, alertAvailability);
                            }
                        }
                    });
                }
            }
            @Override
            public void onProviderDisabled(String provider) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }
            @Override
            public void onStatusChanged(String provider, int status,
                    Bundle extras) {
            }
        };
        
        TextView nextTripInfo = (TextView) findViewById(R.id.next_trip_info);
        nextTripInfo.setSelected(true);
        nextTripInfo.setText(NO_TRIPS);
        
        View centerMapIcon = findViewById(R.id.center_map_icon);
        centerMapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IGeoPoint mapCenter = mapView.getMapCenter();
                int latE6 = mapCenter.getLatitudeE6();
                int lonE6 = mapCenter.getLongitudeE6();
                int lastLatE6 = mapCenterLat.get();
                int lastLonE6 = mapCenterLon.get();
                double threshold = 0.00001;
                if(Math.abs(latE6 - lastLatE6) / Double.valueOf(lastLatE6) < threshold
                        && Math.abs(lonE6 - lastLonE6) / Double.valueOf(lastLonE6) < threshold){
                    if(mapView.getZoomLevel() == ValidationActivity.DEFAULT_ZOOM_LEVEL){
                        zoomMapToFitBulbPOIs();
                    }else{
                        mc.setZoom(ValidationActivity.DEFAULT_ZOOM_LEVEL);
                    }
                }else{
                    lastLocation = null;
                    mapRecenter.set(true);
                    prepareGPS();
                }
            }
        });
        
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        View drawerIcon = findViewById(R.id.drawer_menu_icon);
        drawerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(findViewById(R.id.left_drawer));
            }
        });
        TextView rewardsMenu = (TextView) findViewById(R.id.dashboard);
        rewardsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMenu.onMenuItemSelected(LandingActivity2.this, 0, v.getId());
            }
        });
        TextView shareMenu = (TextView) findViewById(R.id.share_menu);
        shareMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	MainMenu.onMenuItemSelected(LandingActivity2.this, 0, v.getId());
            }
        });
        TextView feedbackMenu = (TextView) findViewById(R.id.feedback_menu);
        feedbackMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedbackDialog d = new FeedbackDialog(LandingActivity2.this);
                d.show();
            }
        });
        TextView settingsMenu = (TextView) findViewById(R.id.map_display_options);
        settingsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMenu.onMenuItemSelected(LandingActivity2.this, 0, v.getId());
            }
        });
        settingsMenu.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainMenu.onMenuItemSelected(LandingActivity2.this, 0, R.id.debug_options);
                return true;
            }                
        });
        TextView logoutMenu = (TextView) findViewById(R.id.logout_option);
        logoutMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMenu.onMenuItemSelected(LandingActivity2.this, 0, v.getId());
            }
        });
        
        final View activityRootView = findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mapView.postInvalidate();
             }
        });
        
        TextView tripAddr = (TextView) findViewById(R.id.trip_address);
        TextView tripDetails = (TextView) findViewById(R.id.trip_details);
        TextView getGoingBtn = (TextView) findViewById(R.id.get_going_button);
        getGoingBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Reservation reserv = (Reservation) findViewById(R.id.trip_panel).getTag();
                Intent intent = new Intent(LandingActivity2.this, RouteActivity.class);
                intent.putExtra("route", reserv.getRoute());
                intent.putExtra("reservation", reserv);
                startActivity(intent);
                findViewById(R.id.trip_panel).setVisibility(View.GONE);
                relayoutIcons();
            }
        });
        TextView rescheBtn = (TextView) findViewById(R.id.reschedule_button);
        rescheBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Reservation reserv = (Reservation) findViewById(R.id.trip_panel).getTag();
                final String addr = reserv.getDestinationAddress();
                AsyncTask<Void, Void, GeoPoint> geoCodeTask = new AsyncTask<Void, Void, GeoPoint>(){
                    @Override
                    protected GeoPoint doInBackground(Void... params) {
                        GeoPoint gp = null;
                        try {
                            List<Address> addrs = Geocoding.lookup(LandingActivity2.this, addr);
                            for (Address a : addrs) {
                                gp = new GeoPoint(a.getLatitude(), a.getLongitude());
                                break;
                            }
                        }
                        catch (Exception e) {
                        }
                        return gp;
                    }
                    @Override
                    protected void onPostExecute(GeoPoint gp) {
                        if(gp != null){
                            Intent intent = new Intent(LandingActivity2.this, RouteActivity.class);
                            intent.putExtra(RouteActivity.CURRENT_LOCATION, true);
                            Bundle extras = new Bundle();
                            extras.putLong(RouteActivity.RESCHEDULE_RESERVATION_ID, reserv.getRid());
                            extras.putString("originAddr", EditAddress.CURRENT_LOCATION);
                            extras.putParcelable(RouteActivity.ORIGIN_COORD, new GeoPoint(0, 0 ));
                            extras.putString("destAddr", addr);
                            extras.putParcelable(RouteActivity.DEST_COORD, gp);
                            intent.putExtras(extras);
                            startActivity(intent);
                            findViewById(R.id.trip_panel).setVisibility(View.GONE);
                            relayoutIcons();
                        }
                    }
                };
                Misc.parallelExecute(geoCodeTask);
            }
        });
        
        final View balloonView = (View) findViewById(R.id.balloon_panel);
        balloonView.findViewById(R.id.saveOrDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String lbl = ((EditText)balloonView.findViewById(R.id.label)).getText().toString();
                final String addr = ((TextView)balloonView.findViewById(R.id.address)).getText().toString();
                final BalloonModel model = (BalloonModel) balloonView.getTag();
                final boolean isSave = model.id == 0;
                AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>(){
                    @Override
                    protected Integer doInBackground(Void... params) {
                        Integer id = null;
                        Request req = null;
                        User user = User.getCurrentUser(LandingActivity2.this);
                        try {
                            if (isSave){
                                FavoriteAddressAddRequest request = new FavoriteAddressAddRequest(
                                    user, lbl, addr, model.lat, model.lon);
                                req = request;
                                id = request.execute(LandingActivity2.this);
                            }
                            else {
                            	FavoriteAddressDeleteRequest request = new FavoriteAddressDeleteRequest(
                                        new AddressLinkRequest(user).execute(LandingActivity2.this), user, model.id);
                            	req = request;
                                request.execute(LandingActivity2.this);
                            }
                        }
                        catch (Exception e) {
                            ehs.registerException(e, "[" + (req==null?"":req.getUrl()) + "]\n" + e.getMessage());
                        }
                        return id;
                    }
                    protected void onPostExecute(Integer id) {
                        if (ehs.hasExceptions()) {
                            ehs.reportExceptions();
                        }
                        else {
                            refreshStarredPOIs();
                            if(isSave){
                                removePOIMarker(mapView);
                                balloonView.setVisibility(View.VISIBLE);
                                hideBottomBar();
                                model.id = id;
                                ImageView saveOrDelView = (ImageView)balloonView.findViewById(R.id.saveOrDelete);
                                saveOrDelView.setImageResource(R.drawable.delete_star_poi);
                            }else{
                                model.id = 0;
                                refreshPOIMarker(mapView, model.lat, model.lon, addr, lbl);
                            }
                        }
                    }
               };
               Misc.parallelExecute(task);
            }
        });
        balloonView.findViewById(R.id.get_going).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BalloonModel model = (BalloonModel) balloonView.getTag();
                startRouteActivity(model.address, model.geopoint);
                hideStarredBalloon();
                hideBulbBalloon();
                removePOIMarker(mapView);
                resizeMap(true);
                final String lbl = ((EditText)balloonView.findViewById(R.id.label)).getText().toString();
                if(model.id == 0 && StringUtils.isNotBlank(lbl)){
                    final String addr = ((TextView)balloonView.findViewById(R.id.address)).getText().toString();
                    AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>(){
                        @Override
                        protected Integer doInBackground(Void... params) {
                            Integer id = null;
                            User user = User.getCurrentUser(LandingActivity2.this);
                            try {
                                FavoriteAddressAddRequest request = new FavoriteAddressAddRequest(
                                    user, lbl, addr, model.lat, model.lon);
                                id = request.execute(LandingActivity2.this);
                            }
                            catch (Exception e) {
                            }
                            return id;
                        }
                        protected void onPostExecute(Integer id) {
                            if (!ehs.hasExceptions()) {
                                refreshStarredPOIs();
                            }
                        }
                   };
                   Misc.parallelExecute(task);
                }
            }
        });
        
        balloonView.findViewById(R.id.label).setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus) {
					final String lbl = ((EditText)balloonView.findViewById(R.id.label)).getText().toString();
	                final String addr = ((TextView)balloonView.findViewById(R.id.address)).getText().toString();
					final BalloonModel model = (BalloonModel) balloonView.getTag();
                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {
                            Request req = null;
                            User user = User.getCurrentUser(LandingActivity2.this);
                            try {
                            	if (model.id > 0) {
	                                FavoriteAddressUpdateRequest request = new FavoriteAddressUpdateRequest(
	                                    new AddressLinkRequest(user).execute(LandingActivity2.this),
	                                        model.id, user, lbl, addr, model.lat, model.lon);
	                                    req = request;
	                                    request.execute(LandingActivity2.this);
                            	}
                            }
                            catch (Exception e) {
                                ehs.registerException(e, "[" + (req==null?"":req.getUrl()) + "]\n" + e.getMessage());
                            }
                            return null;
                        }
                        protected void onPostExecute(Void result) {
                            if (ehs.hasExceptions()) {
                                ehs.reportExceptions();
                            }
                            else {
                                refreshStarredPOIs();
                            }
                        }
                    };
                    Misc.parallelExecute(task);
                    removePOIMarker(mapView);
		        }
		    }
	    });
        
        final TextView onTheWayBtn = (TextView) findViewById(R.id.on_the_way_button);
        onTheWayBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent otwIntent = new Intent(LandingActivity2.this, RouteActivity.class);
                otwIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                otwIntent.putExtra(RouteActivity.LAT, (Double) onTheWayBtn.getTag(R.id.on_the_way_lat));
                otwIntent.putExtra(RouteActivity.LON, (Double) onTheWayBtn.getTag(R.id.on_the_way_lon));
                otwIntent.putExtra(RouteActivity.MSG, (String) onTheWayBtn.getTag(R.id.on_the_way_msg));
                startActivity(otwIntent);
                findViewById(R.id.on_the_way_icon).setVisibility(View.INVISIBLE);
                findViewById(R.id.on_the_way_panel).setVisibility(View.GONE);
            }
        });
        
        final View carIcon = findViewById(R.id.car_icon);
        final View tripPanel = findViewById(R.id.trip_panel);
        final View onTheWayPanel = findViewById(R.id.on_the_way_panel); 
        OnClickListener tripPanelToggler = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(carIcon.getVisibility() == View.VISIBLE){
                    tripPanel.setVisibility(tripPanel.getVisibility() == View.GONE?View.VISIBLE:View.GONE);
                    onTheWayPanel.setVisibility(View.GONE);
                    relayoutIcons();
                }
            }
        };
        carIcon.setOnClickListener(tripPanelToggler);
        nextTripInfo.setOnClickListener(tripPanelToggler);
        
        final View onTheWayIcon = findViewById(R.id.on_the_way_icon);
        onTheWayIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onTheWayIcon.getVisibility() == View.VISIBLE){
                    onTheWayPanel.setVisibility(onTheWayPanel.getVisibility() == View.GONE?View.VISIBLE:View.GONE);
                    tripPanel.setVisibility(View.GONE);
                    relayoutIcons();
                }
            }
        });
        
        TextView osmCredit = (TextView) findViewById(R.id.osm_credit);
        RelativeLayout.LayoutParams osmCreditLp = (RelativeLayout.LayoutParams) osmCredit.getLayoutParams();
        osmCreditLp.bottomMargin = Dimension.dpToPx(48, getResources().getDisplayMetrics());
        osmCredit.setLayoutParams(osmCreditLp);
        
        OnClickListener noopClick = new OnClickListener() {
            @Override
            public void onClick(View v) {}
        };
        findViewById(R.id.header_panel).setOnClickListener(noopClick);
        findViewById(R.id.bottom_bar).setOnClickListener(noopClick);
        findViewById(R.id.left_drawer).setOnClickListener(noopClick);
        tripPanel.setOnClickListener(noopClick);
        onTheWayPanel.setOnClickListener(noopClick);
        
        scheduleNextTripInfoUpdates();
        
        AssetManager assets = getAssets();
//        Font.setTypeface(Font.getBold(assets), tripAddr);
        Font.setTypeface(Font.getLight(assets), tripAddr, osmCredit, searchBox, nextTripInfo,
            rewardsMenu, shareMenu, feedbackMenu, settingsMenu, logoutMenu,
            tripDetails, getGoingBtn, rescheBtn, (TextView)findViewById(R.id.header_text),
            (TextView)findViewById(R.id.menu_bottom_text),
            (TextView)findViewById(R.id.on_the_way_msg), onTheWayBtn);
    }
    
    private void relayoutIcons(){
        View mapView = findViewById(R.id.mapview);
        Boolean collapsedTag = (Boolean) mapView.getTag();
        boolean collapsed = collapsedTag == null?true:collapsedTag.booleanValue();
        View tripPanel = findViewById(R.id.trip_panel);
        View onTheWayPanel = findViewById(R.id.on_the_way_panel);
        View balloonView = (View) findViewById(R.id.balloon_panel);
        int bottomMargin = tripPanel.getVisibility() == View.GONE && onTheWayPanel.getVisibility() == View.GONE
            && balloonView.getVisibility() == View.GONE ?(collapsed?53:10):135;
        View centerMapIcon = findViewById(R.id.center_map_icon);
        RelativeLayout.LayoutParams centerMapIconLp = (RelativeLayout.LayoutParams) centerMapIcon.getLayoutParams();
        centerMapIconLp.bottomMargin = Dimension.dpToPx(bottomMargin, getResources().getDisplayMetrics());
        centerMapIcon.setLayoutParams(centerMapIconLp);
        View menuIcon = findViewById(R.id.drawer_menu_icon);
        FrameLayout.LayoutParams menuIconLp = (FrameLayout.LayoutParams) menuIcon.getLayoutParams();
        menuIconLp.bottomMargin = Dimension.dpToPx(bottomMargin, getResources().getDisplayMetrics());
        menuIcon.setLayoutParams(menuIconLp);
        View openedMenuIcon = findViewById(R.id.drawer_menu_icon_opened);
        LinearLayout.LayoutParams openedMenuIconLp = (LinearLayout.LayoutParams) openedMenuIcon.getLayoutParams();
        openedMenuIconLp.bottomMargin = Dimension.dpToPx(bottomMargin, getResources().getDisplayMetrics());
        openedMenuIcon.setLayoutParams(openedMenuIconLp);
    }
    
    private void searchAddress(final String addr, final boolean zoomIn){
        AsyncTask<Void, Void, GeoPoint> task = new AsyncTask<Void, Void, GeoPoint>(){
            @Override
            protected GeoPoint doInBackground(Void... params) {
                GeoPoint gp = null;
                try {
                    List<Address> addrs = Geocoding.lookup(LandingActivity2.this, addr);
                    for (Address a : addrs) {
                        gp = new GeoPoint(a.getLatitude(), a.getLongitude());
                        break;
                    }
                }
                catch (Exception e) {
                }
                return gp;
            }
            @Override
            protected void onPostExecute(GeoPoint gp) {
                if(gp != null){
                    DebugOptionsActivity.addRecentAddress(LandingActivity2.this, addr);
                    refreshSearchAutoCompleteData();
                    final MapView mapView = (MapView) findViewById(R.id.mapview);
                    ReverseGeocodingTask task = new ReverseGeocodingTask(LandingActivity2.this, 
                            gp.getLatitude(), gp.getLongitude()){
                        @Override
                        protected void onPostExecute(String result) {
                            refreshPOIMarker(mapView, lat, lon, result, "");
                        }
                    };
                    Misc.parallelExecute(task);
                    IMapController mc = mapView.getController();
                    if(zoomIn){
                        mc.setZoom(ValidationActivity.DEFAULT_ZOOM_LEVEL);
                        mc.setCenter(gp);
                    }else{
                        mc.animateTo(gp);
                    }
                }
            }
        };
        Misc.parallelExecute(task);
    }
    
    private void refreshSearchAutoCompleteData(){
        AutoCompleteTextView searchBox = (AutoCompleteTextView) findViewById(R.id.search_box);
        List<String> searchData = new ArrayList<String>();
        @SuppressWarnings("unchecked")
        List<String> starred = (List<String>) searchBox.getTag(R.id.starred_addresses);
        if(starred != null){
            searchData.addAll(starred);
        }
        searchData.addAll(DebugOptionsActivity.getRecentAddresses(this));
        @SuppressWarnings("unchecked")
        List<String> whereTo = (List<String>) searchBox.getTag(R.id.where_to_addresses);
        if(whereTo != null){
            searchData.addAll(whereTo);
        }
        
        searchData.addAll(searchAddresses);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_dropdown_item_1line,
            new ArrayList<String>(new LinkedHashSet<String>(searchAddresses)));
        searchBox.setAdapter(adapter);
    }
    
    private static class BalloonModel {
        
        int id;
        
        double lat;
        
        double lon;
        
        String address;
        
        GeoPoint geopoint;
        
    }
    
    private void updateDeviceId(){
        SharedPreferences globalPrefs = Preferences.getGlobalPreferences(this);
        final String gcmRegistrationId = globalPrefs.getString(Preferences.Global.GCM_REG_ID, "");
        final User currentUser = User.getCurrentUser(this);
        if(!gcmRegistrationId.equals(currentUser.getDeviceId())){
            currentUser.setDeviceId(gcmRegistrationId);
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        new UpdateDeviceIdRequest().execute(currentUser.getId(), gcmRegistrationId,
                            currentUser.getUsername(), currentUser.getPassword(), LandingActivity2.this);
                    }
                    catch (Exception e) {}
                    return null;
                }
            };
            Misc.parallelExecute(task);
        }
    }
    
    private static final String TRIP_INFO_UPDATES = "TRIP_INFO_UPDATES"; 
    
    private void scheduleNextTripInfoUpdates(){
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 6000, 
            PendingIntent.getBroadcast(this, 0, new Intent(TRIP_INFO_UPDATES), PendingIntent.FLAG_UPDATE_CURRENT));
    }
    
    private BroadcastReceiver tripInfoUpdater = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LandingActivity.initializeIfNeccessary(context, new Runnable() {
                @Override
                public void run() {
                    refreshTripsInfo();
                }
            });
        }
    };
    
    public static final String ON_THE_WAY_NOTICE = "ON_THE_WAY_NOTICE"; 
    
    private BroadcastReceiver onTheWayNotifier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra(MSG);
            ((TextView)findViewById(R.id.on_the_way_msg)).setText(msg);
            View otwButton = findViewById(R.id.on_the_way_button);
            otwButton.setTag(R.id.on_the_way_msg, msg);
            otwButton.setTag(R.id.on_the_way_lat, intent.getDoubleExtra(LAT, 0));
            otwButton.setTag(R.id.on_the_way_lon, intent.getDoubleExtra(LON, 0));
            findViewById(R.id.on_the_way_icon).setVisibility(View.VISIBLE);
        }
    };
    
    private void refreshMyLocation(double lat, double lon){
        MapView mapView = (MapView)findViewById(R.id.mapview);
        if(myPointOverlay == null){
            myPointOverlay = new PointOverlay(LandingActivity2.this, 0, 0, R.drawable.landing_page_current_location);
            myPointOverlay.setColor(0xCC2020DF);
            mapView.getOverlays().add(myPointOverlay);
        }
        myPointOverlay.setLocation((float) lat, (float) lon);
        mapView.postInvalidate();
    }
    
    private void prepareGPS(){
        closeGPS();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                15000, 5, locationListener);
        }
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }
    
    private void closeGPS(){
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(tripInfoUpdater, new IntentFilter(TRIP_INFO_UPDATES));
        registerReceiver(onTheWayNotifier, new IntentFilter(ON_THE_WAY_NOTICE));
        SessionM.onActivityResume(this);
        sendBroadcast(new Intent(TRIP_INFO_UPDATES));
        mapRefresh.set(true);
        prepareGPS();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        SessionM.onActivityStart(this);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        SessionM.onActivityStop(this);
    }
    
    @Override
    protected void onPause() {
      unregisterReceiver(tripInfoUpdater);
      unregisterReceiver(onTheWayNotifier);
      SessionM.onActivityPause(this);
      closeGPS();
      super.onPause();
      mSensorManager.unregisterListener(this, accelerometer);
      mSensorManager.unregisterListener(this, magnetometer);
    } 
    
    private void refreshTripsInfo(){
        AsyncTask<Void, Void, List<Reservation>> tripTask = new AsyncTask<Void, Void, List<Reservation>>(){
            @Override
            protected List<Reservation> doInBackground(Void... params) {
                User user = User.getCurrentUser(LandingActivity2.this);
                List<Reservation> reservations= Collections.emptyList();
                ReservationListFetchRequest resReq = new ReservationListFetchRequest(user);
                resReq.invalidateCache(LandingActivity2.this);
                FavoriteAddressFetchRequest addReq = new FavoriteAddressFetchRequest(user);
                addReq.invalidateCache(LandingActivity2.this);
                try {
                    List<com.smartrek.models.Address> addresses = addReq.execute(LandingActivity2.this);
                    reservations = resReq.execute(LandingActivity2.this);
                    for(Reservation r:reservations){
                        if(r.getOriginName() == null){
                            for (com.smartrek.models.Address a : addresses) {
                                if(a.getAddress().equals(r.getOriginAddress())){
                                    r.setOriginName(a.getName());
                                    break;
                                }
                            }
                        }
                        if(r.getDestinationName() == null){
                            for (com.smartrek.models.Address a : addresses) {
                                if(a.getAddress().equals(r.getDestinationAddress())){
                                    r.setDestinationName(a.getName());
                                    break;
                                }
                            }
                        }
                    }
                    Collections.sort(reservations, Reservation.orderByDepartureTime());
                }
                catch (NullPointerException e){}
                catch (Exception e) {
                    ehs.registerException(e, "[" + resReq.getURL() + ", " + addReq.getURL() + "]\n" + e.getMessage());
                }
                return reservations;
            }
            @Override
            protected void onPostExecute(List<Reservation> reservations) {
                if (ehs.hasExceptions()) { 
                    //ehs.reportExceptions();
                } 
                else{
                    View tripPanel = findViewById(R.id.trip_panel);
                    TextView nextTripInfo = (TextView) findViewById(R.id.next_trip_info);
                    View carIcon = findViewById(R.id.car_icon);
                    if(reservations == null || reservations.isEmpty()){
                        tripPanel.setTag(null);
                        nextTripInfo.setText(NO_TRIPS);
                        tripPanel.setVisibility(View.GONE);
                        carIcon.setVisibility(View.INVISIBLE);
                    }else{
                        Reservation reserv = reservations.get(0);
                        tripPanel.setTag(reserv);
                        TextView tripAddr = (TextView) findViewById(R.id.trip_address);
                        tripAddr.setText(reserv.getDestinationAddress());
                        TextView tripDetails = (TextView) findViewById(R.id.trip_details);
                        tripDetails.setText("Duration: " + TimeColumn.getFormattedDuration(reserv.getDuration())
                            + "·mPOINTS: " + reserv.getMpoint());
                        tripDetails.setSelected(true);
                        int getGoingBtnVis = View.GONE;
                        int rescheBtnVis = View.VISIBLE;
                        int carIconVis = View.VISIBLE;
                        String nextTripInfoText;
                        long departureTimeUtc = reserv.getDepartureTimeUtc();
                        long timeUntilDepart = departureTimeUtc - System.currentTimeMillis();
                        if(reserv.isEligibleTrip()){
                            nextTripInfoText = "Get Going";
                            getGoingBtnVis = View.VISIBLE;
                            rescheBtnVis = View.GONE;
                        }else if(timeUntilDepart > 60 * 60 * 1000L){
                            nextTripInfoText = "Next Trip at "
                                + TimeColumn.formatTime(departureTimeUtc, reserv.getRoute().getTimezoneOffset());
                        }else if(timeUntilDepart > Reservation.GRACE_INTERVAL){
                            nextTripInfoText = "Next Trip in "
                                + TimeColumn.getFormattedDuration((int)timeUntilDepart / 1000);
                        }else if(timeUntilDepart > -2 * 60 * 60 * 1000L){
                            nextTripInfoText = "Trip has expired";
                        }else{
                            nextTripInfoText = NO_TRIPS;
                            carIconVis = View.INVISIBLE;
                            tripPanel.setVisibility(View.GONE);
                        }
                        nextTripInfo.setText(nextTripInfoText);
                        TextView getGoingBtn = (TextView) findViewById(R.id.get_going_button);
                        getGoingBtn.setVisibility(getGoingBtnVis);
                        TextView rescheBtn = (TextView) findViewById(R.id.reschedule_button);
                        rescheBtn.setVisibility(rescheBtnVis);
                        carIcon.setVisibility(carIconVis);
                    }
                }
            }
        };
        Misc.parallelExecute(tripTask);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String intentAddress = getIntentAddress(intent);
        if(intent.getBooleanExtra(LandingActivity.LOGOUT, false)){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }else if(StringUtils.isNotBlank(intentAddress)){
            TextView searchBox = (TextView) findViewById(R.id.search_box);
            searchBox.setText(intentAddress);
            searchAddress(intentAddress, true);
        }
    }
    
    private String getIntentAddress(Intent intent){
        String address = null;
        Uri uri = intent.getData();
        if(uri != null){
            address = Uri.decode(StringUtils.substringAfterLast(uri.toString(), "?q="));
        }
        return address;
    }
    
    Typeface boldFont;
    
    Typeface lightFont;
    
    private void initFontsIfNecessary(){
        if(boldFont == null){
            boldFont = Font.getBold(getAssets());
        }
        if(lightFont == null){
            lightFont = Font.getLight(getAssets());
        }
    }
    
    private AtomicBoolean poiTapThrottle = new AtomicBoolean();
    
    private void startRouteActivity(String address, GeoPoint gp){
        if(!poiTapThrottle.get()){
            poiTapThrottle.set(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    poiTapThrottle.set(false);
                }
            }, 500);
            Intent intent = new Intent(this, RouteActivity.class);
            intent.putExtra(RouteActivity.CURRENT_LOCATION, true /*false*/);
            Bundle extras = new Bundle();
            extras.putString("originAddr", EditAddress.CURRENT_LOCATION);
            extras.putParcelable(RouteActivity.ORIGIN_COORD, 
                new GeoPoint(0, 0 /*34.0291747, -118.2734106*/));
            extras.putString("destAddr", address);
            extras.putParcelable(RouteActivity.DEST_COORD, gp);
            intent.putExtras(extras);
            startActivity(intent);
        }
    }
    
    private void refreshStarredPOIs(){
        AsyncTask<Void, Void, List<com.smartrek.models.Address>> task = new AsyncTask<Void, Void, List<com.smartrek.models.Address>>(){
            @Override
            protected List<com.smartrek.models.Address> doInBackground(
                    Void... params) {
                List<com.smartrek.models.Address> addrs = Collections.emptyList();
                FavoriteAddressFetchRequest request = new FavoriteAddressFetchRequest(
                        User.getCurrentUser(LandingActivity2.this));
                try {
                    request.invalidateCache(LandingActivity2.this);
                    addrs = request.execute(LandingActivity2.this);
                }
                catch (Exception e) {
                    ehs.registerException(e, "[" + request.getURL() + "]\n" + e.getMessage());
                }
                return addrs;
            }
            @Override
            protected void onPostExecute(
                    List<com.smartrek.models.Address> result) {
                if (ehs.hasExceptions()) {
                    //ehs.reportExceptions();
                }
                else {
                    List<String> addrList = new ArrayList<String>();
                    final MapView mapView = (MapView) findViewById(R.id.mapview);
                    List<Overlay> overlays = mapView.getOverlays();
                    List<Overlay> otherOverlays = new ArrayList<Overlay>();
                    for (Overlay overlay : overlays) {
                        boolean isOther;
                        if(overlay instanceof POIActionOverlay){
                            POIActionOverlay poiOverlay = (POIActionOverlay)overlay;
                            isOther = poiOverlay.getMarker() != R.drawable.star_poi;
                        }else{
                            isOther = true;
                        }
                        if(isOther){
                            otherOverlays.add(overlay);
                        }
                    }
                    overlays.clear();
                    overlays.addAll(otherOverlays);
                    if (result != null && result.size() > 0) {
                        initFontsIfNecessary();
                        for(final com.smartrek.models.Address a : result){
                            final GeoPoint gp = new GeoPoint(a.getLatitude(), a.getLongitude());
                            final POIActionOverlay star = new POIActionOverlay(mapView, 
                                gp, 
                                boldFont, lightFont, a.getAddress(), a.getName(), 
                                R.drawable.star_poi);
                            star.setAid(a.getId());
                            star.setBalloonOffsetY(Dimension.dpToPx(-17, getResources().getDisplayMetrics()));
                            star.setCallback(new OverlayCallback() {
                                @Override
                                public boolean onTap(int index) {
                                    hideStarredBalloon();
                                    hideBulbBalloon();
                                    removePOIMarker(mapView); 
                                    BalloonModel model = new BalloonModel();
                                    model.id = a.getId();
                                    model.lat = a.getLatitude();
                                    model.lon = a.getLongitude();
                                    model.address = a.getAddress();
                                    model.geopoint = gp;
                                    final View balloonView = (View) findViewById(R.id.balloon_panel);
                                    TextView addrView = ((TextView)balloonView.findViewById(R.id.address));
                                    addrView.setText(a.getAddress());
                                    TextView labelView = ((TextView)balloonView.findViewById(R.id.label));
                                    labelView.setText(a.getName());
                                    ImageView startImg = (ImageView) balloonView.findViewById(R.id.saveOrDelete);
                                    startImg.setImageResource(R.drawable.delete_star_poi);
                                    balloonView.setTag(model);
                                    balloonView.setVisibility(View.VISIBLE);
                                    hideBottomBar();
                                    mapView.postInvalidate();
                                    relayoutIcons();
                                    return true;
                                }
                                @Override
                                public boolean onLongPress(int index, OverlayItem item) {
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
                                public boolean onBalloonTap(int index, OverlayItem item) {
                                    return false;
                                }
                            });
                            overlays.add(star);
                            star.showOverlay();
                            addrList.add(a.getAddress());
                        }
                    }
                    mapView.postInvalidate();
                    findViewById(R.id.search_box).setTag(R.id.starred_addresses, addrList);
                    refreshSearchAutoCompleteData();
                }
            }
        };
        Misc.parallelExecute(task);
    }
    
    private boolean hideStarredBalloon(){
        boolean handled = false;
        MapView mapView = (MapView) findViewById(R.id.mapview);
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay instanceof POIActionOverlay){
                POIActionOverlay poiOverlay = (POIActionOverlay)overlay;
                if(poiOverlay.getMarker() == R.drawable.star_poi){
                    if(isBalloonPanelVisible()){
                        hideBalloonPanel();
                        handled = true;
                    }
                }
            }
        }
        return handled;
    }
    
    private boolean hideBulbBalloon(){
        boolean handled = false;
        MapView mapView = (MapView) findViewById(R.id.mapview);
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay instanceof POIActionOverlay){
                POIActionOverlay poiOverlay = (POIActionOverlay)overlay;
                if(poiOverlay.getMarker() == R.drawable.bulb_poi){
                    if(isBalloonPanelVisible()){
                        hideBalloonPanel();
                        handled = true;
                    }
                }
            }
        }
        return handled;
    }
    
    private void refreshCobranding(final double lat, final double lon, 
            final boolean alertAvailability){
        AsyncTask<Void, Void, City> checkCityAvailability = new AsyncTask<Void, Void, City>(){
            @Override
            protected City doInBackground(Void... params) {
                City result;
                try{
                    CityRequest req = new CityRequest(lat, lon);
                    req.invalidateCache(LandingActivity2.this);
                    result = req.execute(LandingActivity2.this);
                }catch(Throwable t){
                    result = null;
                }
                return result;
            }
            @Override
            protected void onPostExecute(City result) {
                final ImageView skylineView = (ImageView) findViewById(R.id.skyline_bg);
                final ImageView cityImgView = (ImageView) findViewById(R.id.city_logo);
                final View bottomText = findViewById(R.id.menu_bottom_text);
                final TextView headerText = (TextView)findViewById(R.id.header_text);
                if(result != null && StringUtils.isNotBlank(result.html)){
                    if(alertAvailability){
                        CharSequence msg = Html.fromHtml(result.html);
                        NotificationDialog2 dialog = new NotificationDialog2(LandingActivity2.this, msg);
                        dialog.setTitle("Notification");
                        try{
                            dialog.show();
                        }catch(Throwable t){}
                    }
                    skylineView.setImageResource(R.drawable.skyline);
                    cityImgView.setVisibility(View.GONE);
                    bottomText.setVisibility(View.GONE);
                    headerText.setVisibility(View.GONE);
                }else{
                    LoadImageTask skylineTask = new LoadImageTask(LandingActivity2.this, result.skyline) {
                        protected void onPostExecute(final Bitmap rs) {
                            if(rs != null){
                                skylineView.setImageBitmap(rs);
                            }else{
                                skylineView.setImageResource(R.drawable.skyline);
                            }
                        }
                    };
                    Misc.parallelExecute(skylineTask);
                    LoadImageTask logoTask = new LoadImageTask(LandingActivity2.this, result.logo) {
                        protected void onPostExecute(final Bitmap rs) {
                            if(rs != null){
                                cityImgView.setVisibility(View.VISIBLE);
                                cityImgView.setImageBitmap(rs);
                                bottomText.setVisibility(View.VISIBLE);
                            }else{
                                cityImgView.setVisibility(View.GONE);
                                bottomText.setVisibility(View.GONE);
                            }
                        }
                    };
                    Misc.parallelExecute(logoTask);
                    headerText.setVisibility(View.VISIBLE);
                    headerText.setText(result.name + " | " 
                        + Double.valueOf(result.temperature).intValue() + "°" 
                        + result.temperatureUnit);
                }
            }
        };
        Misc.parallelExecute(checkCityAvailability);
    }
    
    private RouteRect routeRect;
    
    private void zoomMapToFitBulbPOIs(){
        if(routeRect != null){
            MapView mapView = (MapView) findViewById(R.id.mapview);
            IMapController mc = mapView.getController();
            GeoPoint mid = routeRect.getMidPoint();
            int[] range = routeRect.getRange();
            mc.zoomToSpan(range[0], range[1]);
            mc.setCenter(mid);
        }
    }
    
    private void refreshBulbPOIs(final double lat, final double lon, final boolean rezoom){
        final User user = User.getCurrentUser(LandingActivity2.this);
        AsyncTask<Void, Void, List<com.smartrek.requests.WhereToGoRequest.Location>> task = 
                new AsyncTask<Void, Void, List<com.smartrek.requests.WhereToGoRequest.Location>>() {
            @Override
            protected List<com.smartrek.requests.WhereToGoRequest.Location> doInBackground(Void... params) {
                List<com.smartrek.requests.WhereToGoRequest.Location> locs = null;
                WhereToGoRequest req = new WhereToGoRequest(user, lat, lon);
                req.invalidateCache(LandingActivity2.this);
                try {
                    locs = req.execute(LandingActivity2.this);
                }
                catch (Exception e) {
                    ehs.registerException(e, "[" + req.getURL() + "]\n" + e.getMessage());
                }
                return locs;
            }
            @Override
            protected void onPostExecute(List<com.smartrek.requests.WhereToGoRequest.Location> locs) {
                if (ehs.hasExceptions()) {
                    //ehs.reportExceptions();
                }
                else {
                    MapView mapView = (MapView) findViewById(R.id.mapview);
                    List<Overlay> overlays = mapView.getOverlays();
                    List<Overlay> otherOverlays = new ArrayList<Overlay>();
                    for (Overlay overlay : overlays) {
                        boolean isOther;
                        if(overlay instanceof POIActionOverlay){
                            POIActionOverlay poiOverlay = (POIActionOverlay)overlay;
                            isOther = poiOverlay.getMarker() != R.drawable.bulb_poi;
                        }else{
                            isOther = true;
                        }
                        if(isOther){
                            otherOverlays.add(overlay);
                        }
                    }
                    overlays.clear();
                    overlays.addAll(otherOverlays);
                    IMapController mc = mapView.getController();
                    List<String> addrList = new ArrayList<String>();
                    if(locs.isEmpty()){
                        routeRect = null;
                        if(rezoom){
                            mc.setZoom(ValidationActivity.DEFAULT_ZOOM_LEVEL);
                            mc.setCenter(new GeoPoint(lat, lon));
                        }
                    }else{
                        drawBulbPOIs(mapView, locs);
                        List<GeoPoint> points = new ArrayList<GeoPoint>();
                        points.add(new GeoPoint(lat, lon));
                        for(com.smartrek.requests.WhereToGoRequest.Location l : locs){
                            points.add(new GeoPoint(l.lat, l.lon));
                        }
                        routeRect = new RouteRect(points, mapZoomVerticalOffset);
                        if(rezoom){
                            zoomMapToFitBulbPOIs();
                        }
                        for(com.smartrek.requests.WhereToGoRequest.Location l : locs){
                            addrList.add(l.addr);
                        }
                    }
                    mapView.postInvalidate();
                    findViewById(R.id.search_box).setTag(R.id.where_to_addresses, addrList);
                    refreshSearchAutoCompleteData();
                }
            }
        };
        Misc.parallelExecute(task);
    }
    
    private void bindMapFunctions(final MapView mapView){
        EventOverlay eventOverlay = new EventOverlay(this);
        eventOverlay.setActionListener(new EventOverlay.ActionListener() {
            @Override
            public void onLongPress(final double lat, final double lon) {
                ReverseGeocodingTask task = new ReverseGeocodingTask(LandingActivity2.this, lat, lon){
                    @Override
                    protected void onPostExecute(String result) {
                        refreshPOIMarker(mapView, lat, lon, result, "");
                    }
                };
                Misc.parallelExecute(task);
            }
            @Override
            public void onSingleTap() {
                boolean handledStarred = hideStarredBalloon();
                boolean handledBulb = hideBulbBalloon();
                boolean handledPOI = removePOIMarker(mapView);
                if(!handledStarred && !handledBulb && !handledPOI){
                    View tripPanel = findViewById(R.id.trip_panel);
                    View onTheWayPanel = findViewById(R.id.on_the_way_panel);
                    if(tripPanel.getVisibility() == View.VISIBLE || onTheWayPanel.getVisibility() == View.VISIBLE){
                        tripPanel.setVisibility(View.GONE);
                        onTheWayPanel.setVisibility(View.GONE);
                        relayoutIcons();
                    }else{
                        resizeMap(!isMapCollapsed());
                    }
                }else{
                    relayoutIcons();
                }
            }
        });
        mapView.getOverlays().add(eventOverlay);
    }
    
    private boolean isMapCollapsed(){
        View mapView = findViewById(R.id.mapview);
        Boolean collapsedTag = (Boolean) mapView.getTag();
        boolean collapsed = collapsedTag == null?true:collapsedTag.booleanValue();
        return collapsed;
    }
    
    private void resizeMap(boolean collapsed){
        View mapView = findViewById(R.id.mapview);
        mapView.setTag(collapsed);
        findViewById(R.id.header_panel).setVisibility(collapsed?View.VISIBLE:View.GONE);
        if(collapsed){
            findViewById(R.id.bottom_bar).setVisibility(View.VISIBLE);
        }else{
            hideBottomBar();
        }
        relayoutIcons();
    }
    
    private static abstract class ReverseGeocodingTask extends AsyncTask<Void, Void, String> {
        
        double lat;
        
        double lon;
        
        Context ctx;
        
        ReverseGeocodingTask(Context ctx, double lat, double lon){
        	this.ctx = ctx;
            this.lat = lat;
            this.lon = lon;
        }
        
        @Override
        protected String doInBackground(Void... params) {
            String address = null;
            try {
                address = Geocoding.lookup(ctx, lat, lon);
            }
            catch (Exception e) {
            }
            return address;
        }
        
    }
    
    private POIActionOverlay curMarker;
    
    private boolean removePOIMarker(MapView mapView){
        boolean handled = false;
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay == curMarker){
                if(isBalloonPanelVisible()){
                    hideBalloonPanel();
                    handled = true;
                }
                overlays.remove(overlay);
                mapView.postInvalidate();
                break;
            }
        }
        return handled;
    }
    
    private void refreshPOIMarker(final MapView mapView, final double lat, final double lon,
            final String address, String label){
        removePOIMarker(mapView);
        final GeoPoint gp = new GeoPoint(lat, lon);
        POIActionOverlay marker = new POIActionOverlay(mapView, 
            gp, Font.getBold(getAssets()), Font.getLight(getAssets()),
            address, label, R.drawable.marker_poi);
        marker.setCallback(new OverlayCallback() {
            @Override
            public boolean onTap(int index) {
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
                return false;
            }
        });
        List<Overlay> overlays = mapView.getOverlays();
        overlays.add(marker);
        marker.showOverlay();
        curMarker = marker;
        mapView.postInvalidate();
        BalloonModel model = new BalloonModel();
        model.lat = lat;
        model.lon = lon;
        model.address = address;
        model.geopoint = gp;
        final View balloonView = (View) findViewById(R.id.balloon_panel);
        TextView addrView = ((TextView)balloonView.findViewById(R.id.address));
        addrView.setText(address);
        TextView labelView = ((TextView)balloonView.findViewById(R.id.label));
        labelView.setText(label);
        ImageView saveOrDelView = (ImageView)balloonView.findViewById(R.id.saveOrDelete);
        saveOrDelView.setImageResource(R.drawable.save_star_poi);
        balloonView.setTag(model);
        balloonView.setVisibility(View.VISIBLE);
        hideBottomBar();
        relayoutIcons();
    }
    
    private boolean isBalloonPanelVisible(){
        return findViewById(R.id.balloon_panel).getVisibility() == View.VISIBLE;
    }
    
    private void hideBalloonPanel(){
        findViewById(R.id.balloon_panel).setVisibility(View.GONE);
        Boolean collapsedTag = (Boolean) findViewById(R.id.mapview).getTag();
        boolean collapsed = collapsedTag == null?true:collapsedTag.booleanValue();
        if(collapsed && !isBottomBarVisible()) {
            findViewById(R.id.bottom_bar).setVisibility(View.VISIBLE);
        }
    }
    
    private boolean isBottomBarVisible() {
    	return findViewById(R.id.bottom_bar).getVisibility() == View.VISIBLE;
    }
    
    private void hideBottomBar() {
    	findViewById(R.id.bottom_bar).setVisibility(View.GONE);
    	findViewById(R.id.trip_panel).setVisibility(View.GONE);
    	findViewById(R.id.on_the_way_panel).setVisibility(View.GONE);
    }
    
    private synchronized void drawBulbPOIs(final MapView mapView, List<com.smartrek.requests.WhereToGoRequest.Location> locs) {
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay instanceof POIActionOverlay){
                POIActionOverlay poiOverlay = (POIActionOverlay)overlay;
                if(poiOverlay.getMarker() == R.drawable.bulb_poi){
                    overlays.remove(overlay);
                }
            }
        }
        
        initFontsIfNecessary();
        for(final com.smartrek.requests.WhereToGoRequest.Location l:locs){
            final GeoPoint gp = new GeoPoint(l.lat, l.lon);
            final POIActionOverlay bulb = new POIActionOverlay(mapView, gp, boldFont, lightFont, 
                    l.addr, l.label, R.drawable.bulb_poi);
            bulb.setBalloonOffsetY(Dimension.dpToPx(-17, getResources().getDisplayMetrics()));
            bulb.setCallback(new OverlayCallback() {
                @Override
                public boolean onTap(int index) {
                    hideStarredBalloon();
                    hideBulbBalloon();
                    removePOIMarker(mapView);
                    BalloonModel model = new BalloonModel();
                    model.lat = l.lat;
                    model.lon = l.lon;
                    model.address = l.addr;
                    model.geopoint = gp;
                    final View balloonView = (View) findViewById(R.id.balloon_panel);
                    TextView addrView = ((TextView)balloonView.findViewById(R.id.address));
                    addrView.setText(l.addr);
                    TextView labelView = ((TextView)balloonView.findViewById(R.id.label));
                    labelView.setText(l.label);
                    balloonView.setTag(model);
                    balloonView.setVisibility(View.VISIBLE);
                    hideBottomBar();
                    mapView.postInvalidate();
                    relayoutIcons();
                    return true;
                }
                @Override
                public boolean onLongPress(int index, OverlayItem item) {
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
                public boolean onBalloonTap(int index, OverlayItem item) {
                    return false;
                }
            });
            overlays.add(bulb);
            bulb.showOverlay();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
    
    static class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
        
        Context ctx;
        
        String url;
        
        LoadImageTask(Context ctx, String url){
            this.ctx = ctx;
            this.url = url;
        }
        
        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap rs = null;
            InputStream is = null;
            Cache cache = Cache.getInstance(ctx);
            try{
                InputStream cachedStream = cache.fetchStream(url);
                if(cachedStream == null){
                    HTTP http = new HTTP(url);
                    http.connect();
                    InputStream tmpStream = http.getInputStream();
                    try{
                        cache.put(url, tmpStream);
                        is = cache.fetchStream(url);
                    }finally{
                        IOUtils.closeQuietly(tmpStream);
                    }
                }else{
                    is = cachedStream;
                }
                rs = BitmapFactory.decodeStream(is);
            }catch(Exception e){
            }finally{
                IOUtils.closeQuietly(is);
            }
            return rs;
        }
        
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        
    }

    float[] mGravity;
    float[] mGeomagnetic;
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float r[] = new float[9];
            float i[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(r, i, mGravity,
                    mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(r, orientation);
                float azimut = Double.valueOf(Math.toDegrees(orientation[0])).floatValue();
                if(myPointOverlay != null){
                    myPointOverlay.setDegrees(azimut);
                    MapView mapView = (MapView)findViewById(R.id.mapview);
                    mapView.postInvalidate();
                }
            }
        }
    }

    
}
