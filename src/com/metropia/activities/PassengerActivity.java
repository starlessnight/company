package com.metropia.activities;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
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
import com.metropia.LocalyticsUtils;
import com.metropia.SendTrajectoryService;
import com.metropia.SkobblerUtils;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.TripService;
import com.metropia.dialogs.CancelableProgressDialog;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.dialogs.NotificationDialog2.ActionListener;
import com.metropia.models.Trajectory;
import com.metropia.models.User;
import com.metropia.requests.PassengerReservationRequest;
import com.metropia.requests.Request;
import com.metropia.ui.ClickAnimation;
import com.metropia.ui.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;
import com.metropia.utils.SystemService;
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
	OnConnectionFailedListener, ResultCallback<LocationSettingsResult> {
	
	private LocationManager locationManager;
	private LocationListener locationListener;
	
	private SKMapViewHolder mapViewHolder;
	private SKMapSurfaceView mapView;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.passenger);
		
		registerReceiver(tripValidator, new IntentFilter(PASSENGER_TRIP_VALIDATOR));
		
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
		passengerMsg.setText(String.format(getResources().getString(R.string.passenger_before_ride), user.getFirstname() + " " + user.getLastname()));
		findViewById(R.id.back_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation clickAnimation = new ClickAnimation(PassengerActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						v.setClickable(true);
						finish();
					}
				});
			}
		});
		
		final TextView startOrEndButton = (TextView) findViewById(R.id.start_or_end_trip);
		startOrEndButton.setTag(false);
		startOrEndButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation clickAnimation = new ClickAnimation(PassengerActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						boolean currentTag = (Boolean)startOrEndButton.getTag();
						if(currentTag) {
							cancelValidation();
						}
						else {
							startTrip();
						}
						v.setClickable(true);
					}
				});
			}
		});
		
		TextView finishButton = (TextView) findViewById(R.id.close);
		finishButton.setText(Html.fromHtml("<u>Close</u>"));
		finishButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnim = new ClickAnimation(PassengerActivity.this, v);
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
				ClickAnimation clickAnimation = new ClickAnimation(PassengerActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Intent intent = new Intent(PassengerActivity.this, ShareActivity.class);
						intent.putExtra(ShareActivity.TITLE, "More Metropians = Less Traffic");
						intent.putExtra(ShareActivity.SHARE_TEXT, Misc.APP_DOWNLOAD_LINK);
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
				ClickAnimation clickAnimation = new ClickAnimation(PassengerActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Intent intent = new Intent(PassengerActivity.this,	FeedbackActivity.class);
						startActivity(intent);
					}
				});
			}
		});
		
		findViewById(R.id.co2_circle).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent;
				if (WebMyMetropiaActivity.hasCo2SavingUrl(PassengerActivity.this) || WebMyMetropiaActivity.hasMyMetropiaUrl(PassengerActivity.this)) {
					intent = new Intent(PassengerActivity.this, WebMyMetropiaActivity.class);
					Integer pageNo = WebMyMetropiaActivity.hasCo2SavingUrl(PassengerActivity.this) ? WebMyMetropiaActivity.CO2_SAVING_PAGE	: WebMyMetropiaActivity.MY_METROPIA_PAGE;
					intent.putExtra(WebMyMetropiaActivity.WHICH_PAGE, pageNo);
				} else {
					intent = new Intent(PassengerActivity.this, MyMetropiaActivity.class);
					intent.putExtra(MyMetropiaActivity.OPEN_TAB, MyMetropiaActivity.CO2_SAVING_TAB);
				}
				startActivity(intent);
			}
		});

		findViewById(R.id.drive_score_circle).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent;
				if (WebMyMetropiaActivity.hasTimeSavingUrl(PassengerActivity.this)	|| WebMyMetropiaActivity.hasMyMetropiaUrl(PassengerActivity.this)) {
					intent = new Intent(PassengerActivity.this, WebMyMetropiaActivity.class);
					Integer pageNo = WebMyMetropiaActivity.hasTimeSavingUrl(PassengerActivity.this) ? WebMyMetropiaActivity.TIME_SAVING_PAGE : WebMyMetropiaActivity.MY_METROPIA_PAGE;
					intent.putExtra(WebMyMetropiaActivity.WHICH_PAGE, pageNo);
				} else {
					intent = new Intent(PassengerActivity.this, MyMetropiaActivity.class);
					intent.putExtra(MyMetropiaActivity.OPEN_TAB, MyMetropiaActivity.DRIVE_SCORE_TAB);
				}
				startActivity(intent);
			}
		});

		findViewById(R.id.mpoint_circle).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent;
				if (WebMyMetropiaActivity.hasMyMetropiaUrl(PassengerActivity.this)) {
					intent = new Intent(PassengerActivity.this, WebMyMetropiaActivity.class);
					intent.putExtra(WebMyMetropiaActivity.WHICH_PAGE, WebMyMetropiaActivity.MY_METROPIA_PAGE);
				} else {
					intent = new Intent(PassengerActivity.this, MyMetropiaActivity.class);
					intent.putExtra(MyMetropiaActivity.OPEN_TAB, MyMetropiaActivity.CO2_SAVING_TAB);
				}
				startActivity(intent);
			}
		});
		
		Font.setTypeface(Font.getRegular(getAssets()), passengerMsg, startOrEndButton, finishButton, feedBackButton);
		
		// init Tracker
		((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);

		if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(PassengerActivity.this) == ConnectionResult.SUCCESS) {
			requestingLocationUpdates = true;
			createGoogleApiClient();
			createLocationRequest();
			buildLocationSettingsRequest();
		}
	}
	
	private void startTrip() {
		AsyncTask<Void, Void, Long> reservTask = new AsyncTask<Void, Void, Long>() {
			CancelableProgressDialog dialog;
			ExceptionHandlingService es = new ExceptionHandlingService(PassengerActivity.this);
			@Override
			protected void onPreExecute() {
				dialog = new CancelableProgressDialog(PassengerActivity.this, "Make a reservation...");
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
							reservId.set(reserId);
							TextView startOrEndTripButton = (TextView) findViewById(R.id.start_or_end_trip);
							startOrEndTripButton.setTag(true);
							startOrEndTripButton.setText("END MY TRIP");
							startOrEndTripButton.setBackgroundColor(getResources().getColor(R.color.metropia_orange));
							TextView passengerMsg = (TextView) findViewById(R.id.passenger_msg);
							passengerMsg.setText(getResources().getString(R.string.passenger_start_ride));
							findViewById(R.id.back_button).setVisibility(View.GONE);
							prepareGPS();
						}
					});
				}
		    }
		};
		Misc.parallelExecute(reservTask);
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
	
	private void locationChanged(Location location) {
		if(reservId.get() > 0) {
			trajectory.accumulate(location, Trajectory.DEFAULT_LINK_ID);
			if (!arrived.get() && trajectory.size() >= 8) {
				saveTrajectory();
			}
		}
		mapView.reportNewGPSPosition(new SKPosition(location));
		mapView.getMapSettings().setCurrentPositionShown(true);
	}
	
	private AtomicBoolean arrivalMsgDisplayed = new AtomicBoolean();
	private NumberFormat nf = new DecimalFormat("#.#");
	
	private void doDisplayArrivalMsg(int uPoints, double co2Value, String message, double timeSavingInMinute) {
		if (!arrivalMsgDisplayed.get()) {
			arrivalMsgDisplayed.set(true);
			findViewById(R.id.opt_panel).setVisibility(View.GONE);
			findViewById(R.id.loading).setVisibility(View.GONE);
			final View panel = findViewById(R.id.congrats_panel);

			if (StringUtils.isNotBlank(message)) {
				TextView congratsMsg = (TextView) findViewById(R.id.congrats_msg);
				congratsMsg.setText(ValidationActivity.formatCongrMessage(PassengerActivity.this, message));
				congratsMsg.setVisibility(View.VISIBLE);
				findViewById(R.id.congrats_msg_shadow).setVisibility(View.VISIBLE);
			}

			TextView co2 = (TextView) findViewById(R.id.co2_circle);
			if (co2Value != 0) {
				String co2String = nf.format(co2Value) + "lbs\nCO2";
				co2.setText(ValidationActivity.formatCO2Desc(PassengerActivity.this, co2String));
				((ImageView) findViewById(R.id.co2_circle_background)).setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.blue_circle)));
				findViewById(R.id.co2_circle_panel).setVisibility(View.VISIBLE);
			}

			TextView mpoint = (TextView) findViewById(R.id.mpoint_circle);
			if (uPoints > 0) {
				mpoint.setText(ValidationActivity.formatCongrValueDesc(PassengerActivity.this, uPoints + "\nPoints"));
				((ImageView) findViewById(R.id.mpoint_circle_background)).setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.green_circle)));
				findViewById(R.id.mpoint_circle_panel).setVisibility(View.VISIBLE);
			} 

			TextView driveScore = (TextView) findViewById(R.id.drive_score_circle);
			if (timeSavingInMinute > 0) {
				String scoreString = new DecimalFormat("0.#").format(timeSavingInMinute) + "\nminutes";
				driveScore.setText(ValidationActivity.formatCongrValueDesc(PassengerActivity.this, scoreString));
				((ImageView) findViewById(R.id.drive_score_circle_background)).setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.red_circle)));
				findViewById(R.id.drive_score_circle_panel).setVisibility(View.VISIBLE);
			}

			ImageView share = (ImageView) findViewById(R.id.share);
			share.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.trip_share)));

			Font.setTypeface(Font.getRobotoBold(getAssets()), co2, mpoint, driveScore, (TextView) findViewById(R.id.congrats_msg),
					(TextView) findViewById(R.id.close), (TextView) findViewById(R.id.feedback));

			panel.setVisibility(View.VISIBLE);
			Misc.fadeIn(PassengerActivity.this, panel);

			closeGPS();
		}
	}
	
	private int seq = 1;
	
	private void saveTrajectory(final Runnable callback) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					final File tFile = SendTrajectoryService.getInFile(PassengerActivity.this, reservId.get(), seq++);
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
		mapView.getMapSettings().setCurrentPositionShown(false);
		mapView.getMapSettings().setFollowerMode(SKMapFollowerMode.NAVIGATION);
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
				boolean success = intent
						.getBooleanExtra(ValidationActivity.REQUEST_SUCCESS, false);
				if (String.valueOf(reservId.get()).equals(id) && success) {
					String message = intent.getStringExtra(ValidationActivity.MESSAGE);
					double co2Saving = intent.getDoubleExtra(ValidationActivity.CO2_SAVING, 0);
					int credit = intent.getIntExtra(ValidationActivity.CREDIT, 0);
					double timeSavingInMinute = intent.getDoubleExtra(ValidationActivity.TIME_SAVING_IN_MINUTE, 0);
					doDisplayArrivalMsg(credit, co2Saving, message, timeSavingInMinute);
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
			if((Boolean)findViewById(R.id.start_or_end_trip).getTag()) {
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

	@Override
	public void onConnected(Bundle arg0) {}

	@Override
	public void onConnectionSuspended(int arg0) {}

	@Override
	public void onActionPan() {}

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
	public void onMapRegionChanged(SKCoordinateRegion arg0) {}

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
        Location loc = new Location("");
        loc.setLatitude(cacheLoc.lastLat);
        loc.setLongitude(cacheLoc.lastLong);
        loc.setTime(cacheLoc.lastLocationUpdateTimestamp - ValidationActivity.TWO_MINUTES);
        loc.setAccuracy(cacheLoc.lastAccuracy);
        loc.setBearing(cacheLoc.lastHeading);
        locationChanged(loc);
	}

	@Override
	public void onDebugInfo(double arg0, float arg1, double arg2) {}

}
