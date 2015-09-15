package com.metropia.activities;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.metropia.SendTrajectoryService;
import com.metropia.SkobblerUtils;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.TripService;
import com.metropia.dialogs.CancelableProgressDialog;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.dialogs.NotificationDialog2.ActionListener;
import com.metropia.models.Passenger;
import com.metropia.models.Trajectory;
import com.metropia.models.User;
import com.metropia.requests.DuoTripCheckRequest;
import com.metropia.requests.PassengerReservationRequest;
import com.metropia.requests.Request;
import com.metropia.tasks.ImageLoader;
import com.metropia.ui.Wheel;
import com.metropia.ui.animation.CircularPopupAnimation;
import com.metropia.ui.animation.ClickAnimation;
import com.metropia.utils.Dimension;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;
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
import com.skobbler.ngx.routing.SKRouteManager;

public class PassengerActivity extends FragmentActivity implements SKMapSurfaceListener, ConnectionCallbacks, 
	OnConnectionFailedListener, ResultCallback<LocationSettingsResult>, OnClickListener {
	
	private LocationManager locationManager;
	private LocationListener locationListener;
	
	private SKMapViewHolder mapViewHolder;
	private SKMapSurfaceView mapView;
	private Wheel wheel;
	
	int[] clickable = {};
	int[] clickableAnimated = {R.id.back_button, R.id.center_map_icon, R.id.startButton, R.id.close, R.id.share, R.id.feedback};
	
	
	
	private static final Integer DEFAULT_ZOOM_LEVEL = 15;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.passenger);
		
		registerReceiver(tripValidator, new IntentFilter(PASSENGER_TRIP_VALIDATOR));
		

        ClickAnimation.OnClickListener onClickListener = new ClickAnimation.OnClickListener(this);
        for (int i=0 ; i<clickableAnimated.length ; i++) findViewById(clickableAnimated[i]).setOnClickListener(onClickListener);
        for (int i=0 ; i<clickable.length; i++) findViewById(clickable[i]).setOnClickListener(this);
		
		mapViewHolder = (SKMapViewHolder) findViewById(R.id.mapview_holder);
		mapViewHolder.hideAllAttributionTextViews();
		mapViewHolder.setMapSurfaceListener(this);
		
		Localytics.integrate(this);
		
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
		
		View startButton = findViewById(R.id.startButton);
		TextView startButtonText = (TextView) findViewById(R.id.startButtonText);
		startButton.setTag(false);
		
		TextView finishButton = (TextView) findViewById(R.id.close);
		finishButton.setText(Html.fromHtml("<u>Close</u>"));

		TextView feedBackButton = (TextView) findViewById(R.id.feedback);
		feedBackButton.setText(Html.fromHtml("<u>Feedback</u>"));
		
		/*int[] styledFontTextView = {R.id.duoTotalPoints, R.id.duoPointText1};
		ArrayList<TextView> textViews = new ArrayList<TextView>();
		for (int i=0 ; i<styledFontTextView.length ; i++) textViews.add((TextView) findViewById(styledFontTextView[i]));
		Font.setTypeface(Font.getRegular(getAssets()), textViews.toArray(new TextView[textViews.size()]));*/
		
		Font.setTypeface(Font.getRegular(getAssets()), passengerMsg, startButtonText, finishButton, feedBackButton);
		
		// init Tracker
		((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);

		if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(PassengerActivity.this) == ConnectionResult.SUCCESS) {
			requestingLocationUpdates = true;
			createGoogleApiClient();
			createLocationRequest();
			buildLocationSettingsRequest();
		}
	}
	
	private void checkLastTrip(final Runnable cb) {
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected void onPreExecute() {
				findViewById(R.id.loading).setVisibility(View.VISIBLE);
			}
			
			@Override
			protected Integer doInBackground(Void... arg0) {
				DuoTripCheckRequest request = new DuoTripCheckRequest(User.getCurrentUser(PassengerActivity.this));
				try {
					int timeToNext = request.execute(PassengerActivity.this);
					return timeToNext;
				} catch (Exception e) {
					
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(final Integer timeToNext) {
				findViewById(R.id.loading).setVisibility(View.GONE);
				NotificationDialog2 dialog = new NotificationDialog2(PassengerActivity.this, getString(R.string.duoTripIntervalCheckMsg));
				dialog.setTitle(getString(R.string.duoTripIntervalCheckTile, 15-timeToNext));
				dialog.setPositiveButtonText("OK");
				dialog.setPositiveActionListener(new ActionListener() {
					public void onClick() {if (cb!=null) cb.run();}
				});
				if (timeToNext.equals(0)) cb.run();
				else dialog.show();
			}
			
		}.execute();
	}
	
	private void startTrip() {
		AsyncTask<Void, Void, Long> reservTask = new AsyncTask<Void, Void, Long>() {
			CancelableProgressDialog dialog;
			ExceptionHandlingService es = new ExceptionHandlingService(PassengerActivity.this);
			@Override
			protected void onPreExecute() {
				dialog = new CancelableProgressDialog(PassengerActivity.this, "Preparing...");
	            dialog.setActionListener(new CancelableProgressDialog.ActionListener() {
	                @Override
	                public void onClickNegativeButton() {
	                    finish();
	                }
	            });
	            dialog.show();
		    }

			@Override
			protected Long doInBackground(Void... params) {
				try {
					
					PassengerReservationRequest resvReq = new PassengerReservationRequest(User.getCurrentUser(PassengerActivity.this), getString(R.string.distribution_date));
					return resvReq.execute(PassengerActivity.this);
				}
				catch(Exception e) {
					es.registerException(e);
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(final Long reserId) {
				if(dialog.isShowing()) {
					dialog.dismiss();
				}
				if(es.hasExceptions()) {
					es.reportExceptions();
				}
				else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							User user = User.getCurrentUser(PassengerActivity.this);
							reservId.set(reserId);
							View startButton = findViewById(R.id.startButton);
							View startButtonIcon = findViewById(R.id.startButtonIcon);
							final TextView startButtonText = (TextView) findViewById(R.id.startButtonText);
							startButton.setTag(true);
							startButtonText.setText("END MY TRIP");
							startButtonText.setBackgroundColor(getResources().getColor(R.color.metropia_passenger_orange));
							startButtonIcon.setBackgroundColor(getResources().getColor(R.color.metropia_passenger_blue));
							TextView passengerMsg = (TextView) findViewById(R.id.passenger_msg);
							passengerMsg.setText(getResources().getString(R.string.passenger_start_ride, user.getFirstname()));
							passengerMsg.setGravity(Gravity.CENTER);
							findViewById(R.id.back_button).setVisibility(View.GONE);
							prepareGPS();
						}
					});
				}
		    }
		};
		Misc.parallelExecute(reservTask);
	}
	

	boolean refreshPassenger = false;
	public static ArrayList<Passenger> remotePassengers = new ArrayList<Passenger>();
	ArrayList<Passenger> localPassengers = new ArrayList<Passenger>();
	
	
	private void updatePassengerPosotion(Location location) {
		SKScreenPoint screenPoint = mapView.coordinateToPoint(new SKCoordinate(location.getLongitude(), location.getLatitude()));
		RelativeLayout parent = (RelativeLayout) findViewById(R.id.passengers);
		RelativeLayout.LayoutParams layoutParams = (LayoutParams) parent.getLayoutParams();
		
		layoutParams.leftMargin = (int)screenPoint.getX()-75;
		layoutParams.topMargin = (int)screenPoint.getY()-75;
		
		if (mapView.getMapSettings().getFollowerMode()==SKMapFollowerMode.POSITION) {
			layoutParams.leftMargin = (int)((View)parent.getParent()).getWidth()/2-75;
			layoutParams.topMargin = (int)((View)parent.getParent()).getHeight()/2-75;
		}
		
		parent.requestLayout();
		
	}
	@SuppressLint("NewApi")
	private void updatePassenger(Location location, boolean forceAnimated) {
		updatePassengerPosotion(location);
		
		boolean equal = remotePassengers.containsAll(localPassengers) && localPassengers.containsAll(remotePassengers);
		
		if (equal && !forceAnimated) return;
		if (!equal) localPassengers = remotePassengers;
		

		ViewGroup parent = (ViewGroup) findViewById(R.id.passengers);
		parent.removeAllViews();
		final ArrayList<View> views = new ArrayList<View>();
		final ArrayList<ImageLoader> tasks = new ArrayList<ImageLoader>();
		
		Runnable cb = new Runnable() {
			public void run() {
				for (ImageLoader task:tasks) {
					if (!task.finished) return;
				}

				new CircularPopupAnimation(views, 1);
			}
		};
		
		for (int i=0 ; i<localPassengers.size() ; i++) {
			Passenger passenger = localPassengers.get(i);
			
			View view;
			
			if (StringUtils.isBlank(passenger.photoUrl)) {
				view = new TextView(this);
				TextView textView = (TextView) view;
				
				
				textView.setText(passenger.userName);
				textView.setGravity(Gravity.CENTER);
				textView.setTextColor(Color.WHITE);
			}
			else {
				view = new ImageView(this);
				if (passenger.drawable!=null) ((ImageView)view).setImageDrawable(passenger.drawable);
				else tasks.add(new ImageLoader(passenger, view, passenger.photoUrl, cb).execute());
			}
			
			views.add(view);
			parent.addView(view, 0);
			
			int padding = Dimension.dpToPx(5, getResources().getDisplayMetrics());

			view.setPadding(0, 0, padding, padding);
			view.setBackgroundResource(R.drawable.circular_button);
			view.getLayoutParams().width = Dimension.dpToPx(60, this.getResources().getDisplayMetrics());
			view.getLayoutParams().height = Dimension.dpToPx(60, this.getResources().getDisplayMetrics());
			view.setAlpha(0);
		}

		if (tasks.size()==0) new CircularPopupAnimation(views, 1);

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
	
	float counter = 0;
	private void locationChanged(Location location) {

		/*counter+=0.001;
		location.setLatitude(location.getLatitude()+counter);*/
		
		if(reservId.get() > 0) {
			trajectory.accumulate(location, Trajectory.DEFAULT_LINK_ID);
			if (!arrived.get() && trajectory.size() >= 8) {
				saveTrajectory();
			}
		}
		mapView.reportNewGPSPosition(new SKPosition(location));
		mapView.getMapSettings().setCurrentPositionShown(true);
		updatePassenger(location, false);
	}
	

	private final static double THRESHOLD_DURATION = 5;
	private final static double THRESHOLD_DISTANCE = 1.5;
	
	private AtomicBoolean arrivalMsgDisplayed = new AtomicBoolean();
	private NumberFormat nf = new DecimalFormat("#.#");
	
	private void doDisplayArrivalMsg(final int uPoints, double duration, double distance, int driverId, String voice, String wheelUrl) {
		if (!arrivalMsgDisplayed.get()) {
			arrivalMsgDisplayed.set(true);
			findViewById(R.id.opt_panel).setVisibility(View.GONE);
			findViewById(R.id.loading).setVisibility(View.GONE);
			final View panel = findViewById(R.id.congrats_panel);
			wheel = (Wheel) findViewById(R.id.wheel);
			
			String userName = User.getCurrentUser(this).getFirstname();
			
			if (uPoints==0) {
				if (duration>=THRESHOLD_DURATION && distance>=THRESHOLD_DISTANCE) {
					((TextView)findViewById(R.id.duoFailedDialogTitle)).setText(R.string.duoNoDriverTitle);
					((TextView)findViewById(R.id.duoFailedDialogDurationText)).setText(R.string.duoNoDriverMsg);
					findViewById(R.id.duoFailedDialogDurationIcon).setVisibility(View.GONE);
					findViewById(R.id.duoFailedDialogDistanceIcon).setVisibility(View.GONE);
				}
				else {
					int icon1 = duration>=THRESHOLD_DURATION? R.drawable.duo_succeed:R.drawable.duo_failed;
					int icon2 = distance>=THRESHOLD_DISTANCE? R.drawable.duo_succeed:R.drawable.duo_failed;
					
		            ((TextView)findViewById(R.id.duoFailedPanelText)).setText(getString(R.string.duoFailHeadMsg, userName));
					((TextView)findViewById(R.id.duoFailedDialogTitle)).setText(R.string.duoFailTitle);
					((TextView)findViewById(R.id.duoFailedDialogDurationText)).setText(getString(R.string.duoFailDurationMsg, duration));
					((TextView)findViewById(R.id.duoFailedDialogDistanceText)).setText(getString(R.string.duoFailDistanceMsg, distance));
					((ImageView)findViewById(R.id.duoFailedDialogDurationIcon)).setImageResource(icon1);
					((ImageView)findViewById(R.id.duoFailedDialogDistanceIcon)).setImageResource(icon2);
				}
				findViewById(R.id.duoFailedPanel).setVisibility(View.VISIBLE);
			}
			else {
				String driverName = localPassengers.size()>1? localPassengers.get(1).userName:"";
				wheel.setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.duoPoint)).setText(Integer.toString(uPoints));
				((TextView)findViewById(R.id.congrats_msg)).setText(getString(R.string.duoCongratulationMsg, driverName));
				if (StringUtils.isNotBlank(voice)) {
					//speak
				}
				
				wheel.setDriverId(driverId);
				wheel.setCallback(new Runnable() {
					public void run() {
						if (wheel.bonus!=null) {
							((TextView)PassengerActivity.this.findViewById(R.id.duoTotalPoints)).setText(Integer.toString(uPoints+wheel.bonus));
						}
						else {
							NotificationDialog2 dialog = new NotificationDialog2(PassengerActivity.this, "An error has occurred.");
							dialog.setPositiveActionListener(new ActionListener() {
								public void onClick() {finish();}
							});
							dialog.show();
						}
					}
				});
				
				findViewById(R.id.duoSucceedPanel).setVisibility(View.VISIBLE);
			}

			TextView[] styledTexts = new TextView[] {
					(TextView) findViewById(R.id.congrats_msg),
					(TextView) findViewById(R.id.close),
					(TextView) findViewById(R.id.feedback)
			};
			Font.setTypeface(Font.getRobotoBold(getAssets()), styledTexts);

			
			LandingActivity2.LoadImageTask wheelDownloader = new LandingActivity2.LoadImageTask(this, wheelUrl) {
                protected void onPostExecute(final Bitmap rs) {
                    if(rs != null){
                    	wheel.setImage(rs);
                    	
                    	panel.setVisibility(View.VISIBLE);
            			Misc.fadeIn(PassengerActivity.this, panel);
                    }else{
                    	
                    }
                }
            };
            Misc.parallelExecute(wheelDownloader);
			
			
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
		PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
				.checkLocationSettings(googleApiClient, locationSettingsRequest);
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
			// Ask the user if they want to quit
			NotificationDialog2 dialog = new NotificationDialog2(PassengerActivity.this, "Are you sure?");
			dialog.setTitle("End Trip");
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
				if (String.valueOf(reservId.get()).equals(id) && success) {
					String voice = intent.getStringExtra(ValidationActivity.VOICE);
					int credit = intent.getIntExtra(ValidationActivity.CREDIT, 0);
					double duration = intent.getDoubleExtra("duration", 0);
					double distance = intent.getDoubleExtra("distance", 0);
					int driverId = intent.getIntExtra("driver_id", -1);
					String wheelUrl = intent.getStringExtra("wheel_url");
					doDisplayArrivalMsg(credit, duration, distance, driverId, voice, wheelUrl);
				} else if (String.valueOf(reservId.get()).equals(id) && !success) {
					showNotifyLaterDialog();
				}
			}
		}
	};
	
	private void showNotifyLaterDialog() {
		if (!arrivalMsgDisplayed.getAndSet(true)) {
			findViewById(R.id.loading).setVisibility(View.GONE);
			NotificationDialog2 dialog = new NotificationDialog2(PassengerActivity.this,
					"There's a temporary connection issue, but we'll update your trip results shortly. Thanks for your patience!");
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
			if((Boolean)findViewById(R.id.startButton).getTag()) {
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
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(tripValidator);
		closeGPS();
		remotePassengers = new ArrayList<Passenger>();
		if (googleApiClient != null) {
			googleApiClient.disconnect();
		}
		super.onDestroy();
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
		updatePassengerPosotion(location);
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
		LocationInfo cacheLoc = new LocationInfo(PassengerActivity.this);
		if(System.currentTimeMillis() - cacheLoc.lastLocationUpdateTimestamp <= LandingActivity2.ONE_HOUR) {
	        Location loc = new Location("");
	        loc.setLatitude(cacheLoc.lastLat);
	        loc.setLongitude(cacheLoc.lastLong);
	        loc.setTime(cacheLoc.lastLocationUpdateTimestamp - ValidationActivity.TWO_MINUTES);
	        loc.setAccuracy(cacheLoc.lastAccuracy);
	        loc.setBearing(cacheLoc.lastHeading);
	        locationChanged(loc);
		}
	}

	@Override
	public void onDebugInfo(double arg0, float arg1, double arg2) {}

	@Override
	public void onClick(View v) {
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
			case R.id.startButton:
				boolean currentTag = (Boolean)v.getTag();
				if(currentTag) {
					cancelValidation();
				}
				else {
					checkLastTrip(new Runnable() {
						public void run() {
							startTrip();
						}
					});
				}
			break;
			case R.id.close:
				if (wheel.getVisibility()==View.VISIBLE && wheel.bonus==null) {
					
					NotificationDialog2 dialog = new NotificationDialog2(this, getString(R.string.duoExitWithoutSpin));
					dialog.setTitle("Please Spin");
					dialog.setPositiveButtonText("OK");
					dialog.show();
					
					return;
				}
				SKRouteManager.getInstance().clearCurrentRoute();
				finish();
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
