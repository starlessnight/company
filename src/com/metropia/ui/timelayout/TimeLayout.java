package com.metropia.ui.timelayout;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.metropia.activities.R;
import com.metropia.models.Route;
import com.metropia.ui.timelayout.TimeButton.DisplayMode;
import com.metropia.ui.timelayout.TimeButton.State;
import com.metropia.utils.Font;


/**
 * This class is a container for multiple TimeButton's.
 *
 */
public final class TimeLayout extends LinearLayout implements OnClickListener {
    
    /**
     * Display mode. Default display mode is to show travel time.
     */
    private DisplayMode displayMode = DisplayMode.Arrival;
    
    /**
     * We know tightly-coupled classes are a bad design, but we'll stick with
     * this because we are under a tight time constraint. We'll revisit this
     * issue later.
     */
    private Route[] models = new Route[128];
    
    private int selectedColumn = 0;
    
    private int columnCount;
    
    private TimeLayoutListener timeLayoutListener;
    
    private Typeface lightFont;
    private Typeface mediumFont;
    private Typeface boldFont;
    
    private int timzoneOffset;
    
    private int[] currentVisibleColumns = {-1};
    
    private int preSelectedColumnIndex = -1;

    public interface TimeLayoutListener {
    	public void updateTimeLayout(TimeLayout timeLayout, int column, boolean visible);
//    	public void cancelOtherRouteTask(TimeLayout timeLayout, int column);
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

        lightFont = Font.getLight(getResources().getAssets());
        boldFont = Font.getBold(getResources().getAssets());
        mediumFont = Font.getMedium(getResources().getAssets());
        
        populateViews();

        //setButtonDisplayModeInRow(0, TimeButton.DisplayMode.Time);
        //setColumnState(0, TimeButton.State.Selected);
    }
    
    public void refresh(){
        removeAllViews();
        populateViews();
        setDisplayMode(displayMode);
    }
    
    private void populateViews(){
        AdjustableTime adjustableTime = new AdjustableTime();
        adjustableTime.setToNow();
        
        columnCount = adjustableTime.getNumTimeBoxes();
        
        for (int i = 0; i < columnCount; i++) {
             TimeColumn timeColumn = new TimeColumn(this, i, mediumFont, mediumFont, timzoneOffset);
             timeColumn.setDepartureTime(adjustableTime.initTime().toMillis(false));
             timeColumn.setOnClickListener(this);
             adjustableTime.incrementBy(15);
             addView(timeColumn, i);
        }
        
        setTerminalColumn();
    }
    
    public DisplayMode getDisplayMode() {
    	return displayMode;
    }
    
    public void setDisplayMode(DisplayMode displayMode) {
    	this.displayMode = displayMode;
    	
    	for (int i = 0; i < getChildCount(); i++) {
    		if (!(getChildAt(i) instanceof TimeColumn)) continue;
    		TimeColumn timeColumn = (TimeColumn) getChildAt(i);
    		timeColumn.setDisplayMode(displayMode);
    	}
    }
    
    public int getSelectedColumn() {
    	return selectedColumn;
    }
    
    public int getColumnCount() {
    	return columnCount;
    }
    
    public int getColumnWidth() {
    	return TimeButton.WIDTH;
    }
    
    public State getColumnState(int column) {
    	if (!(getChildAt(column) instanceof TimeColumn)) return null;
        State s = null;
    	TimeColumn col = (TimeColumn) getChildAt(column);
    	if(col != null){
    	    s = col.getState();
    	}
        return s;
    }
    
    public void setColumnState(int column, State state) {
    	if (!(getChildAt(column) instanceof TimeColumn)) return;
    	TimeColumn col = (TimeColumn) getChildAt(column);
    	if(col != null){
    		if(state == State.Selected) {
    			unSelectOthers();
    		}
    	    col.setState(state, true);
    	}
    }

    public void notifyColumns(int[] columns) {
        for(int column : columns){
        	State state = getColumnState(column);
        	if(state != null && !State.InProgress.equals(state)) {
        		//Log.d("TimeLayout", String.format("Setting column %d state to InProgress", column));
        		//setColumnState(column, TimeButton.State.InProgress);
        		
        		if(timeLayoutListener != null) {
        			timeLayoutListener.updateTimeLayout(this, column, true);
        		}
        	}
        }
        for(int i=0; i<columnCount; i++){
            if(!Arrays.asList(ArrayUtils.toObject(columns)).contains(i)){
                if(timeLayoutListener != null) {
                    timeLayoutListener.updateTimeLayout(this, i, false);
                }
            }
        }
    }
    
