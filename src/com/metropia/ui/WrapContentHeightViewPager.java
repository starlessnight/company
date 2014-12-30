package com.metropia.ui;

import com.metropia.utils.Dimension;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class WrapContentHeightViewPager extends ViewPager {

	public WrapContentHeightViewPager(Context context) {
		super(context);
	}

	public WrapContentHeightViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private static final Integer MINIMUM_HEIGHT = 125; // dp

	@Override
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
	}

}
