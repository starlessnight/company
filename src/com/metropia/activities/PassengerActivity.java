package com.metropia.activities;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.PassiveLocationChangedReceiver;
import com.localytics.android.Localytics;
import com.metropia.ResumeNavigationUtils;
import com.metropia.SendTrajectoryService;
import com.metropia.SkobblerUtils;
import com.metropia.SmarTrekApplication;
import com.metropia.TrajectorySendingService;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.TripService;
import com.metropia.dialogs.CancelableProgressDialog;
import com.metropia.dialogs.DuoStyledDialog;
import com.metropia.dialogs.NotificationDialog;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.dialogs.SunRideshareActivityDialog;
import com.metropia.dialogs.NotificationDialog2.ActionListener;
import com.metropia.models.Passenger;
import com.metropia.models.Trajectory;
import com.metropia.models.User;
import com.metropia.models.Trajectory.Record;
import com.metropia.requests.CityRequest;
import com.metropia.requests.CityRequest.City;
import com.metropia.requests.DuoTripCheckRequest;
import com.metropia.requests.PassengerRequest;
import com.metropia.requests.PassengerReservationRequest;
import com.metropia.requests.Request;
import com.metropia.tasks.ICallback;
import com.metropia.tasks.ImageLoader;
import com.metropia.ui.NavigationView;
import com.metropia.ui.Wheel;
import com.metropia.ui.animation.CircularPopupAnimation;
import com.metropia.ui.animation.ClickAnimation;
import com.metropia.utils.Dimension;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.HTTP;
import com.metropia.utils.ImageUtil;
import com.metropia.utils.Misc;
import com.metropia.utils.Preferences;
import com.metropia.utils.RouteNode;
import com.metropia.utils.Speaker;
import com.metropia.utils.StringUtil;
import com.metropia.utils.SystemService;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.map.SKAnnotation;
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

public class PassengerActivity extends FragmentActivity implements SKMapSurfaceListener, ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<LocationSettingsResult>, OnClickListener {
	
	final static int INITIAL = 0;
	final static int DURING_TRIP = 1;
	final static int END_TRIP = 2;
	
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Speaker speaker;
	
	private Intent validationResultIntent;
	
	private ProgressDialog preparingDialog;
	private SKMapViewHolder mapViewHolder;
	private SKMapSurfaceView mapView;
	private Wheel wheel;
	
	int status = INITIAL;
	int[] clickable = {};
	int[] clickableAnimated = {R.id.back_button, R.id.center_map_icon, R.id.startButtonText, R.id.close, R.id.share, R.id.feedback};
	
	
	
	private static final Integer DEFAULT_ZOOM_LEVEL = 15;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	SkobblerUtils.initializeLibrary(this);
		setContentView(R.layout.passenger);
		
		registerReceiver(tripValidator, new IntentFilter(PASSENGER_TRIP_VALIDATOR));
		

        ClickAnimation.OnClickListener onClickListener = new ClickAnimation.OnClickListener(this);
        for (int i=0 ; i<clickableAnimated.length ; i++) findViewById(clickableAnimated[i]).setOnClickListener(onClickListener);
        for (int i=0 ; i<clickable.length; i++) findViewById(clickable[i]).setOnClickListener(this);
		
		mapViewHolder = (SKMapViewHolder) findViewById(R.id.mapview_holder);
		mapViewHolder.hideAllAttributionTextViews();
		mapViewHolder.setMapSurfaceListener(this);
		
		Localytics.integrate(this);
		
		speaker = new Speaker(this);
		speaker.init();
		
		// Define a listener that responds to location updates
		locationListener = new LocationListener() {

			public void onLocationChanged(Location location) {
				preProcessLocation(location);
			}
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			public void onProviderEnabled(String provider) {}
			public void onProviderDisabled(String provider) {}
		};

