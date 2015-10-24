package com.metropia.activities;

import org.apache.commons.lang3.StringUtils;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.dialogs.CancelableProgressDialog;
import com.metropia.models.PoiOverlayInfo;
import com.metropia.models.Reservation;
import com.metropia.models.User;
import com.metropia.requests.ReservationFetchRequest;
import com.metropia.ui.EditAddress;
import com.metropia.utils.Font;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.Misc;

public final class PreTripAlertActivity extends FragmentActivity {
	
    public static final String URL = "url";
    
    public static final String MSG = "msg";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pretrip_alert);
        
        Localytics.integrate(this);
        
        Bundle extras = getIntent().getExtras();
        
        TextView msg = (TextView)findViewById(R.id.message);
        msg.setText(extras.getString(MSG));
        
        TextView cancel = (TextView)findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        TextView next = (TextView)findViewById(R.id.next);
        final String url = extras.getString(URL);
        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask<Void, Void, Reservation> task = new AsyncTask<Void, Void, Reservation>(){
                    
                    CancelableProgressDialog dialog = new CancelableProgressDialog(PreTripAlertActivity.this, "Loading...");
                    
                    @Override
                    protected void onPreExecute() {
                    	Misc.doQuietly(new Runnable() {
							@Override
							public void run() {
								dialog.show();
							}
                    	});
                    }
                    
                    @Override
                    protected Reservation doInBackground(Void... params) {
                        Reservation reserv = null;
                        try {
                            reserv = new ReservationFetchRequest(
                                User.getCurrentUser(PreTripAlertActivity.this), url)
                                .execute(PreTripAlertActivity.this);
                        }
                        catch (Exception e) {}
                        return reserv;
                    }
                    @Override
                    protected void onPostExecute(Reservation reserv) {
                    	Misc.doQuietly(new Runnable() {
							@Override
							public void run() {
								dialog.dismiss();
							}
                    	});
                        
                        if(reserv != null){
                            Intent intent = new Intent(PreTripAlertActivity.this, RouteActivity.class);
                            Bundle extras = new Bundle();
                            extras.putBoolean(RouteActivity.CURRENT_LOCATION, StringUtils.equalsIgnoreCase(EditAddress.CURRENT_LOCATION, reserv.getOriginAddress()));
                            extras.putLong(RouteActivity.RESCHEDULE_RESERVATION_ID, reserv.getRid());
                            extras.putString("originAddr", reserv.getOriginAddress());
                            try {
                            	GeoPoint originLoc = reserv.getStartGpFromNavLink();
                            	extras.putParcelable(RouteActivity.ORIGIN_COORD, originLoc);
                            	extras.putParcelable(RouteActivity.ORIGIN_OVERLAY_INFO, createPoiOverlayInfo(originLoc, reserv.getOriginAddress()));
                            }
                            catch(Exception ignore) {}
                            extras.putString("destAddr", reserv.getDestinationAddress());
                            GeoPoint destLoc = new GeoPoint(reserv.getEndlat(), reserv.getEndlon());
                            extras.putParcelable(RouteActivity.DEST_COORD, destLoc);
                            extras.putParcelable(RouteActivity.DEST_OVERLAY_INFO, createPoiOverlayInfo(destLoc, reserv.getDestinationAddress()));
                            extras.putLong(RouteActivity.RESCHEDULE_DEPARTURE_TIME, reserv.getDepartureTimeUtc());
                            intent.putExtras(extras);
                            startActivity(intent);
                        }
                        finish();
                    }
                };
                Misc.parallelExecute(task);
            }
        });
        
        AssetManager assets = getAssets();
        Font.setTypeface(Font.getBold(assets), (TextView)findViewById(R.id.title), 
            cancel, next);
        Font.setTypeface(Font.getLight(assets), msg);
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
    }
    
    private PoiOverlayInfo createPoiOverlayInfo(GeoPoint location, String address) {
    	PoiOverlayInfo poiInfo = new PoiOverlayInfo();
		poiInfo.id = 0;
		poiInfo.label = "";
		poiInfo.address = address;
		poiInfo.lat = location.getLatitude();
		poiInfo.lon = location.getLongitude();
		poiInfo.geopoint = location;
		poiInfo.marker = R.drawable.poi_pin;
		poiInfo.markerWithShadow = R.drawable.poi_pin_with_shadow;
		return poiInfo;
    }
    
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
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
	public void onResume() {
		super.onResume();
	    Localytics.openSession();
	    Localytics.tagScreen(this.getClass().getSimpleName());
	    Localytics.upload();
	    Localytics.setInAppMessageDisplayActivity(this);
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
	}

	@Override
	public void onPause() {
	    Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
	    super.onPause();
	}
	
}
