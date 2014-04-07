package com.smartrek.activities;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
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
import android.text.Html;
import android.text.format.Time;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.Image;
import com.smartrek.dialogs.CancelableProgressDialog;
import com.smartrek.dialogs.ContactsDialog;
import com.smartrek.dialogs.FavoriteAddressEditDialog;
import com.smartrek.dialogs.NotificationDialog2;
import com.smartrek.dialogs.ProfileSelectionDialog;
import com.smartrek.dialogs.ProfileSelectionDialog.Type;
import com.smartrek.dialogs.ShortcutAddressDialog;
import com.smartrek.models.Address;
import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.requests.CityRequest;
import com.smartrek.requests.CityRequest.City;
import com.smartrek.requests.FavoriteAddressFetchRequest;
import com.smartrek.requests.Request;
import com.smartrek.requests.ReservationListFetchRequest;
import com.smartrek.requests.ReservationRequest;
import com.smartrek.requests.RouteFetchRequest;
import com.smartrek.requests.TrekpointFetchRequest;
import com.smartrek.requests.TrekpointFetchRequest.Trekpoint;
import com.smartrek.requests.UserIdRequest;
import com.smartrek.requests.ValidatedReservationsFetchRequest;
import com.smartrek.tasks.LoginTask;
import com.smartrek.ui.EditAddress;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.ui.overlays.OverlayCallback;
import com.smartrek.ui.overlays.PointOverlay;
import com.smartrek.ui.overlays.RouteInfoOverlay;
import com.smartrek.ui.overlays.RoutePathOverlay;
import com.smartrek.ui.timelayout.AdjustableTime;
import com.smartrek.utils.Cache;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Geocoding;
import com.smartrek.utils.HTTP;
import com.smartrek.utils.Misc;
import com.smartrek.utils.Preferences;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.RouteRect;
import com.smartrek.utils.SessionM;
import com.smartrek.utils.SmartrekTileProvider;
import com.smartrek.utils.SystemService;

public class LandingActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {

    public static final String LOGOUT = "logout";
    
    public static final String LAT = "lat";
    
    public static final String LON = "lon";
    
    public static final String MSG = "msg";
    
    public static final String RESERVATION_ID = "reservationId";
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
    LocationManager locationManager;
    
    LocationListener locationListener;
    
    LocationManager networkLocManager;
    List<LocationListener> networkLocListeners = new ArrayList<LocationListener>(); 
    
    Typeface boldFont;
    Typeface lightFont;
    
    private static final int iconWidth = 48;
    
    private static final int tripIconWidth = 31;
    
    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

    private ProgressDialog mConnectionProgressDialog;
    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;
    
    private UiLifecycleHelper uiHelper;
    
    private boolean fbPending;
    
    private boolean fbClicked;
    
    private Session.StatusCallback fbCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    PointOverlay myPointOverlay;
    
    PointOverlay othersPointOverlay;
    
