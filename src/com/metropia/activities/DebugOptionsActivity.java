package com.metropia.activities;


import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.localytics.android.Localytics;
import com.metropia.SendTrajectoryService;
import com.metropia.SkobblerUtils;
import com.metropia.models.Reservation;
import com.metropia.models.ReservationTollHovInfo;
import com.metropia.models.Trajectory;
import com.metropia.models.Trajectory.Record;
import com.metropia.requests.Request;
import com.metropia.requests.Request.Setting;
import com.metropia.requests.ServiceDiscoveryRequest.Result;
import com.metropia.requests.TrajectoryFetchRequest;
import com.metropia.tasks.ShortcutNavigationTask;
import com.metropia.ui.DelayTextWatcher;
import com.metropia.ui.DelayTextWatcher.TextChangeListener;
import com.metropia.ui.NavigationView;
import com.metropia.utils.Cache;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.Geocoding.Address;
import com.metropia.utils.Misc;
import com.metropia.utils.RouteNode;

public final class DebugOptionsActivity extends FragmentActivity {
    
    /**
     * Name of the shared preference file
     */
    public static final String DEBUG_PREFS = "debug_prefs";
    
    public static final String DEBUG_MODE = "DebugMode";
    public static final String GPS_MODE = "GPSMode";
    
    public static final int GPS_MODE_REAL = 1;
    public static final int GPS_MODE_LONG_PRESS = 4;
    public static final int GPS_MODE_DEFAULT = GPS_MODE_REAL;
    
    public static final String GPS_UPDATE_INTERVAL = "GPS_UPDATE_INTERVAL";
    
//    private static final String CURRENT_LOCATION = "CURRENT_LOCATION";
    
    private static final String CURRENT_LOCATION_LAT_LON = "CURRENT_LOCATION_LAT_LON";
    
    private static final String ENTRYPOINT = "ENTRYPOINT";
    
    private static final String PRE_RECORDED_DATA_URL = "PRE_RECORDED_DATA_URL";
    
    public static final String GOOGLE_GEOCODING_PATCHED = "GOOGLE_GEOCODING_PATCHED";
    
    public static final String SKOBBLER_PATCHED = "SKOBBLER_PATCHED";
    
    private static final int GOOGLE_GEOCODING_PATCH_NO = 1;
    
    private static final String LAST_USER_LAT_LON = "LAST_USER_LAT_LON";
    
    private static final String LAST_GPS_TOGGLE_LAT_LON = "LAST_GPS_TOGGLE_LAT_LON";
    
    private static final String LAST_USER_LAT_SENT = "LAST_USER_LAT_SENT";
    
    private static final String EULA_ETAG = "EULA_ETAG";
    
    private static final String fakeRoutes = "fakeRouteIds";
    
    private static final int fakeRouteSize = 10;
    
    private static final String navLinks = "navLinks";
    
    private static final int navLinksSize = 100;
    
    private static final String recentAddresses = "recentAddresses";
    
    private static final int recentAddressesSize = 30;
    
    private static final String terminatedReservIds = "terminatedReservIds";
    
    private static final int terminatedReservIdsSize = 30;
    
    private static final String osmdroidCacheDir = "osmdroid";
    
    public static final int defaultUpdateInterval = 1000;
    
    private static final String REROUTING_NOTIFICATION_SOUND = "REROUTING_NOTIFICATION_SOUND";
    
    private static final String REROUTING_DEBUG_MSG = "REROUTING_DEBUG_MSG";
    
    private static final String VOICE_DEBUG_MSG = "VOICE_DEBUG_MSG";
    
    private static final String GPS_ACCURACY_DEBUG_MSG = "GPS_ACCURACY_DEBUG_MSG";
    
    private static final String NAV_API_LOG = "NAV_API_LOG";
    
    private static final String HTTP_4XX_5XX_LOG = "HTTP_4XX_5XX_LOG";
    
    private static final String POPUP_MESSAGE_MORE = "POPUP_MESSAGE_MORE";
    
    public static final String REPLAY = "replay";
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
    private SharedPreferences prefs;
    
    private RadioButton radioRealGPS;
    private RadioButton radioLongPress;
    
    private Button buttonClearCache;
    private Button buttonCrash;
    
    
    EditText rerouteThresholdCoe;
    EditText rerouteThresholdMax;
    EditText rerouteThresholdMin;
    