    /**
     * 
     * @param column Column number (zero-based)
     * @param model An instance of a model class
     */
    public void setModelForColumn(int column, Route model) {
    	models[column] = model;
    	
    	TimeColumn timeButton = (TimeColumn) getChildAt(column);
    	timeButton.setArrivalTime(model.getArrivalTime());
    	timeButton.setMpoint(model.getMpoint());
    	timeButton.setColor(model.getColor());
    }
    
    public long getDepartureTime(int column) {
    	TimeColumn timeButton = (TimeColumn) getChildAt(column);
    	return timeButton.getDepartureTime();
    }
    
    public long getSelectedDepartureTime() {
    	return getDepartureTime(selectedColumn);
    }
    
    public View getSelectedTimeButton() {
    	return getChildAt(selectedColumn);
    }

    @Override
    public void onClick(View v) {
    	int column = ((Integer) v.getTag());
    	
    	Log.d("TimeLayout", "Column state: " + getColumnState(column));
    	
    	if (getColumnState(column).equals(State.None)) {
    	
	        unSelectOthers();
	
//	        setColumnState(column, State.Selected);
	        
	        this.postInvalidate(); // TODO: What is this?
	        
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

    public int getTimzoneOffset() {
        return timzoneOffset;
    }

    public void setTimzoneOffset(int timzoneOffset) {
        if(this.timzoneOffset != timzoneOffset){
            this.timzoneOffset = timzoneOffset;
            refresh();
        }
    }

	public void setCurrentVisibleColumns(int[] currentVisibleColumns) {
		this.currentVisibleColumns = currentVisibleColumns;
	}
	
	public void refreshAllColumns() {
		for(int idx = 0 ; idx < getColumnCount() ; idx++) {
			TimeColumn timeColumn = (TimeColumn) getChildAt(idx);
			if(timeColumn != null) {
				timeColumn.setState(State.Unknown, idx == selectedColumn);
			}
		}
	}
	
	public void notifySelectColumn(int loadedColumn) {
		if(loadedColumn == preSelectedColumnIndex && 
				State.None.equals(getColumnState(preSelectedColumnIndex))) {
    	    for (int i = 0; i < getChildCount(); i++) {
    	    	if (!(getChildAt(i) instanceof TimeColumn)) continue;
    	      	TimeColumn timeColumn = (TimeColumn) getChildAt(i);
    	       	if (timeColumn.getState().equals(State.Selected)) {
    	       		timeColumn.setState(State.None, Arrays.asList(ArrayUtils.toObject(currentVisibleColumns)).contains(i));
    	       	}
    	    }
    	
    	    this.postInvalidate(); // TODO: What is this?
    	        
    	    if(onSelectListener != null) {
    	      	selectedColumn = preSelectedColumnIndex;
    	       	onSelectListener.onSelect(preSelectedColumnIndex, (TimeColumn) getChildAt(preSelectedColumnIndex));
    	    }
    	    preSelectedColumnIndex = -1;
		}
	}
	
	public void preSelectColumn(int column) {
		preSelectedColumnIndex = column;
		notifySelectColumn(column);
	}
	
	private synchronized void unSelectOthers() {
		for (int i = 0; i < this.getChildCount(); i++) {
			if (!(getChildAt(i) instanceof TimeColumn)) continue;
        	TimeColumn timeColumn = (TimeColumn) getChildAt(i);
        	if (timeColumn.getState().equals(State.Selected)) {
        		timeColumn.setState(State.None, Arrays.asList(ArrayUtils.toObject(currentVisibleColumns)).contains(i));
        	}
        }
	}
	
	public int getColumnIndexFromDepartureTime(long departureTime) {
		int columnIdx = -1;
		for (int i = 0; i < getChildCount() && columnIdx < 0; i++) {
			if (!(getChildAt(i) instanceof TimeColumn)) continue;
    		TimeColumn timeColumn = (TimeColumn) getChildAt(i);
    		if(timeColumn.getDepartureTime() == departureTime) {
    			columnIdx = i;
    		}
    	}
		return columnIdx;
	}
	

    public void setTerminalColumn() {
    	
    	ImageView v = new ImageView(getContext());
        v.setBackgroundColor(getContext().getResources().getColor(R.color.metropia_green));
        v.setImageResource(R.drawable.letsgo_icon_unpredictable);
        v.setPadding(TimeButton.WIDTH/4, 0, TimeButton.WIDTH/4, 0);
        addView(v);
        v.getLayoutParams().width = TimeButton.WIDTH*2;
    }
	
//	public void cancelOtherRouteTask() {
//		if(timeLayoutListener != null) {
//			timeLayoutListener.cancelOtherRouteTask(this, selectedColumn);
//		}
//	}
	
}
