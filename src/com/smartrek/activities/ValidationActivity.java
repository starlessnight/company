package com.smartrek.activities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.SendTrajectoryService;
import com.smartrek.TripService;
import com.smartrek.ValidationService;
import com.smartrek.activities.DebugOptionsActivity.FakeRoute;
import com.smartrek.dialogs.FeedbackDialog;
import com.smartrek.dialogs.FloatingMenuDialog;
import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.dialogs.ShareDialog;
import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.models.Trajectory;
import com.smartrek.models.User;
import com.smartrek.requests.ImComingRequest;
import com.smartrek.requests.Request;
import com.smartrek.requests.ReservationDeleteRequest;
import com.smartrek.requests.ReservationFetchRequest;
import com.smartrek.requests.RouteFetchRequest;
import com.smartrek.ui.NavigationView;
import com.smartrek.ui.NavigationView.CheckPointListener;
import com.smartrek.ui.NavigationView.DirectionItem;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.ui.overlays.PointOverlay;
import com.smartrek.ui.overlays.RouteDebugOverlay;
import com.smartrek.ui.overlays.RoutePathOverlay;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Misc;
import com.smartrek.utils.PrerecordedTrajectory;
import com.smartrek.utils.RouteLink;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.RouteRect;
import com.smartrek.utils.SessionM;
import com.smartrek.utils.SmartrekTileProvider;
import com.smartrek.utils.StringUtil;
import com.smartrek.utils.SystemService;
import com.smartrek.utils.ValidationParameters;

public class ValidationActivity extends FragmentActivity implements OnInitListener {
	public static final int DEFAULT_ZOOM_LEVEL = 18;

	private static final String RESERVATION = "reservation";

	private static final String ROUTE = "route";

	private static final String START_TIME = "startTime";

	private static final String POLL_CNT = "pollCnt";

	private static final String GEO_POINT = "geoPoint";

	public static final String EMAILS = "emails";

	private ExceptionHandlingService ehs = new ExceptionHandlingService(this);

	private MapView mapView;
	private NavigationView navigationView;
	private ToggleButton volumnControl;
	private ToggleButton buttonFollow;

	/**
	 * @deprecated
	 */
	private Route route;
	
	private Route reroute;

	private Reservation reservation;

	private List<Overlay> mapOverlays;

	private PointOverlay pointOverlay;

	private long startTime;

	// FIXME: Temporary
	private RouteNode nearestNode;

	// FIXME: Temporary
	private RouteLink nearestLink;

	private Trajectory trajectory = new Trajectory();

	private LocationManager locationManager;

	private LocationListener locationListener;

	private Location lastKnownLocation;

	private FakeLocationService fakeLocationService;

	private AtomicBoolean arrived = new AtomicBoolean(false);

	private TextToSpeech mTts;

	private ListView dirListView;

	private ArrayAdapter<DirectionItem> dirListadapter;

	private static String utteranceId = "utteranceId";

	private AtomicBoolean reported = new AtomicBoolean(false);

	private AtomicBoolean stopValidation = new AtomicBoolean(false);

	private boolean isDebugging;

	private long lastLocChanged;

	private Handler animator;

	private Typeface boldFont;

	private Typeface lightFont;

	private int savedPollCnt;

	private String emails;

	private BroadcastReceiver timeoutReceiver;

	private AtomicBoolean isOnRecreate = new AtomicBoolean(); 
	