		gpsLocationListener = new com.google.android.gms.location.LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				preProcessLocation(location);
			}
		};
		
		User user = User.getCurrentUser(PassengerActivity.this);
		
		TextView passengerMsg = (TextView) findViewById(R.id.passenger_msg);
		passengerMsg.setText(String.format(getResources().getString(R.string.passenger_before_ride), user.getFirstname()));
		
		TextView startButtonText = (TextView) findViewById(R.id.startButtonText);
		startButtonText.setTag(false);
		
		TextView finishButton = (TextView) findViewById(R.id.close);
		finishButton.setText(Html.fromHtml("<u>Close</u>"));

		TextView feedBackButton = (TextView) findViewById(R.id.feedback);
		feedBackButton.setText(Html.fromHtml("<u>Feedback</u>"));
		
		Font.setTypeface(Font.getRegular(getAssets()), passengerMsg, startButtonText, finishButton, feedBackButton);
		
		// init Tracker
		((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);

		if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(PassengerActivity.this) == ConnectionResult.SUCCESS) {
			requestingLocationUpdates = true;
			createGoogleApiClient();
			createLocationRequest();
			buildLocationSettingsRequest();
		}
		
		preparingDialog = new ProgressDialog(this, R.style.PopUpDialog);
    	preparingDialog.setTitle("Metropia");
    	preparingDialog.setMessage("Preparing...");
    	preparingDialog.setCanceledOnTouchOutside(false);
    	preparingDialog.setCancelable(false);
    	preparingDialog.show();
		
		Bundle extra = getIntent().getExtras();
		if (extra==null) return;
		
		
		//resume process
		reservId.set((Long) extra.getLong("reservationID", -1));
		if (reservId.get()!=-1) {
			toggleStatus(DURING_TRIP);
			startTrip();
		} else if (extra.getString("result") != null || extra.getString("ID")!=null) {
			validationResultIntent = new Intent(PassengerActivity.PASSENGER_TRIP_VALIDATOR);
			arrivalMsgTiggered.set(true);
			
			try {
				if (extra.getString("ID")!=null) {
					validationResultIntent.putExtras(getIntent().getExtras());
				}
				else {
					JSONObject result = new JSONObject(extra.getString("result"));
					TripService.putInfo(validationResultIntent, result);
				}
			} catch (JSONException e) {
				Log.e("parse validation result failed", e.toString());
			}
			
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("status", status);
		outState.putLong("reservId", reservId.get());
	}
	@Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        status = savedInstanceState.getInt("status");
        reservId.set(savedInstanceState.getLong("reservId"));
        toggleStatus(status);
    }
	
	private void toggleStatus(int status) {
		this.status = status;
		
		if (status==DURING_TRIP) {
			User user = User.getCurrentUser(PassengerActivity.this);
			View startButtonIcon = findViewById(R.id.startButtonIcon);
			TextView startButtonText = (TextView) findViewById(R.id.startButtonText);
			startButtonText.setTag(true);
			startButtonText.setText("END MY TRIP");
			startButtonText.setBackgroundColor(getResources().getColor(R.color.metropia_passenger_orange));
			startButtonIcon.setBackgroundColor(getResources().getColor(R.color.metropia_passenger_blue));
			TextView passengerMsg = (TextView) findViewById(R.id.passenger_msg);
			passengerMsg.setText(getResources().getString(R.string.passenger_start_ride, user.getFirstname()));
			passengerMsg.setGravity(Gravity.CENTER);
			findViewById(R.id.back_button).setVisibility(View.GONE);
		}
	}
	
	
	
	private void checkLastTrip(final Runnable cb) {
		
		findViewById(R.id.loading).setVisibility(View.VISIBLE);
		new DuoTripCheckRequest(User.getCurrentUser(PassengerActivity.this)).executeAsync(this, new ICallback() {

			@Override
			public void run(Object... obj) {
				Integer timeToNext = (Integer) obj[0];
				
				findViewById(R.id.loading).setVisibility(View.GONE);
				
				if (timeToNext==null) {
					NotificationDialog2 dialog = new NotificationDialog2(PassengerActivity.this, "Please check your internet setting or try again later.");
					dialog.setTitle("No internet connection");
					dialog.show();
					return;
				}
				
				String timeToNextStr = getResources().getQuantityString(R.plurals.minute, timeToNext, timeToNext);
				String timeStr = getResources().getQuantityString(R.plurals.minute, 15-timeToNext, 15-timeToNext);
				
				final DuoStyledDialog dialog = new DuoStyledDialog(PassengerActivity.this);
				dialog.setContent(getString(R.string.duoTripIntervalCheckTile, timeStr), getString(R.string.duoTripIntervalCheckMsg, timeToNextStr));
				dialog.addButton("OK", new ICallback() {
					public void run(Object... obj) {dialog.dismiss();}
				});
				
				if (timeToNext.equals(0)) cb.run();
				else dialog.show();
			}
		});
		
	}
	
	private void startTrip() {
		
		final ICallback start = new ICallback() {

			@Override
			public void run(Object... obj) {
				final long reserId = (Long) obj[0];
				if (reserId==-1) return;
				
				toggleStatus(DURING_TRIP);

				reservId.set(reserId);
				fetchPassengerPeriodly.run();
				checkLowSpeedTimer.run();
			}
		};
		
		if (reservId.get()!=-1) {
			start.run(reservId.get());
			return;
		}
		
		final ICallback cityRequestCb = new ICallback() {
			public void run(Object... obj) {
				final City city = (City) obj[0];
				
				SharedPreferences prefs = Preferences.getGlobalPreferences(PassengerActivity.this);
				int sunRideshareCount = prefs.getInt("SunRideshareCount", 0);
				if (sunRideshareCount==5) new SunRideshareActivityDialog(PassengerActivity.this, city, "sunrideshare", null).showAsync();
				
				PassengerReservationRequest resvReq = new PassengerReservationRequest(User.getCurrentUser(PassengerActivity.this), getString(R.string.distribution_date));
				resvReq.executeAsync(PassengerActivity.this, city, start);
			}
		};
		
		AsyncTask<Void, Void, Void> waitForGps = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				while(lastLocation==null) try {Thread.sleep(1000);} catch (InterruptedException e) {}
				
				new CityRequest(lastLocation.getLatitude(), lastLocation.getLongitude(), HTTP.defaultTimeout).executeAsync(PassengerActivity.this, cityRequestCb);
				return null;
			}
		}.execute();
	}
	
	
	Runnable fetchPassengerPeriodly = new Runnable() {

		@Override
		public void run() {
			
			PassengerRequest request = new PassengerRequest(User.getCurrentUser(PassengerActivity.this));
			request.executeAsync(PassengerActivity.this, reservId.get(), new ICallback() {

				@Override
				public void run(Object... obj) {
					ArrayList<Passenger> passengers = (ArrayList<Passenger>) obj[0];
					if (passengers!=null) remotePassengers = passengers;
				}
			});
			
			int interval = (Integer) DebugOptionsActivity.getDebugValue(PassengerActivity.this, DebugOptionsActivity.BUBBLE_HEAD_REQUESTING_INTERVAL, 1) * 60 * 1000;
			handler.postDelayed(fetchPassengerPeriodly, interval);
		}
	};
	

	boolean refreshPassenger = false;
	public static ArrayList<Passenger> remotePassengers = new ArrayList<Passenger>();
	ArrayList<Passenger> localPassengers = new ArrayList<Passenger>();
	
	
	Animation psAn;
	private void updatePassengerPosotion(Location location, boolean animated) {
		SKScreenPoint screenPoint = mapView.coordinateToPoint(new SKCoordinate(location.getLongitude(), location.getLatitude()));
		final RelativeLayout parent = (RelativeLayout) findViewById(R.id.passengers);
		RelativeLayout.LayoutParams layoutParams = (LayoutParams) parent.getLayoutParams();
		
		int bubbleHeadSize = Dimension.dpToPx(60, this.getResources().getDisplayMetrics());
		int parentWidth = ((View) parent.getParent()).getWidth();
		int parentHeight = ((View) parent.getParent()).getHeight();
		

		if (psAn!=null) parent.clearAnimation();
		
		if (mapView.getMapSettings().getFollowerMode()==SKMapFollowerMode.POSITION) {
			layoutParams.leftMargin = parentWidth/2-bubbleHeadSize/2;
			layoutParams.topMargin = parentHeight/2-bubbleHeadSize/2;
			layoutParams.rightMargin= 0;
			layoutParams.bottomMargin = 0;
		}
		else {
			if (!animated) {
				layoutParams.leftMargin = (int)screenPoint.getX()-bubbleHeadSize/2;
				layoutParams.topMargin = (int)screenPoint.getY()-bubbleHeadSize/2;
				layoutParams.rightMargin= (int) (parentWidth - (screenPoint.getX()-bubbleHeadSize/2 - bubbleHeadSize));
				layoutParams.bottomMargin = (int) (parentHeight - (screenPoint.getY()-bubbleHeadSize/2 - bubbleHeadSize));
			}
			
			final int currentLeftMargin = ((RelativeLayout.LayoutParams)parent.getLayoutParams()).leftMargin;
			final int currentTopMargin = ((RelativeLayout.LayoutParams)parent.getLayoutParams()).topMargin;
			final int newLeftMargin = (int) (screenPoint.getX()-bubbleHeadSize/2);
			final int newTopMargin = (int) (screenPoint.getY()-bubbleHeadSize/2);
			psAn = new Animation() {

			    @Override
			    protected void applyTransformation(float interpolatedTime, android.view.animation.Transformation t) {
			        LayoutParams params = (LayoutParams) parent.getLayoutParams();
			        params.leftMargin = (int) ((int)currentLeftMargin+((newLeftMargin-currentLeftMargin) * interpolatedTime));
			        params.topMargin = (int) ((int)currentTopMargin+((newTopMargin-currentTopMargin) * interpolatedTime));
			        parent.setLayoutParams(params);
			    }
			};
			psAn.setInterpolator(new DecelerateInterpolator());
			psAn.setDuration(3000); // in ms
			if (animated)
			parent.startAnimation(psAn);
		}
		
		parent.requestLayout();
		
	}
	@SuppressLint("NewApi")
	private void updatePassenger(Location location, boolean forceAnimated) {
		updatePassengerPosotion(location, true);
		
		boolean equal = remotePassengers.containsAll(localPassengers) && localPassengers.containsAll(remotePassengers);
		
		if (equal && !forceAnimated) return;
		
		for (int i=0 ; i<remotePassengers.size() ; i++) {
			if (!localPassengers.contains(remotePassengers.get(i))) {
				Misc.playCustomSound(this, R.raw.duo_onboard);
				break;
			}
		}
		if (!equal) localPassengers = remotePassengers;
		

		ViewGroup parent = (ViewGroup) findViewById(R.id.passengers);
		parent.removeAllViews();
		final ArrayList<View> views = new ArrayList<View>();
		final ArrayList<ImageLoader> tasks = new ArrayList<ImageLoader>();
		
		
		
		int[] colors = {R.color.metropia_red, R.color.metropia_orange, R.color.metropia_blue, R.color.metropia_green};
		for (int i=0 ; i<localPassengers.size() ; i++) {
			final Passenger passenger = localPassengers.get(i);
			final boolean halo = i==1;
			final TextView view = new TextView(this);
			
				String name = passenger.userName;
				if (name.length()>6) name = name.substring(0, 1).toUpperCase();
				int padding = Dimension.dpToPx(5, getResources().getDisplayMetrics());
				
				view.setText(name);
				view.setGravity(Gravity.CENTER);
				view.setTextColor(Color.WHITE);
				view.setPadding(0, 0, padding, padding);
				view.setBackgroundDrawable(ImageUtil.getRoundedShape(this, colors[1], halo));
			
			if (!StringUtils.isBlank(passenger.photoUrl)) {
				
				if (passenger.drawable!=null) {
					view.setBackgroundDrawable(passenger.drawable);
					view.setText("");
				}
				else tasks.add(new ImageLoader(this, passenger.photoUrl, new ICallback() {
					public void run(Object... obj) {
						if (obj[0]==null) return;
						
						Drawable drawable = ImageUtil.getRoundedShape((Drawable) obj[0]);
						passenger.setDrawable(ImageUtil.addShadow(PassengerActivity.this, drawable, halo));
						view.setBackgroundDrawable(ImageUtil.addShadow(PassengerActivity.this, drawable, halo));
						view.setText("");
					}
				}).execute(false));
			}
			
			views.add(view);
			parent.addView(view, 0);
			
	    	int haloPadding = halo? Dimension.dpToPx(2, getResources().getDisplayMetrics()):0;
			view.getLayoutParams().width = Dimension.dpToPx(60, this.getResources().getDisplayMetrics()) + haloPadding*2;
			view.getLayoutParams().height = Dimension.dpToPx(60, this.getResources().getDisplayMetrics()) + haloPadding*2;
			view.setAlpha(0);
			if (halo) view.setTag("halo");
		}
		
		new CircularPopupAnimation(views, 1);

	}
	
	@Override
	 public void onWindowFocusChanged(boolean hasFocus) {
	  super.onWindowFocusChanged(hasFocus);
	  refreshPassenger = true;
	 }
	
	
	private Location lastLocation;
	
	private void preProcessLocation(Location location) {
		if (ValidationActivity.isBetterLocation(location, lastLocation)) {
			lastLocation = location;
			locationChanged(location);
			try {
				PassiveLocationChangedReceiver.processLocation(getApplicationContext(), location);
			} catch (Exception ignore) {}
		}
	}
	
	private Trajectory trajectory = new Trajectory();
	private AtomicBoolean arrived = new AtomicBoolean(false);
	private AtomicLong reservId = new AtomicLong(-1);
	
	//float counter = 0;
	private void locationChanged(Location location) {

		//counter+=0.001;
		//location.setLatitude(location.getLatitude()+counter);
		
		if(reservId.get() > 0) {
			trajectory.accumulate(location, Trajectory.DEFAULT_LINK_ID);
			List<Record> records = trajectory.getRecords();
			long interval = records.get(records.size()-1).getTime() - records.get(0).getTime();
			if (!arrived.get() && interval>=60*1000) {
				saveTrajectory();
			}
			
			writeTripLog();
			
			double speed = NavigationView.metersToMiles(location.getSpeed())*(60*60);	//	miles/hour
			if (speed>5) lowSpeedTimer = 0;
		}
		mapView.reportNewGPSPosition(new SKPosition(location));
		mapView.getMapSettings().setCurrentPositionShown(true);
		updatePassenger(location, false);
	}
	
	private void writeTripLog() {
		if (arrived.get()) return;
		try {
			JSONObject tripLog = new JSONObject();
			tripLog.put(ResumeNavigationUtils.LAST_UPDATE_TIME, System.currentTimeMillis());
			FileUtils.writeStringToFile(ResumeNavigationUtils.getFile(this, reservId.get()), tripLog.toString());
		} catch (Exception ignore) {}
	}
	
	int lowSpeedTimer = 0;
	Runnable checkLowSpeedTimer = new Runnable() {

		@Override
		public void run() {
			if (lastLocation==null) return;
			lowSpeedTimer++;
			if (lowSpeedTimer>=15*60) doCancelValidation();
			else handler.postDelayed(checkLowSpeedTimer, 1000);
		}
	};
	

	
	
	private AtomicBoolean arrivalMsgDisplayed = new AtomicBoolean();
	private NumberFormat nf = new DecimalFormat("#.#");
	
	private void doDisplayArrivalMsg(final int uPoints, double duration, double distance, double THRESHOLD_DURATION, double THRESHOLD_DISTANCE, String driverName, String voice, String wheelUrl) {
		if (!arrivalMsgDisplayed.get()) {
			arrivalMsgDisplayed.set(true);
			findViewById(R.id.opt_panel).setVisibility(View.GONE);
			findViewById(R.id.loading).setVisibility(View.GONE);
			final View panel = findViewById(R.id.congrats_panel);
			wheel = (Wheel) findViewById(R.id.wheel);
			
			String userName = User.getCurrentUser(this).getFirstname();
			
			if (uPoints==0) {
				if (duration>=THRESHOLD_DURATION && distance>=THRESHOLD_DISTANCE) {
		            ((TextView)findViewById(R.id.duoFailedPanelText)).setText(getString(R.string.duoFailHeadMsg, userName));
					((TextView)findViewById(R.id.duoFailedDialogTitle)).setText(R.string.duoNoDriverTitle);
					((TextView)findViewById(R.id.duoFailedDialogDurationText)).setText(R.string.duoNoDriverMsg);
					((TextView)findViewById(R.id.duoFailedDialogDurationText)).getLayoutParams().height = -2;
					findViewById(R.id.duoFailedDialogDurationIcon).setVisibility(View.GONE);
					findViewById(R.id.duoFailedDialogDistanceIcon).setVisibility(View.GONE);
				}
				else {
					int icon1 = duration>=THRESHOLD_DURATION? R.drawable.duo_succeed:R.drawable.duo_failed;
					int icon2 = distance>=THRESHOLD_DISTANCE? R.drawable.duo_succeed:R.drawable.duo_failed;
					String durationStr = getResources().getQuantityString(R.plurals.minute, (int)duration, (int)duration);
					String distanceStr = StringUtil.formatRoundingDistance(distance, false);
					
		            ((TextView)findViewById(R.id.duoFailedPanelText)).setText(getString(R.string.duoFailHeadMsg, userName));
					((TextView)findViewById(R.id.duoFailedDialogTitle)).setText(R.string.duoFailTitle);
					((TextView)findViewById(R.id.duoFailedDialogDurationText)).setText(getString(R.string.duoFailDurationMsg, nf.format(THRESHOLD_DURATION), durationStr));
					((TextView)findViewById(R.id.duoFailedDialogDistanceText)).setText(getString(R.string.duoFailDistanceMsg, nf.format(THRESHOLD_DISTANCE), distanceStr));
					((ImageView)findViewById(R.id.duoFailedDialogDurationIcon)).setImageResource(icon1);
					((ImageView)findViewById(R.id.duoFailedDialogDistanceIcon)).setImageResource(icon2);
				}
				findViewById(R.id.duoFailedPanel).setVisibility(View.VISIBLE);
			}
			else {
				wheel.setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.duoPoint)).setText(Integer.toString(uPoints));
				((TextView)findViewById(R.id.congrats_msg)).setText(getString(R.string.duoCongratulationMsg, driverName));
				if (StringUtils.isNotBlank(voice)) {
					speaker.speak(voice);
				}
				
				wheel.setReservationId(reservId.get());
				wheel.setCallback(new Runnable() {
					public void run() {
						if (wheel.bonus!=null) {
							((TextView)PassengerActivity.this.findViewById(R.id.duoTotalPoints)).setText(Integer.toString(uPoints+wheel.bonus));
						}
						else {
							NotificationDialog dialog = new NotificationDialog(PassengerActivity.this, "An error has occurred.");
							dialog.setActionListener(new NotificationDialog.ActionListener() {
								public void onClickDismiss() {finish();}
							});
							dialog.show();
						}
					}
				});
				
				findViewById(R.id.duoSucceedPanel).setVisibility(View.VISIBLE);
				
				SharedPreferences prefs = Preferences.getGlobalPreferences(this);
				int sunRideshareCount = prefs.getInt("SunRideshareCount", 0);
				prefs.edit().putInt("SunRideshareCount", ++sunRideshareCount).commit();
			}
			
			int[] RobotLight = {R.id.duoFailedDialogTitle, R.id.duoFailedDialogDurationText, R.id.duoFailedDialogDistanceText};
			int[] RobotRegular = {R.id.duoFailedPanelText};
			int[] RobotMedium = {R.id.duoPointString1, R.id.duoPointString2};
			int[] RobotBold = {R.id.duoPoint, R.id.congrats_msg, R.id.duoTotalPointsString, R.id.duoTotalPoints, R.id.close, R.id.feedback};
			Font.setTypeface(this, Font.getRobotoLight(getAssets()), RobotLight);
			Font.setTypeface(this, Font.getRegular(getAssets()), RobotRegular);
			Font.setTypeface(this, Font.getMedium(getAssets()), RobotMedium);
			Font.setTypeface(this, Font.getRobotoBold(getAssets()), RobotBold);
			

			if (uPoints==0) {
				panel.setVisibility(View.VISIBLE);
    			Misc.fadeIn(PassengerActivity.this, panel);
			}
			else {
				new ImageLoader(this, wheelUrl, new ICallback() {
					public void run(Object... obj) {
						if(obj[0]==null) return;
	                    wheel.setImage((Drawable)obj[0]);
	                    	
	                    panel.setVisibility(View.VISIBLE);
	            		Misc.fadeIn(PassengerActivity.this, panel);
					}
				}).execute(true);
			}
			
			closeGPS();
		}
	}
	
	private int seq = 1;
	
	private void saveTrajectory(final Runnable callback) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					final File tFile = SendTrajectoryService.getDuoFile(PassengerActivity.this, reservId.get(), seq++);
					final JSONArray tJson = trajectory.toJSON();
					trajectory.clear();
					Misc.parallelExecute(new AsyncTask<Void, Void, Void>() {
						@Override
						protected Void doInBackground(Void... params) {
							try {
								FileUtils.write(tFile, tJson.toString());
							} catch (IOException e) {}
							return null;
						}

						@Override
						protected void onPostExecute(Void result) {
							if (callback != null) {
								callback.run();
							}
							if (!TrajectorySendingService.isRunning) startService(new Intent(PassengerActivity.this, TrajectorySendingService.class));
						}
					});
				} catch (Throwable t) {}
			}
		});
	}

	private void saveTrajectory() {
		saveTrajectory(null);
	}
	
	private GoogleApiClient googleApiClient;
	private LocationRequest highAccuracyLocationRequest;
	private boolean requestingLocationUpdates = false;
	private LocationSettingsRequest locationSettingsRequest;
	private Integer REQUEST_CHECK_SETTINGS = Integer.valueOf(1111);
	private com.google.android.gms.location.LocationListener gpsLocationListener;

	private void createGoogleApiClient() {
		googleApiClient = new GoogleApiClient.Builder(PassengerActivity.this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(PassengerActivity.this)
				.addOnConnectionFailedListener(PassengerActivity.this).build();
	}

	private void createLocationRequest() {
		highAccuracyLocationRequest = new LocationRequest();
		highAccuracyLocationRequest.setInterval(DebugOptionsActivity.getGpsUpdateInterval(this));
		highAccuracyLocationRequest.setFastestInterval(1000);
		highAccuracyLocationRequest.setSmallestDisplacement(0);
		highAccuracyLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	protected void buildLocationSettingsRequest() {
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
		builder.addLocationRequest(highAccuracyLocationRequest).setAlwaysShow(
				true);
		locationSettingsRequest = builder.build();
	}

	protected void checkLocationSettings() {
		PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);
		result.setResultCallback(PassengerActivity.this);
	}

	protected void startLocationUpdates() {
		LocationServices.FusedLocationApi.requestLocationUpdates(
				googleApiClient, highAccuracyLocationRequest,
				gpsLocationListener);
	}
	
	/**
	 * init skmap parameter
	 */
	private void initSKMaps(SKMapViewHolder mapViewHolder) {
		mapView = mapViewHolder.getMapSurfaceView();
		mapView.clearAllOverlays();
		mapView.deleteAllAnnotationsAndCustomPOIs();
		mapView.rotateTheMapToNorth();
		mapView.setZoom(DEFAULT_ZOOM_LEVEL);
		mapView.getMapSettings().setCurrentPositionShown(false);
		mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.POSITION);
		mapView.getMapSettings().setMapDisplayMode(SKMapDisplayMode.MODE_2D);
		mapView.getMapSettings().setMapRotationEnabled(false);
		mapView.getMapSettings().setMapZoomingEnabled(true);
		mapView.getMapSettings().setMapPanningEnabled(true);
		mapView.getMapSettings().setZoomWithAnchorEnabled(true);
		mapView.getMapSettings().setInertiaRotatingEnabled(true);
		mapView.getMapSettings().setInertiaZoomingEnabled(true);
		mapView.getMapSettings().setInertiaPanningEnabled(true);
		mapView.getMapSettings().setMapStyle(SkobblerUtils.getMapViewStyle(PassengerActivity.this, true));
	}

	private void prepareGPS() {
		if (googleApiClient != null && requestingLocationUpdates) {
			checkLocationSettings();
		} else if (googleApiClient == null) {
			closeGPS();
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
					&& locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DebugOptionsActivity.getGpsUpdateInterval(this), 5, locationListener);
			} else {
				SystemService.alertNoGPS(this, true);
			}
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		}
	}

	private void closeGPS() {
		if (googleApiClient != null && googleApiClient.isConnected()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(
					googleApiClient, gpsLocationListener).setResultCallback(
					new ResultCallback<Status>() {
						@Override
						public void onResult(Status status) {
							requestingLocationUpdates = true;
						}
					});
		} else if (locationManager != null) {
			try {
				locationManager.removeUpdates(locationListener);
			}
			catch(Throwable t) {}
		}
	}
	
	private void cancelValidation() {
		if (arrivalMsgTiggered.get()) {
			if (!isFinishing()) {
				finish();
			}
		} else {
			
			final DuoStyledDialog dialog = new DuoStyledDialog(this).setContent("End Trip?", "Are you sure?").centerContent();
			dialog.addButton("NO", new ICallback() {
				public void run(Object... obj) {dialog.dismiss();}
			});
			dialog.addButton("YES", new ICallback() {
				public void run(Object... obj) {doCancelValidation();dialog.dismiss();}
			});
			dialog.show();
		}
	}


	private void doCancelValidation() {
		reportValidation(null);
	}
	
	private AtomicBoolean reported = new AtomicBoolean(false);
	
	private void reportValidation(final Runnable callback) {
		if (!reported.get()) {
			reported.set(true);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					displayArrivalMsg(callback);
				}
			});
		}
	}
	
	private void displayArrivalMsg(final Runnable callback) {
		arrivalMsgTiggered.set(true);
		arrived.set(true);
		status = END_TRIP;
		handler.removeCallbacks(fetchPassengerPeriodly);
		handler.removeCallbacks(checkLowSpeedTimer);
		
		ResumeNavigationUtils.cleanTripLog(this);

		saveTrajectory(new Runnable() {
			@Override
			public void run() {
				saveTrip(new Runnable() {
					@Override
					public void run() {
						TripService.runImd(PassengerActivity.this,	User.getCurrentUser(PassengerActivity.this), reservId.get(), PASSENGER_TRIP_VALIDATOR);
					}
				});
			}
		});

		SKPosition currentPosition = mapView.getCurrentGPSPosition(true);
		if (currentPosition != null) {
			Intent updateMyLocation = new Intent(LandingActivity2.UPDATE_MY_LOCATION);
			updateMyLocation.putExtra("lat", currentPosition.getCoordinate().getLatitude());
			updateMyLocation.putExtra("lon", currentPosition.getCoordinate().getLongitude());
			sendBroadcast(updateMyLocation);
		}

		findViewById(R.id.loading).setVisibility(View.VISIBLE);
		findViewById(R.id.loading).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// do nothing	
			}
		});

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					if (callback != null) {
						callback.run();
					}
					showNotifyLaterDialog();
				} catch (Throwable t) {}
			}
		}, Request.fifteenSecsTimeout * 2);
	}
	
	private void saveTrip(final Runnable callback) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					final File tFile = TripService.getFile(PassengerActivity.this, reservId.get());
					Misc.parallelExecute(new AsyncTask<Void, Void, Void>() {
						@Override
						protected Void doInBackground(Void... params) {
							try {
								JSONObject reservDetail = new JSONObject();
								try {
									reservDetail.put(CongratulationActivity.DEPARTURE_TIME,	reservId.get());
									reservDetail.put("MODE", PASSENGER_TRIP_VALIDATOR);
								} catch (JSONException e) {}
								FileUtils.write(tFile, reservDetail.toString());
							} catch (IOException e) {}
							if (callback != null) {
								callback.run();
							}
							return null;
						}
					});
				} catch (Throwable t) {}
			}
		});
	}
	
	public static final String PASSENGER_TRIP_VALIDATOR = "PASSENGER_TRIP_VALIDATOR";
	
	private AtomicBoolean arrivalMsgTiggered = new AtomicBoolean(false);

	private BroadcastReceiver tripValidator = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (arrivalMsgTiggered.get()) {
				String id = intent.getStringExtra(ValidationActivity.ID);
				boolean success = intent.getBooleanExtra(ValidationActivity.REQUEST_SUCCESS, false);
				
				if (/*String.valueOf(reservId.get()).equals(id) && */success) {
					String voice = intent.getStringExtra(ValidationActivity.VOICE);
					int credit = intent.getIntExtra(ValidationActivity.CREDIT, 0);
					double duration = intent.getDoubleExtra("duration", 0);
					double distance = intent.getDoubleExtra("distance", 0);
					double DUO_duration = intent.getDoubleExtra("DUO_duration", 10);
					double DUO_distance = intent.getDoubleExtra("DUO_distance", 3);
					String driverName = intent.getStringExtra("driver_name");
					String wheelUrl = intent.getStringExtra("wheel_url");
					doDisplayArrivalMsg(credit, duration, distance, DUO_duration, DUO_distance, driverName, voice, wheelUrl);
				} else if (String.valueOf(reservId.get()).equals(id) && !success) {
					showNotifyLaterDialog();
				}
			}
		}
	};
	
	private static final Integer ID = 123451;
	public void showNotification() {
		Intent validationIntent = new Intent(this, MainActivity.class);
		validationIntent.setAction(Intent.ACTION_MAIN);
		validationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent sender = PendingIntent.getActivity(this, ID, validationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon_small, "Metropia", System.currentTimeMillis());
		notification.setLatestEventInfo(this, "Metropia", "DUO trip in progress.", sender);
		notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;            
		notificationManager.notify(ID, notification);
	}
	public void hideNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(ID);
	}
	
	private void showNotifyLaterDialog() {
		if (!arrivalMsgDisplayed.getAndSet(true)) {
			findViewById(R.id.loading).setVisibility(View.GONE);
			NotificationDialog2 dialog = new NotificationDialog2(PassengerActivity.this, "There's a temporary connection issue, but we'll update your trip results shortly. Thanks for your patience!");
			dialog.setTitle("Thanks for using Metropia");
			dialog.setPositiveButtonText("OK");
			dialog.setPositiveActionListener(new ActionListener() {
				@Override
				public void onClick() {
					finish();
				}
			});
			dialog.show();
			closeGPS();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Handle the back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (findViewById(R.id.congrats_panel).getVisibility()==View.VISIBLE) {
				findViewById(R.id.close).performClick();
			}
			else if((Boolean)findViewById(R.id.startButtonText).getTag()) {
				cancelValidation();
			}
			else {
				finish();
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
		if (googleApiClient != null) {
			googleApiClient.connect();
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
		mapViewHolder.onResume();
		hideNotification();
	}
	
	@Override
	protected void onPause() {
		Localytics.dismissCurrentInAppMessage();
		Localytics.clearInAppMessageDisplayActivity();
		Localytics.closeSession();
		Localytics.upload();
		super.onPause();
		mapViewHolder.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
		if (status==DURING_TRIP) showNotification();
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(tripValidator);
		closeGPS();
		remotePassengers = new ArrayList<Passenger>();
		if (googleApiClient != null) {
			googleApiClient.disconnect();
		}
		if (speaker!=null) speaker.shutdown();
		hideNotification();
		super.onDestroy();
	}
	
	@Override
    public void finish() {
    	Intent i = new Intent(this, LandingActivity2.class);
    	this.startActivity(i);
    	super.finish();
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		speaker.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	public void onResult(LocationSettingsResult locationSettingsResult) {
		final Status status = locationSettingsResult.getStatus();
		switch (status.getStatusCode()) {
			case LocationSettingsStatusCodes.SUCCESS:
				startLocationUpdates();
				break;
			case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
				try {
					status.startResolutionForResult(PassengerActivity.this,	REQUEST_CHECK_SETTINGS);
				} catch (IntentSender.SendIntentException e) {}
				break;
			case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
				startLocationUpdates();
				break;
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {}
	public void onConnected(Bundle arg0) {}
	public void onConnectionSuspended(int arg0) {}

	private Handler handler = new Handler();
	private Runnable restoreRunnable = new Runnable() {
		public void run() {
			mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.POSITION);
		}
	};
	
	@Override
	public void onActionPan() {
		mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.NONE);
		handler.removeCallbacks(restoreRunnable);
		handler.postDelayed(restoreRunnable, 30000);
	}

	@Override
	public void onActionZoom() {}

	@Override
	public void onAnnotationSelected(SKAnnotation arg0) {}

	@Override
	public void onBoundingBoxImageRendered(int arg0) {}

	@Override
	public void onCompassSelected() {}

	@Override
	public void onCurrentPositionSelected() {}

	@Override
	public void onCustomPOISelected(SKMapCustomPOI arg0) {}

	@Override
	public void onDoubleTap(SKScreenPoint arg0) {}

	@Override
	public void onGLInitializationError(String arg0) {}

	@Override
	public void onInternationalisationCalled(int arg0) {}

	@Override
	public void onInternetConnectionNeeded() {}

	@Override
	public void onLongPress(SKScreenPoint point) {}

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
		SKPosition point = mapView.getCurrentGPSPosition(true);
		Location location = new Location("");
		location.setLongitude(point.getLongitude());
		location.setLatitude(point.getLatitude());
		updatePassengerPosotion(location, false);
	}

	@Override
	public void onObjectSelected(int arg0) {}

	@Override
	public void onPOIClusterSelected(SKPOICluster arg0) {}

	@Override
	public void onRotateMap() {}

	@Override
	public void onSingleTap(SKScreenPoint arg0) {}

	@Override
	public void onSurfaceCreated(SKMapViewHolder mapViewHolder) {
		initSKMaps(mapViewHolder);
		mapView.postDelayed(new Runnable() {

			@Override
			public void run() {
				preparingDialog.dismiss();
				if (validationResultIntent==null) return;
				
				sendBroadcast(validationResultIntent);
				
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancel(TripService.DUO_NOTI_ID);

				File[] files = new File(getExternalFilesDir(null), "trip").listFiles();
				for (File f : files) {
					if (f.getName().equals(validationResultIntent.getStringExtra("ID"))) FileUtils.deleteQuietly(f);
				}
			}
		}, 2000);
		
		LocationInfo cacheLoc = new LocationInfo(PassengerActivity.this);
		if(System.currentTimeMillis() - cacheLoc.lastLocationUpdateTimestamp <= LandingActivity2.ONE_HOUR) {
	        Location loc = new Location("");
	        loc.setLatitude(cacheLoc.lastLat);
	        loc.setLongitude(cacheLoc.lastLong);
	        loc.setTime(cacheLoc.lastLocationUpdateTimestamp - ValidationActivity.TWO_MINUTES);
	        loc.setAccuracy(cacheLoc.lastAccuracy);
	        loc.setBearing(cacheLoc.lastHeading);
	        locationChanged(loc);
	        if (!arrived.get()) prepareGPS();
		}
	}

	@Override
	public void onDebugInfo(double arg0, float arg1, double arg2) {}

	@Override
	public void onClick(final View v) {
		switch(v.getId()) {
			case R.id.back_button:
				finish();
			break;
			case R.id.center_map_icon:
				mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.POSITION);
				handler.removeCallbacks(restoreRunnable);
				
				SKPosition point = mapView.getCurrentGPSPosition(true);
				Location location = new Location("");
				location.setLongitude(point.getLongitude());
				location.setLatitude(point.getLatitude());
				updatePassenger(location, true);
			break;
			case R.id.startButtonText:
				boolean currentTag = (Boolean)v.getTag();
				if(currentTag) {
					cancelValidation();
				}
				else {
					Runnable startTrip = new Runnable() {
						public void run() {
							startTrip();
						}
					};
					checkLastTrip(startTrip);
				}
			break;
			case R.id.close:
				if (wheel.spinning) ;
				else if (wheel.getVisibility()==View.VISIBLE && wheel.bonus==null) {
					
					final DuoStyledDialog dialog = new DuoStyledDialog(this);
					dialog.setContent("Are you sure?", getString(R.string.duoExitWithoutSpin));
					dialog.addButton("DON'T SPIN", new ICallback() {
						public void run(Object... obj) {
							wheel.spinWithoutAnimation();
							finish();
							dialog.dismiss();
						}
					});
					dialog.addButton("SPIN", new ICallback() {
						public void run(Object... obj) {
							dialog.dismiss();
						}
					});
					dialog.show();
				}
				else finish();
				
			break;
			case R.id.share:
				Intent intentShare = new Intent(this, ShareActivity.class);
				intentShare.putExtra(ShareActivity.TITLE, "More Metropians = Less Traffic");
				intentShare.putExtra(ShareActivity.SHARE_TEXT, Misc.APP_DOWNLOAD_LINK);
				startActivity(intentShare);
			break;
			case R.id.feedback:
				Intent intentFeedback = new Intent(PassengerActivity.this,	FeedbackActivity.class);
				startActivity(intentFeedback);
			break;
		}
		
	}

}
