package com.smartrek.activities;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import android.app.Activity;
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
import android.graphics.Color;
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
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.internal.nineoldandroids.animation.AnimatorSet;
import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;
import com.skobbler.ngx.SKMaps;
import com.smartrek.ResumeNavigationUtils;
import com.smartrek.SmarTrekApplication;
import com.smartrek.SmarTrekApplication.TrackerName;
import com.smartrek.dialogs.CancelableProgressDialog;
import com.smartrek.dialogs.NotificationDialog2;
import com.smartrek.dialogs.NotificationDialog2.ActionListener;
import com.smartrek.dialogs.NotifyResumeDialog;
import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.requests.AddressLinkRequest;
import com.smartrek.requests.CityRequest;
import com.smartrek.requests.CityRequest.City;
import com.smartrek.requests.FavoriteAddressAddRequest;
import com.smartrek.requests.FavoriteAddressDeleteRequest;
import com.smartrek.requests.FavoriteAddressFetchRequest;
import com.smartrek.requests.FavoriteAddressUpdateRequest;
import com.smartrek.requests.MyMetropiaRequest;
import com.smartrek.requests.MyMetropiaRequest.MyMetropia;
import com.smartrek.requests.Request;
import com.smartrek.requests.ReservationDeleteRequest;
import com.smartrek.requests.ReservationListFetchRequest;
import com.smartrek.requests.ReservationRequest;
import com.smartrek.requests.RouteFetchRequest;
import com.smartrek.requests.UpdateDeviceIdRequest;
import com.smartrek.requests.WhereToGoRequest;
import com.smartrek.ui.ClickAnimation;
import com.smartrek.ui.ClickAnimation.ClickAnimationEndCallback;
import com.smartrek.ui.DelayTextWatcher;
import com.smartrek.ui.DelayTextWatcher.TextChangeListener;
import com.smartrek.ui.EditAddress;
import com.smartrek.ui.SwipeDismissListViewTouchListener;
import com.smartrek.ui.SwipeDismissTouchListener;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.ui.overlays.CurrentLocationOverlay;
import com.smartrek.ui.overlays.EventOverlay;
import com.smartrek.ui.overlays.OverlayCallback;
import com.smartrek.ui.overlays.POIOverlay;
import com.smartrek.ui.overlays.POIOverlay.POIActionListener;
import com.smartrek.ui.overlays.RouteDestinationOverlay;
import com.smartrek.ui.overlays.RoutePathOverlay;
import com.smartrek.ui.timelayout.AdjustableTime;
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
import com.smartrek.utils.SmartrekTileProvider;
import com.smartrek.utils.SystemService;

public final class LandingActivity2 extends FragmentActivity implements SensorEventListener{ 
    
    private static final int DEFAULT_ZOOM_LEVEL = 13;
    
    private static final int SEARCH_ZOOM_LEVEL = 16;
    
    private static final double mapZoomVerticalOffset = 0.3;

    public static final String LAT = "lat";
    
    public static final String LON = "lon";
    
    public static final String MSG = "msg";
    
    public static final boolean ENABLED = true;
    
    public static final String NO_TRIPS = "No Upcoming Trip";
    
    public static final int ON_MY_WAY = Integer.valueOf(100);
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
    CurrentLocationOverlay myPointOverlay;
    
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
    
    private List<Address> searchAddresses = new ArrayList<Address>();
    private List<Address> fromSearchAddresses = new ArrayList<Address>();
    
    private AtomicBoolean canDrawReservRoute = new AtomicBoolean();
    
    private ListView searchResultList;
    
    private ListView fromSearchResultList;
    
    private ArrayAdapter<Address> autoCompleteAdapter;
    
    private ArrayAdapter<Address> fromAutoCompleteAdapter;
    
    private static final String NO_AUTOCOMPLETE_RESULT = "No results found.";
    
    private static final String SEARCHING = "Searching...";
    
    private static final String TAP_TO_ADD_FAVORITE = "Tap to Add Favorite";
    
    private List<Address> favSearchAddresses = new ArrayList<Address>();
    
    private EditText favSearchBox;
    
    private ListView favSearchResultList;
    
    private ArrayAdapter<Address> favAutoCompleteAdapter;
    
    private ImageView starView;
    
    private ImageView homeView;
    
    private ImageView workView;
    
    private AtomicBoolean showAutoComplete = new AtomicBoolean(true);
    
    private ListView reservationListView;
    private ArrayAdapter<Reservation> reservationAdapter;
    
    private ImageView tripNotifyIcon;
    
    private TextView getRouteView;
    
    private POIOverlay curFrom;
    private POIOverlay curTo;
    
    private String curFromProvider;
    private long curFromTime;
    
    private EditText searchBox;
    
    private EditText fromSearchBox;
    
    private TextView upointView;
    private TextView saveTimeView;
    private TextView co2View;
    
    private AtomicBoolean needCheckResume = new AtomicBoolean(true);
    
    //debug
//    private GeoPoint debugOrigin = new GeoPoint(33.8689924, -117.9220526);
    
    private int calculateZoomLevel(double lat){
        long sideDistanceOfSquareArea = 10; //miles
        long earthCircumference = 24901;
        int screenPixels = Dimension.pxToDp(getWindowManager().getDefaultDisplay().getWidth(), getResources().getDisplayMetrics());
        return Double.valueOf(Math.floor(
            log2(screenPixels * earthCircumference * Math.cos(Math.toRadians(lat)) / sideDistanceOfSquareArea)
        )).intValue() - 10;
    }
    
    private static double log2(double x){
        return Math.log(x)/Math.log(2); 
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing2);
        
        registerReceiver(tripInfoCachedUpdater, new IntentFilter(TRIP_INFO_CACHED_UPDATES));
        registerReceiver(updateMyLocation, new IntentFilter(UPDATE_MY_LOCATION));
        registerReceiver(updateMenuMyTrips, new IntentFilter(UPDATE_MENU_MY_TRIPS));
        
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
        
        searchResultList = (ListView) findViewById(R.id.search_result_list);
        fromSearchResultList = (ListView) findViewById(R.id.from_search_result_list);
        searchBox = (EditText) findViewById(R.id.search_box);
        searchBox.setHint(Html.fromHtml("<b>Enter Destination</b>"));
        fromSearchBox = (EditText) findViewById(R.id.from_search_box);
        fromSearchBox.setHint(Html.fromHtml("<b>Current Location</b>"));
        autoCompleteAdapter = createAutoCompleteAdapter(searchBox);
        fromAutoCompleteAdapter = createAutoCompleteAdapter(fromSearchBox);
        searchResultList.setAdapter(autoCompleteAdapter);
        fromSearchResultList.setAdapter(fromAutoCompleteAdapter);
        
