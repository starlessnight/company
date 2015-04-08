package com.metropia.activities;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.contributor.util.RecordedGeoPoint;
import org.osmdroid.contributor.util.RecordedRouteGPXFormatter;
import org.osmdroid.tileprovider.util.CloudmadeUtil;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.Time;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.internal.nineoldandroids.animation.Animator;
import com.actionbarsherlock.internal.nineoldandroids.animation.Animator.AnimatorListener;
import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.LocalyticsUtils;
import com.metropia.ResumeNavigationUtils;
import com.metropia.SendTrajectoryService;
import com.metropia.SkobblerUtils;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.TripService;
import com.metropia.activities.DebugOptionsActivity.FakeRoute;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.dialogs.NotificationDialog2.ActionListener;
import com.metropia.models.IncidentIcon;
import com.metropia.models.Reservation;
import com.metropia.models.Route;
import com.metropia.models.Trajectory;
import com.metropia.models.Trajectory.Record;
import com.metropia.models.User;
import com.metropia.requests.CityRequest;
import com.metropia.requests.CityRequest.City;
import com.metropia.requests.ImComingRequest;
import com.metropia.requests.IncidentRequest;
import com.metropia.requests.IncidentRequest.Incident;
import com.metropia.requests.Request;
import com.metropia.requests.Request.Setting;
import com.metropia.requests.ReservationFetchRequest;
import com.metropia.requests.RouteFetchRequest;
import com.metropia.requests.TravelTimeRequest;
import com.metropia.ui.ClickAnimation;
import com.metropia.ui.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.ui.NavigationView;
import com.metropia.ui.NavigationView.CheckPointListener;
import com.metropia.ui.NavigationView.DirectionItem;
import com.metropia.ui.menu.MainMenu;
import com.metropia.ui.timelayout.TimeColumn;
import com.metropia.utils.Dimension;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.HTTP;
import com.metropia.utils.Misc;
import com.metropia.utils.RouteLink;
import com.metropia.utils.RouteNode;
import com.metropia.utils.RouteRect;
import com.metropia.utils.StringUtil;
import com.metropia.utils.SystemService;
import com.metropia.utils.UnitConversion;
import com.metropia.utils.ValidationParameters;
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
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.positioner.SKPosition;
import com.skobbler.ngx.routing.SKRouteListener;
import com.skobbler.ngx.routing.SKRouteManager;
import com.skobbler.ngx.routing.SKRouteSettings;
import com.skobbler.ngx.tracks.SKTracksFile;
import com.skobbler.ngx.util.SKLogging;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class ValidationActivity extends FragmentActivity implements OnInitListener, 
        OnAudioFocusChangeListener, SKMapSurfaceListener, SKRouteListener, RecognitionListener {
	public static final int DEFAULT_ZOOM_LEVEL = 18;
	
	public static final int NAVIGATION_ZOOM_LEVEL = 17;

	private static final String RESERVATION = "reservation";

	private static final String ROUTE = "route";

	private static final String START_TIME = "startTime";

	private static final String POLL_CNT = "pollCnt";

	private static final String GEO_POINT = "geoPoint";

	public static final String EMAILS = "emails";
	
	public static final String PHONES = "phones";
	
	public static final String TRAJECTORY_DATA = "TRAJECTORY_DATA";
	
	public static final Integer REPORT_PROBLEM = Integer.valueOf(100);
	
	public static final int ON_MY_WAY = Integer.valueOf(110);
	
	public static final String CURRENT_LOCATION = "CURRENT_LOCATION";

	private ExceptionHandlingService ehs = new ExceptionHandlingService(this);

	private NavigationView navigationView;
	private ImageView volumnControl;
	private ImageView buttonFollow;
	private ImageView onMyWayBtn;
	
	/**
	 * @deprecated
	 */
	private Route route;
	
	private Route reroute;

	private Reservation reservation;

//	private List<Overlay> mapOverlays;

//	private CurrentLocationOverlay pointOverlay;

	private long startTime;

	// FIXME: Temporary
	private RouteNode nearestNode;

	private Trajectory trajectory = new Trajectory();

	private LocationManager locationManager;

	private LocationListener locationListener;

	private Location lastKnownLocation;

	private FakeLocationService fakeLocationService;

	private AtomicBoolean arrived = new AtomicBoolean(false);

	private TextToSpeech mTts;

	private ListView dirListView;

	private ArrayAdapter<DirectionItem> dirListadapter;

	private static AtomicInteger utteranceCnt = new AtomicInteger();
	
	private static AtomicInteger utteranceCompletedCnt = new AtomicInteger();

	private AtomicBoolean reported = new AtomicBoolean(false);

	private AtomicBoolean stopValidation = new AtomicBoolean(false);

	private boolean isDebugging;

	private long lastLocChanged;

	private Handler animator;

	private Typeface boldFont;

	private Typeface lightFont;

	private int savedPollCnt;

	private String emails;
	
	private String phones;
	
	private String trajectoryData;

	private BroadcastReceiver timeoutReceiver;

	private AtomicBoolean isOnRecreate = new AtomicBoolean(); 
	
	private GeoPoint lastCenter;
	
	private TextView remainDistDirecListView;
	private TextView remainTimesDirectListView;
	
	private SKMapViewHolder mapViewHolder;
	private SKMapSurfaceView mapView;
	
	private String versionNumber = "";
	
	private AtomicBoolean isReplay = new AtomicBoolean(false);
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// init skmap
		SkobblerUtils.initializeLibrary(ValidationActivity.this);
		setContentView(R.layout.post_reservation_map);
		Localytics.integrate(this);
		initSKMaps();
		
		Reservation.cancelNotification(this);
		registerReceiver(tripValidator, new IntentFilter(TRIP_VALIDATOR));
//		registerReceiver(enRouteCheck, new IntentFilter(ENROUTE_CHECK));
		AssetManager assets = getAssets();
		boldFont = Font.getBold(assets);
		lightFont = Font.getLight(assets);
		animator = new Handler(Looper.getMainLooper());
		isOnRecreate.set(savedInstanceState != null);
		Bundle extras = getIntent().getExtras();
		reservation = extras.getParcelable(RESERVATION);
		removeTripFromLandingPage();
		route = (isOnRecreate.get() ? savedInstanceState : extras)
				.getParcelable(ROUTE);
		route.setCredits(reservation.getCredits());
		reservation.setRoute(route);
		
		// init
		lastEnRouteCheckTime.set(-100);
		// Define a listener that responds to location updates
		locationListener = new ValidationLocationListener();

		if (isOnRecreate.get()) {
			startTime = savedInstanceState.getLong(START_TIME);
			savedPollCnt = savedInstanceState.getInt(POLL_CNT);
			emails = savedInstanceState.getString(EMAILS);
			phones = savedInstanceState.getString(PHONES);
			trajectoryData = savedInstanceState.getString(TRAJECTORY_DATA);
			isReplay.set(savedInstanceState.getBoolean(DebugOptionsActivity.REPLAY));
		} else {
			Time now = new Time();
			now.setToNow();
			startTime = now.toMillis(false);
			emails = extras.getString(EMAILS);
			phones = extras.getString(PHONES);
			trajectoryData = extras.getString(TRAJECTORY_DATA);
			isReplay.set(extras.getBoolean(DebugOptionsActivity.REPLAY, false));
		}
		timeoutReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				stopValidation.set(true);
				final boolean tripValidated = isTripValidated();
				NotificationDialog2 dialog = new NotificationDialog2(
						ValidationActivity.this, "There might be a connection problem. Please try again later.");
				dialog.setPositiveActionListener(new NotificationDialog2.ActionListener() {
					@Override
					public void onClick() {
						if (tripValidated) {
						    displayArrivalMsg(null);
						}else if (!isFinishing()) {
							finish();
						}
					}
				});
				dialog.show();
			}
		};
		registerReceiver(timeoutReceiver, new IntentFilter(getClass().getName()));
		Intent timeoutIntent = new Intent(getClass().getName());
		PendingIntent pendingTimeout = PendingIntent.getBroadcast(
				ValidationActivity.this, 0, timeoutIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()
				+ (900 + reservation.getDuration() * 3) * 1000, pendingTimeout);
		
		remainDistDirecListView = (TextView) findViewById(R.id.remain_dist_direc_list);
		remainTimesDirectListView = (TextView) findViewById(R.id.remain_times_direc_list);
		
		try {
			versionNumber = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		}catch(NameNotFoundException ignore) {}

 		dirListadapter = new ArrayAdapter<DirectionItem>(this,
				R.layout.direction_list_item, R.id.text_view_road) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView vRoad = (TextView) view
						.findViewById(R.id.text_view_road);
				TextView vDistance = (TextView) view
						.findViewById(R.id.text_view_distance);
				Font.setTypeface(boldFont, vRoad, vDistance);
			    view.findViewById(R.id.dir_list_item).setBackgroundResource(position == 0 ? 
			    		R.color.pink : 0);
				DirectionItem item = getItem(position);
				ImageView vDirection = (ImageView) view
						.findViewById(R.id.img_view_direction);
				if (item.smallDrawableId == 0) {
					vDirection.setVisibility(View.INVISIBLE);
				} else {
					vDirection.setImageResource(item.smallDrawableId);
					vDirection.setVisibility(View.VISIBLE);
				}
				vDistance.setText(NavigationView.adjustDistanceFontSize(ValidationActivity.this, 
						StringUtil.formatRoundingDistance(UnitConversion.meterToMile(item.distance), true)));
				vDistance.requestLayout();
				StringBuffer roadName = new StringBuffer((StringUtils.isBlank(item.roadName) 
				        || StringUtils.equalsIgnoreCase(item.roadName, "null")) ? "" : (StringUtils.capitalize(item.roadName.substring(0, 1)) 
						        + item.roadName.substring(1)));
				if("Destination".equals(roadName.toString()) && (StringUtils.isNotBlank(reservation.getDestinationName()) || StringUtils.isNotBlank(reservation.getDestinationAddress()))) {
					roadName.append("\n").append(StringUtils.isNotBlank(reservation.getDestinationName()) ? reservation.getDestinationName() : reservation.getDestinationAddress());
				}
				vRoad.setText(roadName.toString());
				vRoad.requestLayout();
				return view;
			}
		};
		final FakeRoute fakeRoute = DebugOptionsActivity.getFakeRoute(
                ValidationActivity.this, route.getId());
        isDebugging = fakeRoute != null;
		boolean loadRoute = isLoadRoute();
		
		if(!loadRoute){
		    route.preprocessNodes();
            routeRect = initRouteRect(route);
            updateDirectionsList();
		}
		
		initViews();
		
		User.initializeIfNeccessary(ValidationActivity.this, new Runnable() {
			@Override
			public void run() {
				countOutOfRouteThreshold = ((Number)Request.getSetting(Setting.reroute_after_N_deviated_samples)).intValue();
			    distanceOutOfRouteThreshold = ((Number)Request.getSetting(Setting.reroute_trigger_distance_in_meter)).doubleValue();
			}
		});
		
		SharedPreferences debugPrefs = getSharedPreferences(DebugOptionsActivity.DEBUG_PREFS, MODE_PRIVATE);
        int gpsMode = debugPrefs.getInt(DebugOptionsActivity.GPS_MODE, DebugOptionsActivity.GPS_MODE_DEFAULT);
        org.osmdroid.util.GeoPoint curLoc = extras.getParcelable(CURRENT_LOCATION);
        if (gpsMode == DebugOptionsActivity.GPS_MODE_LONG_PRESS) {
            Location location = new Location("");
            location.setTime(System.currentTimeMillis());
            locationChanged(location);
        } else if(curLoc != null){
            Location location = new Location("");
            location.setTime(System.currentTimeMillis());
            location.setLatitude(curLoc.getLatitude());
            location.setLongitude(curLoc.getLongitude());
            locationChanged(location);
        }
        
		if (isOnRecreate.get()) {
            lastCenter = new GeoPoint((IGeoPoint) savedInstanceState.getParcelable(GEO_POINT));
        }
		
		if (!loadRoute) {
			centerMap(mapView, isOnRecreate.get(), lastCenter, route);
			drawRoute(mapView, route);
		}
		
		try {
			mTts = new TextToSpeech(this, this);
		} catch (Throwable t) {
		}

		lastLocChanged = SystemClock.elapsedRealtime();
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		if (!isOnRecreate.get()) {
			if (reservation.hasExpired()) {
				stopValidation.set(true);
				NotificationDialog2 dialog = new NotificationDialog2(this,
						getResources().getString(R.string.trip_has_expired));
				dialog.setPositiveActionListener(new NotificationDialog2.ActionListener() {

					@Override
					public void onClick() {
						if (!isFinishing()) {
							finish();
						}
					}
				});
				dialog.show();
			} else if (reservation.isTooEarlyToStart()) {
				stopValidation.set(true);
				long minutes = (reservation.getDepartureTimeUtc() - System
						.currentTimeMillis()) / 60000;
				NotificationDialog2 dialog = new NotificationDialog2(this,
						getResources().getString(
								R.string.trip_too_early_to_start, minutes));
				dialog.setPositiveActionListener(new NotificationDialog2.ActionListener() {

					@Override
					public void onClick() {
						if (!isFinishing()) {
							finish();
						}
					}
				});
				dialog.show();
			}
		}
		
		
		if(!stopValidation.get() && StringUtils.isNotBlank(phones)) {
			sendOnMyWaySms();
		}
		
		initRecognizer();
		
		//init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	private void sendOnMyWaySms() {
		AsyncTask<Void, Void, List<Route>> task = new AsyncTask<Void, Void, List<Route>>() {
            @Override
            protected List<Route> doInBackground(Void... params) {
                List<Route> routes = null;
                try {
                    RouteFetchRequest request = new RouteFetchRequest(
                            reservation.getNavLink(),
                            reservation.getDepartureTime(),
                            reservation.getDuration(),
                            0, 0);
                    routes = request.execute(ValidationActivity.this);
                } catch (Exception e) {
                    ehs.registerException(e);
                }
                return routes;
            }

            protected void onPostExecute(java.util.List<Route> routes) {
                if (ehs.hasExceptions()) {
                    ehs.reportExceptions(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing()) {
                                finish();
                            }
                        }
                    });
                } else if (routes != null && routes.size() > 0) {
                    Route route = routes.get(0);
                    route.preprocessNodes();
                    StringBuilder uri = new StringBuilder("smsto:");
        		    uri.append(phones);
        			Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        			smsIntent.putExtra("sms_body", getTextMessage(route));
        			smsIntent.setType("vnd.android-dir/mms-sms");
        			smsIntent.setData(Uri.parse(uri.toString()));
        			startActivity(smsIntent);
                }
            }
        };
        Misc.parallelExecute(task);
	}
	
	private String getTextMessage(Route _route) {
		StringBuffer msg = new StringBuffer();
		User user = User.getCurrentUser(ValidationActivity.this);
		String remainDist = StringUtils.isNotBlank(remainDistDirecListView.getText()) ? 
				String.valueOf(remainDistDirecListView.getText()):StringUtil.formatImperialDistance(_route.getLength(), false);
		TextView timeInfo = (TextView) findViewById(R.id.remain_times);
		String arriveTime = !"null".equalsIgnoreCase(String.valueOf(timeInfo.getTag(R.id.estimated_arrival_time))) ? 
				String.valueOf(timeInfo.getTag(R.id.estimated_arrival_time)) : TimeColumn.formatTime(reservation.getArrivalTimeUtc(), _route.getTimezoneOffset());
		msg.append(user.getFirstname()).append(" ").append(user.getLastname()).append(" is ")
		   .append(remainDist).append(" away, and will arrive at ")
		   .append(reservation.getDestinationAddress()).append(" at ");
        msg.append(arriveTime).append(".");
        return msg.toString();
	}
	
	private boolean isLoadRoute(){
        return !isOnRecreate.get() && (isDebugging || reservation.getNavLink() != null);
	}

	private static void centerMap(SKMapSurfaceView mapView, boolean isOnRecreate,
			GeoPoint lastCenter, Route route) {
		mapView.setZoom(DEFAULT_ZOOM_LEVEL);
		GeoPoint center = null;
		if (isOnRecreate) {
		    center = lastCenter;
		} else if (route.getFirstNode() != null) {
			center = route.getFirstNode().getGeoPoint();
		}
		if (center != null) {
			SKCoordinate centerCorrdinate = new SKCoordinate(center.getLongitude(), center.getLatitude());
			mapView.centerMapOnPosition(centerCorrdinate);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		finish();
		startActivity(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(ROUTE, route);
		outState.putLong(START_TIME, startTime);
		outState.putInt(POLL_CNT, fakeLocationService == null ? 0
				: fakeLocationService.pollCnt);
		GeoPoint geoPoint = null;
		RouteNode firstNode = route.getFirstNode();
		SKPosition currentPosition = mapView.getCurrentGPSPosition(true);
		if (currentPosition != null) {
			geoPoint = new GeoPoint(currentPosition.getLatitude(), currentPosition.getLongitude());
		} else if (firstNode != null) {
			geoPoint = firstNode.getGeoPoint();
		}
		if (geoPoint != null) {
			outState.putParcelable(GEO_POINT, geoPoint);
		}
		outState.putString(EMAILS, emails);
		outState.putString(PHONES, phones);
		outState.putString(TRAJECTORY_DATA, trajectoryData);
		outState.putBoolean(DebugOptionsActivity.REPLAY, isReplay.get());
	}

	@Override
	protected void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
		if(rawLogDir != null && rawLogDir.exists()) {
			try {
				FileUtils.cleanDirectory(rawLogDir);
			}
			catch(Exception ignore) {}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Localytics.openSession();
		Localytics.tagScreen(this.getClass().getSimpleName());
	    Localytics.upload();
	    Localytics.setInAppMessageDisplayActivity(this);
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
		mapView.onResume();

		SharedPreferences debugPrefs = getSharedPreferences(
				DebugOptionsActivity.DEBUG_PREFS, MODE_PRIVATE);

		// Register the listener with the Location Manager to receive location
		// updates
		int gpsMode = debugPrefs.getInt(DebugOptionsActivity.GPS_MODE,
				DebugOptionsActivity.GPS_MODE_DEFAULT);
		if (StringUtils.isNotBlank(trajectoryData)) {
			int interval = DebugOptionsActivity.getGpsUpdateInterval(this);
			if (fakeLocationService == null) {
				fakeLocationService = new FakeLocationService(locationListener,
						interval, trajectoryData);
			} else {
				fakeLocationService = fakeLocationService.setInterval(interval);
			}
			if (savedPollCnt > 0) {
				fakeLocationService.skip(savedPollCnt);
				savedPollCnt = 0;
			}
		} else if (gpsMode == DebugOptionsActivity.GPS_MODE_REAL && !turnOffGPS.get()) {
            prepareGPS();
	    } else {

		}
		
		registerReceiver(timeInfoCycler, new IntentFilter(TIME_INFO_CYCLE));
		navigationView.stopNotification();
	}

	@Override
	protected void onPause() {
		Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
		super.onPause();
		mapView.onPause();
		unregisterReceiver(timeInfoCycler);
		if(!cancelTrip && !arrived.get()) {
			navigationView.startNotification();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getMenuInflater();
		//mi.inflate(R.menu.validation, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		MainMenu.onMenuItemSelected(this, featureId, item);

		switch (item.getItemId()) {
		case R.id.stop:
			cancelValidation();
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Handle the back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			cancelValidation();

			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}
	
	private RouteRect routeRect;

	public static RouteRect initRouteRect(Route r) {
		return new RouteRect(r.getNodes());
	}
	
	private AtomicBoolean initial = new AtomicBoolean(false);
	private AtomicBoolean dayMode = new AtomicBoolean();
	
	/**
	 * init skmap parameter
	 */
	private void initSKMaps() {
		initial.set(true);
		SKLogging.enableLogs(true);
		mapViewHolder = (SKMapViewHolder) findViewById(R.id.mapview_holder);
		mapViewHolder.hideAllAttributionTextViews();
		mapView = mapViewHolder.getMapSurfaceView();
		CloudmadeUtil.retrieveCloudmadeKey(this);
		
		mapView.setMapSurfaceListener(this);
		mapView.clearAllOverlays();
		mapView.deleteAllAnnotationsAndCustomPOIs();
		mapView.getMapSettings().setCurrentPositionShown(false);
		mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.NAVIGATION);
		mapView.getMapSettings().setMapDisplayMode(SKMapDisplayMode.MODE_3D);
		mapView.getMapSettings().setMapRotationEnabled(true);
        mapView.getMapSettings().setMapZoomingEnabled(true);
        mapView.getMapSettings().setMapPanningEnabled(true);
        mapView.getMapSettings().setZoomWithAnchorEnabled(true);
        mapView.getMapSettings().setInertiaRotatingEnabled(true);
        mapView.getMapSettings().setInertiaZoomingEnabled(true);
        mapView.getMapSettings().setInertiaPanningEnabled(true);
        dayMode.set(SkobblerUtils.isDayMode());
        mapView.getMapSettings().setMapStyle(SkobblerUtils.getMapViewStyle(ValidationActivity.this, dayMode.get()));
        mapView.getMapSettings().setStreetNamePopupsShown(!dayMode.get());
        SKRouteManager.getInstance().setRouteListener(this);
	}
	
	private static final String EN_ROUTE_NOT_ACCEPT_TEXT_1 = "no";
	private static final String EN_ROUTE_NOT_ACCEPT_TEXT_2 = "cancel";
	private static final String EN_ROUTE_ACCEPT_TEXT = "yes";
	
	private void initRecognizer() {
		new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(ValidationActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException ignore) {
                    Log.d("ValidationActivity", "recognizer init fail!");
                }
                return null;
            }

        }.execute();
        
		findViewById(R.id.voice_input_debug_msg).setVisibility(DebugOptionsActivity.isEnrouteVoiceInputDebugMsgEnabled(ValidationActivity.this) ? View.VISIBLE : View.GONE);
	}
	
	private static final String KWS_SEARCH = "wakeup";
	private SpeechRecognizer recognizer;
	private File rawLogDir;
	
	private void setupRecognizer(File assetsDir) {
        File modelsDir = new File(assetsDir, "models");
        rawLogDir = new File(modelsDir, "raws");
        if(!rawLogDir.exists()) {
        	rawLogDir.mkdir();
        }
        recognizer = defaultSetup()
                .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
                .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
                .setRawLogDir(rawLogDir).setKeywordThreshold(1e-1f)
                .getRecognizer();
        recognizer.addListener(this);

     // Create grammar-based searches.
        File decisionGrammar = new File(modelsDir, "grammar/decisiontwo.gram");
        recognizer.addKeywordSearch(KWS_SEARCH, decisionGrammar);
        
//        recognizer.addKeyphraseSearch(KWS_SEARCH, EN_ROUTE_NOT_ACCEPT_TEXT);
//        recognizer.addKeyphraseSearch(KWS_SEARCH, EN_ROUTE_ACCEPT_TEXT);
//        File digitsGrammar = new File(modelsDir, "grammar/digits.gram");
//        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);
//        // Create language model search.
//        File languageModel = new File(modelsDir, "lm/weather.dmp");
//        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);
    }
	
	private void switchSearch(String searchName) {
        recognizer.stop();
        recognizer.startListening(searchName);
    }
	
	private void initViews() {
		
		buttonFollow = (ImageView) findViewById(R.id.center_map_icon);
		buttonFollow.setTag(true);
		
        buttonFollow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	ClickAnimation clickAnimation = new ClickAnimation(ValidationActivity.this, v);
            	clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Boolean tagAfterClick = !((Boolean) buttonFollow.getTag());
		            	buttonFollow.setTag(tagAfterClick);
		            	if (tagAfterClick) {
		            		to3DMap();
		                }
		                else {
		                    to2DMap(routeRect, true);
		                }
		            	navigationView.setToCurrentDireciton();
					}
            	});
            }
        });
        
		navigationView = (NavigationView) findViewById(R.id.navigation_view);
		navigationView.setDestinationAddress(reservation.getDestinationAddress());
		navigationView.setTypeface(boldFont);
		
		Configuration conf = getResources().getConfiguration();
		changeEnRoutePanelMode(conf.orientation==Configuration.ORIENTATION_LANDSCAPE);
		if(conf.orientation==Configuration.ORIENTATION_LANDSCAPE) {
			navigationView.setLandscapMode();
		}
		
		navigationView.setOpenDirectionViewEvent(new Runnable() {
			@Override
			public void run() {
				Log.d("ValidationActivity", "OpenDriectionListView");
				View directionsView = findViewById(R.id.directions_view);
				directionsView.setVisibility(View.VISIBLE);
				ObjectAnimator animator = ObjectAnimator.ofFloat(directionsView, "translationY", -directionsView.getHeight(), 0);
				animator.setDuration(500);
				animator.setInterpolator(new AccelerateDecelerateInterpolator());
				animator.start();
				animator.addListener(new AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						for(View view : getMapViews()) {
							view.setVisibility(View.GONE);
						}
					}

					@Override
					public void onAnimationCancel(Animator animation) {
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}
				});
			}
		});
		
		final View mapViewEndTripBtn = findViewById(R.id.map_view_end_trip_btn);
		mapViewEndTripBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(ValidationActivity.this, v) ;
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						cancelValidation();
					}
					
				});
			}
		});
		
		volumnControl = (ImageView) findViewById(R.id.volumn_control);
		int imageSrc = MapDisplayActivity.isNavigationTtsEnabled(this)?R.drawable.volumn_btn_open:R.drawable.volumn_btn_close;
		volumnControl.setTag(MapDisplayActivity.isNavigationTtsEnabled(this));
		volumnControl.setImageResource(imageSrc);
		volumnControl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(ValidationActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						boolean tagAfterClick = !((Boolean) volumnControl.getTag());
						int imageSrc = tagAfterClick?R.drawable.volumn_btn_open:R.drawable.volumn_btn_close;
		                MapDisplayActivity.setNavigationTts(ValidationActivity.this, tagAfterClick);
		                volumnControl.setTag(tagAfterClick);
		                volumnControl.setImageResource(imageSrc);
		                if(!tagAfterClick){
		                    speak("", true);
		                }else{
		                	Misc.playUnmuteSound(ValidationActivity.this);
		                    utteranceCompletedCnt.set(utteranceCnt.get());
		                }
					}
				});
			}
		});
		
		onMyWayBtn = (ImageView) findViewById(R.id.map_view_on_my_way_btn);
		onMyWayBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(ValidationActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
							NotificationDialog2 dialog = new NotificationDialog2(ValidationActivity.this, "On my Way is currently available for passengers only.");
							dialog.setVerticalOrientation(false);
							dialog.setTitle("Are you the passenger?");
							dialog.setPositiveButtonText("No");
							dialog.setPositiveActionListener(new ActionListener() {
								@Override
								public void onClick() {
									//do nothing
								}
							});
							dialog.setNegativeButtonText("Yes");
							dialog.setNegativeActionListener(new ActionListener() {
								@Override
								public void onClick() {
									Intent contactSelect = new Intent(ValidationActivity.this, ContactsSelectActivity.class);
									contactSelect.putExtra(ContactsSelectActivity.SELECTED_EMAILS, emails);
									contactSelect.putExtra(ContactsSelectActivity.SELECTED_PHONES, "");
									startActivityForResult(contactSelect, ON_MY_WAY);
									onMyWayBtn.setTag(new Object());
								}
							});
							dialog.show();
					}
				});
			}
		});

		dirListView = (ListView) findViewById(R.id.directions_list);
		dirListView.setAdapter(dirListadapter);

		TextView finishButton = (TextView) findViewById(R.id.close);
		finishButton.setText(Html.fromHtml("<u>Close</u>"));
		finishButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnim = new ClickAnimation(ValidationActivity.this, v);
				clickAnim.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						SKRouteManager.getInstance().clearCurrentRoute();
						finish();
					}
				});
			}
		});
		
		final View shareButton = findViewById(R.id.share);
		shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	ClickAnimation clickAnimation = new ClickAnimation(ValidationActivity.this, v);
            	clickAnimation.startAnimation(new ClickAnimationEndCallback() {

					@Override
					public void onAnimationEnd() {
						Intent intent = new Intent(ValidationActivity.this, ShareActivity.class);
		                intent.putExtra(ShareActivity.TITLE, "More Metropians = Less Traffic");
		                intent.putExtra(ShareActivity.SHARE_TEXT, "I earned " + reservation.getMpoint() + " points for traveling at " 
		                    + Reservation.formatTime(route.getDepartureTime(), true) + " to help solve traffic congestion "
		                    + "using Metropia!"
		                    + "\n\n" + Misc.APP_DOWNLOAD_LINK);
		                startActivity(intent);
//						Intent intent = new Intent(Intent.ACTION_SEND);
//						intent.setType("text/plain");
//						intent.putExtra(Intent.EXTRA_SUBJECT, "More Metropians = Less Traffic");
//		                intent.putExtra(Intent.EXTRA_TEXT, "I earned " + reservation.getMpoint() + " points for traveling at " 
//		                    + Reservation.formatTime(route.getDepartureTime(), true) + " to help solve traffic congestion "
//		                    + "using Metropia Mobile!"
//		                    + "\n\n" + Misc.APP_DOWNLOAD_LINK);
//		                startActivity(Intent.createChooser(intent, "Share"));
					}
            		
            	});
            }
        });
		
		TextView feedBackButton = (TextView) findViewById(R.id.feedback);
		feedBackButton.setText(Html.fromHtml("<u>Feedback</u>"));
		feedBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(ValidationActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Intent intent = new Intent(ValidationActivity.this, FeedbackActivity.class);
						startActivity(intent);
					}
				});
			}
		});
		
		TextView doneButton = (TextView) findViewById(R.id.done);
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(ValidationActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						for (View mView : getMapViews()) {
							mView.setVisibility(View.VISIBLE);
						}
						findViewById(R.id.directions_view).setVisibility(View.INVISIBLE);
					}
				});
			}
		});

		TextView destAddr = (TextView) findViewById(R.id.dest_addr);
		destAddr.setText(reservation.getDestinationAddress());

		final TextView timeInfo = (TextView) findViewById(R.id.remain_times);
		if(SkobblerUtils.isDayMode()) {
			timeInfo.setTextColor(Color.parseColor("#ad000000"));
		}
		else {
			timeInfo.setTextColor(Color.parseColor("#adffffff"));
		}
        timeInfo.setTag(R.id.estimated_arrival_time, getFormatedEstimateArrivalTime(
            reservation.getArrivalTimeUtc(), route.getTimezoneOffset()));
        timeInfo.setTag(R.id.remaining_travel_time, getFormatedRemainingTime(reservation.getDuration()));
        refreshTimeInfo();
        final TextView directListTimeInfo = (TextView) findViewById(R.id.remain_times_direc_list);
        directListTimeInfo.setTag(R.id.estimated_arrival_time, getFormatedEstimateArrivalTime(getETA(remainingTime.get()), route.getTimezoneOffset()));
        directListTimeInfo.setTag(R.id.remaining_travel_time, getFormatedRemainingTime(remainingTime.get()));
        refreshDirectListTimeInfo();
        timeInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                timeInfo.setTag(R.id.clicked, true);
                toggleTimeInfo();
            }
        });
        
        findViewById(R.id.co2_circle).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    Intent intent;
			    if(WebMyMetropiaActivity.hasCo2SavingUrl(ValidationActivity.this) || WebMyMetropiaActivity.hasMyMetropiaUrl(ValidationActivity.this)){
			        intent = new Intent(ValidationActivity.this, WebMyMetropiaActivity.class);
			        Integer pageNo = WebMyMetropiaActivity.hasCo2SavingUrl(ValidationActivity.this) ? WebMyMetropiaActivity.CO2_SAVING_PAGE : WebMyMetropiaActivity.MY_METROPIA_PAGE;
			        intent.putExtra(WebMyMetropiaActivity.WHICH_PAGE, pageNo);
			    }else{
			        intent = new Intent(ValidationActivity.this, MyMetropiaActivity.class);
    				intent.putExtra(MyMetropiaActivity.OPEN_TAB, MyMetropiaActivity.CO2_SAVING_TAB);
			    }
			    startActivity(intent);
			}
        });
        
        findViewById(R.id.drive_score_circle).setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        	    Intent intent;
                if(WebMyMetropiaActivity.hasTimeSavingUrl(ValidationActivity.this) || WebMyMetropiaActivity.hasMyMetropiaUrl(ValidationActivity.this)){
                    intent = new Intent(ValidationActivity.this, WebMyMetropiaActivity.class);
                    Integer pageNo = WebMyMetropiaActivity.hasTimeSavingUrl(ValidationActivity.this) ? WebMyMetropiaActivity.TIME_SAVING_PAGE : WebMyMetropiaActivity.MY_METROPIA_PAGE;
			        intent.putExtra(WebMyMetropiaActivity.WHICH_PAGE, pageNo);
                }else{
    				intent = new Intent(ValidationActivity.this, MyMetropiaActivity.class);
    				intent.putExtra(MyMetropiaActivity.OPEN_TAB, MyMetropiaActivity.DRIVE_SCORE_TAB);
                }
				startActivity(intent);
			}
        });
        
        findViewById(R.id.mpoint_circle).setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        	    Intent intent;
                if(WebMyMetropiaActivity.hasMyMetropiaUrl(ValidationActivity.this)){
                    intent = new Intent(ValidationActivity.this, WebMyMetropiaActivity.class);
                    intent.putExtra(WebMyMetropiaActivity.WHICH_PAGE, WebMyMetropiaActivity.MY_METROPIA_PAGE);
                }else{
    				intent = new Intent(ValidationActivity.this, MyMetropiaActivity.class);
    				intent.putExtra(MyMetropiaActivity.OPEN_TAB, MyMetropiaActivity.CO2_SAVING_TAB);
                }
				startActivity(intent);
			}
        });
        
        if(DebugOptionsActivity.isReroutingDebugMsgEnabled(this) || DebugOptionsActivity.isVoiceDebugMsgEnabled(this)
                || DebugOptionsActivity.isGpsAccuracyDebugMsgEnabled(this)){
            findViewById(R.id.rerouting_debug_msg).setVisibility(View.VISIBLE);
        }
        
		findViewById(R.id.en_route_alert_panel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {}
		});
		
		ImageView yesButton = (ImageView) findViewById(R.id.yes_button);
		yesButton.setImageBitmap(Misc.getBitmap(ValidationActivity.this, R.drawable.en_route_yes, 1));
		yesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation click = new ClickAnimation(ValidationActivity.this, v);
				click.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						if(enRoute != null) {
							reroute = enRoute;
	                        reroute.preprocessNodes();
	                        routeRect = initRouteRect(reroute);
	                        updateDirectionsList();
	                        navigationView.setHasVoice(reroute.hasVoice());
	                        sendOnMyWayEmail.set(true);
						}
                        hideEnRouteAlert();
                        v.setClickable(true);
                        Misc.doQuietly(new Runnable() {
							@Override
							public void run() {
								countDown.cancel();
							}
                        });
					}
				});
			}
		});
		
		ImageView noButton = (ImageView) findViewById(R.id.no_button);
		noButton.setImageBitmap(Misc.getBitmap(ValidationActivity.this, R.drawable.en_route_no, 1));
		noButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation click = new ClickAnimation(ValidationActivity.this, v);
				click.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						enRoute = null;
						if(realTravelRemainTimeInSec > 0) {
							sendImComingMsg(realTravelRemainTimeInSec);
						}
						drawRoute(mapView, getRouteOrReroute()); // draw route before en-route
                        hideEnRouteAlert();
                        v.setClickable(true);
                        Misc.doQuietly(new Runnable() {
							@Override
							public void run() {
								countDown.cancel();
							}
                        });
					}
				});
			}
		});
		
