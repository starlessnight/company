package com.metropia.ui;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.PageTransformer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class LoginPager extends ViewPager implements PageTransformer {

	Context context;
	ArrayList<View> arrayView;
	
	public LoginPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.setPageTransformer(true, this);
	}
	
	public void setAdapter(int... layouts) {
		arrayView = new ArrayList<View>();
		
		for (int i=0 ; i<layouts.length ; i++) {
			View view = ((Activity)context).findViewById(layouts[i]);
			arrayView.add(view);
		}
		
		setAdapter(new PagerAdapter() {
			public int getCount() {return arrayView.size();}
			public boolean isViewFromObject(View arg0, Object arg1) {return arg0==arg1;}
			public Object instantiateItem(ViewGroup container, int position) {
				LoginPager.this.addView(arrayView.get(position));
			    return arrayView.get(position);
			}
		});
	}
	

	@SuppressLint("NewApi")
	@Override
	public void transformPage(View view, float position) {
    	view.setAlpha(1 - Math.abs(position)*2);
	}
	
    public boolean onInterceptTouchEvent(MotionEvent event) {return false;}
    public boolean onTouchEvent(MotionEvent event) {return false;}

}
