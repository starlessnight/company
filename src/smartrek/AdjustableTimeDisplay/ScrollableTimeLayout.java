package smartrek.AdjustableTimeDisplay;

import smartrek.ui.ObservableScrollView;
import smartrek.ui.ScrollViewListener;
import android.content.Context;
import android.util.AttributeSet;

/**
 * This is a wrapper around `TimeLayout`. The primary responsibility of this
 * class is to calculate which region of `TimeLayout` is currently visible on
 * screen and notify `TimeLayout` which columns become visible as users scroll
 * this view.
 */
public class ScrollableTimeLayout extends ObservableScrollView implements ScrollViewListener {

	// FIXME: This must be loaded dynamically
	private int width = 450;
	
	private TimeLayout timeLayout;
	
	public ScrollableTimeLayout(Context context, AttributeSet attributes) {
		super(context, attributes);
		setScrollViewListener(this);
	}
	
	public void setTimeLayout(TimeLayout timeLayout) {
		this.timeLayout = timeLayout;
	}

	@Override
	public void onScrollChanged(ObservableScrollView scrollView, int x, int y,	int oldx, int oldy) {
		if(timeLayout != null) {
			int columnWidth = timeLayout.getColumnWidth();
			int upperBound = (width+x)/columnWidth;
			
			timeLayout.notifyColumn(upperBound, true);
		}
	}

}