//		scheduleNextEnRouteCheck();
        scheduleTimeInfoCycle();
        
        Font.setTypeface(boldFont, remainDistDirecListView, timeInfo, finishButton, feedBackButton);
        Font.setTypeface(Font.getRegular(getResources().getAssets()), (TextView) findViewById(R.id.en_route_yes_desc1), 
        		(TextView) findViewById(R.id.en_route_yes_desc1), (TextView) findViewById(R.id.en_route_auto_accept_desc));
		Font.setTypeface(lightFont/*, osmCredit*/, remainTimesDirectListView);
	}
	
	private ViewTreeObserver.OnPreDrawListener onPreDrawListener;
	
	private void to2DMap(final RouteRect _rect, final boolean hasNavigationHeader) {
		if(_rect != null){
            /* Get a midpoint to center the view of  the routes */
			changeEnRoutePanelMode(!navigationView.isPortraitMode());
            mapView.getMapSettings().setMapDisplayMode(SKMapDisplayMode.MODE_2D);
            mapView.rotateTheMapToNorth();
            final int offsetHeight = hasNavigationHeader ? navigationView.getMeasuredHeight() : 0; 
			GeoPoint topLeft = _rect.getTopLeftPoint();
            GeoPoint bottomRight = _rect.getBottomRightPoint();
			final SKBoundingBox boundingBox = new SKBoundingBox(topLeft.getLatitude(), topLeft.getLongitude(), bottomRight.getLatitude(), bottomRight.getLongitude());
            if(!hasNavigationHeader) {
	            ViewTreeObserver vto = findViewById(R.id.alert_content).getViewTreeObserver();
	            onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
	        		@Override
	        		public boolean onPreDraw() {
	        			int alertHeight = findViewById(R.id.alert_content).getMeasuredHeight();
	        			int alertWidth = findViewById(R.id.alert_content).getMeasuredWidth();
	        			RelativeLayout.LayoutParams mapViewLp = (LayoutParams) mapView.getLayoutParams();
	        			mapViewLp.bottomMargin = navigationView.isPortraitMode() ? alertHeight : 0;
	        			mapViewLp.rightMargin = navigationView.isPortraitMode() ? 0 : alertWidth;
	        			mapView.setLayoutParams(mapViewLp);
	                    mapView.fitBoundingBox(boundingBox, 100, offsetHeight);
	        			return true;
	        		}
	        	};
		        vto.addOnPreDrawListener(onPreDrawListener);
            }
            else {
            	mapView.fitBoundingBox(boundingBox, 100, offsetHeight);
            }
            GeoPoint mid = _rect.getMidPoint();
            SKCoordinate coordinate = new SKCoordinate(mid.getLongitude(), mid.getLatitude());
            mapView.centerMapOnPosition(coordinate);
            mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.NONE);
            mapView.getMapSettings().setMapRotationEnabled(false);
            showIncidentsIfNessary();
        }
	}
	
	private void to3DMap() {
		Misc.doQuietly(new Runnable() {
			@Override
			public void run() {
				ViewTreeObserver vto = findViewById(R.id.alert_content).getViewTreeObserver();
				vto.removeOnPreDrawListener(onPreDrawListener);
			}
		});
		mapView.deleteAnnotation(INCIDENT_BALLOON_ID);
		removeAllIncident();
	    LayoutParams mapViewLp = (LayoutParams) mapView.getLayoutParams();
	    mapViewLp.width = LayoutParams.MATCH_PARENT;
	    mapViewLp.height = LayoutParams.MATCH_PARENT;
	    mapViewLp.bottomMargin = 0;
	    mapViewLp.rightMargin = 0;
	    mapView.setLayoutParams(mapViewLp);
        if (lastKnownLocation != null) {
        	double latitude = lastKnownLocation.getLatitude();
        	double longitude = lastKnownLocation.getLongitude();
        	SKCoordinate coordinate = new SKCoordinate(longitude, latitude);
        	mapView.getMapSettings().setMapDisplayMode(SKMapDisplayMode.MODE_3D);
        	mapView.setZoom(isNearOD_or_Intersection(latitude, longitude)
    	        ?DEFAULT_ZOOM_LEVEL:NAVIGATION_ZOOM_LEVEL);
        	mapView.centerMapOnPosition(coordinate);
        	mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.NAVIGATION);
        	mapView.getMapSettings().setMapRotationEnabled(true);
        }
	}
	
	private void toggleTimeInfo(){
	    TextView timeInfo = (TextView) findViewById(R.id.remain_times);
	    Boolean isRemainingTime = (Boolean) timeInfo.getTag();
        if(isRemainingTime == null || !isRemainingTime){
            timeInfo.setText(formatRemainTime(timeInfo.getTag(R.id.remaining_travel_time).toString()));
            isRemainingTime = true;
        }else{
            timeInfo.setText(formatArrivalTime(timeInfo.getTag(R.id.estimated_arrival_time).toString()));
            isRemainingTime = false;
        }
        timeInfo.setTag(isRemainingTime);
	}
	
	private void toggleDirectionListTimeInfo() {
		TextView timeInfo = (TextView) findViewById(R.id.remain_times_direc_list);
	    Boolean isRemainingTime = (Boolean) timeInfo.getTag();
        if(isRemainingTime == null || !isRemainingTime){
            timeInfo.setText(timeInfo.getTag(R.id.remaining_travel_time).toString());
            isRemainingTime = true;
        }else{
            timeInfo.setText(timeInfo.getTag(R.id.estimated_arrival_time).toString());
            isRemainingTime = false;
        }
        timeInfo.setTag(isRemainingTime);
	}
	
	private SpannableString formatRemainTime(String remainTime) {
		String remainDesc = "Arrive in\n" + remainTime;
		SpannableString remainTimeSpan = SpannableString.valueOf(remainDesc);
		remainTimeSpan.setSpan(new AbsoluteSizeSpan(ValidationActivity.this.getResources()
				.getDimensionPixelSize(R.dimen.smaller_font)), 0, "Arrival in".length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		int indexOfSpace = remainDesc.lastIndexOf(" ");
		remainTimeSpan.setSpan(new AbsoluteSizeSpan(ValidationActivity.this.getResources()
				.getDimensionPixelSize(R.dimen.smaller_font)), indexOfSpace,
				remainDesc.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return remainTimeSpan;
	}
	
	private SpannableString formatArrivalTime(String arrivalTime) {
		String arrivalDesc = "Arrive at\n" + arrivalTime;
		SpannableString arrivalTimeSpan = SpannableString.valueOf(arrivalDesc);
		arrivalTimeSpan.setSpan(new AbsoluteSizeSpan(ValidationActivity.this.getResources()
				.getDimensionPixelSize(R.dimen.smaller_font)), 0, "Arrival at".length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		int secondSpaceIndex = arrivalDesc.lastIndexOf(" ");
		arrivalTimeSpan.setSpan(new AbsoluteSizeSpan(ValidationActivity.this.getResources()
				.getDimensionPixelSize(R.dimen.smaller_font)), secondSpaceIndex, arrivalDesc.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return arrivalTimeSpan;
	}
	
	private void refreshTimeInfo(){
	    runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	            final TextView timeInfo = (TextView) findViewById(R.id.remain_times);
	            Boolean isRemainingTime = (Boolean) timeInfo.getTag();
	            if(isRemainingTime == null || !isRemainingTime){
	                timeInfo.setText(formatArrivalTime(timeInfo.getTag(R.id.estimated_arrival_time).toString()));
	            }else{
	                timeInfo.setText(formatRemainTime(timeInfo.getTag(R.id.remaining_travel_time).toString()));
	            }
	            remainTimesDirectListView.setText(timeInfo.getTag(R.id.remaining_travel_time).toString());
	        }
	    });
	}
	
	private void refreshDirectListTimeInfo(){
	    runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	            final TextView timeInfo = (TextView) findViewById(R.id.remain_times_direc_list);
	            Boolean isRemainingTime = (Boolean) timeInfo.getTag();
	            if(isRemainingTime == null || !isRemainingTime){
	                timeInfo.setText(timeInfo.getTag(R.id.estimated_arrival_time).toString());
	            }else{
	                timeInfo.setText(timeInfo.getTag(R.id.remaining_travel_time).toString());
	            }
	        }
	    });
	}
	    
	private static final String timeFormat = "h:mm a";
	    
	private static String getFormatedEstimateArrivalTime(long time, int timzoneOffset){
	    SimpleDateFormat dateFormat = new SimpleDateFormat(timeFormat);
	    dateFormat.setTimeZone(TimeZone.getTimeZone(Request.getTimeZone(timzoneOffset)));
	    return dateFormat.format(new Date(time));
	}
	    
	public static String getFormatedRemainingTime(long seconds){
	    long minute = Double.valueOf(Math.round(seconds / 60.0D)).longValue();
	    return minute + " min";
	}

	public static SpannableString formatCO2Desc(Context ctx, String co2Desc) {
		int lbsIndex = co2Desc.indexOf("lbs");
		SpannableString co2DescSpan = SpannableString.valueOf(co2Desc);
		co2DescSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.micro_font)), lbsIndex, 
				lbsIndex + "lbs".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		int co2Index = co2Desc.indexOf("CO2");
		co2DescSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.smaller_font)), co2Index,
				co2Index + "CO".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		int twoIndex = co2Index + "CO".length();
		co2DescSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.micro_font)), twoIndex,
				twoIndex + "2".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return co2DescSpan;
	}
	
	public static SpannableString formatCongrMessage(Context ctx, String message) {
		int indexOfNewline = message.indexOf("\n");
		SpannableString congrSpan = SpannableString.valueOf(message);
		congrSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.medium_font)), indexOfNewline,
				message.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		congrSpan.setSpan(new ForegroundColorSpan(ctx.getResources().getColor(R.color.transparent_light_gray)), 
				indexOfNewline + 1, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return congrSpan;
	}
	
	public static SpannableString formatCongrValueDesc(Context ctx, String valueDesc) {
		int indexOfNewline = valueDesc.indexOf("\n");
		SpannableString congrValueSpan = SpannableString.valueOf(valueDesc);
		congrValueSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.smaller_font)), indexOfNewline,
				valueDesc.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return congrValueSpan;
	}

	private View[] getMapViews() {
		return new View[] { findViewById(R.id.mapview_holder),
				findViewById(R.id.navigation_view), findViewById(R.id.mapview_options) };
	}

	private void prepareGPS() {
		// Acquire a reference to the system Location Manager
		if (locationManager != null) {
			locationManager.removeUpdates(locationListener);
		}
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			SystemService.alertNoGPS(this, true);
		} else {
			// TODO: Turn on GSP early
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					DebugOptionsActivity.getGpsUpdateInterval(this), 0,
					locationListener);
		}
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	}
	
	private File saveGPXFile(Route _route) {
		try {
			List<RecordedGeoPoint> routeGeoPoints = new ArrayList<RecordedGeoPoint>();
			for(RouteNode routeNode : _route.getNodes()) {
				RecordedGeoPoint geoPoint = new RecordedGeoPoint(routeNode.getGeoPoint().getLatitudeE6(), routeNode.getGeoPoint().getLongitudeE6());
				routeGeoPoints.add(geoPoint);
			}
			String gpxContent = RecordedRouteGPXFormatter.create(routeGeoPoints);
			File gpxFile = getFile(ValidationActivity.this, _route.getId());
			FileUtils.writeStringToFile(gpxFile, gpxContent);
			return gpxFile;
		}
		catch(IOException e) {
			ehs.reportException(e);
		}
		return null;
	}
	
	private static File getDir(Context ctx){
		File gpxDir = new File(ctx.getExternalFilesDir(null), "gpx");
		if(!gpxDir.exists()) {
			gpxDir.mkdir();
		}
        return gpxDir;
    }
	
	public static File getFile(Context ctx, long rId){
        return new File(getDir(ctx), String.valueOf(rId) + ".gpx");
    }
	
	private static final Integer DEST_ANNOTATION_ID = Integer.valueOf(1010);

	public synchronized void drawRoute(SKMapSurfaceView mapView, Route _route) {
		try {
			mapView.clearAllOverlays();
			SKRouteManager routeManager = SKRouteManager.getInstance();
			File gpxFile = saveGPXFile(_route);
			if(gpxFile != null) {
				SKTracksFile routeGpx = SKTracksFile.loadAtPath(gpxFile.getAbsolutePath());
				routeManager.clearCurrentRoute();
				routeManager.clearRouteAlternatives();
				routeManager.clearAllRoutesFromCache();
				routeManager.createRouteFromTrackElement(routeGpx.getRootTrackElement(), 
						SKRouteSettings.SKROUTE_CAR_FASTEST, false, false, false);
				drawDestinationAnnotation(reservation.getEndlat(), reservation.getEndlon());
			}
			/*
			List<SKCoordinate> routeCoors = new ArrayList<SKCoordinate>();
			for(RouteNode node : _route.getNodes()) {
				routeCoors.add(new SKCoordinate(node.getLongitude(), node.getLatitude()));
			}
			SKPolyline routeLine = new SKPolyline();
			routeLine.setNodes(routeCoors);
			routeLine.setColor(new float[] {0f, 0.6f, 0.8f, 0.5f}); //RGBA
			routeLine.setLineSize(30);
			
			//outline properties, otherwise map crash
			routeLine.setOutlineColor(new float[] { 0f, 0.6f, 0.8f, 0.5f });
			routeLine.setOutlineSize(10);
			routeLine.setOutlineDottedPixelsSolid(3);
			routeLine.setOutlineDottedPixelsSkip(3);
			//
			
			mapView.addPolyline(routeLine);
			
			drawDestinationAnnotation(reservation.getEndlat(), reservation.getEndlon());
			if((Boolean)buttonFollow.getTag()) {
				mapView.getMapSettings().setMapDisplayMode(SKMapDisplayMode.MODE_3D);
			}
			*/
			
			_route.setUserId(User.getCurrentUser(this).getId());
		}catch(Exception e) {
			Crashlytics.logException(e);
		}
	}
	
	private void drawDestinationAnnotation(double lat, double lon) {
		SKAnnotation destAnn = new SKAnnotation();
		destAnn.setUniqueID(DEST_ANNOTATION_ID);
		destAnn.setLocation(new SKCoordinate(lon, lat));
		destAnn.setMininumZoomLevel(5);
		SKAnnotationView destAnnView = new SKAnnotationView();
        ImageView destImage = new ImageView(ValidationActivity.this);
        destImage.setImageBitmap(Misc.getBitmap(ValidationActivity.this, R.drawable.pin_destination, 1));
        destAnnView.setView(destImage);
        destAnn.setAnnotationView(destAnnView);
        destAnn.setOffset(new SKScreenPoint(0, Dimension.dpToPx(20, getResources().getDisplayMetrics())));
		mapView.addAnnotation(destAnn, SKAnimationSettings.ANIMATION_NONE);
	}
	
	private int seq = 1;

	private void saveTrajectory(final Runnable callback) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
				    final File tFile = SendTrajectoryService.getInFile(ValidationActivity.this,
			                reservation.getRid(), seq++);
			        final JSONArray tJson = trajectory.toJSON();
			        trajectory.clear();
					Misc.parallelExecute(new AsyncTask<Void, Void, Void>() {
						@Override
						protected Void doInBackground(Void... params) {
							try {
								FileUtils.write(tFile, tJson.toString());
							} catch (IOException e) {
							}
							return null;
						}
						@Override
						protected void onPostExecute(Void result) {
						    if(callback != null){
						        callback.run();
						    }
						}
					});
				} catch (Throwable t) {
				}
			}
		});
	}
	
	private void saveTrajectory() {
	    saveTrajectory(null);
	}

	private JSONArray omwPercentages;
	{
	    omwPercentages = (JSONArray) Request.getSetting(Setting.remaining_percentage_to_trigger_OMW_message);
	    if(omwPercentages == null){
	        omwPercentages = new JSONArray();
	    }
	}
	