    private AsyncTask<Void, Void, Result> initApiLinksTask;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_options);
        
        // Integrate Localytics
     	Localytics.integrate(this);
        
        prefs = getSharedPreferences(DEBUG_PREFS, MODE_PRIVATE);
        
        radioRealGPS = (RadioButton) findViewById(R.id.radio_real_gps);
        radioLongPress = (RadioButton) findViewById(R.id.radio_long_press);
        
        radioRealGPS.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(GPS_MODE, GPS_MODE_REAL);
                editor.commit();
            }
        });
        
        radioLongPress.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(GPS_MODE, GPS_MODE_LONG_PRESS);
                editor.commit();
            }
        });
        
        final EditText preRecordedDataUrlView = (EditText) findViewById(R.id.pre_recorded_data_url);
        preRecordedDataUrlView.setText(String.valueOf(prefs.getString(PRE_RECORDED_DATA_URL, "")));
        preRecordedDataUrlView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                prefs.edit()
                    .putString(PRE_RECORDED_DATA_URL, s.toString())
                    .commit();
            }
        });
        
        Button buttonReplay = (Button) findViewById(R.id.button_replay);
        buttonReplay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Misc.parallelExecute(new AsyncTask<Void, Void, JSONObject>() {
                    @Override
                    protected JSONObject doInBackground(Void... params) {
                        JSONObject rs = null;
                        try {
                            TrajectoryFetchRequest req = new TrajectoryFetchRequest(
                                preRecordedDataUrlView.getText().toString());  
                            req.invalidateCache(DebugOptionsActivity.this);
                            rs = req.execute(DebugOptionsActivity.this);
                        }
                        catch(Exception e) {
                            ehs.registerException(e);
                        }
                        return rs;
                    }
                    @Override
                    protected void onPostExecute(final JSONObject result) {
                        if (ehs.hasExceptions()) {
                            ehs.reportExceptions();
                        }
                        else {
                        	String versionNumber = "";
                        	try {
                    			versionNumber = MapDisplayActivity.OS_NAME + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                    		}catch(NameNotFoundException ignore) {}
                        	
                            try {
                                List<Record> records = Trajectory.from(result.getJSONArray("trajectory")).getRecords();
                                Record origin = records.get(0);
                                Record dest = records.get(records.size() - 1);
                                final ReservationTollHovInfo reservInfo = MapDisplayActivity.getReservationTollHovInfo(DebugOptionsActivity.this, 0L);
                                ShortcutNavigationTask task = new ShortcutNavigationTask(DebugOptionsActivity.this, 
                                    new GeoPoint(origin.getLatitude(), origin.getLongitude()), result.optString("origin"), 
                                    new GeoPoint(dest.getLatitude(), dest.getLongitude()), result.optString("destination"), ehs, versionNumber, reservInfo);
                                task.callback = new ShortcutNavigationTask.Callback() {
                                    @Override
                                    public void run(final Reservation reservation) {
                                    	reservInfo.setReservationId(reservation.getRid());
                                    	MapDisplayActivity.addReservationTollHovInfo(DebugOptionsActivity.this, reservInfo);
                                    	MainActivity.initSettingsIfNecessary(DebugOptionsActivity.this, new Runnable() {
											@Override
											public void run() {
												Intent intent = new Intent(DebugOptionsActivity.this, ValidationActivity.class);
		                                        intent.putExtra(ValidationActivity.TRAJECTORY_DATA, result.toString());
		                                        intent.putExtra("route", reservation.getRoute());
		                                        intent.putExtra("reservation", reservation);
		                                        intent.putExtra(REPLAY, true);
		                                        startActivity(intent);
		                                        finish();
											}
                                    	});
                                    }

									@Override
									public void runOnFail() {
										//do nothing
									}
                                };
                                Misc.parallelExecute(task);
                            }
                            catch (Exception e) {
                                ehs.registerException(e);
                                ehs.reportExceptions();
                            }
                        }
                    }
                });
            }
        });
        
        buttonClearCache = (Button) findViewById(R.id.button_clear_cache);
        buttonCrash = (Button) findViewById(R.id.button_crash);
        
        buttonClearCache.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {
                    
                    ProgressDialog dialog;
                    
                    protected void onPreExecute() {
                        dialog = new ProgressDialog(DebugOptionsActivity.this, R.style.PopUpDialog);
                        dialog.setMessage("Clearing cache...");
                        dialog.setIndeterminate(true);
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    }
                    @Override
                    protected Void doInBackground(Void... params) {
                        Cache.getInstance(DebugOptionsActivity.this).clear();
                        FileUtils.deleteQuietly(new File(
                            Environment.getExternalStorageDirectory(), osmdroidCacheDir));
                        return null;
                    }
                    protected void onPostExecute(Void result) {
                        dialog.cancel();
                        Toast toast = Toast.makeText(
                                DebugOptionsActivity.this,
                                "Cache has been cleared.",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }.execute();
            }

        });
        
        buttonCrash.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ((String) null).length();
            }
            
        });
        
        EditText updateIntervalView = (EditText) findViewById(R.id.update_interval);
        updateIntervalView.setText(String.valueOf(prefs.getInt(GPS_UPDATE_INTERVAL, defaultUpdateInterval)));
        updateIntervalView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    prefs.edit()
                        .putInt(GPS_UPDATE_INTERVAL, Integer.parseInt(s.toString()))
                        .commit();
                }
            }
        });
        
