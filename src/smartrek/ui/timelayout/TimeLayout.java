package smartrek.ui.timelayout;

import smartrek.models.Route;
import smartrek.ui.timelayout.TimeButton.DisplayMode;
import smartrek.ui.timelayout.TimeButton.State;
import android.content.Context;
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
public final class TimeLayout extends LinearLayout implements OnClickListener {
    
    /**
     * Display mode. Default display mode is to show travel time.
     */
    private DisplayMode displayMode = DisplayMode.Time;
    
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
        
        setOrientation(HORIZONTAL);

        AdjustableTime adjustableTime = new AdjustableTime();
        adjustableTime.setToNow();
        
        int numboxes = adjustableTime.getNumTimeBoxes();
        
        for (int i = 0; i < numboxes; i++) {
             TimeColumn timeColumn = new TimeColumn(this, i);
             timeColumn.setDepartureTime(adjustableTime.initTime());
             timeColumn.setOnClickListener(this);
             adjustableTime.incrementBy(15);
             addView(timeColumn, i);
        }

        //setButtonDisplayModeInRow(0, TimeButton.DisplayMode.Time);
        //setColumnState(0, TimeButton.State.Selected);
    }
    
    public DisplayMode getDisplayMode() {
    	return displayMode;
    }
    
    public synchronized void setDisplayMode(DisplayMode displayMode) {
    	this.displayMode = displayMode;
    	
    	for (int i = 0; i < getChildCount(); i++) {
    		TimeColumn timeColumn = (TimeColumn) getChildAt(i);
    		timeColumn.setDisplayMode(displayMode);
    	}
    }
    
    public synchronized int getSelectedColumn() {
    	return selectedColumn;
    }
    
    public int getColumnWidth() {
    	return 150;
    }
    
    public synchronized State getColumnState(int column) {
    	return ((TimeColumn) getChildAt(column)).getState();
    }
    
    public synchronized void setColumnState(int column, State state) {
    	((TimeColumn) getChildAt(column)).setState(state);
    }

    public synchronized void notifyColumn(int column, boolean visible) {
    	State state = getColumnState(column);
    	if(!State.InProgress.equals(state)) {
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
    	
    	TimeColumn timeButton = (TimeColumn) getChildAt(column);
    	timeButton.setArrivalTime(model.getArrivalTime());
    }
    
    public Time getDepartureTime(int column) {
    	TimeColumn timeButton = (TimeColumn) getChildAt(column);
    	return timeButton.getDepartureTime();
    }
    
    public Time getSelectedDepartureTime() {
    	return getDepartureTime(selectedColumn);
    }

    @Override
    public void onClick(View v) {
    	int column = ((Integer) v.getTag());
    	
    	Log.d("TimeLayout", "Column state: " + getColumnState(column));
    	
    	if (getColumnState(column).equals(State.None)) {
    	
	        for (int i = 0; i < this.getChildCount(); i++) {
	        	TimeColumn timeColumn = (TimeColumn) getChildAt(i);
	        	if (timeColumn.getState().equals(State.Selected)) {
	        		timeColumn.setState(State.None);
	        	}
//	        	((TimeButton) getChildAt(i)).setState(TimeButton.State.None);
	        }
	
	        setColumnState(column, State.Selected);
	        
	        this.invalidate(); // TODO: What is this?
	        
	        if(onSelectListener != null) {
	        	selectedColumn = column;
	        	onSelectListener.onSelect(column, (TimeColumn) getChildAt(column));
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
    	public void onSelect(int column, TimeColumn timeButton);
    }
}
