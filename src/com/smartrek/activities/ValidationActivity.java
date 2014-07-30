package com.smartrek.activities;

import java.io.File;
import java.io.IOException;
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
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.internal.nineoldandroids.animation.Animator;
import com.actionbarsherlock.internal.nineoldandroids.animation.Animator.AnimatorListener;
import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.SendTrajectoryService;
import com.smartrek.TripService;
import com.smartrek.activities.DebugOptionsActivity.FakeRoute;
import com.smartrek.dialogs.FloatingMenuDialog;
import com.smartrek.dialogs.NotificationDialog2;
import com.smartrek.dialogs.NotificationDialog2.ActionListener;
import com.smartrek.models.Reservation;
import com.smartrek.models.Route;
import com.smartrek.models.Trajectory;
import com.smartrek.models.Trajectory.Record;
import com.smartrek.models.User;
import com.smartrek.requests.ImComingRequest;
import com.smartrek.requests.Request;
import com.smartrek.requests.Request.Setting;
import com.smartrek.requests.ReservationFetchRequest;
import com.smartrek.requests.RouteFetchRequest;
import com.smartrek.ui.ClickAnimation;
import com.smartrek.ui.ClickAnimation.ClickAnimationEndCallback;
import com.smartrek.ui.NavigationView;
import com.smartrek.ui.NavigationView.CheckPointListener;
import com.smartrek.ui.NavigationView.DirectionItem;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.ui.overlays.CurrentLocationOverlay;
import com.smartrek.ui.overlays.OverlayCallback;
import com.smartrek.ui.overlays.RouteDebugOverlay;
import com.smartrek.ui.overlays.RouteDestinationOverlay;
import com.smartrek.ui.overlays.RoutePathOverlay;
import com.smartrek.ui.timelayout.TimeColumn;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.HTTP;
import com.smartrek.utils.Misc;
import com.smartrek.utils.RouteLink;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.RouteRect;
import com.smartrek.utils.SmartrekTileProvider;
import com.smartrek.utils.StringUtil;
import com.smartrek.utils.SystemService;