    private ShortcutNavigationTask currentSNTask;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing);
        
        myPointOverlay = new PointOverlay(LandingActivity.this, 0, 0, R.drawable.landing_page_current_location);
        myPointOverlay.setColor(0xCC2020DF);
        
        othersPointOverlay = new PointOverlay(LandingActivity.this, 0, 0, R.drawable.landing_page_current_location);
        othersPointOverlay.setColor(0xCC2020DF);
        
        TextView vTitle = (TextView) findViewById(R.id.title);
        TextView vDate = (TextView) findViewById(R.id.date);
        
        TextView vClock = (TextView) findViewById(R.id.clock);
        TextView vWeather = (TextView) findViewById(R.id.weather);
        TextView vTrip1 = (TextView) findViewById(R.id.trip_one);
        vTrip1.setOnClickListener(newTripOnClickListener());
        TextView vTrip2 = (TextView) findViewById(R.id.trip_two);
        vTrip2.setOnClickListener(newTripOnClickListener());
        
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
                currentSNTask = new ShortcutNavigationTask(LandingActivity.this, ehs);
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
                                        currentSNTask.address = ad;
                                        currentSNTask.execute();
                                    }
                                    @Override
                                    public void onClickNegativeButton() {
                                    }
                                });
                                d.show();
                            }else{
                                currentSNTask.address = addr;
                                currentSNTask.execute();
                            }
                        }
                }).execute();
            }
        });
        TextView vGoToWork = (TextView) findViewById(R.id.go_to_work);
        vGoToWork.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSNTask = new ShortcutNavigationTask(LandingActivity.this, ehs);
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
                                        currentSNTask.address = ad;
                                        currentSNTask.execute();
                                    }
                                    @Override
                                    public void onClickNegativeButton() {
                                    }
                                });
                                d.show();
                            }else{
                                currentSNTask.address = addr;
                                currentSNTask.execute();
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
                        currentSNTask = new ShortcutNavigationTask(LandingActivity.this, ehs);
                        currentSNTask.address = d.getAddress();
                        d.dismiss();
                        currentSNTask.execute();
                    }
                    @Override
                    public void onClickNegativeButton() {
                    }
                });
                d.show();
            }
        });
         
        TextView vExploreMap = (TextView) findViewById(R.id.explore_map);
        vExploreMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                expandMap();
            }
        });
        View collapseBtn= findViewById(R.id.collapse_btn);
        collapseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                collapseMap();
                centerMapByCurrentLocation();
            }
        });
        
        final TextView vRewards = (TextView) findViewById(R.id.rewards);
        vRewards.post(new Runnable() {
            @Override
            public void run() {
                int width = getWindowManager().getDefaultDisplay().getWidth();
                Font.autoScaleTextSize(vRewards, width/2);
                vRewards.setVisibility(View.VISIBLE);
            }
        });
        
        TextView vTrekpoints = (TextView) findViewById(R.id.trekpoints);
        
        TextView vValidatedTripsUpdateCount = (TextView) findViewById(R.id.validated_trips_update_count);
        
        MapView mapView = (MapView) findViewById(R.id.mapview);
        Misc.disableHardwareAcceleration(mapView);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setTileSource(new SmartrekTileProvider());
        
        IMapController mc = mapView.getController();
        int lat = (int) Math.round(38.27268853598097f*1E6);
        int lon = (int) Math.round(-99.1406250000000f*1E6);
        mc.setZoom(4); 
        mc.setCenter(new GeoPoint(lat, lon));
        
        TextView osmCredit = (TextView) findViewById(R.id.osm_credit);
        RelativeLayout.LayoutParams osmCreditLp = (RelativeLayout.LayoutParams) osmCredit.getLayoutParams();
        osmCreditLp.rightMargin += Dimension.dpToPx(52, getResources().getDisplayMetrics());
        
        final TextView vImComing = (TextView) findViewById(R.id.im_coming);
        vImComing.post(new Runnable() {
            @Override
            public void run() {
                int width = getWindowManager().getDefaultDisplay().getWidth();
                float offset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, iconWidth, getResources().getDisplayMetrics());
                Font.autoScaleTextSize(vImComing, width/2 - offset);
            }
        });
        vImComing.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactsDialog d = new ContactsDialog(LandingActivity.this);
                d.setActionListener(new ContactsDialog.ActionListener() {
                    @Override
                    public void onClickPositiveButton(List<String> emails) {
                        TextView vGetGoing = (TextView) findViewById(R.id.get_going);
                        Reservation reservation =(Reservation) vGetGoing.getTag();
                        if(reservation.isEligibleTrip()){
                            Intent intent = new Intent(LandingActivity.this, ValidationActivity.class);
                            intent.putExtra("route", reservation.getRoute());
                            intent.putExtra("reservation", reservation);
                            intent.putExtra(ValidationActivity.EMAILS, StringUtils.join(emails, ","));
                            startActivity(intent);
                            collapseMap();
                            centerMapByCurrentLocation();
                        }else{
                            String msg = null;
                            if (reservation.hasExpired()) {
                                msg = getString(R.string.trip_has_expired);
                            }
                            else if (reservation.isTooEarlyToStart()) {
                                long minutes = (reservation.getDepartureTimeUtc() - System.currentTimeMillis()) / 60000;
                                msg = getString(R.string.trip_too_early_to_start, minutes);
                                if(minutes != 1){
                                    msg += "s";
                                }
                            }
                            if(msg != null){
                                NotificationDialog2 dialog = new NotificationDialog2(LandingActivity.this, msg);
                                dialog.show();
                            }
                        }
                    }
                    @Override
                    public void onClickNegativeButton() {}
                });
                d.show();
            }
        });
        
        final TextView vGetGoing = (TextView) findViewById(R.id.get_going);
        vGetGoing.post(new Runnable() {
            @Override
            public void run() {
                int width = getWindowManager().getDefaultDisplay().getWidth();
                float offset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, iconWidth, getResources().getDisplayMetrics());
                Font.autoScaleTextSize(vGetGoing, width/2 - vGetGoing.getPaddingRight()
                    - offset);
            }
        });
        vGetGoing.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Reservation reservation =(Reservation) v.getTag();
                if(reservation.isEligibleTrip()){
                    Intent intent = new Intent(LandingActivity.this, ValidationActivity.class);
                    intent.putExtra("route", reservation.getRoute());
                    intent.putExtra("reservation", reservation);
                    startActivity(intent);
                    collapseMap();
                    centerMapByCurrentLocation();
                }else{
                    String msg = null;
                    if (reservation.hasExpired()) {
                        msg = getString(R.string.trip_has_expired);
                    }
                    else if (reservation.isTooEarlyToStart()) {
                        long minutes = (reservation.getDepartureTimeUtc() - System.currentTimeMillis()) / 60000;
                        msg = getString(R.string.trip_too_early_to_start, minutes);
                        if(minutes != 1){
                            msg += "s";
                        }
                    }
                    if(msg != null){
                        NotificationDialog2 dialog = new NotificationDialog2(LandingActivity.this, msg);
                        dialog.show();
                    }
                }
            }
        });
        
        findViewById(R.id.rewards_panel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandingActivity.this, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        
        TextView vImComingMsg = (TextView) findViewById(R.id.im_coming_msg);
        
        ImageView avatar = (ImageView) findViewById(R.id.avatar);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileSelectionDialog d = new ProfileSelectionDialog(LandingActivity.this);
                d.setActionListener(new ProfileSelectionDialog.ActionListener() {
                    @Override
                    public void onClickPositiveButton(Type type) {
                        loadProfile(type, true);
                        MapDisplayActivity.setProfileSelection(LandingActivity.this, type);
                    }
                    @Override
                    public void onClickNegativeButton() {
                    }
                });
                d.show();
            }
        });
        
        initializeIfNeccessary(this, new Runnable() {
            @Override
            public void run() {
                refreshDate();
                refreshTripsInfo();
                refreshTrekpoints();
                refreshTripUpdateCount();
                
                Intent intent = getIntent();
                String imComingMsg = intent.getStringExtra(MSG);
                long reservId = intent.getLongExtra(RESERVATION_ID, 0);
                if(reservId > 0){
                    handleReservNotification(reservId);
                }else if(StringUtils.isBlank(imComingMsg)){
                    centerMapByCurrentLocation();
                }else{
                    handleImComing(imComingMsg, intent.getDoubleExtra(LAT, 0), 
                        intent.getDoubleExtra(LON, 0));
                }
                
                loadProfile(MapDisplayActivity.getProfileSelection(LandingActivity.this));
                
                getCurrentLocation(new CurrentLocationListener() {
                    @Override
                    public void get(final double lat, final double lon) {
                        AsyncTask<Void, Void, City> checkCityAvailability = new AsyncTask<Void, Void, City>(){
                            @Override
                            protected City doInBackground(Void... params) {
                                City result;
                                try{
                                    CityRequest req = new CityRequest(lat, lon);
                                    req.invalidateCache(LandingActivity.this);
                                    result = req.execute(LandingActivity.this);
                                }catch(Throwable t){
                                    result = null;
                                }
                                return result;
                            }
                            @Override
                            protected void onPostExecute(City result) {
                                if(StringUtils.isNotBlank(result.html)){
                                    CharSequence msg = Html.fromHtml(result.html);
                                    NotificationDialog2 dialog = new NotificationDialog2(LandingActivity.this, msg);
                                    dialog.show();
                                }
                            }
                        };
                        Misc.parallelExecute(checkCityAvailability);
                    }
                }, true);
            }
        });
        
        TextView vNoTrips = (TextView) findViewById(R.id.no_trips);
        
        AssetManager assets = getAssets();
        boldFont = Font.getBold(assets);
        lightFont = Font.getLight(assets);
        
        Font.setTypeface(boldFont, vTitle, vClock, vWeather, vTrip1, 
            vTrip2, vNoTrips, vPlanATrip, vGoHome, vGoToWork, vOuttaHere, vExploreMap,
            vRewards, vTrekpoints, vImComing, vGetGoing, vImComingMsg);
        Font.setTypeface(lightFont, vDate, vValidatedTripsUpdateCount,
            osmCredit);
        
        uiHelper = new UiLifecycleHelper(this, fbCallback);
        uiHelper.onCreate(savedInstanceState);
    }
    
    public static void initializeIfNeccessary(Context ctx, final Runnable callback){
        initializeIfNeccessary(ctx, callback, true);
    }
    
    public static void initializeIfNeccessary(final Context ctx, final Runnable callback, 
            final boolean canLogout){
        Runnable loginAndDoCallback = new Runnable() {
            @Override
            public void run() {
                if(User.getCurrentUser(ctx) != null){
                    callback.run();
                }else{
                    SharedPreferences loginPrefs = Preferences.getAuthPreferences(ctx);
                    final String username = loginPrefs.getString(User.USERNAME, "");
                    final String password = loginPrefs.getString(User.PASSWORD, "");
                    if (!username.equals("") && !password.equals("")) {
                        final String gcmRegistrationId = Preferences.getGlobalPreferences(ctx)
                                .getString("GCMRegistrationID", "");
                        final LoginTask loginTask = new LoginTask(ctx, username, password, gcmRegistrationId) {
                            @Override
                            protected void onPostLogin(final User user) {
                                if(user != null && user.getId() != -1){
                                    User.setCurrentUser(ctx, user);
                                    callback.run();
                                }else if(canLogout && ctx instanceof Activity){
                                    MainMenu.onMenuItemSelected((Activity) ctx, 0, R.id.logout_option);
                                }
                           }
                        }.setDialogEnabled(false);
                        new AsyncTask<Void, Void, Integer>() {
                            @Override
                            protected Integer doInBackground(Void... params) {
                                Integer id = null;
                                try {
                                    UserIdRequest req = new UserIdRequest(username); 
                                    req.invalidateCache(ctx);
                                    id = req.execute(ctx);
                                }
                                catch(Exception e) {
                                }
                                return id;
                            }
                            protected void onPostExecute(Integer userId) {
                                if(canLogout && ctx instanceof Activity && userId == null){                                    
                                    MainMenu.onMenuItemSelected((Activity)ctx, 0, R.id.logout_option);
                                }else{
                                    loginTask.setUserId(userId)
                                        .execute();
                                }
                            }
                        }.execute();
                    }else if(canLogout && ctx instanceof Activity){
                        MainMenu.onMenuItemSelected((Activity) ctx, 0, R.id.logout_option);
                    }
                }
            }
        };
        
        if(Request.hasLinkUrls()){
            loginAndDoCallback.run();
        }else{
            String url = DebugOptionsActivity.getEntrypoint(ctx);
            if(StringUtils.isBlank(url)){
                url = Request.ENTRYPOINT_URL;
            }
            MainActivity.initApiLinks(ctx, url, loginAndDoCallback, null);
        }
    }
    
    private void loadProfile(Type type){
        loadProfile(type, false);
    }
    
    private boolean doLog;
    
    private void loadProfile(Type type, boolean doLog){
        this.doLog = doLog;
        if(type == Type.facebook){
            fbClicked = true; 
            if(isNotLoading()){
                Session session = Session.getActiveSession();
                if (session != null && session.isOpened()) {
                    makeMeRequest();
                }else{
                    fbPending = true;
                    fbLogin();
                }
            }
        }else if(type == type.googlePlus){
            mPlusClient = new PlusClient.Builder(LandingActivity.this, 
                    LandingActivity.this, LandingActivity.this)
                .setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
                .build();
            mPlusClient.connect();
            // Progress bar to be displayed if the connection failure is not resolved.
            showLoading();
        }
        User user = User.getCurrentUser(this);
        String firstname = user.getFirstname();
        if(StringUtils.isNotBlank(firstname)){
            updateTitle(firstname);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        SessionM.onActivityResume(this);
        uiHelper.onResume();
        initializeIfNeccessary(this, new Runnable() {
            @Override
            public void run() {
                refreshDate();
                refreshTripsInfo();
                refreshTripUpdateCount();
                refreshTrekpoints();
                if(findViewById(R.id.collapse_btn).getVisibility() != View.VISIBLE){
                    centerMapByCurrentLocation();
                }
                if(currentSNTask != null && locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    try{
                        currentSNTask.cancelTask();
                    }catch(Throwable t){}
                }
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
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String imComingMsg = intent.getStringExtra(MSG);
        long reservId = intent.getLongExtra(RESERVATION_ID, 0);
        if(intent.getBooleanExtra(LOGOUT, false)){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }else if(StringUtils.isNotBlank(imComingMsg)){
            handleImComing(imComingMsg, intent.getDoubleExtra(LAT, 0), 
                intent.getDoubleExtra(LON, 0));
        }else if(reservId > 0){
            handleReservNotification(reservId);
        }
    }
    
    private void handleImComing(String msg, final double lat, final double lon){
        TextView vImComingMsg = (TextView) findViewById(R.id.im_coming_msg);
        vImComingMsg.setText(msg);
        vImComingMsg.setVisibility(View.VISIBLE);
        final MapView mapView = (MapView) findViewById(R.id.mapview);
        final List<Overlay> mapOverlays = mapView.getOverlays();
        if(infoOverlay != null){
            infoOverlay.hide();
        }
        mapOverlays.clear();
        othersPointOverlay.setLocation((float) lat, (float)lon);
        mapOverlays.add(othersPointOverlay);
        mapView.postInvalidate();
        IMapController mc = mapView.getController();
        mc.setZoom(ValidationActivity.DEFAULT_ZOOM_LEVEL);
        mc.setCenter(new GeoPoint(lat, lon));
        expandMap();
    }
    
    public interface CurrentLocationListener {
        
        void get(double lat, double lon);
        
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
                IMapController mc = mapView.getController();
                mc.setZoom(ValidationActivity.DEFAULT_ZOOM_LEVEL);
                mc.setCenter(new GeoPoint(lat, lon));
                final List<Overlay> mapOverlays = mapView.getOverlays();
                if(infoOverlay != null){
                    infoOverlay.hide();
                }
                mapOverlays.clear();
                myPointOverlay.setLocation((float) lat, (float) lon);
                mapOverlays.add(myPointOverlay);
                mapView.postInvalidate();
            }
        });
    }
    
    private void removeLocationUpdates(){
        if(locationManager != null && locationListener != null){
            locationManager.removeUpdates(locationListener); 
        }
    }
    
    private void expandMap(){
        expandMap(false);
    }
    
    private void refreshTrekpoints(){
        final User user = User.getCurrentUser(this);
        final int uid = user.getId();
        AsyncTask<Void, Void, Trekpoint> task = new AsyncTask<Void, Void, Trekpoint>() {
            @Override
            protected Trekpoint doInBackground(Void... params) {
                Trekpoint tp = null;
                TrekpointFetchRequest req;
                if(Request.NEW_API){
                    req = new TrekpointFetchRequest(user);
                }else{
                    req = new TrekpointFetchRequest(uid);
                }
                req.invalidateCache(LandingActivity.this);
                try {
                    tp = req.execute(LandingActivity.this);
                }
                catch (Exception e) {
                    ehs.registerException(e);
                }
                return tp;
            }
            @Override
            protected void onPostExecute(Trekpoint trekpoints) {
                if (ehs.hasExceptions()) {
                    ehs.reportExceptions();
                }
                else {
                    DecimalFormat fmt = new DecimalFormat("#,###");
                    ((TextView)findViewById(R.id.trekpoints)).setText(fmt.format(trekpoints == null?0:trekpoints.credit));
                }
            }
        };
        Misc.parallelExecute(task);
    }
    
    private void refreshTripUpdateCount(){
        final User user = User.getCurrentUser(this);
        final int uid = user.getId();
        final int validatedTripsCount = MapDisplayActivity.getValidatedTripsCount(this);
        AsyncTask<Void, Void, List<Reservation>> task = new AsyncTask<Void, Void, List<Reservation>>() {
            @Override
            protected List<Reservation> doInBackground(Void... params) {
                List<Reservation> reservations = Collections.emptyList();
                ValidatedReservationsFetchRequest req;
                if(Request.NEW_API){
                    req = new ValidatedReservationsFetchRequest(user);
                }else{
                    req = new ValidatedReservationsFetchRequest(uid);
                }
                req.invalidateCache(LandingActivity.this);
                try {
                    reservations = req.execute(LandingActivity.this);
                }
                catch (Exception e) {
                    ehs.registerException(e);
                }
                Collections.sort(reservations, Collections.reverseOrder(
                    Reservation.orderByDepartureTime()));
                return reservations;
            }
            @Override
            protected void onPostExecute(List<Reservation> reservations) {
                if (ehs.hasExceptions()) {
                    ehs.reportExceptions();
                }
                else {
                    TextView vValidatedTripsUpdateCount = (TextView) findViewById(R.id.validated_trips_update_count);
                    int updateCnt = reservations.size() - validatedTripsCount;
                    if(updateCnt > 0){
                        vValidatedTripsUpdateCount.setText(String.valueOf(updateCnt));
                        vValidatedTripsUpdateCount.setVisibility(View.VISIBLE);
                    }else{
                        vValidatedTripsUpdateCount.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };
        Misc.parallelExecute(task);
    }
    
    private static int mapAnimDuration = 300;
    
    private void expandMap(boolean showMapButton){
        View bottomLeftPanel = findViewById(R.id.bottom_left_panel);
        View bottomRightPanel = findViewById(R.id.bottom_right_panel);
        View rewardsPanel = findViewById(R.id.rewards_panel);
        final View collapseBtn= findViewById(R.id.collapse_btn);
        int width = bottomRightPanel.getWidth();
        int height = bottomRightPanel.getHeight();
        bottomLeftPanel.setVisibility(View.GONE);
        bottomRightPanel.setVisibility(View.GONE);
        rewardsPanel.setVisibility(View.GONE);
        if(showMapButton){
            View mapButtonPanel = findViewById(R.id.map_button_panel);
            mapButtonPanel.setVisibility(View.VISIBLE);
        }
        bottomRightPanel.setVisibility(View.VISIBLE);
        View upperPanel = findViewById(R.id.upper_panel);
        Display display = getWindowManager().getDefaultDisplay();
        float newWidth = display.getWidth();
        float newHeight = display.getHeight() - upperPanel.getHeight();
        ScaleAnimation anim = new ScaleAnimation(width/newWidth, 1, 
            height / newHeight, 1, newWidth, 0);
        anim.setDuration(mapAnimDuration);
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                collapseBtn.setVisibility(View.VISIBLE);
            }
        });
        bottomRightPanel.startAnimation(anim);
    }
    
    private void collapseMap(){
        MapView mapView = (MapView) findViewById(R.id.mapview);
        if(infoOverlay != null){
            infoOverlay.hide();
        }
        mapView.getOverlays().clear();
        mapView.postInvalidate();
        final View bottomLeftPanel = findViewById(R.id.bottom_left_panel);
        final View bottomRightPanel = findViewById(R.id.bottom_right_panel);
        int height = bottomRightPanel.getHeight();
        View mapButtonPanel = findViewById(R.id.map_button_panel);
        final View rewardsPanel = findViewById(R.id.rewards_panel);
        View collapseBtn= findViewById(R.id.collapse_btn);
        TextView vImComingMsg = (TextView) findViewById(R.id.im_coming_msg);
        collapseBtn.setVisibility(View.GONE);
        mapButtonPanel.setVisibility(View.GONE);
        vImComingMsg.setVisibility(View.GONE);
        rewardsPanel.setVisibility(View.VISIBLE);
        bottomLeftPanel.setVisibility(View.VISIBLE);
        Display display = getWindowManager().getDefaultDisplay();
        float newWidth = bottomLeftPanel.getWidth();
        float newHeight = height - rewardsPanel.getHeight();
        ScaleAnimation anim = new ScaleAnimation(1, newWidth / display.getWidth(), 1, 
            newHeight / height, display.getWidth(), 0);
        anim.setDuration(mapAnimDuration);
        anim.setFillEnabled(true);
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                rewardsPanel.setVisibility(View.VISIBLE);
                bottomLeftPanel.setVisibility(View.VISIBLE);
            }
        });
        rewardsPanel.setVisibility(View.GONE);
        bottomLeftPanel.setVisibility(View.GONE);
        bottomRightPanel.startAnimation(anim);
    }
    
    private void refreshTripsInfo(){
        final TextView vTrip1 = (TextView) findViewById(R.id.trip_one);
        final TextView vTrip2 = (TextView) findViewById(R.id.trip_two);
        AsyncTask<Void, Void, List<Reservation>> tripTask = new AsyncTask<Void, Void, List<Reservation>>(){
            @Override
            protected List<Reservation> doInBackground(Void... params) {
                User user = User.getCurrentUser(LandingActivity.this);
                List<Reservation> reservations= Collections.emptyList();
                try {
                    ReservationListFetchRequest resReq = new ReservationListFetchRequest(user);
                    resReq.invalidateCache(LandingActivity.this);
                    FavoriteAddressFetchRequest addReq = new FavoriteAddressFetchRequest(user);
                    addReq.invalidateCache(LandingActivity.this);
                    List<Address> addresses = addReq.execute(LandingActivity.this);
                    reservations = resReq.execute(LandingActivity.this);
                    for(Reservation r:reservations){
                        if(r.getOriginName() == null){
                            for (Address a : addresses) {
                                if(a.getAddress().equals(r.getOriginAddress())){
                                    r.setOriginName(a.getName());
                                    break;
                                }
                            }
                        }
                        if(r.getDestinationName() == null){
                            for (Address a : addresses) {
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
                    ehs.registerException(e);
                }
                return reservations;
            }
            @Override
            protected void onPostExecute(List<Reservation> reservations) {
                if (ehs.hasExceptions()) { 
                    ehs.reportExceptions();
                } 
                else{
                    TextView[] vTrips = {vTrip1, vTrip2};
                    for(int i=0; i<vTrips.length; i++){
                        TextView vTrip = vTrips[i];
                        if(i < reservations.size()){
                            Reservation res = reservations.get(i);
                            vTrip.setTag(res);
                            String originName = res.getOriginName();
                            String destinationName = res.getDestinationName();
                            vTrip.setText((originName == null?res.getOriginAddress():originName) 
                                + " to " + (destinationName == null?res.getDestinationAddress():destinationName));
                            vTrip.setVisibility(View.VISIBLE);
                        }else{
                            vTrip.setVisibility(View.INVISIBLE);
                        }
                    }
                    findViewById(R.id.no_trips).setVisibility(
                       (vTrip1.getVisibility() == View.INVISIBLE && vTrip2.getVisibility() == View.INVISIBLE)?
                           View.VISIBLE:View.INVISIBLE);
                }
            }
        };
        Misc.parallelExecute(tripTask);
    }
    
    private void handleReservNotification(final long id){
        AsyncTask<Void, Void, List<Reservation>> tripTask = new AsyncTask<Void, Void, List<Reservation>>(){
            @Override
            protected List<Reservation> doInBackground(Void... params) {
                User user = User.getCurrentUser(LandingActivity.this);
                List<Reservation> reservations= Collections.emptyList();
                try {
                    ReservationListFetchRequest resReq = new ReservationListFetchRequest(user);
                    resReq.invalidateCache(LandingActivity.this);
                    FavoriteAddressFetchRequest addReq = new FavoriteAddressFetchRequest(user);
                    addReq.invalidateCache(LandingActivity.this);
                    List<Address> addresses = addReq.execute(LandingActivity.this);
                    reservations = resReq.execute(LandingActivity.this);
                    for(Reservation r:reservations){
                        if(r.getOriginName() == null){
                            for (Address a : addresses) {
                                if(a.getAddress().equals(r.getOriginAddress())){
                                    r.setOriginName(a.getName());
                                    break;
                                }
                            }
                        }
                        if(r.getDestinationName() == null){
                            for (Address a : addresses) {
                                if(a.getAddress().equals(r.getDestinationAddress())){
                                    r.setDestinationName(a.getName());
                                    break;
                                }
                            }
                        }
                    }
                }
                catch (NullPointerException e){}
                catch (Exception e) {
                    ehs.registerException(e);
                }
                return reservations;
            }
            @Override
            protected void onPostExecute(List<Reservation> reservations) {
                if (ehs.hasExceptions()) { 
                    ehs.reportExceptions();
                } 
                else{
                    for(Reservation r:reservations){
                        if(r.getRid() == id){
                            final MapView mapView = (MapView) findViewById(R.id.mapview);
                            final List<Overlay> mapOverlays = mapView.getOverlays();
                            if(infoOverlay != null){
                                infoOverlay.hide();
                            }
                            mapOverlays.clear();
                            mapView.postInvalidate();
                            expandMap(true);
                            findViewById(R.id.get_going).setTag(r);
                            displayReservation(r);
                            break;
                        }
                    }
                }
            }
        };
        Misc.parallelExecute(tripTask);
    }
    
    private void refreshDate(){
        TextView vDate = (TextView) findViewById(R.id.date);
        Time now = new Time();
        now.setToNow();
        vDate.setText(String.valueOf(now.monthDay));
    }
    
    private OnClickListener newTripOnClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                final MapView mapView = (MapView) findViewById(R.id.mapview);
                final List<Overlay> mapOverlays = mapView.getOverlays();
                if(infoOverlay != null){
                    infoOverlay.hide();
                }
                mapOverlays.clear();
                mapView.postInvalidate();
                expandMap(true);
                Reservation r = (Reservation) v.getTag();
                findViewById(R.id.get_going).setTag(r);
                displayReservation(r);
            }
        };
    }
    
    RouteInfoOverlay infoOverlay;
    
    private synchronized int[] drawRoute (final MapView mapView, Route route, int routeNum) {
        List<Overlay> mapOverlays = mapView.getOverlays();
        Log.d("LandingActivity", String.format("mapOverlays has %d items", mapOverlays.size()));
        
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
        
        RoutePathOverlay pathOverlay = new RoutePathOverlay(this, route, RoutePathOverlay.GREEN);
        mapOverlays.add(pathOverlay);
        
        infoOverlay = new RouteInfoOverlay(mapView, route, routeNum, new GeoPoint(lat, lon), boldFont, lightFont);
        infoOverlay.setShowArrow(false);
        infoOverlay.setCallback(new OverlayCallback(){
            @Override
            public boolean onBalloonTap(int index, OverlayItem item) {
                return false;
            }
            @Override
            public void onChange() {
                infoOverlay.showOverlay();
            }
            @Override
            public boolean onTap(int index) {
                mapView.getController().setCenter(infoOverlay.getGeoPoint());
                return true;
            }
            @Override
            public boolean onClose() {
                mapView.invalidate();
                return true;
            }
            @Override
            public boolean onLongPress(int index, OverlayItem item) {
                return false;
            }
        });
        mapOverlays.add(infoOverlay);
        infoOverlay.showOverlay();
        infoOverlay.showBalloonOverlay();
        
        route.setUserId(User.getCurrentUser(this).getId());
        
        /* Add offset of 1000 to range so that map displays extra space around route. */
        int [] range = {latMax - latMin + 1500 ,lonMax - lonMin + 1500};
        
        /* Return the range to doRoute so that map can be adjusted to range settings */
        return range;
    }
    
    private void displayReservation(final Reservation reservation){
        AsyncTask<Void, Void, List<Route>> task = new AsyncTask<Void, Void, List<Route>>() {
            @Override
            protected List<Route> doInBackground(Void... params) {
                List<Route> routes = null;
                try {
                    RouteFetchRequest request = new RouteFetchRequest(
                        reservation.getNavLink(), reservation.getDepartureTime(), 
                        reservation.getDuration(), 0, 0);
                    routes = request.execute(LandingActivity.this);
                }
                catch(Exception e) {
                    ehs.registerException(e);
                }                                
                return routes;
            }
            protected void onPostExecute(java.util.List<Route> routes) {
                if (ehs.hasExceptions()) {
                    ehs.reportExceptions(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }else if(routes != null && routes.size() > 0) {
                    Route route = routes.get(0);
                    route.setCredits(reservation.getCredits());
                    route.preprocessNodes();
                    MapView mapView = (MapView) findViewById(R.id.mapview);
                    drawRoute(mapView, route, 0);
                    RouteRect routeRect = ValidationActivity.initRouteRect(route);
                    GeoPoint mid = routeRect.getMidPoint();
                    int[] range = routeRect.getRange();
                    IMapController mc = mapView.getController();
                    mc.zoomToSpan(range[0], range[1]);
                    mc.setCenter(mid);
                }
            }
        };
        Misc.parallelExecute(task);
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
    
    static class ShortcutNavigationTask extends AsyncTask<Void, Void, Void> {
        
        interface Callback {
            
            void run(Reservation reserv);
            
        }
        
        CancelableProgressDialog dialog;
        
        String address;
        
        LandingActivity activity;
        
        Context ctx;
        
        GeoPoint origin;
        
        GeoPoint dest;
        
        ExceptionHandlingService ehs;
        
        Route _route;
        
        boolean startedMakingReserv;
        
        Callback callback = new Callback() {
            @Override
            public void run(Reservation reserv) {
                Intent intent = new Intent(activity, ValidationActivity.class);
                intent.putExtra("route", reserv.getRoute());
                intent.putExtra("reservation", reserv);
                activity.startActivity(intent);
            }
        };
        
        ShortcutNavigationTask(LandingActivity ctx, ExceptionHandlingService ehs){
            this.ehs = ehs;
            this.activity = ctx;
            this.ctx = ctx;
            dialog = new CancelableProgressDialog(ctx, "Getting current location...");
            dialog.setActionListener(new CancelableProgressDialog.ActionListener() {
                @Override
                public void onClickNegativeButton() {
                    ShortcutNavigationTask.this.activity.removeLocationUpdates();
                }
            });
            dialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    ShortcutNavigationTask.this.activity.removeLocationUpdates();
                }
            });
        }
        
        ShortcutNavigationTask(Context ctx, Route route, ExceptionHandlingService ehs){
            this.ehs = ehs;
            this.ctx = ctx;
            _route = route;
            dialog = new CancelableProgressDialog(ctx, "Loading...");
        }
        
        @Override
        protected void onPreExecute() {
            dialog.show();
            if(this._route == null){
                activity.locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                if (!activity.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    SystemService.alertNoGPS(activity, true, new SystemService.Callback() {
                        @Override
                        public void onNo() {
                            if (dialog.isShowing()) {
                                dialog.cancel();
                            }
                        }
                    });
                }
                activity.locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        try{
                            activity.locationManager.removeUpdates(this);
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
                activity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, activity.locationListener);
            }
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            if(this._route == null){
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
                new AsyncTask<Void, Void, Reservation>(){
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
                                    0, 0, null, null);
                                route = routeReq.execute(ctx).get(0);
                                route.setAddresses(EditAddress.CURRENT_LOCATION, address);
                                route.setUserId(user.getId());
                            }else{
                               route = _route; 
                            }
                            ReservationRequest reservReq = new ReservationRequest(user, 
                                route, ctx.getString(R.string.distribution_date));
                            Long id = reservReq.execute(ctx);
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
                        }else if(reserv != null && callback != null){
                            callback.run(reserv);
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
            dialog.setTitle("Metropia");
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

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mConnectionProgressDialog.isShowing()) {
            // The user clicked the sign-in button already. Start to resolve
            // connection errors. Wait until onConnected() to dismiss the
            // connection dialog.
            if (result.hasResolution()) {
                    try {
                            result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                    } catch (SendIntentException e) {
                            mPlusClient.connect();
                    }
            }
        }
        // Save the intent so that we can start an activity when the user clicks
        // the sign-in button.
        mConnectionResult = result;
    }

    private void updateTitle(String name){
        TextView vTitle = (TextView) findViewById(R.id.title);
        vTitle.setText(name + ", where would you like to go?");
    }
    
    private void updateAvatar(final String url){
        AsyncTask<Void, Void, Bitmap> pictureTask = new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                Bitmap rs = null;
                InputStream is = null;
                Cache cache = Cache.getInstance(LandingActivity.this);
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
            protected void onPostExecute(final Bitmap rs) {
                if(rs != null){
                    ImageView avatar = (ImageView)findViewById(R.id.avatar);
                    avatar.setImageBitmap(rs);
                }
            }
        };
        Misc.parallelExecute(pictureTask);
    }
    
    @Override
    public void onConnected(Bundle arg0) {
        // We've resolved any connection errors.
        hideLoading();
        Person person = mPlusClient.getCurrentPerson();
        updateTitle(person.getName().getGivenName());
        Image img = person.getImage();
        if(person.hasImage() && img.hasUrl()){
            ImageView avatar = (ImageView)findViewById(R.id.avatar);
            String url = img.getUrl().replaceAll("\\?sz=50", "\\?sz=" + avatar.getHeight());
            updateAvatar(url);
        }
        mPlusClient.disconnect();
        logLinkSocial();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        uiHelper.onActivityResult(requestCode, responseCode, intent);
        if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
            mConnectionResult = null;
            mPlusClient.connect();
        }
    }

    @Override
    public void onDisconnected() {
        
    }
    
    private void showLoading(){
        if(mConnectionProgressDialog == null){
            mConnectionProgressDialog = new ProgressDialog(LandingActivity.this);
            mConnectionProgressDialog.setMessage("Loading...");
        }
        mConnectionProgressDialog.show();
    }
    
    private void hideLoading(){
        if(mConnectionProgressDialog != null){
            mConnectionProgressDialog.dismiss();
        }
    }
    
    private boolean isNotLoading(){
        return mConnectionProgressDialog == null || !mConnectionProgressDialog.isShowing();
    }
    
    private void fbLogin(){
        try{
            Session.openActiveSession(this, true, fbCallback);
        } catch(Throwable t){}
    }
    
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if(fbClicked){
            if (state == SessionState.OPENED_TOKEN_UPDATED) {
                makeMeRequest();               
            }else if(state == SessionState.OPENED && (fbPending)){
                fbPending = false;
                makeMeRequest();
            }else if(state == SessionState.CLOSED && fbPending){
                fbLogin();
            }
        }
    }
    
    private void makeMeRequest() {
        final Session session = Session.getActiveSession();
        if (session != null) {
            // Make an API call to get user data and define a 
            // new callback to handle the response.
            com.facebook.Request request = com.facebook.Request.newMeRequest(session, 
                    new com.facebook.Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    hideLoading();
                    // If the response is successful
                    if (session == Session.getActiveSession()) {
                        if (user != null) {
                            // Set the id for the ProfilePictureView
                            // view that in turn displays the profile picture.
                            updateAvatar("http://graph.facebook.com/" + user.getId() + "/picture?type=large");
                            // Set the Textview's text to the user's name.
                            updateTitle(user.getFirstName());
                            logLinkSocial();
                        }
                    }
                    if (response.getError() != null) {
                        // Handle errors, will do so later.
                    }
                }
            });
            request.executeAsync();
            showLoading();
        }
    }
    
    private void logLinkSocial(){
        if(doLog){
            doLog = false;
            SessionM.logAction("link_social");
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
        SessionM.onActivityPause(this);
    }
    
}
