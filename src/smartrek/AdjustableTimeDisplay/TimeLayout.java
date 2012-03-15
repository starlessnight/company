package smartrek.AdjustableTimeDisplay;

import smartrek.activities.RouteActivity;
import android.content.Context;
import android.graphics.Color;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

/**
 * This class is a container for multiple TimeButton's.
 *
 */
public class TimeLayout extends LinearLayout implements OnClickListener {
    
    private RouteActivity routeActivity;
    private TimeLayout arriveScroll;
    private TimeLayout travelScroll;
    
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
        
        AdjustableTime atime = new AdjustableTime();
        atime.setToNow();
        
        int numboxes = atime.getNumTimeBoxes();
        TimeButton temp = null;
        
        for (int i = 0; i < numboxes; i++) {
             TimeButton bt1 = new TimeButton(this, atime, i, temp, displayMode);
             if(i == 0) {
                 bt1.setBackgroundColor(Color.parseColor("#cea350"));
             }
             bt1.setWidth(120);
             atime.incrementBy(15);
             this.addView(bt1,i);
             temp = bt1;
        }
        
    }
    
    /**
     * 
     * @param routeActivity
     */
    public void setRouteActivity(RouteActivity routeActivity){
        this.routeActivity = routeActivity;
    }

    /**
     * 
     * @param t1
     * @param t2
     */
    public void setDependents(TimeLayout t1, TimeLayout t2){
        arriveScroll = t1;
        travelScroll = t2;
    }
    

    // FIXME: I think this should be in RrouteActivity
    @Override
    public void onClick(View v) {
        Time time = ((TimeButton) v).getTime();
        Log.d("Time Button " + v.getId(), "OnClick Registered");
        for (int i = 0; i < this.getChildCount(); i++) {
            ((TimeButton) getChildAt(i)).resetColor();
            if(travelScroll != null)
            	((TimeButton) travelScroll.getChildAt(i)).resetColor();
            if(arriveScroll != null)
            	((TimeButton) arriveScroll.getChildAt(i)).resetColor();
        }

        v.setBackgroundColor(Color.parseColor("#cea350"));
        if (travelScroll != null && arriveScroll != null) {
            travelScroll.getChildAt(v.getId()).setBackgroundColor(
                    Color.parseColor("#cea350"));
            arriveScroll.getChildAt(v.getId()).setBackgroundColor(
                    Color.parseColor("#cea350"));
        }
        this.invalidate(); // TODO: What is this?
        routeActivity.doRoute(routeActivity.getOriginCoord(), routeActivity.getDestCoord(), time);
    }

}