	private GeoPoint lastCenter;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_reservation_map);

		AssetManager assets = getAssets();
		boldFont = Font.getBold(assets);
		lightFont = Font.getLight(assets);

		animator = new Handler(Looper.getMainLooper());

		isOnRecreate.set(savedInstanceState != null);

		Bundle extras = getIntent().getExtras();

		reservation = extras.getParcelable(RESERVATION);

		route = (isOnRecreate.get() ? savedInstanceState : extras)
				.getParcelable(ROUTE);
		route.setCredits(reservation.getCredits());
		reservation.setRoute(route);

		// Define a listener that responds to location updates
		locationListener = new ValidationLocationListener();

		if (isOnRecreate.get()) {
			startTime = savedInstanceState.getLong(START_TIME);
			savedPollCnt = savedInstanceState.getInt(POLL_CNT);
			emails = savedInstanceState.getString(EMAILS);
		} else {
			Time now = new Time();
			now.setToNow();
			startTime = now.toMillis(false);
			emails = extras.getString(EMAILS);
		}

		timeoutReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				stopValidation.set(true);
				final boolean tripValidated = isTripValidated();
				NotificationDialog dialog = new NotificationDialog(
						ValidationActivity.this, "Timed out!");
				dialog.setActionListener(new NotificationDialog.ActionListener() {
					@Override
					public void onClickDismiss() {
						if (tripValidated) {
						    displayArrivalMsg();
						}else if (!isFinishing()) {
							finish();
						}
					}
				});
				dialog.show();
			}
		};
		registerReceiver(timeoutReceiver,
				new IntentFilter(getClass().getName()));

		Intent timeoutIntent = new Intent(getClass().getName());
		PendingIntent pendingTimeout = PendingIntent.getBroadcast(
				ValidationActivity.this, 0, timeoutIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()
				+ (900 + reservation.getDuration() * 3) * 1000, pendingTimeout);

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
				for (View v : new View[] { view.findViewById(R.id.left_panel),
						vRoad }) {
					v.setBackgroundResource(position == 0 ? R.color.light_green
							: 0);
				}
				DirectionItem item = getItem(position);
				ImageView vDirection = (ImageView) view
						.findViewById(R.id.img_view_direction);
				if (item.drawableId == 0) {
					vDirection.setVisibility(View.INVISIBLE);
				} else {
					vDirection.setImageResource(item.drawableId);
					vDirection.setVisibility(View.VISIBLE);
				}
				vDistance
						.setText(NavigationView.adjustDistanceFontSize(
								ValidationActivity.this, StringUtil
										.formatImperialDistance(item.distance,
												true)));
				vDistance.requestLayout();
				vRoad.setText((StringUtils.isBlank(item.roadName) || StringUtils
						.equalsIgnoreCase(item.roadName, "null")) ? ""
						: item.roadName);
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

		if (isOnRecreate.get()) {
            lastCenter = new GeoPoint((IGeoPoint) savedInstanceState.getParcelable(GEO_POINT));
        }
		
		if (!loadRoute) {
			centerMap(mapView.getController(), isOnRecreate.get(), lastCenter, route);
			drawRoute(mapView, route, 0);
		}

		try {
			mTts = new TextToSpeech(this, this);
		} catch (Throwable t) {
		}

		lastLocChanged = SystemClock.elapsedRealtime();

		setVolumeControlStream(AudioManager.STREAM_NOTIFICATION);

		if (!isOnRecreate.get()) {
			if (reservation.hasExpired()) {
				stopValidation.set(true);
				NotificationDialog dialog = new NotificationDialog(this,
						getResources().getString(R.string.trip_has_expired));
				dialog.setActionListener(new NotificationDialog.ActionListener() {

					@Override
					public void onClickDismiss() {
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
				NotificationDialog dialog = new NotificationDialog(this,
						getResources().getString(
								R.string.trip_too_early_to_start, minutes));
				dialog.setActionListener(new NotificationDialog.ActionListener() {

					@Override
					public void onClickDismiss() {
						if (!isFinishing()) {
							finish();
						}
					}
				});
				dialog.show();
			}
		}
	}
	
	private boolean isLoadRoute(){
        return !isOnRecreate.get() && (isDebugging || reservation.getNavLink() != null);
	}

	private static void centerMap(IMapController mc, boolean isOnRecreate,
			GeoPoint lastCenter, Route route) {
		mc.setZoom(DEFAULT_ZOOM_LEVEL);
		GeoPoint center = null;
		if (isOnRecreate) {
		    center = lastCenter;
		} else if (route.getFirstNode() != null) {
			center = route.getFirstNode().getGeoPoint();
		}
		if (center != null) {
			mc.setCenter(center);
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
		if (pointOverlay != null) {
			geoPoint = pointOverlay.getLocation();
		} else if (firstNode != null) {
			geoPoint = firstNode.getGeoPoint();
		}
		if (geoPoint != null) {
			outState.putParcelable(GEO_POINT, geoPoint);
		}
		outState.putString(EMAILS, emails);
	}

	@Override
	protected void onStart() {
		super.onStart();
		SessionM.onActivityStart(this);

		EasyTracker.getInstance().activityStart(this);

	}

	@Override
	public void onStop() {
		super.onStop();
		SessionM.onActivityStop(this);
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SessionM.onActivityResume(this);

		SharedPreferences debugPrefs = getSharedPreferences(
				DebugOptionsActivity.DEBUG_PREFS, MODE_PRIVATE);

		// Register the listener with the Location Manager to receive location
		// updates
		int gpsMode = debugPrefs.getInt(DebugOptionsActivity.GPS_MODE,
				DebugOptionsActivity.GPS_MODE_DEFAULT);
		if (gpsMode == DebugOptionsActivity.GPS_MODE_REAL && !turnOffGPS.get()) {
			prepareGPS();
		} else if (gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED
				|| gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA
				|| gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA2
				|| gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA3
				|| gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA4) {
			int interval = DebugOptionsActivity.getGpsUpdateInterval(this);
			if (fakeLocationService == null) {
				fakeLocationService = new FakeLocationService(locationListener,
						interval, gpsMode);
			} else {
				fakeLocationService = fakeLocationService.setInterval(interval);
			}
			if (savedPollCnt > 0) {
				fakeLocationService.skip(savedPollCnt);
				savedPollCnt = 0;
			}
		} else {

		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		SessionM.onActivityPause(this);
		// TODO: Pause location service
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.validation, menu);
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

	private void initViews() {
		mapView = (MapView) findViewById(R.id.mapview);
		Misc.disableHardwareAcceleration(mapView);
		CloudmadeUtil.retrieveCloudmadeKey(this);
		mapView.setBuiltInZoomControls(false);
		mapView.setMultiTouchControls(true);
		mapView.setTileSource(new SmartrekTileProvider());
		
		mapView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                buttonFollow.setChecked(false);
                return false;
            }
        });

		TextView osmCredit = (TextView) findViewById(R.id.osm_credit);
		RelativeLayout.LayoutParams osmCreditLp = (RelativeLayout.LayoutParams) osmCredit
				.getLayoutParams();
		osmCreditLp.bottomMargin += Dimension.dpToPx(52, getResources()
				.getDisplayMetrics());
		
		buttonFollow = (ToggleButton) findViewById(R.id.center_map_icon);
        buttonFollow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (buttonFollow.isChecked()) {
                    if (lastKnownLocation != null) {
                    	double latitude = lastKnownLocation.getLatitude();
                    	double longitude = lastKnownLocation.getLongitude();
                    	IMapController mc = mapView.getController();
                    	mc.setZoom(DEFAULT_ZOOM_LEVEL);
                    	mc.animateTo(new GeoPoint(latitude, longitude));
                    }
                }
                else if(routeRect != null){
                    /* Get a midpoint to center the view of  the routes */
                    GeoPoint mid = routeRect.getMidPoint();
                    /* range holds 2 points consisting of the lat/lon range to be displayed */
                    int[] range = routeRect.getRange();
                    /* Get the MapController set the midpoint and range */
                    IMapController mc = mapView.getController();
                    mc.zoomToSpan(range[0], range[1]);
                    mc.setCenter(mid); // setCenter only works properly after zoomToSpan
                }
            }
        });

		navigationView = (NavigationView) findViewById(R.id.navigation_view);
		navigationView.setTypeface(boldFont);

		View mapViewEndTripBtn = findViewById(R.id.map_view_end_trip_btn);
		mapViewEndTripBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelValidation();
			}
		});

		volumnControl = (ToggleButton) findViewById(R.id.volumn_control);
		volumnControl.setChecked(MapDisplayActivity
				.isNavigationTtsEnabled(this));
		volumnControl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MapDisplayActivity.setNavigationTts(ValidationActivity.this,
						volumnControl.isChecked());
			}
		});

		findViewById(R.id.floating_menu_button).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						FloatingMenuDialog dialog = new FloatingMenuDialog(
								ValidationActivity.this);
						dialog.show();
					}
				});

		View menuButton = findViewById(R.id.menu_button);
		menuButton.setTag(R.id.menu_panel_status, "close");
		menuButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleMenuPanel();
			}
		});

		View menuPanel = findViewById(R.id.menu_panel);
		menuPanel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleMenuPanel();
			}
		});

		dirListView = (ListView) findViewById(R.id.directions_list);
		dirListView.setAdapter(dirListadapter);

		View directionsListMenu = findViewById(R.id.directions_list_menu);
		directionsListMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (View mView : getMapViews()) {
					mView.setVisibility(View.INVISIBLE);
				}
				findViewById(R.id.directions_view).setVisibility(View.VISIBLE);
				toggleMenuPanel();
			}
		});

		View routeViewMenu = findViewById(R.id.route_view);
		routeViewMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (View mView : getMapViews()) {
					mView.setVisibility(View.VISIBLE);
				}
				findViewById(R.id.directions_view)
						.setVisibility(View.INVISIBLE);
				toggleMenuPanel();
				if (lastKnownLocation != null) {
					double latitude = lastKnownLocation.getLatitude();
					double longitude = lastKnownLocation.getLongitude();
					IMapController mc = mapView.getController();
					mc.setZoom(DEFAULT_ZOOM_LEVEL);
					mc.animateTo(new GeoPoint(latitude, longitude));
				}
			}
		});

		View finishButton = findViewById(R.id.finish);
		finishButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ValidationActivity.this.finish();
			}
		});
		
		View shareButton = findViewById(R.id.share);
		shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = User.getCurrentUser(ValidationActivity.this);
                ShareDialog.newInstance(user.getFirstname() + " " + user.getLastname() + " is on the way",
                     "I earned " + route.getCredits() + " points for traveling at " 
                     + Reservation.formatTime(route.getDepartureTime(), true) + " to help solve traffic congestion "
                     + "using Metropia Mobile!"
                     + "\n\n" + Misc.getGooglePlayAppUrl(ValidationActivity.this))
                    .show(getSupportFragmentManager(), null);
            }
        });

		TextView destAddr = (TextView) findViewById(R.id.dest_addr);
		destAddr.setText(reservation.getDestinationAddress());

		final TextView timeInfo = (TextView) findViewById(R.id.remain_times);
        timeInfo.setTag(R.id.estimated_arrival_time, getFormatedEstimateArrivalTime(reservation.getArrivalTime()));
        timeInfo.setTag(R.id.remaining_travel_time, getFormatedRemainingTime(reservation.getDuration()));
        refreshTimeInfo();
        timeInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
		
		Font.setTypeface(lightFont, osmCredit, timeInfo);
	}
	
	   private void refreshTimeInfo(){
	        runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	                final TextView timeInfo = (TextView) findViewById(R.id.remain_times);
	                Boolean isRemainingTime = (Boolean) timeInfo.getTag();
	                if(isRemainingTime == null || !isRemainingTime){
	                    timeInfo.setText(timeInfo.getTag(R.id.estimated_arrival_time).toString());
	                }else{
	                    timeInfo.setText(timeInfo.getTag(R.id.remaining_travel_time).toString());
	                }
	            }
	        });
	    }
	    
	    private static final String timeFormat = "hh:mm a";
	    
	    private static String getFormatedEstimateArrivalTime(long time){
	        return new SimpleDateFormat(timeFormat).format(new Date(time));
	    }
	    
	    private static String getFormatedRemainingTime(long seconds){
	        long minute = Double.valueOf(Math.ceil(seconds / 60.0D)).longValue();
	        return minute + "min";
	    }

	private SpannableString formatCO2Desc(Context ctx, String co2Value) {
		String desc = co2Value + "lbs\nCO2 Reduced";
		int lbsIndex = desc.indexOf("lbs");
		SpannableString co2ValueSpan = SpannableString.valueOf(desc);
		co2ValueSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.smallest_font)), lbsIndex,
				lbsIndex + "lbs".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		int newLineIndex = desc.indexOf("\n");
		co2ValueSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.smaller_font)), newLineIndex,
				newLineIndex + "\nCO2 Reduced".length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		int twoIndex = desc.indexOf("2");
		co2ValueSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.smallest_font)), twoIndex,
				twoIndex + "2".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return co2ValueSpan;
	}

	private SpannableString formatMPointDesc(Context ctx, String mpoint) {
		String desc = mpoint + " \nmPoints earned";
		int indexOfSpace = desc.indexOf(" ");
		SpannableString mpointValueSpan = SpannableString.valueOf(desc);
		mpointValueSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.smaller_font)), indexOfSpace,
				indexOfSpace + " \nmPoints earned".length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return mpointValueSpan;
	}

	private View[] getMapViews() {
		return new View[] { findViewById(R.id.mapview),
				findViewById(R.id.navigation_view) };
	}

	private void toggleMenuPanel() {
		View menuButton = findViewById(R.id.menu_button);
		String currentStatus = menuButton.getTag(R.id.menu_panel_status)
				.toString();
		Log.d("menuStatus", currentStatus);
		if ("open".equals(currentStatus)) {
			menuButton.setBackgroundColor(getResources().getColor(
					R.color.navigation_bottom_gray));
			findViewById(R.id.menu_panel).setVisibility(View.INVISIBLE);
			menuButton.setTag(R.id.menu_panel_status, "close");
		} else {
			menuButton.setBackgroundColor(getResources().getColor(
					R.color.navigation_menu_gray));
			findViewById(R.id.menu_panel).setVisibility(View.VISIBLE);
			menuButton.setTag(R.id.menu_panel_status, "open");
		}
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

	public synchronized int[] drawRoute(MapView mapView, Route route,
			int routeNum) {
		mapOverlays = mapView.getOverlays();
		Log.d("ValidationActivity",
				String.format("mapOverlays has %d items", mapOverlays.size()));

		if (routeNum == 0)
			mapOverlays.clear();

		int latMax = (int) (-81 * 1E6);
		int lonMax = (int) (-181 * 1E6);
		int latMin = (int) (+81 * 1E6);
		int lonMin = (int) (+181 * 1E6);

		List<RouteNode> routeNodes = route.getNodes();

		int lat = 0;
		int lon = 0;

		for (int i = 0; i < routeNodes.size() - 1; i++) {
			GeoPoint point = routeNodes.get(i).getGeoPoint();

			int curLat = point.getLatitudeE6();
			int curLon = point.getLongitudeE6();

			if (i == routeNodes.size() / 2) {
				lat = curLat + 500;
				lon = curLon + 150;
			}

			latMax = Math.max(latMax, curLat);
			lonMax = Math.max(lonMax, curLon);
			latMin = Math.min(latMin, curLat);
			lonMin = Math.min(lonMin, curLon);
		}

		RoutePathOverlay pathOverlay = new RoutePathOverlay(this, route,
				RoutePathOverlay.GREEN);
		mapOverlays.add(pathOverlay);

		pointOverlay = new PointOverlay(this, 0, 0);
		pointOverlay.setColor(0xCC2020DF);
		mapOverlays.add(pointOverlay);

		RouteDebugOverlay debugOverlay = new RouteDebugOverlay(this);
		debugOverlay.setActionListener(new RouteDebugOverlay.ActionListener() {

			@Override
			public void onLongPress(double latitude, double longitude) {
				SharedPreferences debugPrefs = getSharedPreferences(
						DebugOptionsActivity.DEBUG_PREFS, MODE_PRIVATE);
				int gpsMode = debugPrefs.getInt(DebugOptionsActivity.GPS_MODE,
						DebugOptionsActivity.GPS_MODE_DEFAULT);
				if (gpsMode == DebugOptionsActivity.GPS_MODE_LONG_PRESS) {
					Location location = new Location("");
					location.setLatitude(latitude);
					location.setLongitude(longitude);
					location.setTime(System.currentTimeMillis());
					locationChanged(location);
				}
			}

		});

		mapOverlays.add(debugOverlay);

		route.setUserId(User.getCurrentUser(this).getId());

		/*
		 * Add offset of 1000 to range so that map displays extra space around
		 * route.
		 */
		int[] range = { latMax - latMin + 1500, lonMax - lonMin + 1500 };

		/*
		 * Return the range to doRoute so that map can be adjusted to range
		 * settings
		 */
		return range;
	}

	private int seq = 1;

	private void saveTrajectory() {
		final File tFile = SendTrajectoryService.getInFile(this,
				reservation.getRid(), seq++);
		final JSONArray tJson;
		try {
			tJson = trajectory.toJSON();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						new AsyncTask<Void, Void, Void>() {
							@Override
							protected Void doInBackground(Void... params) {
								try {
									FileUtils.write(tFile, tJson.toString());
								} catch (IOException e) {
								}
								return null;
							}
						}.execute();
					} catch (Throwable t) {
					}
				}
			});
		} catch (JSONException e) {
		}
		trajectory.clear();
	}

	private long lastSendImComingMsg;

	private static final long TEN_MINS = 10 * 60 * 1000;

	private void sendImComingMsg() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (emails != null
						&& System.currentTimeMillis() - lastSendImComingMsg > TEN_MINS) {
					try {
						new AsyncTask<Void, Void, Void>() {
							@Override
							protected Void doInBackground(Void... params) {
								try {
									GeoPoint loc = pointOverlay.getLocation();
									ImComingRequest req = new ImComingRequest(
											User.getCurrentUser(ValidationActivity.this),
											emails,
											loc.getLatitude(),
											loc.getLongitude(),
											reservation.getArrivalTime() + etaDelay.get(),
											NavigationView
													.metersToMiles(reservation
															.getRoute()
															.getLength()
															- reservation
																	.getRoute()
																	.getValidatedDistance()),
											reservation.getDestinationAddress());
									req.execute(ValidationActivity.this);
								} catch (Exception e) {
									ehs.registerException(e);
								}
								return null;
							}

							protected void onPostExecute(Void result) {
								String msg;
								if (ehs.hasExceptions()) {
									msg = "msg not sent, "
											+ ehs.popException().getMessage();
								} else {
									msg = "the On My Way msg sent";
								}
								Toast.makeText(ValidationActivity.this, msg,
										Toast.LENGTH_LONG).show();
							}
						}.execute();
					} catch (Throwable t) {
					}
					lastSendImComingMsg = System.currentTimeMillis();
				}
			}
		});
	}

	private void saveValidation() {
		final File tFile = ValidationService
				.getFile(this, reservation.getRid());
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					new AsyncTask<Void, Void, Void>() {
						@Override
						protected Void doInBackground(Void... params) {
							try {
								FileUtils.write(tFile, "");
							} catch (IOException e) {
							}
							return null;
						}
					}.execute();
				} catch (Throwable t) {
				}
				SessionM.logAction("trip_" + reservation.getMpoint());
			}
		});
	}

	private void saveTrip() {
		final File tFile = TripService.getFile(this, reservation.getRid());
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					new AsyncTask<Void, Void, Void>() {
						@Override
						protected Void doInBackground(Void... params) {
							try {
								FileUtils.write(tFile, "");
							} catch (IOException e) {
							}
							return null;
						}
					}.execute();
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
		Log.d("ValidationActivity", "showNavigationInformation()");
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
					DirectionItem item = new DirectionItem(
							NavigationView.getDirectionDrawableId(
									nextNode.getDirection(), false), distance,
							nextNode.getRoadName(),
							NavigationView.getDirectionDrawableId(
									nextNode.getDirection(), true));
					dirListadapter.add(item);
					items.add(item);
					distance = 0;
				}
				distance += nextNode.getDistance();
			} while ((nextNode = nextNode.getNextNode()) != null);
		}
		return items;
	}
	
	private static final int countOutOfRouteThreshold = 3;
	
	private static final double distanceOutOfRouteThreshold = 150;
	
	private static final double speedOutOfRouteThreshold = 10;
	
	private AtomicInteger routeOfRouteCnt = new AtomicInteger();
	
	private void reroute(final double lat, final double lon, final double speedInMph,
	        final float bearing){
	    runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AsyncTask<Void, Void, Route> task = new AsyncTask<Void, Void, Route>(){
                    @Override
                    protected void onPreExecute() {
                        navigationView.setRerouting(true);
                    }
                    @Override
                    protected Route doInBackground(Void... params) {
                        Route navRoute = null;
                        RouteFetchRequest routeReq = new RouteFetchRequest(
                            User.getCurrentUser(ValidationActivity.this), 
                            new GeoPoint(lat, lon), route.getLastNode().getGeoPoint(), 
                            System.currentTimeMillis());
                        try{
                            List<Route> list = routeReq.execute(ValidationActivity.this);
                            if(list != null && !list.isEmpty()){
                                Route resRoute = list.get(0);
                                String speedInMphStr = String.valueOf(speedInMph);
                                String courseAngleClockwise = String.valueOf(bearing);
                                RouteFetchRequest navReq = new RouteFetchRequest(
                                        resRoute.getLink().url
                                           .replaceAll("\\{speed_in_mph\\}", speedInMphStr)
                                           .replaceAll("\\{course_angle_clockwise\\}", courseAngleClockwise)
                                           .replaceAll("\\[speed_in_mph\\]", speedInMphStr)
                                           .replaceAll("\\[course_angle_clockwise\\]", courseAngleClockwise),
                                        System.currentTimeMillis(),
                                        0);
                                List<Route> routes = navReq.execute(ValidationActivity.this);
                                if (routes != null && routes.size() > 0) {
                                    navRoute = routes.get(0);
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
                            Log.d("reroute", Log.getStackTraceString(t));
                        }
                        return navRoute;
                    }
                    @Override
                    protected void onPostExecute(Route result) {
                        navigationView.setRerouting(false);
                        if(result != null){
                            reroute = result;
                            reroute.preprocessNodes();
                            routeRect = initRouteRect(reroute);
                            updateDirectionsList();
                            drawRoute(mapView, reroute, 0);
                            routeOfRouteCnt.set(0);
                        }
                    }
                };
                Misc.parallelExecute(task);
            }
        });
	}
	
	private AtomicBoolean routeLoaded = new AtomicBoolean();
	
	private synchronized void locationChanged(final Location location) {
	    final double speedInMph = Trajectory.msToMph(location.getSpeed());
	    final float bearing = location.getBearing();
	    if (!routeLoaded.get() && isLoadRoute()) {
            routeLoaded.set(true);
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    AsyncTask<Void, Void, List<Route>> task = new AsyncTask<Void, Void, List<Route>>() {
                        @Override
                        protected List<Route> doInBackground(Void... params) {
                            List<Route> routes = null;
                            try {
                                RouteFetchRequest request;
                                if (isDebugging) {
                                    request = new RouteFetchRequest(
                                            route.getDepartureTime());
                                } else {
                                    String speedInMphStr = String.valueOf(speedInMph);
                                    String courseAngleClockwise = String.valueOf(bearing);
                                    request = new RouteFetchRequest(
                                        reservation.getNavLink()
                                           .replaceAll("\\{speed_in_mph\\}", speedInMphStr)
                                           .replaceAll("\\{course_angle_clockwise\\}", courseAngleClockwise)
                                           .replaceAll("\\[speed_in_mph\\]", speedInMphStr)
                                           .replaceAll("\\[course_angle_clockwise\\]", courseAngleClockwise),
                                        reservation.getDepartureTime(),
                                        reservation.getDuration());
                                }
                                routes = request.execute(ValidationActivity.this);
                                if (routes != null && routes.size() > 0) {
                                    List<RouteNode> timeNodes = new ReservationFetchRequest(
                                            User.getCurrentUser(ValidationActivity.this), 
                                            reservation.getRid())
                                        .execute(ValidationActivity.this).getRoute().getNodes();
                                    Map<Integer, Integer> nodeTimes = new HashMap<Integer, Integer>();
                                    for(RouteNode n:timeNodes){
                                        nodeTimes.put(n.getNodeNum(), n.getTime());
                                    }
                                    Route route = routes.get(0);
                                    for(RouteNode n : route.getNodes()){
                                        Integer time = nodeTimes.get(n.getNodeNum());
                                        if(time != null){
                                            n.setTime(time);
                                        }
                                    }
                                }
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
                                final FakeRoute fakeRoute = DebugOptionsActivity.getFakeRoute(
                                    ValidationActivity.this, route.getId());
                                Route oldRoute = route;
                                route = routes.get(isDebugging ? fakeRoute.seq : 0);
                                route.setId(oldRoute.getId());
                                reservation.setRoute(route);
                                route.setCredits(reservation.getCredits());
                                route.preprocessNodes();
                                routeRect = initRouteRect(route);
                                updateDirectionsList();
                                centerMap(mapView.getController(), isOnRecreate.get(),
                                    lastCenter, route);
                                drawRoute(mapView, route, 0);
                            }
                        }
                    };
                    Misc.parallelExecute(task);
                }
            });
	    }
	    
		if (pointOverlay == null) {
			return;
		}
		
		double lat = location.getLatitude();
		double lng = location.getLongitude();

		long now = SystemClock.elapsedRealtime();

		GeoPoint oldLoc = pointOverlay.getLocation();
		if (oldLoc.isEmpty()) {
			if (buttonFollow.isChecked()) {
				mapView.getController().animateTo(new GeoPoint(lat, lng));
			}
			pointOverlay.setLocation((float) lat, (float) lng);
			mapView.postInvalidate();
		} else {
			animator.removeCallbacksAndMessages(null);
			final double oldLat = oldLoc.getLatitude();
			double y = lat - oldLat;
			final double oldLng = oldLoc.getLongitude();
			double x = lng - oldLng;
			final double slop = y / x;
			double timeInterval = 1000 / 30;
			long numOfSteps = Math.round((now - lastLocChanged) / timeInterval);
			final double stepSize = x / numOfSteps;
			long startTimeMillis = SystemClock.uptimeMillis();
			for (int i = 1; i <= numOfSteps; i++) {
				final int seq = i;
				animator.postAtTime(new Runnable() {
					@Override
					public void run() {
						double deltaX = seq * stepSize;
						double newLng = oldLng + deltaX;
						double newLat = oldLat + deltaX * slop;
						pointOverlay
								.setLocation((float) newLat, (float) newLng);
						mapView.postInvalidate();
						if (buttonFollow.isChecked()) {
                            mapView.getController().setCenter(new GeoPoint(newLat, newLng));
                        }
					}
				}, startTimeMillis + Math.round(i * timeInterval));
			}
		}

		lastLocChanged = now;

		long linkId = Trajectory.DEFAULT_LINK_ID;

		// nearestNode = route.getNearestNode(lat, lng);
		if (!route.getNodes().isEmpty()) {
			nearestLink = route.getNearestLink(lat, lng);

			RouteLink rerouteNearestLink = getRouteOrReroute().getNearestLink(lat, lng);
			nearestNode = rerouteNearestLink.getEndNode();

			ValidationParameters params = ValidationParameters.getInstance();

			boolean alreadyValidated = isTripValidated();

			double distanceToLink = nearestLink.distanceTo(lat, lng);
			if (!stopValidation.get()
					&& distanceToLink <= params
							.getValidationDistanceThreshold()) {
				Log.i("validated node", nearestLink.getStartNode()
						.getNodeIndex() + "");
				nearestLink.getStartNode().getMetadata().setValidated(true);
			}

			if (!alreadyValidated && isTripValidated()) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						saveValidation();
					}
				});
			}

			int numberOfValidatedNodes = 0;
			for (RouteNode node : route.getNodes()) {
				if (node.getMetadata().isValidated()) {
					numberOfValidatedNodes += 1;
				}
			}
			Log.d("ValidationActivity", String.format("%d/%d",
					numberOfValidatedNodes, route.getNodes().size()));

			boolean inRoute;
	        if (inRoute = rerouteNearestLink.distanceTo(lat, lng) <= params
	                .getInRouteDistanceThreshold()) {
	            linkId = rerouteNearestLink.getStartNode().getLinkId();
	        }
	        
	        if(!inRoute && NavigationView.metersToFeet(distanceToLink) > distanceOutOfRouteThreshold
                    && speedInMph > speedOutOfRouteThreshold){
                Log.i("reroute", routeOfRouteCnt + ", " + NavigationView.metersToFeet(distanceToLink) 
                    + ", " + speedInMph);
                if(routeOfRouteCnt.incrementAndGet() == countOutOfRouteThreshold){
                    reroute(lat, lng, speedInMph, bearing);
                }
            }else{
                routeOfRouteCnt.set(0);
            }
			
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
			
			long passedNodeTime = 0;
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
            etaDelay.set(currentTime.toMillis(false) - startTime - passedNodeTime * 1000);
            final TextView timeInfo = (TextView) findViewById(R.id.remain_times);
            timeInfo.setTag(R.id.estimated_arrival_time, getFormatedEstimateArrivalTime(reservation.getArrivalTime() + etaDelay.get()));
            timeInfo.setTag(R.id.remaining_travel_time, getFormatedRemainingTime(remainingNodeTime));
            refreshTimeInfo();

		}

		trajectory.accumulate(location, linkId);

		if (!arrived.get() && trajectory.size() >= 8) {
			saveTrajectory();
		}

		sendImComingMsg();

		if (!arrived.get() && !getRouteOrReroute().getNodes().isEmpty()
				&& getRouteOrReroute().hasArrivedAtDestination(lat, lng)) {
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
		lastKnownLocation = location;
	}
	
	private AtomicLong etaDelay = new AtomicLong(); 

	private AtomicBoolean turnOffGPS = new AtomicBoolean();
	
	private void displayArrivalMsg() {
		if (isTripValidated()) {
			final View panel = findViewById(R.id.congrats_panel);
			String msg = "Arrive at Destination";
			((TextView) findViewById(R.id.congrats_msg)).setText(msg);
			
			TextView co2 = (TextView) findViewById(R.id.co2);
			String co2Value = "1.3";  //getCO2
			co2.setText(formatCO2Desc(ValidationActivity.this, co2Value));

			TextView mpoint = (TextView) findViewById(R.id.mPoint);
			mpoint.setText(formatMPointDesc(ValidationActivity.this,
				reservation.getMpoint() + ""));
			
			panel.setVisibility(View.VISIBLE);
			Misc.fadeIn(ValidationActivity.this, panel);
			speakIfTtsEnabled(msg);
			turnOffGPS.set(true);
			// turn off GPS
			if(locationManager != null) {
				locationManager.removeUpdates(locationListener);
			}
		}
	}

	private void arriveAtDestination() {
		saveTrajectory();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				navigationView.setVisibility(View.GONE);
				findViewById(R.id.dest_panel).setVisibility(View.VISIBLE);
				speakIfTtsEnabled("Arrive at Destination");
				displayArrivalMsg();
			}
		});
	}

	private void reportValidation() {
		if (!reported.get()) {
			reported.set(true);

			if (isTripValidated()) {
			    runOnUiThread(new Runnable() {
		            @Override
		            public void run() {
		                displayArrivalMsg();
		            }
			    });
			}
		}
	}

	private boolean isTripValidated() {
		double validatedDistance = route.getValidatedDistance();
		double length = route.getLength();
		double score = validatedDistance / length;
		Log.i("isTripValidated", validatedDistance + " / " + length + " = "
				+ score);
		ValidationParameters params = ValidationParameters.getInstance();
		return score >= params.getScoreThreshold();
	}

	private void deactivateLocationService() {
		if (fakeLocationService != null) {
			fakeLocationService.cancel();
		}
	}

	private void cancelValidation() {
		// Ask the user if they want to quit
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Confirm")
				.setMessage("Are you sure you want to stop this trip?")
				.setPositiveButton("No", null)
				.setNegativeButton("Yes",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								reportValidation();

								// Stop the activity
								if (!isTripValidated()) {
									showValidationFailedDialog();
									AsyncTask<Void, Void, Void> delTask = new AsyncTask<Void, Void, Void>(){
					                    @Override
					                    protected Void doInBackground(Void... params) {
					                        ReservationDeleteRequest request = new ReservationDeleteRequest(
					                            User.getCurrentUser(ValidationActivity.this), reservation.getRid());
					                        try {
					                            request.execute(ValidationActivity.this);
					                        }
					                        catch (Exception e) {
					                        }
					                        return null;
					                    }
					                };
					                Misc.parallelExecute(delTask);
								}
							}

						}).show();
	}

	private class ValidationLocationListener implements LocationListener {

		Location lastLocation;

		public void onLocationChanged(Location location) {
			Log.d(this.getClass().toString(),
					String.format("onLocationChanged: %s", location));
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

	private static final int TWO_MINUTES = 1000 * 60 * 2;

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
	private static boolean isBetterLocation(Location location,
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

		private Queue<GeoPoint> trajectory;

		private int interval;

		private int gpsMode;

		int pollCnt;

		public FakeLocationService(LocationListener listener, int interva,
				int gpsMode) {
			this(listener, interva, null, gpsMode);
		}

		@SuppressWarnings("unchecked")
		public FakeLocationService(LocationListener listener, int interval,
				Queue<GeoPoint> trajectory, int gpsMode) {
			this.listener = listener;
			this.interval = interval;
			this.gpsMode = gpsMode;

			if (trajectory == null) {
				try {
					String tFile;
					if (gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED) {
						tFile = "trajectory.csv";
					} else if (gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA) {
						tFile = "trajectory-la.csv";
					} else if (gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA2) {
						tFile = "trajectory-la-2.csv";
					} else if (gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED_LA3) {
						tFile = "trajectory-la-3.csv";
					} else {
						tFile = "trajectory-la-4.csv";
					}
					InputStream in = getResources().getAssets().open(tFile);
					this.trajectory = (Queue<GeoPoint>) PrerecordedTrajectory
							.read(in, gpsMode);
				} catch (IOException e) {
					e.printStackTrace();
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
						trajectory, gpsMode);
			} else {
				rtn = this;
			}
			return rtn;
		}

		@Override
		public void run() {
			if (trajectory == null || trajectory.isEmpty()) {
				timer.cancel();
			} else {
				Location location = new Location("");
				location.setTime(System.currentTimeMillis());
				GeoPoint geoPoint = trajectory.poll();
				pollCnt++;
				location.setLatitude(geoPoint.getLatitude());
				location.setLongitude(geoPoint.getLongitude());
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
					unmuteMusic();
				}
			});
			navigationView.setListener(new CheckPointListener() {
				@Override
				public void onCheckPoint(final String navText) {
					if (!arrived.get()) {
						speakIfTtsEnabled(navText);
					}
				}
			});
		}
	}

	private void speakIfTtsEnabled(String text) {
		if (MapDisplayActivity.isNavigationTtsEnabled(this)) {
			speak(text);
		}
	}

	private void speak(String text) {
		if (mTts != null) {
			try {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
						utteranceId);
				params.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
						String.valueOf(AudioManager.STREAM_NOTIFICATION));
				AudioManager am = (AudioManager) ValidationActivity.this
						.getSystemService(Context.AUDIO_SERVICE);
				am.setStreamMute(AudioManager.STREAM_MUSIC, true);
				mTts.speak(text, TextToSpeech.QUEUE_ADD, params);
			} catch (Throwable t) {
			}
		}
	}

	private void unmuteMusic() {
		AudioManager am = (AudioManager) ValidationActivity.this
				.getSystemService(Context.AUDIO_SERVICE);
		am.setStreamMute(AudioManager.STREAM_MUSIC, false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unmuteMusic();
		unregisterReceiver(timeoutReceiver);
		if (locationManager != null) {
			locationManager.removeUpdates(locationListener);
		}
		deactivateLocationService();
		if (mTts != null) {
			mTts.shutdown();
		}
		if (Request.NEW_API && isTripValidated()) {
			saveTrip();
		}
	}

	private void showValidationFailedDialog() {
		CharSequence msg = Html
				.fromHtml("Sorry "
						+ User.getCurrentUser(this).getFirstname()
						+ " this trip did not earn you any point. Please try again soon."
						+ "<br/>If you feel you should have earned the points,"
						+ " <a href=\"" + FeedbackDialog.getUrl(this)
						+ "\">tell us why?</a>");
		NotificationDialog dialog = new NotificationDialog(
				ValidationActivity.this, msg);
		dialog.setActionListener(new NotificationDialog.ActionListener() {
			@Override
			public void onClickDismiss() {
				if (!isFinishing()) {
					finish();
				}
			}
		});
		dialog.show();
	}

}
