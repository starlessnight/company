package com.metropia.tasks;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.metropia.activities.DebugOptionsActivity;
import com.metropia.activities.R;
import com.metropia.activities.ValidationActivity;
import com.metropia.dialogs.CancelableProgressDialog;
import com.metropia.models.Reservation;
import com.metropia.models.ReservationTollHovInfo;
import com.metropia.models.Route;
import com.metropia.models.User;
import com.metropia.requests.ReservationListFetchRequest;
import com.metropia.requests.ReservationRequest;
import com.metropia.requests.RouteFetchRequest;
import com.metropia.ui.timelayout.AdjustableTime;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.Geocoding;
import com.metropia.utils.Misc;

public class ShortcutNavigationTask extends AsyncTask<Void, Void, Void> {
    
    public interface Callback {
        
        void run(Reservation reserv);
        
        void runOnFail();
        
    }
    
    CancelableProgressDialog dialog;
    
    String originAddress;
    
    String address;
    
    Context ctx;
    
    GeoPoint origin;
    
    GeoPoint dest;
    
    ExceptionHandlingService ehs;
    
    Route _route;
    
    boolean startedMakingReserv;
    
    String versionNumber;
    
    ReservationTollHovInfo reservInfo;
    
    public Callback callback = new Callback() {
        @Override
        public void run(Reservation reserv) {
            Intent intent = new Intent(ctx, ValidationActivity.class);
            intent.putExtra("route", reserv.getRoute());
            intent.putExtra("reservation", reserv);
            ctx.startActivity(intent);
        }

		@Override
		public void runOnFail() {
		}
    };
    
//    ShortcutNavigationTask(LandingActivity ctx, ExceptionHandlingService ehs){
//        this.ehs = ehs;
//        this.activity = ctx;
//        this.ctx = ctx;
//        dialog = new CancelableProgressDialog(ctx, "Getting current location...");
//        dialog.setActionListener(new CancelableProgressDialog.ActionListener() {
//            @Override
//            public void onClickNegativeButton() {
//                ShortcutNavigationTask.this.activity.removeLocationUpdates();
//            }
//        });
//        dialog.setOnCancelListener(new OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//                ShortcutNavigationTask.this.activity.removeLocationUpdates();
//            }
//        });
//    }
    
    long id;
    
    public ShortcutNavigationTask(Context ctx, Route route, ExceptionHandlingService ehs, long id, String versionNumber, ReservationTollHovInfo reservInfo){
        this.ehs = ehs;
        this.ctx = ctx;
        _route = route;
        dialog = new CancelableProgressDialog(ctx, "Loading...");
        this.id = id;
        this.versionNumber = versionNumber;
        this.reservInfo = reservInfo;
    }
    
    public ShortcutNavigationTask(Context ctx, GeoPoint origin, String originAddress, GeoPoint dest,
            String destAddress, ExceptionHandlingService ehs, String versionNumber, ReservationTollHovInfo reservInfo){
        this.ehs = ehs;
        this.ctx = ctx;
        this.origin = origin;
        this.originAddress = originAddress;
        this.dest = dest;
        this.address = destAddress;
        dialog = new CancelableProgressDialog(ctx, "Loading...");
        this.versionNumber = versionNumber;
        this.reservInfo = reservInfo;
    }
    
    @Override
    protected void onPreExecute() {
        //dialog.show();
//        if(this._route == null && origin == null){
//            activity.locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
//            if (!activity.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                SystemService.alertNoGPS(activity, true, new SystemService.Callback() {
//                    @Override
//                    public void onNo() {
//                        if (dialog.isShowing()) {
//                            dialog.cancel();
//                        }
//                    }
//                });
//            }
//            activity.locationListener = new LocationListener() {
//                @Override
//                public void onLocationChanged(Location location) {
//                    try{
//                        activity.locationManager.removeUpdates(this);
//                        dialog.dismiss();
//                        origin = new GeoPoint(location.getLatitude(), 
//                            location.getLongitude());
//                        makeReservation();
//                    }catch(Throwable t){}
//                }
//                @Override
//                public void onStatusChanged(String provider, int status, Bundle extras) {}
//                @Override
//                public void onProviderEnabled(String provider) {}
//                @Override
//                public void onProviderDisabled(String provider) {}
//            };
//            activity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, activity.locationListener);
//        }
    }
    
    @Override
    protected Void doInBackground(Void... params) {
        if(this._route == null && dest == null){
            try {
                dest = Geocoding.lookup(ctx, address).get(0).getGeoPoint();
                String curLoc = DebugOptionsActivity.getCurrentLocation(ctx);
                if(StringUtils.isNotBlank(curLoc)){ 
                    origin = Geocoding.lookup(ctx, curLoc).get(0).getGeoPoint();
                }
            }
            catch (Exception e) {
                ehs.registerException(e);
            }
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
    
    void cancelTask(){
        if (dialog.isShowing()) {
            dialog.cancel();
        }
        cancel(true);
    }
    
    void makeReservation(){
        if(!startedMakingReserv && ((origin != null && dest != null) || _route != null)){
            startedMakingReserv = true;
            Misc.parallelExecute(new AsyncTask<Void, Void, Reservation>(){
                @Override
                protected Reservation doInBackground(Void... params) {
                    Reservation reserv = null;
                    AdjustableTime departureTime = new AdjustableTime();
                    departureTime.setToNow();
                    User user = User.getCurrentUser(ctx);
                    try {
                        Route route;
                        if(_route == null){
                            RouteFetchRequest routeReq = new RouteFetchRequest(user, 
                                origin, dest, departureTime.initTime().toMillis(false),
                                0, 0, originAddress, address, 
                    	        reservInfo.isIncludeToll(), versionNumber, reservInfo.isHov());
                            route = routeReq.execute(ctx).get(0);
                            route.setAddresses(originAddress, address);
                            route.setUserId(user.getId());
                        }else{
                           route = _route; 
                        }
                        ReservationRequest reservReq = new ReservationRequest(user, 
                            route, ctx.getString(R.string.distribution_date), id);
                        Long newId;
                        if(id!=0) {
                        	newId = route.getId();
                        	reservReq.execute(ctx);
                        }
                        else {
                            newId = reservReq.execute(ctx);
                        }
                        ReservationListFetchRequest reservListReq = new ReservationListFetchRequest(user);
                        reservListReq.invalidateCache(ctx);
                        List<Reservation> reservs = reservListReq.execute(ctx);
                        for (Reservation r : reservs) {
                            if(((Long)r.getRid()).equals(newId)){
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
                        if(callback!=null) {
                        	callback.runOnFail();
                        }
                    }else if(reserv != null && callback != null){
                        callback.run(reserv);
                    }
                }
            });
        }
    }
    
}