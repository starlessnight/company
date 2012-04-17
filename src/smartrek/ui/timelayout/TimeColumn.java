package smartrek.ui.timelayout;

import smartrek.ui.timelayout.TimeButton.DisplayMode;
import smartrek.ui.timelayout.TimeButton.State;
import android.text.format.Time;
import android.widget.LinearLayout;

/**
 * 
 * 
 */
public final class TimeColumn extends LinearLayout {
	
	private TimeButton departureTimeButton;
	private TimeButton arrivalTimeButton;
	
	private Time departureTime;
	private Time arrivalTime;
	
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
	
	public synchronized void setState(State state) {
		this.state = state;
		
		departureTimeButton.setState(state);
		arrivalTimeButton.setState(state);
	}

	public synchronized void setDisplayMode(DisplayMode displayMode) {
		this.displayMode = displayMode;
	}

	public Time getDepartureTime() {
		return departureTime;
	}
	
	public void setDepartureTime(Time time) {
		this.departureTime = time;
		
		departureTimeButton.setText(time.format("%l:%M%p"));
	}
	
	public Time getArrivalTime() {
		return arrivalTime;
	}
	
	public void setArrivalTime(Time time) {
		this.arrivalTime = time;
		
		if (displayMode.equals(DisplayMode.Duration)) {
			arrivalTimeButton.setText(String.format("%d min", getDuration()/60));
		}
		else {
			arrivalTimeButton.setText(time.format("%l:%M%p"));
		}
	}
	
	/**
	 * Returns (arrival time) - (departure time) in seconds.
	 * 
	 * @return Travel duration in seconds.
	 */
	public int getDuration() {
		return (int) ((arrivalTime.toMillis(false) - departureTime.toMillis(false)) / 1000);
	}
}