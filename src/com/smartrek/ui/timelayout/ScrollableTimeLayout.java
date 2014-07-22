package com.smartrek.ui.timelayout;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import com.smartrek.ui.ObservableScrollView;
import com.smartrek.ui.ScrollViewListener;
import com.smartrek.utils.Dimension;

/**
 * This is a wrapper around `TimeLayout`. The primary responsibility of this
 * class is to calculate which region of `TimeLayout` is currently visible on
 * screen and notify `TimeLayout` which columns become visible as users scroll
 * this view.
 */
public final class ScrollableTimeLayout extends ObservableScrollView implements ScrollViewListener {

	// FIXME: This must be loaded dynamically
	private static int screenWidth = 450;
	
	private Runnable scrollerTask;
	private int initialPosition;
	private int checkPosition;

	private int newCheck = 100;
	
	private TimeLayout timeLayout;
	
	private int scrollX;
	
	//private ScrollableTimeLayoutListener listener;
	
//	public interface ScrollableTimeLayoutListener {
//		public void onScroll();
//	}
	
	public ScrollableTimeLayout(Context context, AttributeSet attributes) {
		super(context, attributes);
		setScrollViewListener(this);
		initDetector();
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
	
	private void initDetector() {
		scrollerTask = new Runnable() {
			@Override
			public void run() {
				int newPosition = getScrollX();
	            if(Math.abs(checkPosition - newPosition) < TimeButton.WIDTH){//has stopped
	            	boolean swipeRight = initialPosition - newPosition < 0;
	            	initialPosition = newPosition;
	            	int mod = newPosition%TimeButton.WIDTH;
	            	int columnIndex = newPosition/TimeButton.WIDTH + (mod!=0&&swipeRight?1:0);
	            	smoothScrollTo(columnIndex*TimeButton.WIDTH, 0);
	            	timeLayout.preSelectColumn(columnIndex);
	            }else{
	            	checkPosition = getScrollX();
	                ScrollableTimeLayout.this.postDelayed(scrollerTask, newCheck);
	            }
			}
			
		};
		
		setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				//If the user swipes
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 	                startScrollerTask();
 	            }
 	            return false;
 			}
 		});
	}
	
	public void startScrollerTask(){
		checkPosition = getScrollX();
	    ScrollableTimeLayout.this.postDelayed(scrollerTask, newCheck);
	}
	
//	public void setScrollableTimeLayoutListener(ScrollableTimeLayoutListener listener) {
//		this.listener = listener;
//	}
	
	@Override
	public void onScrollChanged(ObservableScrollView scrollView, int x, int y,	int oldx, int oldy) {
	    scrollX = x;
	    notifyScrollChanged();
	    updateVisibleColumns();
	}
	
	public void notifyScrollChanged(){
	    if(timeLayout != null) {
	        int loadingOffset = screenWidth / 2;
            int columnWidth = timeLayout.getColumnWidth();
            int lowerBound = Math.max(scrollX - loadingOffset, 0) / columnWidth;
            int columnsInScreen = Double.valueOf(Math.ceil((1.0 * screenWidth + loadingOffset) / columnWidth)).intValue();
            int upperBound = Math.min(lowerBound + columnsInScreen, timeLayout.getColumnCount());
            int[] columns = new int[Math.max(upperBound - lowerBound + 1, 0)];
            for(int i=0; i < columns.length; i++){
                columns[i] = lowerBound + i; 
            }
            timeLayout.notifyColumns(columns);
        }
	}
	
	public void updateVisibleColumns() {
		if(timeLayout != null) {
			Rect rect = new Rect();
			getHitRect(rect);
			int visibleColumnCnt = Double.valueOf(Math.ceil((rect.right - rect.left) / timeLayout.getColumnWidth())).intValue();
			int leftInvisibleColumnCnt = Double.valueOf(Math.floor(scrollX / timeLayout.getColumnWidth())).intValue();
			int[] visibleColumn = new int[visibleColumnCnt];
			for(int i = 0 ; i < visibleColumnCnt ; i++) {
				visibleColumn[i] = leftInvisibleColumnCnt + i;
			}
			timeLayout.setCurrentVisibleColumns(visibleColumn);
		}
	}
	
	public static void initScreenWidth(DisplayMetrics dm, Display display) {
		screenWidth = display.getWidth() - Dimension.dpToPx(70, dm);
	}
	
}
