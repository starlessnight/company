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
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
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
import android.graphics.Point;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.smartrek.activities.LandingActivity2.FavoriteSlideFragment.ClickCallback;
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
import com.smartrek.ui.SwipeDeleteTouchListener;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.ui.overlays.CurrentLocationOverlay;
import com.smartrek.ui.overlays.EventOverlay;
import com.smartrek.ui.overlays.OverlayCallback;
import com.smartrek.ui.overlays.POIOverlay;
import com.smartrek.ui.overlays.RouteDestinationOverlay;
import com.smartrek.ui.overlays.RoutePathOverlay;
import com.smartrek.ui.timelayout.AdjustableTime;
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
    private List<Address> inputAddresses = new ArrayList<Address>();
    private List<Address> favoriteAddresses = new ArrayList<Address>();
    
    private AtomicBoolean canDrawReservRoute = new AtomicBoolean();
    
    private ListView searchResultList;
    private ListView fromSearchResultList;
    private ListView fromFavoriteDropdown;
    private ListView toFavoriteDropdown;
    
    private ArrayAdapter<Address> autoCompleteAdapter;
    private ArrayAdapter<Address> fromAutoCompleteAdapter;
    private ArrayAdapter<Address> fromFavoriteAutoCompleteAdapter;
    private ArrayAdapter<Address> toFavoriteAutoCompleteAdapter;
    
    private static final String NO_AUTOCOMPLETE_RESULT = "No results found.";
    
    private static final String SEARCHING = "Searching...";
    
    private static final String TAP_TO_ADD_FAVORITE = "Tap to Add Favorite";
    
    private EditText favSearchBox;
    
    private View favOptPanel;
    private ImageView labelIcon;
    
    private AtomicBoolean showAutoComplete = new AtomicBoolean(true);
    
//    private ListView reservationListView;
//    private ArrayAdapter<Reservation> reservationAdapter;
    
    private ImageView tripNotifyIcon;
    
    private TextView getRouteView;
    
    private POIOverlay curFrom;
    private POIOverlay curTo;
    
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
        fromFavoriteDropdown = (ListView) findViewById(R.id.from_favorite_drop_down);
        toFavoriteDropdown = (ListView) findViewById(R.id.to_favorite_drop_down);
        searchBox = (EditText) findViewById(R.id.search_box);
        searchBox.setHint(Html.fromHtml("<b>Enter Destination</b>"));
        fromSearchBox = (EditText) findViewById(R.id.from_search_box);
        fromSearchBox.setHint(Html.fromHtml("<b>Current Location</b>"));
        autoCompleteAdapter = createAutoCompleteAdapter(searchBox);
        fromAutoCompleteAdapter = createAutoCompleteAdapter(fromSearchBox);
        fromFavoriteAutoCompleteAdapter = createAutoCompleteAdapter(fromSearchBox);
        toFavoriteAutoCompleteAdapter = createAutoCompleteAdapter(searchBox);
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
        
//        fromMask.setOnClickListener(noopClick);
//        toMask.setOnClickListener(noopClick);
        
        refreshSearchAutoCompleteData();
        refreshFromSearchAutoCompleteData();
        
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
            	else if(TAP_TO_ADD_FAVORITE.equals(selected.getName())) {
            		clearSearchResult();
            		removePOIMarker(mapView);
            		hideBulbBalloon();
            		hideStarredBalloon();
            		showFavoriteOptPanel(null);
            	}
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
                else if(TAP_TO_ADD_FAVORITE.equals(selected.getName())) {
                    clearFromSearchResult();
                    removePOIMarker(mapView);
                    hideBulbBalloon();
                    hideStarredBalloon();
                    showFavoriteOptPanel(null);
                }
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
					removeOldOD(mapView, false);
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
		}, 500);
        
        searchBox.addTextChangedListener(delayTextWatcher);
        
        searchBoxClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	removeOldOD(mapView, false);
                searchBox.setText("");
                toIcon.setImageResource(0);
                toIcon.setVisibility(View.INVISIBLE);
                clearSearchResult();
                if(curFrom != null && StringUtils.isBlank(curFrom.getAddress())) {
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
                	removeOldOD(mapView, true);
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
        }, 500);
        
        fromSearchBox.addTextChangedListener(fromDelayTextWatcher);
        fromSearchBoxClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	removeOldOD(mapView, true);
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
                                    	refreshInputAddresses();
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
        
        favOptPanel = findViewById(R.id.fav_opt);
        favSearchBox = (EditText) favOptPanel.findViewById(R.id.favorite_search_box);
        
        final View favSave = favOptPanel.findViewById(R.id.fav_save);
        
        final EditText labelInput = (EditText) favOptPanel.findViewById(R.id.label_input);
        
        favOptPanel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				labelInput.clearFocus();
			}
        });
        
        final View labelInputClear = favOptPanel.findViewById(R.id.label_clear);
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
        
        labelInput.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus) {
					InputMethodManager imm = (InputMethodManager)getSystemService(
							Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
			}
		});
        
        labelInputClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				labelInput.setText("");
			}
		});
        
        findViewById(R.id.fav_cancel).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						hideFavoriteOptPanel();
					}
				});
			}
		});
        
        favSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation clickAnimation = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						if(isFavoriteOptComplete()) {
							InputMethodManager imm = (InputMethodManager)getSystemService(
		                            Context.INPUT_METHOD_SERVICE);
		                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
							PoiOverlayInfo info = (PoiOverlayInfo) favOptPanel.getTag();
							final PoiOverlayInfo _info = info==null ? new PoiOverlayInfo() : info;
							String label = ((EditText)favOptPanel.findViewById(R.id.label_input)).getText().toString();
							if(StringUtils.isBlank(label)) {
								label = "Favorite";
							}
							final String lbl = label;
			                final String addr = ((EditText)favOptPanel.findViewById(R.id.favorite_search_box)).getText().toString();
			                final FavoriteIcon icon = (FavoriteIcon) favOptPanel.findViewById(R.id.icon).getTag();
			                AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>(){
			                	@Override
			                	protected void onPreExecute() {
			                		if(_info.lat == 0 && _info.lon == 0) {
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
			                				_info.address = found.getAddress();
			                				_info.lat = found.getLatitude();
			                				_info.lon = found.getLongitude();
			                				_info.geopoint = found.getGeoPoint();
			                			}
			                		}
			                		else {
			                			_info.address = addr;
			                		}
			                	}
			                	
			                    @Override
			                    protected Integer doInBackground(Void... params) {
			                        Integer id = 0;
			                        Request req = null;
			                        String iconName = icon != null ? icon.name() : FavoriteIcon.star.name();
			                        User user = User.getCurrentUser(LandingActivity2.this);
			                        try {
			                        	if(_info.id==0) {
				                            FavoriteAddressAddRequest request = new FavoriteAddressAddRequest(
				                                user, lbl, _info.address, iconName, _info.lat, _info.lon);
				                            req = request;
				                            id = request.execute(LandingActivity2.this);
			                        	}
			                        	else {
			                        		FavoriteAddressUpdateRequest request = new FavoriteAddressUpdateRequest(
				                                    new AddressLinkRequest(user).execute(LandingActivity2.this),
				                                        _info.id, user, lbl, addr, iconName, _info.lat, _info.lon);
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
			                            _info.id = _info.id !=0 ? _info.id : id;
			                            hideFavoriteOptPanel();
			                        }
			                    }
			               };
			               Misc.parallelExecute(task);
						}
						v.setClickable(true);
					}
				});
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
        
        labelIcon = (ImageView) findViewById(R.id.label_icon);
        
        findViewById(R.id.confirm_panel).setOnClickListener(noopClick);
        
        findViewById(R.id.confirm_del).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation  = new ClickAnimation(LandingActivity2.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						findViewById(R.id.confirm_panel).setVisibility(View.GONE);
						final PoiOverlayInfo info = (PoiOverlayInfo) favOptPanel.getTag();
						final int oldId = info.id;
		                AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>(){
		                    @Override
		                    protected void onPreExecute() {
		                        List<Overlay> overlays = mapView.getOverlays();
		                        List<Overlay> overlaysToKeep = new ArrayList<Overlay>();
		                        for (Overlay overlay : overlays) {
		                            boolean toKeep;
		                            if(overlay instanceof POIOverlay){
		                                POIOverlay poiOverlay = (POIOverlay)overlay;
		                                toKeep = !isFavoriteMark(poiOverlay.getMarker()) || poiOverlay.getAid() != info.id;
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
		                        info.id = 0;
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
		                    	favOptPanel.setTag(null);
		                        refreshStarredPOIs();
		                        if (ehs.hasExceptions()) {
		                            ehs.reportExceptions();
		                        }
		                    }
		               };
		               Misc.parallelExecute(task);
		               hideFavoriteOptPanel();
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
        View drawerIconPanel = findViewById(R.id.drawer_menu_icon_panel);
        drawerIconPanel.setOnClickListener(new View.OnClickListener() {
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
						mDrawerLayout.closeDrawer(findViewById(R.id.left_drawer));
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
						startRouteActivity(mapView);
					}
				});
			}
        });
        toggleGetRouteButton(false);
        
        DebugOptionsActivity.cleanMapTileCacheIfNessary(LandingActivity2.this);
        
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
        
        addressInfo = (TextView) findViewById(R.id.address_info);
        poiIcon = (ImageView) findViewById(R.id.poi_icon);
        
        editMenu = (ImageView) findViewById(R.id.edit_menu);
        editMenu.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(final View v) {
        		v.setClickable(false);
        		final Integer[] resourceIds = (Integer[]) editMenu.getTag();
        		editMenu.setImageResource(resourceIds[0]);
        		ClickAnimation clickAni = new ClickAnimation(LandingActivity2.this, v);
//        		clickAni.setAnimationId(R.anim.menu_click_animation);
        		clickAni.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						editMenu.setImageResource(resourceIds[1]);
						POIOverlay overlay = (POIOverlay) popupPanel.getTag();
						showFavoriteOptPanel(overlay.getPoiOverlayInfo());
						hidePopupMenu();
						v.setClickable(true);
						
					}
        		});
        	}
        });
        
        toMenu = (ImageView) findViewById(R.id.to_menu);
        toMenu.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(final View v) {
				v.setClickable(false);
				toMenu.setImageResource(R.drawable.selected_to_menu);
				ClickAnimation clickAni = new ClickAnimation(LandingActivity2.this, v);
//				clickAni.setAnimationId(R.anim.menu_click_animation);
				clickAni.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						toMenu.setImageResource(R.drawable.to_menu);
						setMenuInfo2Searchbox((POIOverlay)popupPanel.getTag(), false);
						hidePopupMenu();
						v.setClickable(true);
					}
				});
			}
        });
        
        fromMenu = (ImageView) findViewById(R.id.from_menu);
        fromMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				fromMenu.setImageResource(R.drawable.selected_from_menu);
				ClickAnimation clickAni = new ClickAnimation(LandingActivity2.this, v);
