package com.smartrek.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.smartrek.activities.LandingActivity.CurrentLocationListener;
import com.smartrek.dialogs.FeedbackDialog;
import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.dialogs.ShareDialog;
import com.smartrek.models.User;
import com.smartrek.requests.AddressLinkRequest;
import com.smartrek.requests.CityRequest;
import com.smartrek.requests.FavoriteAddressAddRequest;
import com.smartrek.requests.FavoriteAddressFetchRequest;
import com.smartrek.requests.FavoriteAddressUpdateRequest;
import com.smartrek.requests.WhereToGoRequest;
import com.smartrek.ui.EditAddress;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.ui.overlays.EventOverlay;
import com.smartrek.ui.overlays.OverlayCallback;
import com.smartrek.ui.overlays.POIActionOverlay;
import com.smartrek.ui.overlays.PointOverlay;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Geocoding;
import com.smartrek.utils.Geocoding.Address;
import com.smartrek.utils.Misc;
import com.smartrek.utils.RouteRect;
import com.smartrek.utils.SmartrekTileProvider;

public final class LandingActivity2 extends FragmentActivity {
    
    public static final boolean ENABLED = true;
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
    PointOverlay myPointOverlay;
    
    LocationManager locationManager;

    LocationListener locationListener;
    
    LocationManager networkLocManager;
    List<LocationListener> networkLocListeners = new ArrayList<LocationListener>();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing2);
        
        final MapView mapView = (MapView) findViewById(R.id.mapview);
        Misc.disableHardwareAcceleration(mapView);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setTileSource(new SmartrekTileProvider());
        
        final MapController mc = mapView.getController();
        int lat = (int) Math.round(38.27268853598097f*1E6);
        int lon = (int) Math.round(-99.1406250000000f*1E6);
        mc.setZoom(4); 
        mc.setCenter(new GeoPoint(lat, lon));
        
        TextView osmCredit = (TextView) findViewById(R.id.osm_credit);
        Misc.initOsmCredit(osmCredit);
        
        LandingActivity.initializeIfNeccessary(this, new Runnable() {
            @Override
            public void run() {
                getCurrentLocation(new CurrentLocationListener() {
                    @Override
                    public void get(final double lat, final double lon) {
                        AsyncTask<Void, Void, String> checkCityAvailability = new AsyncTask<Void, Void, String>(){
                            @Override
                            protected String doInBackground(Void... params) {
                                String result;
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
                            protected void onPostExecute(String result) {
                                if(StringUtils.isNotBlank(result)){
                                    CharSequence msg = Html.fromHtml(result);
                                    NotificationDialog dialog = new NotificationDialog(LandingActivity2.this, msg);
                                    dialog.show();
                                }
                            }
                        };
                        Misc.parallelExecute(checkCityAvailability);
                    }
                }, true);
                refreshBulbPOIs();
            }
        });
        
        TextView searchBox = (TextView) findViewById(R.id.search_box);
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
        
        View centerMapIcon = findViewById(R.id.center_map_icon);
        centerMapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myPointOverlay != null){
                    mc.animateTo(myPointOverlay.getLocation());
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
                User user = User.getCurrentUser(LandingActivity2.this);
                ShareDialog.newInstance(user.getFirstname() + " " + user.getLastname() + " is on the way",
                     "I helped solve traffic congestion using Smartrek Mobile!"
                     + "\n\n" + Misc.getGooglePlayAppUrl(LandingActivity2.this))
                    .show(getSupportFragmentManager(), null);
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
        
        AssetManager assets = getAssets();
//        Font.setTypeface(Font.getBold(assets));
        Font.setTypeface(Font.getLight(assets), osmCredit, searchBox, nextTripInfo,
            rewardsMenu, shareMenu, feedbackMenu, settingsMenu, logoutMenu);
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
        getCurrentLocation(lis, false);
    }
    
    private void getCurrentLocation(final CurrentLocationListener lis, boolean forceNetworkLocation){
        if(networkLocManager == null){
            networkLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        class CurrentLocationListener implements LocationListener{
            
            boolean changed;
            
            @Override
            public void onLocationChanged(Location location) {
                try{
                    changed = true;
                    networkLocManager.removeUpdates(this);
                    double lon = location.getLongitude();
                    double lat = location.getLatitude();
                    // debug lat lon
//                    lat = 34.0291747;
//                    lon = -118.2734106;
                    lis.get(lat, lon);
                }catch(Throwable t){}
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        }
        final CurrentLocationListener networkLocListener = new CurrentLocationListener();
        networkLocListeners.add(networkLocListener);
        try{
            if (!forceNetworkLocation && networkLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                networkLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, networkLocListener);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!networkLocListener.changed){
                            networkLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocListener);
                        }
                    }
                }, 15000);
            }else{
                networkLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocListener);
            }
        }catch(Throwable t){}
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
                    ehs.registerException(e);
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
                    if (result != null && result.size() > 0) {
                        final MapView mapView = (MapView) findViewById(R.id.mapview);
                        List<Overlay> overlays = mapView.getOverlays();
                        for (Overlay overlay : overlays) {
                            if(overlay instanceof POIActionOverlay){
                                POIActionOverlay poiOverlay = (POIActionOverlay)overlay;
                                if(poiOverlay.getMarker() == R.drawable.star_poi){
                                    poiOverlay.hideBalloon();
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
                                    star.showBalloonOverlay();  
                                    final View balloonView = (View) star.getBalloonView();
                                    View saveIcon = balloonView.findViewById(R.id.save);
                                    final EditText lblView = (EditText)balloonView.findViewById(R.id.label);
                                    lblView.setText(a.getName());
                                    if(saveIcon.getTag() == null){
                                        saveIcon.setTag(true);
                                        saveIcon.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                final String lbl = lblView.getText().toString();
                                                final String addr = ((TextView)balloonView.findViewById(R.id.address)).getText().toString();
                                                if(StringUtils.isNotBlank(lbl)){
                                                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                                                        @Override
                                                        protected Void doInBackground(Void... params) {
                                                            User user = User.getCurrentUser(LandingActivity2.this);
                                                            try {
                                                                FavoriteAddressUpdateRequest request = new FavoriteAddressUpdateRequest(
                                                                    new AddressLinkRequest(user).execute(LandingActivity2.this),
                                                                    a.getId(),
                                                                    user,
                                                                    lbl,
                                                                    addr,
                                                                    a.getLatitude(),
                                                                    a.getLongitude());
                                                                request.execute(LandingActivity2.this);
                                                            }
                                                            catch (Exception e) {
                                                                ehs.registerException(e);
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
                                                }
                                            }
                                        });
                                        balloonView.findViewById(R.id.proceed).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                startRouteActivity(a.getAddress(), gp);
                                            }
                                        });
                                    }
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
                        }
                        mapView.postInvalidate();
                    }
                }
            }
        };
        Misc.parallelExecute(task);
    }
    
    private void hideStarredBalloon(){
        MapView mapView = (MapView) findViewById(R.id.mapview);
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay instanceof POIActionOverlay){
                POIActionOverlay poiOverlay = (POIActionOverlay)overlay;
                if(poiOverlay.getMarker() == R.drawable.star_poi){
                    poiOverlay.hideBalloon();
                }
            }
        }
    }
    
    private void hideBulbBalloon(){
        MapView mapView = (MapView) findViewById(R.id.mapview);
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay instanceof POIActionOverlay){
                POIActionOverlay poiOverlay = (POIActionOverlay)overlay;
                if(poiOverlay.getMarker() == R.drawable.bulb_poi){
                    poiOverlay.hideBalloon();
                }
            }
        }
    }
    
    private void refreshBulbPOIs(){
        getCurrentLocation(new CurrentLocationListener() {
            @Override
            public void get(final double lat, final double lon) {
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
                            ehs.registerException(e);
                        }
                        return locs;
                    }
                    @Override
                    protected void onPostExecute(List<com.smartrek.requests.WhereToGoRequest.Location> locs) {
                        if (ehs.hasExceptions()) {
                            ehs.reportExceptions();
                        }
                        else {
                            MapView mapView = (MapView) findViewById(R.id.mapview);
                            MapController mc = mapView.getController();
                            if(myPointOverlay == null){
                                myPointOverlay = new PointOverlay(LandingActivity2.this, 0, 0);
                                myPointOverlay.setColor(0xCC2020DF);
                            }
                            myPointOverlay.setLocation((float) lat, (float) lon);
                            final List<Overlay> mapOverlays = mapView.getOverlays();
                            mapOverlays.clear();
                            bindMapFunctions(mapView);
                            if(locs.isEmpty()){
                                mc.setZoom(ValidationActivity.DEFAULT_ZOOM_LEVEL);
                                mc.setCenter(new GeoPoint(lat, lon));
                            }else{
                                RouteRect routeRect = drawBulbPOIs(mapView, locs);
                                GeoPoint mid = routeRect.getMidPoint();
                                int[] range = routeRect.getRange();
                                mc.zoomToSpan(range[0], range[1]);
                                mc.setCenter(mid);
                            }
                            mapOverlays.add(myPointOverlay);
                            mapView.postInvalidate();
                            refreshStarredPOIs();
                        }
                    }
                };
                Misc.parallelExecute(task);
            }
        });
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
                hideStarredBalloon();
                hideBulbBalloon();
                removePOIMarker(mapView);
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
    
    private void removePOIMarker(MapView mapView){
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay == curMarker){
                curMarker.hideBalloon();
                overlays.remove(overlay);
                mapView.postInvalidate();
                break;
            }
        }
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
        marker.showBalloonOverlay();
        curMarker = marker;
        mapView.postInvalidate();
        final View balloonView = (View) marker.getBalloonView();
        balloonView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String lbl = ((EditText)balloonView.findViewById(R.id.label)).getText().toString();
                final String addr = ((TextView)balloonView.findViewById(R.id.address)).getText().toString();
                if(StringUtils.isNotBlank(lbl)){
                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {
                            FavoriteAddressAddRequest request = new FavoriteAddressAddRequest(
                                User.getCurrentUser(LandingActivity2.this), lbl, addr, lat, lon);
                            try {
                                request.execute(LandingActivity2.this);
                            }
                            catch (Exception e) {
                                ehs.registerException(e);
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
                }
            }
        });
        balloonView.findViewById(R.id.proceed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRouteActivity(address, gp);
            }
        });
    }
    
    private synchronized RouteRect drawBulbPOIs(final MapView mapView, List<com.smartrek.requests.WhereToGoRequest.Location> locs) {
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay instanceof POIActionOverlay){
                POIActionOverlay poiOverlay = (POIActionOverlay)overlay;
                if(poiOverlay.getMarker() == R.drawable.bulb_poi){
                    poiOverlay.hideBalloon();
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
                    l.addr, l.debug, R.drawable.bulb_poi);
            bulb.setBalloonOffsetY(Dimension.dpToPx(-17, getResources().getDisplayMetrics()));
            bulb.setCallback(new OverlayCallback() {
                @Override
                public boolean onTap(int index) {
                    startRouteActivity(l.addr, gp);
                    return true;
                }
                @Override
                public boolean onLongPress(int index, OverlayItem item) {
                    hideStarredBalloon();
                    hideBulbBalloon();
                    removePOIMarker(mapView);
                    bulb.showBalloonOverlay();  
                    final View balloonView = (View) bulb.getBalloonView();
                    View saveIcon = balloonView.findViewById(R.id.save);
                    final EditText lblView = (EditText)balloonView.findViewById(R.id.label);
                    lblView.setText(l.debug);
                    if(saveIcon.getTag() == null){
                        saveIcon.setTag(true);
                        saveIcon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String lbl = lblView.getText().toString();
                                final String addr = ((TextView)balloonView.findViewById(R.id.address)).getText().toString();
                                if(StringUtils.isNotBlank(lbl)){
                                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            FavoriteAddressAddRequest request = new FavoriteAddressAddRequest(
                                                User.getCurrentUser(LandingActivity2.this), lbl, addr, l.lat, l.lon);
                                            try {
                                                request.execute(LandingActivity2.this);
                                            }
                                            catch (Exception e) {
                                                ehs.registerException(e);
                                            }
                                            return null;
                                        }
                                        protected void onPostExecute(Void result) {
                                            if (ehs.hasExceptions()) {
                                                ehs.reportExceptions();
                                            }
                                            else {
                                                removePOIMarker(mapView);
                                                hideBulbBalloon();
                                                refreshStarredPOIs();
                                            }
                                        }
                                   };
                                   Misc.parallelExecute(task);
                                }
                            }
                        });
                        balloonView.findViewById(R.id.proceed).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startRouteActivity(l.addr, gp);
                            }
                        });
                    }
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
    
}
