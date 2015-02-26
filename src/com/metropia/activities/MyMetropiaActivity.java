package com.metropia.activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.LocalyticsUtils;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.utils.Dimension;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;
import com.metropia.activities.R;

public class MyMetropiaActivity extends FragmentActivity{
	
	public static final String OPEN_TAB = "TAB";
	public static final String DRIVE_SCORE_TAB = "DEIVE_SCORE_TAB";
	public static final String CO2_SAVING_TAB = "CO2_SAVING_TAB";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_metropia);
		
		Localytics.integrate(this);
		LocalyticsUtils.tagVisitMyMetropia();
		
		TextView backButton = (TextView) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		Intent intent = getIntent();
		String openTab = intent.getStringExtra(OPEN_TAB);
		if(CO2_SAVING_TAB.equals(openTab)) {
			findViewById(R.id.drive_score_panel).setVisibility(View.GONE);
			findViewById(R.id.co2_savings_panel).setVisibility(View.VISIBLE);
		}
		else {
			findViewById(R.id.co2_savings_panel).setVisibility(View.GONE);
			findViewById(R.id.drive_score_panel).setVisibility(View.VISIBLE);
		}
		
		
		findViewById(R.id.drive_score_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.co2_savings_panel).setVisibility(View.GONE);
				findViewById(R.id.drive_score_panel).setVisibility(View.VISIBLE);
			}
		});
		
		findViewById(R.id.co2_savings_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.drive_score_panel).setVisibility(View.GONE);
				findViewById(R.id.co2_savings_panel).setVisibility(View.VISIBLE);
			}
		});
		
		TextView co2Reduce = (TextView) findViewById(R.id.co2_reduce_desc);
		String co2ReduceValue = "400";  //to query co2 reduce value
		co2Reduce.setText(formatCO2Desc(co2ReduceValue));
		
		TextView treePlanted = (TextView) findViewById(R.id.tree_planted);
		String treePlantedNum = "15";
		treePlanted.setText(formatTreePlantedDesc(treePlantedNum));
		
		TextView shareMyCo2Saving = (TextView) findViewById(R.id.share_my_co2_saving);
		CharSequence msg = shareMyCo2Saving.getText();
		shareMyCo2Saving.setText(formatCO2(msg.toString()));
		findViewById(R.id.share_my_co2_panel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    Misc.suppressTripInfoPanel(MyMetropiaActivity.this);
				Intent shareIntent = new Intent(MyMetropiaActivity.this, ShareActivity.class);
				shareIntent.putExtra(ShareActivity.TITLE, "...");
				shareIntent.putExtra(ShareActivity.SHARE_TEXT, "...");
				startActivity(shareIntent);
			}
		});
		
		TextView shareMyScore = (TextView) findViewById(R.id.share_my_score);
		findViewById(R.id.share_my_score_panel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    Misc.suppressTripInfoPanel(MyMetropiaActivity.this);
				Intent shareIntent = new Intent(MyMetropiaActivity.this, ShareActivity.class);
				shareIntent.putExtra(ShareActivity.TITLE, "...");
				shareIntent.putExtra(ShareActivity.SHARE_TEXT, "...");
				startActivity(shareIntent);
			}
		});
		
		TextView co2ToTree = (TextView) findViewById(R.id.co2_to_tree_rule_desc);
		co2ToTree.setText(formatMetropia(formatCO2("For every 200 lbs of CO2 emissions" + 
		    " that you save by using metropia a tree will be planted for you!")));
		
		TextView co2ValueMask = (TextView) findViewById(R.id.co2_value_mask);
		String co2Value = "50";  //query co2
		co2ValueMask.setText(co2Value + " lbs");
		Animation slideup = new TranslateAnimation(0, 0, 0, 
				-1 * Dimension.dpToPx(Integer.valueOf(co2Value)*134/200, getResources().getDisplayMetrics()));
		slideup.setDuration(10);
		co2ValueMask.startAnimation(slideup);
		slideup.setFillAfter(true);
		
		findViewById(R.id.mPoint_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Misc.suppressTripInfoPanel(MyMetropiaActivity.this);
            }
        });
		
		AssetManager assets = getAssets();
		
		Font.setTypeface(Font.getBold(assets), (TextView) findViewById(R.id.header), 
				shareMyCo2Saving, shareMyScore);
		Font.setTypeface(Font.getLight(assets), (TextView) findViewById(R.id.back_button), 
				co2Reduce, treePlanted, co2ToTree, backButton);
		
		//init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	private CharSequence formatCO2Desc(String co2ReduceValue) {
		String msg = co2ReduceValue + "lbs\nCO2 Reduced";
		int lbsIndex = msg.indexOf("lbs");
		SpannableString co2ValueSpan = SpannableString.valueOf(msg);
		co2ValueSpan.setSpan(new AbsoluteSizeSpan(MyMetropiaActivity.this.getResources()
				.getDimensionPixelSize(R.dimen.smallest_font)), lbsIndex,
				lbsIndex + "lbs".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		int twoIndex = msg.indexOf("2");
		co2ValueSpan.setSpan(new AbsoluteSizeSpan(MyMetropiaActivity.this.getResources()
				.getDimensionPixelSize(R.dimen.smallest_font)), twoIndex,
				twoIndex + "2".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		co2ValueSpan.setSpan(new ForegroundColorSpan(MyMetropiaActivity.this.getResources().getColor(R.color.metropia_green)), 
				0, co2ReduceValue.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		co2ValueSpan.setSpan(new AbsoluteSizeSpan(MyMetropiaActivity.this.getResources()
				.getDimensionPixelSize(R.dimen.large_font)), 
				0, co2ReduceValue.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return co2ValueSpan;
	}
	
	private CharSequence formatTreePlantedDesc(String treePlantedNum) {
		String msg = treePlantedNum + "\nTrees Planted";
		SpannableString treePlantedSpan = SpannableString.valueOf(msg);
		treePlantedSpan.setSpan(new ForegroundColorSpan(MyMetropiaActivity.this.getResources().getColor(R.color.metropia_green)), 
				0, treePlantedNum.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		treePlantedSpan.setSpan(new AbsoluteSizeSpan(MyMetropiaActivity.this.getResources()
				.getDimensionPixelSize(R.dimen.large_font)), 
				0, treePlantedNum.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return treePlantedSpan;
	}
	
	private CharSequence formatCO2(String msg) {
		int co2Index = msg.indexOf("CO2");
		int twoIndex = co2Index + 2;
		SpannableString msgSpan = SpannableString.valueOf(msg);
		msgSpan.setSpan(new AbsoluteSizeSpan(MyMetropiaActivity.this.getResources().getDimensionPixelSize(R.dimen.smallest_font)), 
				twoIndex, twoIndex + "2".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return msgSpan;
	}
	
	private CharSequence formatMetropia(CharSequence msg) {
		int metropiaIndex = msg.toString().indexOf("metropia");
		SpannableString msgSpan = SpannableString.valueOf(msg);
		msgSpan.setSpan(new StyleSpan(Typeface.BOLD), metropiaIndex, metropiaIndex + "metropia".length(), 
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return msgSpan;
	}

    @Override
    public void onStop() {
        super.onStop();
        Misc.tripInfoPanelOnActivityStop(this);
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
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
    }

    @Override
    protected void onPause() {
    	Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
        super.onPause();
    }
    
    @Override
    protected void onRestart() {
        super.onRestart();
        Misc.tripInfoPanelOnActivityRestart(this);
    }

}
