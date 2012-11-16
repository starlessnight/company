package com.smartrek.ui.timelayout;

import android.content.Context;
import android.util.AttributeSet;

import com.smartrek.ui.ObservableScrollView;
import com.smartrek.ui.ScrollViewListener;

/**
 * This is a wrapper around `TimeLayout`. The primary responsibility of this
 * class is to calculate which region of `TimeLayout` is currently visible on
 * screen and notify `TimeLayout` which columns become visible as users scroll
 * this view.
 */
public final class ScrollableTimeLayout extends ObservableScrollView implements ScrollViewListener {

	// FIXME: This must be loaded dynamically
	private int screenWidth = 450;
	
	private TimeLayout timeLayout;
	
	//private ScrollableTimeLayoutListener listener;
	
//	public interface ScrollableTimeLayoutListener {
//		public void onScroll();
//	}
	
	public ScrollableTimeLayout(Context context, AttributeSet attributes) {
		super(context, attributes);
		setScrollViewListener(this);
	}
	
	public void setTimeLayout(TimeLayout timeLayout) {
		this.timeLayout = timeLayout;
	}
	
	public int getScreenWidth() {
		return screenWidth;
	}
	
	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}
	
//	public void setScrollableTimeLayoutListener(ScrollableTimeLayoutListener listener) {
//		this.listener = listener;
//	}

	@Override
	public void onScrollChanged(ObservableScrollView scrollView, int x, int y,	int oldx, int oldy) {
		if(timeLayout != null) {
			int columnWidth = timeLayout.getColumnWidth();
			int upperBound = (getScreenWidth() + x) / columnWidth;
			
			if (upperBound < timeLayout.getColumnCount()) {
				timeLayout.notifyColumn(upperBound, true);
			}
		}
	}

}
