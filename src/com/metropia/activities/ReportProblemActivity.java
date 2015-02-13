package com.metropia.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.utils.Font;
import com.metropia.activities.R;

public class ReportProblemActivity extends FragmentActivity{
	
	private TextView selectedView;
	
	private TextView badRouteView;
	private TextView streetIncorrectView;
	private TextView missingLocationView;
	private TextView mPointNotValidateView;
	private TextView otherView;
	private LinearLayout selectMenu;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_problem);
		
		Localytics.integrate(this);
		
		TextView backButton = (TextView) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		TextView sendButton = (TextView) findViewById(R.id.send_button);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Integer selected = (Integer) selectedView.getTag();
				if(selected == null) {
					NotificationDialog2 dialog = new NotificationDialog2(ReportProblemActivity.this, "Please select a problem.");
					dialog.show();
					return;
				}
				Log.d("ReportProblem", selected + "");
				EditText descView = (EditText) findViewById(R.id.description);
				String desc = descView.getText().toString();
				// TODO
				Intent resultIntent = new Intent();
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
		});
		
		selectMenu = (LinearLayout) findViewById(R.id.selectMenu);
		
		selectedView = (TextView) findViewById(R.id.select_problem);
		selectedView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Integer selected = (Integer) v.getTag();
				if(selected != null) {
					selectOne(selected);
				}
				selectMenu.setVisibility(View.VISIBLE);
			}
		});
		
		OnClickListener selectListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectOne(v.getId());
				selectedView.setText(((TextView)v).getText());
				selectedView.setTag(v.getId());
				selectMenu.setVisibility(View.GONE);
			}
		};
		
		badRouteView = (TextView) findViewById(R.id.bad_route);
		badRouteView.setOnClickListener(selectListener);
		streetIncorrectView = (TextView) findViewById(R.id.street_incorrect);
		streetIncorrectView.setOnClickListener(selectListener);
		missingLocationView = (TextView) findViewById(R.id.missing_location);
		missingLocationView.setOnClickListener(selectListener);
		mPointNotValidateView = (TextView) findViewById(R.id.mpoint_not_validate);
		mPointNotValidateView.setOnClickListener(selectListener);
		otherView = (TextView) findViewById(R.id.other);
		otherView.setOnClickListener(selectListener);
		
		AssetManager assets = getAssets();
		
		Font.setTypeface(Font.getBold(assets), (TextView) findViewById(R.id.header));
		Font.setTypeface(Font.getLight(assets), backButton, sendButton, selectedView, 
				badRouteView, streetIncorrectView, missingLocationView, mPointNotValidateView, otherView);
		
		//init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	private void selectOne(Integer id) {
		TextView[] selections = new TextView[] { badRouteView, streetIncorrectView, 
				missingLocationView, mPointNotValidateView, otherView };
		
		for(TextView selection : selections) {
			selection.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		
		TextView selected = (TextView) findViewById(id);
		selected.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checked, 0);
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
