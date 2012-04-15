package smartrek.ui.timelayout;

import smartrek.models.Route;
import android.content.Context;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.gridlayout.GridLayout;


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
    
    private TimeLayoutListener timeLayoutListener;

    public interface TimeLayoutListener {
    	public void updateTimeLayout(TimeLayout timeLayout, int column);
    }
    
    public void setTimeLayoutListener(TimeLayoutListener listener) {
    	timeLayoutListener = listener;
    }
    
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
        	timeButton.setWidth(getColumnWidth());
        	addView(timeButton, numboxes+i);
        }
        setButtonDisplayModeInRow(0, TimeButton.DisplayMode.Time);
        //setColumnState(0, TimeButton.State.Selected);

    }
    
    public DisplayMode getDisplayMode() {
    	return displayMode;
    }
    
    public synchronized void setDisplayMode(DisplayMode displayMode) {
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
    
    public synchronized int getSelectedColumn() {
    	return selectedColumn;
    }
    
    public int getColumnWidth() {
    	return 150;
    }
    
    public synchronized TimeButton.State getColumnState(int column) {
    	int cc = getColumnCount();
    	return ((TimeButton) getChildAt(column + cc)).getState();
    }
    
    public synchronized void setColumnState(int column, TimeButton.State state) {
    	int cc = getColumnCount();
    	
    	int k = column % cc;
    	((TimeButton) getChildAt(k)).setState(state);
    	((TimeButton) getChildAt(k + cc)).setState(state);
    }
    
    private synchronized void setButtonDisplayModeInRow(int row, TimeButton.DisplayMode displayMode) {
    	int cc = getColumnCount();
    	for(int i = row * cc; i < (row + 1) * cc; i++) {
    		TimeButton timeButton = (TimeButton) getChildAt(i);
    		timeButton.setDisplayMode(displayMode);
    	}
    }
    
    public synchronized void notifyColumn(int column, boolean visible) {
    	TimeButton.State state = getColumnState(column);
    	if(!TimeButton.State.InProgress.equals(state)) {
    		//Log.d("TimeLayout", String.format("Setting column %d state to InProgress", column));
    		//setColumnState(column, TimeButton.State.InProgress);
    		
    		if(timeLayoutListener != null) {
    			timeLayoutListener.updateTimeLayout(this, column);
    		}
    	}
    }
    
    /**
     * 
     * @param column Column number (zero-based)
     * @param model An instance of a model class
     */
    public synchronized void setModelForColumn(int column, Route model) {
    	models[column] = model;
    	
    	// Get the button on the second row
    	TimeButton timeButton = (TimeButton) getChildAt(getColumnCount() + column);
    	timeButton.setTime(model.getArrivalTime());
    	timeButton.setDuration(model.getDuration());
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
    	
    	Log.d("TimeLayout", "Column state: " + getColumnState(column));
    	
    	if (getColumnState(column).equals(TimeButton.State.None)) {
    	
	        for (int i = 0; i < this.getChildCount(); i++) {
	        	TimeButton button = (TimeButton) getChildAt(i);
	        	if (button.getState().equals(TimeButton.State.Selected)) {
	        		button.setState(TimeButton.State.None);
	        	}
//	        	((TimeButton) getChildAt(i)).setState(TimeButton.State.None);
	        }
	
	        setColumnState(column, TimeButton.State.Selected);
	        
	        this.invalidate(); // TODO: What is this?
	        
	        if(onSelectListener != null) {
	        	selectedColumn = column;
	        	onSelectListener.onSelect(column, (TimeButton) getChildAt(column),
	        			(TimeButton) getChildAt(getColumnCount() + column));
	        }
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