//	private boolean[] omwSent = new boolean[omwPercentages.length()];
	
	private void sendImComingMsg(final long remainTime) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (StringUtils.isNotBlank(emails)) {
				    double distance = 0;
	                for(int i=0; i<dirListadapter.getCount(); i++){
	                    distance += dirListadapter.getItem(i).distance;
	                }
//	                double percentage = distance * 100 / getRouteOrReroute().getLength();
//	                boolean toSent = false;
//	                for(int i=0; i<omwPercentages.length(); i++){
//	                    try {
//                            if(!omwSent[i] && percentage <= omwPercentages.getDouble(i)){
//                                omwSent[i] = true;
//                                toSent = true;
//                            }
//                        }
//                        catch (JSONException e) {
//                        }
//	                }
//	                if(toSent){
	                    final double _distance = distance;
    					try {
    						Misc.parallelExecute(new AsyncTask<Void, Void, Boolean>() {
    							@Override
    							protected Boolean doInBackground(Void... params) {
    							    boolean success = false;
    								try {
    									SKPosition currentPosition = mapView.getCurrentGPSPosition(true);
    									GeoPoint loc = new GeoPoint(currentPosition.getLatitude(), currentPosition.getLongitude());
    									ImComingRequest req = new ImComingRequest(
    											User.getCurrentUser(ValidationActivity.this),
    											emails,
    											loc.getLatitude(),
    											loc.getLongitude(),
    											getETA(remainTime),
    											NavigationView.metersToMiles(_distance),
    											reservation.getDestinationAddress(),
    											route.getTimezoneOffset());
    									req.execute(ValidationActivity.this);
    									success = true;
    								} catch (Exception e) {
    								}
    								return success;
    							}
    
    							protected void onPostExecute(Boolean success) {
    								String msg;
    								if (!success) {
    									msg = "On My Way not sent";
    								} else {
    									msg = "On My Way sent";
    								}
    								if(success) {
    									LocalyticsUtils.tagSendOnMyWay();
    									Misc.playOnMyWaySound(ValidationActivity.this);
    								}
    								Toast.makeText(ValidationActivity.this, msg,
    										Toast.LENGTH_LONG).show();
    							}
    						});
    					} catch (Throwable t) {
    					}
//	                }
				}
			}
		});
	}

	private void saveTrip(final Runnable callback) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
				    final File tFile = TripService.getFile(ValidationActivity.this, reservation.getRid());
					Misc.parallelExecute(new AsyncTask<Void, Void, Void>() {
						@Override
						protected Void doInBackground(Void... params) {
							try {
								JSONObject reservDetail = new JSONObject();
								try {
									reservDetail.put(CongratulationActivity.DESTINATION, reservation.getDestinationAddress());
									reservDetail.put(CongratulationActivity.DEPARTURE_TIME, reservation.getDepartureTime());
								} catch (JSONException e) {
								}
								FileUtils.write(tFile, reservDetail.toString());
							} catch (IOException e) {
							}
							if(callback != null){
							    callback.run();
							}
							return null;
						}
					});
				} catch (Throwable t) {
				}
			}
		});
	}

	private Route getRouteOrReroute(){
	    return reroute == null?route:reroute;
	}
	
	private void showNavigationInformation(final Location location,
			final RouteNode node) {
		//Log.d("ValidationActivity", "showNavigationInformation()");
		runOnUiThread(new Runnable() {
			public void run() {
				List<DirectionItem> items = updateDirectionsList(node, location);
				navigationView.update(getRouteOrReroute(), location, node, items);
			}
		});
	}

	private void updateDirectionsList() {
		updateDirectionsList(null, null);
	}
	
	private List<DirectionItem> updateDirectionsList(RouteNode node,
			Location location) {
		List<DirectionItem> items = new ArrayList<DirectionItem>();
		dirListadapter.clear();
		RouteNode nextNode = node;
		if (nextNode == null) {
			nextNode = getRouteOrReroute().getFirstNode();
		}
		if (nextNode != null) {
			double distance = 0;
			do {
				if (nextNode.getFlag() != 0) {
					if (nextNode == node && location != null) {
						distance = getRouteOrReroute()
								.getDistanceToNextTurn(location.getLatitude(),
										location.getLongitude());
					}
					DirectionItem item = new DirectionItem(nextNode.getDirection(), distance, nextNode.getRoadName());
					dirListadapter.add(item);
					items.add(item);
					distance = 0;
				}
				distance += nextNode.getDistance();
			} while ((nextNode = nextNode.getNextNode()) != null);
		}
		updateDirectListViewRemainDists(items);
		return items;
	}
	
	private void updateDirectListViewRemainDists(List<DirectionItem> items) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		double distance = 0;
		for(DirectionItem item : items) {
			try {
				distance = distance + nf.parse(nf.format(UnitConversion.meterToMile(item.distance))).doubleValue();
			}
			catch(ParseException ignore) {}
		}
		remainDistDirecListView.setText(StringUtil.formatRoundingDistance(distance, false));
	}
	
	// init at onCreate()
	private int countOutOfRouteThreshold = 2;
    
    private double distanceOutOfRouteThreshold = 40;  //meter
	
	public static final double speedOutOfRouteThreshold = 5;
	
	private static final double odZoomDistanceLimit = 1200; //feet
	
	private static final double intersectZoomDistanceLimit = 1320; // feet
	
	private AtomicInteger routeOfRouteCnt = new AtomicInteger();
	
	private String lastRerutingApiCallStatus = "none";
	
	private void reroute(final double lat, final double lon, final double speedInMph,
	        final float bearing, final long passedTime){
	    runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AsyncTask<Void, Void, Route> task = new AsyncTask<Void, Void, Route>(){
                    @Override
                    protected void onPreExecute() {
                        navigationView.setRerouting(true);
                        lastRerutingApiCallStatus = "waiting";
                        RouteNode lastNode = getRouteOrReroute().getLastNode();
                        navigationView.setRestrictVoiceGuidance(RouteNode.distanceBetween(lat, lon, lastNode.getLatitude(), lastNode.getLongitude()) < ValidationParameters.getInstance().getDisableRerouteThreshold());
                    }
                    @Override
                    protected Route doInBackground(Void... params) {
                        return getNewRoute(lat, lon, speedInMph, bearing);
                    }
                    @Override
                    protected void onPostExecute(Route result) {
                        navigationView.setRerouting(false);
                        routeOfRouteCnt.set(0);
                        if(result != null){
                            passedNodeTimeOffset.addAndGet(passedTime);
                            reroute = result;
                            reroute.preprocessNodes();
                            routeRect = initRouteRect(reroute);
                            updateDirectionsList();
                            drawRoute(mapView, reroute);
                            lastRerutingApiCallStatus = "success";
                            navigationView.setHasVoice(reroute.hasVoice());
                        }else{
                            lastRerutingApiCallStatus = "failed";
                        }
                    }
                };
                Misc.parallelExecute(task);
            }
        });
	}
	
	private Route getNewRoute(double lat, double lon, double speedInMph, float bearing) {
		Route navRoute = null;
        RouteFetchRequest routeReq = new RouteFetchRequest(User.getCurrentUser(ValidationActivity.this), 
            new GeoPoint(lat, lon), new GeoPoint(reservation.getEndlat(), reservation.getEndlon()), 
            System.currentTimeMillis(), speedInMph, bearing, null,
            reservation.getDestinationAddress(), MapDisplayActivity.isIncludeTollRoadsEnabled(ValidationActivity.this), versionNumber);
        try{
            List<Route> list = routeReq.execute(ValidationActivity.this);
            if(list != null && !list.isEmpty()){
                Route resRoute = list.get(0);
                RouteFetchRequest navReq = new RouteFetchRequest(
                        resRoute.getLink().url,
                        System.currentTimeMillis(),
                        0,
                        speedInMph,
                        bearing);
                List<Route> routes = navReq.execute(ValidationActivity.this);
                if (routes != null && routes.size() > 0) {
                    navRoute = routes.get(0);
                    navRoute.setLink(resRoute.getLink());
                    Map<Integer, Integer> nodeTimes = new HashMap<Integer, Integer>();
                    for(RouteNode n:resRoute.getNodes()){
                        nodeTimes.put(n.getNodeNum(), n.getTime());
                    }
                    for(RouteNode n : navRoute.getNodes()){
                        Integer time = nodeTimes.get(n.getNodeNum());
                        if(time != null){
                            n.setTime(time);
                        }
                    }
                }
            }
        }catch(Throwable t){
            Log.d("EnRoute", Log.getStackTraceString(t));
        }
        return navRoute;
	}
	
	private AtomicBoolean routeLoaded = new AtomicBoolean();
	
	private AtomicLong passedNodeTimeOffset = new AtomicLong(); 
	
	private List<String> ttsBuffer = new ArrayList<String>();
	
	private AtomicInteger ttsDelayCnt = new AtomicInteger();
	
	private boolean isNearOD_or_Intersection(double lat, double lng){
	    boolean hasNodes = !getRouteOrReroute().getNodes().isEmpty();
	    RouteNode intersectNode = null;
	    if(hasNodes){
    	    final RouteLink rerouteNearestLink = getRouteOrReroute().getNearestLink(lat, lng);
    	    intersectNode = rerouteNearestLink.getEndNode();
            while (intersectNode.getFlag() == 0 && intersectNode.getNextNode() != null) {
                intersectNode = intersectNode.getNextNode();
            }
	    }
        return !route.getNodes().isEmpty() && NavigationView.metersToFeet(
	            route.getFirstNode().distanceTo(lat, lng)) <= odZoomDistanceLimit 
            || hasNodes && NavigationView.metersToFeet(
                getRouteOrReroute().getLastNode().distanceTo(lat, lng)) <= odZoomDistanceLimit
            || intersectNode != null && NavigationView.metersToFeet(
                intersectNode.distanceTo(lat, lng)) <= intersectZoomDistanceLimit;
	}
	
	private Long startCountDownTime = Long.MAX_VALUE;
	private Long incidentInitTime = Long.valueOf(-1);
	private String incidentUrl;
	private Map<Integer, Incident> incidents = new HashMap<Integer, Incident>();
	private AtomicBoolean sendOnMyWayEmail = new AtomicBoolean(true);
	
	private synchronized void locationChanged(final Location location) {
	    final double speedInMph = Trajectory.msToMph(location.getSpeed());
	    final float bearing = location.getBearing();
	    final double lat = location.getLatitude();
	    final double lng = location.getLongitude();
	    final float accuracy = location.getAccuracy();
	    if (!routeLoaded.get() && isLoadRoute()) {
            routeLoaded.set(true);
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    AsyncTask<Void, Void, List<Route>> task = new AsyncTask<Void, Void, List<Route>>() {
                        @Override
                        protected List<Route> doInBackground(Void... params) {
                        	List<Route> navRoutes = null;
                            try {
                            	GeoPoint curPosi = new GeoPoint(lat, lng);
                                Reservation reser = new ReservationFetchRequest(
                                        User.getCurrentUser(ValidationActivity.this), 
                                        reservation.getRid())
                                    .execute(ValidationActivity.this);
                                reservation.setEndlat(reser.getEndlat());
                                reservation.setEndlon(reser.getEndlon());
                                
                                RouteFetchRequest request;
                                if (isDebugging) {
                                    request = new RouteFetchRequest(
                                            route.getDepartureTime());
                                } else {
                                	//re-query route
                                	if(curPosi.isEmpty()) {
                                		RouteNode firstNode = reser.getRoute().getFirstNode();
	                                    curPosi = new GeoPoint(firstNode.getLatitude(), firstNode.getLongitude());
                                	}
                                	request = new RouteFetchRequest(User.getCurrentUser(ValidationActivity.this), 
                                		curPosi, new GeoPoint(reser.getEndlat(), reser.getEndlon()), 
                                		System.currentTimeMillis(), speedInMph, bearing, null, reser.getDestinationAddress(), 
                                		MapDisplayActivity.isIncludeTollRoadsEnabled(ValidationActivity.this), versionNumber);
                                }
                                List<Route> routes = request.execute(ValidationActivity.this);
                                if (routes != null && routes.size() > 0) {
                                	Route resRoute = routes.get(0);
                                    RouteFetchRequest navReq = new RouteFetchRequest(
                                        resRoute.getLink().url,
                                        System.currentTimeMillis(),
                                        0,
                                        speedInMph,
                                        bearing);
                                    navRoutes = navReq.execute(ValidationActivity.this);
                                    if (navRoutes != null && navRoutes.size() > 0) {
                                        Route navRoute = navRoutes.get(0);
                                        navRoute.setLink(resRoute.getLink());
                                        Map<Integer, Integer> nodeTimes = new HashMap<Integer, Integer>();
                                        for(RouteNode n:resRoute.getNodes()){
                                    	    nodeTimes.put(n.getNodeNum(), n.getTime());
                                        }
                                        for(RouteNode n : navRoute.getNodes()){
                                            Integer time = nodeTimes.get(n.getNodeNum());
                                            if(time != null){
                                                n.setTime(time);
                                            }
                                        }
                                   }
                                }
                            } catch (Exception e) {
                                ehs.registerException(e, e.getMessage());
                            }
                            return navRoutes;
                        }

                        protected void onPostExecute(java.util.List<Route> routes) {
                            if (ehs.hasExceptions()) {
                                ehs.reportExceptions(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isFinishing()) {
                                            finish();
                                        }
                                    }
                                });
                            } else if (routes != null && routes.size() > 0) {
                                final FakeRoute fakeRoute = DebugOptionsActivity.getFakeRoute(
                                		ValidationActivity.this, route.getId());
                                Route oldRoute = route;
                                route = routes.get(isDebugging ? fakeRoute.seq : 0);
                                route.setId(oldRoute.getId());
                                route.setTimezoneOffset(oldRoute.getTimezoneOffset());
                                reservation.setRoute(route);
                                route.setCredits(reservation.getCredits());
                                route.preprocessNodes();
                                routeRect = initRouteRect(route);
                                updateDirectionsList();
                                centerMap(mapView, isOnRecreate.get(),
                                    lastCenter, route);
                                drawRoute(mapView, route);
                                navigationView.setHasVoice(route.hasVoice());
                                SharedPreferences debugPrefs = getSharedPreferences(DebugOptionsActivity.DEBUG_PREFS, MODE_PRIVATE);
                                int gpsMode = debugPrefs.getInt(DebugOptionsActivity.GPS_MODE, DebugOptionsActivity.GPS_MODE_DEFAULT);
                                if(lastKnownLocation != null && gpsMode != DebugOptionsActivity.GPS_MODE_LONG_PRESS){
                                    locationChanged(lastKnownLocation);
                                }
                            }
                            
                        }
                    };
                    Misc.parallelExecute(task);
                }
            });
	    }
	    
	    if(DebugOptionsActivity.isIncidentEnabled(ValidationActivity.this) && StringUtils.isBlank(incidentUrl)) {
	    	MainActivity.initApiLinksIfNecessary(ValidationActivity.this, new Runnable() {
				@Override
				public void run() {
					AsyncTask<Void, Void, Void> getIncidentTask = new AsyncTask<Void, Void, Void>() {
				   		@Override
						protected Void doInBackground(Void... params) {
				    		CityRequest cityReq = new CityRequest(lat, lng, HTTP.defaultTimeout);
				            try {
				            	City city = cityReq.execute(ValidationActivity.this);
								if(city != null && StringUtils.isBlank(city.html)) {
									incidentUrl = city.incidents;
								}
							} catch (Exception ignore) {}
				   			return null;
						}
				   		
				    };
				    Misc.parallelExecute(getIncidentTask);
				}
	    	});
	    }
	    
	    if(DebugOptionsActivity.isIncidentEnabled(ValidationActivity.this) && incidentInitTime < 0 && StringUtils.isNotBlank(incidentUrl)) {
	    	AsyncTask<Void, Void, Boolean> retriveIncidentTask = new AsyncTask<Void, Void, Boolean>() {
	    		@Override
				protected Boolean doInBackground(Void... params) {
	    			incidents.clear();
	    			IncidentRequest incidentReq = new IncidentRequest(User.getCurrentUser(ValidationActivity.this), incidentUrl, HTTP.defaultTimeout);
	    			incidentReq.invalidateCache(ValidationActivity.this);
	    			try {
	    				List<Incident> allIncident = incidentReq.execute(ValidationActivity.this);
	    				for(Incident inc : allIncident) {
	    					incidents.put(SkobblerUtils.getUniqueId(inc.lat, inc.lon), inc);
	    				}
	    			} catch (Exception ignore) {return false;}
	    			return true;
				}
	    		
	    		@Override
		        protected void onPostExecute(Boolean result) {
	    			if(result) {
	    				incidentInitTime = System.currentTimeMillis();
	    				showIncidentsIfNessary();
	    			}
	    		}
	    	};
	    	Misc.parallelExecute(retriveIncidentTask);
	    }
	    
	    if(lastEnRouteCheckTime.get() < 0) {
	    	lastEnRouteCheckTime.set(System.currentTimeMillis());
	    }
	    
	    if(lastEnRouteCheckTime.get() > 0 && System.currentTimeMillis() - lastEnRouteCheckTime.get() >= 5 * 60 * 1000 && getRouteOrReroute().getDistanceToNextTurn(lat, lng) >= 20 * location.getSpeed()) {
	    	lastEnRouteCheckTime.set(System.currentTimeMillis());
	    	enrouteCheck();
	    }
	    
	    runOnUiThread(new Runnable(){
            @Override
            public void run() {
            	final GeoPoint position = new GeoPoint(lat, lng);
                if(position.isEmpty()){
                    SharedPreferences debugPrefs = getSharedPreferences(DebugOptionsActivity.DEBUG_PREFS, MODE_PRIVATE);
                    int gpsMode = debugPrefs.getInt(DebugOptionsActivity.GPS_MODE, DebugOptionsActivity.GPS_MODE_DEFAULT);
                    if(gpsMode != DebugOptionsActivity.GPS_MODE_LONG_PRESS){
                        navigationView.setTextViewWaiting("Waiting for the route...");
                    }
                }
                
                if(speedInMph <= speedOutOfRouteThreshold && lastKnownLocation != null){
                    location.setBearing(lastKnownLocation.getBearing());
                }
                
                if ((Boolean)buttonFollow.getTag()) {
                    mapView.setZoom(isNearOD_or_Intersection(lat, lng)?
                        DEFAULT_ZOOM_LEVEL:NAVIGATION_ZOOM_LEVEL);
                }
                
                long now = SystemClock.elapsedRealtime();
                SKPosition currentPosition = mapView.getCurrentGPSPosition(true);
                GeoPoint oldLoc = new GeoPoint(currentPosition.getLatitude(), currentPosition.getLongitude());
                if (oldLoc.isEmpty() || initial.getAndSet(false)) {
                	if(!position.isEmpty()) {
	                    mapView.reportNewGPSPosition(new SKPosition(location));
                	}
                } else {
                    animator.removeCallbacksAndMessages(null);
                    mapView.reportNewGPSPosition(new SKPosition(location));
                }
                lastLocChanged = now;
                lastKnownLocation = location;
            }
	    });
	    if(!routeLoaded.get()){
	        return;
	    }
	    
		long linkId = Trajectory.DEFAULT_LINK_ID;
		
		if (!route.getNodes().isEmpty()) {	        
	        getRouteOrReroute().getNearestNode(lat, lng).getMetadata().setPassed(true);
	        
	        long passedNodeTime = passedNodeTimeOffset.get();
	        
	        List<RouteLink> rerouteNearbyLinks = getRouteOrReroute().getNearbyLinks(lat, lng, distanceOutOfRouteThreshold + accuracy);
            List<RouteLink> rerouteSameDirLinks = getRouteOrReroute().getSameDirectionLinks(rerouteNearbyLinks, speedInMph, bearing);
            
            if(!Route.isPending(rerouteNearbyLinks, rerouteSameDirLinks)){
                if(Route.isOutOfRoute(rerouteNearbyLinks, rerouteSameDirLinks) && speedInMph > speedOutOfRouteThreshold){
                    if(routeOfRouteCnt.incrementAndGet() == countOutOfRouteThreshold){
                        reroute(lat, lng, speedInMph, bearing, passedNodeTime);
                    }
                }else{
                    routeOfRouteCnt.set(0);
                }
                
                if(rerouteSameDirLinks.size() > 0){
                    final RouteLink rerouteNearestLink = Route.getClosestLink(rerouteSameDirLinks, lat, lng);
        	        
        	        if(DebugOptionsActivity.isReroutingDebugMsgEnabled(this) || DebugOptionsActivity.isVoiceDebugMsgEnabled(this)
        	                || DebugOptionsActivity.isGpsAccuracyDebugMsgEnabled(this)){
            	        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String msg = "";
                                if(DebugOptionsActivity.isReroutingDebugMsgEnabled(ValidationActivity.this)){
                                    msg += "distance from route: " 
                                        + Double.valueOf(NavigationView.metersToFeet(rerouteNearestLink.distanceTo(lat, lng))).intValue() + " ft" 
                                        + ", speed: " + Double.valueOf(speedInMph).intValue() + " mph" 
                                        + "\nconsecutive out of route count: " + routeOfRouteCnt.get()
                                        + "\nlast API call status: " + lastRerutingApiCallStatus;
                                }
                                if(DebugOptionsActivity.isVoiceDebugMsgEnabled(ValidationActivity.this)){
                                    RouteNode endNodeForLink = rerouteNearestLink.getEndNode();
                                    RouteNode endNode = endNodeForLink;
                                    while(StringUtils.isBlank(endNode.getVoice()) && endNode.getNextNode() != null){
                                        endNode = endNode.getNextNode();
                                    }
                                    msg += (StringUtils.isBlank("")?"":"\n")
                                        + "node: " + endNode.getNodeNum()
                                        + ", voice radius: " + Double.valueOf(endNode.getVoiceRadius()).intValue() + " ft"
                                        + "\ndistance from node: " + Double.valueOf(NavigationView.metersToFeet(endNode.distanceTo(lat, lng))).intValue() + " ft"
                                        + "\nvoice:" + endNode.getVoice()
                                        + "\nvoice for link (nearest node): " + endNodeForLink.getVoiceForLink();
                                }
                                if(DebugOptionsActivity.isGpsAccuracyDebugMsgEnabled(ValidationActivity.this)){
                                    msg += (StringUtils.isBlank("")?"":"\n")
                                            + "gps accuracy: " + accuracy  + " meters";
                                }
                                ((TextView)findViewById(R.id.rerouting_debug_msg)).setText(msg);
                            }
                        });
        		    }
        	        
        	        linkId = rerouteNearestLink.getStartNode().getLinkId();
                }
            }
            
            nearestNode = getRouteOrReroute().getNearestLink(lat, lng).getEndNode();
            
            long remainingNodeTime = 0;
            for (RouteNode node : getRouteOrReroute().getNodes()) {
                int time = node.getTime();
                if (node.getNodeIndex() > nearestNode.getNodeIndex()) {
                    remainingNodeTime += time;
                }else{
                    passedNodeTime += time;
                }
            }
            Time currentTime = new Time();
            currentTime.setToNow();
            
            remainingTime.set(remainingNodeTime);
            
            etaDelay.set(currentTime.toMillis(false) - startTime - passedNodeTime * 1000);
            final TextView timeInfo = (TextView) findViewById(R.id.remain_times);
            timeInfo.setTag(R.id.estimated_arrival_time, getFormatedEstimateArrivalTime(getETA(remainingTime.get()), route.getTimezoneOffset()));
            timeInfo.setTag(R.id.remaining_travel_time, getFormatedRemainingTime(remainingTime.get()));
            refreshTimeInfo();
            final TextView directListTimeInfo = (TextView) findViewById(R.id.remain_times_direc_list);
            directListTimeInfo.setTag(R.id.estimated_arrival_time, getFormatedEstimateArrivalTime(getETA(remainingTime.get()), route.getTimezoneOffset()));
            directListTimeInfo.setTag(R.id.remaining_travel_time, getFormatedRemainingTime(remainingTime.get()));
            refreshDirectListTimeInfo();
            if (nearestNode.getFlag() != 0) {
                showNavigationInformation(location, nearestNode);
            } else {
                // find the closest RouteNode with a non-zero flag
                RouteNode node = nearestNode;
                while (node.getNextNode() != null) {
                    node = node.getNextNode();
                    if (node.getFlag() != 0) {
                        showNavigationInformation(location, node);
                        break;
                    }
                }
            }
		}
		
		if(ttsBuffer.isEmpty()){
		    ttsDelayCnt.set(0);
		}else if(ttsDelayCnt.incrementAndGet() > countOutOfRouteThreshold){
		    speakIfTtsEnabled(ttsBuffer.remove(0), false);
		}

		if(!isReplay.get()) {
			trajectory.accumulate(location, linkId);
	
			if (!arrived.get() && trajectory.size() >= 8) {
				saveTrajectory();
			}
		}

		if(sendOnMyWayEmail.getAndSet(false)) {
			sendImComingMsg(remainingTime.get());
		}

		if (!arrived.get() && !getRouteOrReroute().getNodes().isEmpty()) {
			startCountDownTime = reservation.getStartCountDownTime(lat, lng, speedInMph, startCountDownTime);
			if(reservation.hasArrivedAtDestination(lat, lng, startCountDownTime)) {
				arrived.set(true);
				arriveAtDestination();
				Log.d("ValidationActivity", "Arriving at destination");
	
				try {
					Log.d("ValidationActivity", "trajectory = "
							+ trajectory.toJSON().toString());
				} catch (JSONException e) {
					ehs.registerException(e);
				}
			}
		}
		// for resume interrupt trip
		if(!isReplay.get()) {
			writeTripLog();
		}
		//show current location
	    mapView.getMapSettings().setCurrentPositionShown(true);
	}
	
	private void showIncidentsIfNessary() {
		if(DebugOptionsActivity.isIncidentEnabled(ValidationActivity.this) && mapView.getMapSettings().getMapDisplayMode() == SKMapDisplayMode.MODE_2D) {
			mapView.deleteAllAnnotationsAndCustomPOIs();
			drawDestinationAnnotation(reservation.getEndlat(), reservation.getEndlon());
			List<Incident> incidentsOfTime = getIncidentsOfTime();
			Log.d("ValidationActivity", "show incident size : " + incidentsOfTime.size());
			for(Incident incident : incidentsOfTime) {
				SKAnnotation incAnn = new SKAnnotation();
				incAnn.setUniqueID(SkobblerUtils.getUniqueId(incident.lat, incident.lon));
				incAnn.setLocation(new SKCoordinate(incident.lon, incident.lat));
				incAnn.setMininumZoomLevel(incident.getMinimalDisplayZoomLevel());
//				incAnn.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_MARKER);
				SKAnnotationView iconView = new SKAnnotationView();
				ImageView incImage = new ImageView(ValidationActivity.this);
				incImage.setImageBitmap(Misc.getBitmap(ValidationActivity.this, IncidentIcon.fromType(incident.type).getResourceId(ValidationActivity.this), 1));
				iconView.setView(incImage);
				incAnn.setAnnotationView(iconView);
				mapView.addAnnotation(incAnn, SKAnimationSettings.ANIMATION_NONE);
			}
		}
	}
	
	private void removeAllIncident() {
		if(DebugOptionsActivity.isIncidentEnabled(ValidationActivity.this)) {
			mapView.deleteAllAnnotationsAndCustomPOIs();
			drawDestinationAnnotation(reservation.getEndlat(), reservation.getEndlon());
		}
	}
	
	private List<Incident> getIncidentsOfTime() {
		List<Incident> incidentOfDepTime = new ArrayList<Incident>();
    	if(incidents != null && incidents.size() > 0) {
    		for(Incident incident : incidents.values()) {
    			if(incident.severity > 0 && incident.isInTimeRange(reservation.getDepartureTimeUtc())) {
    				incidentOfDepTime.add(incident);
    			}
    		}
    	}
    	return incidentOfDepTime;
	}
	
	private AtomicLong etaDelay = new AtomicLong();
	
	private AtomicLong remainingTime = new AtomicLong(); 

	private long getETA(long remainTime){
	    Time currentTime = new Time();
        currentTime.setToNow();
	    return currentTime.toMillis(false) + remainTime * 1000; 
	}
	
	private AtomicBoolean turnOffGPS = new AtomicBoolean();
	
	private AtomicBoolean arrivalMsgTiggered = new AtomicBoolean();
	
	private void displayArrivalMsg(final Runnable callback) {
		if (isTripValidated()) {
		    arrivalMsgTiggered.set(true);
		    
		    ResumeNavigationUtils.cleanTripLog(ValidationActivity.this);
		    
		    if(!isReplay.get()) {
			    saveTrajectory(new Runnable() {
	                @Override
	                public void run() {
	                    saveTrip(new Runnable() {
	                        @Override
	                        public void run() {
	                            TripService.runImd(ValidationActivity.this, User.getCurrentUser(ValidationActivity.this), reservation.getRid());
	                        }
	                    });
	                }
	            });
		    }
		    
		    SKPosition currentPosition = mapView.getCurrentGPSPosition(true);
		    if(currentPosition != null) {
		    	Intent updateMyLocation = new Intent(LandingActivity2.UPDATE_MY_LOCATION);
				updateMyLocation.putExtra("lat", currentPosition.getLatitude());
				updateMyLocation.putExtra("lon", currentPosition.getLongitude());
				sendBroadcast(updateMyLocation);
		    }
		    
			turnOffGPS.set(true);
			// turn off GPS
			if(locationManager != null) {
				locationManager.removeUpdates(locationListener);
			}
			
			findViewById(R.id.loading).setVisibility(View.VISIBLE);
			
			if(isReplay.get()) {
				int uPoints = 0;
				double co2Value = 0, timeSavingInMinute = 0;
				String message = "", voice = "";
				if(StringUtils.isNotBlank(trajectoryData)) {
					try {
						JSONObject trajectoryJson = new JSONObject(trajectoryData);
						uPoints = trajectoryJson.optInt("credit", 0);
						co2Value = trajectoryJson.optDouble("co2_saving", 0);
						timeSavingInMinute = trajectoryJson.optDouble("time_saving_in_second", 0) / 60;
						voice = trajectoryJson.optString("voice", "");
						message = trajectoryJson.optString("message", "");
					}
					catch(Exception ignore) {}
				}
				doDisplayArrivalMsg(uPoints, co2Value, message, voice, timeSavingInMinute);
			}
			else {
	            new Handler().postDelayed(new Runnable() {
	                @Override
	                public void run() {
	                    try{
	                        if(callback!=null) {
	                            callback.run();
	                        }
	                        showNotifyLaterDialog();
	                    }catch(Throwable t){}
	                }
	            }, Request.fifteenSecsTimeout * 2);
			}
		}
	}
	
	private void writeTripLog() {
		try {
			JSONObject tripLog = new JSONObject();
			tripLog.put(ResumeNavigationUtils.DESTINATION_TIME, reservation.getArrivalTimeUtc());
			tripLog.put(ResumeNavigationUtils.DEST_LAT, reservation.getEndlat());
			tripLog.put(ResumeNavigationUtils.DEST_LON, reservation.getEndlon());
			FileUtils.writeStringToFile(ResumeNavigationUtils.getFile(ValidationActivity.this, reservation.getRid()), tripLog.toString());
		}
		catch(Exception ignore) {}
	}
	
	private void showNotifyLaterDialog() {
		if (!arrivalMsgDisplayed.getAndSet(true)) {
			findViewById(R.id.loading).setVisibility(View.GONE);
			NotificationDialog2 dialog = new NotificationDialog2(ValidationActivity.this, "There's a temporary connection issue, but we'll update your trip results shortly. Thanks for your patience!");
	    	dialog.setTitle("Thanks for using Metropia");
	    	dialog.setPositiveButtonText("OK");
	    	dialog.setPositiveActionListener(new ActionListener() {
				@Override
				public void onClick() {
					finish();
				}
	    	});
	    	dialog.show();
	    	navigationView.notifyIfNecessary("There's a temporary connection issue, but we'll update your trip results shortly. Thanks for your patience!", false);
		}
	}
	
	private AtomicBoolean arrivalMsgDisplayed = new AtomicBoolean();
	
	private void doDisplayArrivalMsg(int uPoints, double co2Value,
	        String message, String voice, double timeSavingInMinute){
	    if(!arrivalMsgDisplayed.get()){
	        arrivalMsgDisplayed.set(true);
	        hideEnRouteAlert();
	        findViewById(R.id.loading).setVisibility(View.GONE);
    	    navigationView.setVisibility(View.GONE);
            final View panel = findViewById(R.id.congrats_panel);
            String dest = reservation.getDestinationAddress();
            
            if(StringUtils.isNotBlank(message)){
                String msg = message + "\n" + 
                    dest.substring(0, dest.indexOf(",")>-1?dest.indexOf(","):dest.length());
                TextView congratsMsg = (TextView) findViewById(R.id.congrats_msg);
                congratsMsg.setText(formatCongrMessage(ValidationActivity.this, msg));
                congratsMsg.setVisibility(View.VISIBLE);
                findViewById(R.id.congrats_msg_shadow).setVisibility(View.VISIBLE);
                navigationView.notifyIfNecessary(message, false);
            }
            
            TextView co2 = (TextView) findViewById(R.id.co2_circle);
            if(co2Value != 0) {
                String co2String = co2Value + "lbs\nCO2";  
                co2.setText(formatCO2Desc(ValidationActivity.this, co2String));
                ((ImageView)findViewById(R.id.co2_circle_background)).setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.blue_circle)));
                findViewById(R.id.co2_circle_panel).setVisibility(View.VISIBLE);
            }
            
            TextView mpoint = (TextView) findViewById(R.id.mpoint_circle);
            if(uPoints > 0){
            	LocalyticsUtils.tagTrip(LocalyticsUtils.COMPLETED_TRIP);
                mpoint.setText(formatCongrValueDesc(ValidationActivity.this, uPoints + "\nPoints"));
                ((ImageView)findViewById(R.id.mpoint_circle_background)).setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.green_circle)));
                findViewById(R.id.mpoint_circle_panel).setVisibility(View.VISIBLE);
            }
            else {
            	LocalyticsUtils.tagTrip(LocalyticsUtils.ABORTED_TRIP);
            }
            
            TextView driveScore = (TextView) findViewById(R.id.drive_score_circle);
            if(timeSavingInMinute > 0) {
                String scoreString = new DecimalFormat("0.#").format(timeSavingInMinute) + "\nminutes"; 
                driveScore.setText(formatCongrValueDesc(ValidationActivity.this, scoreString));
                ((ImageView)findViewById(R.id.drive_score_circle_background)).setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.red_circle)));
                findViewById(R.id.drive_score_circle_panel).setVisibility(View.VISIBLE);
            }
            
            ImageView share = (ImageView) findViewById(R.id.share);
            share.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.menu_share_new)));
            
            Font.setTypeface(Font.getRobotoBold(getAssets()), co2, mpoint, driveScore, 
            		(TextView) findViewById(R.id.congrats_msg), 
            		(TextView) findViewById(R.id.close), 
            		(TextView) findViewById(R.id.feedback));
            
            //hide map view options
            findViewById(R.id.mapview_options).setVisibility(View.GONE);
            panel.setVisibility(View.VISIBLE);
            Misc.fadeIn(ValidationActivity.this, panel);
            
            if(StringUtils.isNotBlank(voice)){
                speakIfTtsEnabled(voice, true);
            }
	    }
	}

	private void arriveAtDestination() {
		if(!isReplay.get()) {
			saveTrajectory();
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				displayArrivalMsg(null);
			}
		});
	}
	
	private boolean isCloseToOrigin() {
		if(lastKnownLocation != null && getRouteOrReroute() != null) {
			RouteNode firstNode = getRouteOrReroute().getFirstNode();
			double speedInMph = Trajectory.msToMph(lastKnownLocation.getSpeed());
			return RouteNode.distanceBetween(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), firstNode.getLatitude(), firstNode.getLongitude()) < ValidationParameters.getInstance().getArrivalDistanceThreshold() && 
					speedInMph < ValidationParameters.getInstance().getStopSpeedThreshold(); 
		}
		return false;
	}

	private void reportValidation(final Runnable callback) {
		if (!reported.get()) {
			reported.set(true);

			if (isTripValidated()) {
			    runOnUiThread(new Runnable() {
		            @Override
		            public void run() {
		                displayArrivalMsg(callback);
		            }
			    });
			}
		}
	}

	private boolean isTripValidated() {
		return true;
	}

	private void deactivateLocationService() {
		if (fakeLocationService != null) {
			fakeLocationService.cancel();
		}
	}

	private void cancelValidation() {
	    if(arrivalMsgTiggered.get()){
            if (!isFinishing()) {
                finish();
            }
        }else{
    		// Ask the user if they want to quit
            NotificationDialog2 dialog = new NotificationDialog2(ValidationActivity.this, "Are you sure?");
        	dialog.setTitle("Exit Navigation");
        	dialog.setVerticalOrientation(false);
        	dialog.setPositiveButtonText("No");
        	dialog.setNegativeButtonText("Yes");
        	dialog.setNegativeActionListener(new NotificationDialog2.ActionListener() {
				@Override
				public void onClick() {
					LocalyticsUtils.tagTrip(LocalyticsUtils.TRIP_EXITED_MANUALLY);
					doCancelValidation();
				}
			});
        	dialog.setPositiveActionListener(new NotificationDialog2.ActionListener() {
				@Override
				public void onClick() {
					// do nothing
				}
			});
        	dialog.show();
        }
	}
	
	private void removeTripFromLandingPage(){
        DebugOptionsActivity.addTerminatedReservIds(ValidationActivity.this, reservation.getRid());
        sendBroadcast(new Intent(LandingActivity2.TRIP_INFO_CACHED_UPDATES));
    }
	
	boolean cancelTrip = false;
	
	private void doCancelValidation() {
		cancelTrip = true;
        
        reportValidation(new Runnable() {
			@Override
			public void run() {
				restoreMusic();
				if (mTts != null) {
					mTts.shutdown();
					mTts = null;
				}
			}
        });
        
        
        // Stop the activity
        if (!isTripValidated()) {
        	if(!isReplay.get()) {
	            saveTrajectory(new Runnable() {
	                @Override
	                public void run() {
	                    saveTrip(null);
	                }
	            });
        	}
            if (!isFinishing()) {
                finish();
            }
        }
    }

	private class ValidationLocationListener implements LocationListener {

		Location lastLocation;

		public void onLocationChanged(Location location) {
			//Log.d(this.getClass().toString(), String.format("onLocationChanged: %s", location));
			SharedPreferences debugPrefs = getSharedPreferences(
					DebugOptionsActivity.DEBUG_PREFS, MODE_PRIVATE);
			int gpsMode = debugPrefs.getInt(DebugOptionsActivity.GPS_MODE,
					DebugOptionsActivity.GPS_MODE_DEFAULT);
			if (gpsMode == DebugOptionsActivity.GPS_MODE_REAL) {
				if (isBetterLocation(location, lastLocation)) {
					lastLocation = location;
					locationChanged(location);
				}
			} else {
				locationChanged(location);
			}
			
			final boolean currentMode = SkobblerUtils.isDayMode();
			if(dayMode.get() != currentMode) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mapView.getMapSettings().setMapStyle(SkobblerUtils.getMapViewStyle(ValidationActivity.this, currentMode));
						TextView timeInfo = (TextView) findViewById(R.id.remain_times);
						if(currentMode) {
							timeInfo.setTextColor(Color.parseColor("#ad000000"));
						}
						else {
							timeInfo.setTextColor(Color.parseColor("#adffffff"));
						}
						dayMode.set(currentMode);
						mapView.getMapSettings().setStreetNamePopupsShown(!dayMode.get());
					}
				});
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(this.getClass().toString(), String.format(
					"onStatusChanged: %s, %d, %s", provider, status, extras));
		}

		public void onProviderEnabled(String provider) {
			Log.d(this.getClass().toString(),
					String.format("onProviderEnabled: %s", provider));
		}

		public void onProviderDisabled(String provider) {
			Log.d(this.getClass().toString(),
					String.format("onProviderDisabled: %s", provider));
		}
	}

	public static final int TWO_MINUTES = 1000 * 60 * 2;

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	public static boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	/**
	 * Fake data player
	 */
	private class FakeLocationService extends TimerTask {
		private Timer timer;
		private LocationListener listener;
		// private Queue<RouteNode> nodes;

		private Trajectory trajectory;

		private int interval;

		private String trajectoryData;

		int pollCnt;

		public FakeLocationService(LocationListener listener, int interva,
				String trajectoryData) {
			this(listener, interva, null, trajectoryData);
		}

		public FakeLocationService(LocationListener listener, int interval,
		        Trajectory trajectory, String trajectoryData) {
			this.listener = listener;
			this.interval = interval;
			this.trajectoryData = trajectoryData;

			if (trajectory == null) {
				try {
					this.trajectory = Trajectory.from(new JSONObject(trajectoryData).getJSONArray("trajectory"));
				} catch (Throwable e) {
				    Log.e("ValidationActivity", Log.getStackTraceString(e));
				}
			} else {
				this.trajectory = trajectory;
			}

			timer = new Timer();
			timer.schedule(this, 1000, interval);
		}

		FakeLocationService setInterval(int millisecond) {
			FakeLocationService rtn;
			if (interval != millisecond) {
				cancel();
				rtn = new FakeLocationService(listener, millisecond,
						trajectory, trajectoryData);
			} else {
				rtn = this;
			}
			return rtn;
		}

		@Override
		public void run() {
			if (trajectory == null || trajectory.size() == 0) {
				timer.cancel();
			} else {
				Location location = new Location("");
				location.setTime(System.currentTimeMillis());
				Record record = trajectory.poll();
				pollCnt++;
				location.setLatitude(record.getLatitude());
				location.setLongitude(record.getLongitude());
				location.setSpeed(record.getSpeed());
				location.setBearing(record.getHeading());
				location.setAccuracy(record.getAccuracy());
//				Log.i("FakeLocation", pollCnt + "," + location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy());
                listener.onLocationChanged(location);
			}
		}

		void skip(int cnt) {
			for (int i = 0; i < cnt; i++) {
				trajectory.poll();
			}
		}

	}

	@Override
	public void onInit(int status) {
		if (mTts != null) {
			mTts.setLanguage(Locale.US);
			mTts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
				@Override
				public void onUtteranceCompleted(String utteranceId) {
				    if(String.valueOf(utteranceCompletedCnt.incrementAndGet()).equals(
				            String.valueOf(utteranceCnt.get()))){
				        restoreMusic();
				    }
				}
			});
			navigationView.setListener(new CheckPointListener() {
				@Override
				public void onCheckPoint(String navText, boolean flush, boolean delayed) {
					if (!arrived.get()) {
					    if(flush){
					        speakIfTtsEnabled(navText, flush);
					        ttsBuffer.clear();
					    }else if(delayed){
					        ttsBuffer.add(navText);
					    }else{
					        speakIfTtsEnabled(navText, false);
					    }
					}
				}
			});
		}
	}

	private void speakIfTtsEnabled(String text, boolean flush) {
		if (MapDisplayActivity.isNavigationTtsEnabled(this) && !isCloseToOrigin()) {
			speak(text, flush);
		}
	}
	
	private void enRouteCueSpeak(String text, boolean flush) {
		if(mTts != null) {
			mTts.stop();
			ttsBuffer.clear();
			mTts.setLanguage(Locale.US);
			speak(text, flush);
		}
	}
	
	private void speak(String text, boolean flush) {
		if (mTts != null) {
			try {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
			        String.valueOf(utteranceCnt.incrementAndGet()));
				params.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
						String.valueOf(AudioManager.STREAM_MUSIC));
				AudioManager am = (AudioManager) ValidationActivity.this
			        .getSystemService(Context.AUDIO_SERVICE);
				am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, 
			        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
				if(flush){
				    utteranceCompletedCnt.set(utteranceCnt.get() - 1);
				}
				mTts.speak(text, flush?TextToSpeech.QUEUE_FLUSH:TextToSpeech.QUEUE_ADD, params);
			} catch (Throwable t) {
			}
		}
	}

	private void restoreMusic() {
	    AudioManager am = (AudioManager) ValidationActivity.this
            .getSystemService(Context.AUDIO_SERVICE);
	    am.abandonAudioFocus(this);
	}

	@Override
	protected void onDestroy() {
		restoreMusic();
		unregisterReceiver(timeoutReceiver);
		unregisterReceiver(tripValidator);
//		unregisterReceiver(enRouteCheck);
		if (locationManager != null) {
			locationManager.removeUpdates(locationListener);
		}
		deactivateLocationService();
		NavigationView.removeNotification(this);
		if (mTts != null) {
			mTts.shutdown();
		}
		if (Request.NEW_API && !arrivalMsgTiggered.get() && isTripValidated() && !isReplay.get()) {
	        saveTrajectory(new Runnable() {
                @Override
                public void run() {
                    saveTrip(null);
                }
            });
		}
		
		try {
			FileUtils.cleanDirectory(getDir(ValidationActivity.this));
		} catch (Exception ignore) { ignore.printStackTrace();}
		
		super.onDestroy();
		SKMaps.getInstance().destroySKMaps();
	}
	
    @Override
    public void onAudioFocusChange(int focusChange) {
        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d("ValidationActivity", "Request Code : " + requestCode + " result Code : " + resultCode);
        if(requestCode == REPORT_PROBLEM && resultCode == Activity.RESULT_OK) {
            doCancelValidation();
        }
        else if(requestCode == ON_MY_WAY && resultCode == Activity.RESULT_OK) {
        	Bundle extras = intent == null?null:intent.getExtras();
        	emails = extras.getString(EMAILS);
        	phones = extras.getString(PHONES);
        	if(StringUtils.isNotBlank(phones)) {
        		sendOnMyWaySms();
        	}
        	if(StringUtils.isNotBlank(emails)) {
        		sendOnMyWayEmail.set(true);
        	}
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	navigationView.setLandscapMode();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        	navigationView.setPortraitMode();
        }
        changeEnRoutePanelMode(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
        refreshTimeInfo();
        refreshDirectListTimeInfo();
    }
    
    private static final String TIME_INFO_CYCLE = "TIME_INFO_CYCLE"; 
    
    private void scheduleTimeInfoCycle(){
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 5000, 
            PendingIntent.getBroadcast(this, 0, new Intent(TIME_INFO_CYCLE), PendingIntent.FLAG_UPDATE_CURRENT));
    }
    
    private BroadcastReceiver timeInfoCycler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(findViewById(R.id.remain_times).getTag(R.id.clicked) == null){
                toggleTimeInfo();
            }
            toggleDirectionListTimeInfo();
        }
    };
    
    public static final String TRIP_VALIDATOR = "TRIP_VALIDATOR";
    
    public static final String ID = "ID";
    
    public static final String CREDIT = "CREDIT";
    
    public static final String TIME_SAVING_IN_MINUTE = "TIME_SAVING_IN_MINUTE";
    
    public static final String CO2_SAVING = "CO2_SAVING";
    
    public static final String VOICE = "VOICE";
    
    public static final String MESSAGE = "MESSAGE";
    
    public static final String REQUEST_SUCCESS = "REQUEST_SUCCESS";
    
    private BroadcastReceiver tripValidator = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if(arrivalMsgTiggered.get()) {
	            String id = intent.getStringExtra(ID);
	            boolean success = intent.getBooleanExtra(REQUEST_SUCCESS, false);
	            if(String.valueOf(reservation.getRid()).equals(id) && success){
	                String message = intent.getStringExtra(MESSAGE);
	                double co2Saving = intent.getDoubleExtra(CO2_SAVING, 0);
	                int credit = intent.getIntExtra(CREDIT, reservation.getCredits());
	                String voice = intent.getStringExtra(VOICE);
	                double timeSavingInMinute = intent.getDoubleExtra(TIME_SAVING_IN_MINUTE, 0);
	                doDisplayArrivalMsg(credit, co2Saving, message, voice, timeSavingInMinute);
	            }
	            else if(String.valueOf(reservation.getRid()).equals(id) && !success) {
	            	showNotifyLaterDialog();
	            }
        	}
        }
    };
    
    private long realTravelRemainTimeInSec = -1;  //sec
    private AtomicLong lastEnRouteCheckTime = new AtomicLong(-100);
    
    private void enrouteCheck() {
    	AsyncTask<Void, Void, Route> checkTask = new AsyncTask<Void, Void, Route>() {
			@Override
			protected Route doInBackground(Void... params) {
				if(lastKnownLocation != null) {
					realTravelRemainTimeInSec = -1;
					double realRemainTimeInMin = -1;
					Route enRoute = null;
					lastEnRouteCheckTime.set(System.currentTimeMillis());
					Log.d("ValidationActivity", "En-Route Check");
					try {
						TravelTimeRequest travelTimeReq = new TravelTimeRequest(User.getCurrentUser(ValidationActivity.this), 
								reservation.getCity(), getRouteOrReroute().getRemainNodes(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
						realRemainTimeInMin = travelTimeReq.execute(ValidationActivity.this);  // min
						realTravelRemainTimeInSec = Double.valueOf(realRemainTimeInMin * 60).longValue(); // sec
						if(realRemainTimeInMin > 0 && (realTravelRemainTimeInSec - remainingTime.get()) >= Math.max(5 * 60, 0.2 * getRouteOrReroute().getDurationFromNodes())) {
							enRoute = getNewRoute(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), lastKnownLocation.getSpeed(), lastKnownLocation.getBearing());
							if(enRoute != null && (realTravelRemainTimeInSec - enRoute.getDurationFromNodes()) > Math.max(3 * 60, 0.15 * realTravelRemainTimeInSec)) {
								((TextView) findViewById(R.id.en_route_debug_msg)).setText(
										String.format(getResources().getString(R.string.en_route_debug_msg), getFormatedEstimateArrivalTime(getETA(remainingTime.get()), route.getTimezoneOffset()), 
												getFormatedEstimateArrivalTime(getETA(realTravelRemainTimeInSec), route.getTimezoneOffset()), 
												getFormatedEstimateArrivalTime(getETA(enRoute.getDurationFromNodes()), route.getTimezoneOffset())));
								return enRoute;
							}
						}
					}
					catch(Exception ignore) {
						Log.d("EnRoute", Log.getStackTraceString(ignore));
					}
				}
				return null;
			}
			
			@Override
	        protected void onPostExecute(Route result) {
    			if(result != null) {
    				enRoute = result;
    				drawEnRoute.set(true);
    				drawRoute(mapView, enRoute);
    			}
    		}
    		
    	};
    	Misc.parallelExecute(checkTask);
    }
    
    private Route enRoute;
    private CountDownTimer countDown;
    private AtomicBoolean drawEnRoute = new AtomicBoolean(false);
    
    private void showEnRouteAlert() {
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				findViewById(R.id.loading).setVisibility(View.GONE);
	    	    navigationView.setVisibility(View.GONE);
	    	    findViewById(R.id.directions_view).setVisibility(View.INVISIBLE);
	    	    findViewById(R.id.mapview_options).setVisibility(View.GONE);
	    	    findViewById(R.id.en_route_alert_panel).setVisibility(View.VISIBLE);
	    	    if(DebugOptionsActivity.isEnrouteDebugMsgEnabled(ValidationActivity.this)) {
	    	    	findViewById(R.id.en_route_debug_msg).setVisibility(View.VISIBLE);
	    	    }
	    	    StringBuffer alertMessage = new StringBuffer(getResources().getText(R.string.en_route_desc1));
	    	    alertMessage.append("\n").append(getResources().getText(R.string.en_route_desc2));
	    	    enRouteCueSpeak(alertMessage.toString(), true);
	    	    final CharSequence autoAcceptDesc = getResources().getText(R.string.en_route_auto_accept);
	    	    final NumberFormat nf = new DecimalFormat("#");
	    	    String message = String.format(autoAcceptDesc.toString(), nf.format(5));
	    		((TextView)findViewById(R.id.en_route_auto_accept_desc)).setText(formatAutoAcceptDesc(message));
	    	    countDown = new CountDownTimer(6000, 1000) {
	    	    	public void onTick(long millisUntilFinished) {
	    	    		String message = String.format(autoAcceptDesc.toString(), nf.format(millisUntilFinished / 1000));
	    	    		((TextView)findViewById(R.id.en_route_auto_accept_desc)).setText(formatAutoAcceptDesc(message));
	   		     	}

	   		     	public void onFinish() {
	   		     		findViewById(R.id.yes_button).performClick();
	   		     	}
	   		 	};
	   		 	
	   		 	new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						countDown.start();
						if(recognizer != null) {
			   		 		((TextView)findViewById(R.id.voice_input_debug_msg)).setText("");
			   		 		switchSearch(KWS_SEARCH);
			   		 	}
					}
	   		 	}, 2000);
	   		 	
	   		 	buttonFollow.setTag(Boolean.valueOf(false));
	   		 	RouteRect enRouteRect = initRouteRect(enRoute);
	   		 	to2DMap(enRouteRect, false);
	   		 	if(DebugOptionsActivity.isEnrouteVoiceInputDebugMsgEnabled(ValidationActivity.this)) {
	   		 		findViewById(R.id.voice_input_debug_msg).setVisibility(View.VISIBLE);
	   		 	}
			}
    	});
    }
    
    private SpannableString formatAutoAcceptDesc(String desc) {
    	int firstNumIdx = getFirstNumberIndex(desc);
    	SpannableString autoAcceptDesc = SpannableString.valueOf(desc);
    	if(firstNumIdx > 0) {
    		autoAcceptDesc.setSpan(new AbsoluteSizeSpan(ValidationActivity.this.getResources()
    				.getDimensionPixelSize(R.dimen.medium_font)), firstNumIdx, firstNumIdx + 1,
    				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    	}
    	return autoAcceptDesc;
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
    
    private void hideEnRouteAlert() {
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for(View view : getMapViews()) {
					view.setVisibility(View.VISIBLE);
				}
	    	    findViewById(R.id.directions_view).setVisibility(View.INVISIBLE);
	    	    findViewById(R.id.en_route_alert_panel).setVisibility(View.GONE);
	    	    findViewById(R.id.en_route_debug_msg).setVisibility(View.GONE);
	    	    ((TextView)findViewById(R.id.en_route_auto_accept_desc)).setText("");
	    	    enRoute = null;
	    	    buttonFollow.setTag(Boolean.valueOf(true));
	    	    to3DMap();
	    	    if(recognizer != null) {
	    	    	recognizer.stop();
	    	    	enRouteResultTriggered.set(false);
	    	    }
			}
    	});
    }
    
    private void changeEnRoutePanelMode(boolean landscape) {
    	if(findViewById(R.id.en_route_alert_panel).getVisibility() == View.VISIBLE) {
	    	View enRoutePanel = findViewById(R.id.alert_content);
	    	RelativeLayout.LayoutParams enRoutePanelLp = (LayoutParams) enRoutePanel.getLayoutParams();
	    	enRoutePanelLp.width = landscape ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT;
	    	enRoutePanelLp.height = landscape ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT;
	    	enRoutePanelLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
	    	enRoutePanelLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
	    	enRoutePanelLp.addRule(landscape ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_BOTTOM , RelativeLayout.TRUE);
	    	enRoutePanel.setLayoutParams(enRoutePanelLp);
    	}
    }
    
