package com.smartrek.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.smartrek.dialogs.CancelableProgressDialog;
import com.smartrek.models.Reservation;
import com.smartrek.models.User;
import com.smartrek.requests.ReservationFetchRequest;
import com.smartrek.ui.EditAddress;
import com.smartrek.utils.Font;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Misc;

public final class PreTripAlertActivity extends Activity {
	
    public static final String URL = "url";
    
    public static final String MSG = "msg";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pretrip_alert);
        
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
                        dialog.show();
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
                        dialog.dismiss();
                        if(reserv != null){
                            Intent intent = new Intent(PreTripAlertActivity.this, RouteActivity.class);
                            intent.putExtra(RouteActivity.CURRENT_LOCATION, true);
                            Bundle extras = new Bundle();
                            extras.putLong(RouteActivity.RESCHEDULE_RESERVATION_ID, reserv.getRid());
                            extras.putString("originAddr", EditAddress.CURRENT_LOCATION);
                            extras.putParcelable(RouteActivity.ORIGIN_COORD, new GeoPoint(0, 0 ));
                            extras.putString("destAddr", reserv.getDestinationAddress());
                            extras.putParcelable(RouteActivity.DEST_COORD, new GeoPoint(reserv.getEndlat(), reserv.getEndlon()));
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
    }
    
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }
	
}