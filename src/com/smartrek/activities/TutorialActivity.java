package com.smartrek.activities;

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

import com.smartrek.utils.Dimension;
import com.smartrek.utils.Font;
import com.smartrek.utils.Misc;

public class TutorialActivity extends FragmentActivity implements OnPageChangeListener {
	
	private static final SlideMarginInfo[] indicatorMargins = new SlideMarginInfo[] {
		SlideMarginInfo.of(RelativeLayout.ALIGN_PARENT_BOTTOM, 0, 80),
		SlideMarginInfo.of(RelativeLayout.ALIGN_PARENT_TOP, 100, 0), 
		SlideMarginInfo.of(RelativeLayout.ALIGN_PARENT_BOTTOM, 0, 10), 
		SlideMarginInfo.of(RelativeLayout.ALIGN_PARENT_BOTTOM, 0, 200)
	};
	
	private ViewPager mPager;
	private LinearLayout indicatorsPanel;
	private TextView skipView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial);
        
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOnPageChangeListener(this);
        SlideAdapter slideAdapter = new SlideAdapter(getSupportFragmentManager());
        mPager.setAdapter(slideAdapter);
        
        indicatorsPanel = (LinearLayout) findViewById(R.id.indicators_panel);
        RelativeLayout.LayoutParams indicatorLp = (RelativeLayout.LayoutParams) indicatorsPanel.getLayoutParams();
        SlideMarginInfo marginInfo = indicatorMargins[0];
        indicatorLp.addRule(marginInfo.alignParent);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        indicatorLp.topMargin = Dimension.dpToPx(marginInfo.marginTop, dm);
        indicatorLp.bottomMargin = Dimension.dpToPx(marginInfo.marginBottom, dm);
        
        skipView = (TextView) findViewById(R.id.skip_button);
        skipView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
            ((ImageView)view.findViewById(R.id.image)).setImageResource(image);
            return view;
        }
        
    }
    
    static class SlideMarginInfo {
    	int alignParent;
    	int marginTop;
    	int marginBottom;
    	
    	static SlideMarginInfo of(int alignParent, int marginTop, int marginBottom) {
    		SlideMarginInfo info = new SlideMarginInfo();
    		info.alignParent = alignParent;
    		info.marginTop = marginTop;
    		info.marginBottom = marginBottom;
    		return info;
    	}
    }
    
    public static class SlideAdapter extends FragmentPagerAdapter {
        
        private static int[] slides = {
            R.drawable.tutorial_1,
            R.drawable.tutorial_2,
            R.drawable.tutorial_3,
            R.drawable.tutorial_4
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
        indicatorsPanelLp.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        indicatorsPanelLp.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
        indicatorsPanelLp.addRule(marginInfo.alignParent);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        indicatorsPanelLp.topMargin = Dimension.dpToPx(marginInfo.marginTop, dm);
        indicatorsPanelLp.bottomMargin = Dimension.dpToPx(marginInfo.marginBottom, dm);
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
    }
    
    @Override
    protected void onRestart() {
        super.onRestart();
        Misc.tripInfoPanelOnActivityRestart(this);
    }
    
}