        refreshSearchAutoCompleteData();
        refreshFromSearchAutoCompleteData();
        
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                final String addrInput = v.getText().toString();
                boolean handled = StringUtils.isNotBlank(addrInput);
                if(handled){
                    searchAddress(addrInput, true);
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    clearSearchResult();
                }
                return handled;
            }
        });
        fromSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                final String addrInput = v.getText().toString();
                boolean handled = StringUtils.isNotBlank(addrInput);
                if(handled){
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    clearFromSearchResult();
                }
                return handled;
            }
        });
        searchBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                	InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    showAutoComplete.set(false);
                    if(curTo != null) {
                    	curTo.showMiniBalloonOverlay();
                    }
                    searchResultList.setVisibility(View.GONE);
                    fromSearchResultList.setVisibility(View.GONE);
                }
                else {
                    showAutoComplete.set(true);
                    if(curFrom != null) {
                    	curFrom.showMiniBalloonOverlay();
                    }
                    if(StringUtils.isBlank(searchBox.getText())) {
                    	searchAddresses.clear();
                    }
                    refreshSearchAutoCompleteData();
                }
            }
        });
        fromSearchBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    showAutoComplete.set(false);
                    if(curFrom != null) {
                    	curFrom.showMiniBalloonOverlay();
                    }
                    searchResultList.setVisibility(View.GONE);
                    fromSearchResultList.setVisibility(View.GONE);
                }
                else {
                    showAutoComplete.set(true);
                    if(curTo != null) {
                    	curTo.showMiniBalloonOverlay();
                    }
                    if(StringUtils.isBlank(fromSearchBox.getText())) {
                        fromSearchAddresses.clear();
                    }
                    refreshFromSearchAutoCompleteData();
                }
            }
        });
        searchResultList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	Address selected = (Address)parent.getItemAtPosition(position);
            	if(StringUtils.isNotBlank(selected.getAddress())) {
            		dropPinForAddress(selected, true);
	                searchBox.setText(selected.getAddress());
	                InputMethodManager imm = (InputMethodManager)getSystemService(
	                        Context.INPUT_METHOD_SERVICE);
	                imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
	                showAutoComplete.set(false);
	                clearSearchResult();
            	}
            	else if(TAP_TO_ADD_FAVORITE.equals(selected.getName())) {
            		clearSearchResult();
            		removePOIMarker(mapView);
            		hideBulbBalloon();
            		hideStarredBalloon();
            		findViewById(R.id.landing_panel).setVisibility(View.GONE);
            		findViewById(R.id.fav_opt).setVisibility(View.VISIBLE);
            	}
            }
        });
        fromSearchResultList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address selected = (Address)parent.getItemAtPosition(position);
                if(StringUtils.isNotBlank(selected.getAddress())) {
                	dropPinForAddress(selected, true);
                    fromSearchBox.setText(selected.getAddress());
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(fromSearchBox.getWindowToken(), 0);
                    showAutoComplete.set(false);
                    clearFromSearchResult();
                }
                else if(TAP_TO_ADD_FAVORITE.equals(selected.getName())) {
                    clearFromSearchResult();
                    removePOIMarker(mapView);
                    hideBulbBalloon();
                    hideStarredBalloon();
                    findViewById(R.id.landing_panel).setVisibility(View.GONE);
                    findViewById(R.id.fav_opt).setVisibility(View.VISIBLE);
                }
            }
        });
        searchResultList.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
                return false;
			}
		});
        fromSearchResultList.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(fromSearchBox.getWindowToken(), 0);
                return false;
            }
        });
        final View searchBoxClear = findViewById(R.id.search_box_clear);
        DelayTextWatcher delayTextWatcher = new DelayTextWatcher(searchBox, new TextChangeListener(){
			@Override
			public void onTextChanged(CharSequence text) {
				searchBoxClear.setVisibility(StringUtils.isBlank(text)?View.GONE:View.VISIBLE); 
                final String addrInput = text.toString();
                if(StringUtils.isNotBlank(addrInput)) {
                	AsyncTask<Void, Void, List<Address>> searchPoiTask = new AsyncTask<Void, Void, List<Address>>(){
                		@Override
                		protected void onPreExecute() {
                			
                		}
                		
        				@Override
        				protected List<Address> doInBackground(Void... params) {
        					List<Address> addresses = new ArrayList<Address>();
        					try {
        						if(lastLocation != null) {
        							addresses = Geocoding.searchPoi(LandingActivity2.this, addrInput, lastLocation.getLatitude(), lastLocation.getLongitude());
        						}
        						else {
        							addresses = Geocoding.searchPoi(LandingActivity2.this, addrInput);
        						}
        					}
        					catch(Exception e) {
        						Log.e("LandingActivity2", "search error!");
        					}
        					return addresses;
        				}
        				
        				@Override
        				protected void onPostExecute(List<Address> addresses) {
        					searchAddresses.clear();
        					for(Address a:addresses){
        					    if(StringUtils.isNotBlank(a.getAddress())){
        					        searchAddresses.add(a);
        					    }
        					}
        					if(searchAddresses.isEmpty()) {
        						Address notFound = new Address();
        						notFound.setName(NO_AUTOCOMPLETE_RESULT);
        						notFound.setAddress("");
        						searchAddresses.add(notFound);
        						List<Address> emptyAddresses = getEmptyAddressesForUI();
        						searchAddresses.addAll(emptyAddresses);
        					}
        					refreshSearchAutoCompleteData();
        				}
                	};
                	Misc.parallelExecute(searchPoiTask); 
                }
                else {
                	clearSearchResult();
                }
			}

			@Override
			public void onTextChanging() {
				if(searchAddresses.isEmpty()) {
					Address searching = new Address();
					searching.setName(SEARCHING);
					searching.setAddress("");
					searchAddresses.add(searching);
				}
				else {
					boolean hasResult = false;
					for(Address addr : searchAddresses) {
						if(StringUtils.isNotBlank(addr.getAddress())) {
							hasResult = true;
						}
					}
					if(!hasResult) {
						searchAddresses.clear();
						Address searching = new Address();
						searching.setName(SEARCHING);
						searching.setAddress("");
						searchAddresses.add(searching);
					}
				}
				refreshSearchAutoCompleteData();
			}
		}, 500);
        
        searchBox.addTextChangedListener(delayTextWatcher);
        searchBoxClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBox.setText("");
                clearSearchResult();
            }
        });
        final View fromSearchBoxClear = findViewById(R.id.from_search_box_clear);
        DelayTextWatcher fromDelayTextWatcher = new DelayTextWatcher(fromSearchBox, new TextChangeListener(){
            @Override
            public void onTextChanged(CharSequence text) {
                fromSearchBoxClear.setVisibility(StringUtils.isBlank(text)?View.GONE:View.VISIBLE); 
                final String addrInput = text.toString();
                if(StringUtils.isNotBlank(addrInput)) {
                    AsyncTask<Void, Void, List<Address>> searchPoiTask = new AsyncTask<Void, Void, List<Address>>(){
                        @Override
                        protected List<Address> doInBackground(Void... params) {
                            List<Address> addresses = new ArrayList<Address>();
                            try {
                                if(lastLocation != null) {
                                    addresses = Geocoding.searchPoi(LandingActivity2.this, addrInput, lastLocation.getLatitude(), lastLocation.getLongitude());
                                }
                                else {
                                    addresses = Geocoding.searchPoi(LandingActivity2.this, addrInput);
                                }
                            }
                            catch(Exception e) {
                                Log.e("LandingActivity2", "search error!");
                            }
                            return addresses;
                        }
                        
                        @Override
                        protected void onPostExecute(List<Address> addresses) {
                            fromSearchAddresses.clear();
                            for(Address a:addresses){
                                if(StringUtils.isNotBlank(a.getAddress())){
                                    fromSearchAddresses.add(a);
                                }
                            }
                            if(fromSearchAddresses.isEmpty()) {
                                Address notFound = new Address();
                                notFound.setName(NO_AUTOCOMPLETE_RESULT);
                                notFound.setAddress("");
                                fromSearchAddresses.add(notFound);
                                List<Address> emptyAddresses = getEmptyAddressesForUI();
                                fromSearchAddresses.addAll(emptyAddresses);
                            }
                            refreshFromSearchAutoCompleteData();
                        }
                    };
                    Misc.parallelExecute(searchPoiTask); 
                }
                else {
                    clearFromSearchResult();
                }
            }
            
            @Override
			public void onTextChanging() {
				if(fromSearchAddresses.isEmpty()) {
					Address searching = new Address();
					searching.setName(SEARCHING);
					searching.setAddress("");
					fromSearchAddresses.add(searching);
				}
				else {
					boolean hasResult = false;
					for(Address addr : fromSearchAddresses) {
						if(StringUtils.isNotBlank(addr.getAddress())) {
							hasResult = true;
						}
					}
					if(!hasResult) {
						fromSearchAddresses.clear();
						Address searching = new Address();
						searching.setName(SEARCHING);
						searching.setAddress("");
						fromSearchAddresses.add(searching);
					}
				}
				refreshFromSearchAutoCompleteData();
			}
        }, 500);
        fromSearchBox.addTextChangedListener(fromDelayTextWatcher);
        fromSearchBoxClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fromSearchBox.setText("");
                clearFromSearchResult();
            }
        });
        
        final String intentAddress = getIntentAddress(getIntent());
        boolean hasIntentAddr = StringUtils.isNotBlank(intentAddress); 
        mapRezoom.set(!hasIntentAddr);
        if(hasIntentAddr){
        	searchIntentAddress(intentAddress);
        }
        
        LandingActivity.initializeIfNeccessary(this, new Runnable() {
            @Override
            public void run() {
                updateDeviceId();
            }
        });
        
        initReservationListView();
        
        final IMapController mc = mapView.getController();
        
        locationListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location location) {
//                fake lat-lon
//                location.setLatitude(34.0291747); // LA
//                location.setLongitude(-118.2734106);
//                location.setLatitude(32.1559094); // Tucson
//                location.setLongitude(-110.883805);
                if (ValidationActivity.isBetterLocation(location, lastLocation)) {
                    lastLocation = location;
                    final boolean refresh = mapRefresh.getAndSet(false);
                    final boolean alertAvailability = mapAlertAvailability.getAndSet(false);
                    final boolean rezoom = mapRezoom.getAndSet(false);
                    final double lat = location.getLatitude();
                    final double lon = location.getLongitude();
                    refreshMyLocation(lat, lon);
                    popupResumeNavigationIfNeccessary();
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
                                refreshCobranding(lat, lon, alertAvailability, new Runnable() {
                                    public void run() {
                                        refreshBulbPOIs(lat , lon, rezoom);
                                        if(!canDrawReservRoute.getAndSet(true)) {
                                        	refreshTripsInfo();
                                        }
                                    }
                                });
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
        
        View centerMapIcon = findViewById(R.id.center_map_icon);
        centerMapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
            	clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						IGeoPoint mapCenter = mapView.getMapCenter();
		                int latE6 = mapCenter.getLatitudeE6();
		                int lonE6 = mapCenter.getLongitudeE6();
		                int lastLatE6 = mapCenterLat.get();
		                int lastLonE6 = mapCenterLon.get();
		                int threshold = 100 + 2300 * (Math.max(18 - mapView.getZoomLevel(), 0));
		                if(Math.abs(latE6 - lastLatE6) < threshold && Math.abs(lonE6 - lastLonE6) < threshold){
		                    if(mapView.getZoomLevel() == ValidationActivity.DEFAULT_ZOOM_LEVEL){
		                    	if(routeRect != null) {
		                    		zoomMapToFitBulbPOIs();
		                    	}
		                    	else {
		                    	    mc.setZoom(calculateZoomLevel(mapCenter.getLatitude()));
		                    	}
		                    }else{
		                        mc.setZoom(ValidationActivity.DEFAULT_ZOOM_LEVEL);
		                        if(myPointOverlay != null){
		                            mc.setCenter(myPointOverlay.getLocation());
		                        }
		                    }
		                }else{
		                    lastLocation = null;
		                    mapRecenter.set(true);
		                    prepareGPS();
		                }
					}
				});
            }
        });
        
        favSearchBox = (EditText) findViewById(R.id.favorite_search_box);
        favSearchResultList = (ListView) findViewById(R.id.fav_search_result);
        favAutoCompleteAdapter = createAutoCompleteAdapter(favSearchBox);
        favSearchResultList.setAdapter(favAutoCompleteAdapter);
        
        refreshFavAutoCompleteData();
        favSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                final String addrInput = v.getText().toString();
                boolean handled = StringUtils.isNotBlank(addrInput);
                if(handled){
                    searchFavAddress(addrInput, true);
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    showAutoComplete.set(false);
                    clearFavSearchResult();
                }
                return handled;
            }
        });
        favSearchBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                	InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    showAutoComplete.set(false);
                    clearFavSearchResult();
                }
                else {
                	showAutoComplete.set(true);
                }
            }
        });
        favSearchResultList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	Address selected = (Address)parent.getItemAtPosition(position);
            	if(StringUtils.isNotBlank(selected.getAddress())) {
            		dropPinForAddress(selected, true);
	                InputMethodManager imm = (InputMethodManager)getSystemService(
	                        Context.INPUT_METHOD_SERVICE);
	                imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
	                showAutoComplete.set(false);
	                clearFavSearchResult();
            	}
            }
        });
        favSearchResultList.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
				return false;
			}
        	
        });
        final View favSearchBoxClear = findViewById(R.id.fav_search_box_clear);
        final View favSave = findViewById(R.id.fav_save);
        DelayTextWatcher delayFavTextWatcher = new DelayTextWatcher(favSearchBox, new TextChangeListener(){
			@Override
			public void onTextChanged(CharSequence text) {
				favSearchBoxClear.setVisibility(StringUtils.isBlank(text)||!favSearchBox.isEnabled()?View.GONE:View.VISIBLE); 
				favSave.setVisibility(StringUtils.isBlank(text)?View.GONE:View.VISIBLE);
                final String addrInput = text.toString();
                if(StringUtils.isNotBlank(addrInput) && favSearchBox.isEnabled()) {
                	AsyncTask<Void, Void, List<Address>> searchPoiTask = new AsyncTask<Void, Void, List<Address>>(){
        				@Override
        				protected List<Address> doInBackground(Void... params) {
        					List<Address> addresses = new ArrayList<Address>();
        					try {
        						if(lastLocation != null) {
        							addresses = Geocoding.searchPoi(LandingActivity2.this, addrInput, lastLocation.getLatitude(), lastLocation.getLongitude());
        						}
        						else {
        							addresses = Geocoding.searchPoi(LandingActivity2.this, addrInput);
        						}
        					}
        					catch(Exception e) {
        						Log.e("LandingActivity2", "search error!");
        					}
        					return addresses;
        				}
        				
        				@Override
        				protected void onPostExecute(List<Address> addresses) {
        					favSearchAddresses.clear();
        					for(Address a:addresses){
        					    if(StringUtils.isNotBlank(a.getAddress())){
        					    	favSearchAddresses.add(a);
        					    }
        					}
        					if(favSearchAddresses.isEmpty()) {
        						Address notFound = new Address();
        						notFound.setName(NO_AUTOCOMPLETE_RESULT);
        						notFound.setAddress("");
        						favSearchAddresses.add(notFound);
        						List<Address> emptyAddresses = getEmptyAddressesForUI();
        						favSearchAddresses.addAll(emptyAddresses);
        					}
        					refreshFavAutoCompleteData();
        				}
                	};
                	Misc.parallelExecute(searchPoiTask); 
                }
                else {
                	favSearchAddresses.clear();
            		favAutoCompleteAdapter.clear();
            		refreshFavAutoCompleteData();
                }
			}
			
			@Override
			public void onTextChanging() {
				if(favSearchBox.isEnabled()) {
					if(favSearchAddresses.isEmpty()) {
						Address searching = new Address();
						searching.setName(SEARCHING);
						searching.setAddress("");
						favSearchAddresses.add(searching);
					}
					else {
						boolean hasResult = false;
						for(Address addr : favSearchAddresses) {
							if(StringUtils.isNotBlank(addr.getAddress())) {
								hasResult = true;
							}
						}
						if(!hasResult) {
							favSearchAddresses.clear();
							Address searching = new Address();
							searching.setName(SEARCHING);
							searching.setAddress("");
							favSearchAddresses.add(searching);
						}
					}
				}
				refreshFavAutoCompleteData();
			}
		}, 500);
        
        favSearchBox.addTextChangedListener(delayFavTextWatcher);
        favSearchBoxClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                favSearchBox.setText("");
            }
        });
        
        View favSearchButton = findViewById(R.id.favorite_search_button);
        favSearchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String addrInput = favSearchBox.getText().toString();
                boolean handled = StringUtils.isNotBlank(addrInput);
                if(handled){
                	searchFavAddress(addrInput, true);
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    showAutoComplete.set(false);
                    clearFavSearchResult();
                }
			}
		});
        
        final EditText labelInput = (EditText) findViewById(R.id.label_input);
        final View labelInputClear = findViewById(R.id.label_clear);
        labelInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if(StringUtils.isNotBlank(s.toString())) {
					labelInputClear.setVisibility(View.VISIBLE);
				}
				else {
					labelInputClear.setVisibility(View.GONE);
				}
			}
		});
        
        labelInputClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				labelInput.setText("");
			}
		});
        
        starView = (ImageView) findViewById(R.id.star);
        homeView = (ImageView) findViewById(R.id.home);
        workView = (ImageView) findViewById(R.id.work);
        
        starView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				unSelectAllIcon();
				starView.setImageResource(R.drawable.star);
				findViewById(R.id.icon).setTag(IconType.star);
			}
		});
        
        homeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				unSelectAllIcon();
				homeView.setImageResource(R.drawable.home);
				findViewById(R.id.icon).setTag(IconType.home);
			}
		});
        
        workView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				unSelectAllIcon();
				workView.setImageResource(R.drawable.work);
				findViewById(R.id.icon).setTag(IconType.work);
			}
		});
        
        findViewById(R.id.fav_cancel).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				hideFavoriteOptPanel();
			}
		});
        
        favSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isFavoriteOptComplete()) {
					InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					View favOptPanel = findViewById(R.id.fav_opt);
					BalloonModel model = (BalloonModel) favOptPanel.getTag();
					final BalloonModel _model = model==null?new BalloonModel():model;
					String label = ((EditText)favOptPanel.findViewById(R.id.label_input)).getText().toString();
					if(StringUtils.isBlank(label)) {
						label = "Favorite";
					}
					final String lbl = label;
	                final String addr = ((EditText)favOptPanel.findViewById(R.id.favorite_search_box)).getText().toString();
	                final IconType icon = (IconType) favOptPanel.findViewById(R.id.icon).getTag();
	                AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>(){
	                	@Override
	                	protected void onPreExecute() {
	                		if(_model.lat == 0 && _model.lon == 0) {
	                			List<Address> result = Collections.emptyList();
	                			try {
		                			if(lastLocation != null) {
		                				result = Geocoding.searchPoi(LandingActivity2.this, addr, lastLocation.getLatitude(), lastLocation.getLongitude());
		                			}
		                			else {
		                				result = Geocoding.searchPoi(LandingActivity2.this, addr);
		                			}
	                			}
	                			catch(Exception e) {
	                				e.printStackTrace();
	                				ehs.registerException(e, e.getMessage());
	                			}
	                			if(result.isEmpty()) {
	                				ehs.registerException(new RuntimeException(), "Address [" + addr + "] not found!");
	                			}
	                			else {
	                				Address found = result.get(0);
	                				_model.address = found.getAddress();
	                				_model.lat = found.getLatitude();
	                				_model.lon = found.getLongitude();
	                				_model.geopoint = found.getGeoPoint();
	                			}
	                		}
	                		else {
	                			_model.address = addr;
	                		}
	                	}
	                	
	                    @Override
	                    protected Integer doInBackground(Void... params) {
	                        Integer id = 0;
	                        Request req = null;
	                        String iconName = icon!=null?icon.name():IconType.star.name();
	                        User user = User.getCurrentUser(LandingActivity2.this);
	                        try {
	                        	if(_model.id==0) {
		                            FavoriteAddressAddRequest request = new FavoriteAddressAddRequest(
		                                user, lbl, _model.address, iconName, _model.lat, _model.lon);
		                            req = request;
		                            id = request.execute(LandingActivity2.this);
	                        	}
	                        	else {
	                        		FavoriteAddressUpdateRequest request = new FavoriteAddressUpdateRequest(
		                                    new AddressLinkRequest(user).execute(LandingActivity2.this),
		                                        _model.id, user, lbl, addr, iconName, _model.lat, _model.lon);
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
	                        refreshStarredPOIs();
	                        if (ehs.hasExceptions()) {
	                            ehs.reportExceptions();
	                        }
	                        else {
	                            removePOIMarker(mapView);
	                            _model.id = _model.id!=0?_model.id:id;
	                            reInitFavoriteOperationPanel();
	                            findViewById(R.id.fav_opt).setVisibility(View.GONE);
	                            findViewById(R.id.landing_panel).setVisibility(View.VISIBLE);
	                        }
	                    }
	               };
	               Misc.parallelExecute(task);
				}
            }
		});
        
        findViewById(R.id.fav_del).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						findViewById(R.id.confirm_panel).setVisibility(View.VISIBLE);
					}
				});
			}
		});
        
        OnClickListener noopClick = new OnClickListener() {
            @Override
            public void onClick(View v) {}
        };
        
        findViewById(R.id.confirm_panel).setOnClickListener(noopClick);
        
        findViewById(R.id.confirm_del).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation  = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						findViewById(R.id.confirm_panel).setVisibility(View.GONE);
						final View favOpterationPanel = findViewById(R.id.fav_opt);
						final BalloonModel model = (BalloonModel) favOpterationPanel.getTag();
						final int oldId = model.id;
		                AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>(){
		                    @Override
		                    protected void onPreExecute() {
		                        List<Overlay> overlays = mapView.getOverlays();
		                        List<Overlay> overlaysToKeep = new ArrayList<Overlay>();
		                        for (Overlay overlay : overlays) {
		                            boolean toKeep;
		                            if(overlay instanceof POIOverlay){
		                                POIOverlay poiOverlay = (POIOverlay)overlay;
		                                toKeep = !isFavoriteMark(poiOverlay.getMarker()) || poiOverlay.getAid() != model.id;
		                            }else{
		                                toKeep = true;
		                            }
		                            if(toKeep){
		                                overlaysToKeep.add(overlay);
		                            }
		                        }
		                        overlays.clear();
		                        overlays.addAll(overlaysToKeep);
		                        mapView.postInvalidate();
		                        model.id = 0;
		                    }
		                    @Override
		                    protected Integer doInBackground(Void... params) {
		                        Integer id = null;
		                        Request req = null;
		                        User user = User.getCurrentUser(LandingActivity2.this);
		                        try {
		                            FavoriteAddressDeleteRequest request = new FavoriteAddressDeleteRequest(
		                                    new AddressLinkRequest(user).execute(LandingActivity2.this), user, oldId);
		                            req = request;
		                            request.execute(LandingActivity2.this);
		                        }
		                        catch (Exception e) {
		                            ehs.registerException(e, "[" + (req==null?"":req.getUrl()) + "]\n" + e.getMessage());
		                        }
		                        return id;
		                    }
		                    protected void onPostExecute(Integer id) {
		                    	favOpterationPanel.setTag(null);
		                        refreshStarredPOIs();
		                        clearFavSearchResult();
		                        if (ehs.hasExceptions()) {
		                            ehs.reportExceptions();
		                        }
		                    }
		               };
		               Misc.parallelExecute(task);
		               reInitFavoriteOperationPanel();
		               favOpterationPanel.setVisibility(View.GONE);
					   findViewById(R.id.landing_panel).setVisibility(View.VISIBLE);
					}
				});
			}
        });
        
        findViewById(R.id.confirm_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						findViewById(R.id.confirm_panel).setVisibility(View.GONE);
					}
				});
			}
		});
        
        Display display = getWindowManager().getDefaultDisplay();
        DrawerLayout.LayoutParams leftDrawerLp = (DrawerLayout.LayoutParams) findViewById(R.id.left_drawer).getLayoutParams();
        leftDrawerLp.width=display.getWidth()*3/4 + Dimension.dpToPx(10, getResources().getDisplayMetrics());
        View menuPanel = findViewById(R.id.menu_panel);
        LayoutParams menuPanelLp = menuPanel.getLayoutParams();
        menuPanelLp.width=display.getWidth()*3/4;
        
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        View drawerIcon = findViewById(R.id.drawer_menu_icon);
        drawerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(findViewById(R.id.left_drawer));
            }
        });
        TextView newTripMenu = (TextView) findViewById(R.id.new_trip);
        newTripMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						mDrawerLayout.closeDrawer(findViewById(R.id.left_drawer));
					}
				});
			}
		});
        TextView myMetropiaMenu = (TextView) findViewById(R.id.dashboard);
        myMetropiaMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	final int viewId = v.getId();
            	ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
            	clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						MainMenu.onMenuItemSelected(LandingActivity2.this, 0, viewId);
					}
				});
            }
        });
        TextView myTripsMenu = (TextView) findViewById(R.id.my_trips);
        myTripsMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int viewId = v.getId();
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						MainMenu.onMenuItemSelected(LandingActivity2.this, 0, viewId);
					}
				});
			}
        });
        if(MyTripsActivity.hasUrl(LandingActivity2.this)) {
        	myTripsMenu.setVisibility(View.VISIBLE);
        }
        TextView reservationsMenu = (TextView) findViewById(R.id.reservations);
        reservationsMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						lockMenu();
						findViewById(R.id.reservations_list).setVisibility(View.VISIBLE);
					}
				});
			}
        });
        TextView shareMenu = (TextView) findViewById(R.id.share_menu);
        shareMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	final int viewId = v.getId();
            	ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
            	clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						MainMenu.onMenuItemSelected(LandingActivity2.this, 0, viewId);
					}
				});
            }
        });
        TextView feedbackMenu = (TextView) findViewById(R.id.feedback_menu);
        feedbackMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
            	clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Intent intent = new Intent(LandingActivity2.this, FeedbackActivity.class);
						startActivity(intent);
					}
				});
            }
        });
        TextView rewardsMenu = (TextView) findViewById(R.id.rewards_menu);
        rewardsMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
                clickAnimation.startAnimation(new ClickAnimationEndCallback() {
                    @Override
                    public void onAnimationEnd() {
                        Intent intent = new Intent(LandingActivity2.this, RewardsActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
        if(RewardsActivity.hasUrl(this)){
            findViewById(R.id.rewards_menu_panel).setVisibility(View.VISIBLE);
        }
        TextView settingsMenu = (TextView) findViewById(R.id.map_display_options);
        settingsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	final int viewId = v.getId();
            	ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
            	clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						MainMenu.onMenuItemSelected(LandingActivity2.this, 0, viewId);
					}
				});
            }
        });
        settingsMenu.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
            	v.startAnimation(AnimationUtils.loadAnimation(LandingActivity2.this, R.anim.click_animation));
                MainMenu.onMenuItemSelected(LandingActivity2.this, 0, R.id.debug_options);
                return true;
            }                
        });
        
        final TextView userInfoView = (TextView) findViewById(R.id.user_info);
        
        LandingActivity.initializeIfNeccessary(this, new Runnable() {
            @Override
            public void run() {
            	User user = User.getCurrentUser(LandingActivity2.this);
                userInfoView.setText(user.getFirstname() + " " + user.getLastname());
            }
        });
        
        final View activityRootView = findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mapView.postInvalidate();
             }
        });
        
        TextView osmCredit = (TextView) findViewById(R.id.osm_credit);
        RelativeLayout.LayoutParams osmCreditLp = (RelativeLayout.LayoutParams) osmCredit.getLayoutParams();
        osmCreditLp.bottomMargin = Dimension.dpToPx(48, getResources().getDisplayMetrics());
        osmCredit.setLayoutParams(osmCreditLp);
        
        findViewById(R.id.left_drawer).setOnClickListener(noopClick);
        
        getRouteView = (TextView) findViewById(R.id.get_route);
        getRouteView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						searchBox.setText("");
						clearSearchResult();
						fromSearchBox.setText("");
						clearFromSearchResult();
						startRouteActivity(mapView);
					}
				});
			}
        });
        
        scheduleNextTripInfoUpdates();
        
        upointView = (TextView) findViewById(R.id.upoint);
        upointView.setText(formatMyMetropiaInfo("000Pts"));
        saveTimeView = (TextView)findViewById(R.id.save_time);
        saveTimeView.setText(formatMyMetropiaInfo("00Min"));
        co2View = (TextView) findViewById(R.id.co2);
        co2View.setText(formatMyMetropiaInfo("000lbs"));
        
        findViewById(R.id.my_metropia_panel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(WebMyMetropiaActivity.hasUrl(LandingActivity2.this)){
	               Intent intent = new Intent(LandingActivity2.this, WebMyMetropiaActivity.class);
	               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	               startActivity(intent);
	            }else{
	               Intent intent = new Intent(LandingActivity2.this, MyMetropiaActivity.class);
	               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	               startActivity(intent);
	            }
			}
        });
        
        AssetManager assets = getAssets();
        Font.setTypeface(Font.getLight(assets), osmCredit, searchBox, fromSearchBox, myMetropiaMenu, 
            reservationsMenu, shareMenu, feedbackMenu, rewardsMenu, settingsMenu, userInfoView, myTripsMenu);
        Font.setTypeface(Font.getMedium(assets), favSearchBox, labelInput, 
        		(TextView)findViewById(R.id.label), (TextView)findViewById(R.id.icon), getRouteView, 
        		upointView, saveTimeView, co2View, (TextView) findViewById(R.id.head));
        //init Tracker
        ((SmarTrekApplication)getApplication()).getTracker(TrackerName.APP_TRACKER);
        showTutorialIfNessary();
    }
    
    private void showTutorialIfNessary() {
    	SharedPreferences prefs = Preferences.getGlobalPreferences(this);
    	int tutorialFinish = prefs.getInt(Preferences.Global.TUTORIAL_FINISH, 0);
    	if(tutorialFinish != TutorialActivity.TUTORIAL_FINISH) {
    		Intent intent = new Intent(this, TutorialActivity.class);
            startActivity(intent);
    	}
    }
    
    private void popupResumeNavigationIfNeccessary() {
    	if(lastLocation != null && needCheckResume.get()) {
    		needCheckResume.set(false);
    		GeoPoint loc = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
    		final String resumeReservationId = ResumeNavigationUtils.getInterruptRId(LandingActivity2.this, loc);
    		if(resumeReservationId != null) {
    			AsyncTask<Void, Void, Reservation> task = new AsyncTask<Void, Void, Reservation>() {
					@Override
					protected Reservation doInBackground(Void... params) {
						try {
							Reservation reser = null;
							ReservationListFetchRequest resReq = new ReservationListFetchRequest(User.getCurrentUser(LandingActivity2.this));
							List<Reservation> reservs = resReq.execute(LandingActivity2.this);
							for(Reservation reservation : reservs) {
								Log.d("NotifyResumeDialog", reservation.getRid() + "");
								if(StringUtils.endsWithIgnoreCase(resumeReservationId, reservation.getRid()+"")) {
									reser = reservation;
								}
							}
							return reser;
						}
						catch(Exception e) {}
						return null;
					}
					
					@Override
					protected void onPostExecute(final Reservation reser) {
						if(reser != null) {
							NotifyResumeDialog dialog = new NotifyResumeDialog(LandingActivity2.this);
			    			dialog.setYesListener(new NotifyResumeDialog.ActionListener() {
								@Override
								public void onClick() {
									startValidationActivity(reser);
									
								};
			    			});
			    			dialog.show();
						}
					}
    			};
    			Misc.parallelExecute(task);
    		}
    	}
    }
    
    private Integer EMPTY_ITEM_SIZE = 5;
    
    private List<Address> getEmptyAddressesForUI() {
    	List<Address> emptyAddresses = new ArrayList<Address>();
    	for(int i = 0 ; i < EMPTY_ITEM_SIZE ; i++) {
    		Address empty = new Address();
    		empty.setAddress("");
    		empty.setDistance("");
    		empty.setName("");
    		emptyAddresses.add(empty);
    	}
    	return emptyAddresses;
    }
    
    private Long dismissReservId = Long.valueOf(-1);
    private Boolean swipeRight = Boolean.FALSE;
    
    private void initReservationListView() {
    	tripNotifyIcon = (ImageView) findViewById(R.id.trip_notify_icon);
    	tripNotifyIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						showTripInfoPanel(true, true);
					}
				});
			}
		});
    	final View tripInfoPanel = findViewById(R.id.trip_info);
        tripInfoPanel.setOnTouchListener(new SwipeDismissTouchListener(tripInfoPanel, null, new SwipeDismissTouchListener.OnDismissCallback() {
			@Override
			public void onDismiss(View view, Object token, boolean dismissRight) {
				hideTripInfoPanel();
				Reservation res = (Reservation)tripInfoPanel.getTag();
				if(res != null){
    				dismissReservId = res.getRid();
    				swipeRight = dismissRight;
				}
				
				if(dismissRight) {
					unlockMenu();
				}
			}

			@Override
			public void onSwipeRight() {
				findViewById(R.id.reservations_list).setVisibility(View.GONE);
			}

			@Override
			public void onSwipeLeft() {
				View reservationListView = findViewById(R.id.reservations_list);
				reservationListView.setVisibility(View.VISIBLE);
				tripInfoPanel.bringToFront();
			}
		}));
        
    	ImageView multipleTripMenu = (ImageView) tripInfoPanel.findViewById(R.id.multiple_trip_menu);
    	multipleTripMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				View reservationListView = findViewById(R.id.reservations_list);
				reservationListView.setVisibility(View.VISIBLE);
				reservationListView.bringToFront();
			}
		});
    	
    	ImageView startTrip = (ImageView) tripInfoPanel.findViewById(R.id.start_trip);
    	startTrip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						final Reservation reserv = (Reservation) findViewById(R.id.trip_info).getTag();
						if(reserv.isEligibleTrip()) {
							startValidationActivity(reserv);
						}
						else {
							NotificationDialog2 dialog = new NotificationDialog2(LandingActivity2.this, "Would you like to start your trip early?");
							dialog.setVerticalOrientation(false);
							dialog.setTitle("");
							dialog.setNegativeButtonText("Yes");
							dialog.setNegativeActionListener(new ActionListener() {
								@Override
								public void onClick() {
								    GeoPoint origin = null;
								    if(myPointOverlay != null){
								        origin = myPointOverlay.getLocation();
								    }
									RescheduleTripTask rescheduleTask = new RescheduleTripTask(LandingActivity2.this, 
									        origin, null, reserv.getDestinationAddress(), 
							        		reserv.getRid(), ehs);
									rescheduleTask.callback = new RescheduleTripTask.Callback() {
			                            @Override
			                            public void run(Reservation reservation) {
			                            	startValidationActivity(reservation);
			                            }
			                        };
			                        Misc.parallelExecute(rescheduleTask);
								}
							});
							dialog.setPositiveButtonText("No");
							dialog.setPositiveActionListener(new ActionListener() {
								@Override
								public void onClick() {
									//do nothing
								}
							});
							dialog.show();
						}
					}
				});
			}
    	});
    	
    	ImageView reschTrip = (ImageView) tripInfoPanel.findViewById(R.id.reschedule_trip);
    	reschTrip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						try {
							Reservation reserv = (Reservation) findViewById(R.id.trip_info).getTag();
			                Intent intent = new Intent(LandingActivity2.this, RouteActivity.class);
			                Bundle extras = new Bundle();
			                extras.putLong(RouteActivity.RESCHEDULE_RESERVATION_ID, reserv.getRid());
			                extras.putString("originAddr", reserv.getOriginAddress());
			                extras.putParcelable(RouteActivity.ORIGIN_COORD, reserv.getStartGpFromNavLink());
			                extras.putString(RouteActivity.ORIGIN_COORD_PROVIDER, null);
			                extras.putLong(RouteActivity.ORIGIN_COORD_TIME, 0);
			                extras.putString("destAddr", reserv.getDestinationAddress());
			                extras.putParcelable(RouteActivity.DEST_COORD, reserv.getEndGpFromNavLink());
			                extras.putLong(RouteActivity.RESCHEDULE_DEPARTURE_TIME, reserv.getDepartureTimeUtc());
			                intent.putExtras(extras);
			                hideBulbBalloon();
			                hideStarredBalloon();
			                removeAllOD();
			                startActivity(intent);
						}
						catch(Exception e) {
							ehs.reportException(e);
						}
					}
				});
			}
    	});
    	
    	ImageView onMyWayTrip = (ImageView) findViewById(R.id.on_my_way_trip);
    	onMyWayTrip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Reservation reserv = (Reservation) findViewById(R.id.trip_info).getTag();
					    Intent contactSelect = new Intent(LandingActivity2.this, ContactsSelectActivity.class);
					    JSONObject reservRecipients = DebugOptionsActivity.getReservRecipients(LandingActivity2.this, reserv.getRid());
					    if(reservRecipients != null) {
						    contactSelect.putExtra(ContactsSelectActivity.SELECTED_EMAILS, reservRecipients.optString(ValidationActivity.EMAILS, ""));
						    contactSelect.putExtra(ContactsSelectActivity.SELECTED_PHONES, reservRecipients.optString(ValidationActivity.PHONES, ""));
					    }
						startActivityForResult(contactSelect, ON_MY_WAY);
					}
				});
			}
    	});
    	
    	findViewById(R.id.header_panel).setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {
    		}
    	});
    	
    	ImageView reservListBack = (ImageView) findViewById(R.id.reservation_list_back);
    	reservListBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tripInfoPanel.bringToFront();
				showTripInfoPanel(true, false);
				findViewById(R.id.reservations_list).setVisibility(View.GONE);
			}
		});
        reservationListView = (ListView) findViewById(R.id.reservation_list_view);
        reservationAdapter = new ArrayAdapter<Reservation>(LandingActivity2.this, R.layout.reservation_list_item2, R.id.od_info) {
        	@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		View view = super.getView(position, convertView, parent);
                Reservation reserv = getItem(position);
                View reservItemPanel = view.findViewById(R.id.reservation_item);
                View rightArrow = view.findViewById(R.id.right_arrow);
                boolean isAbout2Go = false;
                if(reserv.isEligibleTrip()) {
                	isAbout2Go = true;
                	int color = LandingActivity2.this.getResources().getColor(R.color.metropia_green);
                	reservItemPanel.setBackgroundColor(color);
                	rightArrow.setBackgroundColor(color);
                }
                else {
                	int color = LandingActivity2.this.getResources().getColor(R.color.metropia_orange);
                	reservItemPanel.setBackgroundColor(color);
                	rightArrow.setBackgroundColor(color);
                }
                TextView odInfoView = (TextView) view.findViewById(R.id.od_info);
                StringBuffer odInfo = new StringBuffer();
                odInfo.append(StringUtils.isNotBlank(reserv.getOriginName())?reserv.getOriginName():reserv.getOriginAddress());
                odInfo.append(" to ");
                odInfo.append(StringUtils.isNotBlank(reserv.getDestinationName())?reserv.getDestinationName():reserv.getDestinationAddress());
                odInfoView.setText(odInfo.toString());
                
                TextView leaveInfoView = (TextView) view.findViewById(R.id.leave_info);
                StringBuffer leaveInfo = new StringBuffer("Leave at: ");
                leaveInfo.append(isAbout2Go?"NOW":TimeColumn.formatTime(reserv.getDepartureTimeUtc(), reserv.getRoute().getTimezoneOffset()));
                leaveInfoView.setText(formatTripArrivalTime(leaveInfo.toString()));
                
                TextView arriveInfoView = (TextView) view.findViewById(R.id.arrive_info);
                StringBuffer arriveInfo = new StringBuffer("Arrive at: ");
                long arrivalTime = isAbout2Go?(System.currentTimeMillis() + reserv.getDuration()*1000):reserv.getArrivalTimeUtc();
                arriveInfo.append(TimeColumn.formatTime(arrivalTime, reserv.getRoute().getTimezoneOffset()));
                arriveInfoView.setText(formatTripArrivalTime(arriveInfo.toString()));

                Font.setTypeface(lightFont, odInfoView, leaveInfoView, arriveInfoView);
                reservItemPanel.requestLayout();
                odInfoView.requestLayout();
                leaveInfoView.requestLayout();
                arriveInfoView.requestLayout();
                return view;
        	}
        };
        reservationListView.setAdapter(reservationAdapter);
        reservationListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final Reservation reserv = (Reservation) parent.getItemAtPosition(position);
				if(reserv.isEligibleTrip()) {
					startValidationActivity(reserv);
				}
				else {
					try {
						Intent intent = new Intent(LandingActivity2.this, RouteActivity.class);
		                Bundle extras = new Bundle();
		                extras.putLong(RouteActivity.RESCHEDULE_RESERVATION_ID, reserv.getRid());
		                extras.putString("originAddr", reserv.getOriginAddress());
		                extras.putParcelable(RouteActivity.ORIGIN_COORD, reserv.getStartGpFromNavLink());
		                extras.putString(RouteActivity.ORIGIN_COORD_PROVIDER, null);
	                    extras.putLong(RouteActivity.ORIGIN_COORD_TIME, 0);
		                extras.putString("destAddr", reserv.getDestinationAddress());
		                extras.putParcelable(RouteActivity.DEST_COORD, reserv.getEndGpFromNavLink());
		                intent.putExtras(extras);
		                hideBulbBalloon();
		                hideStarredBalloon();
		                removeAllOD();
		                startActivity(intent);
					}
					catch(Exception e) {
						ehs.reportException(e);
					}
	            }
			}
		});
        
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                		reservationListView, findViewById(R.id.reservations_list), 
                        new SwipeDismissListViewTouchListener.OnDismissCallback() {
                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                	final Reservation reserv = reservationAdapter.getItem(position);
                                	reservationAdapter.remove(reserv);
                                	AsyncTask<Void, Void, Boolean> delTask = new AsyncTask<Void, Void, Boolean>(){
            		                    @Override
            		                    protected Boolean doInBackground(Void... params) {
            		                        ReservationDeleteRequest request = new ReservationDeleteRequest(
            		                            User.getCurrentUser(LandingActivity2.this), reserv.getRid());
            		                        Boolean success = Boolean.TRUE;
            		                        try {
            		                            request.execute(LandingActivity2.this);
            		                        }
            		                        catch (Exception e) {
            		                        	success = Boolean.FALSE;
            		                        }
            		                        return success;
            		                    }
            		                    
            		                    @Override
            		                    protected void onPostExecute(Boolean success) {
            		                    	if(success) {
            		                    		DebugOptionsActivity.removeReservRecipients(LandingActivity2.this, reserv.getRid());
            		                    	}
            		                    }
            		                };
            		                Misc.parallelExecute(delTask);
                                }
                                reservationAdapter.notifyDataSetChanged();
                                if(reservationAdapter.isEmpty()) {
                                	showNoReservedTrips();
                                }
                                refreshTripsInfo();
                            }

							@Override
							public void onDismissRight() {
								findViewById(R.id.reservations_list).bringToFront();
								showTripInfoPanel(true, false);
							}

							@Override
							public void onDismissParent() {
								hideReservationListPanel();
								tripInfoPanel.bringToFront();
							}
                         });
        reservationListView.setOnTouchListener(touchListener);
        Font.setTypeface(boldFont, (TextView) findViewById(R.id.no_reserved_trips));
    }
    
    private void unSelectAllIcon() {
    	starView.setImageResource(R.drawable.star_dim);
    	homeView.setImageResource(R.drawable.home_dim);
    	workView.setImageResource(R.drawable.work_dim);
    }
    
    private boolean isFavoriteMark(int markResourceId) {
    	Integer[] favIconIds = new Integer[] {R.drawable.star, R.drawable.home, R.drawable.work};
    	for(Integer iconId : favIconIds) {
    		if(iconId == markResourceId) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private void reInitFavoriteOperationPanel() {
    	View favOptPanel = findViewById(R.id.fav_opt);
    	favOptPanel.setTag(null);
    	((EditText)favOptPanel.findViewById(R.id.favorite_search_box)).setText("");
    	((EditText)favOptPanel.findViewById(R.id.favorite_search_box)).setEnabled(true);
    	((EditText)favOptPanel.findViewById(R.id.label_input)).setText("");
    	favOptPanel.findViewById(R.id.label_clear).setVisibility(View.GONE);
    	favOptPanel.findViewById(R.id.fav_del_panel).setVisibility(View.GONE);
    	favOptPanel.findViewById(R.id.fav_search_box_clear).setVisibility(View.GONE);
    	favOptPanel.findViewById(R.id.icon).setTag(null);
    	((ImageView)favOptPanel.findViewById(R.id.star)).setImageResource(R.drawable.star);
    	((ImageView)favOptPanel.findViewById(R.id.home)).setImageResource(R.drawable.home);
    	((ImageView)favOptPanel.findViewById(R.id.work)).setImageResource(R.drawable.work);
    	clearFavSearchResult();
    }
    
    private void clearFavSearchResult() {
    	favSearchAddresses.clear();
		favAutoCompleteAdapter.clear();
		refreshFavAutoCompleteData();
		favSearchBox.clearFocus();
    }
    
    private void clearSearchResult() {
    	searchAddresses.clear();
		autoCompleteAdapter.clear();
		refreshSearchAutoCompleteData();
    }
    
    private void clearFromSearchResult() {
        fromSearchAddresses.clear();
        fromAutoCompleteAdapter.clear();
        refreshFromSearchAutoCompleteData();
    }
    
    private ArrayAdapter<Address> createAutoCompleteAdapter(final EditText searchBox) {
    	return new ArrayAdapter<Address>(LandingActivity2.this, R.layout.dropdown_select, R.id.name) {
        	@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		View view = super.getView(position, convertView, parent);
                Address item = getItem(position);
                View namePanel = view.findViewById(R.id.name_panel);
                TextView name = (TextView) view.findViewById(R.id.name);
                name.setText(item.getName());
                TextView address = (TextView) view.findViewById(R.id.address);
                address.setText(item.getAddress());
                TextView distance = (TextView) view.findViewById(R.id.distance);
                if(StringUtils.isBlank(item.getDistance())) {
                	distance.setVisibility(View.GONE);
                }
                else {
                	distance.setVisibility(View.VISIBLE);
                	distance.setText("> " + item.getDistance() + "mi");
                }
                
                initFontsIfNecessary();
                
                if(TAP_TO_ADD_FAVORITE.equals(item.getName())) {
                	name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.star, 0, 0, 0);
                	name.setCompoundDrawablePadding(Dimension.dpToPx(20, getResources().getDisplayMetrics()));
                	int paddingSize = Dimension.dpToPx(5, getResources().getDisplayMetrics());
                	namePanel.setPadding(Dimension.dpToPx(20, getResources().getDisplayMetrics()), paddingSize, 0, paddingSize);
                	Font.setTypeface(lightFont, name);
                	address.setVisibility(View.GONE);
                }
                else {
                	name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                	name.setCompoundDrawablePadding(0);
                	int paddingSize = Dimension.dpToPx(5, getResources().getDisplayMetrics());
                	namePanel.setPadding(paddingSize, paddingSize, 0, 0);
                	Font.setTypeface(boldFont, name);
                	address.setVisibility(View.VISIBLE);
                }
                Font.setTypeface(boldFont, distance);
                Font.setTypeface(lightFont, address);
                namePanel.requestLayout();
                name.requestLayout();
                distance.requestLayout();
                address.requestLayout();
                view.setPadding(0, 0, 0, position == getCount() - 1 ? 
                    Dimension.dpToPx(135, getResources().getDisplayMetrics()) : 0);
                return view;
        	}
        	
        	@Override
        	public Filter getFilter() {
        		Filter filter = new Filter() {
					@Override
					protected FilterResults performFiltering(CharSequence constraint) {
						List<Address> all = new ArrayList<Address>();
						List<Address> result = new ArrayList<Address>();
						for(int i = 0 ; i < getCount() ; i++) {
							all.add(getItem(i));
						}
						if(constraint != null) {
				            result.clear();
				            for (Address addr : all) {
				                if(addr.getName().toLowerCase().startsWith(constraint.toString().toLowerCase()) 
				               		|| addr.getAddress().toLowerCase().startsWith(constraint.toString().toLowerCase())){
				                    result.add(addr);
				                }
				            }
				            FilterResults filterResults = new FilterResults();
				            filterResults.values = result;
				            filterResults.count = result.size();
				            return filterResults;
				        } else {
				            return new FilterResults();
				        }
					}

					@Override
					protected void publishResults(CharSequence constraint,	FilterResults results) {
						ArrayList<Address> filteredList = (ArrayList<Address>) results.values;
			            if(results != null && results.count > 0) {
			                clear();
			                for (Address c : filteredList) {
			                    add(c);
			                }
			                notifyDataSetChanged();
			            }
					}
					
					@Override
					public CharSequence convertResultToString(Object selected) {
						String selectedAddr = ((Address)selected).getAddress();
						String selectedName = ((Address)selected).getName();
						if(NO_AUTOCOMPLETE_RESULT.equals(selectedName) && StringUtils.isBlank(selectedAddr)) {
							return searchBox.getText();
						}
						return selectedAddr;
					}
        			
        		};
        		return filter;
        	}
        };
    }
    
    private void searchFavAddress(String addrStr, boolean zoomIn) {
    	searchPOIAddress(addrStr, zoomIn, lastLocation);
    }
    
    private void searchAddress(String addrStr, boolean zoomIn) {
    	searchPOIAddress(addrStr, zoomIn, lastLocation);
    }
    
    private void searchPOIAddress(final String addrStr, final boolean zoomIn, final Location _location){
        AsyncTask<Void, Void, Address> task = new AsyncTask<Void, Void, Address>(){
            @Override
            protected Address doInBackground(Void... params) {
                Address addr = null;
                try {
                    List<Address> addrs;
                    if(_location == null) {
                        addrs = Geocoding.lookup(LandingActivity2.this, addrStr);
                    }
                    else {
                        addrs = Geocoding.lookup(LandingActivity2.this, addrStr, _location.getLatitude(), _location.getLongitude());
                    }
                    for (Address a : addrs) {
                        addr = a;
                        break;
                    }
                }
                catch (Exception e) {
                }
                return addr;
            }
            @Override
            protected void onPostExecute(Address addr) {
                if(addr != null){
                    dropPinForAddress(addr, zoomIn);
                }
                else {
                	NotificationDialog2 dialog = new NotificationDialog2(LandingActivity2.this, "No results");
                	dialog.setTitle("");
                	dialog.setPositiveButtonText("OK");
                	dialog.show();
                }
            }
        };
        Misc.parallelExecute(task);
    }
    
    private void dropPinForAddress(Address addr, boolean zoomIn) {
    	GeoPoint gp = addr.getGeoPoint();
        DebugOptionsActivity.addRecentAddress(LandingActivity2.this, addr.getAddress());
        final MapView mapView = (MapView) findViewById(R.id.mapview);
        POIOverlay poiOverlay = getPOIOverlayByAddress(mapView, addr.getAddress());
        if(poiOverlay != null) {
        	hideStarredBalloon();
        	hideBulbBalloon();
        	removePOIMarker(mapView);
        	handleOD(mapView, poiOverlay, isFromPoi());
        	poiOverlay.markODPoi();
        	poiOverlay.setIsFromPoi(isFromPoi());
        	poiOverlay.showBalloonOverlay();
            mapView.postInvalidate();
        }
        else {
        	refreshPOIMarker(mapView, gp.getLatitude(), gp.getLongitude(), addr.getAddress(), addr.getName());
        }
        IMapController mc = mapView.getController();
        if(zoomIn){
            mc.setZoom(SEARCH_ZOOM_LEVEL);
            mc.setCenter(gp);
        }else{
            mc.animateTo(gp);
        }
    }
    
    private POIOverlay getPOIOverlayByAddress(MapView mapView, String addr) {
    	List<Overlay> overlays = mapView.getOverlays();
    	for(Overlay overlay : overlays) {
    		if(overlay instanceof POIOverlay) {
    			POIOverlay poi = (POIOverlay)overlay;
    			if(StringUtils.equalsIgnoreCase(addr, poi.getAddress())) {
    				return poi;
    			}
    		}
    	}
    	return null;
    }
    
    private void refreshAutoCompleteData(ListView searchList, ArrayAdapter<Address> adapter, List<Address> searchedAddresses, EditText _searchBox) {
    	adapter.clear();
    	if(showAutoComplete.get() && _searchBox.isFocused()) {
	        for(Address a : searchedAddresses) {
	        	adapter.add(a);
	        }
	        if(!adapter.isEmpty()) {
	        	searchList.setVisibility(View.VISIBLE);
	        }else{
	        	searchList.setVisibility(View.GONE);
	        }
    	}
    	else {
    		searchList.setVisibility(View.GONE);
    	}
    }
    
    private void refreshSearchAutoCompleteData(){
    	refreshAutoCompleteData(searchResultList, autoCompleteAdapter, searchAddresses, searchBox);
    }
    
    private void refreshFromSearchAutoCompleteData(){
        refreshAutoCompleteData(fromSearchResultList, fromAutoCompleteAdapter, fromSearchAddresses, fromSearchBox);
    }
    
    private void refreshFavAutoCompleteData() {
    	refreshAutoCompleteData(favSearchResultList, favAutoCompleteAdapter, favSearchAddresses, favSearchBox);
    }
    
    public static class BalloonModel {
        
        public int id;
        
        public double lat;
        
        public double lon;
        
        public String address;
        
        public GeoPoint geopoint;
        
        public String label;
        
    }
    
    public static class PoiOverlayInfo extends BalloonModel implements Parcelable {
    	
    	public int marker = R.drawable.transparent_poi;
    	
    	public int markerWithShadow = R.drawable.transparent_poi;
    	
    	public static final Parcelable.Creator<PoiOverlayInfo> CREATOR = new Parcelable.Creator<PoiOverlayInfo>() {
            public PoiOverlayInfo createFromParcel(Parcel in) {
                return new PoiOverlayInfo(in);
            }

            public PoiOverlayInfo[] newArray(int size) {
                return new PoiOverlayInfo[size];
            }
        };
        
        public PoiOverlayInfo() {}
    	
    	public PoiOverlayInfo(Parcel in) {
    		id = in.readInt();
            lat = in.readDouble();
            lon = in.readDouble();
            address = in.readString();
            label = in.readString();
            marker = in.readInt();
            markerWithShadow = in.readInt();
            geopoint = new GeoPoint(lat, lon);
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(id);
			dest.writeDouble(lat);
			dest.writeDouble(lon);
			dest.writeString(address);
			dest.writeString(label);
			dest.writeInt(marker);
			dest.writeInt(markerWithShadow);
		}
    	
    	public static PoiOverlayInfo fromAddress(com.smartrek.models.Address address) {
    		PoiOverlayInfo poiInfo = new PoiOverlayInfo();
    		poiInfo.id = address.getId();
    		poiInfo.label = address.getName();
    		poiInfo.address = address.getAddress();
    		poiInfo.lat = address.getLatitude();
    		poiInfo.lon = address.getLongitude();
    		poiInfo.geopoint = new GeoPoint(address.getLatitude(), address.getLongitude());
    		IconType icon = IconType.fromName(address.getIconName());
    		poiInfo.marker = icon.getResourceId();
    		poiInfo.markerWithShadow = icon.getResourceWithShadowId();
    		return poiInfo;
    	}
    	
    	public static PoiOverlayInfo fromLocation(com.smartrek.requests.WhereToGoRequest.Location location) {
    		PoiOverlayInfo poiInfo = new PoiOverlayInfo();
    		poiInfo.label = "";
    		poiInfo.address = location.addr;
    		poiInfo.lat = location.lat;
    		poiInfo.lon = location.lon;
    		poiInfo.geopoint = new GeoPoint(location.lat, location.lon);
    		poiInfo.marker = R.drawable.bulb_poi;
    		poiInfo.markerWithShadow = R.drawable.bulb_poi_with_shadow;
    		return poiInfo;
    	}
    	
    	public static PoiOverlayInfo fromBalloonModel(BalloonModel model) {
    		PoiOverlayInfo poiInfo = new PoiOverlayInfo();
    		poiInfo.id = model.id;
    		poiInfo.label = model.label;
    		poiInfo.address = model.address;
    		poiInfo.lat = model.lat;
    		poiInfo.lon = model.lon;
    		poiInfo.geopoint = model.geopoint;
    		return poiInfo;
    	}
    	
    	public static PoiOverlayInfo fromCurrentLocation(CurrentLocationOverlay currentLoc) {
    		PoiOverlayInfo poiInfo = new PoiOverlayInfo();
    		poiInfo.lat = currentLoc.getLocation().getLatitude();
    		poiInfo.lon = currentLoc.getLocation().getLongitude();
    		poiInfo.geopoint = currentLoc.getLocation();
    		return poiInfo;
    	}

    }
    
    public enum IconType {
    	star, home, work;
    	
    	public static IconType fromName(String name) {
    		for(IconType type : values()) {
    			if(type.name().equals(name)) {
    				return type;
    			}
    		}
    		return star;
    	}
    	
    	private static Integer[] getIconInfos(IconType type) {
    		switch(type) {
    		  case star:
    			  return new Integer[] {R.id.star, R.drawable.star, R.drawable.star_with_shadow};
    		  case home:
    			  return new Integer[] {R.id.home, R.drawable.home, R.drawable.home_with_shadow};
    		  case work:
    			  return new Integer[] {R.id.work, R.drawable.work, R.drawable.work_with_shadow};
    		  default:
    			  return null;
    		}
    	}
    	
    	public static Integer[] getIconInfosFromName(String name) {
    		IconType icon = fromName(name);
    		return getIconInfos(icon);
    	}
    	
    	public Integer getIconId() {
    		return getIconInfos(this)[0];
    	}
    	
    	public Integer getResourceId() {
    		return getIconInfos(this)[1];
    	}
    	
    	public Integer getResourceWithShadowId() {
    		return getIconInfos(this)[2];
    	}
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
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 60000, 
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
    
    public static final String TRIP_INFO_CACHED_UPDATES = "TRIP_INFO_CACHED_UPDATES";
    
    private BroadcastReceiver tripInfoCachedUpdater = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LandingActivity.initializeIfNeccessary(context, new Runnable() {
                @Override
                public void run() {
                    drawedReservId = Long.valueOf(-1);
                    dismissReservId = Long.valueOf(-1);
                    refreshTripsInfo(true);
                }
            });
        }
    };
    
    public static final String ON_THE_WAY_NOTICE = "ON_THE_WAY_NOTICE"; 
    
    private BroadcastReceiver onTheWayNotifier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            String msg = intent.getStringExtra(MSG);
