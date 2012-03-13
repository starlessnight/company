package smartrek.AdjustableTimeDisplay;

import smartrek.activities.RouteActivity;
import android.content.Context;
import android.graphics.Color;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridLayout;

/**
 * This class is a container for multiple TimeButton's.
 *
 */
public class TimeLayout extends GridLayout implements OnClickListener {
    
	public enum DisplayMode {
		TravelTime, ArrivalTime
	}
	
	/**
	 * @deprecated
	 */
    private RouteActivity routeActivity;
    
    /**
     * Display mode. Default display mode is to show travel time.
     */
    private DisplayMode displayMode = DisplayMode.TravelTime;
    
//    /**
//     * 
//     * @param context
//     */
//    public TimeLayout(Context context) {
//        super(context);
//        
//        
//        AdjustableTime atime = new AdjustableTime();
//        atime.setToNow();
//        
//        int numboxes = atime.getNumTimeBoxes();
//        TimeButton temp = null;
//        
//        for (int i = 0; i < numboxes; i++) {
//             TimeButton bt1 = new TimeButton(this, atime, i, temp);
//             if(i == 0) {
//                 bt1.setBackgroundColor(Color.parseColor("#cea350"));
//             }
//             atime.incrementBy(15);
//             this.addView(bt1,i);
//             temp = bt1;
//        }
//    }
    
    /**
     * 
     * @param context
     * @param attributes
     */
    public TimeLayout(Context context, AttributeSet attributes) {
        super(context, attributes);
        
        String display = attributes.getAttributeValue(null, "display");
        TimeButton.DisplayMode displayMode = display.equals("duration") ? TimeButton.DisplayMode.Duration : TimeButton.DisplayMode.Time;
        
        AdjustableTime adjustableTime = new AdjustableTime();
        adjustableTime.setToNow();
        
        int numboxes = adjustableTime.getNumTimeBoxes();
        TimeButton temp = null;
        
        setColumnCount(numboxes);
        
        for (int i = 0; i < numboxes*2; i++) {
             TimeButton bt1 = new TimeButton(this, adjustableTime, i, temp, displayMode);
             bt1.setWidth(140);
             adjustableTime.incrementBy(15);
             addView(bt1,i);
             temp = bt1;
        }
        
        setColumnState(0, TimeButton.State.Selected);
    }
    
    public DisplayMode getDisplayMode() {
    	return displayMode;
    }
    
    public void setDisplayMode(DisplayMode displayMode) {
    	this.displayMode = displayMode;
    	
    }
    
    public void setColumnState(int column, TimeButton.State state) {
    	int cc = getColumnCount();
    	
    	int k = column % cc;
    	((TimeButton) getChildAt(k)).setState(state);
    	((TimeButton) getChildAt(k + cc)).setState(state);
    }
    
    /**
     * 
     * @param routeActivity
     */
    public void setRouteActivity(RouteActivity routeActivity){
        this.routeActivity = routeActivity;
    }


    // FIXME: I think this should be in RrouteActivity
    @Override
    public void onClick(View v) {
    	// FIXME: This is very hack-ish
    	int column = v.getId() % getColumnCount();
    	
        Time time = ((TimeButton) v).getTime();
        Log.d("Time Button " + v.getId(), "OnClick Registered");
        
        for (int i = 0; i < this.getChildCount(); i++) {
        	((TimeButton) getChildAt(i)).setState(TimeButton.State.None);
        }

        setColumnState(column, TimeButton.State.Selected);
        
        this.invalidate(); // TODO: What is this?
//        routeActivity.doRoute(routeActivity.getOriginCoord(), routeActivity.getDestCoord(), time);
    }

}
