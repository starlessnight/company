package com.smartrek.ui.timelayout;

import android.text.format.Time;
import android.widget.LinearLayout;

import com.smartrek.ui.timelayout.TimeButton.DisplayMode;
import com.smartrek.ui.timelayout.TimeButton.State;

/**
 * 
 * 
 */
public final class TimeColumn extends LinearLayout {
	
	private TimeButton departureTimeButton;
	private TimeButton arrivalTimeButton;
	
	private long departureTime;
	private long arrivalTime;
	
	private State state = State.Unknown;
	private DisplayMode displayMode = DisplayMode.Time;
	
	/**
	 * 
	 * @param timelayout Container
	 * @param time
	 * @param btnum
	 * @param before
	 */
	public TimeColumn(TimeLayout timelayout, int btnum) {
		super(timelayout.getContext());
		
		setOrientation(VERTICAL);
		setTag(btnum);

		departureTimeButton = new TimeButton(getContext());
		addView(departureTimeButton);
		
		arrivalTimeButton = new TimeButton(getContext());
		addView(arrivalTimeButton);
	}

	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		this.state = state;
		
		departureTimeButton.setState(state);
		arrivalTimeButton.setState(state);
	}

	public void setDisplayMode(DisplayMode displayMode) {
		this.displayMode = displayMode;

		setArrivalTime(arrivalTime);
	}

	public long getDepartureTime() {
		return departureTime;
	}
	
	public void setDepartureTime(long time) {
		this.departureTime = time;
		
		Time t = new Time();
		t.set(time);
		departureTimeButton.setText(t.format("%l:%M%p"));
		
		postInvalidate();
	}
	
	public long getArrivalTime() {
		return arrivalTime;
	}
	
	public void setArrivalTime(long time) {
		this.arrivalTime = time;
		
		if (time != 0) {
    		if (displayMode.equals(DisplayMode.Duration)) {
    			arrivalTimeButton.setText(String.format("%d min", getDuration()/60));
    		}
    		else {
    			Time t = new Time();
    			t.set(time);
    			arrivalTimeButton.setText(t.format("%l:%M%p"));
    		}
    		
    		postInvalidate();
		}
	}
	
	/**
	 * Returns (arrival time) - (departure time) in seconds.
	 * 
	 * @return Travel duration in seconds.
	 */
	public int getDuration() {
		return (int) ((arrivalTime - departureTime) / 1000);
	}
}