//            ((TextView)findViewById(R.id.on_the_way_msg)).setText(msg);
//            View otwButton = findViewById(R.id.on_the_way_button);
//            otwButton.setTag(R.id.on_the_way_msg, msg);
//            otwButton.setTag(R.id.on_the_way_lat, intent.getDoubleExtra(LAT, 0));
//            otwButton.setTag(R.id.on_the_way_lon, intent.getDoubleExtra(LON, 0));
//            findViewById(R.id.on_the_way_icon).setVisibility(View.VISIBLE);
        }
    };
    
    public static final String UPDATE_MY_LOCATION = "UPDATE_MY_LOCATION";
    
    private BroadcastReceiver updateMyLocation = new BroadcastReceiver() {
    	@Override
        public void onReceive(Context context, Intent intent) {
    		Double lat = intent.getDoubleExtra("lat", 0);
    		Double lon = intent.getDoubleExtra("lon", 0);
    		if(lat != 0 && lon != 0) {
    			refreshMyLocation(lat, lon);
    		}
    	}
    };
    
    public static final String UPDATE_MENU_MY_TRIPS = "UPDATE_MENU_MY_TRIPS";
    
    private BroadcastReceiver updateMenuMyTrips = new BroadcastReceiver() {
    	@Override
        public void onReceive(Context context, Intent intent) {
    		boolean hasTrips = intent.getBooleanExtra("hasTrips", false);
    		if(hasTrips) {
    			findViewById(R.id.my_trips).setVisibility(View.VISIBLE);
    		}
    		else {
    			findViewById(R.id.my_trips).setVisibility(View.GONE);
    		}
    	}
    };
    
    private void refreshMyLocation(double lat, double lon){
        MapView mapView = (MapView)findViewById(R.id.mapview);
        List<Overlay> mapOverlays = mapView.getOverlays();
        if(myPointOverlay == null){
            myPointOverlay = new CurrentLocationOverlay(LandingActivity2.this, 0, 0, R.drawable.landing_page_current_location);
            mapOverlays.add(myPointOverlay);
        }
        else {
        	Collections.swap(mapOverlays, mapOverlays.indexOf(myPointOverlay), (mapOverlays.size()-1));
        }
        myPointOverlay.setLocation((float) lat, (float) lon);
        mapView.postInvalidate();
    }
    
    private void prepareGPS(){
        closeGPS();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                10000, 5, locationListener);
        }else{
            SystemService.alertNoGPS(this, true);
        }
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }
    
    private void closeGPS(){
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
    
    private void showODBalloon() {
    	MapView mapView = (MapView) findViewById(R.id.mapview);
    	List<Overlay> allOverlays = mapView.getOverlays();
    	for(Overlay overlay : allOverlays) {
    		if(overlay instanceof POIOverlay) {
    			POIOverlay poi = (POIOverlay)overlay;
    			if(curFrom != null && ((curFrom.getAid() != 0 && curFrom.getAid() == poi.getAid()) 
    					|| (StringUtils.isNotBlank(curFrom.getAddress()) && curFrom.getAddress().equals(poi.getAddress())))) {
    				curFrom = poi;
    				curFrom.setIsFromPoi(true);
    				curFrom.markODPoi();
	    			if(isFromPoi()) {
	    				curFrom.showBalloonOverlay();
	    			}
	    			else {
	    				curFrom.showMiniBalloonOverlay();
	    			}
    			}
    			else if(curTo!=null && ((curTo.getAid() != 0 && curTo.getAid() == poi.getAid()) 
    					|| (StringUtils.isNotBlank(curTo.getAddress()) && curTo.getAddress().equals(poi.getAddress())))) {
    				curTo = poi;
    				curTo.setIsFromPoi(false);
    				curTo.markODPoi();
	    			if(isFromPoi()) {
	    				curTo.showMiniBalloonOverlay();
	    			}
	    			else {
	    				curTo.showBalloonOverlay();
	    			}
    			}
    		}
    	}
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(tripInfoUpdater, new IntentFilter(TRIP_INFO_UPDATES));
        registerReceiver(onTheWayNotifier, new IntentFilter(ON_THE_WAY_NOTICE));
        mapRefresh.set(true);
        prepareGPS();
        drawedReservId = Long.valueOf(-1);
        dismissReservId = Long.valueOf(-1);
        refreshTripsInfo();
        updateMyMetropiaInfo();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
    
    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
    
    @Override
    protected void onPause() {
      unregisterReceiver(tripInfoUpdater);
      unregisterReceiver(onTheWayNotifier);
      super.onPause();
      mSensorManager.unregisterListener(this, accelerometer);
      mSensorManager.unregisterListener(this, magnetometer);
      closeGPS();
      drawedReservId = Long.valueOf(-1);
      dismissReservId = Long.valueOf(-1);
      refreshTripsInfo(true);
    } 
    
    private void refreshTripsInfo(){
        refreshTripsInfo(false);
    }
    
    private void refreshTripsInfo(final boolean cached){
        ReservationListTask task = new ReservationListTask(this, cached){
	        @Override
	        protected void onPostExecute(List<Reservation> reservations) {
	            if (reservations == null || reservations.isEmpty()) {
	                MapView mapView = (MapView) findViewById(R.id.mapview);
	                List<Overlay> mapOverlays = mapView.getOverlays();
	                List<Overlay> need2Remove = getDrawedRouteOverlays(mapOverlays);
	                if(!need2Remove.isEmpty()) {
	                    mapOverlays.removeAll(need2Remove);
	                    mapView.postInvalidate();
	                }
	                hideTripInfoPanel();
	                findViewById(R.id.trip_info).setTag(null);
	                tripNotifyIcon.setVisibility(View.GONE);
	                refreshReservationList(new ArrayList<Reservation>());
	                unlockMenu();
	            } 
	            else{
                    Reservation reserv = reservations.get(0);
	                drawRoute(reserv);
                    refreshTripInfoPanel(reservations);
                    refreshReservationList(reservations);
	            }
	        }
	    };
	    Misc.parallelExecute(task);
    }
    
    public static class ReservationListTask extends AsyncTask<Void, Void, List<Reservation>>{
        
        boolean cached;
        
        Context ctx;
        
        public ReservationListTask(Context ctx){
            this(ctx, false);
        }
        
        public ReservationListTask(Context ctx, boolean cached){
            this.ctx = ctx;
            this.cached = cached;
        }
        
        @Override
        protected List<Reservation> doInBackground(Void... params) {
            User user = User.getCurrentUser(ctx);
            List<Reservation> reservations= Collections.emptyList();
            ReservationListFetchRequest resReq = new ReservationListFetchRequest(user);
            FavoriteAddressFetchRequest addReq = new FavoriteAddressFetchRequest(user);
            if(!cached){
                resReq.invalidateCache(ctx);
                addReq.invalidateCache(ctx);
            }
            try {
                List<com.smartrek.models.Address> addresses = addReq.execute(ctx);
                reservations = resReq.execute(ctx);
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
                removeTerminatedReservs(ctx, reservations);
                Collections.sort(reservations, Reservation.orderByDepartureTime());
            }
            catch (NullPointerException e){}
            catch (Exception e) {
            }
            return reservations;
        }
    }
    
    private static void removeTerminatedReservs(Context ctx, List<Reservation> reservations) {
        List<Long> idsToRemove = DebugOptionsActivity.getTerminatedReservIds(ctx);
        Iterator<Reservation> iter = reservations.iterator();
        while(iter.hasNext()){
            Reservation r = iter.next();
            if(idsToRemove.contains(r.getRid())){
                iter.remove();
            }
        }
    }
    
    private void refreshTripInfoPanel(List<Reservation> reservations) {
    	Reservation reserv = null;
    	int curReservIdx = -1;
    	while(curReservIdx < (reservations.size()-1) && reserv == null) {
    		curReservIdx++;
    		Reservation tempReserv = reservations.get(curReservIdx);
    		long departureTimeUtc = tempReserv.getDepartureTimeUtc();
    		long timeUntilDepart = departureTimeUtc - System.currentTimeMillis();
    		if(timeUntilDepart > -1 * Reservation.GRACE_INTERVAL) {
    			reserv = tempReserv;
    		}
    	}
    	
    	initFontsIfNecessary();
    	
    	View tripInfoPanel = findViewById(R.id.trip_info);
    	if(reserv != null) {
	    	String nextTripInfoDesc = "";
	    	String nextTripStartTime = "";
	    	String arrivalTime = formatTime(reserv.getArrivalTimeUtc(), reserv.getRoute().getTimezoneOffset());
	    	int backgroundColor = R.color.metropia_orange;
	    	long departureTimeUtc = reserv.getDepartureTimeUtc();
	        long timeUntilDepart = departureTimeUtc - System.currentTimeMillis();
	        long durationTime = reserv.getDuration();  //sec
	        if(reserv.isEligibleTrip()){
	        	nextTripInfoDesc = "It's Time to Go!";
	            backgroundColor = R.color.metropia_green;
	            nextTripStartTime = getFormattedDuration((int)durationTime);
	            arrivalTime = formatTime(System.currentTimeMillis() + durationTime*1000, reserv.getRoute().getTimezoneOffset());
	        }else if(timeUntilDepart > 60 * 60 * 1000L){
	        	nextTripInfoDesc = "Your Next Trip will be at:";
	            nextTripStartTime = formatTime(departureTimeUtc, reserv.getRoute().getTimezoneOffset());
	            backgroundColor = R.color.metropia_orange;
	        }else if(timeUntilDepart > Reservation.GRACE_INTERVAL){
	        	nextTripInfoDesc = "Your Next Trip is in:";
	            nextTripStartTime = getFormattedDuration((int)timeUntilDepart / 1000);
	            backgroundColor = R.color.metropia_orange;
	        }
	        else {
	        	Log.d("LandingActivity2", "startTime : " + new Date(reserv.getDepartureTimeUtc()) + " arriveTime : " + new Date(reserv.getArrivalTimeUtc()));
	        }
	        tripInfoPanel.setBackgroundColor(getResources().getColor(backgroundColor));
	        tripInfoPanel.setTag(reserv);
	        TextView tripStartDescView = (TextView)tripInfoPanel.findViewById(R.id.trip_start_desc);
	        tripStartDescView.setText(nextTripInfoDesc);
	        TextView tripDurationDesc = (TextView)tripInfoPanel.findViewById(R.id.trip_duration_desc);
	        tripDurationDesc.setVisibility(reserv.isEligibleTrip()?View.VISIBLE:View.INVISIBLE);
	        TextView tripStartTimeView = (TextView)tripInfoPanel.findViewById(R.id.trip_start_time);
	        tripStartTimeView.setText(formatTripStartTime(nextTripStartTime));
	        TextView tripArriveTimeView = (TextView)tripInfoPanel.findViewById(R.id.trip_arrival_time);
	        tripArriveTimeView.setText(formatTripArrivalTime(arrivalTime));
	        Font.setTypeface(boldFont, tripStartTimeView, tripArriveTimeView);
	        Font.setTypeface(lightFont, tripStartDescView, tripDurationDesc, 
	        		(TextView) tripInfoPanel.findViewById(R.id.trip_arrival_desc));
	        showTripInfoPanel(false, false);
	        tripNotifyIcon.setImageResource(reserv.isEligibleTrip()?R.drawable.upcoming_trip_green:R.drawable.upcoming_trip_orange);
	        tripNotifyIcon.setVisibility(View.VISIBLE);
    	}
    	else {
    		Log.d("LandingActivity2", "hideTripInfoPanel");
    		hideReservationInfoPanel();
    		tripNotifyIcon.setVisibility(View.GONE);
    		findViewById(R.id.trip_info).setTag(null);
    	}
    	refreshReservationList(reservations);
    }
    
    private String formatTime(long time, int timzoneOffset){
	    SimpleDateFormat dateFormat = new SimpleDateFormat("h:mma", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone(Request.getTimeZone(timzoneOffset)));
        return dateFormat.format(new Date(time));
	}
    
    private String getFormattedDuration(int duration){
	    return String.format("%dmin", duration/60);
	}
    
    private void refreshReservationList(List<Reservation> reservations) {
    	reservationAdapter.clear();
    	int curReservIdx = -1;
    	boolean cont = true;
    	while(curReservIdx < (reservations.size()-1) && cont) {
    		curReservIdx++;
    		Reservation tempReserv = reservations.get(curReservIdx);
    		long departureTimeUtc = tempReserv.getDepartureTimeUtc();
    		long timeUntilDepart = departureTimeUtc - System.currentTimeMillis();
    		if(timeUntilDepart > -1*Reservation.GRACE_INTERVAL) {
    			cont = false;
    		}
    	}
    	
    	if(!cont) {
    		for(int i = curReservIdx ; i < reservations.size() ; i++) {
    			reservationAdapter.add(reservations.get(i));
    		}
    		reservationListView.setVisibility(View.VISIBLE);
    		findViewById(R.id.no_reserved_trips).setVisibility(View.GONE);
    	}
    	else {
    		showNoReservedTrips();
    	}
    }
    
    private void showNoReservedTrips() {
//    	reservationListView.setVisibility(View.INVISIBLE);
		findViewById(R.id.no_reserved_trips).setVisibility(View.VISIBLE);
    }
    
    private void showTripInfoPanel(boolean force, boolean animation) {
    	View reservationListPanel = findViewById(R.id.reservations_list);
    	View tripInfoPanel = findViewById(R.id.trip_info);
    	if((force && hasReservTrip()) || 
    			(reservationListPanel.getVisibility() != View.VISIBLE && hasReservTrip() && !dismissReservId.equals(((Reservation)tripInfoPanel.getTag()).getRid()))) {
    		tripInfoPanel.setVisibility(View.VISIBLE);
    		if(animation) {
    			float fromX = swipeRight?tripInfoPanel.getWidth():-1*tripInfoPanel.getWidth();
	    		ObjectAnimator slideAnimator = ObjectAnimator.ofFloat(tripInfoPanel, "translationX", fromX, 0f);
	    		slideAnimator.setDuration(500);
	    		slideAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
	    		ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(tripInfoPanel, "alpha", 0f, 1f);
	    		alphaAnimator.setDuration(500);
	    		alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
	    		AnimatorSet animatorSet = new AnimatorSet();
	    		animatorSet.play(slideAnimator).with(alphaAnimator);
	    		animatorSet.start();
    		}
    		lockMenu();
    		hideFavoriteOptPanel();
    	}
    }
    
    private void hideFavoriteOptPanel() {
    	reInitFavoriteOperationPanel();
		findViewById(R.id.fav_opt).setVisibility(View.GONE);
		findViewById(R.id.landing_panel).setVisibility(View.VISIBLE);
    }
    
    private boolean hasReservTrip() {
    	return findViewById(R.id.trip_info).getTag() != null;
    }
    
    private void hideTripInfoPanel() {
    	View tripInfoPanel = findViewById(R.id.trip_info);
    	tripInfoPanel.setVisibility(View.GONE);
    }
    
    private void hideReservationInfoPanel() {
    	hideTripInfoPanel();
    	hideReservationListPanel();
    	unlockMenu();
    }
    
    private void unlockMenu() {
    	DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
    
    private void lockMenu() {
    	DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
    
    private void hideReservationListPanel() {
    	findViewById(R.id.reservations_list).setVisibility(View.GONE);
    }
    
    private SpannableString formatTripStartTime(String startTime) {
    	int indexOfChange = startTime.endsWith("M")?(startTime.indexOf("A")!=-1?startTime.indexOf("A"):startTime.indexOf("P")):startTime.indexOf("m");
		SpannableString startTimeSpan = SpannableString.valueOf(startTime);
		if(indexOfChange != -1) {
			startTimeSpan.setSpan(new AbsoluteSizeSpan(getResources()
					.getDimensionPixelSize(R.dimen.smaller_font)), indexOfChange,
					startTime.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return startTimeSpan;
    }
    
    private SpannableString formatTripArrivalTime(String arrivalTime) {
    	int indexOfChange = arrivalTime.indexOf("AM")!=-1?arrivalTime.indexOf("AM"):arrivalTime.indexOf("PM");
    	SpannableString arrivalTimeSpan = SpannableString.valueOf(arrivalTime);
    	if(indexOfChange != -1) {
			arrivalTimeSpan.setSpan(new AbsoluteSizeSpan(getResources()
					.getDimensionPixelSize(R.dimen.smaller_font)), indexOfChange, arrivalTime.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    	}
		return arrivalTimeSpan;
    }
    
    private Long drawedReservId = Long.valueOf(-1);
    
    private void drawRoute(final Reservation reserv) {
    	if(!drawedReservId.equals(reserv.getRid()) && canDrawReservRoute.get()) {
    		drawedReservId = reserv.getRid();
			final AsyncTask<Void, Void, List<Route>> routeTask = new AsyncTask<Void, Void, List<Route>>() {
	            @Override
	            protected List<Route> doInBackground(Void... params) {
	                List<Route> routes = null;
	                try {
	                    RouteFetchRequest reservRequest = new RouteFetchRequest(
	                    		reserv.getNavLink(),
	                    		reserv.getDepartureTime(), 
	                    		reserv.getDuration(),
	                        0,
	                        0);
	                    List<Route> tempRoutes = reservRequest.execute(LandingActivity2.this);
	                    if(tempRoutes !=null && tempRoutes.size() > 0) {
	                    	Route route = tempRoutes.get(0);
		                    route.setCredits(reserv.getCredits());
		                    route.preprocessNodes();
	                    	RouteFetchRequest request = new RouteFetchRequest(User.getCurrentUser(LandingActivity2.this), 
	                    	        route.getFirstNode().getGeoPoint(), route.getLastNode().getGeoPoint(), 
	                    	        reserv.getDepartureTimeUtc(), 0, 0, reserv.getOriginAddress(), reserv.getDestinationAddress());
	                    	routes = request.execute(LandingActivity2.this);
	                    }
	                }
	                catch(Exception e) {
	                	Log.d("drawRoute", Log.getStackTraceString(e));
	                }                                
	                return routes;
	            }
	            protected void onPostExecute(java.util.List<Route> routes) {
	                if(routes != null && routes.size() > 0) {
	                    Route route = routes.get(0);
	                    route.setCredits(reserv.getCredits());
	                    route.preprocessNodes();
	                    updateMap(routes, reserv.getDestinationAddress());
	                } 
	            }
	        };
	        Misc.parallelExecute(routeTask);
    	}
	}
    
    private void updateMap(List<Route> possibleRoutes, String destinationAddr) {
    	if(!possibleRoutes.isEmpty()) {
    		final MapView mapView = (MapView) findViewById(R.id.mapview);
    		Route route = possibleRoutes.get(0);
    		
    		// remove previous drew route
    		List<Overlay> mapOverlays = mapView.getOverlays();
    		List<Overlay> need2Remove = getDrawedRouteOverlays(mapOverlays);
    		mapOverlays.removeAll(need2Remove);
    		int routeColor = route.getColor()!=null?Color.parseColor(route.getColor()):RoutePathOverlay.GREEN;
    		RoutePathOverlay path = new RoutePathOverlay(this, route, routeColor, R.drawable.pin_origin);
    		path.setDashEffect();
    		mapOverlays.add(0, path);
    		
    		POIOverlay poi = getPOIOverlayByAddress(mapView, destinationAddr);
    		if(poi==null) {
	    		RouteDestinationOverlay destOverlay = new RouteDestinationOverlay(mapView, route.getLastNode().getGeoPoint(), 
	    				lightFont, destinationAddr, R.drawable.pin_destination);
	    		mapOverlays.add(destOverlay);
    		}
    		
    		RouteRect routeRect = new RouteRect(route.getNodes());
    		GeoPoint center = routeRect.getMidPoint();
//    		int[] range = routeRect.getRange();
    		IMapController imc = mapView.getController();
//    		imc.zoomToSpan(range[0], range[1]);
    		imc.setCenter(center);
    		mapView.postInvalidate();
    	}
    }
    
    private List<Overlay> getDrawedRouteOverlays(List<Overlay> currentOverlays) {
    	List<Overlay> routeOverlays = new ArrayList<Overlay>();
    	for(Overlay overlay : currentOverlays) {
    		if(overlay instanceof RoutePathOverlay || overlay instanceof RouteDestinationOverlay) {
    			routeOverlays.add(overlay);
    		}
    	}
    	return routeOverlays;
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
        	searchIntentAddress(intentAddress);
        }
    }
    
    private void searchIntentAddress(final String address) {
    	LandingActivity.initializeIfNeccessary(this, new Runnable() {
			@Override
			public void run() {
				Location _location = lastLocation;
				if(_location == null) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LandingActivity2.this.getApplicationContext());
				    float lastLat = ((int) (prefs.getFloat(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_LAT, Integer.MIN_VALUE) * 1000000f)) / 1000000f;
			        float lastLng = ((int) (prefs.getFloat(LocationLibraryConstants.SP_KEY_LAST_LOCATION_UPDATE_LNG, Integer.MIN_VALUE) * 1000000f)) / 1000000f;
//					float lastLat = 32.1559094f; //Tuson
//			        float lastLng = -110.883805f;
			        _location = new Location("");
			        _location.setLatitude(lastLat);
			        _location.setLongitude(lastLng);
				}
				searchPOIAddress(address, true, _location);
			}
    	});
    }
    
    private String getIntentAddress(Intent intent){
        String address = null;
        Uri uri = intent.getData();
        if(uri != null){
            address = Uri.decode(StringUtils.substringAfterLast(uri.toString(), "q="));
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
    
    private void startRouteActivity(MapView mapView){
        if(!poiTapThrottle.get()){
            poiTapThrottle.set(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    poiTapThrottle.set(false);
                }
            }, 500);
            boolean hasFromAddr = curFrom == null?false:StringUtils.isNotBlank(curFrom.getAddress());
            Intent intent = new Intent(this, RouteActivity.class);
            Bundle extras = new Bundle();
            extras.putString(RouteActivity.ORIGIN_ADDR, hasFromAddr?curFrom.getAddress():EditAddress.CURRENT_LOCATION);
            if(curFrom != null){
                extras.putParcelable(RouteActivity.ORIGIN_COORD, curFrom.getGeoPoint());
                extras.putParcelable(RouteActivity.ORIGIN_OVERLAY_INFO, curFrom.getPoiOverlayInfo());
            }else if(myPointOverlay != null){
                extras.putParcelable(RouteActivity.ORIGIN_COORD, myPointOverlay.getLocation());
                extras.putParcelable(RouteActivity.ORIGIN_OVERLAY_INFO, PoiOverlayInfo.fromCurrentLocation(myPointOverlay));
            }
            extras.putString(RouteActivity.ORIGIN_COORD_PROVIDER, curFromProvider);
            extras.putLong(RouteActivity.ORIGIN_COORD_TIME, curFromTime);
            if(curTo != null){
                extras.putString(RouteActivity.DEST_ADDR, curTo.getAddress());
                extras.putParcelable(RouteActivity.DEST_COORD, curTo.getGeoPoint());
                extras.putParcelable(RouteActivity.DEST_OVERLAY_INFO, curTo.getPoiOverlayInfo());
                intent.putExtras(extras);
                hideBulbBalloon();
                hideStarredBalloon();
                removeAllOD();
                startActivity(intent);
            }
        }
    }
    
    private void startValidationActivity(final Reservation reserv) {
    	MainActivity.initSettingsIfNecessary(LandingActivity2.this, new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(LandingActivity2.this, ValidationActivity.class);
		        intent.putExtra("route", reserv.getRoute());
		        intent.putExtra("reservation", reserv);
		        JSONObject reservRecipients = DebugOptionsActivity.getReservRecipientsAndRemove(LandingActivity2.this, reserv.getRid());
		        intent.putExtra(ValidationActivity.EMAILS, reservRecipients.optString("emails", ""));
		        intent.putExtra(ValidationActivity.PHONES, reservRecipients.optString("phones", ""));
		        hideBulbBalloon();
		        hideStarredBalloon();
		        removeAllOD();
		        startActivity(intent);
			}
    	});
    }
    
    private void refreshStarredPOIs(){
        refreshStarredPOIs(null);
    }
    
    private void refreshStarredPOIs(final Runnable callback){
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
                    //ehs.registerException(e, "[" + request.getURL() + "]\n" + e.getMessage());
                }
                return addrs;
            }
            @Override
            protected void onPostExecute(
                    List<com.smartrek.models.Address> result) {
                if(callback != null){
                    callback.run();
                }
                if (ehs.hasExceptions()) {
                    //ehs.reportExceptions();
                }
                else {
                    Set<String> addrList = new HashSet<String>();
                    final MapView mapView = (MapView) findViewById(R.id.mapview);
                    List<Overlay> overlays = mapView.getOverlays();
                    List<Overlay> otherOverlays = new ArrayList<Overlay>();
                    for (Overlay overlay : overlays) {
                        boolean isOther;
                        if(overlay instanceof POIOverlay){
                            POIOverlay poiOverlay = (POIOverlay)overlay;
                            isOther = !isFavoriteMark(poiOverlay.getMarker());
                            if(!isOther && poiOverlay.isBalloonVisible()){
                                poiOverlay.hideBalloon();
                            }
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
                            PoiOverlayInfo poiInfo = PoiOverlayInfo.fromAddress(a);
                            final POIOverlay star = new POIOverlay(mapView, boldFont, poiInfo, HotspotPlace.CENTER, new POIActionListener() {
									@Override
									public void onClickEdit() {
										hideStarredBalloon();
										View favOpt = findViewById(R.id.fav_opt);
										BalloonModel model = new BalloonModel();
										model.id=a.getId();
										model.label=a.getName();
										model.lat=a.getLatitude();
										model.lon=a.getLongitude();
										model.address=a.getAddress();
										model.geopoint=gp;
										writeInfo2FavoritePanel(model, a.getIconName());
										findViewById(R.id.landing_panel).setVisibility(View.GONE);
										favOpt.setVisibility(View.VISIBLE);
										removeAllOD();
									}

								});
                            star.setAid(a.getId());
                            star.setCallback(new OverlayCallback() {
                                @Override
                                public boolean onTap(int index) {
                                    hideStarredBalloon();
                                    hideBulbBalloon();
                                    removePOIMarker(mapView);
                                    IMapController controller = mapView.getController();
                                    controller.setCenter(star.getGeoPoint());
                                    handleOD(mapView, star, false);
                                    star.showBalloonOverlay();
                                    mapView.postInvalidate();
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
                            insertBeforeMyPointOverlay(overlays, star);
                            star.showOverlay();
                            addrList.add(a.getAddress());
                        }
                    }
                    showODBalloon();
                    mapView.postInvalidate();
                    write2SearchBoxTag(addrList);
                }
            }
        };
        Misc.parallelExecute(task);
    }
    
    private void handleOD(MapView mapView, POIOverlay poi, boolean from) {
    	removeOldOD(mapView, from);
    	poi.setIsFromPoi(from);
    	poi.markODPoi();
    	if(from) {
    		curFrom = poi;
    		curFromProvider = null;
    		curFromTime = 0;
    	}
    	else {
    		curTo = poi;
    		if(curFrom==null && myPointOverlay!=null) {
    			curFrom = new POIOverlay(mapView, Font.getBold(getAssets()), 
    					PoiOverlayInfo.fromCurrentLocation(myPointOverlay), 
    					HotspotPlace.BOTTOM_CENTER, null);
    			curFrom.markODPoi();
    			curFrom.setIsFromPoi(true);
    			mapView.getOverlays().add(curFrom);
    			curFrom.showMiniBalloonOverlay();
    			if(lastLocation != null){
        			curFromProvider = lastLocation.getProvider();
                    curFromTime = lastLocation.getTime();
    			}
    		}
    		toggleGetRouteButton(true);
    	}
    }
    
    private boolean hideStarredBalloon(){
        boolean handled = false;
        MapView mapView = (MapView) findViewById(R.id.mapview);
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay instanceof POIOverlay){
                POIOverlay poiOverlay = (POIOverlay)overlay;
                if(isFavoriteMark(poiOverlay.getMarker())){
                    if(poiOverlay.isBalloonVisible() && !isMarkedOD(poiOverlay)){
                	    poiOverlay.hideBalloon();
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
            if(overlay instanceof POIOverlay){
                POIOverlay poiOverlay = (POIOverlay)overlay;
                if(poiOverlay.getMarker() == R.drawable.bulb_poi){
                    if(poiOverlay.isBalloonVisible() && !isMarkedOD(poiOverlay)){
                    	poiOverlay.hideBalloon();
                        handled = true;
                    }
                }
            }
        }
        return handled;
    }
    
    private RouteRect cityRange;
    
    private void refreshCobranding(final double lat, final double lon, 
            final boolean alertAvailability, final Runnable callback){
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
                if(result != null && StringUtils.isNotBlank(result.html)){
                    if(alertAvailability){
                        CharSequence msg = Html.fromHtml(result.html);
                        NotificationDialog2 dialog = new NotificationDialog2(LandingActivity2.this, msg);
                        dialog.setTitle("Notification");
                        try{
                            dialog.show();
                        }catch(Throwable t){}
                    }
                }else{
                    try{
                        cityRange = new RouteRect(Double.valueOf(result.maxLat * 1E6).intValue(), 
                    		Double.valueOf(result.maxLon * 1E6).intValue(), Double.valueOf(result.minLat * 1E6).intValue(), 
                    		Double.valueOf(result.minLon * 1E6).intValue());
                    }catch(Throwable t){}
                }
                if(callback != null){
                    callback.run();
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
                List<com.smartrek.requests.WhereToGoRequest.Location> locs = Collections.emptyList();
                WhereToGoRequest req = new WhereToGoRequest(user, lat, lon);
                req.invalidateCache(LandingActivity2.this);
                try {
                    locs = req.execute(LandingActivity2.this);
                }
                catch (Exception e) {
                    //ehs.registerException(e, "[" + req.getURL() + "]\n" + e.getMessage());
                }
                return locs;
            }
            @Override
            protected void onPostExecute(final List<com.smartrek.requests.WhereToGoRequest.Location> locs) {
                final int zoomLevel = calculateZoomLevel(lat);
                final MapView mapView = (MapView) findViewById(R.id.mapview);
                final IMapController mc = mapView.getController();
                if (ehs.hasExceptions()) {
                    //ehs.reportExceptions();
                    routeRect = null;
                    if(rezoom){
                        mc.setZoom(zoomLevel);
                        mc.setCenter(new GeoPoint(lat, lon));
                    }
                }
                else {
                    refreshStarredPOIs(new Runnable() {
                        @Override
                        public void run() {
                            List<Overlay> overlays = mapView.getOverlays();
                            List<Overlay> otherOverlays = new ArrayList<Overlay>();
                            for (Overlay overlay : overlays) {
                                boolean isOther;
                                if(overlay instanceof POIOverlay){
                                    POIOverlay poiOverlay = (POIOverlay)overlay;
                                    isOther = poiOverlay.getMarker() != R.drawable.bulb_poi;
                                    if(!isOther && poiOverlay.isBalloonVisible()) {
                                    	poiOverlay.hideBalloon();
                                    }
                                }else{
                                    isOther = true;
                                }
                                if(isOther){
                                    otherOverlays.add(overlay);
                                }
                            }
                            overlays.clear();
                            overlays.addAll(otherOverlays);
                            Set<String> addrSet = new HashSet<String>();
                            if(locs.isEmpty()){
                                routeRect = null;
                                if(rezoom){
                                    mc.setZoom(zoomLevel);
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
                                    addrSet.add(l.addr);
                                }
                            }
                            showODBalloon();
                            mapView.postInvalidate();
                            write2SearchBoxTag(addrSet);
                            refreshSearchAutoCompleteData();
                        }
                    });
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
            	if(findViewById(R.id.fav_opt).getVisibility() == View.GONE) {
	                boolean handledStarred = hideStarredBalloon();
	                boolean handledBulb = hideBulbBalloon();
	                boolean handledPOI = removePOIMarker(mapView);
	                boolean handledOD = removeOldOD(mapView, false);
	                boolean hasFocus = searchBox.isFocused() || fromSearchBox.isFocused();
	                if(hasFocus) {
	                	searchBox.clearFocus();
	                	fromSearchBox.clearFocus();
	                }
	                if(!handledStarred && !handledBulb && !handledPOI && !handledOD && !hasFocus){
	                    resizeMap(!isMapCollapsed());
	                }
            	}
            	else {
            		hideStarredBalloon();
            		hideBulbBalloon();
            		removePOIMarker(mapView);
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
        View landingPanelView = findViewById(R.id.landing_panel_content);
        int landingPanelHeight = landingPanelView.getHeight();
        ObjectAnimator landingPanelAnimator;
        if(collapsed) {
        	landingPanelAnimator = ObjectAnimator.ofFloat(landingPanelView, "translationY", -landingPanelHeight, 0); 
        }
        else {
        	landingPanelAnimator = ObjectAnimator.ofFloat(landingPanelView, "translationY", 0, -landingPanelHeight);
        }
        landingPanelAnimator.setDuration(500);
        landingPanelAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        View myMetropiaPanel = findViewById(R.id.my_metropia_panel);
        int myMetropiaPanelHeight = myMetropiaPanel.getHeight();
        ObjectAnimator myMetropiaPanelAnimator;
        if(collapsed) {
        	myMetropiaPanelAnimator = ObjectAnimator.ofFloat(myMetropiaPanel, "translationY", myMetropiaPanelHeight, 0);
        }
        else {
        	myMetropiaPanelAnimator = ObjectAnimator.ofFloat(myMetropiaPanel, "translationY", 0, myMetropiaPanelHeight);
        }
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.play(landingPanelAnimator).with(myMetropiaPanelAnimator);
		animatorSet.start();
    }
    
    private void toggleGetRouteButton(boolean show) {
    	View getRoutePanel = findViewById(R.id.get_route_panel);
    	View getRouteButton = findViewById(R.id.get_route);
    	getRouteButton.setVisibility(show?View.VISIBLE:View.GONE);
    	float height = Dimension.dpToPx(55, getResources().getDisplayMetrics());
    	ObjectAnimator getRouteAnimator;
    	if(show) {
    		getRouteAnimator = ObjectAnimator.ofFloat(getRoutePanel, "translationY", height, 0);
    		getRouteAnimator.setDuration(300);
    		getRouteAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    		getRouteAnimator.start();
    	}
    }
    
    private void write2SearchBoxTag(Set<String> addresses) {
    	Set<String> oAddrs = (Set<String>) findViewById(R.id.search_box).getTag();
        if(oAddrs == null) {
        	oAddrs = new HashSet<String>();
        }
        oAddrs.addAll(addresses);
        findViewById(R.id.search_box).setTag(oAddrs);
    }
    
    private boolean isPoiOverlay(String address) {
    	View searchBox = findViewById(R.id.search_box);
    	if(searchBox.getTag() != null) {
    		Set<String> poiAddrs = (Set<String>) searchBox.getTag();
    		return poiAddrs.contains(address);
    	}
    	return false;
    }
    
    
    
    protected static abstract class ReverseGeocodingTask extends AsyncTask<Void, Void, String> {
        
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
    
    private POIOverlay curMarker;
    
    private boolean removePOIMarker(MapView mapView){
        boolean handled = false;
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay == curMarker){
            	POIOverlay curOverlay = (POIOverlay)overlay;
            	if(curOverlay.isMarked() && (curOverlay.isFromPoi() == isFromTab())) {
	            	Log.d("LandingActivity2", "removePOIMarker : " + curOverlay);
	                if(curOverlay.isBalloonVisible()){
	                    curOverlay.hideBalloon();
	                    handled = true;
	                }
	                overlays.remove(curOverlay);
	                mapView.postInvalidate();
	                curMarker = null;
	                break;
            	}
            }
        }
        return handled;
    }
    
    private boolean isMarkedOD(Overlay overlay) {
    	return overlay==curFrom || overlay==curTo;
    }
    
    private boolean hidePOIMarkerBalloon(MapView mapView){
    	boolean handled = false;
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay instanceof POIOverlay){
                POIOverlay poiOverlay = (POIOverlay)overlay;
                if(overlay == curMarker){
                    if(poiOverlay.isBalloonVisible()){
                	    poiOverlay.hideBalloon();
                        handled = true;
                    }
                }
            }
        }
        return handled;
    }
    
    private boolean removeAllOD() {
    	MapView mapView = (MapView) findViewById(R.id.mapview);
    	boolean handleFrom = removeOldOD(mapView, true);
    	boolean handleTo = removeOldOD(mapView, false);
    	return handleFrom || handleTo;
    }
    
    private boolean removeOldOD(MapView mapView, boolean from) {
    	List<Overlay> overlays = mapView.getOverlays();
    	if(from) {
    		curFrom = null;
    	}
    	else {
    		curTo = null;
    		toggleGetRouteButton(false);
    	}
    	boolean handle = false;
        for (Overlay overlay : overlays) {
            if(overlay instanceof POIOverlay){
                POIOverlay poiOverlay = (POIOverlay)overlay;
                if(poiOverlay.isMarked() && (from == poiOverlay.isFromPoi())){
                	handle = true;
                    if(poiOverlay.isBalloonVisible()){
                	    poiOverlay.hideBalloon();
                    }
                    if(poiOverlay.getMarker() == 0) {
                    	overlays.remove(poiOverlay);
                    }
                    else {
                    	poiOverlay.cancelMark();
                    }
                    mapView.postInvalidate();
                    break;
                }
            }
        }
        return handle;
    }
    
    private void refreshPOIMarker(final MapView mapView, final double lat, final double lon,
            final String address, final String label){
        removePOIMarker(mapView);
        final GeoPoint gp = new GeoPoint(lat, lon);
        final BalloonModel model = new BalloonModel();
        model.lat = lat;
        model.lon = lon;
        model.address = address;
        model.label = label;
        model.geopoint = gp;
        PoiOverlayInfo poiInfo = PoiOverlayInfo.fromBalloonModel(model);
        final POIOverlay marker = new POIOverlay(mapView, Font.getBold(getAssets()), poiInfo, 
        		HotspotPlace.BOTTOM_CENTER , new POIActionListener() {
					@Override
					public void onClickEdit() {
						hidePOIMarkerBalloon(mapView);
						writeInfo2FavoritePanel(model, null);
						findViewById(R.id.landing_panel).setVisibility(View.GONE);
						findViewById(R.id.fav_opt).setVisibility(View.VISIBLE);
						removeAllOD();
					}
        });
        
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
        if(!isInFavoriteOperation()) {
        	handleOD(mapView, marker, false);
        	marker.showBalloonOverlay();
        }
        else {
        	writeInfo2FavoritePanel(model, null);
        }
        curMarker = marker;
        mapView.postInvalidate();
    }
    
    private boolean isInFavoriteOperation() {
    	return findViewById(R.id.fav_opt).getVisibility() == View.VISIBLE;
    }
    
    private boolean isFavoriteOptComplete() {
    	View favOptPanel = findViewById(R.id.fav_opt);
    	String favAddr = ((EditText) favOptPanel.findViewById(R.id.favorite_search_box)).getText().toString();
//    	String label = ((EditText) favOptPanel.findViewById(R.id.label_input)).getText().toString();
//    	IconType icon = (IconType) favOptPanel.findViewById(R.id.icon).getTag();
//    	return StringUtils.isNotBlank(favAddr) && StringUtils.isNotBlank(label) && icon!=null;
    	return StringUtils.isNotBlank(favAddr);
    }
    
    private void writeInfo2FavoritePanel(BalloonModel model, String iconName) {
    	View favOptPanel = findViewById(R.id.fav_opt);
    	favOptPanel.setTag(model);
    	((EditText) favOptPanel.findViewById(R.id.label_input)).setText(model.label);
    	if(StringUtils.isNotBlank(model.label)) {
    		favOptPanel.findViewById(R.id.label_clear).setVisibility(View.VISIBLE);
    	}
    	IconType icon = IconType.fromName(iconName);
    	if(icon != null) {
    		favOptPanel.findViewById(R.id.icon).setTag(icon);
    		Integer[] iconInfo = IconType.getIconInfos(icon);
    		unSelectAllIcon();
    		((ImageView)favOptPanel.findViewById(iconInfo[0])).setImageResource(iconInfo[1]);
    	}
    	EditText searchBox = (EditText) favOptPanel.findViewById(R.id.favorite_search_box); 
    	searchBox.setText(model.address);
    	searchBox.setEnabled(StringUtils.isNotBlank(model.address)?false:true);
    	favOptPanel.findViewById(R.id.fav_search_box_clear).setVisibility(model.id!=0?View.GONE:View.VISIBLE);
    	favOptPanel.findViewById(R.id.fav_save).setVisibility(StringUtils.isNotBlank(model.address)?View.VISIBLE:View.GONE);
    	favOptPanel.findViewById(R.id.fav_del_panel).setVisibility(model.id!=0?View.VISIBLE:View.GONE);
    	((TextView)favOptPanel.findViewById(R.id.header)).setText(model.id!=0?"Edit Favorite":"Add Favorite");
    }
    
    private void updateMyMetropiaInfo() {
    	AsyncTask<Void, Void, MyMetropia> updateTask = new AsyncTask<Void, Void, MyMetropia>() {
			@Override
			protected MyMetropia doInBackground(Void... params) {
				User user = User.getCurrentUser(LandingActivity2.this);
				MyMetropiaRequest request = new MyMetropiaRequest(user.getUsername());
				MyMetropia info = null;
				try {
					info = request.execute(LandingActivity2.this);
				}
				catch(Exception e) {
					ehs.registerException(e);
				}
				return info;
			}
			
			protected void onPostExecute(MyMetropia info) {
				if(!ehs.hasExceptions()) {
					int reward = info.getCredit();
					StringBuffer rewardString = new StringBuffer();
					if(reward >= 1000) {
						rewardString = rewardString.append(reward / 1000).append("K");
					}
					else {
						rewardString.append(reward); 
						while(rewardString.length() < 3) {
							rewardString.insert(0, "0");
						}
					}
					rewardString.append("Pts");
					upointView.setText(formatMyMetropiaInfo(rewardString.toString()));
					
					int timeSaving = info.getTimeSaving();
					StringBuffer timeSavingString = new StringBuffer();
					if(timeSaving >= 1000) {
						timeSavingString.append(timeSaving / 1000).append("K");
					}
					else {
						timeSavingString.append(timeSaving);
						while(timeSavingString.length() < 2) {
							timeSavingString.insert(0, "0");
						}
					}
					timeSavingString.append("Min");
					saveTimeView.setText(formatMyMetropiaInfo(timeSavingString.toString()));
					
					double co2Saving = info.getCo2Saving();
					StringBuffer co2SavingString = new StringBuffer();
					if(co2Saving >= 1000) {
						co2SavingString.append(co2Saving / 1000).append("K");
					}
					else {
						co2SavingString.append(co2Saving);
						while(co2SavingString.length() < 3) {
							co2SavingString.insert(0, "0");
						}
					}
					co2SavingString.append("lbs");
					co2View.setText(formatMyMetropiaInfo(co2SavingString.toString()));
				}
			}
    		
    	};
    	Misc.parallelExecute(updateTask);
    }
    
    private SpannableString formatMyMetropiaInfo(String content) {
    	int indexOfChange = getAlphaIndex(content);
    	SpannableString startTimeSpan = SpannableString.valueOf(content);
    	if(indexOfChange != -1) {
    		startTimeSpan.setSpan(new AbsoluteSizeSpan(Dimension.dpToPx(13, getResources().getDisplayMetrics())), 
    				indexOfChange, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    	}
    	return startTimeSpan;
    	
    }
    
    private int getAlphaIndex(String content) {
    	char[] charArray = content.toCharArray();
    	for(int i = 0 ; i < charArray.length ; i++) {
    		if(Character.isLetter(charArray[i])) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    private synchronized void drawBulbPOIs(final MapView mapView, List<com.smartrek.requests.WhereToGoRequest.Location> locs) {
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay instanceof POIOverlay){
                POIOverlay poiOverlay = (POIOverlay)overlay;
                if(poiOverlay.getMarker() == R.drawable.bulb_poi){
                    overlays.remove(overlay);
                }
            }
        }
        
        initFontsIfNecessary();
        for(final com.smartrek.requests.WhereToGoRequest.Location l:locs){
            final GeoPoint gp = new GeoPoint(l.lat, l.lon);
            PoiOverlayInfo poiInfo = PoiOverlayInfo.fromLocation(l);
            final POIOverlay bulb = new POIOverlay(mapView, boldFont, poiInfo, 
            		HotspotPlace.CENTER, new POIActionListener() {
	            	@Override
					public void onClickEdit() {
						hideBulbBalloon();
						View favOpt = findViewById(R.id.fav_opt);
						BalloonModel model = new BalloonModel();
						model.label="";
						model.lat=l.lat;
						model.lon=l.lon;
						model.address=l.addr;
						model.geopoint=gp;
						writeInfo2FavoritePanel(model, null);
						findViewById(R.id.landing_panel).setVisibility(View.GONE);
						favOpt.setVisibility(View.VISIBLE);
						removeAllOD();
					}
	
            });
            bulb.setCallback(new OverlayCallback() {
                @Override
                public boolean onTap(int index) {
                    hideStarredBalloon();
                    hideBulbBalloon();
                    removePOIMarker(mapView);
                    IMapController controller = mapView.getController();
                    controller.setCenter(bulb.getGeoPoint());
                    handleOD(mapView, bulb, false);
                    bulb.showBalloonOverlay();
                    mapView.postInvalidate();
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
            insertBeforeMyPointOverlay(overlays, bulb);
            bulb.showOverlay();
            showODBalloon();
        }
    }
    
    private void insertBeforeMyPointOverlay(List<Overlay> mapOverlays, Overlay overlay) {
    	int myCurrentOverlayIdx = mapOverlays.indexOf(myPointOverlay)!=-1?mapOverlays.indexOf(myPointOverlay):mapOverlays.size();
    	mapOverlays.add(myCurrentOverlayIdx, overlay);
    }
    
    private boolean isFromPoi() {
    	return fromSearchBox.isFocused() && !isInFavoriteOperation();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(tripInfoCachedUpdater);
        unregisterReceiver(updateMyLocation);
        unregisterReceiver(updateMenuMyTrips);
        closeGPS();
        SKMaps.getInstance().destroySKMaps();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        if(requestCode == -1) {
            finish();
        }
        
        Bundle extras = intent == null?null:intent.getExtras();
        
        if(requestCode == ON_MY_WAY && resultCode == Activity.RESULT_OK) {
        	final String emails = extras.getString(ValidationActivity.EMAILS);
        	final String phones = extras.getString(ValidationActivity.PHONES);
        	Reservation reserv = (Reservation) findViewById(R.id.trip_info).getTag();
        	DebugOptionsActivity.addRecipientsOfReserv(LandingActivity2.this, reserv.getRid(), emails, phones);
        }
    }
    
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
            	if(!isInFavoriteOperation() && 
            			findViewById(R.id.trip_info).getVisibility()!=View.VISIBLE &&
            			findViewById(R.id.reservations_list).getVisibility()!=View.VISIBLE) {
	                final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	                View drawer = findViewById(R.id.left_drawer);
	                if(mDrawerLayout.isDrawerOpen(drawer)){
	                    mDrawerLayout.closeDrawer(drawer);
	                }else{
	                    mDrawerLayout.openDrawer(drawer);
	                }
                }
                return true;
        }

        return super.onKeyDown(keycode, e);
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
    
    static class RescheduleTripTask extends AsyncTask<Void, Void, Void> {
        
        interface Callback {
            
            void run(Reservation reserv);
            
        }
        
        CancelableProgressDialog dialog;
        
        String originAddress;
        
        String address;
        
        LandingActivity2 activity;
        
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
        
        long id;
        
        RescheduleTripTask(LandingActivity2 ctx, GeoPoint origin, String originAddress, String destAddress, 
        		long rescheduleId, ExceptionHandlingService ehs){
            this.ehs = ehs;
            this.ctx = ctx;
            this.activity = ctx;
            this.origin = origin;
            this.originAddress = originAddress;
            this.address = destAddress;
            this.id = rescheduleId;
            dialog = new CancelableProgressDialog(ctx, "Loading...");
        }
        
        @Override
        protected void onPreExecute() {
            dialog.show();
            if(this._route == null && origin == null){
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
                                    0, 0, originAddress, address);
                                route = routeReq.execute(ctx).get(0);
                                route.setAddresses(originAddress, address);
                                route.setUserId(user.getId());
                            }else{
                               route = _route; 
                            }
                            ReservationRequest reservReq = new ReservationRequest(user, 
                                route, ctx.getString(R.string.distribution_date), id);
                            reservReq.execute(ctx);
                            Log.d("LandingActivity2", "new reservation id : " + route.getId());
                            ReservationListFetchRequest reservListReq = new ReservationListFetchRequest(user);
                            reservListReq.invalidateCache(ctx);
                            List<Reservation> reservs = reservListReq.execute(ctx);
                            
                            for (Reservation r : reservs) {
                            	Log.d("LandingActivity2", "reserved id : " + r.getRid());
                                if(((Long)r.getRid()).equals(route.getId())){
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
                });
            }
        }
        
    }
    
}
