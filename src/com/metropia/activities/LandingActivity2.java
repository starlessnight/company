package com.metropia.activities;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.internal.nineoldandroids.animation.Animator;
import com.actionbarsherlock.internal.nineoldandroids.animation.AnimatorSet;
import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.PassiveLocationChangedReceiver;
import com.localytics.android.Localytics;
import com.metropia.CalendarService;
import com.metropia.LocalyticsUtils;
import com.metropia.ResumeNavigationUtils;
import com.metropia.SkobblerUtils;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.dialogs.BlurDialog;
import com.metropia.dialogs.CancelableProgressDialog;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.dialogs.NotificationDialog2.ActionListener;
import com.metropia.dialogs.NotifyResumeDialog;
import com.metropia.dialogs.ReleaseDialog;
import com.metropia.models.FavoriteIcon;
import com.metropia.models.POIContainer;
import com.metropia.models.Reservation;
import com.metropia.models.ReservationTollHovInfo;
import com.metropia.models.Route;
import com.metropia.models.User;
import com.metropia.requests.CityRequest;
import com.metropia.requests.CityRequest.City;
import com.metropia.requests.FavoriteAddressFetchRequest;
import com.metropia.requests.MyMetropiaRequest;
import com.metropia.requests.MyMetropiaRequest.MyMetropia;
import com.metropia.requests.Request;
import com.metropia.requests.Request.Page;
import com.metropia.requests.ReservationDeleteRequest;
import com.metropia.requests.ReservationListFetchRequest;
import com.metropia.requests.ReservationRequest;
import com.metropia.requests.RouteFetchRequest;
import com.metropia.requests.SaveLocationRequest;
import com.metropia.requests.UpdateDeviceIdRequest;
import com.metropia.requests.WhereToGoRequest;
import com.metropia.ui.DelayTextWatcher;
import com.metropia.ui.DelayTextWatcher.TextChangeListener;
import com.metropia.ui.EditAddress;
import com.metropia.ui.SkobblerImageView;
import com.metropia.ui.SwipeDeleteTouchListener;
import com.metropia.ui.animation.ClickAnimation;
import com.metropia.ui.animation.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.ui.menu.MainMenu;
import com.metropia.ui.timelayout.AdjustableTime;
import com.metropia.utils.Cache;
import com.metropia.utils.CalendarContract.Instances;
import com.metropia.utils.Dimension;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.Geocoding;
import com.metropia.utils.Geocoding.Address;
import com.metropia.utils.HTTP;
import com.metropia.utils.Misc;
import com.metropia.utils.Preferences;
import com.metropia.utils.RouteNode;
import com.metropia.utils.RouteRect;
import com.metropia.utils.SystemService;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.map.SKAnimationSettings;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKAnnotationView;
import com.skobbler.ngx.map.SKBoundingBox;
import com.skobbler.ngx.map.SKCoordinateRegion;
import com.skobbler.ngx.map.SKMapCustomPOI;
import com.skobbler.ngx.map.SKMapPOI;
import com.skobbler.ngx.map.SKMapSettings.SKMapDisplayMode;
import com.skobbler.ngx.map.SKMapSettings.SKMapFollowerMode;
import com.skobbler.ngx.map.SKMapSurfaceListener;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKMapViewHolder;
import com.skobbler.ngx.map.SKPOICluster;
import com.skobbler.ngx.map.SKPolyline;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.positioner.SKPosition;


public final class LandingActivity2 extends FragmentActivity implements SKMapSurfaceListener, SensorEventListener, ConnectionCallbacks, 
                                      OnConnectionFailedListener, ResultCallback<LocationSettingsResult> { 
    
    private static final int DEFAULT_ZOOM_LEVEL = 12;
    
    private static final int SEARCH_ZOOM_LEVEL = 16;
    
    private static final double mapZoomVerticalOffset = 0.3;

    public static final String LAT = "lat";
    
    public static final String LON = "lon";
    
    public static final String MSG = "msg";
    
    public static final boolean ENABLED = true;
    
    public static final String NO_TRIPS = "No Upcoming Trip";
    
    public static final int ON_MY_WAY = Integer.valueOf(100);
    
    public static final String LOGOUT = "logout";
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    private Queue<Runnable> mapActionQueue = new LinkedList<Runnable>();
	
    private GeoPoint myPoint;
    
    private LocationManager locationManager;

    private LocationListener locationListener;
    private android.location.LocationListener systemLocationListener;
    
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
    private List<Address> inputAddresses = new ArrayList<Address>();
    private ArrayList<com.metropia.models.Address> favoriteAddresses = new ArrayList<com.metropia.models.Address>();
    
    private AtomicBoolean canDrawReservRoute = new AtomicBoolean();
    
    private ListView searchResultList;
    private ListView fromSearchResultList;
    private ListView fromFavoriteDropdown;
    private ListView toFavoriteDropdown;
    
    private ArrayAdapter<Address> autoCompleteAdapter;
    private ArrayAdapter<Address> fromAutoCompleteAdapter;
    private ArrayAdapter<Address> fromFavoriteAutoCompleteAdapter;
    private ArrayAdapter<Address> toFavoriteAutoCompleteAdapter;
    
    public static final String NO_AUTOCOMPLETE_RESULT = "No results found.";
    
    public static final String SEARCHING = "Searching...";
    
//    private static final String TAP_TO_ADD_FAVORITE = "Tap to Add Favorite";
    
    private AtomicBoolean showAutoComplete = new AtomicBoolean(true);
    
    private ImageView tripNotifyIcon;
    
    private TextView getRouteView;
    
    private PoiOverlayInfo curFrom;
    private PoiOverlayInfo curTo;
    
    private String curFromProvider;
    private long curFromTime;
    
    private EditText searchBox;
    private EditText fromSearchBox;
    private View searchBoxClear;
    private View fromSearchBoxClear;
    
    private TextView upointView;
    private TextView saveTimeView;
    private TextView co2View;
    
    private AtomicBoolean needCheckResume = new AtomicBoolean(true);
    
    private FrameLayout popupPanel;
    private ImageView fromMenu;
    private ImageView toMenu;
    private ImageView editMenu;
    private ImageView poiIcon;
    private TextView addressInfo;
    
    private ImageView fromIcon;
    private ImageView toIcon;
    private View fromMask;
    private View toMask;
    
    private View newUserTipView;
    
    View toDropDownButton;
    View fromDropDownButton;
    
    private static final String DROP_STATE = "dropdown";
    
    private AtomicBoolean checkCalendarEvent = new AtomicBoolean(true);
    
    private POIContainer poiContainer = new POIContainer();
    
    private SKMapViewHolder mapViewHolder;
    private SKMapSurfaceView mapView;
    
    private String versionNumber = "";
//    private AtomicBoolean locationRefreshed = new AtomicBoolean(false);
    private AtomicBoolean needTagAustinLaunch = new AtomicBoolean(true);
    private AtomicBoolean cancelGetRoute = new AtomicBoolean(false);
    
    public static final Long TEXT_INPUT_DELAY = Long.valueOf(1000);
    
    //debug
//    private GeoPoint debugOrigin = new GeoPoint(33.8689924, -117.9220526);
    
    private AtomicBoolean disableShowPassengerMode = new AtomicBoolean(false);
    
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
    	SkobblerUtils.initializeLibrary(LandingActivity2.this);
        setContentView(R.layout.landing2);
        
        mapViewHolder = (SKMapViewHolder) findViewById(R.id.mapview_holder);
        mapViewHolder.hideAllAttributionTextViews();
		mapViewHolder.setMapSurfaceListener(this);
        
        Localytics.integrate(this);
        
        registerReceiver(tripInfoCachedUpdater, new IntentFilter(TRIP_INFO_CACHED_UPDATES));
        registerReceiver(updateMyLocation, new IntentFilter(UPDATE_MY_LOCATION));
        registerReceiver(updateMenuMyTrips, new IntentFilter(UPDATE_MENU_MY_TRIPS));
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
        try {
			versionNumber = MapDisplayActivity.OS_NAME + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		}catch(NameNotFoundException ignore) {}
        
//        RouteActivity.setViewToNorthAmerica(mapView);
        setLocationRefreshStatus(false);
        
        searchResultList = (ListView) findViewById(R.id.search_result_list);
        fromSearchResultList = (ListView) findViewById(R.id.from_search_result_list);
        fromFavoriteDropdown = (ListView) findViewById(R.id.from_favorite_drop_down);
        toFavoriteDropdown = (ListView) findViewById(R.id.to_favorite_drop_down);
        searchBox = (EditText) findViewById(R.id.search_box);
        searchBox.setHint(Html.fromHtml("<b>Enter Name or Address</b>"));
        fromSearchBox = (EditText) findViewById(R.id.from_search_box);
        fromSearchBox.setHint(Html.fromHtml("<b>Current or any Location</b>"));
        autoCompleteAdapter = createAutoCompleteAdapter(LandingActivity2.this, searchBox);
        fromAutoCompleteAdapter = createAutoCompleteAdapter(LandingActivity2.this, fromSearchBox);
        fromFavoriteAutoCompleteAdapter = createAutoCompleteAdapter(LandingActivity2.this, fromSearchBox);
        toFavoriteAutoCompleteAdapter = createAutoCompleteAdapter(LandingActivity2.this, searchBox);
        searchResultList.setAdapter(autoCompleteAdapter);
        fromSearchResultList.setAdapter(fromAutoCompleteAdapter);
        fromFavoriteDropdown.setAdapter(fromFavoriteAutoCompleteAdapter);
        toFavoriteDropdown.setAdapter(toFavoriteAutoCompleteAdapter);
        
        fromMask = findViewById(R.id.from_mask);
        toMask = findViewById(R.id.to_mask);
        
        OnClickListener noopClick = new OnClickListener() {
            @Override
            public void onClick(View v) {}
        };
        
        View landingPanelView = findViewById(R.id.landing_panel_content);
        landingPanelView.setOnClickListener(noopClick);
        
//        refreshSearchAutoCompleteData();
//        refreshFromSearchAutoCompleteData();
        
        toDropDownButton = findViewById(R.id.to_drop_down_button);
        fromDropDownButton = findViewById(R.id.from_drop_down_button);
        final ImageView toDropDownIcon = (ImageView) findViewById(R.id.to_drop_down_icon);
        toDropDownButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						hidePopupMenu();
						if(DROP_STATE.equals(toDropDownButton.getTag())) {
							toDropDownIcon.setImageResource(R.drawable.drop_down_arrow);
							toFavoriteDropdown.setVisibility(View.GONE);
							toDropDownButton.setTag("");
						}
						else {
							searchBox.clearFocus();
							if(DROP_STATE.equals(fromDropDownButton.getTag())) {
								fromDropDownButton.performClick();
							}
							toDropDownIcon.setImageResource(R.drawable.shrink_arrow);
							toFavoriteDropdown.setVisibility(View.VISIBLE);
							toDropDownButton.setTag(DROP_STATE);
						}
					}
				});
			}
        });
        
        final ImageView fromDropDownIcon = (ImageView) findViewById(R.id.from_drop_down_icon);
        fromDropDownButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						hidePopupMenu();
						if(DROP_STATE.equals(fromDropDownButton.getTag())) {
							fromDropDownIcon.setImageResource(R.drawable.drop_down_arrow);
							fromFavoriteDropdown.setVisibility(View.GONE);
							fromDropDownButton.setTag("");
						}
						else {
							fromSearchBox.clearFocus();
							if(DROP_STATE.equals(toDropDownButton.getTag())) {
								toDropDownButton.performClick();
							}
							fromDropDownIcon.setImageResource(R.drawable.shrink_arrow);
							fromFavoriteDropdown.setVisibility(View.VISIBLE);
							fromDropDownButton.setTag(DROP_STATE);
						}
					}
				});
			}
        });
        
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                final String addrInput = v.getText().toString();
                boolean handled = StringUtils.isNotBlank(addrInput);
                if(handled){
                    searchToAddress(addrInput, true);
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
                	searchFromAddress(addrInput, true);
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
            	fromMask.setVisibility(hasFocus?View.VISIBLE:View.GONE);
                if(!hasFocus) {
                	InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    showAutoComplete.set(false);
                    searchResultList.setVisibility(View.GONE);
                    fromSearchResultList.setVisibility(View.GONE);
                }
                else {
                	if(DROP_STATE.equals(toDropDownButton.getTag())) {
                		toDropDownButton.performClick();
                	}
                    if(StringUtils.isBlank(searchBox.getText())) {
                    	searchAddresses.clear();
                    	searchAddresses.addAll(inputAddresses);
                    	showAutoComplete.set(true);
                    }
                    refreshSearchAutoCompleteData();
                }
            }
        });
        
        fromSearchBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            	toMask.setVisibility(hasFocus?View.VISIBLE:View.GONE);
                if(!hasFocus) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    showAutoComplete.set(false);
                    searchResultList.setVisibility(View.GONE);
                    fromSearchResultList.setVisibility(View.GONE);
                }
                else {
                	if(DROP_STATE.equals(fromDropDownButton.getTag())) {
                		fromDropDownButton.performClick();
                	}
                    if(StringUtils.isBlank(fromSearchBox.getText())) {
                        fromSearchAddresses.clear();
                        fromSearchAddresses.addAll(inputAddresses);
                        showAutoComplete.set(true);
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
            		dropPinForAddress(selected, true, false);
	                InputMethodManager imm = (InputMethodManager)getSystemService(
	                        Context.INPUT_METHOD_SERVICE);
	                imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
	                showAutoComplete.set(false);
	                clearSearchResult();
            	}
//            	else if(TAP_TO_ADD_FAVORITE.equals(selected.getName())) {
//            		clearSearchResult();
//            		removePOIMarker(mapView);
//            		hideBulbBalloon();
//            		hideStarredBalloon();
//            		showFavoriteOptPanel(null);
//            	}
            }
        });
        
        fromSearchResultList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address selected = (Address)parent.getItemAtPosition(position);
                if(StringUtils.isNotBlank(selected.getAddress())) {
                	dropPinForAddress(selected, true, true);
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(fromSearchBox.getWindowToken(), 0);
                    showAutoComplete.set(false);
                    clearFromSearchResult();
                }
//                else if(TAP_TO_ADD_FAVORITE.equals(selected.getName())) {
//                    clearFromSearchResult();
//                    removePOIMarker(mapView);
//                    hideBulbBalloon();
//                    hideStarredBalloon();
//                    showFavoriteOptPanel(null);
//                }
            }
        });
        
        fromFavoriteDropdown.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Address selected = (Address)parent.getItemAtPosition(position);
                if(StringUtils.isNotBlank(selected.getAddress())) {
                	dropPinForAddress(selected, true, true);
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(fromSearchBox.getWindowToken(), 0);
                    showAutoComplete.set(false);
                    clearFromSearchResult();
                    fromDropDownButton.performClick();
                }
			}
        });
        
        toFavoriteDropdown.setOnItemClickListener(new OnItemClickListener() {
        	@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Address selected = (Address)parent.getItemAtPosition(position);
                if(StringUtils.isNotBlank(selected.getAddress())) {
                	dropPinForAddress(selected, true, false);
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
                    showAutoComplete.set(false);
                    clearFromSearchResult();
                    toDropDownButton.performClick();
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
        
        searchBoxClear = findViewById(R.id.search_box_clear);
        DelayTextWatcher delayTextWatcher = new DelayTextWatcher(searchBox, new TextChangeListener(){
			@Override
			public void onTextChanged(CharSequence text) {
				searchBoxClear.setVisibility(StringUtils.isBlank(text)?View.GONE:View.VISIBLE); 
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
//        						List<Address> emptyAddresses = getEmptyAddressesForUI();
//        						searchAddresses.addAll(emptyAddresses);
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
				showAutoComplete.set(true);
				if(removeOD.get()) {
					removeOldOD(false);
				}
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
		}, TEXT_INPUT_DELAY, DelayTextWatcher.FORCE_NOTIFY_SPACE);
        
        searchBox.addTextChangedListener(delayTextWatcher);
        
        searchBoxClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	removeOldOD(false);
                searchBox.setText("");
                toIcon.setImageResource(0);
                toIcon.setVisibility(View.INVISIBLE);
                clearSearchResult();
                if(curFrom != null && StringUtils.isBlank(curFrom.address)) {
                	fromSearchBoxClear.performClick();
                }
            }
        });
        
        fromSearchBoxClear = findViewById(R.id.from_search_box_clear);
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
//                                List<Address> emptyAddresses = getEmptyAddressesForUI();
//                                fromSearchAddresses.addAll(emptyAddresses);
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
            	showAutoComplete.set(true);
            	if(removeOD.get()) {
                	removeOldOD(true);
                }
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
        }, TEXT_INPUT_DELAY, DelayTextWatcher.FORCE_NOTIFY_SPACE);
        
        fromSearchBox.addTextChangedListener(fromDelayTextWatcher);
        fromSearchBoxClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	removeOldOD(true);
                fromIcon.setImageResource(0);
                fromIcon.setVisibility(View.INVISIBLE);
                fromSearchBox.setText("");
                clearFromSearchResult();
            }
        });
        
        final String intentAddress = getIntentAddress(getIntent());
        boolean hasIntentAddr = StringUtils.isNotBlank(intentAddress); 
        mapRezoom.set(!hasIntentAddr);
        if(hasIntentAddr){
        	searchIntentAddress(intentAddress);
        	disableRefreshTripInfo.set(true);
        }
        
        if(isToSignInPage(getIntent())) {
            Intent signInIntent = new Intent(LandingActivity2.this, LoginActivity.class);
            startActivity(signInIntent);
            finish();
        }
        
        User.initializeIfNeccessary(this, new Runnable() {
        	@Override
        	public void run() {
        		updateDeviceId();
        	}
       	});
        
        initReservationListView();
        
        final Bundle extras = getIntent().getExtras();
        
        locationListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location location) {
//                fake lat-lon
//                location.setLatitude(34.0291747); // LA
//                location.setLongitude(-118.2734106);
//                location.setLatitude(32.1559094); // Tucson
//                location.setLongitude(-110.883805);
//	              location.setLatitude(22.980648); // Tainan
//	              location.setLongitude(120.236046);
//            	  location.setLatitude(30.18155); // Austin
//            	  location.setLongitude(-97.62175);
            	GeoPoint debugLoc = DebugOptionsActivity.getCurrentLocationLatLon(LandingActivity2.this);
				if(debugLoc != null) {
					location.setLatitude(debugLoc.getLatitude());
					location.setLongitude(debugLoc.getLongitude());
				}
				
                if (ValidationActivity.isBetterLocation(location, lastLocation)) {
                	setLocationRefreshStatus(true);
					if(needTagAustinLaunch.getAndSet(false)) {
						LocalyticsUtils.setAustinLaunchIfInBoundingBox(location.getLatitude(), location.getLongitude());
					}
                    locationChanged(location);
                    if(checkCalendarEvent.getAndSet(false) && extras != null) {
                    	handleCalendarNotification(extras.getInt(RouteActivity.EVENT_ID));
                    }
                }
            }
        };
        
        systemLocationListener = new android.location.LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
//              fake lat-lon
//              location.setLatitude(34.0291747); // LA
//              location.setLongitude(-118.2734106);
//              location.setLatitude(32.1559094); // Tucson
//              location.setLongitude(-110.883805);
//	            location.setLatitude(22.980648); // Tainan
//	            location.setLongitude(120.236046);
				GeoPoint debugLoc = DebugOptionsActivity.getCurrentLocationLatLon(LandingActivity2.this);
				if(debugLoc != null) {
					location.setLatitude(debugLoc.getLatitude());
					location.setLongitude(debugLoc.getLongitude());
				}
				setLocationRefreshStatus(true);
				if (ValidationActivity.isBetterLocation(location, lastLocation)) {
					if(needTagAustinLaunch.getAndSet(false)) {
						LocalyticsUtils.setAustinLaunchIfInBoundingBox(location.getLatitude(), location.getLongitude());
					}
                  locationChanged(location);
                  if(checkCalendarEvent.getAndSet(false) && extras != null) {
                  	handleCalendarNotification(extras.getInt(RouteActivity.EVENT_ID));
                  }
              }
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {}

			@Override
			public void onProviderEnabled(String provider) {}

			@Override
			public void onProviderDisabled(String provider) {}
        	
        };
        
        View centerMapIcon = findViewById(R.id.center_map_icon);
        centerMapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
            	clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
