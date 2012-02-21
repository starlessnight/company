package smartrek.AdjustableTimeDisplay;

import smartrek.activities.RouteActivity;
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
public class TimeLayout extends LinearLayout implements OnLongClickListener {
	
	private RouteActivity map_activity;
	private TimeLayout arriveScroll;
	private TimeLayout travelScroll;
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public TimeLayout(Context context) {
		super(context);
		
		
	    AdjustableTime atime = new AdjustableTime();
	    atime.setToNow();
	    
	    int numboxes = atime.getNumTimeBoxes();
	    TimeButton temp = null;
	    
	    for (int i = 0; i < numboxes; i++) {
	    	 TimeButton bt1 = new TimeButton(this, atime, i, temp);
	    	 if(i == 0) {
	    		 bt1.setBackgroundColor(Color.parseColor("#cea350"));
	    		
	    	 }
	    	 atime.incBy(15);
	    	 this.addView(bt1,i);
	    	 temp = bt1;
		}
	}
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public TimeLayout(Context context, AttributeSet attributes) {
		super(context,attributes);
		
	    AdjustableTime atime = new AdjustableTime();
	    atime.setToNow();
	    
	    int numboxes = atime.getNumTimeBoxes();
	    TimeButton temp = null;
	    
	    for (int i = 0; i < numboxes; i++) {
	    	 TimeButton bt1 = new TimeButton(this, atime, i, temp);
	    	 if(i == 0) {
	    		 bt1.setBackgroundColor(Color.parseColor("#cea350"));
	    	 }
	    	 atime.incBy(15);
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
	public void setDependents(TimeLayout t1, TimeLayout t2){
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
		map_activity.doRoute(time);
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
