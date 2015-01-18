package com.metropia.activities;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.utils.Dimension;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;
import com.metropia.utils.Preferences;

public class TutorialActivity extends FragmentActivity implements OnPageChangeListener {
	
	public static final Integer TUTORIAL_FINISH = Integer.valueOf(1);
	
	private static final SlideMarginInfo[] indicatorMargins = new SlideMarginInfo[] {
		SlideMarginInfo.of(RelativeLayout.ALIGN_PARENT_BOTTOM, 0, LayoutParams.MATCH_PARENT, 0, 120, 0, 0), 
		SlideMarginInfo.of(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.ALIGN_PARENT_RIGHT, LayoutParams.WRAP_CONTENT, 0, 200, 0, 10),
		SlideMarginInfo.of(RelativeLayout.ALIGN_PARENT_BOTTOM, 0, LayoutParams.MATCH_PARENT, 0, 250, 0, 0)
	};
	
	private ViewPager mPager;
	private LinearLayout indicatorsPanel;
	private TextView skipView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial);
        
        Localytics.integrate(this);
        
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOnPageChangeListener(this);
        SlideAdapter slideAdapter = new SlideAdapter(getSupportFragmentManager());
        mPager.setAdapter(slideAdapter);
        
        indicatorsPanel = (LinearLayout) findViewById(R.id.indicators_panel);
        RelativeLayout.LayoutParams indicatorLp = (RelativeLayout.LayoutParams) indicatorsPanel.getLayoutParams();
        SlideMarginInfo marginInfo = indicatorMargins[0];
        indicatorLp.addRule(marginInfo.alignParentY, RelativeLayout.TRUE);
        indicatorLp.addRule(marginInfo.alignParentX, RelativeLayout.TRUE);
        indicatorLp.width = marginInfo.width;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        indicatorLp.topMargin = Dimension.dpToPx(marginInfo.marginTop, dm);
        indicatorLp.bottomMargin = Dimension.dpToPx(marginInfo.marginBottom, dm);
        indicatorLp.leftMargin = Dimension.dpToPx(marginInfo.marginLeft, dm);
        indicatorLp.rightMargin = Dimension.dpToPx(marginInfo.marginRight, dm);
        indicatorsPanel.setLayoutParams(indicatorLp);
        
        skipView = (TextView) findViewById(R.id.skip_button);
        skipView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences prefs = Preferences.getGlobalPreferences(TutorialActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(Preferences.Global.TUTORIAL_FINISH, TUTORIAL_FINISH);
                editor.commit();
				finish();
			}
        });
        
        findViewById(R.id.left_arrow).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int nextItemIndex = Math.max(mPager.getCurrentItem()-1, 0);
				mPager.setCurrentItem(nextItemIndex, true);
			}
		});
        
        findViewById(R.id.right_arrow).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int nextItemIndex = Math.min(mPager.getCurrentItem()+1, indicatorMargins.length-1);
				mPager.setCurrentItem(nextItemIndex, true);
			}
		});
        
        LinearLayout indicators = (LinearLayout)findViewById(R.id.indicators);
        for(int i=0; i<slideAdapter.getCount(); i++){
            View indicator = getLayoutInflater().inflate(R.layout.tutorial_indicator, indicators, false);
            if(i == 0){
                ((LinearLayout.LayoutParams)indicator.getLayoutParams()).leftMargin = 0;
            }else{
                indicator.setEnabled(false);
            }
            indicators.addView(indicator);
        }
        
        Font.setTypeface(Font.getMedium(getAssets()), skipView);
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
    }
    
    public static class SlideFragment extends Fragment {
        
        static final String IMAGE = "image";
        
        private int image;
        
        static SlideFragment of(Integer slide){
            SlideFragment f = new SlideFragment();
            Bundle args = new Bundle();
            args.putInt(IMAGE, slide);
            f.setArguments(args);
            return f;
        }
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();
            this.image = args.getInt(IMAGE);
        }
     
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }
     
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.tutorial_slide, container, false);
            ((ImageView)view.findViewById(R.id.image)).setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(image)));
            return view;
        }
        
    }
    
    static class SlideMarginInfo {
    	int alignParentY;
    	int alignParentX;
    	int width;
    	int marginTop;
    	int marginBottom;
    	int marginLeft;
    	int marginRight;
    	
    	static SlideMarginInfo of(int alignParentY, int alignParentX, int width, int marginTop, int marginBottom, int marginLeft, int marginRight) {
    		SlideMarginInfo info = new SlideMarginInfo();
    		info.alignParentY = alignParentY;
    		info.alignParentX = alignParentX;
    		info.width = width;
    		info.marginTop = marginTop;
    		info.marginBottom = marginBottom;
    		info.marginLeft = marginLeft;
    		info.marginRight = marginRight;
    		return info;
    	}
    }
    
    public static class SlideAdapter extends FragmentPagerAdapter {
        
        private static int[] slides = {
            R.drawable.new_tutorial_1,
            R.drawable.new_tutorial_2, 
            R.drawable.new_tutorial_3
        };
        
        public SlideAdapter(FragmentManager fm) {
            super(fm);
        }
 
        @Override
        public int getCount() {
            return slides.length;
        }
 
        @Override
        public Fragment getItem(int position) {
            return SlideFragment.of(slides[position]);
        }
        
    }
    
    @Override
    public void onPageSelected(int pos) {
        LinearLayout indicators = (LinearLayout)findViewById(R.id.indicators);
        for(int i=0; i<indicators.getChildCount(); i++){
            indicators.getChildAt(i).setEnabled(i == pos);
        }
        SlideMarginInfo marginInfo = indicatorMargins[pos];
        LayoutParams indicatorsPanelLp = (RelativeLayout.LayoutParams)indicatorsPanel.getLayoutParams();
        indicatorsPanelLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        indicatorsPanelLp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        indicatorsPanelLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
        indicatorsPanelLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        indicatorsPanelLp.addRule(marginInfo.alignParentY, RelativeLayout.TRUE);
        indicatorsPanelLp.width = marginInfo.width;
        if(marginInfo.alignParentX != 0) {
        	indicatorsPanelLp.addRule(marginInfo.alignParentX, RelativeLayout.TRUE);
        }
        DisplayMetrics dm = getResources().getDisplayMetrics();
        indicatorsPanelLp.topMargin = Dimension.dpToPx(marginInfo.marginTop, dm);
        indicatorsPanelLp.bottomMargin = Dimension.dpToPx(marginInfo.marginBottom, dm);
        indicatorsPanelLp.leftMargin = Dimension.dpToPx(marginInfo.marginLeft, dm);
        indicatorsPanelLp.rightMargin = Dimension.dpToPx(marginInfo.marginRight, dm);
        indicatorsPanel.setLayoutParams(indicatorsPanelLp);
        if(pos==indicatorMargins.length-1) {
        	skipView.setText("Finish");
        }
        else {
        	skipView.setText("Skip");
        }
    }
    
    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Misc.tripInfoPanelOnActivityStop(this);
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
    
    @Override
    protected void onRestart() {
        super.onRestart();
        Misc.tripInfoPanelOnActivityRestart(this);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
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
