package com.metropia.activities;

import java.text.DecimalFormat;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.localytics.android.Localytics;
import com.metropia.models.Reservation;
import com.metropia.ui.ClickAnimation;
import com.metropia.ui.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;
import com.metropia.activities.R;

public class CongratulationActivity extends FragmentActivity {

	public static final String DEPARTURE_TIME = "DEPARTURE_TIME";
	
	public static final String DESTINATION = "DESTINATION";

	private Typeface boldFont;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.congratulation);
		
		Localytics.integrate(this);

		AssetManager assets = getAssets();
		boldFont = Font.getBold(assets);
		
		Bundle extras = getIntent().getExtras();
		final int credit = extras.getInt(ValidationActivity.CREDIT);
		final long departureTime = extras.getLong(DEPARTURE_TIME);

		final View shareButton = findViewById(R.id.share);
		shareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(
						CongratulationActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {

					@Override
					public void onAnimationEnd() {
						Intent intent = new Intent(CongratulationActivity.this,
								ShareActivity.class);
						intent.putExtra(ShareActivity.TITLE,
								"More Metropians = Less Traffic");
						intent.putExtra(
								ShareActivity.SHARE_TEXT,
								"I earned "
										+ credit
										+ " points for traveling at "
										+ Reservation.formatTime(departureTime, true)
										+ " to help solve traffic congestion "
										+ "using Metropia!" + "\n\n"
										+ Misc.APP_DOWNLOAD_LINK);
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
				ClickAnimation clickAnimation = new ClickAnimation(CongratulationActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Intent intent = new Intent(CongratulationActivity.this, FeedbackActivity.class);
						startActivity(intent);
					}
				});
			}
		});
		
		TextView finishButton = (TextView) findViewById(R.id.close);
		finishButton.setText(Html.fromHtml("<u>Close</u>"));
		finishButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent landingIntent = new Intent(CongratulationActivity.this, LandingActivity2.class);
				landingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(landingIntent);
				finish();
			}
		});
		
		double timeSavingInMinute = extras.getDouble(ValidationActivity.TIME_SAVING_IN_MINUTE);
		double co2Saving = extras.getDouble(ValidationActivity.CO2_SAVING);
		String message = extras.getString(ValidationActivity.MESSAGE, "");
		String dest = extras.getString(DESTINATION);
		
        String msg = message + "\n" + 
            dest.substring(0, dest.indexOf(",")>-1?dest.indexOf(","):dest.length());
        TextView congratsMsg = (TextView) findViewById(R.id.congrats_msg);
        congratsMsg.setText(ValidationActivity.formatCongrMessage(CongratulationActivity.this, msg));
        congratsMsg.setVisibility(View.VISIBLE);
        findViewById(R.id.congrats_msg_shadow).setVisibility(View.VISIBLE);
        
        TextView co2 = (TextView) findViewById(R.id.co2_circle);
        if(co2Saving != 0) {
            String co2String = co2Saving + "lbs\nCO2";  
            co2.setText(ValidationActivity.formatCO2Desc(CongratulationActivity.this, co2String));
            ((ImageView)findViewById(R.id.co2_circle_background)).setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.blue_circle)));
            findViewById(R.id.co2_circle_panel).setVisibility(View.VISIBLE);
        }
        
        TextView mpoint = (TextView) findViewById(R.id.mpoint_circle);
        if(credit > 0){
            mpoint.setText(ValidationActivity.formatCongrValueDesc(CongratulationActivity.this, credit + "\nPoints"));
            ((ImageView)findViewById(R.id.mpoint_circle_background)).setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.green_circle)));
            findViewById(R.id.mpoint_circle_panel).setVisibility(View.VISIBLE);
        }
        
        TextView driveScore = (TextView) findViewById(R.id.drive_score_circle);
        if(timeSavingInMinute > 0) {
            String scoreString = new DecimalFormat("0.#").format(timeSavingInMinute) + "\nminutes"; 
            driveScore.setText(ValidationActivity.formatCongrValueDesc(CongratulationActivity.this, scoreString));
            ((ImageView)findViewById(R.id.drive_score_circle_background)).setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.red_circle)));
            findViewById(R.id.drive_score_circle_panel).setVisibility(View.VISIBLE);
        }
        
        findViewById(R.id.co2_circle).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    Intent intent;
			    if(WebMyMetropiaActivity.hasCo2SavingUrl(CongratulationActivity.this) || WebMyMetropiaActivity.hasMyMetropiaUrl(CongratulationActivity.this)){
			        intent = new Intent(CongratulationActivity.this, WebMyMetropiaActivity.class);
			        Integer pageNo = WebMyMetropiaActivity.hasCo2SavingUrl(CongratulationActivity.this) ? WebMyMetropiaActivity.CO2_SAVING_PAGE : WebMyMetropiaActivity.CO2_SAVING_PAGE; 
			        intent.putExtra(WebMyMetropiaActivity.WHICH_PAGE, pageNo);
			    }else{
			        intent = new Intent(CongratulationActivity.this, MyMetropiaActivity.class);
    				intent.putExtra(MyMetropiaActivity.OPEN_TAB, MyMetropiaActivity.CO2_SAVING_TAB);
			    }
			    startActivity(intent);
			}
        });
        
        findViewById(R.id.drive_score_circle).setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        	    Intent intent;
                if(WebMyMetropiaActivity.hasTimeSavingUrl(CongratulationActivity.this) || WebMyMetropiaActivity.hasMyMetropiaUrl(CongratulationActivity.this)){
                    intent = new Intent(CongratulationActivity.this, WebMyMetropiaActivity.class);
                    Integer pageNo = WebMyMetropiaActivity.hasTimeSavingUrl(CongratulationActivity.this) ? WebMyMetropiaActivity.TIME_SAVING_PAGE : WebMyMetropiaActivity.CO2_SAVING_PAGE; 
			        intent.putExtra(WebMyMetropiaActivity.WHICH_PAGE, pageNo);
                }else{
    				intent = new Intent(CongratulationActivity.this, MyMetropiaActivity.class);
    				intent.putExtra(MyMetropiaActivity.OPEN_TAB, MyMetropiaActivity.DRIVE_SCORE_TAB);
                }
				startActivity(intent);
			}
        });
        
        findViewById(R.id.mpoint_circle).setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        	    Intent intent;
                if(WebMyMetropiaActivity.hasMyMetropiaUrl(CongratulationActivity.this)){
                    intent = new Intent(CongratulationActivity.this, WebMyMetropiaActivity.class);
                    intent.putExtra(WebMyMetropiaActivity.WHICH_PAGE, WebMyMetropiaActivity.MY_METROPIA_PAGE);
                }else{
    				intent = new Intent(CongratulationActivity.this, MyMetropiaActivity.class);
    				intent.putExtra(MyMetropiaActivity.OPEN_TAB, MyMetropiaActivity.CO2_SAVING_TAB);
                }
				startActivity(intent);
			}
        });
        
        Font.setTypeface(boldFont, co2, mpoint, driveScore);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	    Localytics.openSession();
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