//						SKCoordinateRegion mapRegion = mapView.getCurrentMapRegion();
//						SKCoordinate centerPoint = mapRegion.getCenter();
//		                int latE6 = Double.valueOf(centerPoint.getLatitude() * 1E6).intValue();
//		                int lonE6 = Double.valueOf(centerPoint.getLongitude() * 1E6).intValue();
//		                int lastLatE6 = mapCenterLat.get();
//		                int lastLonE6 = mapCenterLon.get();
//		                int threshold = 100 + 2300 * (Math.max(Double.valueOf(18 - mapView.getZoomLevel()).intValue(), 0));
//		                if(Math.abs(latE6 - lastLatE6) < threshold && Math.abs(lonE6 - lastLonE6) < threshold){
						if (restrictedMode && routeRect!=null) zoomMapToFitBulbPOIs();
						else if(lastLocation != null) {
		                    if(mapView.getZoomLevel() == ValidationActivity.DEFAULT_ZOOM_LEVEL){
		                    	if(routeRect != null) {
		                    		zoomMapToFitBulbPOIs();
		                    	}
		                    	else {
		                    	    mapView.setZoom(calculateZoomLevel(lastLocation.getLatitude()));
		                    	}
		                    }else{
		                        mapView.setZoom(ValidationActivity.DEFAULT_ZOOM_LEVEL);
		                        if(myPoint != null){
		                            mapView.centerMapOnPosition(new SKCoordinate(myPoint.getLongitude(), myPoint.getLatitude()));
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
        
        Display display = getWindowManager().getDefaultDisplay();
        DrawerLayout.LayoutParams leftDrawerLp = (DrawerLayout.LayoutParams) findViewById(R.id.left_drawer).getLayoutParams();
        leftDrawerLp.width=display.getWidth()*3/4 + Dimension.dpToPx(10, getResources().getDisplayMetrics());
        View menuPanel = findViewById(R.id.menu_panel);
        LayoutParams menuPanelLp = menuPanel.getLayoutParams();
        menuPanelLp.width=display.getWidth()*3/4;
        
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        View drawerIconPanel = findViewById(R.id.drawer_menu_icon_panel);
        drawerIconPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenu(mDrawerLayout);
            }
        });
        
        /*
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
		*/
        
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
						mDrawerLayout.closeDrawer(findViewById(R.id.left_drawer));
						closeIfEmpty.set(false);
						findViewById(R.id.reservations_list_view).setVisibility(View.VISIBLE);
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
        
        TextView favoriteListMenu = (TextView) findViewById(R.id.favorite_list);
        favoriteListMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
            	clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Intent favListIntent = new Intent(LandingActivity2.this, FavoriteListActivity.class);
						favListIntent.putParcelableArrayListExtra(FavoriteListActivity.FAVORITE_LIST, favoriteAddresses);
						startActivity(favListIntent);
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
        
        User.initializeIfNeccessary(this, new Runnable() {
            @Override
            public void run() {
            	User user = User.getCurrentUser(LandingActivity2.this);
                userInfoView.setText(user.getFirstname() + " " + user.getLastname());
            }
        });
        
        final TextView inboxNotification = (TextView) findViewById(R.id.inbox_notification);
        TextView inBoxMenu = (TextView) findViewById(R.id.message_inbox);
        inBoxMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if(StringUtils.isNotBlank(inboxCityName)) {
					v.setClickable(false);
					ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
					clickAnimation.startAnimation(new ClickAnimationEndCallback() {
						@Override
						public void onAnimationEnd() {
							DebugOptionsActivity.setInboxLastVisitFeedTime(LandingActivity2.this, inboxCityName, realLastFeed);
							findViewById(R.id.menu_notification).setVisibility(View.GONE);
							inboxNotification.setVisibility(View.GONE);
							Intent inboxIntent = new Intent(LandingActivity2.this, InBoxActivity.class);
							inboxIntent.putExtra(InBoxActivity.CITY_NAME, inboxCityName);
							startActivity(inboxIntent);
							v.setClickable(true);
						}
					});
				}
			}
        });
        
//        final View activityRootView = findViewById(android.R.id.content);
//        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                mapView.postInvalidate();
//             }
//        });
        
        findViewById(R.id.left_drawer).setOnClickListener(noopClick);
        findViewById(R.id.loading_panel).setOnClickListener(noopClick);
        
        getRouteView = (TextView) findViewById(R.id.get_route);
        getRouteView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!serviceArea.get()) {
					if(notifyOutOfService.getAndSet(false)) {
						CharSequence msg = Html.fromHtml(outOfServiceHtml);
                        NotificationDialog2 dialog = new NotificationDialog2(LandingActivity2.this, msg);
                        dialog.setTitle("Notification");
                        dialog.setPositiveActionListener(new ActionListener() {
							@Override
							public void onClick() {
								setGetRouteButtonState(true);
							}
                        });
                        try{
                            dialog.show();
                        }catch(Throwable t){}
					}
				}
				else {
					ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
					clickAnimation.startAnimation(new ClickAnimationEndCallback() {
						@Override
						public void onAnimationEnd() {
							//disableShowPassengerMode.set(true);
							if(curFrom != null && StringUtils.isBlank(curFrom.address)) {
								
								ResultCallback<LocationSettingsResult> callback = new ResultCallback<LocationSettingsResult>() {
									public void onResult(LocationSettingsResult result) {
										try {
											if (result.getStatus().getStatusCode()==LocationSettingsStatusCodes.RESOLUTION_REQUIRED)
												result.getStatus().startResolutionForResult(LandingActivity2.this, REQUEST_CHECK_SETTINGS);
										} catch (SendIntentException e) {}
									}
								};
								if (googleApiClient!=null) {
									PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);
									result.setResultCallback(callback);
								}
								
								Misc.parallelExecute(new AsyncTask<Void, Void, Void>() {
									
									@Override
									protected void onPreExecute() {
										findViewById(R.id.loading_panel).setVisibility(View.VISIBLE);
									}
									
									@Override
									protected Void doInBackground(Void... params) {
										try {
											while(!getLocationRefreshStatus() && !cancelGetRoute.get()) {
												Thread.sleep(1000);
											}
										}
										catch(Exception ignore) {}
										return null;
									}
									
									@Override
			        				protected void onPostExecute(Void result) {
										findViewById(R.id.loading_panel).setVisibility(View.GONE);
										if(!cancelGetRoute.getAndSet(false)) {
											startRouteActivity();
										}
										else {
											//disableShowPassengerMode.set(false);
										}
									}
								});
							}
							else {
								startRouteActivity();
							}
						}
					});
				}
			}
        });
        passengerIcon = findViewById(R.id.passenger_mode_icon);
        
        toggleGetRouteButton(false);
        
//        DebugOptionsActivity.cleanMapTileCacheIfNessary(LandingActivity2.this);
        
        scheduleNextTripInfoUpdates();
        scheduleInboxNotifier();
        
        upointView = (TextView) findViewById(R.id.upoint);
        upointView.setText(formatMyMetropiaInfo("000Pts"));
        saveTimeView = (TextView)findViewById(R.id.save_time);
        saveTimeView.setText(formatMyMetropiaInfo("00Min"));
        co2View = (TextView) findViewById(R.id.co2);
        co2View.setText(formatMyMetropiaInfo("000lbs"));
        
        findViewById(R.id.upoint_panel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						if(WebMyMetropiaActivity.hasMyMetropiaUrl(LandingActivity2.this)){
				           Intent intent = new Intent(LandingActivity2.this, WebMyMetropiaActivity.class);
				           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				           intent.putExtra(WebMyMetropiaActivity.WHICH_PAGE, WebMyMetropiaActivity.MY_METROPIA_PAGE);
				           startActivity(intent);
				        }else{
				           Intent intent = new Intent(LandingActivity2.this, MyMetropiaActivity.class);
				           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				           startActivity(intent);
				        }
					}
				});
			}
        });
        
        findViewById(R.id.save_time_panel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						if(WebMyMetropiaActivity.hasTimeSavingUrl(LandingActivity2.this) || WebMyMetropiaActivity.hasMyMetropiaUrl(LandingActivity2.this)){
				           Intent intent = new Intent(LandingActivity2.this, WebMyMetropiaActivity.class);
				           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				           Integer pageNo = WebMyMetropiaActivity.hasTimeSavingUrl(LandingActivity2.this) ? WebMyMetropiaActivity.TIME_SAVING_PAGE : WebMyMetropiaActivity.MY_METROPIA_PAGE;
				           intent.putExtra(WebMyMetropiaActivity.WHICH_PAGE, pageNo);
				           startActivity(intent);
				        }
						else{
				           Intent intent = new Intent(LandingActivity2.this, MyMetropiaActivity.class);
				           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				           startActivity(intent);
				        }
					}
				});
			}
        });
        
        findViewById(R.id.co2_panel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						if(WebMyMetropiaActivity.hasCo2SavingUrl(LandingActivity2.this) || WebMyMetropiaActivity.hasMyMetropiaUrl(LandingActivity2.this)){
				           Intent intent = new Intent(LandingActivity2.this, WebMyMetropiaActivity.class);
				           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				           Integer pageNo = WebMyMetropiaActivity.hasCo2SavingUrl(LandingActivity2.this) ? WebMyMetropiaActivity.CO2_SAVING_PAGE : WebMyMetropiaActivity.MY_METROPIA_PAGE; 
				           intent.putExtra(WebMyMetropiaActivity.WHICH_PAGE, pageNo);
				           startActivity(intent);
				        }else{
				           Intent intent = new Intent(LandingActivity2.this, MyMetropiaActivity.class);
				           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				           startActivity(intent);
				        }
					}
				});
			}
        });
        
        addressInfo = (TextView) findViewById(R.id.address_info);
        poiIcon = (ImageView) findViewById(R.id.poi_icon);
        
        editMenu = (ImageView) findViewById(R.id.edit_menu);
        editMenu.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(final View v) {
        		if(!LOADING_ADDRESS.equals(addressInfo.getText())) {
		        	v.setClickable(false);
		        	final Integer[] resourceIds = (Integer[]) editMenu.getTag();
		        	editMenu.setImageResource(resourceIds[0]);
		        	ClickAnimation clickAni = new ClickAnimation(LandingActivity2.this, v);
		//        	clickAni.setAnimationId(R.anim.menu_click_animation);
		        	clickAni.startAnimation(new ClickAnimationEndCallback() {
						@Override
						public void onAnimationEnd() {
							editMenu.setImageResource(resourceIds[1]);
							PoiOverlayInfo info = (PoiOverlayInfo) popupPanel.getTag();
							showFavoriteOptPanel(info);
							hidePopupMenu();
							v.setClickable(true);
						}
		        	});
        		}
        	}
        });
        
        toMenu = (ImageView) findViewById(R.id.to_menu);
        toMenu.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(final View v) {
        		if(!LOADING_ADDRESS.equals(addressInfo.getText())) {
					v.setClickable(false);
					toMenu.setImageResource(R.drawable.selected_to_menu);
					ClickAnimation clickAni = new ClickAnimation(LandingActivity2.this, v);
		//			clickAni.setAnimationId(R.anim.menu_click_animation);
					clickAni.startAnimation(new ClickAnimationEndCallback() {
						@Override
						public void onAnimationEnd() {
							toMenu.setImageResource(R.drawable.to_menu);
							setMenuInfo2Searchbox((PoiOverlayInfo)popupPanel.getTag(), false);
							hidePopupMenu();
							v.setClickable(true);
						}
					});
        		}
			}
        });
        
        fromMenu = (ImageView) findViewById(R.id.from_menu);
        fromMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if(!LOADING_ADDRESS.equals(addressInfo.getText())) {
					v.setClickable(false);
					fromMenu.setImageResource(R.drawable.selected_from_menu);
					ClickAnimation clickAni = new ClickAnimation(LandingActivity2.this, v);
		//			clickAni.setAnimationId(R.anim.menu_click_animation);
					clickAni.startAnimation(new ClickAnimationEndCallback() {
						@Override
						public void onAnimationEnd() {
							fromMenu.setImageResource(R.drawable.from_menu);
							setMenuInfo2Searchbox((PoiOverlayInfo)popupPanel.getTag(), true);
							hidePopupMenu();
							v.setClickable(true);
						}
					});
				}
			}
        });
        
        fromIcon = (ImageView) findViewById(R.id.from_icon);
        toIcon = (ImageView) findViewById(R.id.to_icon);
        
        popupPanel = (FrameLayout)findViewById(R.id.popup_panel);
        /*
        popupPanel.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
					mapView.dispatchTouchEvent(event);
				}
				else if(event.getActionMasked() == MotionEvent.ACTION_MOVE) {
					for(int i = 0 ; i < popupPanel.getChildCount() && selectedMenu == null ; i++) {
						View icon = popupPanel.getChildAt(i);
						Rect iconRect = new Rect(icon.getLeft(), icon.getTop(), icon.getRight(), icon.getBottom());
						if(icon.getVisibility() == View.VISIBLE && iconRect.contains((int)event.getX(), (int)event.getY())) {
							selectedMenu = icon;
						}
					}
					fromMenu.setImageResource(R.drawable.home);
					if(selectedMenu != null && selectedMenu instanceof ImageView) {
						((ImageView)selectedMenu).setImageResource(R.drawable.work);
					}
					mapView.dispatchTouchEvent(event);
				}
				else if(event.getActionMasked() == MotionEvent.ACTION_UP) {
					fromMenu.setImageResource(R.drawable.home);
					popupPanel.setBackgroundColor(android.R.color.transparent);
					fromMenu.setVisibility(View.INVISIBLE);
					mapView.dispatchTouchEvent(event);
				}
				return true;
			}
    	});
        */
        
        passengerIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						SharedPreferences prefs = Preferences.getGlobalPreferences(LandingActivity2.this);
			            int duoTutorialFinish = prefs.getInt(Preferences.Global.DUO_TUTORIAL_FINISH, 0);
			            
						if (duoTutorialFinish==1) {
			                Intent intent = new Intent(LandingActivity2.this, PassengerActivity.class);
							startActivity(intent);
						}
						else new BlurDialog(LandingActivity2.this).show();
						v.setClickable(true);
					}
				});
			}
        });
        
        AssetManager assets = getAssets();
        Font.setTypeface(Font.getLight(assets), searchBox, fromSearchBox, myMetropiaMenu, 
            reservationsMenu, shareMenu, feedbackMenu, rewardsMenu, settingsMenu, userInfoView, myTripsMenu,
            favoriteListMenu, inBoxMenu, inboxNotification, (TextView) findViewById(R.id.menu_notification));
        Font.setTypeface(Font.getMedium(assets), upointView, saveTimeView, co2View, (TextView) findViewById(R.id.head));
        Font.setTypeface(Font.getRobotoBold(assets), getRouteView);
        //init Tracker
        ((SmarTrekApplication)getApplication()).getTracker(TrackerName.APP_TRACKER);
        boolean showTutorial = showTutorialIfNessary();
        
        if(!showTutorial) {
        	preparingDialog = new ProgressDialog(LandingActivity2.this, R.style.PopUpDialog);
        	preparingDialog.setTitle("Metropia");
        	preparingDialog.setMessage("Preparing...");
        	preparingDialog.setCanceledOnTouchOutside(false);
        	preparingDialog.setCancelable(false);
        	preparingDialog.show();
        }
    
        if(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LandingActivity2.this) == ConnectionResult.SUCCESS) {
	        requestingLocationUpdates = true;
	        createGoogleApiClient();
	        createLocationRequest();
	        buildLocationSettingsRequest();
        }
        
    }
    
    private ProgressDialog preparingDialog;
    
    public static final long ONE_HOUR = 60 * 60 * 1000L;
    
    private synchronized void locationChanged(Location location) {
    	lastLocation = location;
        final boolean refresh = mapRefresh.getAndSet(false);
        final boolean alertAvailability = mapAlertAvailability.getAndSet(false);
        final boolean rezoom = mapRezoom.getAndSet(false);
        final double lat = location.getLatitude();
        final double lon = location.getLongitude();
        refreshMyLocation(location);
        popupResumeNavigationIfNeccessary();
        if(mapRecenter.getAndSet(false)){
            if(myPoint != null){
                SKCoordinate loc = new SKCoordinate(myPoint.getLongitude(), myPoint.getLatitude());
                int latE6 = (int) (loc.getLatitude() * 1E6);
                int lonE6 = (int) (loc.getLongitude() * 1E6);
                mapView.centerMapOnPosition(loc);
                mapCenterLat.set(latE6);
                mapCenterLon.set(lonE6);
            }
        }
        
        if(preparingDialog != null && preparingDialog.isShowing()) {
        	Misc.doQuietly(new Runnable() {
				@Override
				public void run() {
					preparingDialog.dismiss();
					preparingDialog = null;
				}
        	});
        }
        
        User.initializeIfNeccessary(LandingActivity2.this, new Runnable() {
            @Override
            public void run() {
                if(refresh){
                	SkobblerUtils.initSunriseSunsetTime(LandingActivity2.this, lat, lon);
                	MainActivity.initApiLinksIfNecessary(LandingActivity2.this, new Runnable() {
						@Override
						public void run() {
							refreshCobranding(lat, lon, alertAvailability, new Runnable() {
                                public void run() {
                                	refreshInputAddresses();
                                    refreshBulbPOIs(lat , lon, rezoom);
                                    if(!canDrawReservRoute.getAndSet(true)) {
                                    	refreshTripsInfo();
                                    }
                                }
                            });
						}
                	});
                }
            }
        });
    }
    
    private static final String LOCATION_REFRESH_STATUS = "locationRefreshStatus";
    private static final String LANDING_PAGE = "landingPage";
    
    private void setLocationRefreshStatus(boolean status) {
    	if(getLocationRefreshStatus() != status) {
	    	SharedPreferences.Editor editor = getSharedPreferences(LANDING_PAGE, MODE_PRIVATE).edit();
	        editor.putBoolean(LOCATION_REFRESH_STATUS, status);
	        editor.commit();
    	}
    }
    
    private boolean getLocationRefreshStatus() {
    	SharedPreferences perf = getSharedPreferences(LANDING_PAGE, MODE_PRIVATE);
        return perf.getBoolean(LOCATION_REFRESH_STATUS, false);
    }
    
    private void initSKMaps(SKMapViewHolder holder) {
		mapView = holder.getMapSurfaceView();
		mapView.clearAllOverlays();
		mapView.deleteAllAnnotationsAndCustomPOIs();
		mapView.rotateTheMapToNorth();
		mapView.getMapSettings().setCurrentPositionShown(true);
		mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.NONE);
		mapView.getMapSettings().setMapDisplayMode(SKMapDisplayMode.MODE_2D);
		mapView.getMapSettings().setMapRotationEnabled(false);
        mapView.getMapSettings().setMapZoomingEnabled(true);
        mapView.getMapSettings().setMapPanningEnabled(true);
        mapView.getMapSettings().setZoomWithAnchorEnabled(true);
        mapView.getMapSettings().setInertiaRotatingEnabled(false);
        mapView.getMapSettings().setInertiaZoomingEnabled(true);
        mapView.getMapSettings().setInertiaPanningEnabled(true);
        mapView.getMapSettings().setMapStyle(SkobblerUtils.getMapViewStyle(LandingActivity2.this, true));
	}
    
    private void handleCalendarNotification(int eventId) {
    	if(eventId > 0) {
    		JSONObject event = CalendarService.getEvent(LandingActivity2.this, eventId);
    		String location = event.optString(Instances.EVENT_LOCATION, "");
    		searchBox.setText(location);
    		searchBox.requestFocus();
    	}
    }
    
    private void setMenuInfo2Searchbox(PoiOverlayInfo info, boolean from) {
    	if(info != null) {
            handleOD(info, from);
    	}
    }
    
    private boolean showTutorialIfNessary() {
    	SharedPreferences prefs = Preferences.getGlobalPreferences(this);
    	int tutorialFinish = prefs.getInt(Preferences.Global.TUTORIAL_FINISH, 0);
    	// hide tutorial page
    	if(false && tutorialFinish != TutorialActivity.TUTORIAL_FINISH) {
    		Intent intent = new Intent(this, TutorialActivity.class);
    		intent.putExtra(TutorialActivity.FROM_LANDING_PAGE, true);
            startActivity(intent);
            return true;
    	}
    	return false;
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
    
//    private Integer EMPTY_ITEM_SIZE = 5;
    
//    private List<Address> getEmptyAddressesForUI() {
//    	List<Address> emptyAddresses = new ArrayList<Address>();
//    	for(int i = 0 ; i < EMPTY_ITEM_SIZE ; i++) {
//    		Address empty = new Address();
//    		empty.setAddress("");
//    		empty.setDistance(-1);
//    		empty.setName("");
//    		emptyAddresses.add(empty);
//    	}
//    	return emptyAddresses;
//    }
    
    private Long dismissReservId = Long.valueOf(-1);
    private Boolean swipeRight = Boolean.FALSE;
    
    private LinearLayout reservationListPanel;
    
    private void initReservationListView() {
    	reservationListPanel = (LinearLayout) findViewById(R.id.reservation_list);
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
    	
    	final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    	findViewById(R.id.reservation_list_menu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						openMenu(mDrawerLayout);
					}
				});
			}
    	});
    	
    	findViewById(R.id.reservation_head_add).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						dismissReservId = getFirstReservation() != null ? getFirstReservation().getRid() : -1;
						closeIfEmpty.set(true);
						hideTripInfoPanel();
						centerMap();
					}
				});
			}
    	});
    	
    	findViewById(R.id.add_new_reservation_panel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						dismissReservId = getFirstReservation() != null ? getFirstReservation().getRid() : -1;
						closeIfEmpty.set(true);
						hideTripInfoPanel();
						centerMap();
					}
				});
			}
    	});
    	
    	newUserTipView = findViewById(R.id.new_user_tip);
    	newUserTipView.setVisibility(DebugOptionsActivity.isUserCloseTip(LandingActivity2.this)?View.GONE:View.VISIBLE);
    	
    	ImageView tipCloseView = (ImageView) findViewById(R.id.tip_close);
    	if(!DebugOptionsActivity.isUserCloseTip(LandingActivity2.this)) {
    		tipCloseView.setImageBitmap(Misc.getBitmap(LandingActivity2.this, R.drawable.tip_close, 1));
    	}
    	
    	tipCloseView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newUserTipView.setVisibility(View.GONE);
				DebugOptionsActivity.userCloseTip(LandingActivity2.this);
				int reservCount = 0;
				for(int i = 0 ; i < reservationListPanel.getChildCount() ; i++) {
					View child = reservationListPanel.getChildAt(i);
					if(child.getTag() != null && child.getTag() instanceof Reservation) {
						reservCount++;
					}
				}
				
				if(reservCount < 2) {
					findViewById(R.id.add_new_trip_background).setVisibility(View.VISIBLE);
				}
			}
    	});
    	
    	SwipeDeleteTouchListener touchListener =
                new SwipeDeleteTouchListener(reservationListPanel, 
                        new SwipeDeleteTouchListener.OnDismissCallback() {
                            @Override
                            public void onDismiss(View tripInfoView, final List<Reservation> reservs) {
                                final Reservation reserv = (Reservation) tripInfoView.getTag();
                                final Long removeReservId = reserv.getRid();
                                AsyncTask<Void, Void, Boolean> delTask = new AsyncTask<Void, Void, Boolean>(){
                                	@Override
                                	protected void onPreExecute() {
                                		removedReservIds.add(removeReservId);
                                		closeIfEmpty.set(true);
                                		refreshReservationList(reservs);
                                	}
                                	
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
            		                   		refreshTripsInfo(false, false);
            		                   		removedReservIds.remove(removeReservId);
            		                   	}
            		                }
            		            };
            		            Misc.parallelExecute(delTask);
                            }

							@Override
							public void onDismissRight() {}

                         });
    	reservationListPanel.setOnTouchListener(touchListener);