//    private void scheduleNextEnRouteCheck(){
//        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
//        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5 * 60 * 1000, 5 * 60 * 1000, 
//            PendingIntent.getBroadcast(this, 0, new Intent(ENROUTE_CHECK), PendingIntent.FLAG_UPDATE_CURRENT));
//    }
//    
//    private static final String ENROUTE_CHECK = "ENROUTE_CHECK";
//    
//    private BroadcastReceiver enRouteCheck = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            User.initializeIfNeccessary(context, new Runnable() {
//                @Override
//                public void run() {
//                	if(!arrivalMsgTiggered.get()) {
//                		enrouteCheck();
//                	}
//                }
//            });
//        }
//    };

	@Override
	public void onActionPan() {
	}

	@Override
	public void onActionZoom() {
	}
	
	private static final Integer INCIDENT_BALLOON_ID = Integer.valueOf(1234);
	
	@Override
	public void onAnnotationSelected(SKAnnotation annotation) {
		int selectedAnnotationId = annotation.getUniqueID();
		Incident selectedInc = incidents.get(selectedAnnotationId);
		if(selectedInc != null && mapView.getZoomLevel() >= selectedInc.getMinimalDisplayZoomLevel()) {
			DisplayMetrics dm = getResources().getDisplayMetrics();
            SKAnnotation fromAnnotation = new SKAnnotation();
            fromAnnotation.setUniqueID(INCIDENT_BALLOON_ID);
            SKAnnotationView fromView = new SKAnnotationView();
            fromAnnotation.setLocation(annotation.getLocation());
            fromAnnotation.setOffset(new SKScreenPoint(0, Dimension.dpToPx(60, dm)));
            ImageView balloon = new ImageView(ValidationActivity.this);
            balloon.setImageBitmap(loadBitmapFromView(ValidationActivity.this, selectedInc));
            fromView.setView(balloon);
            fromAnnotation.setAnnotationView(fromView);
            mapView.addAnnotation(fromAnnotation, SKAnimationSettings.ANIMATION_POP_OUT);
            View roadPanel = findViewById(R.id.road_panel);
            SKScreenPoint annotationPoint = mapView.coordinateToPoint(annotation.getLocation());
            SKScreenPoint centerPoint = new SKScreenPoint();
            centerPoint.setY(annotationPoint.getY() - roadPanel.getMeasuredHeight());
            centerPoint.setX(annotationPoint.getX());
            mapView.centerMapOnPositionSmooth(mapView.pointToCoordinate(centerPoint), 500);
		}
	}
	
	private View incidentBalloon;
	
	public Bitmap loadBitmapFromView(Context ctx, Incident selectedInc) {
		if(incidentBalloon == null) {
			FrameLayout layout = new FrameLayout(ctx);
			ViewGroup.LayoutParams layoutLp = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(layoutLp);
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			incidentBalloon = inflater.inflate(R.layout.incident_balloon, layout);
		}
        TextView textView = (TextView) incidentBalloon.findViewById(R.id.text);
        textView.setText(selectedInc.shortDesc);
        Font.setTypeface(Font.getRegular(ctx.getAssets()), textView);
        incidentBalloon.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        incidentBalloon.layout(0, 0, incidentBalloon.getMeasuredWidth(), incidentBalloon.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(incidentBalloon.getMeasuredWidth(), incidentBalloon.getMeasuredHeight(), Bitmap.Config.ARGB_8888);                
        Canvas canvas = new Canvas(bitmap);
        incidentBalloon.draw(canvas);
        return bitmap;
	}
	
	@Override
	public void onCompassSelected() {
	}

	@Override
	public void onCustomPOISelected(SKMapCustomPOI arg0) {
	}

	@Override
	public void onDoubleTap(SKScreenPoint point) {
		mapView.zoomInAt(point);
	}

	@Override
	public void onInternationalisationCalled(int arg0) {
	}

	@Override
	public void onInternetConnectionNeeded() {
	}

	@Override
	public void onLongPress(SKScreenPoint point) {
		SharedPreferences debugPrefs = getSharedPreferences(
                DebugOptionsActivity.DEBUG_PREFS, MODE_PRIVATE);
        int gpsMode = debugPrefs.getInt(DebugOptionsActivity.GPS_MODE,
                DebugOptionsActivity.GPS_MODE_DEFAULT);
        if (gpsMode == DebugOptionsActivity.GPS_MODE_LONG_PRESS) {
        	SKCoordinate coordinate = mapView.pointToCoordinate(point);
            Location location = new Location("");
            location.setLatitude(coordinate.getLatitude());
            location.setLongitude(coordinate.getLongitude());
            location.setSpeed(9999f);
            location.setTime(System.currentTimeMillis());
            locationChanged(location);
        }
        
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
	public void onMapRegionChanged(SKCoordinateRegion region) {
	}

	@Override
	public void onPOIClusterSelected(SKPOICluster arg0) {}

	@Override
	public void onRotateMap() {}

	@Override
	public void onScreenOrientationChanged() {}

	@Override
	public void onSingleTap(SKScreenPoint arg0) {
		buttonFollow.setTag(Boolean.valueOf(false));
		mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.NONE);
		mapView.deleteAnnotation(INCIDENT_BALLOON_ID);
	}

	@Override
	public void onSurfaceCreated() {
	}

	@Override
	public void onCurrentPositionSelected() {
	}

	@Override
	public void onObjectSelected(int arg0) {
	}

	@Override
	public void onAllRoutesCompleted() {
	}

	@Override
	public void onOnlineRouteComputationHanging(int arg0) {
	}
	
//	@Override
//	public void onRouteCalculationCompleted(SKRouteInfo arg0) {
//		if(drawEnRoute.get()) {
//			drawEnRoute.set(false);
//			showEnRouteAlert();
//		}
//    	else {
//    		if((Boolean)buttonFollow.getTag()) {
//    			mapView.getMapSettings().setMapDisplayMode(SKMapDisplayMode.MODE_3D);
//    		}
//		}
//	}
//	
//	@Override
//	public void onRouteCalculationFailed(SKRoutingErrorCode arg0) {
//		Log.d("ValidationActivity", "draw route internal error!");
//		drawRoute(mapView, getRouteOrReroute());
//	}

	private AtomicBoolean enRouteResultTriggered = new AtomicBoolean(false);
	
	@Override
    public void onPartialResult(Hypothesis hypothesis) {
        String text = hypothesis.getHypstr();
        Log.d("ValidationActivity", "Voice : " + text);
        if(!enRouteResultTriggered.get()) {
        	((TextView)findViewById(R.id.voice_input_debug_msg)).setText(text);
        }
        if(!enRouteResultTriggered.get() && 
        		(StringUtils.startsWithIgnoreCase(text, EN_ROUTE_NOT_ACCEPT_TEXT_1) || StringUtils.startsWithIgnoreCase(text, EN_ROUTE_NOT_ACCEPT_TEXT_2))) {
        	enRouteResultTriggered.set(true);
        	findViewById(R.id.no_button).performClick();
        }
        else if(!enRouteResultTriggered.get() && StringUtils.startsWithIgnoreCase(text, EN_ROUTE_ACCEPT_TEXT)) {
        	enRouteResultTriggered.set(true);
        	findViewById(R.id.yes_button).performClick();
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
    }

    @Override
    public void onBeginningOfSpeech() {
    	Log.d("PocketSphinxActivity", "Start Speech");
    }

    @Override
    public void onEndOfSpeech() {
    }

	@Override
	public void onRouteCalculationCompleted(int statusMessage, int routeDistance, int routeEta, boolean thisRouteIsComplete, int id) {
		if(ROUTE_INTERNAL_ERROR == statusMessage) {
			drawRoute(mapView, getRouteOrReroute());
		}
		else {
			if(drawEnRoute.get()) {
				drawEnRoute.set(false);
				showEnRouteAlert();
			}
			else {
				if((Boolean)buttonFollow.getTag()) {
					mapView.getMapSettings().setMapDisplayMode(SKMapDisplayMode.MODE_3D);
				}
			}
		}
	}

	@Override
	public void onServerLikeRouteCalculationCompleted(int arg0) {}

	@Override
	public void onDebugInfo(double arg0, float arg1, double arg2) {}

	@Override
	public void onOffportRequestCompleted(int arg0) {}

//	@Override
//	public void onServerLikeRouteCalculationCompleted(SKRouteJsonAnswer arg0) {
//	}
//
//	@Override
//	public void onBoundingBoxImageRendered(int arg0) {
//	}
//
//	@Override
//	public void onGLInitializationError(String arg0) {
//	}

}
