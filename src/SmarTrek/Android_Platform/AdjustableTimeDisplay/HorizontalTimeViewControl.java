package SmarTrek.Android_Platform.AdjustableTimeDisplay;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.HorizontalScrollView;


public class HorizontalTimeViewControl implements OnScrollListener {

	private HorizontalScrollView first;
	private HorizontalScrollView second;
	private HorizontalScrollView third;
	
	public HorizontalTimeViewControl(HorizontalScrollView departtime,
									 HorizontalScrollView arrivetime,
									 HorizontalScrollView traveltime){
		this.first = arrivetime;
		this.second = arrivetime;
		this.third = traveltime;
		
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
		second.scrollBy(first.getScrollX(),0);
		third.scrollBy(first.getScrollX(),0);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}

}
