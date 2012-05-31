package com.smartrek.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;


public class ObservableScrollView extends HorizontalScrollView {

	private ScrollViewListener scrollViewListener = null;

    public ObservableScrollView(Context context, AttributeSet attributes) {
    	super(context, attributes);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if(scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }
}
