package smartrek.AdjustableTimeDisplay;

import java.util.ArrayList;

import smartrek.activities.RouteActivity;
import smartrek.models.Route;



import android.content.Context;
import android.graphics.Color;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;

import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;

/*********************************************************************************************************
 * 
 * 
 * 
 *********************************************************************************************************/
public class TimeLayout2 extends LinearLayout implements OnLongClickListener {
	
	private RouteActivity map_activity;
	private TimeLayout2 arriveScroll;
	private TimeLayout2 travelScroll;
	private int numBoxes;
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
//	public TimeLayout2(Context context) {
//		super(context);
//		
//		
//	    AdjustableTime atime = new AdjustableTime();
//	    atime.setToNow();
//	    
//	    this.numBoxes = atime.getNumTimeBoxes();
//	    TimeButton temp = null;
//	    
//	    for (int i = 0; i < numBoxes; i++) {
//	    	 TimeButton bt1 = new TimeButton(this, atime, i, temp);
//	    	 if(i == 0) {
//	    		 bt1.setBackgroundColor(Color.parseColor("#cea350"));
//	    		
//	    	 }
//	    	 atime.incBy(15);
//	    	 this.addView(bt1,i);
//	    	 temp = bt1;
//		}
//	}
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public TimeLayout2(Context context, AttributeSet attributes) {
		super(context,attributes);
	}
	
	public void setTimes(ArrayList<Route> routes) {
	    
		TimeButton temp = null;
		
	    for (int i = 0; i < numBoxes; i++) {
	    	 Route route = routes.get(i);
	    	 TimeButton bt1 = new TimeButton(this, route.getMin(),route.getSec(), i, temp);
	    	 if(i == 0) {
	    		 bt1.setBackgroundColor(Color.parseColor("#cea350"));
	    	 }
	    	 this.addView(bt1,i);
	    	 temp = bt1;
		}
	}
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public void setMapActivity(RouteActivity map_activity){
		this.map_activity = map_activity;
	}

	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public void setDependents(TimeLayout2 t1, TimeLayout2 t2){
		arriveScroll = t1;
		travelScroll = t2;
	}
	
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	@Override
	public boolean onLongClick(View v) {
		Time time = ((TimeButton) v).getTime();
		//map_activity.doRoute(null, null, time);
		Log.d("Time Button " + v.getId(), "OnLongClick Registered");
		for (int i = 0; i < this.getChildCount(); i++) {
			((TimeButton) getChildAt(i)).resetColor();
			((TimeButton) travelScroll.getChildAt(i)).resetColor();
			((TimeButton) arriveScroll.getChildAt(i)).resetColor();
		}

		v.setBackgroundColor(Color.parseColor("#cea350"));
		if (travelScroll != null && arriveScroll != null) {
			travelScroll.getChildAt(v.getId()).setBackgroundColor(
					Color.parseColor("#cea350"));
			arriveScroll.getChildAt(v.getId()).setBackgroundColor(
					Color.parseColor("#cea350"));
		}
		this.invalidate();
		return true;
	}

}