//        EditText curLocView = (EditText) findViewById(R.id.current_location);
//        curLocView.setText(String.valueOf(prefs.getString(CURRENT_LOCATION, "")));
//        curLocView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//            
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count,
//                    int after) {
//            }
//            
//            @Override
//            public void afterTextChanged(Editable s) {
//                prefs.edit()
//                    .putString(CURRENT_LOCATION, s.toString())
//                    .commit();
//            }
//        });
        
        EditText curLocLatLonView = (EditText) findViewById(R.id.current_location_lat_lon);
        curLocLatLonView.setText(String.valueOf(prefs.getString(CURRENT_LOCATION_LAT_LON, "")));
        curLocLatLonView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                prefs.edit()
                    .putString(CURRENT_LOCATION_LAT_LON, s.toString())
                    .commit();
            }
        });
        
        EditText entrypointView = (EditText) findViewById(R.id.entry_point);
        entrypointView.setText(String.valueOf(prefs.getString(ENTRYPOINT, "")));
        
        DelayTextWatcher entrypointTextWatcher = new DelayTextWatcher(entrypointView, new TextChangeListener(){
			@Override
			public void onTextChanged(CharSequence text) {
				String entrypoint = text.toString();
                prefs.edit().putString(ENTRYPOINT, entrypoint).commit();
                if(initApiLinksTask != null){
                    initApiLinksTask.cancel(true);
                }
                initApiLinksTask = MainActivity.initApiLinks(DebugOptionsActivity.this, entrypoint, 
                        new Runnable() {
    						@Override
    						public void run() {
    							Intent updateMenu = new Intent(LandingActivity2.UPDATE_MENU_MY_TRIPS);
    							updateMenu.putExtra("hasTrips", MyTripsActivity.hasUrl(DebugOptionsActivity.this));
    							sendBroadcast(updateMenu);
    						}
                    	
                    }, null);
			}
			
			@Override
			public void onTextChanging() {
			}
		}, 500, null);
        
        entrypointView.addTextChangedListener(entrypointTextWatcher);
        
        SeekBar seekBarTrajectorySendingInterval = (SeekBar) findViewById(R.id.seekBarTrajectorySendingInterval);
        seekBarTrajectorySendingInterval.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				setDebugValue(DebugOptionsActivity.this, TRAJECTORY_SENDING_INTERVAL, progress+1);
				((TextView)findViewById(R.id.seekBarTrajectorySendingIntervalText)).setText("Trajectory Sending Interval: "+(progress+1)+" min");
			}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarTrajectorySendingInterval.setProgress((Integer) getDebugValue(this, TRAJECTORY_SENDING_INTERVAL, 4));
        
        rerouteThresholdCoe = (EditText) findViewById(R.id.reroute_threshod_coe);
        rerouteThresholdMax = (EditText) findViewById(R.id.reroute_threshod_max);
        rerouteThresholdMin = (EditText) findViewById(R.id.reroute_threshod_min);
        
        rerouteThresholdCoe.setText(getDebugValue(this, REROUTE_THRESHOLD_COE, 0.05f).toString());
        rerouteThresholdMax.setText(getDebugValue(this, REROUTE_THRESHOLD_MAX, 5).toString());
        rerouteThresholdMin.setText(getDebugValue(this, REROUTE_THRESHOLD_MIN, 2).toString());
        
        
        
        
        CheckBox arrivalLogicLog = (CheckBox) findViewById(R.id.arrival_logic_log);
        arrivalLogicLog.setChecked(isArrivalLogicLogEnabled(this));
        arrivalLogicLog.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	setArrivalLogicLogEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
        
        EditText arrivalLogicCoefficientA = (EditText)findViewById(R.id.arrival_logic_coefficient_a);
        arrivalLogicCoefficientA.setText(getArrivalLogicCoefficientA(this).toString());
        arrivalLogicCoefficientA.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				setArrivalLogicCoefficientA(DebugOptionsActivity.this, s);
			}

			@Override
			public void afterTextChanged(Editable s) {}
		});
        
        EditText arrivalLogicCoefficientB = (EditText)findViewById(R.id.arrival_logic_coefficient_b);
        arrivalLogicCoefficientB.setText(getArrivalLogicCoefficientB(this).toString());
        arrivalLogicCoefficientB.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				setArrivalLogicCoefficientB(DebugOptionsActivity.this, s);
			}

			@Override
			public void afterTextChanged(Editable s) {}
		});
        
        EditText arrivalLogicCoefficientC = (EditText)findViewById(R.id.arrival_logic_coefficient_c);
        arrivalLogicCoefficientC.setText(getArrivalLogicCoefficientC(this).toString());
        arrivalLogicCoefficientC.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				setArrivalLogicCoefficientC(DebugOptionsActivity.this, s);
			}

			@Override
			public void afterTextChanged(Editable s) {}
		});
        
        CheckBox reroutingNotificationSound = (CheckBox) findViewById(R.id.rerouting_notification_sound);
        reroutingNotificationSound.setChecked(isReroutingNotificationSoundEnabled(this));
        reroutingNotificationSound.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setReroutingNotificationSoundEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
        
        CheckBox reroutingDebugMsg = (CheckBox) findViewById(R.id.rerouting_debug_msg);
        reroutingDebugMsg.setChecked(isReroutingDebugMsgEnabled(this));
        reroutingDebugMsg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setReroutingDebugMsgEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
        
        CheckBox voiceDebugMsg = (CheckBox) findViewById(R.id.voice_debug_msg);
        voiceDebugMsg.setChecked(isVoiceDebugMsgEnabled(this));
        voiceDebugMsg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setVoiceDebugMsgEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
        
        CheckBox gpsAccuracyDebugMsg = (CheckBox) findViewById(R.id.gps_accuracy_debug_msg);
        gpsAccuracyDebugMsg.setChecked(isGpsAccuracyDebugMsgEnabled(this));
        gpsAccuracyDebugMsg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setGpsAccuracyDebugMsgEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
        
        CheckBox navAPILog = (CheckBox) findViewById(R.id.nav_api_log);
        navAPILog.setChecked(isNavApiLogEnabled(this));
        navAPILog.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setNavApiLogEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
        
        CheckBox http4xx5xxLog = (CheckBox) findViewById(R.id.http_4xx_5xx_log);
        http4xx5xxLog.setChecked(isHttp4xx5xxLogEnabled(this));
        http4xx5xxLog.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setHttp4xx5xxLogEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
        
        CheckBox popupMessageMore = (CheckBox) findViewById(R.id.popup_message_more);
        popupMessageMore.setChecked(isPopupMessageMoreEnabled(this));
        popupMessageMore.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setPopupMessageMoreEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
        
        CheckBox enRouteDebugMsg = (CheckBox) findViewById(R.id.en_route_debug_msg);
        enRouteDebugMsg.setChecked(isEnrouteDebugMsgEnabled(this));
        enRouteDebugMsg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setEnrouteDebugMsgEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
        
        CheckBox polylineRoute = (CheckBox) findViewById(R.id.polyline_route);
        polylineRoute.setChecked(isPolylineRouteEnabled(this));
        polylineRoute.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setPolylineRouteEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
        
        CheckBox navigationHeadingInfo = (CheckBox) findViewById(R.id.navigation_heading_info);
        navigationHeadingInfo.setChecked(isNavigationHeadingInfoEnabled(this));
        navigationHeadingInfo.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setNavigationHeadingInfoEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
        
        CheckBox voiceInputDebugMsg = (CheckBox) findViewById(R.id.voice_input_debug_msg);
        voiceInputDebugMsg.setChecked(isEnrouteVoiceInputDebugMsgEnabled(this));
        voiceInputDebugMsg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setEnrouteVoiceInputDebugMsgEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
        
        findViewById(R.id.voice_command).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.count_down_panel).setVisibility(View.VISIBLE);
				findViewById(R.id.recognize_result_panel).setVisibility(View.VISIBLE);
				countDown = new CountDownTimer(20000, 1000) {
	    	    	public void onTick(long millisUntilFinished) {
	    	    		((TextView)findViewById(R.id.count_down)).setText(nf.format(millisUntilFinished / 1000));
	   		     	}

	   		     	public void onFinish() {
	   		     		findViewById(R.id.count_down_panel).setVisibility(View.GONE);
	   		     		findViewById(R.id.recognize_result_panel).setVisibility(View.GONE);
	   		     	}
	   		 	};
	   		 	countDown.start();
	   		 	((TextView)findViewById(R.id.recognized)).setText("");
			}
        });
    }
    
	private CountDownTimer countDown;
    
    @Override
    protected void onResume() {
        super.onResume();
        
        Localytics.openSession();
        Localytics.tagScreen(this.getClass().getSimpleName());
        Localytics.upload();

        if (DebugOptionsActivity.this instanceof FragmentActivity) {
            Localytics.setInAppMessageDisplayActivity((FragmentActivity) DebugOptionsActivity.this);
        }

        Localytics.handleTestMode(getIntent());
        Localytics.handlePushNotificationOpened(getIntent());
        
        int gpsMode = prefs.getInt(GPS_MODE, GPS_MODE_DEFAULT);
        
        switch (gpsMode) {
        case GPS_MODE_REAL:
            radioRealGPS.setChecked(true);
            break;
            
        case GPS_MODE_LONG_PRESS:
            radioLongPress.setChecked(true);
            break;
            
        default:
            radioRealGPS.setChecked(true);
            Log.e("DebugOptionsActivity", "Should not reach here.");
        }
    }
    
    public void onPause() {
        if (DebugOptionsActivity.this instanceof FragmentActivity) {
            Localytics.dismissCurrentInAppMessage();
            Localytics.clearInAppMessageDisplayActivity();
        }
        Localytics.closeSession();
        Localytics.upload();
        super.onPause();
    }
    
    private static SharedPreferences getPrefs(Context ctx){
        return ctx.getSharedPreferences(DebugOptionsActivity.DEBUG_PREFS, 
                MODE_PRIVATE);
    }
    
    public static int getGpsUpdateInterval(Context ctx){
        return getPrefs(ctx).getInt(GPS_UPDATE_INTERVAL, defaultUpdateInterval);
    }
    
    public static void setActivityDistanceInterval(Context ctx, long val){
        getPrefs(ctx).edit()
            .putLong(Setting.activity_distance_interval.name(), val)
            .commit();
    }
    
    public static long getActivityDistanceInterval(Context ctx){
        return getPrefs(ctx).getLong(Setting.activity_distance_interval.name(), 100);
    }
    