//        Font.setTypeface(boldFont, (TextView) findViewById(R.id.no_reserved_trips));
    }
    
    private void centerMap() {
     	if(routeRect == null && myPoint != null) {
    		mapView.setZoom(calculateZoomLevel(myPoint.getLatitude()));
    		mapView.centerMapOnPosition(new SKCoordinate(myPoint.getLongitude(), myPoint.getLatitude()));
//    		mapView.postInvalidate();
    	}
    	else {
    		// simulate default view
//    		RouteActivity.setViewToNorthAmerica(mapView);
    		zoomMapToFitBulbPOIs();
    	}
    }
    
    private View createReservationInfoView(final Reservation reserv, boolean isFirst) {
    	View reservInfo = getLayoutInflater().inflate(R.layout.reservation_trip_info, reservationListPanel, false);
    	
    	String arrivalTime = StringUtils.replace(formatTime(reserv.getArrivalTimeUtc(), reserv.getRoute().getTimezoneOffset(), false), " ", "");
    	String durationTimeDesc = "";
    	String arrivalTimeDesc = "";
    	int backgroundColor = R.color.metropia_orange;
    	int startTimeVisible = View.VISIBLE;
    	int durationTimeVisible = View.VISIBLE;
    	int startButtonResourceId = R.drawable.reservation_start_trip_transparent;
    	long departureTimeUtc = reserv.getDepartureTimeUtc();
    	String nextTripStartTime = "";
        long timeUntilDepart = departureTimeUtc - System.currentTimeMillis();
        long durationTime = reserv.getDuration();  //sec
        String originDesc = StringUtils.isNotBlank(reserv.getOriginName()) ? reserv.getOriginName() : reserv.getOriginAddress();
        String destDesc = StringUtils.isNotBlank(reserv.getDestinationName()) ? reserv.getDestinationName() : reserv.getDestinationAddress();
        final boolean lessThanOneMinite = timeUntilDepart < 1 * 60 * 1000;
        if(reserv.isEligibleTrip()){
        	startTimeVisible = lessThanOneMinite && isFirst ? View.GONE : View.VISIBLE;
        	durationTimeVisible = lessThanOneMinite ? View.VISIBLE : View.GONE;
            backgroundColor = isFirst ? (lessThanOneMinite ? R.color.metropia_green : R.color.metropia_orange) : R.color.metropia_blue;
            if(isFirst && !lessThanOneMinite) {
            	int countDownMins = Double.valueOf(Math.ceil(Double.valueOf(timeUntilDepart)/Double.valueOf(60*1000))).intValue();
            	nextTripStartTime = String.format("%d\nMINS", countDownMins);
            }
            else {
            	nextTripStartTime = StringUtils.replace(formatTime(departureTimeUtc, reserv.getRoute().getTimezoneOffset(), !isFirst), " ", "\n");
            }
            arrivalTime = lessThanOneMinite ? formatTime(System.currentTimeMillis() + durationTime*1000, reserv.getRoute().getTimezoneOffset(), false) : arrivalTime;
            arrivalTimeDesc = "Arrival: " + arrivalTime;
            durationTimeDesc = "Duration: " + getFormattedDuration(Long.valueOf(durationTime).intValue());
            startButtonResourceId = isFirst ? (lessThanOneMinite ? R.drawable.reservation_start_trip : R.drawable.reservation_start_trip_transparent) : R.drawable.reservation_start_trip_disable;
        }else {
        	startTimeVisible = View.VISIBLE;
        	durationTimeVisible = View.GONE;
            nextTripStartTime = StringUtils.replace(formatTime(departureTimeUtc, reserv.getRoute().getTimezoneOffset(), !isFirst), " ", "\n");
            backgroundColor = isFirst ? R.color.metropia_orange : R.color.metropia_blue;
            arrivalTimeDesc = "Arrival: " + arrivalTime;
            durationTimeDesc = "Duration: " + getFormattedDuration(Long.valueOf(durationTime).intValue());
            startButtonResourceId = isFirst ? R.drawable.reservation_start_trip_transparent : R.drawable.reservation_start_trip_disable;
        }
        
        reservInfo.findViewById(R.id.trip_info_desc).setVisibility(isFirst?View.VISIBLE:View.GONE);
        
        TextView tripInfoFromAddressView =  (TextView) reservInfo.findViewById(R.id.trip_info_from_address);
    	tripInfoFromAddressView.setText(originDesc);
    	TextView tripInfoToAddressView =  (TextView) reservInfo.findViewById(R.id.trip_info_to_address);
    	tripInfoToAddressView.setText(destDesc);
    	TextView timeToGo = (TextView) reservInfo.findViewById(R.id.time_to_go_desc);
    	timeToGo.setText(startTimeVisible == View.GONE ? "It's Time to Go!" : (reserv.isEligibleTrip() ? "WILL START IN:" : ""));
    	timeToGo.setVisibility(isFirst?View.VISIBLE:View.GONE);
        reservInfo.setBackgroundColor(getResources().getColor(backgroundColor));
        reservInfo.setTag(reserv);
        TextView tripDurationTimeView = (TextView) reservInfo.findViewById(R.id.reservation_duration_time);
        tripDurationTimeView.setVisibility(durationTimeVisible);
        tripDurationTimeView.setText(formatTripTime(durationTimeDesc));
        TextView tripArrivalTimeView = (TextView) reservInfo.findViewById(R.id.reservation_arrive_time);
        tripArrivalTimeView.setVisibility(View.VISIBLE);
        tripArrivalTimeView.setText(formatTripTime(arrivalTimeDesc));
        TextView tripStartTimeView = (TextView) reservInfo.findViewById(R.id.reservation_start_time);
        tripStartTimeView.setVisibility(startTimeVisible);
        if(isFirst && reserv.isEligibleTrip()) {
        	tripStartTimeView.setText(formatCountDownTime(nextTripStartTime));
        	tripStartTimeView.setPadding(0, Dimension.dpToPx(8, getResources().getDisplayMetrics()), 0, 0);
        }
        else {
        	tripStartTimeView.setText(formatStartTripTime(nextTripStartTime));
        	tripStartTimeView.setPadding(0, 0, 0, 0);
        }
        ImageView startButton = (ImageView) reservInfo.findViewById(R.id.reservation_start_button);
        startButton.setImageBitmap(Misc.getBitmap(LandingActivity2.this, startButtonResourceId, 1));
//        startButton.setImageResource(startButtonResourceId);
        reservInfo.findViewById(R.id.reservation_trip_times).setVisibility(isFirst?View.VISIBLE:View.GONE);
//        reservInfo.findViewById(R.id.leave_label).setVisibility((isFirst && !reserv.isEligibleTrip())?View.VISIBLE:View.GONE);
        reservInfo.findViewById(R.id.center_line).setVisibility(isFirst? View.GONE : View.VISIBLE);
        
        if(isFirst) {
        	startButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
					clickAnimation.startAnimation(new ClickAnimationEndCallback() {
						@Override
						public void onAnimationEnd() {
							if(reserv.isEligibleTrip()) {
								startValidationActivity(reserv);
							}
							else {
								NotificationDialog2 dialog = new NotificationDialog2(LandingActivity2.this, "Would you like to start your trip early?");
								dialog.setVerticalOrientation(false);
								dialog.setTitle("");
								dialog.setPositiveButtonText("Yes");
								dialog.setPositiveActionListener(new ActionListener() {
									@Override
									public void onClick() {
									    GeoPoint origin = null;
									    final ReservationTollHovInfo reservationInfo = MapDisplayActivity.getReservationTollHovInfo(LandingActivity2.this, reserv.getRid());
									    if(myPoint != null){
									        origin = new GeoPoint(myPoint.getLatitude(), myPoint.getLongitude());
									    }
										RescheduleTripTask rescheduleTask = new RescheduleTripTask(LandingActivity2.this, 
										        origin, null, reserv.getDestinationAddress(), 
								        		reserv.getRid(), versionNumber, ehs, reservationInfo);
										rescheduleTask.callback = new RescheduleTripTask.Callback() {
				                            @Override
				                            public void run(Reservation reservation) {
				                            	Log.d("LandingActivity2", "Reschedule trip start");
				                            	reservationInfo.setReservationId(reservation.getRid());
				                            	MapDisplayActivity.addReservationTollHovInfo(LandingActivity2.this, reservationInfo);
				                            	startValidationActivity(reservation);
				                            }
				                        };
				                        Misc.parallelExecute(rescheduleTask);
									}
								});
								dialog.setNegativeButtonText("No");
								dialog.setNegativeActionListener(new ActionListener() {
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
        }
        
        TextView reservationOnMyWay = (TextView) reservInfo.findViewById(R.id.reservation_on_my_way);
        reservationOnMyWay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						v.setClickable(true);
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
        
        View reservReschedule = reservInfo.findViewById(R.id.reschedule_panel);
        reservReschedule.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				mapView.deleteAllAnnotationsAndCustomPOIs();
				mapView.clearAllOverlays();
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						try {
			                Intent intent = new Intent(LandingActivity2.this, RouteActivity.class);
			                Bundle extras = new Bundle();
			                extras.putLong(RouteActivity.RESCHEDULE_RESERVATION_ID, reserv.getRid());
			                extras.putString("originAddr", reserv.getOriginAddress());
			                extras.putParcelable(RouteActivity.ORIGIN_COORD, reserv.getStartGpFromNavLink());
			                extras.putString(RouteActivity.ORIGIN_COORD_PROVIDER, null);
			                extras.putLong(RouteActivity.ORIGIN_COORD_TIME, 0);
			                extras.putParcelable(RouteActivity.ORIGIN_OVERLAY_INFO, poiContainer.getExistedPOIByLocation(reserv.getStartGpFromNavLink().getLatitude(), reserv.getStartGpFromNavLink().getLongitude()));
			                extras.putString("destAddr", reserv.getDestinationAddress());
			                extras.putParcelable(RouteActivity.DEST_COORD, reserv.getEndGpFromNavLink());
			                extras.putLong(RouteActivity.RESCHEDULE_DEPARTURE_TIME, reserv.getDepartureTimeUtc());
			                extras.putParcelable(RouteActivity.DEST_OVERLAY_INFO, poiContainer.getExistedPOIByLocation(reserv.getEndGpFromNavLink().getLatitude(), reserv.getEndGpFromNavLink().getLongitude()));
			                intent.putExtras(extras);
//			                hideBulbBalloon();
//			                hideStarredBalloon();
			                removeAllOD();
			                startActivity(intent);
						}
						catch(Exception e) {
							ehs.reportException(e);
						}
						v.setClickable(true);
					}
				});
			}
        });
        
        reservInfo.findViewById(R.id.trip_od_detail).setVisibility(isFirst?View.GONE:View.VISIBLE);
        reservInfo.findViewById(R.id.reservation_on_my_way).setVisibility(isFirst?View.VISIBLE:View.GONE);
        
        TextView fromAddressView = (TextView) reservInfo.findViewById(R.id.od_from_address);
        fromAddressView.setText(originDesc);
        TextView toAddressView = (TextView) reservInfo.findViewById(R.id.od_to_address);
        toAddressView.setText(destDesc);
        
        Font.setTypeface(robotoBoldFont, timeToGo, tripDurationTimeView, tripArrivalTimeView, tripStartTimeView, tripInfoFromAddressView, tripInfoToAddressView);
        Font.setTypeface(robotoLightFont, reservationOnMyWay, (TextView) reservInfo.findViewById(R.id.reschedule_desc));
        
        if(isFirst) {
	        tripNotifyIcon.setImageResource(lessThanOneMinite?R.drawable.upcoming_trip_green:R.drawable.upcoming_trip_orange);
	        tripNotifyIcon.setVisibility(View.VISIBLE);
	        passengerIcon.setVisibility(View.GONE);
        }
        
        return reservInfo;
    }
    
    private View createEmptyReservationInfoView() {
    	FrameLayout emptyReservInfo = (FrameLayout) getLayoutInflater().inflate(R.layout.reservation_trip_info, reservationListPanel, false);
    	int startButtonResourceId = R.drawable.reservation_start_trip_transparent;
        emptyReservInfo.findViewById(R.id.trip_info_desc).setVisibility(View.GONE);
    	TextView timeToGo = (TextView) emptyReservInfo.findViewById(R.id.time_to_go_desc);
    	timeToGo.setVisibility(View.GONE);
        TextView tripDurationTimeView = (TextView) emptyReservInfo.findViewById(R.id.reservation_duration_time);
        tripDurationTimeView.setVisibility(View.GONE);
        TextView tripArrivalTimeView = (TextView) emptyReservInfo.findViewById(R.id.reservation_arrive_time);
        tripArrivalTimeView.setVisibility(View.INVISIBLE);
        TextView tripStartTimeView = (TextView) emptyReservInfo.findViewById(R.id.reservation_start_time);
        tripStartTimeView.setVisibility(View.INVISIBLE);
        ImageView startButton = (ImageView) emptyReservInfo.findViewById(R.id.reservation_start_button);
        startButton.setImageBitmap(Misc.getBitmap(LandingActivity2.this, startButtonResourceId, 1));
//        startButton.setImageResource(startButtonResourceId);
        startButton.setVisibility(View.INVISIBLE);
        emptyReservInfo.findViewById(R.id.reservation_trip_times).setVisibility(View.GONE);
//        emptyReservInfo.findViewById(R.id.leave_label).setVisibility(View.GONE);
        emptyReservInfo.findViewById(R.id.center_line).setVisibility(View.INVISIBLE);
        emptyReservInfo.findViewById(R.id.reservation_on_my_way).setVisibility(View.INVISIBLE);
        emptyReservInfo.findViewById(R.id.reschedule_panel).setVisibility(View.INVISIBLE);
        emptyReservInfo.findViewById(R.id.center_line).setVisibility(View.VISIBLE);;
        return emptyReservInfo;
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
    
    public static ArrayAdapter<Address> createAutoCompleteAdapter(final Context ctx, final EditText searchBox) {
    	return new ArrayAdapter<Address>(ctx, R.layout.dropdown_select, R.id.name) {
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
                ImageView favIcon = (ImageView) view.findViewById(R.id.fav_icon);
                if(item.getDistance() >= 0) {
                	distance.setVisibility(View.VISIBLE);
                	distance.setText("> " + item.getDistance() + "mi");
                }
                else {
                	distance.setVisibility(View.GONE);
                }
                
                FavoriteIcon icon = FavoriteIcon.fromName(item.getIconName(), null);
                if(icon == null) {
                	favIcon.setImageBitmap(Misc.getBitmap(ctx, R.drawable.poi_pin, 1));
                	favIcon.setVisibility(View.VISIBLE);
                }
                else {
                	favIcon.setImageBitmap(Misc.getBitmap(ctx, icon.getFavoritePageResourceId(ctx), 2));
                	favIcon.setVisibility(View.VISIBLE);
                }
                
                
                Font.setTypeface(Font.getBold(ctx.getAssets()), name, distance);
                Font.setTypeface(Font.getLight(ctx.getAssets()), address);
                namePanel.requestLayout();
                name.requestLayout();
                distance.requestLayout();
                address.requestLayout();
                favIcon.requestLayout();
                int leftRightPadding = Dimension.dpToPx(10, ctx.getResources().getDisplayMetrics());
                int topBottomPadding = Dimension.dpToPx(2, ctx.getResources().getDisplayMetrics());
                view.setPadding(leftRightPadding, topBottomPadding, leftRightPadding, position == getCount() - 1 ? 
                		leftRightPadding : topBottomPadding);
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
						if(NO_AUTOCOMPLETE_RESULT.equals(selectedName) && StringUtils.isBlank(selectedAddr) && searchBox != null) {
							return searchBox.getText();
						}
						return selectedAddr;
					}
        			
        		};
        		return filter;
        	}
        };
    }
    
    private void searchFromAddress(String addrStr, boolean zoomIn) {
    	searchPOIAddress(addrStr, zoomIn, lastLocation, true);
    }
    
    private void searchToAddress(String addrStr, boolean zoomIn) {
    	searchPOIAddress(addrStr, zoomIn, lastLocation, false);
    }
    
    private void searchPOIAddress(final String addrStr, final boolean zoomIn, final Location _location, final boolean isFrom){
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
            protected void onPostExecute(final Address addr) {
                if(addr != null){
                	Runnable r = new Runnable() {
						public void run() {dropPinForAddress(addr, zoomIn, isFrom);}
                	};
                	if (mapView!=null) r.run();
                	else mapActionQueue.add(r);
                }
                else {
                	final NotificationDialog2 dialog = new NotificationDialog2(LandingActivity2.this, "No results");
                	dialog.setTitle("");
                	dialog.setPositiveButtonText("OK");
                	Misc.doQuietly(new Runnable() {
						@Override
						public void run() {
							dialog.show();
						}
                	});
                }
            }
        };
        Misc.parallelExecute(task);
    }
    
    private void dropPinForAddress(Address addr, final boolean zoomIn, boolean isFrom) {
    	final GeoPoint gp = addr.getGeoPoint();
        DebugOptionsActivity.addRecentAddress(LandingActivity2.this, addr.getAddress());
        PoiOverlayInfo poiOverlayInfo = poiContainer.getExistedPOIByAddress(addr.getAddress());
        if(poiOverlayInfo != null) {
        	handleOD(poiOverlayInfo, isFrom);
        }
        else {
        	PoiOverlayInfo poiInfo = refreshPOIMarker(gp.getLatitude(), gp.getLongitude(), addr.getAddress(), addr.getName());
        	handleOD(poiInfo, isFrom);
        }
        // record input address
        addInputAddress(addr);
		if(zoomIn) mapView.setZoom(SEARCH_ZOOM_LEVEL);
		mapView.centerMapOnPositionSmooth(new SKCoordinate(gp.getLongitude(), gp.getLatitude()), MAP_ANIMATION_DURATION);
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
    	
    	public String iconName;
    	
    	public int uniqueId;
    	
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
            iconName = in.readString();
            geopoint = new GeoPoint(lat, lon);
            uniqueId = in.readInt();
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
			dest.writeString(iconName);
			dest.writeInt(uniqueId);
		}
    	
    	public static PoiOverlayInfo fromAddress(Context ctx, com.metropia.models.Address address) {
    		PoiOverlayInfo poiInfo = new PoiOverlayInfo();
    		poiInfo.id = address.getId();
    		poiInfo.label = address.getName();
    		poiInfo.address = address.getAddress();
    		poiInfo.lat = address.getLatitude();
    		poiInfo.lon = address.getLongitude();
    		poiInfo.geopoint = new GeoPoint(address.getLatitude(), address.getLongitude());
    		poiInfo.iconName = address.getIconName();
    		FavoriteIcon icon = FavoriteIcon.fromName(address.getIconName(), FavoriteIcon.star);
    		poiInfo.marker = icon.getResourceId(ctx);
    		poiInfo.markerWithShadow = icon.getShadowResourceId(ctx);
    		return poiInfo;
    	}
    	
    	public static PoiOverlayInfo fromLocation(com.metropia.requests.WhereToGoRequest.Location location) {
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
    		poiInfo.marker = R.drawable.poi_pin;
    		poiInfo.markerWithShadow = R.drawable.poi_pin_with_shadow;
    		return poiInfo;
    	}
    	
    	public static PoiOverlayInfo fromCurrentLocation(GeoPoint currentLoc) {
    		PoiOverlayInfo poiInfo = new PoiOverlayInfo();
    		poiInfo.lat = currentLoc.getLatitude();
    		poiInfo.lon = currentLoc.getLongitude();
    		poiInfo.geopoint = new GeoPoint(currentLoc.getLatitude(), currentLoc.getLongitude(), currentLoc.getHeading());
    		poiInfo.marker = R.drawable.landing_page_current_location;
    		poiInfo.markerWithShadow = R.drawable.landing_page_current_location;
    		return poiInfo;
    	}
    	
    	@Override
    	public boolean equals(Object other) {
    		if(other instanceof PoiOverlayInfo) {
    			PoiOverlayInfo that = (PoiOverlayInfo) other;
    			return new EqualsBuilder().append(that.lat + "", this.lat + "").append(that.lon + "", that.lon + "").append(that.marker + "", this.marker + "").isEquals();
    		}
    		return false;
    	}
    	
    	@Override
    	public int hashCode() {
    		return new HashCodeBuilder().append(this.lat + "").append(this.lon + "").append(this.marker + "").toHashCode();
    	}

    }
    
    private void updateDeviceId(){
        SharedPreferences globalPrefs = Preferences.getGlobalPreferences(this);
        final String gcmRegistrationId = globalPrefs.getString(Preferences.Global.GCM_REG_ID, "");
        final User currentUser = User.getCurrentUser(this);
        final String appVersion = StringUtils.defaultString(currentUser.getAppVersion(), "");
        final String currentDeviceId = currentUser.getDeviceId();
        if (StringUtils.isBlank(gcmRegistrationId)) return;
        
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {

                	if (StringUtils.isBlank(currentDeviceId)) {
                		while (lastLocation==null) ;
                		new SaveLocationRequest(currentUser).execute(LandingActivity2.this, lastLocation.getLatitude(), lastLocation.getLongitude());
                	}
                	
                	if(!currentDeviceId.equals(gcmRegistrationId) || !appVersion.equals(versionNumber)){
                        currentUser.setDeviceId(gcmRegistrationId);
                        currentUser.setAppVersion(versionNumber);
                        
                        new UpdateDeviceIdRequest(currentUser).execute(LandingActivity2.this, gcmRegistrationId, versionNumber);
                	}
                	
                }
                catch (Exception e) {}
                return null;
            }
        };
        Misc.parallelExecute(task);
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
            User.initializeIfNeccessary(context, new Runnable() {
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
            User.initializeIfNeccessary(context, new Runnable() {
                @Override
                public void run() {
                    drawedReservId = Long.valueOf(-1);
                    dismissReservId = Long.valueOf(-1);
                    refreshTripsInfo(true, true);
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
    
    public static final String IN_BOX_NOTICE = "IN_BOX_NOTICE";
    
    private void scheduleInboxNotifier(){
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 600000, 
            PendingIntent.getBroadcast(this, 0, new Intent(IN_BOX_NOTICE), PendingIntent.FLAG_UPDATE_CURRENT));
    }
    
    private BroadcastReceiver inboxNotifier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	checkInboxUrlAndUpdateMenu(inboxCityName);
        }
    };
    
    public static final String UPDATE_MY_LOCATION = "UPDATE_MY_LOCATION";
    
    private BroadcastReceiver updateMyLocation = new BroadcastReceiver() {
    	@Override
        public void onReceive(Context context, Intent intent) {
    		final Double lat = intent.getDoubleExtra("lat", 0);
    		final Double lon = intent.getDoubleExtra("lon", 0);
    		if(lat != 0 && lon != 0) {
    			runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Location loc = new Location("");
						loc.setLatitude(lat);
						loc.setLongitude(lon);
						refreshMyLocation(loc);
						centerMap();
					}
    			});
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
    
    private void refreshMyLocation(Location loc) {
        if(myPoint == null){
            myPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude(), loc.getBearing());
        }
        myPoint.updateLocation(loc.getLatitude(), loc.getLongitude(), loc.getBearing());
        mapView.getMapSettings().setCurrentPositionShown(!restrictedMode);
        mapView.reportNewGPSPosition(new SKPosition(loc));
        
       	updateCurrentLocationOrigin(loc);
        
       	final Location _loc = loc;
       	Misc.parallelExecute(new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
		        	PassiveLocationChangedReceiver.processLocation(getApplicationContext(), _loc);
		        }catch(Exception ignore){}
				return null;
			}
       	});
    }
    
    private void updateCurrentLocationOrigin(Location loc) {
    	if(curFrom != null && StringUtils.isBlank(curFrom.address) && !curFrom.geopoint.equals(new GeoPoint(loc.getLatitude(), loc.getLongitude()))) {
	    	curFrom.lat = loc.getLatitude();
	    	curFrom.lon = loc.getLongitude();
	    	curFrom.geopoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
	    	drawODBalloon(curFrom, true);
    	}
    }
    
    private GoogleApiClient googleApiClient;
    private LocationRequest highAccuracyLocationRequest;
    private boolean requestingLocationUpdates = false;
    private LocationSettingsRequest locationSettingsRequest;
    private Integer REQUEST_CHECK_SETTINGS = Integer.valueOf(1111);
    
    private void createGoogleApiClient() {
    	googleApiClient = new GoogleApiClient.Builder(LandingActivity2.this).addApi(LocationServices.API)
    			.addConnectionCallbacks(LandingActivity2.this).addOnConnectionFailedListener(LandingActivity2.this).build();
    }
    
    private void createLocationRequest() {
    	highAccuracyLocationRequest = new LocationRequest();
    	highAccuracyLocationRequest.setInterval(5000);
    	highAccuracyLocationRequest.setFastestInterval(2500);
    	highAccuracyLocationRequest.setSmallestDisplacement(5);
    	highAccuracyLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(highAccuracyLocationRequest).setAlwaysShow(true);
        locationSettingsRequest = builder.build();
    }
    
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);
        result.setResultCallback(LandingActivity2.this);
        
    }
    
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, highAccuracyLocationRequest, locationListener);
    }
    
    private void prepareGPS(){
    	if(googleApiClient != null && requestingLocationUpdates) {
    		checkLocationSettings();
    	}
    	else if(googleApiClient == null){
    		closeGPS();
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean gps_location_provided = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean network_location_provided = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            
            if (gps_location_provided && network_location_provided) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5, systemLocationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, systemLocationListener);
            }
            else if (network_location_provided) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, systemLocationListener);
            }else{
                SystemService.alertNoGPS(this, true);
            }
    	}
    }
    
    private void closeGPS(){
    	if(googleApiClient != null && googleApiClient.isConnected()) {
	    	LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener).setResultCallback(new ResultCallback<Status>() {
	            @Override
	            public void onResult(Status status) {
	                requestingLocationUpdates = true;
	            }
	        });
    	}
    	else if(locationManager != null){
    		locationManager.removeUpdates(systemLocationListener);
    	}
    }
    
    private void showODBalloon() {
    	if(curFrom != null) {
    		drawODBalloon(curFrom, true);
    	}
    	
    	if(curTo != null) {
    		drawODBalloon(curTo, false);
    	}
    }
    
    private View fromToBalloon;
    
    private Bitmap loadBitmapOfFromToBalloon(Context ctx, boolean from) {
		if(fromToBalloon == null) {
			FrameLayout layout = new FrameLayout(ctx);
			ViewGroup.LayoutParams layoutLp = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(layoutLp);
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			fromToBalloon = inflater.inflate(R.layout.from_to_balloon, layout);
		}
        TextView textView = (TextView) fromToBalloon.findViewById(R.id.poi_mini_title);
        textView.setText(from ? "FROM" : "TO");
        Font.setTypeface(Font.getRegular(ctx.getAssets()), textView);
        fromToBalloon.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        fromToBalloon.layout(0, 0, fromToBalloon.getMeasuredWidth(), fromToBalloon.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(fromToBalloon.getMeasuredWidth(), fromToBalloon.getMeasuredHeight(), Bitmap.Config.ARGB_8888);                
        Canvas canvas = new Canvas(bitmap);
        fromToBalloon.draw(canvas);
        return bitmap;
	}
    
    private AtomicBoolean enableDrawRoute = new AtomicBoolean(true);
    
    @Override
    protected void onResume() {
        super.onResume();
        Localytics.openSession();
        Localytics.tagScreen(this.getClass().getSimpleName());
	    Localytics.upload();
	    Localytics.setInAppMessageDisplayActivity(this);
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
	    
	    annSize.set(Dimension.dpToPx(Misc.ANNOTATION_MINIMUM_SIZE_IN_DP, getResources().getDisplayMetrics()));
	    
	    //SKobbler 
	    mapViewHolder.onResume();
//	    mapView.getMapSettings().setMapStyle(SkobblerUtils.getMapViewStyle(LandingActivity2.this, true));
//	    mapView.getMapSettings().setCurrentPositionShown(true);
//	    SKRouteManager.getInstance().clearAllRoutesFromCache();
//	    SKRouteManager.getInstance().clearCurrentRoute();
//	    SKRouteManager.getInstance().clearRouteAlternatives();
	    //
        registerReceiver(tripInfoUpdater, new IntentFilter(TRIP_INFO_UPDATES));
        registerReceiver(onTheWayNotifier, new IntentFilter(ON_THE_WAY_NOTICE));
        registerReceiver(inboxNotifier, new IntentFilter(IN_BOX_NOTICE));
//        prepareGPS();
        
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        
        //disableShowPassengerMode.set(false);
        refreshHead();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        if(googleApiClient != null) {
        	googleApiClient.connect();
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        if(googleApiClient != null) {
        	googleApiClient.disconnect();
        }
    }
    
    @Override
    protected void onPause() {
    	Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
	    unregisterReceiver(tripInfoUpdater);
	    unregisterReceiver(onTheWayNotifier);
	    unregisterReceiver(inboxNotifier);
	    super.onPause();
	    if(mapView != null) {
	    	mapView.clearAllOverlays();
	    }
	    enableDrawRoute.set(false);
	    mapViewHolder.onPause();
	    mSensorManager.unregisterListener(this, accelerometer);
	    mSensorManager.unregisterListener(this, magnetometer);
	    closeGPS();
//	    drawedReservId = Long.valueOf(-1);
//	    dismissReservId = Long.valueOf(-1);
//	    refreshTripsInfo(true, true);
	    hidePopupMenu();
	    //findViewById(R.id.loading_panel).setVisibility(View.GONE);
    } 
    
    private void refreshTripsInfo(){
        refreshTripsInfo(false, true);
    }
    
    private void refreshTripsInfo(final boolean cached, final boolean closeIfEmpty){
        ReservationListTask task = new ReservationListTask(this, cached){
	        @Override
	        protected void onPostExecute(List<Reservation> reservations) {
	            if (reservations == null || reservations.isEmpty()) {
	            	if(mapView != null) {
		                mapView.clearAllOverlays();
		                mapView.deleteAnnotation(ROUTE_DESTINATION_ID);
	            	}
	                tripNotifyIcon.setVisibility(View.GONE);
	                passengerIcon.setVisibility((getRouteView.getVisibility() == View.GONE && !disableShowPassengerMode.get()) ? View.VISIBLE : View.GONE);
	                refreshReservationList(new ArrayList<Reservation>());
	                //unlockMenu();
	            } 
	            else{
                    Reservation reserv = reservations.get(0);
	                drawRoute(reserv);
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
                List<com.metropia.models.Address> addresses = addReq.execute(ctx);
                reservations = resReq.execute(ctx);
                for(Reservation r:reservations){
                    if(r.getOriginName() == null){
                        for (com.metropia.models.Address a : addresses) {
                            if(a.getAddress().equals(r.getOriginAddress())){
                                r.setOriginName(a.getName());
                                break;
                            }
                        }
                    }
                    if(r.getDestinationName() == null){
                        for (com.metropia.models.Address a : addresses) {
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
    
    private List<Long> removedReservIds = new ArrayList<Long>();
    
    private AtomicBoolean closeIfEmpty = new AtomicBoolean(true);
    private View passengerIcon;
    
    private void refreshReservationList(List<Reservation> reservations) {
    	reservationListPanel.removeAllViews();
    	Reservation notifyReserv = null;
    	int curReservIdx = -1;
    	boolean cont = true;
    	while(curReservIdx < (reservations.size()-1) && cont) {
    		curReservIdx++;
    		Reservation tempReserv = reservations.get(curReservIdx);
    		if(!tempReserv.hasExpired()) {
    			notifyReserv = tempReserv;
    			cont = false;
    		}
    	}
    	
    	if(notifyReserv != null) {
	        tripNotifyIcon.setImageResource(notifyReserv.isEligibleTrip()?R.drawable.upcoming_trip_green:R.drawable.upcoming_trip_orange);
	        tripNotifyIcon.setVisibility(View.VISIBLE);
	        passengerIcon.setVisibility(View.GONE);
    	}
    	
    	initFontsIfNecessary();
    	
    	if(!cont) {
    		TextView nextDesc = new TextView(LandingActivity2.this);
    		LinearLayout.LayoutParams nextDescLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    		nextDesc.setLayoutParams(nextDescLp);
    		nextDesc.setGravity(Gravity.CENTER);
    		nextDesc.setText("NEXT");
    		nextDesc.setTextColor(getResources().getColor(android.R.color.white));
    		nextDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
    		nextDesc.setBackgroundColor(getResources().getColor(android.R.color.black));
    		reservationListPanel.addView(nextDesc);
    		
    		for(int i = curReservIdx ; i < reservations.size() ; i++) {
    			Reservation reserv = reservations.get(i);
    			if(!removedReservIds.contains(reserv.getRid())) {
    				boolean isFirst = i == curReservIdx;
    				View reservInfoView = createReservationInfoView(reserv, isFirst);
    				if(isFirst) {
    					MapDisplayActivity.cleanReservationTollHovInfoBeforeId(LandingActivity2.this, reserv.getRid());
    				}
    				reservationListPanel.addView(reservInfoView);
    				if(isFirst) {
    					TextView scheduledDesc = new TextView(LandingActivity2.this);
    		    		LinearLayout.LayoutParams scheduledDescLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    		    		scheduledDesc.setLayoutParams(scheduledDescLp);
    		    		scheduledDesc.setGravity(Gravity.CENTER);
    		    		scheduledDesc.setText("SCHEDULED");
    		    		scheduledDesc.setTextColor(getResources().getColor(android.R.color.white));
    		    		scheduledDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
    		    		scheduledDesc.setBackgroundColor(getResources().getColor(android.R.color.black));
    		    		reservationListPanel.addView(scheduledDesc);
    				}
    				else if(i != reservations.size() - 1) {
    					View spliter = new View(LandingActivity2.this);
    					LinearLayout.LayoutParams spliterLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Dimension.dpToPx(1, getResources().getDisplayMetrics()));
    					spliter.setLayoutParams(spliterLp);
    					spliter.setBackgroundColor(getResources().getColor(R.color.light_gray));
    					reservationListPanel.addView(spliter);
    				}
    				
    			}
    		}
    		
    		if (!disableRefreshTripInfo.get()) showTripInfoPanel(false, false);
    	}
    	else {
    		dismissReservId = -1L;
    		tripNotifyIcon.setVisibility(View.GONE);
    		if(closeIfEmpty.get()) {
    			hideReservationInfoPanel();
    		}
    	}
 
    	int reservCount = curReservIdx == -1 ? 0 : reservations.size() - curReservIdx;
    	if((reservCount == 1 && !DebugOptionsActivity.isUserCloseTip(LandingActivity2.this)) || reservCount > 1) {
			findViewById(R.id.add_new_trip_background).setVisibility(View.GONE);
			if(reservCount > 1) {
				newUserTipView.setVisibility(View.GONE);
				if(reservCount == 2) {
					View emptyView = createEmptyReservationInfoView();
					reservationListPanel.addView(emptyView);
				}
			}
			else {
				newUserTipView.setVisibility(DebugOptionsActivity.isUserCloseTip(LandingActivity2.this)?View.GONE:View.VISIBLE);
			}
		}
		else {
			findViewById(R.id.add_new_trip_background).setVisibility(View.VISIBLE);
			newUserTipView.setVisibility(DebugOptionsActivity.isUserCloseTip(LandingActivity2.this)?View.GONE:View.VISIBLE);
		}
    }
    
    private SpannableString formatCountDownTime(String countDownDesc) {
    	int indexOfChange = countDownDesc.indexOf("\n");
    	SpannableString countDownSpan = SpannableString.valueOf(countDownDesc);
		countDownSpan.setSpan(new AbsoluteSizeSpan(Dimension.dpToPx(50, getResources().getDisplayMetrics())), 0, indexOfChange,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		countDownSpan.setSpan(new AbsoluteSizeSpan(getResources()
					.getDimensionPixelSize(R.dimen.smallest_font)), indexOfChange + 1, countDownDesc.length(), 
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		countDownSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.transparent_black)), indexOfChange + 1, countDownDesc.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return countDownSpan;
    }
    
    private String formatTime(long time, int timzoneOffset, boolean showDate){
    	String format = showDate ? "EEEE h:mm a" : "h:mm a";
	    SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone(Request.getTimeZone(timzoneOffset)));
        return dateFormat.format(new Date(time));
	}
    
    private String getFormattedDuration(int duration){
	    return String.format("%dmin", duration/60);
	}
    
    private void showTripInfoPanel(boolean force, boolean animation) {
    	View tripInfoPanel = findViewById(R.id.reservations_list_view);
    	if((force && hasReservTrip()) || 
    			(tripInfoPanel.getVisibility() != View.VISIBLE && hasReservTrip() && !dismissReservId.equals(getFirstReservation().getRid()))) {
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
    	}
    }
    
    private void showFavoriteOptPanel(PoiOverlayInfo info) {
    	Intent favOpt = new Intent(LandingActivity2.this, FavoriteOperationActivity.class);
    	favOpt.putExtra(FavoriteOperationActivity.FAVORITE_POI_INFO, info);
    	startActivityForResult(favOpt, FavoriteOperationActivity.FAVORITE_OPT);
    }
    
    private boolean hasReservTrip() {
    	int childCount = reservationListPanel.getChildCount();
    	for(int i = 0 ; i < childCount ; i++) {
    		View child = reservationListPanel.getChildAt(i);
    		if(child.getTag() != null && child.getTag() instanceof Reservation) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private Reservation getFirstReservation() {
    	int childCount = reservationListPanel.getChildCount();
    	for(int i = 0 ; i < childCount ; i++) {
    		View child = reservationListPanel.getChildAt(i);
    		if(child.getTag() != null && child.getTag() instanceof Reservation && child.findViewById(R.id.reservation_on_my_way).getVisibility() == View.VISIBLE) {
    			return (Reservation) child.getTag();
    		}
    	}
    	return null;
    }
    
    private void hideTripInfoPanel() {
    	findViewById(R.id.reservations_list_view).setVisibility(View.GONE);
    }
    
    private void hideReservationInfoPanel() {
    	hideTripInfoPanel();
    	//unlockMenu();
    }
    
    private void unlockMenu() {
    	DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
    
    private boolean isReservationInfoShown() {
    	return findViewById(R.id.reservations_list_view).getVisibility() == View.VISIBLE;
    }
    
    private SpannableString formatTripTime(String startTime) {
    	int firstNumberIdx = getFirstNumberIndex(startTime);
		SpannableString startTimeSpan = SpannableString.valueOf(startTime);
		if(firstNumberIdx != -1) {
			startTimeSpan.setSpan(new AbsoluteSizeSpan(getResources()
					.getDimensionPixelSize(R.dimen.smaller_font)), 0, firstNumberIdx,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			startTimeSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.transparent_white)), 0 , firstNumberIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		int lastNumberIdx = getLastNumberIndex(startTime);
		if(lastNumberIdx != -1) {
			startTimeSpan.setSpan(new AbsoluteSizeSpan(getResources()
					.getDimensionPixelSize(R.dimen.smaller_font)), lastNumberIdx + 1, startTime.length(), 
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return startTimeSpan;
    }
    
    private SpannableString formatStartTripTime(String startTripTime) {
    	int firstNumberIdx = getFirstNumberIndex(startTripTime);
		SpannableString startTimeSpan = SpannableString.valueOf(startTripTime);
		if(firstNumberIdx != -1) {
			startTimeSpan.setSpan(new AbsoluteSizeSpan(getResources()
					.getDimensionPixelSize(R.dimen.micro_font)), 0, firstNumberIdx,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		int lastNumberIdx = getLastNumberIndex(startTripTime);
		if(lastNumberIdx != -1) {
			startTimeSpan.setSpan(new AbsoluteSizeSpan(getResources()
					.getDimensionPixelSize(R.dimen.smaller_font)), lastNumberIdx + 1, startTripTime.length(), 
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			startTimeSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.transparent_black)), lastNumberIdx + 1, startTripTime.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return startTimeSpan;
    }
    
    private int getFirstNumberIndex(String str) {
    	char[] strChars = str.toCharArray();
    	for(int i = 0 ; i < strChars.length ; i++) {
    		char c = strChars[i];
    		if(CharUtils.isAsciiNumeric(c)) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    private int getLastNumberIndex(String str) {
    	char[] strChars = str.toCharArray();
    	for(int i = strChars.length - 1 ; i >= 0 ; i--) {
    		char c = strChars[i];
    		if(CharUtils.isAsciiNumeric(c)) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    private Long drawedReservId = Long.valueOf(-1);
    
    private void drawRoute(final Reservation reserv) {
    	if(!drawedReservId.equals(reserv.getRid()) && canDrawReservRoute.get() && enableDrawRoute.get()) {
    		drawedReservId = reserv.getRid();
    		final ReservationTollHovInfo reservInfo = MapDisplayActivity.getReservationTollHovInfo(LandingActivity2.this, drawedReservId);
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
	                    	        reserv.getDepartureTimeUtc(), 0, 0, reserv.getOriginAddress(), reserv.getDestinationAddress(), 
	                    	        reservInfo.isIncludeToll(), versionNumber, reservInfo.isHov());
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
	                    updateMap(route, reserv.getDestinationAddress());
	                } 
	            }
	        };
	        Misc.parallelExecute(routeTask);
    	}
	}
    
    private void updateMap(Route _route, String address) {
    	if(_route != null) {
    		// remove previous drew route
    		mapView.clearAllOverlays();
    		mapView.deleteAnnotation(ROUTE_DESTINATION_ID);
    		List<SKCoordinate> routeCoors = new ArrayList<SKCoordinate>();
    		for(RouteNode node : _route.getNodes()) {
    			routeCoors.add(new SKCoordinate(node.getLongitude(), node.getLatitude()));
    		}
    		
    		SKPolyline routeLine = new SKPolyline();
    		routeLine.setNodes(routeCoors);
    		routeLine.setColor(SkobblerUtils.getRouteColorArray(_route.getColor())); //RGBA
    		routeLine.setLineSize(10);
    		
    		//outline properties, otherwise map crash
    		routeLine.setOutlineColor(SkobblerUtils.getRouteColorArray(_route.getColor()));
    		routeLine.setOutlineSize(10);
    		routeLine.setOutlineDottedPixelsSolid(0);
    		routeLine.setOutlineDottedPixelsSkip(0);
    		//
    		mapView.addPolyline(routeLine);
    		
    		PoiOverlayInfo poiInfo = poiContainer.getExistedPOIByAddress(address);
    		if(poiInfo == null) {
    			SKAnnotation destAnn = new SKAnnotation(ROUTE_DESTINATION_ID);
    			destAnn.setUniqueID(ROUTE_DESTINATION_ID);
    			destAnn.setLocation(new SKCoordinate(_route.getLastNode().getLongitude(), _route.getLastNode().getLatitude()));
    			destAnn.setMininumZoomLevel(POIOVERLAY_HIDE_ZOOM_LEVEL);
    			SKAnnotationView destAnnView = new SKAnnotationView();
                SkobblerImageView destImage = new SkobblerImageView(LandingActivity2.this, R.drawable.pin_destination, 1);
                destImage.setLat(_route.getLastNode().getLatitude());
                destImage.setLon(_route.getLastNode().getLongitude());
                destImage.setImageBitmap(Misc.getBitmap(LandingActivity2.this, R.drawable.pin_destination, 1));
                destAnnView.setView(destImage);
                destAnn.setAnnotationView(destAnnView);
                destAnn.setOffset(new SKScreenPoint(0, Dimension.dpToPx(20, getResources().getDisplayMetrics())));
    			mapView.addAnnotation(destAnn, SKAnimationSettings.ANIMATION_NONE);
    		}
    		
    		RouteRect routeRect = new RouteRect(_route.getNodes());
    		mapView.centerMapOnPosition(new SKCoordinate(routeRect.getMidPoint().getLongitude(), routeRect.getMidPoint().getLatitude()));
    	}
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String intentAddress = getIntentAddress(intent);
        if(intent.getBooleanExtra(LOGOUT, false)){
            startActivity(new Intent(this, LoginActivity.class));
            User.setCurrentUser(LandingActivity2.this, null);
            finish();
            return;
        }else if(StringUtils.isNotBlank(intentAddress)){
        	searchIntentAddress(intentAddress);
        	disableRefreshTripInfo.set(true);
        }
    }
    
    private boolean isToSignInPage(Intent intent) {
        Uri uri = intent.getData();
        if(uri != null) {
            return uri.toString().endsWith("signin");
        }
        return false;
    }
    
    private void searchIntentAddress(final String address) {
    	User.initializeIfNeccessary(this, new Runnable() {
			@Override
			public void run() {
				Location _location = lastLocation;
				if(_location == null) {
					LocationInfo locationInfo = new LocationInfo(LandingActivity2.this);
//			        float lastLat = 32.1559094f; // Tucson
//	                float lastLng = -110.883805f;
			        _location = new Location("");
			        _location.setLatitude(locationInfo.lastLat);
			        _location.setLongitude(locationInfo.lastLong);
				}
				Log.d("LandingActivity2", String.format("current loc lat : %s, lon : %s, address : %s", _location.getLatitude() + "", _location.getLongitude() + "", address));
				searchPOIAddress(address, true, _location, false);
			}
    	});
    }
    
    private String getIntentAddress(Intent intent){
        String address = null;
        Uri uri = intent.getData();
        if(uri != null){
        	try {
	        	if(StringUtils.startsWith(uri.toString(), "http")) {
	        		address = uri.getQueryParameter("q");
	        		if(StringUtils.isBlank(address)) {
	        			address = uri.getQueryParameter("daddr");
	        		}
	        	}
	        	else {
	        		address = Uri.decode(StringUtils.substringAfterLast(uri.toString(), "q="));
	        		address = StringUtils.replace(address, "+", " ");
	        	}
        	}
        	catch(Throwable ignore) {}
        }
        return address;
    }
    
    Typeface boldFont;
    Typeface lightFont;
    Typeface mediumFont;
    Typeface regularFont;
    Typeface robotoLightFont;
    Typeface robotoBoldFont;
    
    private void initFontsIfNecessary(){
        if(boldFont == null){
            boldFont = Font.getBold(getAssets());
        }
        if(lightFont == null){
            lightFont = Font.getLight(getAssets());
        }
        if(mediumFont == null) {
        	mediumFont = Font.getMedium(getAssets());
        }
        if(regularFont == null) {
        	regularFont = Font.getRegular(getAssets());
        }
        if(robotoLightFont == null) {
        	robotoLightFont = Font.getRobotoLight(getAssets());
        }
        if(robotoBoldFont == null) {
        	robotoBoldFont = Font.getRobotoBold(getAssets());
        }
    }
    
    private AtomicBoolean poiTapThrottle = new AtomicBoolean();
    
    private void startRouteActivity(){
        if(!poiTapThrottle.get()){
            poiTapThrottle.set(true);
            mapView.deleteAllAnnotationsAndCustomPOIs();
            mapView.clearAllOverlays();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    poiTapThrottle.set(false);
                }
            }, 500);
            boolean hasFromAddr = curFrom == null?false:StringUtils.isNotBlank(curFrom.address);
            Intent intent = new Intent(this, RouteActivity.class);
            Bundle extras = new Bundle();
            extras.putString(RouteActivity.ORIGIN_ADDR, hasFromAddr?curFrom.address:EditAddress.CURRENT_LOCATION);
            if(curFrom != null){
                extras.putParcelable(RouteActivity.ORIGIN_COORD, curFrom.geopoint);
                extras.putParcelable(RouteActivity.ORIGIN_OVERLAY_INFO, curFrom);
            }else if(myPoint != null){
                extras.putParcelable(RouteActivity.ORIGIN_COORD, myPoint);
                extras.putParcelable(RouteActivity.ORIGIN_OVERLAY_INFO, PoiOverlayInfo.fromCurrentLocation(myPoint));
            }
            extras.putString(RouteActivity.ORIGIN_COORD_PROVIDER, curFromProvider);
            extras.putLong(RouteActivity.ORIGIN_COORD_TIME, curFromTime);
            if(curTo != null){
                extras.putString(RouteActivity.DEST_ADDR, curTo.address);
                extras.putParcelable(RouteActivity.DEST_COORD, curTo.geopoint);
                extras.putParcelable(RouteActivity.DEST_OVERLAY_INFO, curTo);
                intent.putExtras(extras);
//                hideBulbBalloon();
//                hideStarredBalloon();
                removeAllOD();
                startActivity(intent);
            }
            else {
            	//disableShowPassengerMode.set(false);
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
		        LocationInfo cacheLoc = new LocationInfo(LandingActivity2.this);
		        if(System.currentTimeMillis() - cacheLoc.lastLocationUpdateTimestamp < 5 * 60 * 1000) {
		        	GeoPoint curLoc = new GeoPoint(cacheLoc.lastLat, cacheLoc.lastLong);
		        	curLoc.setHeading(cacheLoc.lastHeading);
		        	intent.putExtra(ValidationActivity.CURRENT_LOCATION, (Parcelable)curLoc);
		        }
//		          hideBulbBalloon();
//                hideStarredBalloon();
		        removeAllOD();
		        startActivity(intent);
			}
    	});
    }
    
//    private void refreshStarredPOIs(){
//        refreshStarredPOIs(null, false);
//    }
//    
//    private void refreshStarredPOIsAndUpdateFavoriteList() {
//    	refreshStarredPOIs(null, true);
//    }
    
    private void refreshStarredPOIs(final Runnable callback, final boolean forceUpdateFavorite){
        AsyncTask<Void, Void, List<com.metropia.models.Address>> task = new AsyncTask<Void, Void, List<com.metropia.models.Address>>(){
            @Override
            protected List<com.metropia.models.Address> doInBackground(
                    Void... params) {
                List<com.metropia.models.Address> addrs = Collections.emptyList();
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
            protected void onPostExecute(List<com.metropia.models.Address> result) {
                if(callback != null){
                    callback.run();
                }
                
                if (ehs.hasExceptions()) {
                    //ehs.reportExceptions();
                }
                else {
                    Set<com.metropia.models.Address> addrList = new HashSet<com.metropia.models.Address>();
                    Set<Integer> existedStarPoiUniqueIdSet = poiContainer.getStarUniqueIdSet();
                    for (Integer uniqueId : existedStarPoiUniqueIdSet) {
                        mapView.deleteAnnotation(uniqueId);
                    }
                    poiContainer.cleanStarPois();
                    if (result != null && result.size() > 0) {
                        initFontsIfNecessary();
                        for(com.metropia.models.Address a : result){
                            PoiOverlayInfo poiInfo = PoiOverlayInfo.fromAddress(LandingActivity2.this, a);
                            addAnnotationFromPoiInfo(poiInfo);
                            addrList.add(a);
                        }
                    }
                    if (routeRect==null && restrictedMode) {
                        List<GeoPoint> points = new ArrayList<GeoPoint>();
                        for(com.metropia.models.Address a : addrList){
                            points.add(new GeoPoint(a.getLatitude(), a.getLongitude()));
                        }
                        if (points.size()==0) routeRect=null;
                        else routeRect = new RouteRect(points, mapZoomVerticalOffset);
                        zoomMapToFitBulbPOIs();
                    }
                    showODBalloon();
                    //redraw poi
                    sizeRatio.set(0);
                    updateAnnotationSize(getSizeRatioByZoomLevel());
                    //
                    initFavoriteDropdownIfNessary(addrList, forceUpdateFavorite);
                }
            }
        };
        Misc.parallelExecute(task);
    }
    
    private Map<Integer, com.metropia.models.Address> afterUpdateUse = new HashMap<Integer, com.metropia.models.Address>();
    
    private synchronized void initFavoriteDropdownIfNessary(Collection<com.metropia.models.Address> favorites, boolean forceUpdate) {
    	if(forceUpdate || favoriteAddresses.size() != favorites.size()) {
    		afterUpdateUse.clear();
    		List<Address> newFavorites = new ArrayList<Address>();
    		List<Address> homeFavorite = new ArrayList<Address>();
    		List<Address> workFavorite = new ArrayList<Address>();
    		List<Address> otherFavorite = new ArrayList<Address>();
    		List<com.metropia.models.Address> newModelFavs = new ArrayList<com.metropia.models.Address>();
    		List<com.metropia.models.Address> homeModelFavs = new ArrayList<com.metropia.models.Address>();
    		List<com.metropia.models.Address> workModelFavs = new ArrayList<com.metropia.models.Address>();
    		List<com.metropia.models.Address> otherModelFavs = new ArrayList<com.metropia.models.Address>();
    		for(com.metropia.models.Address addr : favorites) {
    			if(FavoriteIcon.home.name().equals(addr.getIconName())) {
    				homeFavorite.add(Address.fromModelAddress(addr, lastLocation));
    				homeModelFavs.add(addr);
    			}
    			else if(FavoriteIcon.work.name().equals(addr.getIconName())) {
    				workFavorite.add(Address.fromModelAddress(addr, lastLocation));
    				workModelFavs.add(addr);
    			}
    			else {
    				otherFavorite.add(Address.fromModelAddress(addr, lastLocation));
    				otherModelFavs.add(addr);
    			}
    			afterUpdateUse.put(Integer.valueOf(addr.getId()), addr);
    		}
    		Collections.sort(homeFavorite, DebugOptionsActivity.distanceComparator);
    		Collections.sort(workFavorite, DebugOptionsActivity.distanceComparator);
    		Collections.sort(otherFavorite, DebugOptionsActivity.distanceComparator);
    		newFavorites.addAll(homeFavorite);
    		newFavorites.addAll(workFavorite);
    		newFavorites.addAll(otherFavorite);
    		newModelFavs.addAll(homeModelFavs);
    		newModelFavs.addAll(workModelFavs);
    		newModelFavs.addAll(otherModelFavs);
    		favoriteAddresses.clear();
    		favoriteAddresses.addAll(newModelFavs);

    		fromFavoriteAutoCompleteAdapter.clear();
    		for (Address adress:newFavorites) fromFavoriteAutoCompleteAdapter.add(adress);
    		toFavoriteAutoCompleteAdapter.clear();
    		for (Address adress:newFavorites) toFavoriteAutoCompleteAdapter.add(adress);
    	}
    }
    
    private synchronized void refreshInputAddresses() {
    	List<Address> storedInputAddresses = DebugOptionsActivity.getInputAddress(LandingActivity2.this, lastLocation, cityName, DebugOptionsActivity.inputTimeComparator);
    	List<Address> homeAddress = new ArrayList<Address>();
    	List<Address> workAddress = new ArrayList<Address>();
    	List<Address> otherAddress = new ArrayList<Address>();
    	for(Address addr : storedInputAddresses) {
    		if(FavoriteIcon.home.name().equals(addr.getIconName())) {
    			homeAddress.add(addr);
    		}
    		else if(FavoriteIcon.work.name().equals(addr.getIconName())) {
    			workAddress.add(addr);
    		}
    		else {
    			otherAddress.add(addr);
    		}
    	}
    	inputAddresses.clear();
    	inputAddresses.addAll(homeAddress);
    	inputAddresses.addAll(workAddress);
    	inputAddresses.addAll(otherAddress);
	}
    
    private synchronized void addInputAddress(Address address) {
    	DebugOptionsActivity.addInputAddress(LandingActivity2.this, address, cityName);
    	refreshInputAddresses();
    }
    
    private void handleOD(PoiOverlayInfo poiInfo, boolean isFrom) {
    	removeOldOD(isFrom);
    	if(isFrom) {
    		curFrom = poiInfo;
    		drawODBalloon(poiInfo, true);
    		curFromProvider = null;
    		curFromTime = 0;
    		setFromInfo(poiInfo);
    	}
    	else {
    		curTo = poiInfo;
    		drawODBalloon(poiInfo, false);
    		setToInfo(poiInfo);
    		if(curFrom == null && myPoint != null) {
    			PoiOverlayInfo currentLocationInfo = PoiOverlayInfo.fromCurrentLocation(myPoint);
    			curFrom = currentLocationInfo;
    			drawODBalloon(currentLocationInfo, true);
    			setFromInfo(currentLocationInfo);
    			if(lastLocation != null){
        			curFromProvider = lastLocation.getProvider();
                    curFromTime = lastLocation.getTime();
    			}
    		}
    		toggleGetRouteButton(true);
    	}
    	if(!isMapCollapsed()) {
    		resizeMap(true);
    	}
    }
    
    private void drawODBalloon(PoiOverlayInfo info, boolean from) {
    	SKAnnotation balloonAnn = new SKAnnotation(from ? FROM_BALLOON_ID : TO_BALLOON_ID);
    	balloonAnn.setUniqueID(from ? FROM_BALLOON_ID : TO_BALLOON_ID);
    	balloonAnn.setOffset(new SKScreenPoint(0, Dimension.dpToPx(20, getResources().getDisplayMetrics())));
    	balloonAnn.setLocation(new SKCoordinate(info.lon, info.lat));
    	SKAnnotationView balloonView = new SKAnnotationView();
    	SkobblerImageView balloonImage = new SkobblerImageView(LandingActivity2.this, 0, 0);
    	balloonImage.setLat(info.lat);
    	balloonImage.setLon(info.lon);
    	balloonImage.setDesc(from ? "FROM" : "TO");
    	balloonImage.setImageBitmap(loadBitmapOfFromToBalloon(LandingActivity2.this, from));
    	balloonView.setView(balloonImage);
    	balloonAnn.setAnnotationView(balloonView);
    	mapView.addAnnotation(balloonAnn, SKAnimationSettings.ANIMATION_POP_OUT);
    }
    
    private AtomicBoolean removeOD = new AtomicBoolean(true);
    
    private void setFromInfo(PoiOverlayInfo info) {
    	int marker = (info.marker == R.drawable.transparent_poi && StringUtils.isBlank(info.address)) ? R.drawable.landing_page_current_location : info.marker;
    	fromIcon.setImageResource(marker);
		fromIcon.setVisibility(View.VISIBLE);
		removeOD.set(false);
		fromSearchBox.setText(StringUtils.isNotBlank(info.label) ? info.label : info.address);
		fromSearchBox.clearFocus();
		removeOD.set(true);
    }
    
    private void setToInfo(PoiOverlayInfo info) {
    	toIcon.setImageResource(info.marker);
		toIcon.setVisibility(View.VISIBLE);
		removeOD.set(false);
	    searchBox.setText(StringUtils.isNotBlank(info.label) ? info.label : info.address);
	    searchBox.clearFocus();
	    removeOD.set(true);
    }
    
//    private boolean hideStarredBalloon(){
//        boolean handled = false;
//        MapView mapView = (MapView) findViewById(R.id.mapview);
//        List<Overlay> overlays = mapView.getOverlays();
//        for (Overlay overlay : overlays) {
//            if(overlay instanceof POIOverlay){
//                POIOverlay poiOverlay = (POIOverlay)overlay;
//                if(isFavoriteMark(poiOverlay.getMarker())){
//                    if(poiOverlay.isBalloonVisible() && !isMarkedOD(poiOverlay)){
//                	    poiOverlay.hideBalloon();
//                        handled = true;
//                    }
//                }
//            }
//        }
//        return handled;
//    }
//    
//    private boolean hideBulbBalloon(){
//        boolean handled = false;
//        MapView mapView = (MapView) findViewById(R.id.mapview);
//        List<Overlay> overlays = mapView.getOverlays();
//        for (Overlay overlay : overlays) {
//            if(overlay instanceof POIOverlay){
//                POIOverlay poiOverlay = (POIOverlay)overlay;
//                if(poiOverlay.getMarker() == R.drawable.bulb_poi){
//                    if(poiOverlay.isBalloonVisible() && !isMarkedOD(poiOverlay)){
//                    	poiOverlay.hideBalloon();
//                        handled = true;
//                    }
//                }
//            }
//        }
//        return handled;
//    }
    
    private RouteRect cityRange;
    private String cityName = CityRequest.NO_CITY_NAME;
    private AtomicBoolean serviceArea = new AtomicBoolean(true);
    private AtomicBoolean notifyOutOfService = new AtomicBoolean(true);
    private String outOfServiceHtml = "";
    
    private void refreshCobranding(final double lat, final double lon, 
            final boolean alertAvailability, final Runnable callback){
    	final ImageView logoView = (ImageView) findViewById(R.id.logo);
        AsyncTask<Void, Void, City> checkCityAvailability = new AsyncTask<Void, Void, City>(){
            @Override
            protected City doInBackground(Void... params) {
                City result;
                try{
                    CityRequest req = new CityRequest(lat, lon, HTTP.defaultTimeout);
                    req.invalidateCache(LandingActivity2.this);
                    result = req.execute(LandingActivity2.this);
                }catch(Throwable t){
                    result = null;
                }
                return result;
            }
            @Override
            protected void onPostExecute(City result) {
            	new ReleaseDialog(LandingActivity2.this).show();
            	
            	if (result==null) return;
            	LandingActivity2.restrictedMode = RouteActivity.restrictedMode = StringUtils.equals(result.signUp, "http://www.metropia.com/elpasolite");
            	restrictedMode(restrictedMode);
            	
                if(StringUtils.isNotBlank(result.html)){
//                	serviceArea.set(false);
                	outOfServiceHtml = result.html;
                    if(alertAvailability){
                        CharSequence msg = Html.fromHtml(result.html);
                        NotificationDialog2 dialog = new NotificationDialog2(LandingActivity2.this, msg);
                        dialog.setTitle("Notification");
                        try{
                            dialog.show();
                        }catch(Throwable t){}
                    }
                }else{
                	serviceArea.set(true);
                	checkInboxUrlAndUpdateMenu(result.name);
                    try{
                        cityRange = new RouteRect(Double.valueOf(result.maxLat * 1E6).intValue(), 
                    		Double.valueOf(result.maxLon * 1E6).intValue(), Double.valueOf(result.minLat * 1E6).intValue(), 
                    		Double.valueOf(result.minLon * 1E6).intValue());
                        cityName = result.name;
                        LoadImageTask logoTask = new LoadImageTask(LandingActivity2.this, result.logo) {
                            protected void onPostExecute(final Bitmap rs) {
                                if(rs != null){
                                    logoView.setVisibility(View.VISIBLE);
                                    logoView.setImageBitmap(rs);
                                }else{
                                    logoView.setVisibility(View.GONE);
                                }
                            }
                        };
                        Misc.parallelExecute(logoTask);
                    }catch(Throwable t){}
                }
                if(callback != null){
                    callback.run();
                }
            }
        };
        Misc.parallelExecute(checkCityAvailability);
    }
    
    private String inboxCityName;
    private Long realLastFeed = Long.valueOf(0);
    
    private void checkInboxUrlAndUpdateMenu(final String cityName) {
    	if(StringUtils.isNotBlank(cityName) && StringUtils.isNotBlank(Request.getPageUrl(Page.bulletinboard))) {
    		Misc.parallelExecute(new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					String inboxUrl = Request.getPageUrl(Page.bulletinboard).replaceAll("\\{city\\}", cityName);
		    		try {
						HTTP http = new HTTP(inboxUrl);
						http.connect();
						int responseCode = http.getResponseCode();
						if(responseCode == 200) {
							String responseBody = http.getResponseBody();
							final Long visitedTime = DebugOptionsActivity.getInboxLastVisitFeedTime(LandingActivity2.this, cityName);
							final Integer newMessageCount = Misc.getNewInboxMessageCount(responseBody, visitedTime);
							realLastFeed = Misc.getCurrentLastFeed(responseBody);
							inboxCityName = cityName;
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									findViewById(R.id.message_menu).setVisibility(View.VISIBLE);
									TextView newFeed = (TextView) findViewById(R.id.inbox_notification);
									newFeed.setText(newMessageCount + "");
									newFeed.setVisibility(newMessageCount > 0 ? View.VISIBLE : View.GONE);
									boolean isMenuNotificationDismissed = DebugOptionsActivity.getInboxMenuDismissRecord(LandingActivity2.this, cityName).equals(visitedTime);
									findViewById(R.id.menu_notification).setVisibility((!isMenuNotificationDismissed && newMessageCount > 0) ? View.VISIBLE : View.GONE);
								}
							});
						}
						else {
							inboxCityName = "";
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									findViewById(R.id.message_menu).setVisibility(View.GONE);
								}
							});
						}
					} catch (IOException e) {
						inboxCityName = "";
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								findViewById(R.id.message_menu).setVisibility(View.GONE);
							}
						});
						Log.d("LandingActivity2", "no inbox url");
					}
					return null;
				}
    		});
    	}
    }
    
    private RouteRect routeRect;
    
    private void zoomMapToFitBulbPOIs(){
    	if(routeRect != null){
            GeoPoint topLeft = routeRect.getTopLeftPoint();
            GeoPoint bottomRight = routeRect.getBottomRightPoint();
			final SKBoundingBox boundingBox = new SKBoundingBox(topLeft.getLatitude(), topLeft.getLongitude(), bottomRight.getLatitude(), bottomRight.getLongitude());
			mapView.fitBoundingBox(boundingBox, 200, 200);
        }
    }
    
    private void refreshBulbPOIs(final double lat, final double lon, final boolean rezoom){
        final User user = User.getCurrentUser(LandingActivity2.this);
        AsyncTask<Void, Void, List<com.metropia.requests.WhereToGoRequest.Location>> task = 
                new AsyncTask<Void, Void, List<com.metropia.requests.WhereToGoRequest.Location>>() {
            @Override
            protected List<com.metropia.requests.WhereToGoRequest.Location> doInBackground(Void... params) {
                List<com.metropia.requests.WhereToGoRequest.Location> locs = Collections.emptyList();
                try {
                	WhereToGoRequest req = new WhereToGoRequest(user, lat, lon);
                	req.invalidateCache(LandingActivity2.this);
                    locs = req.execute(LandingActivity2.this);
                }
                catch (Exception e) {
                    //ehs.registerException(e, "[" + req.getURL() + "]\n" + e.getMessage());
                }
                return locs;
            }
            @Override
            protected void onPostExecute(final List<com.metropia.requests.WhereToGoRequest.Location> locs) {
                if (ehs.hasExceptions()) {
                    //ehs.reportExceptions();
                    routeRect = null;
                    if(rezoom){
                        mapView.setZoom(DEFAULT_ZOOM_LEVEL);
                        mapView.centerMapOnPosition(new SKCoordinate(lon, lat));
                    }
                }
                else {
                    refreshStarredPOIs(new Runnable() {
                        @Override
                        public void run() {
                            Set<Integer> bulbUniqueIdSet = poiContainer.getBulbUniqueIdSet();
                            for (Integer uniqueId : bulbUniqueIdSet) {
                                mapView.deleteAnnotation(uniqueId);
                            }
                            poiContainer.cleanBulbPois();
                            Set<String> addrSet = new HashSet<String>();
                            if(locs.isEmpty()){
                                routeRect = null;
                                if(rezoom){
                                    mapView.setZoom(DEFAULT_ZOOM_LEVEL);
                                    mapView.centerMapOnPosition(new SKCoordinate(lon, lat));
                                }
                            }else{
                                drawBulbPOIs(locs);
                                List<GeoPoint> points = new ArrayList<GeoPoint>();
                                points.add(new GeoPoint(lat, lon));
                                for(com.metropia.requests.WhereToGoRequest.Location l : locs){
                                    points.add(new GeoPoint(l.lat, l.lon));
                                }
                                routeRect = new RouteRect(points, mapZoomVerticalOffset);
                                if(rezoom){
                                    zoomMapToFitBulbPOIs();
                                }
                                for(com.metropia.requests.WhereToGoRequest.Location l : locs){
                                    addrSet.add(l.addr);
                                }
                            }
                            showODBalloon();
                            refreshSearchAutoCompleteData();
                        }
                    }, true);
                    updateAnnotationSize(getSizeRatioByZoomLevel());
                }
            }
        };
        Misc.parallelExecute(task);
    }
    
    private static final Integer POIOVERLAY_HIDE_ZOOM_LEVEL = 9;
    private static final String LOADING_ADDRESS = "Loading Address...";
    
	private void showPopupMenu(Screen xy, PoiOverlayInfo info) {
    	poiIcon.setVisibility(View.VISIBLE);
    	poiIcon.setImageResource(info.markerWithShadow);
    	
    	BitmapFactory.Options dimensions = new BitmapFactory.Options(); 
    	dimensions.inJustDecodeBounds = false;
    	Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.edit_menu, dimensions);
    	int menuButtonWidth = decodeResource.getWidth();
    	int menuButtonHeight = decodeResource.getHeight();
    	decodeResource.recycle();
    	
    	int poiIconWidth = Dimension.dpToPx(45, getResources().getDisplayMetrics());
    	int poiIconHeight = poiIconWidth;
    	
    	FrameLayout.LayoutParams poiLp = (android.widget.FrameLayout.LayoutParams) poiIcon.getLayoutParams();
    	poiLp.leftMargin = Float.valueOf(xy.x).intValue() - (poiIconWidth / 2);
    	poiLp.topMargin = Float.valueOf(xy.y).intValue() - (poiIconHeight / 2);
    	poiIcon.setLayoutParams(poiLp);
    	
    	final View predictiveDesttutorial = findViewById(R.id.predictive_destination_tutorial);
    	if(info.markerWithShadow == R.drawable.bulb_poi_with_shadow && !DebugOptionsActivity.isPredictiveDestinationTutorialShown(LandingActivity2.this)) {
    		DebugOptionsActivity.setPredictiveDestinationTutorialShown(LandingActivity2.this);
    		FrameLayout.LayoutParams predictiveDestTutorialLp = (android.widget.FrameLayout.LayoutParams) predictiveDesttutorial.getLayoutParams();
    		predictiveDestTutorialLp.topMargin = Float.valueOf(xy.y).intValue() + (poiIconHeight / 2);
    		predictiveDesttutorial.setLayoutParams(predictiveDestTutorialLp);
    		predictiveDesttutorial.setVisibility(View.VISIBLE);
    		ImageView tutorialCloseView = (ImageView) findViewById(R.id.tutorial_close);
        	tutorialCloseView.setImageBitmap(Misc.getBitmap(LandingActivity2.this, R.drawable.tip_close, 1));
        	tutorialCloseView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					v.setClickable(false);
					ClickAnimation clickAni = new ClickAnimation(LandingActivity2.this, v);
					clickAni.startAnimation(new ClickAnimationEndCallback() {
						@Override
						public void onAnimationEnd() {
							predictiveDesttutorial.setVisibility(View.GONE);
							v.setClickable(true);
						}
					});
				}
        	});
    	}
    	else {
    		predictiveDesttutorial.setVisibility(View.GONE);
    	}
    	
    	editMenu.setVisibility(!restrictedMode? View.VISIBLE:View.GONE);
    	Integer[] imageResourceIds;
    	if(info.id == 0) {
    		editMenu.setImageResource(R.drawable.save_menu);
    		imageResourceIds = new Integer[] {R.drawable.selected_save_menu, R.drawable.save_menu};
    	}
    	else {
    		editMenu.setImageResource(R.drawable.edit_menu);
    		imageResourceIds = new Integer[] {R.drawable.selected_edit_menu, R.drawable.edit_menu};
    	}
    	DisplayMetrics dm = getResources().getDisplayMetrics();
    	editMenu.setTag(imageResourceIds);
    	FrameLayout.LayoutParams editMenuLp = (android.widget.FrameLayout.LayoutParams) editMenu.getLayoutParams();
    	editMenuLp.leftMargin = Float.valueOf(xy.x).intValue() - (menuButtonWidth / 2);
    	editMenuLp.topMargin = Float.valueOf(xy.y).intValue() - (menuButtonHeight + poiIconHeight / 2 + Dimension.dpToPx(20, dm));
    	editMenu.setLayoutParams(editMenuLp);
    	
    	Screen corespondXY = new Screen();
    	corespondXY.x = 0;
    	corespondXY.y = (menuButtonHeight / 2 + poiIconHeight / 2 + Dimension.dpToPx(20, dm));
    	
    	Screen fromXY = getRelativeCoorOfDegree(corespondXY, -65);
    	fromMenu.setVisibility(View.VISIBLE);
    	FrameLayout.LayoutParams fromMenuLp = (android.widget.FrameLayout.LayoutParams) fromMenu.getLayoutParams();
    	fromMenuLp.leftMargin = Float.valueOf(xy.x).intValue() - Float.valueOf(fromXY.x).intValue() - (menuButtonWidth / 2);
    	fromMenuLp.topMargin = Float.valueOf(xy.y).intValue() - Float.valueOf(fromXY.y).intValue() - (menuButtonWidth / 2);
    	fromMenu.setLayoutParams(fromMenuLp);
    	
    	toMenu.setVisibility(View.VISIBLE);
    	FrameLayout.LayoutParams toMenuLp = (android.widget.FrameLayout.LayoutParams) toMenu.getLayoutParams();
    	toMenuLp.leftMargin = Float.valueOf(xy.x).intValue() + Float.valueOf(fromXY.x).intValue() - (menuButtonWidth / 2);
    	toMenuLp.topMargin = Float.valueOf(xy.y).intValue() - Float.valueOf(fromXY.y).intValue() - (menuButtonWidth / 2);
    	toMenu.setLayoutParams(toMenuLp);
    	
    	addressInfo.setVisibility(View.VISIBLE);
    	addressInfo.setText(StringUtils.isNotBlank(info.label) ? info.label : info.address);
    	FrameLayout.LayoutParams addressInfoLp = (FrameLayout.LayoutParams) addressInfo.getLayoutParams();
    	View landingPanelView = findViewById(R.id.landing_panel_content);
        int landingPanelHeight = landingPanelView.getHeight();
    	int margin = Dimension.dpToPx(8, dm);
    	addressInfoLp.leftMargin = margin;
    	addressInfoLp.rightMargin = margin;
    	addressInfoLp.topMargin = isMapCollapsed() ? (margin + landingPanelHeight) : margin;
    	addressInfo.setLayoutParams(addressInfoLp);
    	
    	popupPanel.setTag(info);
    	popupPanel.setVisibility(View.VISIBLE);
    	popupPanel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removePOIMarker();
				hidePopupMenu();
			}
        });
    }
	
	private static Object mutex = new Object();
	
	private void updatePopupMenu(String address) {
		synchronized(mutex) {
			if(popupPanel.getVisibility() == View.VISIBLE && popupPanel.getTag() != null) {
				PoiOverlayInfo info = (PoiOverlayInfo) popupPanel.getTag();
				info.label = "";
				info.address = address;
				popupPanel.setTag(info);
				addressInfo.setText(address);
			}
		}
	}
	
	private void hidePopupMenu() {
		synchronized(mutex) {
			editMenu.setVisibility(View.INVISIBLE);
			fromMenu.setVisibility(View.INVISIBLE);
			toMenu.setVisibility(View.INVISIBLE);
			poiIcon.setVisibility(View.INVISIBLE);
			addressInfo.setText("");
			addressInfo.setVisibility(View.INVISIBLE);
			popupPanel.setTag(null);
			popupPanel.setVisibility(View.GONE);
		}
	}
	
	private boolean isPopupMenuShown() {
		return popupPanel.getVisibility() == View.VISIBLE;
	}
	
	private void simulateTouch(View popupPanel, Screen xy) {
		// Obtain MotionEvent object
		long downTime = System.currentTimeMillis();
		long eventTime = downTime + 100;
		// List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
		int metaState = 0;
		MotionEvent motionEvent = MotionEvent.obtain(
		    downTime, 
		    eventTime, 
		    MotionEvent.ACTION_DOWN, 
		    xy.x, 
		    xy.y, 
		    metaState
		);

		// Dispatch touch event to view
		popupPanel.dispatchTouchEvent(motionEvent);
	}
    
	// temporarily always show center
    public Screen getScreenXY(double lat, double lon) {
    	SKScreenPoint poiScreenPoint = mapView.coordinateToPoint(new SKCoordinate(lon, lat));
    	Screen pointXY = new Screen();
    	pointXY.x = poiScreenPoint.getX();
    	pointXY.y = poiScreenPoint.getY();
    	return pointXY;
    }
    
    public Screen getScreenWidthHeight() {
    	SKCoordinateRegion currentMapRegion = mapView.getCurrentMapRegion();
    	SKScreenPoint centerPoint = mapView.coordinateToPoint(currentMapRegion.getCenter());
    	Screen screen = new Screen();
    	screen.x = 2 * centerPoint.getX();
    	screen.y = 2 * centerPoint.getY();
    	return screen;
    }
    
    private Screen getRelativeCoorOfDegree(Screen oriCoor, Integer degree) {
    	Screen after = new Screen();
    	double radian = (degree % 360) * Math.PI / 180.0;
    	after.x = Double.valueOf(oriCoor.x * Math.cos(radian) - oriCoor.y * Math.sin(radian)).intValue();
    	after.y = Double.valueOf(oriCoor.x * Math.sin(radian) + oriCoor.y * Math.cos(radian)).intValue();
    	return after;
    }
    
    private static class Screen {
    	public float x;
    	public float y;
    }
    
    private Screen getScreenCenter() {
    	SKCoordinateRegion currentMapRegion = mapView.getCurrentMapRegion();
    	SKScreenPoint centerPoint = mapView.coordinateToPoint(currentMapRegion.getCenter());
       	Screen pointXY = new Screen();
       	pointXY.x = centerPoint.getX();
       	pointXY.y = centerPoint.getY();
       	return pointXY;
    }
    
    private SKCoordinate getCenterGeoPointByMapSize(double lat, double lon) {
    	Screen favXY = getScreenXY(lat, lon);
    	Screen centerOfMap = new Screen();
    	centerOfMap.x = favXY.x;
    	centerOfMap.y = favXY.y;
    	if(isMapCollapsed()) {
	    	View landingPanelView = findViewById(R.id.landing_panel_content);
	        int landingPanelHeight = landingPanelView.getHeight();
	        View myMetropiaPanel = findViewById(R.id.my_metropia_panel);
	        int myMetropiaPanelHeight = myMetropiaPanel.getHeight();
	        centerOfMap.y = favXY.y - landingPanelHeight + myMetropiaPanelHeight;
    	}
    	return mapView.pointToCoordinate(new SKScreenPoint(centerOfMap.x, centerOfMap.y));
    }
    
    private Screen getPopupFavIconPosition() {
    	Screen center = getScreenCenter();
    	Screen centerOfMap = new Screen();
    	centerOfMap.x = center.x;
    	centerOfMap.y = center.y;
    	if(isMapCollapsed()) {
	    	View landingPanelView = findViewById(R.id.landing_panel_content);
	        int landingPanelHeight = landingPanelView.getHeight();
	        View myMetropiaPanel = findViewById(R.id.my_metropia_panel);
	        int myMetropiaPanelHeight = myMetropiaPanel.getHeight();
	        centerOfMap.y = center.y + landingPanelHeight - myMetropiaPanelHeight;
    	}
    	return centerOfMap;
    }
    
    private boolean isMapCollapsed(){
        Boolean collapsedTag = (Boolean) mapViewHolder.getTag();
        boolean collapsed = collapsedTag == null?true:collapsedTag.booleanValue();
        return collapsed;
    }
    
    private void resizeMap(boolean collapsed){
        mapViewHolder.setTag(collapsed);
        searchBox.setFocusableInTouchMode(collapsed);
        searchBox.setFocusable(collapsed);
        fromSearchBox.setFocusableInTouchMode(collapsed);
        fromSearchBox.setFocusable(collapsed);
        fromDropDownButton.setClickable(collapsed);
        toDropDownButton.setClickable(collapsed);
        View landingPanelView = findViewById(R.id.landing_panel_content);
        int landingPanelHeight = landingPanelView.getHeight();
        List<Animator> allAnimators = new ArrayList<Animator>();
        ObjectAnimator landingPanelAnimator;
        if(collapsed) {
        	landingPanelAnimator = ObjectAnimator.ofFloat(landingPanelView, "translationY", -landingPanelHeight, 0); 
        }
        else {
        	landingPanelAnimator = ObjectAnimator.ofFloat(landingPanelView, "translationY", 0, -landingPanelHeight);
        }
        landingPanelAnimator.setDuration(500);
        landingPanelAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        allAnimators.add(landingPanelAnimator);
        
        View myMetropiaPanel = findViewById(R.id.my_metropia_panel);
        int myMetropiaPanelHeight = myMetropiaPanel.getHeight();
        ObjectAnimator myMetropiaPanelAnimator;
        if(collapsed) {
        	myMetropiaPanelAnimator = ObjectAnimator.ofFloat(myMetropiaPanel, "translationY", myMetropiaPanelHeight, 0);
        }
        else {
        	myMetropiaPanelAnimator = ObjectAnimator.ofFloat(myMetropiaPanel, "translationY", 0, myMetropiaPanelHeight);
        }
        myMetropiaPanelAnimator.setDuration(500);
        myMetropiaPanelAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        allAnimators.add(myMetropiaPanelAnimator);
        
        View compass = findViewById(R.id.center_map_icon);
        ObjectAnimator compassAnimator;
        if(collapsed) {
        	compassAnimator = ObjectAnimator.ofFloat(compass, "translationY", myMetropiaPanelHeight, 0);
        }
        else {
        	compassAnimator = ObjectAnimator.ofFloat(compass, "translationY", 0, myMetropiaPanelHeight);
        }
        compassAnimator.setDuration(500);
        compassAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        allAnimators.add(compassAnimator);
        
        View notifyTrip = findViewById(R.id.trip_notify_icon);
        ObjectAnimator notifyTripAnimator;
        if(collapsed) {
        	notifyTripAnimator = ObjectAnimator.ofFloat(notifyTrip, "translationY", myMetropiaPanelHeight, 0);
        }
        else {
        	notifyTripAnimator = ObjectAnimator.ofFloat(notifyTrip, "translationY", 0, myMetropiaPanelHeight);
        }
        notifyTripAnimator.setDuration(500);
        notifyTripAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        allAnimators.add(notifyTripAnimator);
        
        ObjectAnimator getRouteAnimator;
        if(collapsed) {
        	getRouteAnimator = ObjectAnimator.ofFloat(getRouteView, "translationY", myMetropiaPanelHeight, 0);
        }
        else {
        	getRouteAnimator = ObjectAnimator.ofFloat(getRouteView, "translationY", 0, myMetropiaPanelHeight);
        }
        getRouteAnimator.setDuration(500);
        getRouteAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        allAnimators.add(getRouteAnimator);
        
        View passengerIcon = findViewById(R.id.passenger_mode_icon);
        ObjectAnimator passengerIconAnimator;
        if(collapsed) {
        	passengerIconAnimator = ObjectAnimator.ofFloat(passengerIcon, "translationY", myMetropiaPanelHeight, 0);
        }
        else {
        	passengerIconAnimator = ObjectAnimator.ofFloat(passengerIcon, "translationY", 0, myMetropiaPanelHeight);
        }
        passengerIconAnimator.setDuration(500);
        passengerIconAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        allAnimators.add(passengerIconAnimator);
        
        View scoreNotifyPanel = findViewById(R.id.score_notify);
        ObjectAnimator scoreNotifyAnimator;
        if(scoreNotifyPanel.getVisibility() == View.VISIBLE) {
        	if(collapsed) {
        		scoreNotifyAnimator = ObjectAnimator.ofFloat(scoreNotifyPanel, "translationY", myMetropiaPanelHeight, 0);
            }
            else {
            	scoreNotifyAnimator = ObjectAnimator.ofFloat(scoreNotifyPanel, "translationY", 0, myMetropiaPanelHeight);
            }
        	scoreNotifyAnimator.setDuration(500);
        	scoreNotifyAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        	allAnimators.add(scoreNotifyAnimator);
        }
        
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(allAnimators);
		animatorSet.start();
    }
    
    private void toggleGetRouteButton(boolean enabled) {
//    	getRouteView.setClickable(enabled);
//    	getRouteView.setBackgroundResource(enabled ? R.drawable.get_route_button : R.drawable.disabled_get_route_button);
//    	getRouteView.setTextColor(enabled ? getResources().getColor(android.R.color.white) : getResources().getColor(R.color.transparent_white));
//    	int padding = Dimension.dpToPx(5, getResources().getDisplayMetrics());
//    	getRouteView.setPadding(padding, 0, padding, 0);
    	setGetRouteButtonState(!serviceArea.get() && !notifyOutOfService.get());
    	getRouteView.setVisibility(enabled?View.VISIBLE:View.GONE);
    	if(enabled || disableShowPassengerMode.get()) {
    		passengerIcon.setVisibility(View.GONE);
    	}
    	else if(tripNotifyIcon.getVisibility() == View.GONE) {
    		passengerIcon.setVisibility(View.VISIBLE);
    	}
    }
    
    private void setGetRouteButtonState(boolean greyOut) {
    	getRouteView.setBackgroundResource(greyOut ? R.drawable.get_route_grey_button : R.drawable.get_route_green_button);
    }
    
    private static final String SANDBOX = "http://sandbox.metropia.com/";
    
    private void refreshHead() {
    	String entrypoint = DebugOptionsActivity.getDebugEntrypoint(LandingActivity2.this);
    	TextView head = (TextView)findViewById(R.id.head);
    	if(StringUtils.startsWith(entrypoint, SANDBOX)) {
    		int slashIdx = StringUtils.indexOf(entrypoint, "/", SANDBOX.length());
    		if(slashIdx > 0 && slashIdx > SANDBOX.length()) {
    			String sandboxEntryPoint = StringUtils.substring(entrypoint, SANDBOX.length() - 1, slashIdx);
    			head.setText("Metropia     " + sandboxEntryPoint);
    		}
    	}
    	else {
    		head.setText("Metropia");
    	}
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
    
    private void removePOIMarker(){
        for(Integer removeUniqueId : getRemainPOIMarkerId()) {
        	mapView.deleteAnnotation(removeUniqueId);
        }
    }
    
    private Integer[] getRemainPOIMarkerId() {
    	Integer[] poiMarkerUniqueIds = {POI_MARKER_ONE, POI_MARKER_TWO, POI_MARKER_THREE};
        if(curFrom != null && ArrayUtils.contains(poiMarkerUniqueIds, curFrom.uniqueId)) {
        	poiMarkerUniqueIds = ArrayUtils.removeElement(poiMarkerUniqueIds, curFrom.uniqueId);
        }
        
        if(curTo != null && ArrayUtils.contains(poiMarkerUniqueIds, curTo.uniqueId)) {
        	poiMarkerUniqueIds = ArrayUtils.removeElement(poiMarkerUniqueIds, curTo.uniqueId);
        }
        return poiMarkerUniqueIds;
    }
    
    private boolean isFromOrToSetted() {
    	return curFrom != null || curTo != null;
    }
    
    private void removeAllOD() {
    	removeOldOD(true);
    	removeOldOD(false);
    	cleanSearchBox();
    }
    
    private void cleanSearchBox() {
    	searchBox.setText("");
		toIcon.setVisibility(View.INVISIBLE);
		clearSearchResult();
		fromSearchBox.setText("");
		fromIcon.setVisibility(View.INVISIBLE);
		clearFromSearchResult();
		disableRefreshTripInfo.set(false);
    }
    
    private static final Integer FROM_BALLOON_ID = 0;
    private static final Integer TO_BALLOON_ID = FROM_BALLOON_ID + 100;  //100
    private static final Integer ROUTE_DESTINATION_ID = TO_BALLOON_ID + 1; // 101
    private static final Integer POI_MARKER_ONE = ROUTE_DESTINATION_ID + 1; // 102
    private static final Integer POI_MARKER_TWO = POI_MARKER_ONE + 1; // 103
    private static final Integer POI_MARKER_THREE = POI_MARKER_TWO + 1; // 104
    private static final Integer CURRENT_LOCATION_ID = POI_MARKER_THREE + 1; // 105
    
    private void removeOldOD(boolean from) {
    	if (mapView==null) return;
    	Integer[] poiMarkerIds = {POI_MARKER_ONE, POI_MARKER_TWO, POI_MARKER_THREE};
    	if(from) {
    		fromIcon.setVisibility(View.INVISIBLE);
    		if(curFrom != null && curFrom.uniqueId > 0 && ArrayUtils.contains(poiMarkerIds, curFrom.uniqueId)) {
    			mapView.deleteAnnotation(curFrom.uniqueId);
    		}
    		mapView.deleteAnnotation(FROM_BALLOON_ID);
    		curFrom = null;
    	}
    	else {
    		toIcon.setVisibility(View.INVISIBLE);
    		if(curTo != null && curTo.uniqueId > 0 && ArrayUtils.contains(poiMarkerIds, curTo.uniqueId)) {
    			mapView.deleteAnnotation(curTo.uniqueId);
    		}
    		mapView.deleteAnnotation(TO_BALLOON_ID);
    		curTo = null;
    		toggleGetRouteButton(false);
    	}
    }
    
    private PoiOverlayInfo refreshPOIMarker(double lat, double lon, String address, String label){
        GeoPoint gp = new GeoPoint(lat, lon);
        BalloonModel model = new BalloonModel();
        model.lat = lat;
        model.lon = lon;
        model.address = address;
        model.label = label;
        model.geopoint = gp;
        PoiOverlayInfo poiInfo = PoiOverlayInfo.fromBalloonModel(model);
        addPOIMarker(poiInfo);
        return poiInfo;
    }
    
    private void addPOIMarker(PoiOverlayInfo markerInfo) {
    	Integer[] poiMarkerIds = getRemainPOIMarkerId();
    	if(poiMarkerIds.length > 0) {
    		Integer uniqueId = poiMarkerIds[0];
    		markerInfo.uniqueId = uniqueId;
    		SKAnnotation incAnn = new SKAnnotation(uniqueId);
    		incAnn.setUniqueID(uniqueId);
    		incAnn.setLocation(new SKCoordinate(markerInfo.lon, markerInfo.lat));
    		incAnn.setMininumZoomLevel(POIOVERLAY_HIDE_ZOOM_LEVEL);
    		SKAnnotationView iconView = new SKAnnotationView();
    		SkobblerImageView incImage = new SkobblerImageView(LandingActivity2.this, markerInfo.markerWithShadow, sizeRatio.get());
    		incImage.setLat(markerInfo.lat);
    		incImage.setLon(markerInfo.lon);
    		incImage.setDesc(markerInfo.address);
    		incImage.setMinimumHeight(annSize.get() / sizeRatio.get());
    		incImage.setMinimumWidth(annSize.get() / sizeRatio.get());
    		incImage.setImageBitmap(Misc.getBitmap(LandingActivity2.this, markerInfo.markerWithShadow, sizeRatio.get()));
    		iconView.setView(incImage);
    		incAnn.setAnnotationView(iconView);
    		mapView.addAnnotation(incAnn, SKAnimationSettings.ANIMATION_NONE);
    	}
    }
    
    private void addAnnotationFromPoiInfo(PoiOverlayInfo poiInfo) {
   		SKAnnotation incAnn = new SKAnnotation(poiContainer.addPOIToMap(poiInfo));
//   		incAnn.setUniqueID();
   		incAnn.setLocation(new SKCoordinate(poiInfo.lon, poiInfo.lat));
   		incAnn.setMininumZoomLevel(POIOVERLAY_HIDE_ZOOM_LEVEL);
   		SKAnnotationView iconView = new SKAnnotationView();
   		SkobblerImageView incImage = new SkobblerImageView(LandingActivity2.this, poiInfo.markerWithShadow, sizeRatio.get());
   		incImage.setLat(poiInfo.lat);
   		incImage.setLon(poiInfo.lon);
   		incImage.setDesc(poiInfo.address);
   		incImage.setMinimumHeight(annSize.get() / sizeRatio.get());
		incImage.setMinimumWidth(annSize.get() / sizeRatio.get());
//		incImage.setImageResource(poiInfo.markerWithShadow);
   		incImage.setImageBitmap(Misc.getBitmap(LandingActivity2.this, poiInfo.markerWithShadow, sizeRatio.get()));
   		iconView.setView(incImage);
   		incAnn.setAnnotationView(iconView);
   		mapView.addAnnotation(incAnn, SKAnimationSettings.ANIMATION_NONE);
    }
    
    private AtomicInteger sizeRatio = new AtomicInteger(1);
    private AtomicInteger annSize = new AtomicInteger();
    
    private void updateAnnotationSize(int ratio) {
    	if(sizeRatio.get() != ratio) {
    		sizeRatio.set(ratio);
	    	Set<Integer> starIds = poiContainer.getStarUniqueIdSet();
	    	for(Integer uniqueId : starIds) {
	    		PoiOverlayInfo poiInfo = poiContainer.getExistedPOIByUniqueId(uniqueId);
	    		if(poiInfo != null) {
	    			SKAnnotation incAnn = new SKAnnotation(uniqueId);
	    			incAnn.setUniqueID(uniqueId);
	    			incAnn.setLocation(new SKCoordinate(poiInfo.lon, poiInfo.lat));
	    			incAnn.setMininumZoomLevel(POIOVERLAY_HIDE_ZOOM_LEVEL);
	    			SKAnnotationView iconView = new SKAnnotationView();
	    			SkobblerImageView incImage = new SkobblerImageView(LandingActivity2.this, poiInfo.markerWithShadow, ratio);
	    			incImage.setLat(poiInfo.lat);
	    			incImage.setLon(poiInfo.lon);
	    			incImage.setDesc(poiInfo.address);
	    			incImage.setMinimumHeight(annSize.get() / ratio);
	    			incImage.setMinimumWidth(annSize.get() / ratio);
	    			incImage.setMaxHeight(annSize.get() / ratio);
	    			incImage.setMaxWidth(annSize.get() / ratio);
//	    			incImage.setImageResource(poiInfo.markerWithShadow);
	    			incImage.setImageBitmap(Misc.getBitmap(LandingActivity2.this, poiInfo.markerWithShadow, ratio));
	    			iconView.setView(incImage);
	    			incAnn.setAnnotationView(iconView);
	    			mapView.addAnnotation(incAnn, SKAnimationSettings.ANIMATION_NONE);
//	    			mapView.updateAnnotation(incAnn);
	    		}
	    	}
	    	Set<Integer> bulbIds = poiContainer.getBulbUniqueIdSet();
	    	for(Integer uniqueId : bulbIds) {
	    		PoiOverlayInfo poiInfo = poiContainer.getExistedPOIByUniqueId(uniqueId);
	    		if(poiInfo != null) {
	    			SKAnnotation incAnn = new SKAnnotation(uniqueId);
	    			incAnn.setUniqueID(uniqueId);
	    			incAnn.setLocation(new SKCoordinate(poiInfo.lon, poiInfo.lat));
	    			incAnn.setMininumZoomLevel(POIOVERLAY_HIDE_ZOOM_LEVEL);
	    			SKAnnotationView iconView = new SKAnnotationView();
	    			SkobblerImageView incImage = new SkobblerImageView(LandingActivity2.this, poiInfo.markerWithShadow, ratio);
	    			incImage.setLat(poiInfo.lat);
	    			incImage.setLon(poiInfo.lon);
	    			incImage.setDesc(poiInfo.address);
	    			incImage.setMinimumHeight(annSize.get() / ratio);
	    			incImage.setMinimumWidth(annSize.get() / ratio);
	    			incImage.setMaxHeight(annSize.get() / ratio);
	    			incImage.setMaxWidth(annSize.get() / ratio);
//	    			incImage.setImageResource(poiInfo.markerWithShadow);
	    			incImage.setImageBitmap(Misc.getBitmap(LandingActivity2.this, poiInfo.markerWithShadow, ratio));
	    			iconView.setView(incImage);
	    			incAnn.setAnnotationView(iconView);
	    			mapView.addAnnotation(incAnn, SKAnimationSettings.ANIMATION_NONE);
//	    			mapView.updateAnnotation(incAnn);
	    		}
	    	}
    	}
    }
    
    private int getSizeRatioByZoomLevel() {
    	float zoomLevel = mapView.getZoomLevel();
    	if(zoomLevel >= 13) {
    		return 1;
    	}
    	else if(zoomLevel >= 9) {
    		return 2;
    	}
    	return 1;
    }
    
    private void updateMyMetropiaInfo() {
    	AsyncTask<Void, Void, MyMetropia> updateTask = new AsyncTask<Void, Void, MyMetropia>() {
    		final User user = User.getCurrentUser(LandingActivity2.this);
			@Override
			protected MyMetropia doInBackground(Void... params) {
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
					SharedPreferences loginPrefs = Preferences.getAuthPreferences(LandingActivity2.this);
                    SharedPreferences.Editor loginPrefsEditor = loginPrefs.edit();
                    final boolean play = StringUtils.equalsIgnoreCase(user.getUsername(), loginPrefs.getString(User.PLAY_SCORE_ANIMATION, ""));
                    if(play) {
                    	loginPrefsEditor.remove(User.PLAY_SCORE_ANIMATION);
                    }
                    loginPrefsEditor.commit();
                    
                    final View scoreNotify = findViewById(R.id.score_notify);
                	TextView scoreNotifyCloseView = (TextView) findViewById(R.id.score_notify_close);
                    scoreNotifyCloseView.setOnClickListener(new OnClickListener() {
            			@Override
            			public void onClick(final View v) {
            				v.setClickable(false);
            				ClickAnimation clickAni = new ClickAnimation(LandingActivity2.this, v);
            				clickAni.startAnimation(new ClickAnimationEndCallback() {
            					@Override
            					public void onAnimationEnd() {
            						scoreNotify.setVisibility(View.GONE);
            						v.setClickable(true);
            					}
            				});
            			}
                	});
                    
                    final StringBuffer rewardString = new StringBuffer();
                    final String formatScore = "%spts";
                    if(play) {
                    	rewardString.append(100);
                    	scoreNotify.setVisibility(View.VISIBLE);
                    	new CountDownTimer(2000, 10) {
    						
    						@Override
    						public void onTick(long millisUntilFinished) {
    							String score = Integer.valueOf(rewardString.toString()) - (millisUntilFinished / 20) + "";
    							upointView.setText(formatMyMetropiaInfo(String.format(formatScore, score)));
    						}
    						
    						@Override
    						public void onFinish() {
    							upointView.setText(formatMyMetropiaInfo(String.format(formatScore, rewardString.toString())));
    						}
    					}.start();
                    }
                    else {
						int reward = info.getCredit();
						if(reward >= 1000) {
							rewardString.append(reward / 1000).append("K");
						}
						else {
							rewardString.append(new DecimalFormat("#000.#").format(reward)); 
						}
						upointView.setText(formatMyMetropiaInfo(String.format(formatScore, rewardString.toString())));
                    }
                    
					int timeSaving = info.getTimeSaving();
					StringBuffer timeSavingString = new StringBuffer();
					if(timeSaving >= 1000) {
						timeSavingString.append(timeSaving / 1000).append("K");
					}
					else {
						timeSavingString.append(new DecimalFormat("#00").format(timeSaving));
					}
					timeSavingString.append("min");
					saveTimeView.setText(formatMyMetropiaInfo(timeSavingString.toString()));
					
					double co2Saving = info.getCo2Saving();
					StringBuffer co2SavingString = new StringBuffer();
					if(co2Saving >= 1000) {
						co2SavingString.append(Double.valueOf(co2Saving / 1000).intValue()).append("K");
					}
					else {
						co2SavingString.append(new DecimalFormat("#000.#").format(co2Saving));
					}
					co2SavingString.append("lbs");
					co2View.setText(formatMyMetropiaInfo(co2SavingString.toString()));
				}
			}
    		
    	};
    	Misc.parallelExecute(updateTask);
    }
    
    private SpannableString formatMyMetropiaInfo(String content) {
    	int indexOfChange = getAlphaIndexExcludeK(content);
    	SpannableString startTimeSpan = SpannableString.valueOf(content);
    	if(indexOfChange != -1) {
    		startTimeSpan.setSpan(new AbsoluteSizeSpan(Dimension.dpToPx(13, getResources().getDisplayMetrics())), 
    				indexOfChange, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    	}
    	return startTimeSpan;
    	
    }
    
    private int getAlphaIndexExcludeK(String content) {
    	char[] charArray = content.toCharArray();
    	for(int i = 0 ; i < charArray.length ; i++) {
    		if(Character.isLetter(charArray[i]) && !"K".equals(String.valueOf(charArray[i]))) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    private synchronized void drawBulbPOIs(List<com.metropia.requests.WhereToGoRequest.Location> locs) {
   		Set<Integer> bulbUniqueIdSet = poiContainer.getBulbUniqueIdSet();
   		for (Integer uniqueId : bulbUniqueIdSet) {
   			mapView.deleteAnnotation(uniqueId);
   		}
   		poiContainer.cleanBulbPois();
   		
   		initFontsIfNecessary();
   		for(final com.metropia.requests.WhereToGoRequest.Location l:locs){
   			PoiOverlayInfo poiInfo = PoiOverlayInfo.fromLocation(l);
   			addAnnotationFromPoiInfo(poiInfo);
   		}
   		showODBalloon();
    }
    
//    /**
//     * insert overlay by the order, current location > home > work > fav
//     * @param mapOverlays
//     * @param overlay
//     */
//    private void insertOverlayByOrderOrSort(List<Overlay> mapOverlays, POIOverlay overlay) {
//    	List<Overlay> homeOverlay = new ArrayList<Overlay>();
//    	List<Overlay> workOverlay = new ArrayList<Overlay>();
//    	List<Overlay> otherFavOverlay = new ArrayList<Overlay>();
//    	List<Overlay> bulbOverlay = new ArrayList<Overlay>();
//    	List<Overlay> otherOverlay = new ArrayList<Overlay>();
//    	Overlay currentLocationOverlay = null;
//    	if(overlay != null) {
//	    	if(R.drawable.home == overlay.getMarker()) {
//	    		homeOverlay.add(overlay);
//	    	}
//	    	else if(R.drawable.work == overlay.getMarker()){
//	    		 workOverlay.add(overlay);
//	    	}
//	    	else if(R.drawable.bulb_poi == overlay.getMarker()) {
//	    		overlay.setEnabled(MapDisplayActivity.isPredictDestEnabled(LandingActivity2.this));
//	    		bulbOverlay.add(overlay);
//	    	}
//	    	else {
//	    		otherFavOverlay.add(overlay);
//	    	}
//    	}
//    	
//    	for(Overlay cur : mapOverlays) {
//    		if(cur instanceof POIOverlay) {
//    			if(R.drawable.home == ((POIOverlay)cur).getMarker()) {
//    				homeOverlay.add(cur);
//    			}
//    			else if(R.drawable.work == ((POIOverlay)cur).getMarker()) {
//    				workOverlay.add(cur);
//    			}
//    			else if(R.drawable.bulb_poi == ((POIOverlay)cur).getMarker()) {
//    	    		bulbOverlay.add(cur);
//    	    	}
//    			else {
//    				otherFavOverlay.add(cur);
//    			}
//    		}
//    		else if(cur instanceof CurrentLocationOverlay) {
//    			currentLocationOverlay = cur;
//    		}
//    		else {
//    			otherOverlay.add(cur);
//    		}
//    	}
//    	
//    	mapOverlays.clear();
//    	mapOverlays.addAll(otherOverlay);
//    	mapOverlays.addAll(bulbOverlay);
//    	if(currentLocationOverlay != null) {
//    		mapOverlays.add(currentLocationOverlay);
//    	}
//    	mapOverlays.addAll(otherFavOverlay);
//    	mapOverlays.addAll(workOverlay);
//    	mapOverlays.addAll(homeOverlay);
//    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(tripInfoCachedUpdater);
        unregisterReceiver(updateMyLocation);
        unregisterReceiver(updateMenuMyTrips);
        closeGPS();
        SKMaps.getInstance().destroySKMaps();
    }
    
    private AtomicBoolean disableRefreshTripInfo = new AtomicBoolean(false);
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        if(requestCode == -1) {
            finish();
        }
        
        Bundle extras = intent == null ? null : intent.getExtras();
        
        if(requestCode == ON_MY_WAY && resultCode == Activity.RESULT_OK) {
        	final String emails = extras.getString(ValidationActivity.EMAILS);
        	final String phones = extras.getString(ValidationActivity.PHONES);
        	Reservation reserv = getFirstReservation();
        	if(reserv != null) {
        		DebugOptionsActivity.addRecipientsOfReserv(LandingActivity2.this, reserv.getRid(), emails, phones);
        	}
        }
        else if(requestCode == FavoriteOperationActivity.FAVORITE_OPT && resultCode == Activity.RESULT_OK) {
        	hideTripInfoPanel();
        	disableRefreshTripInfo.set(true);
        	String optType = extras.getString(FavoriteOperationActivity.FAVORITE_OPT_TYPE);
        	if(FavoriteOperationActivity.FAVORITE_DELETE.equals(optType)) {
        		Integer deleteUniqueId = extras.getInt(FavoriteOperationActivity.FAVORITE_POI_UNIQUE_ID);
        		mapView.deleteAnnotation(deleteUniqueId);
        	}
        	else {
        		if(FavoriteOperationActivity.FAVORITE_UPDATE.equals(optType)) {
        			PoiOverlayInfo updatePoi = extras.getParcelable(FavoriteOperationActivity.FAVORITE_POI_INFO);
        			com.metropia.models.Address oldInfo = afterUpdateUse.get(updatePoi.id);
        			if(oldInfo != null) {
	        			oldInfo.setAddress(updatePoi.address);
	        			oldInfo.setLatitude(updatePoi.lat);
	        			oldInfo.setLongitude(updatePoi.lon);
	        			oldInfo.setName(updatePoi.label);
	        			oldInfo.setIconName(updatePoi.iconName);
	        			afterUpdateUse.put(updatePoi.id, oldInfo);
	        			Collection<com.metropia.models.Address> temp = new ArrayList<com.metropia.models.Address>();
	        			temp.addAll(afterUpdateUse.values());
	        			initFavoriteDropdownIfNessary(temp, true);
        			}
        			poiContainer.updateExistedPOIByPoiId(updatePoi.id, updatePoi);
        		}
        		removePOIMarker();
        	}
        }
        else if(requestCode == REQUEST_CHECK_SETTINGS) {
        	if(resultCode != Activity.RESULT_OK) requestingLocationUpdates = false;
        	startLocationUpdates();
        }
    }
    
    public void openMenu(DrawerLayout drawer) {
    	View menuNotification = findViewById(R.id.menu_notification);
    	if(menuNotification.getVisibility() == View.VISIBLE) {
    		if(StringUtils.isNotBlank(inboxCityName)) {
    			DebugOptionsActivity.setInboxMenuDismissRecord(this, inboxCityName, DebugOptionsActivity.getInboxLastVisitFeedTime(this, inboxCityName));
    		}
    		menuNotification.setVisibility(View.GONE);
    	}
    	drawer.openDrawer(findViewById(R.id.left_drawer));
    }
    
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
            	if(findViewById(R.id.reservations_list_view).getVisibility()!=View.VISIBLE) {
	                final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	                View drawer = findViewById(R.id.left_drawer);
	                if(mDrawerLayout.isDrawerOpen(drawer)){
	                    mDrawerLayout.closeDrawer(drawer);
	                }else{
	                    openMenu(mDrawerLayout);
	                }
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
            	if(findViewById(R.id.loading_panel).getVisibility() == View.VISIBLE) {
            		findViewById(R.id.loading_panel).setVisibility(View.GONE);
            		cancelGetRoute.set(true);
            		return true;
            	}
            	
            	if(isPopupMenuShown()) {
            		hidePopupMenu();
            		return true;
            	}
            	else if(searchBox.isFocused() || fromSearchBox.isFocused()) {
            		searchBox.clearFocus();
            		fromSearchBox.clearFocus();
            		return true;
            	}
            	else if(isFromOrToSetted()) {
            		removeAllOD();
            		return true;
            	}
            	else if(!isReservationInfoShown() && hasReservTrip()) {
            		showTripInfoPanel(true, true);
            		return true;
            	}
            	else {
            		confirmExit();
            		return true;
            	}
        }
        return super.onKeyDown(keycode, e);
    }
    
    private void confirmExit() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.PopUpDialog)).setCancelable(false).setTitle("Really quit?");
    	builder.setMessage("Do you really want to exit the app?");
    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			finish();
    		}
    	});
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {}
    	});
    	builder.create().show();
    	
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
            	LocalyticsUtils.tagAppError(LocalyticsUtils.NETWORK_ERROR);
            }finally{
                IOUtils.closeQuietly(is);
            }
            return rs;
        }
        
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    float[] mGravity;
    float[] mGeomagnetic;
    
    @Override
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
//                float azimut = Double.valueOf(Math.toDegrees(orientation[0])).floatValue();
                if(myPoint != null){
//                    myPointOverlay.setDegrees(azimut);
//                    MapView mapView = (MapView)findViewById(R.id.mapview);
//                    mapView.postInvalidate();
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
        
        String versionNumber = "";
        
        ReservationTollHovInfo reservInfo;
        
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
        		long rescheduleId, String versionNumber, ExceptionHandlingService ehs, ReservationTollHovInfo info){
            this.ehs = ehs;
            this.ctx = ctx;
            this.activity = ctx;
            this.origin = origin;
            this.originAddress = originAddress;
            this.address = destAddress;
            this.id = rescheduleId;
            dialog = new CancelableProgressDialog(ctx, "Loading...");
            this.versionNumber = versionNumber;
            this.reservInfo = info;
        }
        
        @Override
        protected void onPreExecute() {
            dialog.show();
            if(this._route == null && origin == null){
                final LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    SystemService.alertNoGPS(activity, true, new SystemService.Callback() {
                        @Override
                        public void onNo() {
                            if (dialog.isShowing()) {
                                dialog.cancel();
                            }
                        }
                    });
                }
                android.location.LocationListener locationListener = new android.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        try{
                            locationManager.removeUpdates(this);
                            dialog.dismiss();
                            origin = new GeoPoint(location.getLatitude(), location.getLongitude());
                        }catch(Throwable t){}
                    }
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override
                    public void onProviderEnabled(String provider) {}
                    @Override
                    public void onProviderDisabled(String provider) {}
                };
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            if(this._route == null && dest == null){
                try {
                	dest = Geocoding.lookup(ctx, address, origin.getLatitude(), origin.getLongitude()).get(0).getGeoPoint();
                    GeoPoint curLoc = DebugOptionsActivity.getCurrentLocationLatLon(ctx);
                    if(curLoc != null){
                        origin = curLoc;
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
                                    0, 0, originAddress, address, reservInfo.isIncludeToll(), versionNumber, reservInfo.isHov());
                                route = routeReq.execute(ctx).get(0);
                                route.setAddresses(originAddress, address);
                                route.setUserId(user.getId());
                            }else{
                               route = _route; 
                            }
                            ReservationRequest reservReq = new ReservationRequest(user, 
                                route, ctx.getString(R.string.distribution_date), id);
                            reservReq.execute(ctx);
                            ReservationListFetchRequest reservListReq = new ReservationListFetchRequest(user);
                            reservListReq.invalidateCache(ctx);
                            List<Reservation> reservs = reservListReq.execute(ctx);
                            for (Reservation r : reservs) {
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

	@Override
	public void onActionPan() {}

	@Override
	public void onActionZoom() {}

	@Override
	public void onAnnotationSelected(SKAnnotation annotation) {
		if(mapView.getZoomLevel() >= POIOVERLAY_HIDE_ZOOM_LEVEL) {
			PoiOverlayInfo poiInfo;
			if(annotation.getUniqueID() == POI_MARKER_ONE || annotation.getUniqueID() == POI_MARKER_TWO || 
					annotation.getUniqueID() == POI_MARKER_THREE) {
				poiInfo = getPoiOverlayInfoFromCurrentOD(annotation.getUniqueID());
			}
			else if(annotation.getUniqueID() == FROM_BALLOON_ID && curFrom != null && StringUtils.isNotBlank(curFrom.address)) {
				poiInfo = curFrom;
			}
			else if(annotation.getUniqueID() == TO_BALLOON_ID) {
				poiInfo = curTo;
			}
			else {
				poiInfo = poiContainer.getExistedPOIByUniqueId(annotation.getUniqueID());
			}
			if(poiInfo != null) {
	        	Screen xy = getPopupFavIconPosition();
	            showPopupMenu(xy, poiInfo);
	            mapView.centerMapOnPositionSmooth(getCenterGeoPointByMapSize(poiInfo.lat, poiInfo.lon), MAP_ANIMATION_DURATION);
			}
		}
	}
	
	private PoiOverlayInfo getPoiOverlayInfoFromCurrentOD(Integer uniqueId) {
		if(curFrom != null && uniqueId.equals(curFrom.uniqueId)) {
			return curFrom;
		}
		else if(curTo != null && uniqueId.equals(curTo.uniqueId)) {
			return curTo;
		}
		return null;
	}

	@Override
	public void onCompassSelected() {}

	@Override
	public void onCurrentPositionSelected() {}

	@Override
	public void onCustomPOISelected(SKMapCustomPOI arg0) {}

//	@Override
//	public void onDebugInfo(double arg0, float arg1, double arg2) {}

	@Override
	public void onDoubleTap(SKScreenPoint arg0) {}

	@Override
	public void onInternationalisationCalled(int arg0) {}

	@Override
	public void onInternetConnectionNeeded() {}
	
	private static final Integer MAP_ANIMATION_DURATION = Integer.valueOf(500);

	@Override
	public void onLongPress(SKScreenPoint screenPoint) {
		if (restrictedMode) return;
		SKCoordinate coordinate = mapView.pointToCoordinate(screenPoint);
    	Screen xy = getPopupFavIconPosition();
        PoiOverlayInfo marker = refreshPOIMarker(coordinate.getLatitude(), coordinate.getLongitude(), LOADING_ADDRESS, "");
        showPopupMenu(xy, marker);
        mapView.centerMapOnPositionSmooth(getCenterGeoPointByMapSize(coordinate.getLatitude(), coordinate.getLongitude()), MAP_ANIMATION_DURATION);
        ReverseGeocodingTask task = new ReverseGeocodingTask(LandingActivity2.this, coordinate.getLatitude(), coordinate.getLongitude()){
            @Override
            protected void onPostExecute(String result) {
            	updatePopupMenu(result);
            }
        };
        Misc.parallelExecute(task);
	}

	@Override
	public void onMapActionDown(SKScreenPoint arg0) {}

	@Override
	public void onMapActionUp(SKScreenPoint arg0) {}

	@Override
	public void onMapPOISelected(SKMapPOI arg0) {}

	@Override
	public void onMapRegionChangeEnded(SKCoordinateRegion arg0) {}

	@Override
	public void onMapRegionChangeStarted(SKCoordinateRegion arg0) {}

	@Override
	public void onMapRegionChanged(SKCoordinateRegion arg0) {
		if(mapView != null) {
			updateAnnotationSize(getSizeRatioByZoomLevel());
		}
	}

	@Override
	public void onObjectSelected(int arg0) {}

//	@Override
//	public void onOffportRequestCompleted(int arg0) {}

	@Override
	public void onPOIClusterSelected(SKPOICluster arg0) {}

	@Override
	public void onRotateMap() {}

//	@Override
//	public void onScreenOrientationChanged() {}

	@Override
	public void onSingleTap(SKScreenPoint arg0) {
		if (restrictedMode) return;
		boolean hasFocus = searchBox.isFocused() || fromSearchBox.isFocused();
        boolean hasFavoriteDropDown = DROP_STATE.equals(fromDropDownButton.getTag()) || 
         		DROP_STATE.equals(toDropDownButton.getTag());
        if(hasFavoriteDropDown) {
          	if(DROP_STATE.equals(fromDropDownButton.getTag())) {
           		fromDropDownButton.performClick();
           	}
           	else {
           		toDropDownButton.performClick();
           	}
        }
            
        if(hasFocus) {
          	searchBox.clearFocus();
           	fromSearchBox.clearFocus();
        }
        if(!hasFocus && !hasFavoriteDropDown){
            resizeMap(!isMapCollapsed());
        }
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.d("LandingActivity2", "google api client connection failed!");
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.d("LandingActivity2", "google api client connected!");
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Log.d("LandingActivity2", "google api client connection suspended");
	}

	@Override
	public void onResult(LocationSettingsResult locationSettingsResult) {
		final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
            	startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i("LandingActivity2", "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(LandingActivity2.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i("LandingActivity2", "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
//                if(googleApiClient != null) {
//                	googleApiClient.disconnect();
//                	googleApiClient = null;
//                }
//                prepareGPS();
                startLocationUpdates();
                break;
        }
	}

	@Override
	public void onBoundingBoxImageRendered(int arg0) {}

	@Override
	public void onGLInitializationError(String arg0) {}

	@Override
	public void onSurfaceCreated(SKMapViewHolder mapViewHolder) {
		initSKMaps(mapViewHolder);
		prepareGPS();
		mapRefresh.set(true);
		enableDrawRoute.set(true);
		
		Runnable r;
		while((r = mapActionQueue.poll()) != null) r.run();
        
        //redraw poi
        sizeRatio.set(0);
        updateAnnotationSize(getSizeRatioByZoomLevel());
        //
        User.initializeIfNeccessary(LandingActivity2.this, new Runnable() {
			@Override
			public void run() {
				drawedReservId = Long.valueOf(-1);
				if(!disableRefreshTripInfo.get()) {
					dismissReservId = Long.valueOf(-1);
					refreshTripsInfo();
				}
		        updateMyMetropiaInfo();
		        if(!mapRecenter.get()) {
		        	centerMap();
		        }
			}
        });
		GeoPoint debugLoc = DebugOptionsActivity.getCurrentLocationLatLon(LandingActivity2.this);
		mapRecenter.set(true);
        if(debugLoc != null) {
        	Location loc = new Location("");
        	loc.setLatitude(debugLoc.getLatitude());
        	loc.setLongitude(debugLoc.getLongitude());
        	loc.setTime(System.currentTimeMillis() - ValidationActivity.TWO_MINUTES);
        	locationChanged(loc);
        }
        else {
			LocationInfo cacheLoc = new LocationInfo(LandingActivity2.this);
	        if(System.currentTimeMillis() - cacheLoc.lastLocationUpdateTimestamp <= ONE_HOUR) {
	        	Location loc = new Location("");
	        	loc.setLatitude(cacheLoc.lastLat);
	        	loc.setLongitude(cacheLoc.lastLong);
	        	loc.setTime(cacheLoc.lastLocationUpdateTimestamp - ValidationActivity.TWO_MINUTES);
	        	loc.setAccuracy(cacheLoc.lastAccuracy);
	        	loc.setBearing(cacheLoc.lastHeading);
	        	locationChanged(loc);
	        }
        }
	}

	@Override
	public void onDebugInfo(double arg0, float arg1, double arg2) {}
	
	public static boolean restrictedMode = false;
	public void restrictedMode(final boolean mode) {
		int visibility = mode? View.GONE:View.VISIBLE;
		int padding = mode? 5:60;
		int drawerLock = mode? DrawerLayout.LOCK_MODE_LOCKED_CLOSED:DrawerLayout.LOCK_MODE_UNLOCKED;
		disableShowPassengerMode.set(mode || disableShowPassengerMode.get());
		
		findViewById(R.id.passenger_mode_icon).setVisibility(getRouteView.getVisibility()==View.VISIBLE||disableShowPassengerMode.get()? View.GONE:View.VISIBLE);
		findViewById(R.id.landing_panel).setVisibility(visibility);
		findViewById(R.id.my_metropia_panel).setVisibility(visibility);
		((RelativeLayout.LayoutParams)findViewById(R.id.center_map_icon).getLayoutParams()).setMargins(0, 0, Dimension.pxToDp(20, getResources().getDisplayMetrics()), Dimension.pxToDp(padding, getResources().getDisplayMetrics()));
		((DrawerLayout) findViewById(R.id.drawer_layout)).setDrawerLockMode(drawerLock);
		
		Runnable r = new Runnable() {
			public void run() {
				mapView.getMapSettings().setCurrentPositionShown(!mode);
			}
		};
		if (mapView!=null) r.run();
		else mapActionQueue.add(r);
		
	}

}

