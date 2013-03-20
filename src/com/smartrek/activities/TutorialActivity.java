package com.smartrek.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartrek.utils.Font;

public class TutorialActivity extends FragmentActivity implements OnPageChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial);
        
        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOnPageChangeListener(this);
        SlideAdapter slideAdapter = new SlideAdapter(getSupportFragmentManager());
        mPager.setAdapter(slideAdapter);
        
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
        
        findViewById(R.id.sign_up_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TutorialActivity.this, UserRegistrationActivity.class);
                startActivity(intent);
            }
        });
        
        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TutorialActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    
    public static class SlideFragment extends Fragment {
        
        static final String IMAGE = "image";
        
        static final String TEXT = "text";
        
        private int image;
        
        private int text;
        
        static SlideFragment of(Slide slide){
            SlideFragment f = new SlideFragment();
            Bundle args = new Bundle();
            args.putInt(IMAGE, slide.image);
            args.putInt(TEXT, slide.text);
            f.setArguments(args);
            return f;
        }
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();
            this.image = args.getInt(IMAGE);
            this.text = args.getInt(TEXT);
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
            TextView textView = (TextView)view.findViewById(R.id.text);
            textView.setText(text);
            Font.setTypeface(Font.getLight(getActivity().getAssets()), textView);
            return view;
        }
        
    }
    
    public static class SlideAdapter extends FragmentPagerAdapter {
        
        private static Slide[] slides = {
            new Slide(R.drawable.tutorial_slide1, R.string.tutorial_slide1),
            new Slide(R.drawable.tutorial_slide2, R.string.tutorial_slide2),
            new Slide(R.drawable.tutorial_slide3, R.string.tutorial_slide3),
            new Slide(R.drawable.tutorial_slide4, R.string.tutorial_slide4),
            new Slide(R.drawable.tutorial_slide5, R.string.tutorial_slide5),
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
    
    private static class Slide {
        
        int image;
        
        int text;

        public Slide(int image, int text) {
            this.image = image;
            this.text = text;
        }
        
    }

    @Override
    public void onPageSelected(int pos) {
        LinearLayout indicators = (LinearLayout)findViewById(R.id.indicators);
        for(int i=0; i<indicators.getChildCount(); i++){
            indicators.getChildAt(i).setEnabled(i == pos);
        }
    }
    
    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }
    
}
