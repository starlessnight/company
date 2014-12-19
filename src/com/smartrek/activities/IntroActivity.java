package com.smartrek.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.smartrek.SmarTrekApplication;
import com.smartrek.SmarTrekApplication.TrackerName;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.Font;
import com.smartrek.utils.Preferences;

public class IntroActivity extends FragmentActivity implements OnPageChangeListener{
	
	public static final Integer INTRO_FINISH = 1011;
	
	public static final Integer INTRO_ACTIVITY = 101;
	
	private TextView getStartedView;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        
        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOnPageChangeListener(this);
        SlideAdapter slideAdapter = new SlideAdapter(getSupportFragmentManager());
        mPager.setAdapter(slideAdapter);
        
        LinearLayout indicators = (LinearLayout)findViewById(R.id.indicators);
        for(int i=0; i<slideAdapter.getCount(); i++){
            View indicator = getLayoutInflater().inflate(R.layout.intro_indicator, indicators, false);
            if(i == 0){
                ((LinearLayout.LayoutParams)indicator.getLayoutParams()).leftMargin = 0;
            }else{
                indicator.setEnabled(false);
            }
            indicators.addView(indicator);
        }
        
        getStartedView = (TextView) findViewById(R.id.get_started);
        getStartedView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences prefs = Preferences.getGlobalPreferences(IntroActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(Preferences.Global.INTRO_FINISH, INTRO_FINISH);
                editor.commit();
                setResult(INTRO_FINISH);
                finish();
			}
        });
        
        Font.setTypeface(Font.getRobotoBold(getAssets()), getStartedView);
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
    }
    
    public static class SlideFragment extends Fragment {
        
        static final String IMAGE = "image";
        
        private int image;
        
        static SlideFragment of(int slide){
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
            View view = inflater.inflate(R.layout.intro_slide, container, false);
            ((ImageView)view.findViewById(R.id.image)).setImageResource(image);
            return view;
        }
        
    }
    
    public static class SlideAdapter extends FragmentPagerAdapter {
        
        private static int[] slides = {
            R.drawable.introduction_1,
            R.drawable.introduction_2,
            R.drawable.introduction_3,
            R.drawable.introduction_4
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
        
        View getStarted = findViewById(R.id.get_started);
        LinearLayout.LayoutParams indicatorsLp = (LayoutParams) indicators.getLayoutParams();
        if(pos == indicators.getChildCount() - 1) {
        	indicatorsLp.bottomMargin = 0;
        	getStarted.setVisibility(View.VISIBLE);
        }
        else {
        	indicatorsLp.bottomMargin = Dimension.dpToPx(40, getResources().getDisplayMetrics());
        	getStarted.setVisibility(View.GONE);
        }
        indicators.setLayoutParams(indicatorsLp);
    }
    
    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
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

}
