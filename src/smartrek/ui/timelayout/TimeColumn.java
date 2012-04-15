package smartrek.ui.timelayout;

import smartrek.ui.timelayout.TimeButton.State;
import android.text.format.Time;
import android.widget.LinearLayout;

/**
 * 
 * 
 */
public final class TimeColumn extends LinearLayout {
	
//	public static final int DEFAULT_BACKGROUND_COLOR = Color.parseColor("#3f3e40");
//	public static final int BACKGROUND
	

	public enum DisplayMode {
		Time, Duration
	}
	
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
		
//		SimpleDateFormat format = new SimpleDateFormat("hh:mm aa");
//		arrivalTimeButton.setText(format.format(time));
	}
	
	/**
	 * Returns (arrival time) - (departure time) in seconds.
	 * 
	 * @return Travel duration in seconds.
	 */
	public int getDuration() {
		// FIXME: Not implemented yet
		return 0;
	}
}