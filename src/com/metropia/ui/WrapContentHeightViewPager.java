package com.metropia.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.metropia.utils.Dimension;

public class WrapContentHeightViewPager extends ViewPager {
	
	private boolean enabled = true;

	public WrapContentHeightViewPager(Context context) {
		super(context);
	}

	public WrapContentHeightViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private Integer MINIMUM_HEIGHT = 125; // dp

	/*@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = 0;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			child.measure(widthMeasureSpec,
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			int h = child.getMeasuredHeight();
			if (h > height) {
				height = h;
			}
		}
		
		height = Math.max(height, Dimension.dpToPx(MINIMUM_HEIGHT, getContext().getResources().getDisplayMetrics()));

		heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
				MeasureSpec.EXACTLY);

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}*/
	
	//dp
	public void setMinimemHeight(Integer minimumHeight) {
		this.MINIMUM_HEIGHT = minimumHeight;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    if (this.enabled) {
	        return super.onTouchEvent(event);
	    }

	    return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
	    if (this.enabled) {
	        return super.onInterceptTouchEvent(event);
	    }

	    return false;
	}

	public void setPagingEnabled(boolean enabled) {
	    this.enabled = enabled;
	} 

}
