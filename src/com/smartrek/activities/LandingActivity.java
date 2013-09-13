package com.smartrek.activities;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.smartrek.dialogs.FavoriteAddressEditDialog;
import com.smartrek.dialogs.ShortcutAddressDialog;
import com.smartrek.models.Address;
import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.requests.FavoriteAddressFetchRequest;
import com.smartrek.requests.ReservationListFetchRequest;
import com.smartrek.requests.ReservationRequest;
import com.smartrek.requests.RouteFetchRequest;
import com.smartrek.ui.EditAddress;
import com.smartrek.ui.timelayout.AdjustableTime;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Geocoding;
import com.smartrek.utils.Misc;
import com.smartrek.utils.SmartrekTileProvider;
import com.smartrek.utils.SystemService;

public class LandingActivity extends Activity {

    public static final String LOGOUT = "logout";
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
    LocationManager locationManager;
    
    LocationListener locationListener;
    
    LocationManager networkLocManager;
    LocationListener networkLocListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing);
        
        TextView vTitle = (TextView) findViewById(R.id.title);
        TextView vDate = (TextView) findViewById(R.id.date);
        TextView vClock = (TextView) findViewById(R.id.clock);
        TextView vWeather = (TextView) findViewById(R.id.weather);
        TextView vTrip1 = (TextView) findViewById(R.id.trip_one);
        TextView vTrip2 = (TextView) findViewById(R.id.trip_two);
        
        TextView vPlanATrip = (TextView) findViewById(R.id.plan_a_trip);
        vPlanATrip.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandingActivity.this, HomeActivity.class);
                intent.putExtra(HomeActivity.INIT, true);
                startActivity(intent);
            }
        });
        TextView vGoHome = (TextView) findViewById(R.id.go_home);
        vGoHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final ShortcutNavigationTask t = new ShortcutNavigationTask(LandingActivity.this, ehs);
                new ShortcutAddressTask(LandingActivity.this, ehs, Address.HOME_STRING, 
                    new ShortcutAddressTask.Callback(){
                        @Override
                        public void run(String addr) {
                            if(StringUtils.isBlank(addr)){
                                final FavoriteAddressEditDialog d = new FavoriteAddressEditDialog(LandingActivity.this, 
                                    new Address(0, User.getCurrentUser(LandingActivity.this).getId(), Address.HOME_STRING, 
                                        "", 0, 0), true);
                                d.setActionListener(new FavoriteAddressEditDialog.ActionListener() {
                                    @Override
                                    public void onClickPositiveButton() {
                                        String ad = d.getAddress();
                                        MapDisplayActivity.setHomeAddress(LandingActivity.this, ad);
                                        d.dismiss();
                                        t.address = ad;
                                        t.execute();
                                    }
                                    @Override
                                    public void onClickNegativeButton() {
                                    }
                                });
                                d.show();
                            }else{
                                t.address = addr;
                                t.execute();
                            }
                        }
                }).execute();
            }
        });
        TextView vGoToWork = (TextView) findViewById(R.id.go_to_work);
        vGoToWork.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final ShortcutNavigationTask t = new ShortcutNavigationTask(LandingActivity.this, ehs);
                new ShortcutAddressTask(LandingActivity.this, ehs, Address.WORK_STRING, 
                    new ShortcutAddressTask.Callback(){
                        @Override
                        public void run(String addr) {
                            if(StringUtils.isBlank(addr)){
                                final FavoriteAddressEditDialog d = new FavoriteAddressEditDialog(LandingActivity.this, 
                                    new Address(0, User.getCurrentUser(LandingActivity.this).getId(), Address.WORK_STRING, 
                                        "", 0, 0), true);
                                d.setActionListener(new FavoriteAddressEditDialog.ActionListener() {
                                    @Override
                                    public void onClickPositiveButton() {
                                        String ad = d.getAddress();
                                        MapDisplayActivity.setHomeAddress(LandingActivity.this, ad);
                                        d.dismiss();
                                        t.address = ad;
                                        t.execute();
                                    }
                                    @Override
                                    public void onClickNegativeButton() {
                                    }
                                });
                                d.show();
                            }else{
                                t.address = addr;
                                t.execute();
                            }
                        }
                }).execute();
            }
        });
        TextView vOuttaHere = (TextView) findViewById(R.id.outta_here);
        vOuttaHere.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final ShortcutAddressDialog d = new ShortcutAddressDialog(LandingActivity.this, "Enter Location");
                d.setActionListener(new ShortcutAddressDialog.ActionListener() {
                    @Override
                    public void onClickPositiveButton() {
                        ShortcutNavigationTask t = new ShortcutNavigationTask(LandingActivity.this, ehs);
                        t.address = d.getAddress();
                        d.dismiss();
                        t.execute();
                    }
                    @Override
                    public void onClickNegativeButton() {
                    }
                });
                d.show();
            }
        });
        TextView vExploreMap = (TextView) findViewById(R.id.explore_map);
        final TextView vRewards = (TextView) findViewById(R.id.rewards);
        vRewards.post(new Runnable() {
            @Override
            public void run() {
                int width = getWindowManager().getDefaultDisplay().getWidth();
                Font.autoScaleTextSize(vRewards, width/2);
            }
        });
        TextView vTrekpoints = (TextView) findViewById(R.id.trekpoints);
        TextView vValidatedTripsUpdateCount = (TextView) findViewById(R.id.validated_trips_update_count);
        
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
        
        networkLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        networkLocListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try{
                    networkLocManager.removeUpdates(this);
                    mc.setZoom(ValidationActivity.DEFAULT_ZOOM_LEVEL);
                    mc.setCenter(new GeoPoint(location.getLatitude(), 
                        location.getLongitude()));
                }catch(Throwable t){}
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };
        networkLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocListener);
        
        AssetManager assets = getAssets();
        Font.setTypeface(Font.getBold(assets), vTitle, vClock, vWeather, vTrip1, 
            vTrip2, vPlanATrip, vGoHome, vGoToWork, vOuttaHere, vExploreMap,
            vRewards, vTrekpoints);
        Font.setTypeface(Font.getLight(assets), vDate, vValidatedTripsUpdateCount);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(networkLocManager != null && networkLocListener != null){
            networkLocManager.removeUpdates(networkLocListener); 
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getBooleanExtra(LOGOUT, false)){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
    }
    
    private void removeLocationUpdates(){
        if(locationManager != null && locationListener != null){
            locationManager.removeUpdates(locationListener); 
        }
    }
    
    private static class ShortcutNavigationTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;
        
        String address;
        
        LandingActivity ctx;
        
        GeoPoint origin;
        
        GeoPoint dest;
        
        ExceptionHandlingService ehs;
        
        boolean startedMakingReserv;
        
        ShortcutNavigationTask(LandingActivity ctx, ExceptionHandlingService ehs){
            this.ehs = ehs;
            this.ctx = ctx;
            dialog = new ProgressDialog(ctx);
            dialog.setTitle("Smartrek");
            dialog.setMessage("Loading...");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    ShortcutNavigationTask.this.ctx.removeLocationUpdates();
                }
            });
        }
        
        @Override
        protected void onPreExecute() {
            dialog.show();
            ctx.locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            if (!ctx.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                SystemService.alertNoGPS(ctx);
            }
            ctx.locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    try{
                        ctx.locationManager.removeUpdates(this);
                        dialog.dismiss();
                        origin = new GeoPoint(location.getLatitude(), 
                            location.getLongitude());
                        makeReservation();
                    }catch(Throwable t){}
                }
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override
                public void onProviderEnabled(String provider) {}
                @Override
                public void onProviderDisabled(String provider) {}
            };
            ctx.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, ctx.locationListener);
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            try {
                dest = Geocoding.lookup(address).get(0).getGeoPoint();
                String curLoc = DebugOptionsActivity.getCurrentLocation(ctx);
                if(StringUtils.isNotBlank(curLoc)){
                    origin = Geocoding.lookup(curLoc).get(0).getGeoPoint();
                }
            }
            catch (Exception e) {
                ehs.registerException(e);
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            if (ehs.hasExceptions()) {
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
                ehs.reportExceptions();
            }else{
                makeReservation();
            }
        }
        
        void makeReservation(){
            if(!startedMakingReserv && origin != null && dest != null){
                startedMakingReserv = true;
                new AsyncTask<Void, Void, Reservation>(){
                    @Override
                    protected Reservation doInBackground(Void... params) {
                        Reservation reserv = null;
                        AdjustableTime departureTime = new AdjustableTime();
                        departureTime.setToNow();
                        User user = User.getCurrentUser(ctx);
                        RouteFetchRequest routeReq = new RouteFetchRequest(user, 
                            origin, dest, departureTime.initTime().toMillis(false));
                        try {
                            Route route = routeReq.execute(ctx).get(0);
                            route.setAddresses(EditAddress.CURRENT_LOCATION, address);
                            route.setUserId(user.getId());
                            ReservationRequest reservReq = new ReservationRequest(user, 
                                route, ctx.getString(R.string.distribution_date));
                            Long id = reservReq.execute();
                            ReservationConfirmationActivity.scheduleNotification(ctx, route);
                            ReservationListFetchRequest reservListReq = new ReservationListFetchRequest(user);
                            reservListReq.invalidateCache(ctx);
                            List<Reservation> reservs = reservListReq.execute(ctx);
                            for (Reservation r : reservs) {
                                if(((Long)r.getRid()).equals(id)){
                                    reserv = r;
                                }
                            }
                        }
                        catch(Exception e) {
                            ehs.registerException(e);
                        }
                        return reserv;
                    }
                    protected void onPostExecute(Reservation reserv) {
                        if (dialog.isShowing()) {
                            dialog.cancel();
                        }
                        if (ehs.hasExceptions()) {
                            ehs.reportExceptions();
                        }else if(reserv != null){
                            Intent intent = new Intent(ctx, ValidationActivity.class);
                            intent.putExtra("route", reserv.getRoute());
                            intent.putExtra("reservation", reserv);
                            ctx.startActivity(intent);
                        }
                    }
                }.execute();
            }
        }
        
    }
    
    private static class ShortcutAddressTask extends AsyncTask<Void, Void, String> {

        ProgressDialog dialog;
        
        ExceptionHandlingService ehs;
        
        Context ctx;
        
        Callback callback;
        
        String name;
        
        static interface Callback {
            
            void run(String address);
            
        }
        
        ShortcutAddressTask(Context ctx, ExceptionHandlingService ehs, String name, Callback callback){
            this.ctx = ctx;
            this.ehs = ehs;
            this.name = name;
            this.callback = callback;
            dialog = new ProgressDialog(ctx);
            dialog.setTitle("Smartrek");
            dialog.setMessage("Loading...");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
        }
        
        @Override
        protected void onPreExecute() {
            dialog.show();
        }
        
        @Override
        protected String doInBackground(Void... params) {
            String address = null;
            FavoriteAddressFetchRequest request = new FavoriteAddressFetchRequest(User.getCurrentUser(ctx));
            try {
                request.invalidateCache(ctx);
                List<Address> favoriteAddresses = request.execute(ctx);
                for (Address a : favoriteAddresses) {
                    if(a.getName().equals(name)){
                        address = a.getAddress();
                    }
                }
            }
            catch (Exception e) {
                ehs.registerException(e);
            }
            return address;
        }
        
        @Override
        protected void onPostExecute(String result) {
            if (dialog.isShowing()) {
                dialog.cancel();
            }
            if (ehs.hasExceptions()) {
                ehs.reportExceptions();
            }else{
                callback.run(result);
            }
        }
        
    }
    
}
