package com.metropia.ui;

import java.util.ArrayList;

import com.metropia.activities.PassengerActivity;
import com.metropia.activities.R;
import com.metropia.ui.animation.ClickAnimation;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.PageTransformer;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

public class DUOTutorial extends LinearLayout implements OnPageChangeListener, OnClickListener, PageTransformer {
	
	int[] clickableAnimated = {R.id.duoTutorialDriverButton, R.id.duoTutorialPassengerButton};
	int[] backgrounds = {R.drawable.duo_back_driver, R.drawable.duo_back_passenger};
	
	ViewPager pager;
	ViewGroup radios;
	ArrayList<View> arrayView;
	LayerDrawable background;
	
	public DUOTutorial(Context context, AttributeSet attrs) {
		super(context, attrs);
		View view = LayoutInflater.from(context).inflate(R.layout.duo_tutorial, null);
		addView(view);

		ClickAnimation.OnClickListener onClickListener = new ClickAnimation.OnClickListener(this);
		for (int i=0 ; i<clickableAnimated.length ; i++) findViewById(clickableAnimated[i]).setOnClickListener(onClickListener);
		radios = (ViewGroup)findViewById(R.id.duoTutorialRadios);
		for (int i=0 ; i<radios.getChildCount() ; i++) radios.getChildAt(i).setOnClickListener(onClickListener);
		pager = (ViewPager) view.findViewById(R.id.duoTutorialPager);
		
		arrayView = new ArrayList<View>();
		for (int i=0 ; i<pager.getChildCount() ; i++) {
			View v = pager.getChildAt(i);
			v.setTag(i);
			arrayView.add(v);
		}
		((TextView)pager.getChildAt(0).findViewById(R.id.duoTutorialDriverText)).setText(Html.fromHtml(getResources().getString(R.string.duoForDrivers)));
		((TextView)pager.getChildAt(1).findViewById(R.id.duoTutorialPassengerText)).setText(Html.fromHtml(getResources().getString(R.string.duoForPassengers)));
		((TextView)pager.getChildAt(1).findViewById(R.id.duoTutorialPassengerText)).setMovementMethod(LinkMovementMethod.getInstance());
		
		pager.setPageTransformer(true, this);
		pager.addOnPageChangeListener(this);
		pager.setAdapter(new PagerAdapter() {
			public int getCount() {return arrayView.size();}
			public boolean isViewFromObject(View arg0, Object arg1) {return arg0==arg1;}
			public Object instantiateItem(ViewGroup container, int position) {
				pager.addView(arrayView.get(position));
			    return arrayView.get(position);
			}
			
		});
		
		
		background = (LayerDrawable) pager.getBackground();
		background.getDrawable(0).setAlpha(0);
        background.getDrawable(1).setAlpha(255);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.duoTutorialDriverButton:
				setVisibility(View.GONE);
			break;
			case R.id.duoTutorialPassengerButton:
				setVisibility(View.GONE);
				Intent intent = new Intent(getContext(), PassengerActivity.class);
				getContext().startActivity(intent);
			break;
			default:
				int position = radios.indexOfChild(v);
				if (position>=0) pager.setCurrentItem(position);
		}
	}

	
	@Override
	public void onPageScrollStateChanged(int page) {}
	public void onPageScrolled(int arg0, float offset, int arg2) {}
	public void onPageSelected(int page) {
		ViewGroup radios = (ViewGroup)findViewById(R.id.duoTutorialRadios);
		((RadioButton)radios.getChildAt(page)).setChecked(true);
	}

	@Override
	public void transformPage(View view, float position) {
		int index = (Integer) view.getTag();
		Drawable currentDrawableInLayerDrawable;
		currentDrawableInLayerDrawable = background.getDrawable(index);

		if(position <= -1 || position >= 1) {
			currentDrawableInLayerDrawable.setAlpha(0);
		} else if( position == 0 ) {
			currentDrawableInLayerDrawable.setAlpha(255);
		} else { 
			currentDrawableInLayerDrawable.setAlpha((int)(255 - Math.abs(position*255)));
		}
	}
	

}
