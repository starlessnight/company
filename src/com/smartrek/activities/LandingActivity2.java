package com.smartrek.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartrek.activities.LandingActivity.CurrentLocationListener;
import com.smartrek.dialogs.FeedbackDialog;
import com.smartrek.dialogs.NotificationDialog;
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
import com.smartrek.requests.ReservationDeleteRequest;
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

public final class LandingActivity2 extends FragmentActivity {
    
    public static final String LAT = "lat";
    
    public static final String LON = "lon";
    
    public static final String MSG = "msg";
    
    public static final boolean ENABLED = true;
    
    public static final String NO_TRIPS = "No Upcoming Trips";
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
    PointOverlay myPointOverlay;
    
    LocationManager locationManager;

    LocationListener locationListener;
    
    LocationManager networkLocManager;
    List<LocationListener> networkLocListeners = new ArrayList<LocationListener>();
    
    private AtomicBoolean locationInited = new AtomicBoolean();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing2);
        
        final MapView mapView = (MapView) findViewById(R.id.mapview);
        Misc.disableHardwareAcceleration(mapView);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setTileSource(new SmartrekTileProvider());
        
        final IMapController mc = mapView.getController();
        int lat = (int) Math.round(38.27268853598097f*1E6);
        int lon = (int) Math.round(-99.1406250000000f*1E6);
        mc.setZoom(4); 
        mc.setCenter(new GeoPoint(lat, lon));
        
        TextView osmCredit = (TextView) findViewById(R.id.osm_credit);
        
        LandingActivity.initializeIfNeccessary(this, new Runnable() {
            @Override
            public void run() {
                updateDeviceId();
                getCurrentLocation(new CurrentLocationListener() {
                    @Override
                    public void get(double lat, double lon) {
                        locationInited.set(true);
                        refreshBulbPOIs(lat , lon, true);
                        refreshCobranding(lat, lon, true);
                        scheduleLocationUpdates();
                    }
                    @Override
                    public void adjusted(double lat, double lon) {
                        refreshBulbPOIs(lat , lon, false);
                        refreshCobranding(lat, lon, false);
                    }
                });
            }
        });
        
        AutoCompleteTextView searchBox = (AutoCompleteTextView) findViewById(R.id.search_box);
        refreshSearchAutoCompleteData();
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                final String addrInput = v.getText().toString();
                boolean handled = StringUtils.isNotBlank(addrInput);
                if(handled){
                    AsyncTask<Void, Void, GeoPoint> task = new AsyncTask<Void, Void, GeoPoint>(){
                        @Override
                        protected GeoPoint doInBackground(Void... params) {
                            GeoPoint gp = null;
                            try {
                                List<Address> addrs = Geocoding.lookup(addrInput);
                                for (Address a : addrs) {
                                    gp = new GeoPoint(a.getLatitude(), a.getLongitude());
                                    break;
                                }
                            }
                            catch (IOException e) {
                            }
                            catch (JSONException e) {
                            }
                            return gp;
                        }
                        @Override
                        protected void onPostExecute(GeoPoint gp) {
                            if(gp != null){
                                DebugOptionsActivity.addRecentAddress(LandingActivity2.this, addrInput);
                                refreshSearchAutoCompleteData();
                                ReverseGeocodingTask task = new ReverseGeocodingTask(
                                        gp.getLatitude(), gp.getLongitude()){
                                    @Override
                                    protected void onPostExecute(String result) {
                                        refreshPOIMarker(mapView, lat, lon, result, "");
                                    }
                                };
                                Misc.parallelExecute(task);
                                mc.animateTo(gp);
                            }
                        }
                    };
                    Misc.parallelExecute(task);
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return handled;
            }
        });
        
        TextView nextTripInfo = (TextView) findViewById(R.id.next_trip_info);
        nextTripInfo.setSelected(true);
        nextTripInfo.setText(NO_TRIPS);
        
        View centerMapIcon = findViewById(R.id.center_map_icon);
        centerMapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation(new CurrentLocationListener() {
                    @Override
                    public void get(double lat, double lon) {
                        refreshBulbPOIs(lat , lon, true);
                        refreshCobranding(lat, lon, true);
                    }
                    @Override
                    public void adjusted(double lat, double lon) {
                        refreshBulbPOIs(lat , lon, false);
                        refreshCobranding(lat, lon, false);
                    }
                });
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
            }
        });
        TextView rescheBtn = (TextView) findViewById(R.id.reschedule_button);
        rescheBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Reservation reserv = (Reservation) findViewById(R.id.trip_panel).getTag();
                AsyncTask<Void, Void, Void> delTask = new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... params) {
                        ReservationDeleteRequest request = new ReservationDeleteRequest(
                            User.getCurrentUser(LandingActivity2.this), reserv.getRid());
                        try {
                            request.execute(LandingActivity2.this);
                        }
                        catch (Exception e) {
                        }
                        return null;
                    }
                };
                Misc.parallelExecute(delTask);
                final String addr = reserv.getDestinationAddress();
                AsyncTask<Void, Void, GeoPoint> geoCodeTask = new AsyncTask<Void, Void, GeoPoint>(){
                    @Override
                    protected GeoPoint doInBackground(Void... params) {
                        GeoPoint gp = null;
                        try {
                            List<Address> addrs = Geocoding.lookup(addr);
                            for (Address a : addrs) {
                                gp = new GeoPoint(a.getLatitude(), a.getLongitude());
                                break;
                            }
                        }
                        catch (IOException e) {
                        }
                        catch (JSONException e) {
                        }
                        return gp;
                    }
                    @Override
                    protected void onPostExecute(GeoPoint gp) {
                        if(gp != null){
                            startRouteActivity(addr, gp);
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
                if(StringUtils.isNotBlank(lbl)){
                    final BalloonModel model = (BalloonModel) balloonView.getTag();
                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {
                            Request req = null;
                            User user = User.getCurrentUser(LandingActivity2.this);
                            try {
                                if (model.id == 0){
                                    FavoriteAddressAddRequest request = new FavoriteAddressAddRequest(
                                        user, lbl, addr, model.lat, model.lon);
                                    req = request;
                                    request.execute(LandingActivity2.this);
                                }
                                else {
                                	FavoriteAddressDeleteRequest request = new FavoriteAddressDeleteRequest(
                                            new AddressLinkRequest(user).execute(LandingActivity2.this), user, model.id);
                                	req = request;
                                    request.execute(LandingActivity2.this);
                                }
                            }
                            catch (Exception e) {
                                ehs.registerException(e, "[" + req==null?"":req.getUrl() + "]\n" + e.getMessage());
                            }
                            return null;
                        }
                        protected void onPostExecute(Void result) {
                            if (ehs.hasExceptions()) {
                                ehs.reportExceptions();
                            }
                            else {
                                removePOIMarker(mapView);
                                refreshStarredPOIs();
                            }
                        }
                   };
                   Misc.parallelExecute(task);
                   hideBalloonPanel();
                }
            }
        });
        balloonView.findViewById(R.id.get_going).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BalloonModel model = (BalloonModel) balloonView.getTag();
                startRouteActivity(model.address, model.geopoint);
            }
        });
        
        balloonView.findViewById(R.id.label).setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus) {
					final String lbl = ((EditText)balloonView.findViewById(R.id.label)).getText().toString();
	                final String addr = ((TextView)balloonView.findViewById(R.id.address)).getText().toString();
					final BalloonModel model = (BalloonModel) balloonView.getTag();
					if(StringUtils.isNotBlank(lbl)) {
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
	                                ehs.registerException(e, "[" + req==null?"":req.getUrl() + "]\n" + e.getMessage());
	                            }
	                            return null;
	                        }
	                        protected void onPostExecute(Void result) {
	                            if (ehs.hasExceptions()) {
	                                ehs.reportExceptions();
	                            }
	                            else {
	                                removePOIMarker(mapView);
	                                refreshStarredPOIs();
	                            }
	                        }
	                    };
	                    Misc.parallelExecute(task);
	                    hideBalloonPanel();
			        }
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
        
        scheduleNextTripInfoUpdates();
        
        AssetManager assets = getAssets();
        Font.setTypeface(Font.getBold(assets), tripAddr);
        Font.setTypeface(Font.getLight(assets), osmCredit, searchBox, nextTripInfo,
            rewardsMenu, shareMenu, feedbackMenu, settingsMenu, logoutMenu,
            tripDetails, getGoingBtn, rescheBtn, (TextView)findViewById(R.id.header_text),
            (TextView)findViewById(R.id.menu_bottom_text),
            (TextView)findViewById(R.id.on_the_way_msg), onTheWayBtn);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_dropdown_item_1line, searchData);
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
            findViewById(R.id.on_the_way_panel).setVisibility(View.VISIBLE);
        }
    };
    
    public static final String LOCATION_UPDATES = "LOCATION_UPDATES"; 
    
    private void scheduleLocationUpdates(){
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 15000, 
            PendingIntent.getBroadcast(this, 0, new Intent(LOCATION_UPDATES), PendingIntent.FLAG_UPDATE_CURRENT));
    }
    
    private BroadcastReceiver locationUpdater = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LandingActivity.initializeIfNeccessary(context, new Runnable() {
                @Override
                public void run() {
                    getCurrentLocation(new CurrentLocationListener() {
                        @Override
                        public void get(double lat, double lon) {
                            refreshBulbPOIs(lat , lon, false);
                            refreshCobranding(lat, lon, false);
                        }
                        @Override
                        public void adjusted(double lat, double lon) {
                            refreshBulbPOIs(lat , lon, false);
                            refreshCobranding(lat, lon, false);
                        }
                    });
                }
            });
        }
    };
    
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(tripInfoUpdater, new IntentFilter(TRIP_INFO_UPDATES));
        registerReceiver(onTheWayNotifier, new IntentFilter(ON_THE_WAY_NOTICE));
        SessionM.onActivityResume(this);
        if(locationInited.get()){
            LandingActivity.initializeIfNeccessary(this, new Runnable() {
                @Override
                public void run() {
                    getCurrentLocation(new CurrentLocationListener() {
                        @Override
                        public void get(double lat, double lon) {
                            refreshBulbPOIs(lat , lon, true);
                            refreshCobranding(lat, lon, true);
                            registerReceiver(locationUpdater, new IntentFilter(LOCATION_UPDATES));
                        }
                        @Override
                        public void adjusted(double lat, double lon) {
                            refreshBulbPOIs(lat , lon, false);
                            refreshCobranding(lat, lon, false);
                        }
                    });
                }
            });
        }else{
            registerReceiver(locationUpdater, new IntentFilter(LOCATION_UPDATES));
        }
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
      unregisterReceiver(locationUpdater);
      SessionM.onActivityPause(this);
      super.onPause();
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
                    ehs.reportExceptions();
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
                        int tripPanelVis = View.VISIBLE;
                        String nextTripInfoText;
                        long timeUntilDepart = reserv.getDepartureTimeUtc() - System.currentTimeMillis();
                        if(reserv.isEligibleTrip()){
                            nextTripInfoText = "Get Going";
                            getGoingBtnVis = View.VISIBLE;
                            rescheBtnVis = View.GONE;
                        }else if(timeUntilDepart > 60 * 60 * 1000L){
                            nextTripInfoText = "Next Trip at "
                                + TimeColumn.formatTime(reserv.getDepartureTime());
                        }else if(timeUntilDepart > Reservation.GRACE_INTERVAL){
                            nextTripInfoText = "Next Trip in "
                                + TimeColumn.getFormattedDuration((int)timeUntilDepart / 1000);
                        }else if(timeUntilDepart > -2 * 60 * 60 * 1000L){
                            nextTripInfoText = "Trip has expired";
                        }else{
                            nextTripInfoText = NO_TRIPS;
                            carIconVis = View.INVISIBLE;
                            tripPanelVis = View.GONE;
                        }
                        nextTripInfo.setText(nextTripInfoText);
                        TextView getGoingBtn = (TextView) findViewById(R.id.get_going_button);
                        getGoingBtn.setVisibility(getGoingBtnVis);
                        TextView rescheBtn = (TextView) findViewById(R.id.reschedule_button);
                        rescheBtn.setVisibility(rescheBtnVis);
                        carIcon.setVisibility(carIconVis);
                        tripPanel.setVisibility(tripPanelVis);
                    }
                }
            }
        };
        Misc.parallelExecute(tripTask);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getBooleanExtra(LandingActivity.LOGOUT, false)){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
    }
    
    private void getCurrentLocation(final CurrentLocationListener lis){
        if(networkLocManager == null){
            networkLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        class AdjustedLocationListener implements LocationListener {
            
            private AtomicBoolean init = new AtomicBoolean();
            
            @Override
            public void onLocationChanged(Location location) {
                try{
                    double lon = location.getLongitude();
                    double lat = location.getLatitude();
                    if(!init.get()){
                        init.set(true); 
                        lis.get(lat, lon);
                    }else if(LocationManager.GPS_PROVIDER.equals(location.getProvider())){
                        networkLocManager.removeUpdates(this);
                        lis.adjusted(lat, lon);
                    }
                }catch(Throwable t){}
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        }
        final AdjustedLocationListener networkLocListener = new AdjustedLocationListener();
        networkLocListeners.add(networkLocListener);
        try{
            if (networkLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                networkLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, networkLocListener);
            }
            networkLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocListener);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    networkLocManager.removeUpdates(networkLocListener);
                }
            }, 10000);
        }catch(Throwable t){}
        //lis.get(34.0291747, -118.2734106);
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
                    ehs.reportExceptions();
                }
                else {
                    List<String> addrList = new ArrayList<String>();
                    if (result != null && result.size() > 0) {
                        final MapView mapView = (MapView) findViewById(R.id.mapview);
                        List<Overlay> overlays = mapView.getOverlays();
                        for (Overlay overlay : overlays) {
                            if(overlay instanceof POIActionOverlay){
                                POIActionOverlay poiOverlay = (POIActionOverlay)overlay;
                                if(poiOverlay.getMarker() == R.drawable.star_poi){
                                    overlays.remove(overlay);
                                }
                            }
                        }
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
                                    startRouteActivity(a.getAddress(), gp);
                                    return true;
                                }
                                @Override
                                public boolean onLongPress(int index, OverlayItem item) {
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
                                    startImg.setImageResource(R.drawable.star_poi);
                                    balloonView.setTag(model);
                                    balloonView.setVisibility(View.VISIBLE);
                                    hideBottomBar();
                                    mapView.postInvalidate();
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
                        mapView.postInvalidate();
                        findViewById(R.id.search_box).setTag(R.id.starred_addresses, addrList);
                        refreshSearchAutoCompleteData();
                    }
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
                        NotificationDialog dialog = new NotificationDialog(LandingActivity2.this, msg);
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
    
    private void refreshBulbPOIs(final double lat, final double lon, final boolean recenter){
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
                    ehs.reportExceptions();
                }
                else {
                    hideStarredBalloon();
                    hideBulbBalloon();
                    MapView mapView = (MapView) findViewById(R.id.mapview);
                    removePOIMarker(mapView);
                    IMapController mc = mapView.getController();
                    if(myPointOverlay == null){
                        myPointOverlay = new PointOverlay(LandingActivity2.this, 0, 0);
                        myPointOverlay.setColor(0xCC2020DF);
                    }
                    myPointOverlay.setLocation((float) lat, (float) lon);
                    final List<Overlay> mapOverlays = mapView.getOverlays();
                    mapOverlays.clear();
                    bindMapFunctions(mapView);
                    List<String> addrList = new ArrayList<String>();
                    if(locs.isEmpty()){
                        if(recenter){
                            mc.setZoom(ValidationActivity.DEFAULT_ZOOM_LEVEL);
                            mc.setCenter(new GeoPoint(lat, lon));
                        }
                    }else{
                        if(recenter){
                            RouteRect routeRect = drawBulbPOIs(mapView, locs);
                            GeoPoint mid = routeRect.getMidPoint();
                            int[] range = routeRect.getRange();
                            mc.zoomToSpan(range[0], range[1]);
                            mc.setCenter(mid);
                        }
                        for(com.smartrek.requests.WhereToGoRequest.Location l : locs){
                            addrList.add(l.addr);
                        }
                    }
                    mapOverlays.add(myPointOverlay);
                    mapView.postInvalidate();
                    findViewById(R.id.search_box).setTag(R.id.where_to_addresses, addrList);
                    refreshSearchAutoCompleteData();
                    refreshStarredPOIs();
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
                ReverseGeocodingTask task = new ReverseGeocodingTask(lat, lon){
                    @Override
                    protected void onPostExecute(String result) {
                        hideStarredBalloon();
                        hideBulbBalloon();
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
                    Boolean collapsedTag = (Boolean) mapView.getTag();
                    boolean collapsed = collapsedTag == null?true:collapsedTag.booleanValue();
                    mapView.setTag(!collapsed);
                    findViewById(R.id.header_panel).setVisibility(collapsed?View.GONE:View.VISIBLE);
                    findViewById(R.id.bottom_bar).setVisibility(collapsed?View.GONE:View.VISIBLE);
                    View menuIcon = findViewById(R.id.drawer_menu_icon_opened);
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) menuIcon.getLayoutParams();
                    lp.bottomMargin = Dimension.dpToPx(collapsed?30:68, getResources().getDisplayMetrics());
                    menuIcon.setLayoutParams(lp);
                }
            }
        });
        mapView.getOverlays().add(eventOverlay);
    }
    
    private static abstract class ReverseGeocodingTask extends AsyncTask<Void, Void, String> {
        
        double lat;
        
        double lon;
        
        ReverseGeocodingTask(double lat, double lon){
            this.lat = lat;
            this.lon = lon;
        }
        
        @Override
        protected String doInBackground(Void... params) {
            String address = null;
            try {
                address = Geocoding.lookup(lat, lon);
            }
            catch (IOException e) {
            }
            catch (JSONException e) {
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
    }
    
    private boolean isBalloonPanelVisible(){
        return findViewById(R.id.balloon_panel).getVisibility() == View.VISIBLE;
    }
    
    private void hideBalloonPanel(){
        findViewById(R.id.balloon_panel).setVisibility(View.GONE);
        if(!isBottomBarVisible()) {
            findViewById(R.id.bottom_bar).setVisibility(View.VISIBLE);
        }
    }
    
    private boolean isBottomBarVisible() {
    	return findViewById(R.id.bottom_bar).getVisibility() == View.VISIBLE;
    }
    
    private void hideBottomBar() {
    	findViewById(R.id.bottom_bar).setVisibility(View.GONE);
    }
    
    private synchronized RouteRect drawBulbPOIs(final MapView mapView, List<com.smartrek.requests.WhereToGoRequest.Location> locs) {
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay instanceof POIActionOverlay){
                POIActionOverlay poiOverlay = (POIActionOverlay)overlay;
                if(poiOverlay.getMarker() == R.drawable.bulb_poi){
                    overlays.remove(overlay);
                }
            }
        }
        
        int latMax = (int)(-81 * 1E6);
        int lonMax = (int)(-181 * 1E6);
        int latMin = (int)(+81 * 1E6);
        int lonMin = (int)(+181 * 1E6);
        
        initFontsIfNecessary();
        for(final com.smartrek.requests.WhereToGoRequest.Location l:locs){
            final GeoPoint gp = new GeoPoint(l.lat, l.lon);
            final POIActionOverlay bulb = new POIActionOverlay(mapView, gp, boldFont, lightFont, 
                    l.addr, l.label, R.drawable.bulb_poi);
            bulb.setBalloonOffsetY(Dimension.dpToPx(-17, getResources().getDisplayMetrics()));
            bulb.setCallback(new OverlayCallback() {
                @Override
                public boolean onTap(int index) {
                    startRouteActivity(l.addr, gp);
                    return true;
                }
                @Override
                public boolean onLongPress(int index, OverlayItem item) {
                    /*hideStarredBalloon();
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
                    mapView.postInvalidate();*/
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
            int curLat = gp.getLatitudeE6();
            int curLon = gp.getLongitudeE6();
            latMax = Math.max(latMax, curLat);
            lonMax = Math.max(lonMax, curLon);
            latMin = Math.min(latMin, curLat);
            lonMin = Math.min(lonMin, curLon);
        }
        
        return new RouteRect(latMax, lonMax, latMin, lonMin);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(networkLocManager != null){
            for(LocationListener networkLocListener:networkLocListeners){
                networkLocManager.removeUpdates(networkLocListener); 
            }
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

    
}