public class ValidationActivity extends FragmentActivity implements OnInitListener, 
        OnAudioFocusChangeListener {
	public static final int DEFAULT_ZOOM_LEVEL = 18;
	
	public static final int NAVIGATION_ZOOM_LEVEL = 16;

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

	private MapView mapView;
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

	private List<Overlay> mapOverlays;

	private CurrentLocationOverlay pointOverlay;

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
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_reservation_map);
		
		ReservationConfirmationActivity.cancelNotification(this);
		
		registerReceiver(tripValidator, new IntentFilter(TRIP_VALIDATOR));
		
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

		// Define a listener that responds to location updates
		locationListener = new ValidationLocationListener();

		if (isOnRecreate.get()) {
			startTime = savedInstanceState.getLong(START_TIME);
			savedPollCnt = savedInstanceState.getInt(POLL_CNT);
			emails = savedInstanceState.getString(EMAILS);
			phones = savedInstanceState.getString(PHONES);
			trajectoryData = savedInstanceState.getString(TRAJECTORY_DATA);
		} else {
			Time now = new Time();
			now.setToNow();
			startTime = now.toMillis(false);
			emails = extras.getString(EMAILS);
			phones = extras.getString(PHONES);
			trajectoryData = extras.getString(TRAJECTORY_DATA);
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
		registerReceiver(timeoutReceiver,
				new IntentFilter(getClass().getName()));

		Intent timeoutIntent = new Intent(getClass().getName());
		PendingIntent pendingTimeout = PendingIntent.getBroadcast(
				ValidationActivity.this, 0, timeoutIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()
				+ (900 + reservation.getDuration() * 3) * 1000, pendingTimeout);
		
		remainDistDirecListView = (TextView) findViewById(R.id.remain_dist_direc_list);
		remainTimesDirectListView = (TextView) findViewById(R.id.remain_times_direc_list);

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
				vDistance
						.setText(NavigationView.adjustDistanceFontSize(
								ValidationActivity.this, StringUtil
										.formatImperialDistance(item.distance,
												true)));
				vDistance.requestLayout();
				vRoad.setText((StringUtils.isBlank(item.roadName) 
			        || StringUtils.equalsIgnoreCase(item.roadName, "null")) ? ""
					: (StringUtils.capitalize(item.roadName.substring(0, 1)) 
			        + item.roadName.substring(1))
		        );
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
			centerMap(mapView.getController(), isOnRecreate.get(), lastCenter, route);
			drawRoute(mapView, route, 0);
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
	
	private String getTextMessage(Route route) {
		StringBuffer msg = new StringBuffer();
		User user = User.getCurrentUser(ValidationActivity.this);
		msg.append(user.getFirstname()).append(" ").append(user.getLastname()).append(" is ")
		   .append(StringUtil.formatImperialDistance(route.getLength(), false))
		   .append(" away, and will arrive at ")
		   .append(reservation.getDestinationAddress()).append(" at ");
        msg.append(TimeColumn.formatTime(reservation.getArrivalTimeUtc(), route.getTimezoneOffset())).append(".");
        return msg.toString();
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
		outState.putString(PHONES, phones);
		outState.putString(TRAJECTORY_DATA, trajectoryData);
	}

	@Override
	protected void onStart() {
		super.onStart();

		EasyTracker.getInstance().activityStart(this);

	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

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
		super.onPause();
		unregisterReceiver(timeInfoCycler);
		if(!cancelTrip) {
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
                buttonFollow.setTag(Boolean.valueOf(false));
                return false;
            }
        });
		
		RouteActivity.setViewToNorthAmerica(mapView);
		
		TextView osmCredit = (TextView) findViewById(R.id.osm_credit);
		RelativeLayout.LayoutParams osmCreditLp = (RelativeLayout.LayoutParams) osmCredit
				.getLayoutParams();
		osmCreditLp.bottomMargin = Dimension.dpToPx(5, getResources().getDisplayMetrics());
		
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
		                    if (lastKnownLocation != null) {
		                    	double latitude = lastKnownLocation.getLatitude();
		                    	double longitude = lastKnownLocation.getLongitude();
		                    	IMapController mc = mapView.getController();
		                    	mc.setZoom(isNearOD_or_Intersection(latitude, longitude)
		                	        ?DEFAULT_ZOOM_LEVEL:NAVIGATION_ZOOM_LEVEL);
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
		            	navigationView.setToCurrentDireciton();
					}
            		
            	});
            }
        });

		navigationView = (NavigationView) findViewById(R.id.navigation_view);
		navigationView.setDestinationAddress(reservation.getDestinationAddress());
		navigationView.setTypeface(boldFont);
		
		Configuration conf = getResources().getConfiguration();
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
		                	Uri ding = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ding);
		                	Ringtone r = RingtoneManager.getRingtone(ValidationActivity.this, ding);
		                	r.play();
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
							NotificationDialog2 dialog = new NotificationDialog2(ValidationActivity.this, "On My Way is availiable to passengers only.");
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
									startActivityForResult(contactSelect, ON_MY_WAY);
									onMyWayBtn.setTag(new Object());
								}
							});
							dialog.show();
					}
				});
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


		dirListView = (ListView) findViewById(R.id.directions_list);
		dirListView.setAdapter(dirListadapter);

		TextView finishButton = (TextView) findViewById(R.id.close);
		finishButton.setText(Html.fromHtml("<u>Close</u>"));
		finishButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ValidationActivity.this.finish();
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
		                    + "using Metropia Mobile!"
		                    + "\n\n" + Misc.APP_DOWNLOAD_LINK);
		                startActivity(intent);
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
						if (lastKnownLocation != null) {
							double latitude = lastKnownLocation.getLatitude();
							double longitude = lastKnownLocation.getLongitude();
							IMapController mc = mapView.getController();
							mc.setZoom(isNearOD_or_Intersection(latitude, longitude)
		                        ?DEFAULT_ZOOM_LEVEL:NAVIGATION_ZOOM_LEVEL);
							mc.animateTo(new GeoPoint(latitude, longitude));
						}
					}
				});
			}
		});

		TextView destAddr = (TextView) findViewById(R.id.dest_addr);
		destAddr.setText(reservation.getDestinationAddress());

		final TextView timeInfo = (TextView) findViewById(R.id.remain_times);
        timeInfo.setTag(R.id.estimated_arrival_time, getFormatedEstimateArrivalTime(
            reservation.getArrivalTimeUtc(), route.getTimezoneOffset()));
        timeInfo.setTag(R.id.remaining_travel_time, getFormatedRemainingTime(reservation.getDuration()));
        refreshTimeInfo();
        final TextView directListTimeInfo = (TextView) findViewById(R.id.remain_times_direc_list);
        directListTimeInfo.setTag(R.id.estimated_arrival_time, getFormatedEstimateArrivalTime(getETA(), route.getTimezoneOffset()));
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
			    if(WebMyMetropiaActivity.hasUrl(ValidationActivity.this)){
			        intent = new Intent(ValidationActivity.this, WebMyMetropiaActivity.class);
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
                if(WebMyMetropiaActivity.hasUrl(ValidationActivity.this)){
                    intent = new Intent(ValidationActivity.this, WebMyMetropiaActivity.class);
                }else{
    				intent = new Intent(ValidationActivity.this, MyMetropiaActivity.class);
    				intent.putExtra(MyMetropiaActivity.OPEN_TAB, MyMetropiaActivity.DRIVE_SCORE_TAB);
                }
				startActivity(intent);
			}
        });
        
        if(DebugOptionsActivity.isReroutingDebugMsgEnabled(this) || DebugOptionsActivity.isVoiceDebugMsgEnabled(this)
                || DebugOptionsActivity.isGpsAccuracyDebugMsgEnabled(this)){
            findViewById(R.id.rerouting_debug_msg).setVisibility(View.VISIBLE);
        }
		
        scheduleTimeInfoCycle();
        
        Font.setTypeface(boldFont, remainDistDirecListView, timeInfo, finishButton, feedBackButton);
		Font.setTypeface(lightFont, osmCredit, remainTimesDirectListView);
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
	    
	private static String getFormatedRemainingTime(long seconds){
	    long minute = Double.valueOf(Math.round(seconds / 60.0D)).longValue();
	    return minute + " min";
	}

	private SpannableString formatCO2Desc(Context ctx, String co2Desc) {
		int co2Index = co2Desc.indexOf("CO2");
		SpannableString co2DescSpan = SpannableString.valueOf(co2Desc);
		co2DescSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.smaller_font)), co2Index,
				co2Index + "CO".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		int twoIndex = co2Index + "CO".length();
		co2DescSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.micro_font)), twoIndex,
				twoIndex + "2".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return co2DescSpan;
	}
	
	private SpannableString formatCongrMessage(Context ctx, String message) {
		int indexOfNewline = message.indexOf("\n");
		SpannableString congrSpan = SpannableString.valueOf(message);
		congrSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.smaller_font)), indexOfNewline,
				message.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return congrSpan;
	}
	
	private SpannableString formatCongrValueDesc(Context ctx, String valueDesc) {
		int indexOfNewline = valueDesc.indexOf("\n");
		SpannableString congrValueSpan = SpannableString.valueOf(valueDesc);
		congrValueSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.smaller_font)), indexOfNewline,
				valueDesc.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return congrValueSpan;
	}

	/*
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
	*/

	private View[] getMapViews() {
		return new View[] { findViewById(R.id.mapview),
				findViewById(R.id.navigation_view), findViewById(R.id.mapview_options) };
	}

