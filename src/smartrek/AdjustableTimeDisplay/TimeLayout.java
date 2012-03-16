package smartrek.AdjustableTimeDisplay;

import smartrek.models.Route;
import android.content.Context;
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
     * Display mode. Default display mode is to show travel time.
     */
    private DisplayMode displayMode = DisplayMode.TravelTime;
    
    /**
     * We know tightly-coupled classes are a bad design, but we'll stick with
     * this because we are under a tight time constraint. We'll revisit this
     * issue later.
     */
    private Route[] models = new Route[128];
    
    private int selectedColumn = 0;
    
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

        AdjustableTime adjustableTime = new AdjustableTime();
        adjustableTime.setToNow();
        
        int numboxes = adjustableTime.getNumTimeBoxes();
        
        setColumnCount(numboxes);
        
        for (int i = 0; i < numboxes; i++) {
             TimeButton timeButton = new TimeButton(this, i);
             timeButton.setTime(adjustableTime.initTime());
             adjustableTime.incrementBy(15);
             timeButton.setWidth(150);
             addView(timeButton, i);
        }
        for (int i = 0; i < numboxes; i++) {
        	TimeButton timeButton = new TimeButton(this, numboxes+i);
        	timeButton.setDuration(-1);
        	timeButton.setWidth(150);
        	addView(timeButton, numboxes+i);
        }
        setButtonDisplayModeInRow(0, TimeButton.DisplayMode.Time);
        setColumnState(0, TimeButton.State.Selected);
    }
    
    public DisplayMode getDisplayMode() {
    	return displayMode;
    }
    
    public void setDisplayMode(DisplayMode displayMode) {
    	this.displayMode = displayMode;
    	
    	if(DisplayMode.TravelTime.equals(displayMode)) {
    		setButtonDisplayModeInRow(1, TimeButton.DisplayMode.Duration);
    	}
    	else if(DisplayMode.ArrivalTime.equals(displayMode)) {
    		setButtonDisplayModeInRow(1, TimeButton.DisplayMode.Time);
    	}
    	else {
    		Log.d("TimeLayout", "Unknown display mode");
    	}
    }
    
    public int getSelectedColumn() {
    	return selectedColumn;
    }
    
    private void setColumnState(int column, TimeButton.State state) {
    	int cc = getColumnCount();
    	
    	int k = column % cc;
    	((TimeButton) getChildAt(k)).setState(state);
    	((TimeButton) getChildAt(k + cc)).setState(state);
    }
    
    private void setButtonDisplayModeInRow(int row, TimeButton.DisplayMode displayMode) {
    	int cc = getColumnCount();
    	for(int i = row * cc; i < (row + 1) * cc; i++) {
    		TimeButton timeButton = (TimeButton) getChildAt(i);
    		timeButton.setDisplayMode(displayMode);
    	}
    }
    
    /**
     * 
     * @param column Column number (zero-based)
     * @param model An instance of a model class
     */
    public void setModelForColumn(int column, Route model) {
    	models[column] = model;
    	
    	// Get the button on the second row
    	TimeButton timeButton = (TimeButton) getChildAt(getColumnCount() + column);
    	timeButton.setTime(model.getArrivalTime());
    	timeButton.setDuration(model.getDuration());
    	
    	Log.d("TimeLayout", "duration = " + model.getDuration());
    }
    
    public Time getDepartureTime(int column) {
    	TimeButton timeButton = (TimeButton) getChildAt(column);
    	return timeButton.getTime();
    }
    
    public Time getSelectedDepartureTime() {
    	return getDepartureTime(selectedColumn);
    }

    @Override
    public void onClick(View v) {
    	int column = ((Integer) v.getTag()) % getColumnCount();
    	
        for (int i = 0; i < this.getChildCount(); i++) {
        	((TimeButton) getChildAt(i)).setState(TimeButton.State.None);
        }

        setColumnState(column, TimeButton.State.Selected);
        
        this.invalidate(); // TODO: What is this?
        
        if(onSelectListener != null) {
        	selectedColumn = column;
        	onSelectListener.onSelect(column, (TimeButton) getChildAt(column),
        			(TimeButton) getChildAt(getColumnCount() + column));
        }
    }

    private TimeLayoutOnSelectListener onSelectListener;
    
    /**
     * Unlike other common practices in Java, TimeLayout supports only one
     * listener. And, thus, the method name is 'setListener' instead of
     * 'addListener'.
     * 
     * @param listener
     */
    public void setOnSelectListener(TimeLayoutOnSelectListener listener) {
    	onSelectListener = listener;
    }
    
    public interface TimeLayoutOnSelectListener {
    	/**
    	 * 
    	 * @param column Selected column
    	 * @param timeButton1 TimeButton on the first row
    	 * @param timeButton2 TimeButton on the second row
    	 */
    	public void onSelect(int column, TimeButton timeButton1, TimeButton timeButton2);
    }
}