//				clickAni.setAnimationId(R.anim.menu_click_animation);
				clickAni.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						fromMenu.setImageResource(R.drawable.from_menu);
						setMenuInfo2Searchbox((POIOverlay)popupPanel.getTag(), true);
						hidePopupMenu();
						v.setClickable(true);
					}
				});
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
        
        initFavoritePage();
        
        AssetManager assets = getAssets();
        Font.setTypeface(Font.getLight(assets), osmCredit, searchBox, fromSearchBox, myMetropiaMenu, 
            reservationsMenu, shareMenu, feedbackMenu, rewardsMenu, settingsMenu, userInfoView, myTripsMenu);
        Font.setTypeface(Font.getMedium(assets), favSearchBox, labelInput, 
        		(TextView)findViewById(R.id.label), (TextView)findViewById(R.id.icon), getRouteView, 
        		upointView, saveTimeView, co2View, (TextView) findViewById(R.id.head), 
        		(TextView) findViewById(R.id.favorite_address_desc));
        //init Tracker
        ((SmarTrekApplication)getApplication()).getTracker(TrackerName.APP_TRACKER);
        showTutorialIfNessary();
        
        //end oncreate
    }
    
    private void setMenuInfo2Searchbox(POIOverlay overlay, boolean from) {
    	if(overlay != null) {
    		MapView mapView = (MapView) findViewById(R.id.mapview);
            handleOD(mapView, overlay, from);
    	}
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
						mDrawerLayout.openDrawer(findViewById(R.id.left_drawer));
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
						hideTripInfoPanel();
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
						hideTripInfoPanel();
					}
				});
			}
    	});
    	
    	newUserTipView = findViewById(R.id.new_user_tip);
    	newUserTipView.setVisibility(DebugOptionsActivity.isUserCloseTip(LandingActivity2.this)?View.GONE:View.VISIBLE);
    	
    	findViewById(R.id.tip_close).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newUserTipView.setVisibility(View.GONE);
				DebugOptionsActivity.userCloseTip(LandingActivity2.this);
			}
    	});
    	
    	SwipeDeleteTouchListener touchListener =
                new SwipeDeleteTouchListener(reservationListPanel, 
                        new SwipeDeleteTouchListener.OnDismissCallback() {
                            @Override
                            public void onDismiss(View tripInfoView, final List<Reservation> reservs) {
                                final Reservation reserv = (Reservation) tripInfoView.getTag();
                                AsyncTask<Void, Void, Boolean> delTask = new AsyncTask<Void, Void, Boolean>(){
                                	@Override
                                	protected void onPreExecute() {
                                		dismissReservId = reserv.getRid();
                                		refreshTripInfoPanel(reservs);
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
            		                   		refreshTripsInfo(false);
            		                   	}
            		                 	dismissReservId = -1L;
            		                }
            		            };
            		            Misc.parallelExecute(delTask);
                            }

							@Override
							public void onDismissRight() {
							}

                         });
    	reservationListPanel.setOnTouchListener(touchListener);