//	private void toggleMenuPanel() {
//		View menuButton = findViewById(R.id.menu_button);
//		String currentStatus = menuButton.getTag(R.id.menu_panel_status)
//				.toString();
//		Log.d("menuStatus", currentStatus);
//		if ("open".equals(currentStatus)) {
//			findViewById(R.id.menu_panel).setVisibility(View.INVISIBLE);
//			menuButton.setTag(R.id.menu_panel_status, "close");
//		} else {
//			findViewById(R.id.menu_panel).setVisibility(View.VISIBLE);
//			menuButton.setTag(R.id.menu_panel_status, "open");
//		}
//	}

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
	
	private void bindDebugOverlay(MapView mapView){
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
                    location.setSpeed(9999f);
                    location.setTime(System.currentTimeMillis());
                    locationChanged(location);
                }
            }

        });
        mapView.getOverlays().add(debugOverlay);
	}

	public synchronized void drawRoute(MapView mapView, Route route,
			int routeNum) {
		mapOverlays = mapView.getOverlays();
		Log.d("ValidationActivity",
				String.format("mapOverlays has %d items", mapOverlays.size()));

		if (routeNum == 0)
			mapOverlays.clear();		

		RoutePathOverlay pathOverlay = new RoutePathOverlay(this, route,
				RoutePathOverlay.GREEN, R.drawable.pin_origin);
		mapOverlays.add(pathOverlay);
		
		if (!route.getNodes().isEmpty()) {
    		RouteDestinationOverlay destOverlay = new RouteDestinationOverlay(mapView, 
    	        route.getLastNode().getGeoPoint(), lightFont, reservation.getDestinationAddress(), 
    	        R.drawable.pin_destination);
    		destOverlay.setCallback(new OverlayCallback() {
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
    		mapOverlays.add(destOverlay);
		}
		
		drawCurrentLocation();

		bindDebugOverlay(mapView);
		
		route.setUserId(User.getCurrentUser(this).getId());
	}
	
	private void drawCurrentLocation(){
	    pointOverlay = new CurrentLocationOverlay(this, 0, 0, R.drawable.navigation_page_current_location);
        pointOverlay.disableRadarEffect();
        (mapOverlays == null?mapView.getOverlays():mapOverlays).add(pointOverlay);
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
	
	private boolean[] omwSent = new boolean[omwPercentages.length()];
	
	private void sendImComingMsg() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (emails != null) {
				    double distance = 0;
	                for(int i=0; i<dirListadapter.getCount(); i++){
	                    distance += dirListadapter.getItem(i).distance;
	                }
	                double percentage = distance * 100 / getRouteOrReroute().getLength();
	                boolean toSent = false;
	                for(int i=0; i<omwPercentages.length(); i++){
	                    try {
                            if(!omwSent[i] && percentage <= omwPercentages.getDouble(i)){
                                omwSent[i] = true;
                                toSent = true;
                            }
                        }
                        catch (JSONException e) {
                        }
	                }
	                if(toSent){
	                    final double _distance = distance;
    					try {
    						Misc.parallelExecute(new AsyncTask<Void, Void, Void>() {
    							@Override
    							protected Void doInBackground(Void... params) {
    								try {
    									GeoPoint loc = pointOverlay.getLocation();
    									ImComingRequest req = new ImComingRequest(
    											User.getCurrentUser(ValidationActivity.this),
    											emails,
    											loc.getLatitude(),
    											loc.getLongitude(),
    											getETA(),
    											NavigationView.metersToMiles(_distance),
    											reservation.getDestinationAddress(),
    											route.getTimezoneOffset());
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
    									msg = "On My Way sent";
    								}
    								Toast.makeText(ValidationActivity.this, msg,
    										Toast.LENGTH_LONG).show();
    							}
    						});
    					} catch (Throwable t) {
    					}
	                }
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
								FileUtils.write(tFile, "");
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
		double distance = 0;
		for(DirectionItem item : items) {
			distance = distance + item.distance;
		}
		remainDistDirecListView.setText(StringUtil.formatImperialDistance(distance, false));
	}
	
	private int countOutOfRouteThreshold = ((Number)Request.getSetting(Setting.reroute_after_N_deviated_samples)).intValue();
    
    private double distanceOutOfRouteThreshold = ((Number)Request.getSetting(Setting.reroute_trigger_distance_in_meter)).doubleValue();
	
	public static final double speedOutOfRouteThreshold = 10;
	
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
                    }
                    @Override
                    protected Route doInBackground(Void... params) {
                        Route navRoute = null;
                        RouteFetchRequest routeReq = new RouteFetchRequest(
                            User.getCurrentUser(ValidationActivity.this), 
                            new GeoPoint(lat, lon), new GeoPoint(reservation.getEndlat(), reservation.getEndlon()), 
                            System.currentTimeMillis(), speedInMph, bearing, null,
                            reservation.getDestinationAddress());
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
                        routeOfRouteCnt.set(0);
                        if(result != null){
                            passedNodeTimeOffset.addAndGet(passedTime);
                            reroute = result;
                            reroute.preprocessNodes();
                            routeRect = initRouteRect(reroute);
                            updateDirectionsList();
                            drawRoute(mapView, reroute, 0);
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
                            List<Route> routes = null;
                            try {
                                RouteFetchRequest request;
                                if (isDebugging) {
                                    request = new RouteFetchRequest(
                                            route.getDepartureTime());
                                } else {
                                    request = new RouteFetchRequest(
                                        reservation.getNavLink(),
                                        reservation.getDepartureTime(),
                                        reservation.getDuration(),
                                        speedInMph,
                                        bearing);
                                }
                                routes = request.execute(ValidationActivity.this);
                                if (routes != null && routes.size() > 0) {
                                    Reservation reser = new ReservationFetchRequest(
                                            User.getCurrentUser(ValidationActivity.this), 
                                            reservation.getRid())
                                        .execute(ValidationActivity.this);
                                    reservation.setEndlat(reser.getEndlat());
                                    reservation.setEndlon(reser.getEndlon());
                                    List<RouteNode> timeNodes = reser.getRoute().getNodes();
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
                                route.setTimezoneOffset(oldRoute.getTimezoneOffset());
                                reservation.setRoute(route);
                                route.setCredits(reservation.getCredits());
                                route.preprocessNodes();
                                routeRect = initRouteRect(route);
                                updateDirectionsList();
                                centerMap(mapView.getController(), isOnRecreate.get(),
                                    lastCenter, route);
                                drawRoute(mapView, route, 0);
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
	    
	    runOnUiThread(new Runnable(){
            @Override
            public void run() {
                if(pointOverlay == null){
                    drawCurrentLocation();
                    SharedPreferences debugPrefs = getSharedPreferences(DebugOptionsActivity.DEBUG_PREFS, MODE_PRIVATE);
                    int gpsMode = debugPrefs.getInt(DebugOptionsActivity.GPS_MODE, DebugOptionsActivity.GPS_MODE_DEFAULT);
                    if(gpsMode != DebugOptionsActivity.GPS_MODE_LONG_PRESS){
                        navigationView.setTextViewWaiting("Waiting for the route...");
                    }
                }
                
                if(speedInMph > speedOutOfRouteThreshold){
                    pointOverlay.setDegrees(bearing);
                }
                
                if ((Boolean)buttonFollow.getTag()) {
                    mapView.getController().setZoom(isNearOD_or_Intersection(lat, lng)?
                        DEFAULT_ZOOM_LEVEL:NAVIGATION_ZOOM_LEVEL);
                }
                
                long now = SystemClock.elapsedRealtime();

                GeoPoint oldLoc = pointOverlay.getLocation();
                if (oldLoc.isEmpty()) {
                    if ((Boolean)buttonFollow.getTag()) {
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
                    if(!isNearOD_or_Intersection(lat, lng) || x == 0){
                        for (int i = 0; i <= numOfSteps; i++) {
                            final int seq = i;
                            animator.postAtTime(new Runnable() {
                                @Override
                                public void run() {
                                    if(seq == 0){
                                        if ((Boolean)buttonFollow.getTag()) {
                                            mapView.getController().setCenter(new GeoPoint(lat, lng));
                                        }
                                        pointOverlay.setLocation((float) lat, (float) lng);
                                    }
                                    mapView.postInvalidate();
                                }
                            }, startTimeMillis + Math.round(i * timeInterval));
                        }
                    }else{
                        for (int i = 1; i <= numOfSteps; i++) {
                            final int seq = i;
                            animator.postAtTime(new Runnable() {
                                @Override
                                public void run() {
                                    double deltaX = seq * stepSize;
                                    double newLng = oldLng + deltaX;
                                    double newLat = oldLat + deltaX * slop;
                                    pointOverlay.setLocation((float) newLat, (float) newLng);
                                    mapView.postInvalidate();
                                    if ((Boolean)buttonFollow.getTag()) {
                                        mapView.getController().setCenter(new GeoPoint(newLat, newLng));
                                    }
                                }
                            }, startTimeMillis + Math.round(i * timeInterval));
                        }
                    }
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
                if(Route.isOutOfRoute(rerouteNearbyLinks, rerouteSameDirLinks)){
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
            timeInfo.setTag(R.id.estimated_arrival_time, getFormatedEstimateArrivalTime(getETA(), route.getTimezoneOffset()));
            timeInfo.setTag(R.id.remaining_travel_time, getFormatedRemainingTime(remainingTime.get()));
            refreshTimeInfo();
            final TextView directListTimeInfo = (TextView) findViewById(R.id.remain_times_direc_list);
            directListTimeInfo.setTag(R.id.estimated_arrival_time, getFormatedEstimateArrivalTime(getETA(), route.getTimezoneOffset()));
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

		trajectory.accumulate(location, linkId);

		if (!arrived.get() && trajectory.size() >= 8) {
			saveTrajectory();
		}

		sendImComingMsg();

		if (!arrived.get() && !getRouteOrReroute().getNodes().isEmpty()
				&& getRouteOrReroute().hasArrivedAtDestination(lat, lng, accuracy, speedInMph, bearing)) {
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
	
	private AtomicLong etaDelay = new AtomicLong();
	
	private AtomicLong remainingTime = new AtomicLong(); 

	private long getETA(){
	    Time currentTime = new Time();
        currentTime.setToNow();
	    return currentTime.toMillis(false) + remainingTime.get() * 1000; 
	}
	
	private AtomicBoolean turnOffGPS = new AtomicBoolean();
	
	private AtomicBoolean arrivalMsgTiggered = new AtomicBoolean();
	
	private void displayArrivalMsg(final Runnable callback) {
		if (isTripValidated()) {
		    arrivalMsgTiggered.set(true);
		    saveTrajectory(new Runnable() {
                @Override
                public void run() {
                    saveTrip(new Runnable() {
                        @Override
                        public void run() {
                            TripService.run(ValidationActivity.this, User.getCurrentUser(ValidationActivity.this));
                        }
                    });
                }
            });
		    
		    if(pointOverlay != null) {
		    	Intent updateMyLocation = new Intent(LandingActivity2.UPDATE_MY_LOCATION);
				updateMyLocation.putExtra("lat", pointOverlay.getLocation().getLatitude());
				updateMyLocation.putExtra("lon", pointOverlay.getLocation().getLongitude());
				sendBroadcast(updateMyLocation);
		    }
		    
			turnOffGPS.set(true);
			// turn off GPS
			if(locationManager != null) {
				locationManager.removeUpdates(locationListener);
			}
			findViewById(R.id.loading).setVisibility(View.VISIBLE);
			new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try{
                        if(callback!=null) {
                        	callback.run();
                        }
                        if (!isFinishing()) {
                            finish();
                        }
                    }catch(Throwable t){}
                }
            }, HTTP.defaultTimeout);
		}
	}
	
	private AtomicBoolean arrivalMsgDisplayed = new AtomicBoolean();
	
	private void doDisplayArrivalMsg(int uPoints, int driveScoreValue, double co2Value){
	    if(!arrivalMsgDisplayed.get()){
	        arrivalMsgDisplayed.set(true);
	        findViewById(R.id.loading).setVisibility(View.GONE);
    	    navigationView.setVisibility(View.GONE);
            final View panel = findViewById(R.id.congrats_panel);
            String dest = reservation.getDestinationAddress();
            String msg = "You Have Arrived!" + "\n" + 
                dest.substring(0, dest.indexOf(",")>-1?dest.indexOf(","):dest.length());
            ((TextView) findViewById(R.id.congrats_msg)).setText(formatCongrMessage(ValidationActivity.this, msg));
            
            
            TextView co2 = (TextView) findViewById(R.id.co2_circle);
            if(co2Value != 0) {
                String co2String = co2Value + "\nCO2";  
                co2.setText(formatCO2Desc(ValidationActivity.this, co2String));
                co2.setVisibility(View.VISIBLE);
            }
            
            TextView mpoint = (TextView) findViewById(R.id.mpoint_circle);
            mpoint.setText(formatCongrValueDesc(ValidationActivity.this, uPoints + "\nPoints"));
            
            TextView driveScore = (TextView) findViewById(R.id.drive_score_circle);
            if(driveScoreValue/60>0) {
                String scoreString = driveScoreValue/60 + "\nminutes"; 
                driveScore.setText(formatCongrValueDesc(ValidationActivity.this, scoreString));
                driveScore.setVisibility(View.VISIBLE);
            }
            
            Font.setTypeface(boldFont, co2, mpoint, driveScore);
            
            //hide map view options
            findViewById(R.id.mapview_options).setVisibility(View.GONE);;
            panel.setVisibility(View.VISIBLE);
            Misc.fadeIn(ValidationActivity.this, panel);
            
            String congratMsg = String.format("Congratulations! You've earned %d points using Metropia mobile", uPoints);
            speakIfTtsEnabled(congratMsg, true);
	    }
	}

	private void arriveAtDestination() {
		saveTrajectory();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				displayArrivalMsg(null);
			}
		});
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
            saveTrajectory(new Runnable() {
                @Override
                public void run() {
                    saveTrip(null);
                }
            });
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
				//Log.i("FakeLocation", pollCnt + "," + location.getLatitude() + "," + location.getLongitude());
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
		if (MapDisplayActivity.isNavigationTtsEnabled(this)) {
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
		if (locationManager != null) {
			locationManager.removeUpdates(locationListener);
		}
		deactivateLocationService();
		NavigationView.removeNotification(this);
		if (mTts != null) {
			mTts.shutdown();
		}
		if (Request.NEW_API && !arrivalMsgTiggered.get() && isTripValidated()) {
	        saveTrajectory(new Runnable() {
                @Override
                public void run() {
                    saveTrip(null);
                }
            });
		}
		super.onDestroy();
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
    
    public static final String TIME_SAVING_IN_SECOND = "TIME_SAVING_IN_SECOND";
    
    public static final String CO2_SAVING = "CO2_SAVING";
    
    private BroadcastReceiver tripValidator = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra(ID);
            if(String.valueOf(reservation.getRid()).equals(id)){
                int credit = intent.getIntExtra(CREDIT, reservation.getMpoint());
                if(credit > 0){
                    int timeSavingInSecond = intent.getIntExtra(TIME_SAVING_IN_SECOND, 0);
                    double co2Saving = intent.getDoubleExtra(CO2_SAVING, 0);
                    doDisplayArrivalMsg(credit, timeSavingInSecond, co2Saving);
                }else if (!isFinishing()) {
                    finish();
                }
            }
        }
    };

}