//    public static String getCurrentLocation(Context ctx){
//        return getPrefs(ctx).getString(CURRENT_LOCATION, "");
//    }
    
    public static GeoPoint getCurrentLocationLatLon(Context ctx){
    	String latLon = getPrefs(ctx).getString(CURRENT_LOCATION_LAT_LON, "");
    	GeoPoint curLoc = null;
    	if(StringUtils.isNotBlank(latLon) && StringUtils.split(latLon, ",").length > 1) {
    		String[] latLonArray = StringUtils.split(latLon, ",");
    		curLoc = new GeoPoint(Double.valueOf(StringUtils.trim(latLonArray[0])), Double.valueOf(StringUtils.trim(latLonArray[1])));
    	}
        return curLoc;
    }
    
    public static String getDebugEntrypoint(Context ctx){
        return getPrefs(ctx).getString(ENTRYPOINT, "");
    }
    
    public static boolean isGoogleGeocodingPatched(Context ctx){
        boolean patched;
        try{
            patched = getPrefs(ctx).getInt(GOOGLE_GEOCODING_PATCHED, 0) == GOOGLE_GEOCODING_PATCH_NO;
        }catch(Throwable t){
            patched = false;
        }
        return patched;
    }
    
    public static void setGoogleGeocodingPatched(Context ctx, boolean patched){
        getPrefs(ctx).edit()
            .putInt(GOOGLE_GEOCODING_PATCHED, patched?GOOGLE_GEOCODING_PATCH_NO:0)
            .commit();
    }
    
    public static void setLastUserLatLonSent(Context ctx,long time){
        getPrefs(ctx).edit()
            .putLong(LAST_USER_LAT_SENT, time)
            .commit();
    }
    
    public static long getLastUserLatLonSent(Context ctx){
        return getPrefs(ctx).getLong(LAST_USER_LAT_SENT, 0);
    }
    
    public static void setEulaEtag(Context ctx, String eTag){
        getPrefs(ctx).edit()
            .putString(EULA_ETAG, eTag)
            .commit();
    }
    
    public static String getEulaEtag(Context ctx){
        return getPrefs(ctx).getString(EULA_ETAG, "");
    }
    
    public static LatLon getLastUserLatLon(Context ctx){
        LatLon rs = null;
        try{
            String latLonStr = getPrefs(ctx).getString(LAST_USER_LAT_LON, "");
            if(StringUtils.isNotBlank(latLonStr)){
                LatLon latLon = new LatLon();
                String[] toks = latLonStr.split(",");
                latLon.lat = Float.parseFloat(toks[0]);
                latLon.lon = Float.parseFloat(toks[1]);
                if(toks.length > 2){
                    latLon.time = Long.parseLong(toks[2]);
                }
                rs = latLon;
            }
        }catch(Throwable t){
        }
        return rs;
    }
    
    public static void setLastUserLatLon(Context ctx, float lat, float lon, long time){
        getPrefs(ctx).edit()
            .putString(LAST_USER_LAT_LON, lat + "," + lon + "," + time)
            .commit();
    }
    
    public static LatLon getLastGpsToggleLatLon(Context ctx){
        LatLon rs = null;
        try{
            String latLonStr = getPrefs(ctx).getString(LAST_GPS_TOGGLE_LAT_LON, "");
            if(StringUtils.isNotBlank(latLonStr)){
                LatLon latLon = new LatLon();
                String[] toks = latLonStr.split(",");
                latLon.lat = Float.parseFloat(toks[0]);
                latLon.lon = Float.parseFloat(toks[1]);
                if(toks.length > 2){
                    latLon.time = Long.parseLong(toks[2]);
                }
                rs = latLon;
            }
        }catch(Throwable t){
        }
        return rs;
    }
    
    public static void setLastGpsToggleLatLon(Context ctx, float lat, float lon, long time){
        getPrefs(ctx).edit()
            .putString(LAST_GPS_TOGGLE_LAT_LON, lat + "," + lon + "," + time)
            .commit();
    }
    
    public static class LatLon {
        
        public float lat;
        
        public float lon;
        
        public long time;
        
    }
    
    private static JSONArray getFakeRoutes(Context ctx){
        JSONArray ids = null;
        try {
            ids = new JSONArray(getPrefs(ctx).getString(fakeRoutes, "[]"));
        }
        catch (JSONException e) {
            ids = new JSONArray();
        }
        return ids;
    }
    
    private static void saveFakeRoutes(Context ctx, JSONArray routes){
        SharedPreferences.Editor editor = getPrefs(ctx).edit();
        editor.putString(fakeRoutes, routes.toString());
        editor.commit();
    }
    
    public static void addFakeRoute(Context ctx, FakeRoute route){
        JSONArray newRoutes;
        JSONArray oldRoutes = getFakeRoutes(ctx);
        if(oldRoutes.length() > fakeRouteSize - 1){
            newRoutes = new JSONArray();
            for(int i=oldRoutes.length() - fakeRouteSize + 1; i<oldRoutes.length(); i++){
                newRoutes.put(oldRoutes.optString(i));
            }
        }else{
            newRoutes = oldRoutes;
        }
        saveFakeRoutes(ctx, newRoutes.put(route.toString()));
    }
    
    public static FakeRoute getFakeRoute(Context ctx, long id){
        FakeRoute route = null;
        JSONArray routes = getFakeRoutes(ctx);
        for(int i=0; i<routes.length(); i++){
            FakeRoute r = FakeRoute.fromString(routes.optString(i));
            if(r.id == id){
                route = r;
                break;
            }
        }
        return route;
    }
    
    public static class FakeRoute {
        
        long id;
        
        int seq;
        
        @Override
        public String toString() {
            return id + "," + seq;
        }
        
        public static FakeRoute fromString(String val){
            String[] vals = val.split(",");
            FakeRoute r = new FakeRoute();
            r.id = Long.parseLong(vals[0]);
            r.seq = Integer.parseInt(vals[1]);
            return r;
        }
        
    }
    
    private static SortedMap<Long, NavigationLink> getNavLinks(Context ctx){
        SortedMap<Long, NavigationLink> rs = new TreeMap<Long, NavigationLink>();
        JSONArray array = null;
        try {
            array = new JSONArray(getPrefs(ctx).getString(navLinks, "[]"));
        }
        catch (Throwable t) {
            array = new JSONArray();
        }
        for (int i=0; i < array.length(); i++) {
            JSONObject json = array.optJSONObject(i);
            if(json != null){
                NavigationLink l = NavigationLink.fromJSON(json);
                rs.put(l.id, l);
            }
        }
        return rs;
    }
    
    private static void saveNavLinks(Context ctx, Map<Long, NavigationLink> links){
        JSONArray array = new JSONArray();
        for(NavigationLink l:links.values()){
            try {
                array.put(l.toJSON());
            }
            catch (JSONException e) { }
        }
        SharedPreferences.Editor editor = getPrefs(ctx).edit();
        editor.putString(navLinks, array.toString());
        editor.commit();
    }
    
    public static void addNavLink(Context ctx, NavigationLink l){
        SortedMap<Long, NavigationLink> links = getNavLinks(ctx);
        while(links.size() > navLinksSize - 1){
            links.remove(links.firstKey());
        }
        links.put(l.id, l);
        saveNavLinks(ctx, links);
    }
    
    public static NavigationLink getNavLink(Context ctx, long id){
        return getNavLinks(ctx).get(id);
    }
    
    public static class NavigationLink {
        
        private static String ID = "ID";
        
        private static String URL = "URL";
        
        public long id;
        
        public String url;
        
        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put(ID, id);
            json.put(URL, url);
            return json;
        }
        
        public static NavigationLink fromJSON(JSONObject json){
            NavigationLink l = new NavigationLink();
            l.id = json.optLong(ID);
            l.url = json.optString(URL);
            return l;
        }
        
    }
    
    private static void saveRecentAddresses(Context ctx, List<String> addrs){
        JSONArray array = new JSONArray();
        for(String a : addrs){
            array.put(a);
        }
        SharedPreferences.Editor editor = getPrefs(ctx).edit();
        editor.putString(recentAddresses, array.toString());
        editor.commit();
    }
    
    public static void addRecentAddress(Context ctx, String addr){
        List<String> list = getRecentAddresses(ctx);
        while(list.size() > recentAddressesSize - 1){
            list.remove(list.size() - 1);
        }
        list.add(0, addr);
        saveRecentAddresses(ctx, list);
    }
    
    public static List<String> getRecentAddresses(Context ctx){
        List<String> list = new ArrayList<String>();
        JSONArray array = null;
        try {
            array = new JSONArray(getPrefs(ctx).getString(recentAddresses, "[]"));
        }
        catch (Throwable t) {
            array = new JSONArray();
        }
        for (int i=0; i < array.length(); i++) {
            String addr = array.optString(i);
            if(StringUtils.isNotBlank(addr)){
                list.add(addr);
            }
        }
        return list;
    }
    
    private static void saveTerminatedReservIds(Context ctx, List<Long> ids){
        JSONArray array = new JSONArray();
        for(Long id : ids){
            array.put(id.longValue());
        }
        SharedPreferences.Editor editor = getPrefs(ctx).edit();
        editor.putString(terminatedReservIds, array.toString());
        editor.commit();
    }
    
    public static void addTerminatedReservIds(Context ctx, Long id){
        List<Long> list = getTerminatedReservIds(ctx);
        list.remove(id);
        while(list.size() > terminatedReservIdsSize - 1){
            list.remove(list.size() - 1);
        }
        list.add(0, id);
        saveTerminatedReservIds(ctx, list);
    }
    
    public static List<Long> getTerminatedReservIds(Context ctx){
        List<Long> list = new ArrayList<Long>();
        JSONArray array = null;
        try {
            array = new JSONArray(getPrefs(ctx).getString(terminatedReservIds, "[]"));
        }
        catch (Throwable t) {
            array = new JSONArray();
        }
        for (int i=0; i < array.length(); i++) {
            long id = array.optLong(i, 0);
            if(id != 0){
                list.add(id);
            }
        }
        return list;
    }
    
    public static void removeTerminatedReservIds(Context ctx, Long id) {
    	List<Long> list = getTerminatedReservIds(ctx);
        list.remove(id);
        while(list.size() > terminatedReservIdsSize - 1){
            list.remove(list.size() - 1);
        }
        saveTerminatedReservIds(ctx, list);
    }
    
    public static boolean isReroutingNotificationSoundEnabled(Context ctx){
        boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(REROUTING_NOTIFICATION_SOUND, false);
        }catch(Throwable t){
            enabled = false;
        }
        return enabled;
    }
    
    public static void setReroutingNotificationSoundEnabled(Context ctx, boolean enabled){
        getPrefs(ctx).edit()
            .putBoolean(REROUTING_NOTIFICATION_SOUND, enabled)
            .commit();
    }
    
    public static boolean isReroutingDebugMsgEnabled(Context ctx){
        boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(REROUTING_DEBUG_MSG, false);
        }catch(Throwable t){
            enabled = false;
        }
        return enabled;
    }
    
    public static void setReroutingDebugMsgEnabled(Context ctx, boolean enabled){
        getPrefs(ctx).edit()
            .putBoolean(REROUTING_DEBUG_MSG, enabled)
            .commit();
    }
    
    public static boolean isVoiceDebugMsgEnabled(Context ctx){
        boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(VOICE_DEBUG_MSG, false);
        }catch(Throwable t){
            enabled = false;
        }
        return enabled;
    }
    
    public static void setVoiceDebugMsgEnabled(Context ctx, boolean enabled){
        getPrefs(ctx).edit()
            .putBoolean(VOICE_DEBUG_MSG, enabled)
            .commit();
    }
    
    public static boolean isNavApiLogEnabled(Context ctx){
        boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(NAV_API_LOG, false);
        }catch(Throwable t){
            enabled = false;
        }
        return enabled;
    }
    
    public static void setNavApiLogEnabled(Context ctx, boolean enabled){
        getPrefs(ctx).edit()
            .putBoolean(NAV_API_LOG, enabled)
            .commit();
    }
    
    public static boolean isHttp4xx5xxLogEnabled(Context ctx){
        boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(HTTP_4XX_5XX_LOG, false);
        }catch(Throwable t){
            enabled = false;
        }
        return enabled;
    }
    
    public static void setHttp4xx5xxLogEnabled(Context ctx, boolean enabled){
        getPrefs(ctx).edit()
            .putBoolean(HTTP_4XX_5XX_LOG, enabled)
            .commit();
    }
    
    public static boolean isGpsAccuracyDebugMsgEnabled(Context ctx){
        boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(GPS_ACCURACY_DEBUG_MSG, false);
        }catch(Throwable t){
            enabled = false;
        }
        return enabled;
    }
    
    public static void setGpsAccuracyDebugMsgEnabled(Context ctx, boolean enabled){
        getPrefs(ctx).edit()
            .putBoolean(GPS_ACCURACY_DEBUG_MSG, enabled)
            .commit();
    }
    
    private static final String recipientsInfo = "recipientsInfo";
    
    public static void addRecipientsOfReserv(Context ctx, Long reservId, String emails, String phones) {
    	Map<Long, JSONObject> infos = getAllReservRecipients(ctx);
    	if(infos.containsKey(reservId)) {
    		infos.remove(reservId);
    	}
    	try {
	    	JSONObject object = new JSONObject();
	    	object.put("id", reservId);
	    	object.put("emails", emails);
	    	object.put("phones", phones);
	    	infos.put(reservId, object);
    	}
    	catch(JSONException ignore) {}
    	
    	saveReservRecipients(ctx, infos);
    	
    }
    
    public static JSONObject getReservRecipients(Context ctx, Long reservId) {
    	Map<Long, JSONObject> infos = getAllReservRecipients(ctx);
    	JSONObject jsonObject = new JSONObject();
    	if(infos.containsKey(reservId)) {
    		jsonObject = infos.get(reservId);
    	}
    	return jsonObject;
    }
    
    public static JSONObject getReservRecipientsAndRemove(Context ctx, Long reservId) {
    	Map<Long, JSONObject> infos = getAllReservRecipients(ctx);
    	JSONObject jsonObject = new JSONObject();
    	if(infos.containsKey(reservId)) {
    		jsonObject = infos.get(reservId);
    		infos.remove(reservId);
    		saveReservRecipients(ctx, infos);
    	}
    	return jsonObject;
    }
    
    private static Map<Long, JSONObject> getAllReservRecipients(Context ctx) {
    	Map<Long, JSONObject> reservIdObjectMap = new TreeMap<Long, JSONObject>();
        JSONArray array = null;
        try {
            array = new JSONArray(getPrefs(ctx).getString(recipientsInfo, "[]"));
        }
        catch (Throwable t) {
            array = new JSONArray();
        }
        
        for (int i=0; i < array.length(); i++) {
            try {
				JSONObject recipientInfo = array.getJSONObject(i);
				Long reservId = recipientInfo.optLong("id", 0);
				if(reservId > 0) {
					reservIdObjectMap.put(reservId, recipientInfo);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
        return reservIdObjectMap;
    }
    
    public static void saveReservRecipients(Context ctx, Map<Long, JSONObject> infos) {
    	JSONArray array = new JSONArray();
    	Set<Long> keySet = infos.keySet();
    	for(Long id : keySet) {
    		array.put(infos.get(id));
    	}
    	SharedPreferences.Editor editor = getPrefs(ctx).edit();
        editor.putString(recipientsInfo, array.toString());
        editor.commit();
    }
    
    public static void removeReservRecipients(Context ctx, Long reservId) {
    	Map<Long, JSONObject> infos = getAllReservRecipients(ctx);
    	Set<Long> remove = new HashSet<Long>();
    	Set<Long> reservIds = infos.keySet();
    	
    	for(Long id : reservIds) {
    		if(id <= reservId) {
    			remove.add(id);
    		}
    	}
    	
    	// remove recipient records before the reservId
    	for(Long removeId : remove) {
    		infos.remove(removeId);
    	}
    	
    	saveReservRecipients(ctx, infos);
    }
    
    public static boolean isSkobblerPatched(Context ctx){
        boolean patched;
        try{
            patched = getPrefs(ctx).getString(SKOBBLER_PATCHED, "").equals(SkobblerUtils.SDK_VERSION);
        }catch(Throwable t){
            patched = false;
        }
        return patched;
    }
    
    public static void setSkobblerPatched(Context ctx, boolean patched){
        getPrefs(ctx).edit()
            .putString(SKOBBLER_PATCHED, patched?SkobblerUtils.SDK_VERSION:"")
            .commit();
    }
    
    private static final String MAP_TILES_PATCHED = "MAP_TILES_PATCHED";
    
    public static void cleanMapTileCacheIfNessary(final Context ctx){
        boolean patched;
        try{
            patched = getPrefs(ctx).getString(MAP_TILES_PATCHED, "").equals(Request.getSetting(Setting.tile));
        }catch(Throwable t){
            patched = false;
        }
        
        if(!patched) {
        	AsyncTask<Void, Void, Void> cleanTask = new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					FileUtils.deleteQuietly(new File(Environment.getExternalStorageDirectory(), osmdroidCacheDir));
					getPrefs(ctx).edit().putString(MAP_TILES_PATCHED, (String)Request.getSetting(Setting.tile)).commit();
					return null;
				}
        	};
        	Misc.parallelExecute(cleanTask);
        }
    }
    
    private static final String INPUT_ADDRESSES = "INPUT_ADDRESS";
    private static final String INPUT_TIME = "inputTime";
    private static final NumberFormat nf = new DecimalFormat("#.#");
    
    public static Comparator<Address> distanceComparator = new Comparator<Address>() {
		@Override
		public int compare(Address lhs, Address rhs) {
			return Double.valueOf(lhs.getDistance()).compareTo(Double.valueOf(rhs.getDistance()));
		}
    };
    
    public static Comparator<Address> inputTimeComparator = new Comparator<Address>() {
    	@Override
		public int compare(Address lhs, Address rhs) {
			return Double.valueOf(rhs.getInputTime()).compareTo(Double.valueOf(lhs.getInputTime()));
		}
    };
    
    public static void addInputAddress(Context ctx, Address address, String cityName) {
    	try {
	    	LinkedList<Address> currentInputAddresses = getInputAddress(ctx, null, cityName, inputTimeComparator);
	    	// get existed Address
	    	int existedIndex = -1;
	    	for(int i = 0 ; i < currentInputAddresses.size() ; i++) {
	    		Address existedAddr = currentInputAddresses.get(i);
	    		if(existedAddr.getLatitude() == address.getLatitude() && 
	    				existedAddr.getLongitude() == address.getLongitude() && 
	    				StringUtils.equalsIgnoreCase(existedAddr.getAddress(), address.getAddress())) {
	    			existedIndex = i;
	    		}
	    	}
	    	// remove old address and add new address to the first
	    	if(existedIndex >= 0) {
	    		currentInputAddresses.remove(existedIndex);
	    	}
	    	address.setInputTime(System.currentTimeMillis());
	    	currentInputAddresses.addFirst(address);
	    	
	    	int endIndex = currentInputAddresses.size() > 9 ? 10 : currentInputAddresses.size();
	    	JSONArray array = new JSONArray();
	    	for(int j = 0 ; j < endIndex ; j++) {
	    		Address curAddr = currentInputAddresses.get(j);
	    		JSONObject obj = new JSONObject();
	    		obj.put("address", curAddr.getAddress());
	    		obj.put("iconName", curAddr.getIconName());
	    		obj.put("lat", curAddr.getLatitude());
	    		obj.put("lon", curAddr.getLongitude());
	    		obj.put("name", curAddr.getName());
	    		obj.put(INPUT_TIME, curAddr.getInputTime());
	    		array.put(obj);
	    	}
	    	
	    	updateInputAddress(ctx, array, cityName);
    	}
    	catch(JSONException ignore) {}
    }
    
    private static void updateInputAddress(Context ctx, JSONArray array, String cityName) {
    	try {
	    	JSONObject allAddress = null;
	    	try {
	    		allAddress = new JSONObject(getPrefs(ctx).getString(INPUT_ADDRESSES, ""));
	    	}
	    	catch(Throwable t) {
	    		allAddress = new JSONObject();
	    	}
	    	
	    	allAddress.remove(cityName);
	    	allAddress.put(cityName, array);
	    	
	    	SharedPreferences.Editor editor = getPrefs(ctx).edit();
	        editor.putString(INPUT_ADDRESSES, allAddress.toString());
	        editor.commit();
    	}
    	catch(JSONException ignore) {}
    }
    
    public synchronized static LinkedList<Address> getInputAddress(Context ctx, Location userLoc, String cityName, Comparator<Address> sortBy) {
    	LinkedList<Address> inputAddresses = new LinkedList<Address>();
    	try {
	        JSONObject allAddress = null;
	        try {
	            allAddress = new JSONObject(getPrefs(ctx).getString(INPUT_ADDRESSES, ""));
	        }
	        catch (Throwable t) {
	            allAddress = new JSONObject();
	        }
	        
	        JSONArray cityAddresses = allAddress.getJSONArray(cityName);
	        
	        for(int i = 0 ; i < cityAddresses.length() ; i++) {
	        	JSONObject addrObj = cityAddresses.getJSONObject(i);
	        	Double distance = Double.valueOf(-1);
	        	if(userLoc != null) {
	        		distance = NavigationView.metersToMiles(
		    				RouteNode.distanceBetween(userLoc.getLatitude(), userLoc.getLongitude(), addrObj.optDouble("lat", 0), addrObj.optDouble("lon", 0)));
	        	}
	        	
	        	Address addr = new Address();
	        	addr.setAddress(addrObj.optString("address", ""));
	        	addr.setDistance(Double.valueOf(nf.format(distance)));
	        	addr.setIconName(addrObj.optString("iconName", ""));
	        	addr.setLatitude(addrObj.optDouble("lat", 0));
	        	addr.setLongitude(addrObj.optDouble("lon", 0));
	        	addr.setName(addrObj.optString("name", ""));
	        	addr.setInputTime(addrObj.optLong(INPUT_TIME, 0));
	        	inputAddresses.add(addr);
	        }
	        
	        Collections.sort(inputAddresses, sortBy);
    	}
    	catch(JSONException ignore) {}
        
        return inputAddresses;
    }
    
    private static final String NEW_USER_TIP_CLOSE = "newUserTipClose";
    
    public static boolean isUserCloseTip(Context ctx) {
    	boolean closed;
        try{
            closed = getPrefs(ctx).getBoolean(NEW_USER_TIP_CLOSE, false);
        }catch(Throwable t){
            closed = false;
        }
        return closed;
    }
    
    public static void userCloseTip(Context ctx) {
    	getPrefs(ctx).edit().putBoolean(NEW_USER_TIP_CLOSE, true).commit();
    }
    
    public static boolean isPopupMessageMoreEnabled(Context ctx) {
    	boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(POPUP_MESSAGE_MORE, false);
        }catch(Throwable t){
            enabled = false;
        }
        return enabled;
    }
    
    public static void setPopupMessageMoreEnabled(Context ctx, boolean enabled){
        getPrefs(ctx).edit()
            .putBoolean(POPUP_MESSAGE_MORE, enabled)
            .commit();
    }
    
    public static boolean isIncidentEnabled(Context ctx) {
//    	return "http://sandbox.metropia.com/v1/rest/index.json".equalsIgnoreCase(getDebugEntrypoint(ctx));
    	return true;
    }
    
    private static final String EN_ROUTE_DEBUG_MSG = "EN_ROUTE_DEBUG_MSG";
    
    public static boolean isEnrouteDebugMsgEnabled(Context ctx){
        boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(EN_ROUTE_DEBUG_MSG, false);
        }catch(Throwable t){
            enabled = false;
        }
        return enabled;
    }
    
    public static void setEnrouteDebugMsgEnabled(Context ctx, boolean enabled){
        getPrefs(ctx).edit()
            .putBoolean(EN_ROUTE_DEBUG_MSG, enabled)
            .commit();
    }
    
    private static final String POLYLINE_ROUTE = "POLYLINE_ROUTE";
    
    public static boolean isPolylineRouteEnabled(Context ctx){
        boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(POLYLINE_ROUTE, true);
        }catch(Throwable t){
            enabled = true;
        }
        return enabled;
    }
    
    public static void setPolylineRouteEnabled(Context ctx, boolean enabled){
        getPrefs(ctx).edit()
            .putBoolean(POLYLINE_ROUTE, enabled)
            .commit();
    }
    
    private static final String NAV_HEADING_INFO = "NAVIGATION_HEADING_INFO";
    
    public static boolean isNavigationHeadingInfoEnabled(Context ctx){
        boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(NAV_HEADING_INFO, false);
        }catch(Throwable t){
            enabled = false;
        }
        return enabled;
    }
    
    public static void setNavigationHeadingInfoEnabled(Context ctx, boolean enabled){
        getPrefs(ctx).edit()
            .putBoolean(NAV_HEADING_INFO, enabled)
            .commit();
    }
    
    private static final String EN_ROUTE_VOICE_INPUT_DEBUG_MSG = "EN_ROUTE_VOICE_INPUT_DEBUG_MSG";
    
    public static boolean isEnrouteVoiceInputDebugMsgEnabled(Context ctx){
        boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(EN_ROUTE_VOICE_INPUT_DEBUG_MSG, false);
        }catch(Throwable t){
            enabled = false;
        }
        return enabled;
    }
    
    public static void setEnrouteVoiceInputDebugMsgEnabled(Context ctx, boolean enabled){
        getPrefs(ctx).edit()
            .putBoolean(EN_ROUTE_VOICE_INPUT_DEBUG_MSG, enabled)
            .commit();
    }
    
    private static final String PREDICTIVE_DESTINATION_TUTORIAL = "PREDICTIVE_DESTINATION_TUTORIAL";
    
    public static boolean isPredictiveDestinationTutorialShown(Context ctx) {
    	boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(PREDICTIVE_DESTINATION_TUTORIAL, false);
        }catch(Throwable t){
            enabled = false;
        }
        return enabled;
    }
    
    public static void setPredictiveDestinationTutorialShown(Context ctx){
        getPrefs(ctx).edit()
            .putBoolean(PREDICTIVE_DESTINATION_TUTORIAL, true)
            .commit();
    }
    
    private static final String INBOX_LAST_VISIT_FEED = "INBOX_LAST_VISIT_FEED";
    
    public static Long getInboxLastVisitFeedTime(Context ctx, String cityName) {
    	Long lastFeed = Long.valueOf(0);
        try{
        	JSONObject record = getInboxLastVisitFeedTimeRecord(ctx);
        	lastFeed = record.optLong(cityName, 0);
        }catch(Throwable ignore){}
        return lastFeed;
    }
    
    public static void setInboxLastVisitFeedTime(Context ctx, String cityName, Long lastFeed){
    	JSONObject feedTimed = getInboxLastVisitFeedTimeRecord(ctx);
    	try {
			feedTimed.put(cityName, lastFeed);
		} catch (JSONException ignore) {}
        getPrefs(ctx).edit().putString(INBOX_LAST_VISIT_FEED, feedTimed.toString()).commit();
    }
    
    private static JSONObject getInboxLastVisitFeedTimeRecord(Context ctx) {
    	String existedRecord = null;
    	try {
    		existedRecord = getPrefs(ctx).getString(INBOX_LAST_VISIT_FEED, "");
    	}
    	catch(Exception ignore){}
    	JSONObject feedTimed;
    	if(StringUtils.isBlank(existedRecord)) {
    		feedTimed = new JSONObject();
    	}
    	else {
    		try {
				feedTimed = new JSONObject(existedRecord);
			} catch (JSONException e) {
				feedTimed = new JSONObject();
			}
    	}
    	return feedTimed;
    }
    
    private static final String MENU_NOTIFICATION_DISMISS_TIME = "MENU_NOTIFICATION_DISMISS_TIME";
    
    public enum NotificationType {
    	inbox;
    }
    
    public static Long getInboxMenuDismissRecord(Context ctx, String cityName) {
    	JSONObject allRecord = getMenuDismissRecord(ctx);
    	return allRecord.optLong(NotificationType.inbox.name() + "_" + cityName, -1);
    }
    
    public static void setInboxMenuDismissRecord(Context ctx, String cityName, Long record) {
    	JSONObject allRecord = getMenuDismissRecord(ctx);
    	try {
			allRecord.put(NotificationType.inbox.name() + "_" + cityName, record);
		} catch (JSONException ignore) {}
    	getPrefs(ctx).edit().putString(MENU_NOTIFICATION_DISMISS_TIME, allRecord.toString()).commit();
    }
    
    private static JSONObject getMenuDismissRecord(Context ctx) {
    	String existedRecord = null;
    	try {
    		existedRecord = getPrefs(ctx).getString(MENU_NOTIFICATION_DISMISS_TIME, "");
    	}
    	catch(Exception ignore){}
    	JSONObject feedTimed;
    	if(StringUtils.isBlank(existedRecord)) {
    		feedTimed = new JSONObject();
    	}
    	else {
    		try {
				feedTimed = new JSONObject(existedRecord);
			} catch (JSONException e) {
				feedTimed = new JSONObject();
			}
    	}
    	return feedTimed;
    }
    
    private static final String ARRIVAL_LOGIC_LOG = "ARRIVAL_LOGIC_LOG";
    private static final String ARRIVAL_LOGIC_COEFFICIENT_A = "ARRIVAL_LOGIC_COEFFICIENT_A";
    private static final String ARRIVAL_LOGIC_COEFFICIENT_B = "ARRIVAL_LOGIC_COEFFICIENT_B";
    private static final String ARRIVAL_LOGIC_COEFFICIENT_C = "ARRIVAL_LOGIC_COEFFICIENT_C";
    
    public static boolean isArrivalLogicLogEnabled(Context ctx) {
    	boolean enabled;
    	try{
    		enabled = getPrefs(ctx).getBoolean(ARRIVAL_LOGIC_LOG, false);
    	}catch(Throwable t){
    		enabled = false;
    	}
    	return enabled;
    }
    public static void setArrivalLogicLogEnabled(Context ctx, boolean enabled) {
    	getPrefs(ctx).edit()
        .putBoolean(ARRIVAL_LOGIC_LOG, enabled)
        .commit();
    }
    
    public static Float getArrivalLogicCoefficientA(Context ctx) {
    	Float coefficientA = Float.valueOf(-0.0000005f);
    	try {
    		coefficientA = getPrefs(ctx).getFloat(ARRIVAL_LOGIC_COEFFICIENT_A, coefficientA);
    	}
    	catch(Exception ignore){}
    	return coefficientA;
    }
    
    private static void setArrivalLogicCoefficientA(Context ctx, CharSequence coefficientA) {
    	try {
    		getPrefs(ctx).edit().putFloat(ARRIVAL_LOGIC_COEFFICIENT_A, Float.valueOf(coefficientA.toString())).commit();
    	}
    	catch(Exception ignore){}
    }
    
    public static Float getArrivalLogicCoefficientB(Context ctx) {
    	Float coefficientB = Float.valueOf(0.0007f);
    	try {
    		coefficientB = getPrefs(ctx).getFloat(ARRIVAL_LOGIC_COEFFICIENT_B, coefficientB);
    	}
    	catch(Exception ignore){}
    	return coefficientB;
    }
    
    private static void setArrivalLogicCoefficientB(Context ctx, CharSequence coefficientB) {
    	try {
    		getPrefs(ctx).edit().putFloat(ARRIVAL_LOGIC_COEFFICIENT_B, Float.valueOf(coefficientB.toString())).commit();
    	}
    	catch(Exception ignore){}
    }
    
    public static Float getArrivalLogicCoefficientC(Context ctx) {
    	Float coefficientC = Float.valueOf(0.0097f);
    	try {
    		coefficientC = getPrefs(ctx).getFloat(ARRIVAL_LOGIC_COEFFICIENT_C, coefficientC);
    	}
    	catch(Exception ignore){}
    	return coefficientC;
    }
    
    private static void setArrivalLogicCoefficientC(Context ctx, CharSequence coefficientC) {
    	try {
    		getPrefs(ctx).edit().putFloat(ARRIVAL_LOGIC_COEFFICIENT_C, Float.valueOf(coefficientC.toString())).commit();
    	}
    	catch(Exception ignore){}
    }
    
    
    public static final String TRAJECTORY_SENDING_INTERVAL = "TRAJECTORY_SENDING_INTERVAL";
    
    public static final String REROUTE_THRESHOLD_COE = "REROUTE_THRESHOLD_COE";
    public static final String REROUTE_THRESHOLD_MAX = "REROUTE_THRESHOLD_MAX";
    public static final String REROUTE_THRESHOLD_MIN = "REROUTE_THRESHOLD_MIN";
    
    public static Object getDebugValue(Context ctx, String key, Object defaultValue) {
    	Object value = defaultValue;
    	try {
    		if (defaultValue instanceof Integer) value = getPrefs(ctx).getInt(key, (Integer) defaultValue);
    		else if (defaultValue instanceof Float) value = getPrefs(ctx).getFloat(key, (Float) defaultValue);
    		else if (defaultValue instanceof String) value = getPrefs(ctx).getString(key, (String) defaultValue);
    	}
    	catch(Exception ignore){}
    	return value;
    }
    
    private static void setDebugValue(Context ctx, String key, Object value) {
    	try {
    		SharedPreferences.Editor editer = getPrefs(ctx).edit();
    		
    		if (value instanceof Integer) editer.putInt(key, (Integer) value).commit();
    		else if (value instanceof Float) editer.putFloat(key, (Float) value).commit();
    		else if (value instanceof String) editer.putString(key, (String) value).commit();
    	}
    	catch(Exception ignore){}
    }
    
    
    
	
	
	@Override
	public void onStop() {
		super.onStop();
		SendTrajectoryService.schedule(this);
		
		setDebugValue(this, REROUTE_THRESHOLD_COE, Float.parseFloat(rerouteThresholdCoe.getText().toString()));
		setDebugValue(this, REROUTE_THRESHOLD_MAX, Integer.parseInt(rerouteThresholdMax.getText().toString()));
		setDebugValue(this, REROUTE_THRESHOLD_MIN, Integer.parseInt(rerouteThresholdMin.getText().toString()));
	}
    
}
