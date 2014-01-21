package com.smartrek.activities;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.smartrek.activities.LandingActivity.CurrentLocationListener;
import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.requests.CityRequest;
import com.smartrek.ui.overlays.PointOverlay;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Misc;
import com.smartrek.utils.SmartrekTileProvider;

public final class LandingActivity2 extends Activity {
    
    public static final boolean ENABLED = false;
    
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
        
        myPointOverlay = new PointOverlay(LandingActivity2.this, 0, 0);
        myPointOverlay.setColor(0xCC2020DF);
        
        MapView mapView = (MapView) findViewById(R.id.mapview);
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
            }
        });
        
        TextView searchBox = (TextView) findViewById(R.id.search_box);
        TextView nextTripInfo = (TextView) findViewById(R.id.next_trip_info);
        nextTripInfo.setSelected(true);
        
        View centerMapIcon = findViewById(R.id.center_map_icon);
        centerMapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mc.animateTo(myPointOverlay.getLocation());
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
        
        AssetManager assets = getAssets();
//        Font.setTypeface(Font.getBold(assets));
        Font.setTypeface(Font.getLight(assets), osmCredit, searchBox, nextTripInfo);
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
                    lis.get(location.getLatitude(), location.getLongitude());
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
    
    private void centerMapByCurrentLocation(){
        getCurrentLocation(new CurrentLocationListener() {
            @Override
            public void get(double lat, double lon) {
                MapView mapView = (MapView) findViewById(R.id.mapview);
                MapController mc = mapView.getController();
                mc.setZoom(ValidationActivity.DEFAULT_ZOOM_LEVEL);
                mc.setCenter(new GeoPoint(lat, lon));
                final List<Overlay> mapOverlays = mapView.getOverlays();
                mapOverlays.clear();
                myPointOverlay.setLocation((float) lat, (float) lon);
                mapOverlays.add(myPointOverlay);
                mapView.postInvalidate();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LandingActivity.initializeIfNeccessary(this, new Runnable() {
            @Override
            public void run() {
                centerMapByCurrentLocation();
            }
        });
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