//        Font.setTypeface(boldFont, (TextView) findViewById(R.id.no_reserved_trips));
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
        StringBuffer tripInfoDesc = new StringBuffer(originDesc).append(" TO: ").append(destDesc);
        if(reserv.isEligibleTrip()){
        	startTimeVisible = isFirst ? View.GONE : View.VISIBLE;
        	durationTimeVisible = View.VISIBLE;
            backgroundColor = isFirst ? R.color.metropia_green : R.color.metropia_blue;
            nextTripStartTime = StringUtils.replace(formatTime(departureTimeUtc, reserv.getRoute().getTimezoneOffset(), !isFirst), " ", "\n");
            arrivalTime = formatTime(System.currentTimeMillis() + durationTime*1000, reserv.getRoute().getTimezoneOffset(), false);
            arrivalTimeDesc = "Arrival: " + arrivalTime;
            durationTimeDesc = "Duration: " + getFormattedDuration(Long.valueOf(durationTime).intValue());
            startButtonResourceId = isFirst ? R.drawable.reservation_start_trip : R.drawable.reservation_start_trip_disable;
        }else {
        	startTimeVisible = View.VISIBLE;
        	durationTimeVisible = View.GONE;
            nextTripStartTime = StringUtils.replace(formatTime(departureTimeUtc, reserv.getRoute().getTimezoneOffset(), !isFirst), " ", "\n");
            backgroundColor = isFirst ? R.color.metropia_orange : R.color.metropia_blue;
            arrivalTimeDesc = "Arrival: " + arrivalTime;
            durationTimeDesc = "Duration: " + getFormattedDuration(Long.valueOf(durationTime).intValue());
            startButtonResourceId = isFirst ? R.drawable.reservation_start_trip_transparent : R.drawable.reservation_start_trip_disable;
        }
        
        TextView tripInfoDescView =  (TextView) reservInfo.findViewById(R.id.trip_info_desc);
    	tripInfoDescView.setVisibility(isFirst ? View.VISIBLE : View.GONE);
    	tripInfoDescView.setText(formatTripInfoDesc(tripInfoDesc.toString()));
    	TextView timeToGo = (TextView) reservInfo.findViewById(R.id.time_to_go_desc);
    	timeToGo.setText(startTimeVisible == View.GONE ? "It's Time to Go!" : "");
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
        tripStartTimeView.setText(formatStartTripTime(nextTripStartTime));
        ImageView startButton = (ImageView) reservInfo.findViewById(R.id.reservation_start_button);
        startButton.setImageResource(startButtonResourceId);
        reservInfo.findViewById(R.id.reservation_trip_times).setVisibility(isFirst?View.VISIBLE:View.GONE);
        reservInfo.findViewById(R.id.leave_label).setVisibility((isFirst && !reserv.isEligibleTrip())?View.VISIBLE:View.GONE);
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
				                            	Log.d("LandingActivity2", "Reschedule trip start");
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
        
        ImageView reservReschedule = (ImageView) reservInfo.findViewById(R.id.reservation_reschedule);
        reservReschedule.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
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
        
        Font.setTypeface(robotoBoldFont, timeToGo, tripDurationTimeView, tripArrivalTimeView, tripStartTimeView);
        Font.setTypeface(robotoLightFont, tripInfoDescView, reservationOnMyWay);
        
        if(isFirst) {
	        tripNotifyIcon.setImageResource(reserv.isEligibleTrip()?R.drawable.upcoming_trip_green:R.drawable.upcoming_trip_orange);
	        tripNotifyIcon.setVisibility(View.VISIBLE);
        }
        
        return reservInfo;
    }
    
    private boolean isFavoriteMark(int markResourceId) {
    	for(FavoriteIcon icon : FavoriteIcon.values()) {
    		if(markResourceId == icon.getResourceId(LandingActivity2.this)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private void reInitFavoriteOperationPanel() {
    	favOptPanel.setTag(null);
    	((EditText)favOptPanel.findViewById(R.id.favorite_search_box)).setText("");
    	((EditText)favOptPanel.findViewById(R.id.favorite_search_box)).setEnabled(true);
    	((EditText)favOptPanel.findViewById(R.id.label_input)).setText("");
    	favOptPanel.findViewById(R.id.label_clear).setVisibility(View.GONE);
    	favOptPanel.findViewById(R.id.fav_del_panel).setVisibility(View.GONE);
    	favOptPanel.findViewById(R.id.label_icon).setVisibility(View.GONE);
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
                	favIcon.setImageResource(R.drawable.poi_pin);
                	favIcon.setVisibility(View.VISIBLE);
                }
                else {
                	favIcon.setImageResource(icon.getResourceId(LandingActivity2.this));
                	favIcon.setVisibility(View.VISIBLE);
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
                favIcon.requestLayout();
//                view.setPadding(0, 0, 0, position == getCount() - 1 ? 
//                    Dimension.dpToPx(135, getResources().getDisplayMetrics()) : 0);
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
            protected void onPostExecute(Address addr) {
                if(addr != null){
                    dropPinForAddress(addr, zoomIn, isFrom);
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
    
    private void dropPinForAddress(Address addr, boolean zoomIn, boolean isFrom) {
    	GeoPoint gp = addr.getGeoPoint();
        DebugOptionsActivity.addRecentAddress(LandingActivity2.this, addr.getAddress());
        MapView mapView = (MapView) findViewById(R.id.mapview);
        POIOverlay poiOverlay = getPOIOverlayByAddress(mapView, addr.getAddress());
        if(poiOverlay != null) {
        	handleOD(mapView, poiOverlay, isFrom);
        }
        else {
        	POIOverlay poi = refreshPOIMarker(mapView, gp.getLatitude(), gp.getLongitude(), addr.getAddress(), addr.getName());
        	handleOD(mapView, poi, isFrom);
        }
        // record input address
        addInputAddress(addr);
        
        IMapController mc = mapView.getController();
        if(zoomIn){
            mc.setZoom(SEARCH_ZOOM_LEVEL);
        }
        mc.animateTo(gp);
        mapView.postInvalidate();
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
		}
    	
    	public static PoiOverlayInfo fromAddress(Context ctx, com.smartrek.models.Address address) {
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
    		poiInfo.marker = R.drawable.poi_pin;
    		poiInfo.markerWithShadow = R.drawable.poi_pin;
    		return poiInfo;
    	}
    	
    	public static PoiOverlayInfo fromCurrentLocation(CurrentLocationOverlay currentLoc) {
    		PoiOverlayInfo poiInfo = new PoiOverlayInfo();
    		poiInfo.lat = currentLoc.getLocation().getLatitude();
    		poiInfo.lon = currentLoc.getLocation().getLongitude();
    		poiInfo.geopoint = currentLoc.getLocation();
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
    
    public enum FavoriteIcon {
    	
    	home, work, office, star, schedule, friend, restaurant, fastfood, pencil, coffee, airport,  fruit, 
    	gift, place, pharmacy, guitar, music, repair_shop, football, magnifier, sunglasses, zoo, temperature, credit_card;
    	
        public static FavoriteIcon fromName(String name, FavoriteIcon failback) {
        	for(FavoriteIcon type : values()) {
        		if(type.name().equals(name)) {
        			return type;
        		}
        	}
        	return failback;
       	}
        	
       	public static Integer getIconResourceId(Context ctx, String name) {
       		return ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
      	}
       	
       	public Integer getResourceId(Context ctx) {
       		return getIconResourceId(ctx, name());
       	}
       	
       	public Integer getShadowResourceId(Context ctx) {
       		return getIconResourceId(ctx, name() + "_with_shadow");
       	}
       	
       	public Integer getFavoritePageResourceId(Context ctx) {
       		return getIconResourceId(ctx, "favorite_page_" + name());
       	}
       	
       	public static FavoriteIcon[][] getFirstPageIcons() {
       		return new FavoriteIcon[][] { {home, work, office, star}, {schedule, friend, restaurant, fastfood}, {pencil, coffee, airport, fruit} };
       	}
       	
       	public static FavoriteIcon[][] getSecondPageIcons() {
       		return new FavoriteIcon[][] { {gift, place, pharmacy, guitar}, {music, repair_shop, football, magnifier}, {sunglasses, zoo, temperature, credit_card} };
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
    				curFrom.markODPoi();
    				curFrom.setIsFromPoi(true);
    			}
    			else if(curTo!=null && ((curTo.getAid() != 0 && curTo.getAid() == poi.getAid()) 
    					|| (StringUtils.isNotBlank(curTo.getAddress()) && curTo.getAddress().equals(poi.getAddress())))) {
    				curTo = poi;
    				curTo.markODPoi();
    				curTo.setIsFromPoi(false);
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
        
        LandingActivity.initializeIfNeccessary(LandingActivity2.this, new Runnable() {
			@Override
			public void run() {
				refreshTripsInfo();
		        updateMyMetropiaInfo();
			}
        });
        
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        processHideOverlays();
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
    
    private void processHideOverlays() {
    	for(POIOverlay poi : hideOverlays) {
    		poi.hideBalloon();
    	}
    	hideOverlays.clear();
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
	                	for(Overlay remove : need2Remove) {
	                		if(remove instanceof RouteDestinationOverlay) {
	                			((RouteDestinationOverlay)remove).hideBalloon();
	                		}
	                	}
	                    mapOverlays.removeAll(need2Remove);
	                    mapView.postInvalidate();
	                }
	                tripNotifyIcon.setVisibility(View.GONE);
	                refreshTripInfoPanel(new ArrayList<Reservation>());
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
    	
    	if(reserv != null) {
	        tripNotifyIcon.setImageResource(reserv.isEligibleTrip()?R.drawable.upcoming_trip_green:R.drawable.upcoming_trip_orange);
	        tripNotifyIcon.setVisibility(View.VISIBLE);
    	}
    	refreshReservationList(reservations);
    }
    
    private SpannableString formatTripInfoDesc(String tripDesc) {
    	int indexOfChange = tripDesc.indexOf("TO:");
    	SpannableString tripDescSpan = SpannableString.valueOf(tripDesc);
    	if(indexOfChange != -1) {
    		tripDescSpan.setSpan(new AbsoluteSizeSpan(getResources()
					.getDimensionPixelSize(R.dimen.micro_font)), indexOfChange, indexOfChange + "TO:".length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    	}
		return tripDescSpan;
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
    
    private List<Long> removedReservIds = new ArrayList<Long>();
    
    private void refreshReservationList(List<Reservation> reservations) {
    	reservationListPanel.removeAllViews();
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
    		
    		showTripInfoPanel(false, false);
    	}
    	else {
    		dismissReservId = -1L;
    		tripNotifyIcon.setVisibility(View.GONE);
    	}
 
    	View centerDashLine = findViewById(R.id.white_center_dash_line);
    	centerDashLine.setVisibility(View.GONE);
    	int reservCount = curReservIdx == -1 ? 0 : reservations.size() - curReservIdx;
    	if((reservCount == 1 && !DebugOptionsActivity.isUserCloseTip(LandingActivity2.this)) || reservCount > 1) {
			findViewById(R.id.add_new_trip_background).setVisibility(View.GONE);
			if(reservCount > 1) {
				newUserTipView.setVisibility(View.GONE);
				centerDashLine.setVisibility(reservCount == 2 ? View.VISIBLE : View.GONE);
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
//    		lockMenu();
    		hideFavoriteOptPanel();
    	}
    }
    
    private void hideFavoriteOptPanel() {
    	reInitFavoriteOperationPanel();
    	favOptPanel.setVisibility(View.GONE);
		findViewById(R.id.landing_panel).setVisibility(View.VISIBLE);
		ViewPager favoriteIconPager = (ViewPager) findViewById(R.id.favorite_icons_pager);
		favoriteIconPager.setCurrentItem(0);
    }
    
    private void showFavoriteOptPanel(PoiOverlayInfo info) {
    	writeInfo2FavoritePanel(info);
    	findViewById(R.id.landing_panel).setVisibility(View.GONE);
    	favOptPanel.setVisibility(View.VISIBLE);
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
    		IMapController imc = mapView.getController();
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
				searchPOIAddress(address, true, _location, false);
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
                    Set<Address> addrList = new HashSet<Address>();
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
                            final PoiOverlayInfo poiInfo = PoiOverlayInfo.fromAddress(LandingActivity2.this, a);
                            final POIOverlay star = new POIOverlay(mapView, boldFont, poiInfo, HotspotPlace.CENTER, null);
                            star.setAid(a.getId());
                            star.setCallback(new OverlayCallback() {
                                @Override
                                public boolean onTap(int index) {
                                	mapView.getController().animateTo(new GeoPoint(poiInfo.lat, poiInfo.lon));
                                    Screen xy = getScreenXY(mapView, poiInfo.lat, poiInfo.lon);
                                    showPopupMenu(xy, star);
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
                            insertOverlayByOrderOrSort(overlays, star);
                            star.showOverlay();
                            addrList.add(Address.fromModelAddress(a, lastLocation));
                        }
                    }
                    showODBalloon();
                    handleFavoriteIconByZoomLevel(mapView);
                    mapView.postInvalidate();
//                    write2SearchBoxTag(addrList);
                    initFavoriteDropdownIfNessary(addrList);
                }
            }
            
        };
        Misc.parallelExecute(task);
    }
    
    private synchronized void initFavoriteDropdownIfNessary(Set<Address> favorites) {
    	if(favoriteAddresses.size() != favorites.size()) {
    		List<Address> newFavorites = new ArrayList<Address>();
    		List<Address> homeFavorite = new ArrayList<Address>();
    		List<Address> workFavorite = new ArrayList<Address>();
    		List<Address> otherFavorite = new ArrayList<Address>();
    		for(Address addr : favorites) {
    			if(FavoriteIcon.home.name().equals(addr.getIconName())) {
    				homeFavorite.add(addr);
    			}
    			else if(FavoriteIcon.work.name().equals(addr.getIconName())) {
    				workFavorite.add(addr);
    			}
    			else {
    				otherFavorite.add(addr);
    			}
    		}
    		Collections.sort(homeFavorite, DebugOptionsActivity.distanceComparator);
    		Collections.sort(workFavorite, DebugOptionsActivity.distanceComparator);
    		Collections.sort(otherFavorite, DebugOptionsActivity.distanceComparator);
    		newFavorites.addAll(homeFavorite);
    		newFavorites.addAll(workFavorite);
    		newFavorites.addAll(otherFavorite);
    		favoriteAddresses.clear();
    		favoriteAddresses.addAll(newFavorites);

    		fromFavoriteAutoCompleteAdapter.clear();
    		fromFavoriteAutoCompleteAdapter.addAll(favoriteAddresses);
    		toFavoriteAutoCompleteAdapter.clear();
    		toFavoriteAutoCompleteAdapter.addAll(favoriteAddresses);
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
    
    private void handleOD(MapView mapView, POIOverlay poi, boolean isFrom) {
    	removeOldOD(mapView, isFrom);
    	poi.markODPoi();
    	poi.setIsFromPoi(isFrom);
    	PoiOverlayInfo info = poi.getPoiOverlayInfo();
    	if(isFrom) {
    		curFrom = poi;
    		curFromProvider = null;
    		curFromTime = 0;
    		setFromInfo(info);
    	}
    	else {
    		curTo = poi;
    		setToInfo(info);
    		if(curFrom == null && myPointOverlay != null) {
    			PoiOverlayInfo currentLocationInfo = PoiOverlayInfo.fromCurrentLocation(myPointOverlay);
    			curFrom = new POIOverlay(mapView, Font.getBold(getAssets()), currentLocationInfo, 
    					HotspotPlace.CENTER, null);
    			curFrom.markODPoi();
    			curFrom.setIsFromPoi(true);
    			mapView.getOverlays().add(curFrom);
    			setFromInfo(currentLocationInfo);
    			curFrom.showBalloonOverlay();
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
    	poi.showBalloonOverlay();
    }
    
    AtomicBoolean removeOD = new AtomicBoolean(true);
    
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
    private String cityName = CityRequest.NO_CITY_NAME;
    
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
                        cityName = result.name;
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
//                            write2SearchBoxTag(addrSet);
                            refreshSearchAutoCompleteData();
                        }
                    });
                }
            }
        };
        Misc.parallelExecute(task);
    }
    
    private static final Integer POIOVERLAY_HIDE_ZOOM_LEVEL = 5;
    private Set<POIOverlay> hideOverlays = new HashSet<POIOverlay>();
    
    private void bindMapFunctions(final MapView mapView){
        EventOverlay eventOverlay = new EventOverlay(this);
        eventOverlay.setActionListener(new EventOverlay.ActionListener() {
            @Override
            public void onLongPress(final double lat, final double lon) {
                ReverseGeocodingTask task = new ReverseGeocodingTask(LandingActivity2.this, lat, lon){
                    @Override
                    protected void onPostExecute(String result) {
                    	mapView.getController().animateTo(new GeoPoint(lat, lon));
                    	Screen xy = getScreenXY(mapView, lat, lon);
                        POIOverlay marker = refreshPOIMarker(mapView, lat, lon, result, "");
                        showPopupMenu(xy, marker);
                    }
                };
                Misc.parallelExecute(task);
            }
            @Override
            public void onSingleTap() {
            	if(favOptPanel.getVisibility() == View.GONE) {
//	                boolean handledStarred = hideStarredBalloon();
//	                boolean handledBulb = hideBulbBalloon();
//	                boolean handledPOI = removePOIMarker(mapView);
//	                boolean handledOD = removeOldOD(mapView, isFromPoi());
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
	                if(/*!handledStarred && !handledBulb && !handledPOI && !handledOD &&*/ !hasFocus && !hasFavoriteDropDown){
	                    resizeMap(!isMapCollapsed());
	                }
            	}
            }
        });
        mapView.getOverlays().add(eventOverlay);
        
        mapView.setMapListener(new MapListener() {
			@Override
			public boolean onScroll(ScrollEvent event) {
				return false;
			}

			@Override
			public boolean onZoom(ZoomEvent event) {
				handleFavoriteIconByZoomLevel(mapView);
				return false;
			}
		});
    }
    
    private void handleFavoriteIconByZoomLevel(MapView mapView) {
    	if(mapView.getZoomLevel() <= POIOVERLAY_HIDE_ZOOM_LEVEL) {
			List<Overlay> overlays = mapView.getOverlays();
			Set<POIOverlay> hideThisTime = new HashSet<POIOverlay>();
			for(Overlay overlay : overlays) {
				if(overlay instanceof POIOverlay) {
					POIOverlay poi = (POIOverlay)overlay;
					if(!hideOverlays.contains(poi)) {
						hideThisTime.add(poi);
					}
				}
			}
			
			for(POIOverlay poi : hideThisTime) {
				poi.hideOverlay();
			}
			hideOverlays.addAll(hideThisTime);
			mapView.postInvalidate();
		}
		else if(mapView.getZoomLevel() > POIOVERLAY_HIDE_ZOOM_LEVEL && hideOverlays.size() > 0){
			for(POIOverlay overlay : hideOverlays) {
				overlay.showOverlay();
			}
			hideOverlays.clear();
			insertOverlayByOrderOrSort(mapView.getOverlays(), null);
			mapView.postInvalidate();
		}
    }
    
	private void showPopupMenu(Screen xy, POIOverlay overlay) {
		PoiOverlayInfo info = overlay.getPoiOverlayInfo();
    	
    	poiIcon.setVisibility(View.VISIBLE);
    	poiIcon.setImageResource(overlay.getMarker());
    	
    	BitmapFactory.Options dimensions = new BitmapFactory.Options(); 
    	dimensions.inJustDecodeBounds = false;
    	Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), overlay.getMarker(), dimensions);
    	int poiIconWidth = decodeResource.getWidth();
    	int poiIconHeight = decodeResource.getHeight();
    	decodeResource.recycle();
    	
    	FrameLayout.LayoutParams poiLp = (android.widget.FrameLayout.LayoutParams) poiIcon.getLayoutParams();
    	poiLp.leftMargin = xy.x - (poiIconWidth / 2);
    	poiLp.topMargin = xy.y - (poiIconHeight / 2);
    	poiIcon.setLayoutParams(poiLp);
    	
    	editMenu.setVisibility(View.VISIBLE);
    	Integer[] imageResourceIds;
    	if(overlay.getAid() == 0) {
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
    	editMenuLp.leftMargin = xy.x - (editMenu.getMeasuredWidth() / 2);
    	editMenuLp.topMargin = xy.y - (editMenu.getMeasuredHeight() + poiIconHeight / 2 + Dimension.dpToPx(20, dm));
    	editMenu.setLayoutParams(editMenuLp);
    	
    	Screen corespondXY = new Screen();
    	corespondXY.x = 0;
    	corespondXY.y = (editMenu.getMeasuredHeight() / 2 + poiIconHeight / 2 + Dimension.dpToPx(20, dm));
    	
    	Screen fromXY = getRelativeCoorOfDegree(corespondXY, -65);
    	fromMenu.setVisibility(View.VISIBLE);
    	FrameLayout.LayoutParams fromMenuLp = (android.widget.FrameLayout.LayoutParams) fromMenu.getLayoutParams();
    	fromMenuLp.leftMargin = xy.x - fromXY.x - (editMenu.getMeasuredWidth() / 2);
    	fromMenuLp.topMargin = xy.y - fromXY.y - (editMenu.getMeasuredWidth() / 2);
    	fromMenu.setLayoutParams(fromMenuLp);
    	
    	toMenu.setVisibility(View.VISIBLE);
    	FrameLayout.LayoutParams toMenuLp = (android.widget.FrameLayout.LayoutParams) toMenu.getLayoutParams();
    	toMenuLp.leftMargin = xy.x + fromXY.x - (editMenu.getMeasuredWidth() / 2);
    	toMenuLp.topMargin = xy.y - fromXY.y - (editMenu.getMeasuredWidth() / 2);
    	toMenu.setLayoutParams(toMenuLp);
    	
    	addressInfo.setVisibility(View.VISIBLE);
    	addressInfo.setText(StringUtils.isNotBlank(info.label) ? info.label : info.address);
    	FrameLayout.LayoutParams addressInfoLp = (FrameLayout.LayoutParams) addressInfo.getLayoutParams();
    	View landingPanelView = findViewById(R.id.landing_panel_content);
        int landingPanelHeight = landingPanelView.getHeight();
    	int margin = Dimension.dpToPx(5, dm);
    	addressInfoLp.leftMargin = margin;
    	addressInfoLp.rightMargin = margin;
    	addressInfoLp.topMargin = isMapCollapsed() ? (margin + landingPanelHeight) : margin;
    	addressInfo.setLayoutParams(addressInfoLp);
    	
    	popupPanel.setTag(overlay);
    	popupPanel.setVisibility(View.VISIBLE);
    	popupPanel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MapView mapView = (MapView) findViewById(R.id.mapview);
				removePOIMarker(mapView);
				hidePopupMenu();
			}
        });
    }
	
	private void hidePopupMenu() {
		editMenu.setVisibility(View.INVISIBLE);
		fromMenu.setVisibility(View.INVISIBLE);
		toMenu.setVisibility(View.INVISIBLE);
		poiIcon.setVisibility(View.INVISIBLE);
		addressInfo.setText("");
		addressInfo.setVisibility(View.INVISIBLE);
		popupPanel.setTag(null);
		popupPanel.setVisibility(View.GONE);
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
    public Screen getScreenXY(MapView mapView, double lat, double lon) {
    	Screen widthHeight = getScreenWidthHeight(mapView);
//    	Point reuse = null;
//    	Projection projection = mapView.getProjection();
//    	IGeoPoint northEast = projection.getNorthEast();
//    	GeoPoint in = new GeoPoint(lat, lon);
//    	reuse = projection.toPixels(in, reuse);
//    	int x = reuse.x;
//    	int y = reuse.y;
//    	reuse = projection.toPixels(northEast, reuse);
//    	int nx = reuse.x;
//    	int ny = reuse.y;
    	Screen pointXY = new Screen();
    	pointXY.x = widthHeight.x / 2;
    	pointXY.y = widthHeight.y / 2 ;
//    	pointXY.x = widthHeight.x + x - nx;
//    	pointXY.y = y-ny;
    	return pointXY;
    }
    
    public Screen getScreenWidthHeight(MapView mapView) {
    	Point reuse = null;
    	Projection projection = mapView.getProjection();
    	IGeoPoint northEast = projection.getNorthEast();
    	reuse = projection.toPixels(northEast, reuse);
    	int nx = reuse.x;
    	int ny = reuse.y;
    	IGeoPoint southWest = projection.getSouthWest();
    	reuse = projection.toPixels(southWest, reuse);
    	int sx = reuse.x;
    	int sy = reuse.y;
    	Screen screen = new Screen();
    	screen.x = nx - sx;
    	screen.y = sy - ny;
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
    	public int x;
    	public int y;
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
        View compass = findViewById(R.id.center_map_icon);
        ObjectAnimator compassAnimator;
        if(collapsed) {
        	compassAnimator = ObjectAnimator.ofFloat(compass, "translationY", myMetropiaPanelHeight, 0);
        }
        else {
        	compassAnimator = ObjectAnimator.ofFloat(compass, "translationY", 0, myMetropiaPanelHeight);
        }
        
        View notifyTrip = findViewById(R.id.trip_notify_icon);
        ObjectAnimator notifyTripAnimator;
        if(collapsed) {
        	notifyTripAnimator = ObjectAnimator.ofFloat(notifyTrip, "translationY", myMetropiaPanelHeight, 0);
        }
        else {
        	notifyTripAnimator = ObjectAnimator.ofFloat(notifyTrip, "translationY", 0, myMetropiaPanelHeight);
        }
        
        ObjectAnimator getRouteAnimator;
        if(collapsed) {
        	getRouteAnimator = ObjectAnimator.ofFloat(getRouteView, "translationY", myMetropiaPanelHeight, 0);
        }
        else {
        	getRouteAnimator = ObjectAnimator.ofFloat(getRouteView, "translationY", 0, myMetropiaPanelHeight);
        }
        
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.play(landingPanelAnimator).with(myMetropiaPanelAnimator).with(compassAnimator).with(notifyTripAnimator).with(getRouteAnimator);
		animatorSet.start();
    }
    
    private void toggleGetRouteButton(boolean enabled) {
//    	getRouteView.setClickable(enabled);
//    	getRouteView.setBackgroundResource(enabled ? R.drawable.get_route_button : R.drawable.disabled_get_route_button);
//    	getRouteView.setTextColor(enabled ? getResources().getColor(android.R.color.white) : getResources().getColor(R.color.transparent_white));
//    	int padding = Dimension.dpToPx(5, getResources().getDisplayMetrics());
//    	getRouteView.setPadding(padding, 0, padding, 0);
    	getRouteView.setVisibility(enabled?View.VISIBLE:View.GONE);
    }
    
//    private void write2SearchBoxTag(Set<String> addresses) {
//    	Set<String> oAddrs = (Set<String>) findViewById(R.id.search_box).getTag();
//        if(oAddrs == null) {
//        	oAddrs = new HashSet<String>();
//        }
//        oAddrs.addAll(addresses);
//        findViewById(R.id.search_box).setTag(oAddrs);
//    }
    
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
    
    private boolean removePOIMarker(MapView mapView){
        boolean handled = false;
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if(overlay instanceof POIOverlay ){
            	POIOverlay curOverlay = (POIOverlay)overlay;
            	if(curOverlay.getMarker() == R.drawable.poi_pin && !isMarkedOD(curOverlay)) {
	            	Log.d("LandingActivity2", "removePOIMarker : " + curOverlay);
	                if(curOverlay.isBalloonVisible()){
	                    curOverlay.hideBalloon();
	                    handled = true;
	                }
	                overlays.remove(curOverlay);
	                mapView.postInvalidate();
	                break;
            	}
            }
        }
        return handled;
    }
    
    private boolean isMarkedOD(Overlay overlay) {
    	return overlay == curFrom || overlay == curTo;
    }
    
    private boolean removeAllOD() {
    	MapView mapView = (MapView) findViewById(R.id.mapview);
    	boolean handleFrom = removeOldOD(mapView, true);
    	boolean handleTo = removeOldOD(mapView, false);
    	cleanSearchBox();
    	return handleFrom || handleTo;
    }
    
    private void cleanSearchBox() {
    	searchBox.setText("");
		toIcon.setVisibility(View.INVISIBLE);
		clearSearchResult();
		fromSearchBox.setText("");
		fromIcon.setVisibility(View.INVISIBLE);
		clearFromSearchResult();
    }
    
    private boolean removeOldOD(MapView mapView, boolean from) {
    	List<Overlay> overlays = mapView.getOverlays();
    	if(from) {
    		fromIcon.setVisibility(View.INVISIBLE);
    		curFrom = null;
    	}
    	else {
    		toIcon.setVisibility(View.INVISIBLE);
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
	                if(poiOverlay.getMarker() == R.drawable.poi_pin || poiOverlay.getMarker() == R.drawable.transparent_poi) {
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
    
    private POIOverlay refreshPOIMarker(final MapView mapView, final double lat, final double lon,
            final String address, final String label){
        GeoPoint gp = new GeoPoint(lat, lon);
        BalloonModel model = new BalloonModel();
        model.lat = lat;
        model.lon = lon;
        model.address = address;
        model.label = label;
        model.geopoint = gp;
        final PoiOverlayInfo poiInfo = PoiOverlayInfo.fromBalloonModel(model);
        final POIOverlay marker = new POIOverlay(mapView, Font.getBold(getAssets()), poiInfo, 
        		HotspotPlace.CENTER , null);
        
        marker.setCallback(new OverlayCallback() {
            @Override
            public boolean onTap(int index) {
            	mapView.getController().animateTo(new GeoPoint(poiInfo.lat, poiInfo.lon));
            	Screen xy = getScreenXY(mapView, poiInfo.lat, poiInfo.lon);
                showPopupMenu(xy, marker);
                mapView.postInvalidate();
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
        mapView.postInvalidate();
        return marker;
    }
    
    private boolean isInFavoriteOperation() {
    	return favOptPanel.getVisibility() == View.VISIBLE;
    }
    
    private boolean isFavoriteOptComplete() {
    	String favAddr = ((EditText) favOptPanel.findViewById(R.id.favorite_search_box)).getText().toString();
//    	String label = ((EditText) favOptPanel.findViewById(R.id.label_input)).getText().toString();
//    	IconType icon = (IconType) favOptPanel.findViewById(R.id.icon).getTag();
//    	return StringUtils.isNotBlank(favAddr) && StringUtils.isNotBlank(label) && icon!=null;
    	return StringUtils.isNotBlank(favAddr);
    }
    
    private void writeInfo2FavoritePanel(PoiOverlayInfo info) {
    	if(info != null) {
	    	favOptPanel.setTag(info);
	    	((EditText) favOptPanel.findViewById(R.id.label_input)).setText(info.label);
	    	FavoriteIcon icon = FavoriteIcon.fromName(info.iconName, FavoriteIcon.star);
	    	if(icon != null) {
	    		favOptPanel.findViewById(R.id.icon).setTag(icon);
	    		labelIcon.setVisibility(View.VISIBLE);
	    		labelIcon.setImageResource(icon.getResourceId(LandingActivity2.this));
	    	}
	    	EditText favSearchBox = (EditText) favOptPanel.findViewById(R.id.favorite_search_box); 
	    	favSearchBox.setText(info.address);
	    	favSearchBox.setEnabled(false);
	    	favOptPanel.findViewById(R.id.fav_save).setVisibility(StringUtils.isNotBlank(info.address) ? View.VISIBLE : View.GONE);
	    	favOptPanel.findViewById(R.id.fav_del_panel).setVisibility(info.id!=0 ? View.VISIBLE : View.GONE);
	    	((TextView)favOptPanel.findViewById(R.id.header)).setText(info.id!=0 ? "Edit Favorite" : "Save Favorite");
    	}
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
            final PoiOverlayInfo poiInfo = PoiOverlayInfo.fromLocation(l);
            final POIOverlay bulb = new POIOverlay(mapView, boldFont, poiInfo, 
            		HotspotPlace.CENTER, null);
            bulb.setCallback(new OverlayCallback() {
                @Override
                public boolean onTap(int index) {
                	mapView.getController().animateTo(new GeoPoint(poiInfo.lat, poiInfo.lon));
                    Screen xy = getScreenXY(mapView, poiInfo.lat, poiInfo.lon);
                    showPopupMenu(xy, bulb);
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
            insertOverlayByOrderOrSort(overlays, bulb);
            bulb.showOverlay();
            showODBalloon();
        }
    }
    
    /**
     * insert overlay by the order, current location > home > work > fav
     * @param mapOverlays
     * @param overlay
     */
    private void insertOverlayByOrderOrSort(List<Overlay> mapOverlays, POIOverlay overlay) {
    	List<Overlay> homeOverlay = new ArrayList<Overlay>();
    	List<Overlay> workOverlay = new ArrayList<Overlay>();
    	List<Overlay> otherFavOverlay = new ArrayList<Overlay>();
    	List<Overlay> bulbOverlay = new ArrayList<Overlay>();
    	List<Overlay> otherOverlay = new ArrayList<Overlay>();
    	Overlay currentLocationOverlay = null;
    	if(overlay != null) {
	    	if(R.drawable.home == overlay.getMarker()) {
	    		homeOverlay.add(overlay);
	    	}
	    	else if(R.drawable.work == overlay.getMarker()){
	    		 workOverlay.add(overlay);
	    	}
	    	else if(R.drawable.bulb_poi == overlay.getMarker()) {
	    		bulbOverlay.add(overlay);
	    	}
	    	else {
	    		otherFavOverlay.add(overlay);
	    	}
    	}
    	
    	for(Overlay cur : mapOverlays) {
    		if(cur instanceof POIOverlay) {
    			if(R.drawable.home == ((POIOverlay)cur).getMarker()) {
    				homeOverlay.add(cur);
    			}
    			else if(R.drawable.work == ((POIOverlay)cur).getMarker()) {
    				workOverlay.add(cur);
    			}
    			else if(R.drawable.bulb_poi == ((POIOverlay)cur).getMarker()) {
    	    		bulbOverlay.add(cur);
    	    	}
    			else {
    				otherFavOverlay.add(cur);
    			}
    		}
    		else if(cur instanceof CurrentLocationOverlay) {
    			currentLocationOverlay = cur;
    		}
    		else {
    			otherOverlay.add(cur);
    		}
    	}
    	
    	mapOverlays.clear();
    	mapOverlays.addAll(otherOverlay);
    	mapOverlays.addAll(bulbOverlay);
    	if(currentLocationOverlay != null) {
    		mapOverlays.add(currentLocationOverlay);
    	}
    	mapOverlays.addAll(otherFavOverlay);
    	mapOverlays.addAll(workOverlay);
    	mapOverlays.addAll(homeOverlay);
    }
    
    private void initFavoritePage() {
    	ViewPager favoriteIconPager = (ViewPager) findViewById(R.id.favorite_icons_pager);
    	favoriteIconPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
				favOptPanel.findViewById(R.id.label_input).clearFocus();
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				favOptPanel.findViewById(R.id.label_input).clearFocus();
			}

			@Override
			public void onPageSelected(int pos) {
				LinearLayout indicators = (LinearLayout)findViewById(R.id.indicators);
		        for(int i=0; i<indicators.getChildCount(); i++){
		            indicators.getChildAt(i).setEnabled(i == pos);
		        }
			}
		});
    	
        FavoriteSlideAdapter slideAdapter = new FavoriteSlideAdapter(getSupportFragmentManager(), new ClickCallback() {
			@Override
			public void onClick(FavoriteIcon icon) {
				favOptPanel.findViewById(R.id.icon).setTag(icon);
				favOptPanel.findViewById(R.id.label_input).clearFocus();
				labelIcon.setImageResource(icon.getResourceId(LandingActivity2.this));
				labelIcon.setVisibility(View.VISIBLE);
			}
        });
        
        favoriteIconPager.setAdapter(slideAdapter);
        
    	LinearLayout indicators = (LinearLayout)findViewById(R.id.indicators);
        for(int i=0; i<slideAdapter.getCount(); i++){
            View indicator = getLayoutInflater().inflate(R.layout.onboard_indicator, indicators, false);
            if(i == 0){
                ((LinearLayout.LayoutParams)indicator.getLayoutParams()).leftMargin = 0;
            }else{
                indicator.setEnabled(false);
            }
            indicators.addView(indicator);
        }
    }
    
    public static class FavoriteSlideFragment extends Fragment {
        
        static final String ICONS = "icons";
        
        private FavoriteIcon[][] icons;
        private ClickCallback clickCallback;
        
        public interface ClickCallback {
        	public void onClick(FavoriteIcon icon);
        }
        
        static FavoriteSlideFragment of(FavoriteIcon[][] icons, ClickCallback _clickCallback){
        	FavoriteSlideFragment f = new FavoriteSlideFragment(_clickCallback);
            Bundle args = new Bundle();
            args.putSerializable(ICONS, icons);
            f.setArguments(args);
            return f;
        }
        
        public FavoriteSlideFragment() {}
        
        private FavoriteSlideFragment(ClickCallback clickCallback) {
        	this.clickCallback = clickCallback;
        }
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();
            this.icons = (FavoriteIcon[][]) args.getSerializable(ICONS);
        }
     
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }
     
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.favorite_icon_slide, container, false);
            DisplayMetrics dm = view.getContext().getResources().getDisplayMetrics();
            int iconMargin = Dimension.dpToPx(10, dm);
            for(FavoriteIcon[] rowIcons : icons) {
	            LinearLayout row = new LinearLayout(view.getContext());
	            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
	            lp.weight = 1;
	            row.setLayoutParams(lp);
	            row.setWeightSum(rowIcons.length);
	            for(FavoriteIcon icon : rowIcons) {
	            	ImageView iconView = new ImageView(row.getContext());
	            	LinearLayout.LayoutParams imageLp = new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
	            	imageLp.weight = 1;
	            	imageLp.bottomMargin = iconMargin;
	            	imageLp.leftMargin = iconMargin;
	            	imageLp.rightMargin = iconMargin;
	            	imageLp.topMargin = iconMargin;
	            	imageLp.gravity = Gravity.CENTER;
	            	iconView.setLayoutParams(imageLp);
	            	iconView.setTag(icon);
	            	iconView.setImageResource(icon.getFavoritePageResourceId(view.getContext()));
	            	iconView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(final View v) {
							ClickAnimation clickAnimation = new ClickAnimation(view.getContext(), v);
							clickAnimation.startAnimation(new ClickAnimationEndCallback() {
								@Override
								public void onAnimationEnd() {
									if(clickCallback != null) {
										clickCallback.onClick((FavoriteIcon)v.getTag());
									}
								}
							});
						}
					});
	            	row.addView(iconView);
	            }
	            view.addView(row);
            }
            return view;
        }
        
    }
    
    public static class FavoriteSlideAdapter extends FragmentPagerAdapter {
        
        private static FavoriteIcon[][][] slides = {
            FavoriteIcon.getFirstPageIcons(), 
            FavoriteIcon.getSecondPageIcons()
        };
        
        private ClickCallback clickCallback;
        
        public FavoriteSlideAdapter(FragmentManager fm, ClickCallback callback) {
            super(fm);
            this.clickCallback = callback;
        }
 
        @Override
        public int getCount() {
            return slides.length;
        }
 
        @Override
        public Fragment getItem(int position) {
            return FavoriteSlideFragment.of(slides[position], clickCallback);
        }
        
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
        	Reservation reserv = getFirstReservation();
        	if(reserv != null) {
        		DebugOptionsActivity.addRecipientsOfReserv(LandingActivity2.this, reserv.getRid(), emails, phones);
        	}
        }
    }
    
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
            	if(!isInFavoriteOperation() && 
            			findViewById(R.id.reservations_list_view).getVisibility()!=View.VISIBLE) {
	                final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	                View drawer = findViewById(R.id.left_drawer);
	                if(mDrawerLayout.isDrawerOpen(drawer)){
	                    mDrawerLayout.closeDrawer(drawer);
	                }else{
	                    mDrawerLayout.openDrawer(drawer);
	                }
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
            	if(isInFavoriteOperation()) {
            		hideFavoriteOptPanel();
            		return true;
            	}
            	else if(isPopupMenuShown()) {
            		hidePopupMenu();
            		return true;
            	}
            	else if(searchBox.isFocused() || fromSearchBox.isFocused()) {
            		searchBox.clearFocus();
            		fromSearchBox.clearFocus();
            		return true;
            	}
            	else if(isReservationInfoShown()) {
            		hideReservationInfoPanel();
            		return true;
            	}
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
//                float azimut = Double.valueOf(Math.toDegrees(orientation[0])).floatValue();
                if(myPointOverlay != null){
//                    myPointOverlay.setDegrees(azimut);
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
                	dest = Geocoding.lookup(ctx, address, origin.getLatitude(), origin.getLongitude()).get(0).getGeoPoint();
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
    
}
