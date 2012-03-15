package smartrek.AdjustableTimeDisplay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.format.Time;
import android.view.Gravity;
import android.widget.TextView;

/**
 * 
 * 
 * 
 * @author Tim Olivas
 * 
 */
public class TimeButton extends TextView {
	
//	public static final int DEFAULT_BACKGROUND_COLOR = Color.parseColor("#3f3e40");
//	public static final int BACKGROUND
	
	public enum State {
		None, Selected, Disabled;
		
		public int getTextColor() {
			if(Disabled.equals(this)) {
				return Color.parseColor("#C0C0C0");
			}
			else {
				return Color.parseColor("#FFFFFF");
			}
		}
		
		public int getBackgroundColor() {
			if(None.equals(this)) {
				return Color.parseColor("#3f3e40");
			}
			else if(Selected.equals(this)) {
				return Color.parseColor("#cea350");
			}
			else {
				return Color.parseColor("#3f3e40");
			}
		}
	}
	
	public enum DisplayMode {
		Time, Duration
	}
	
	private Time time;
	private int duration;
	
	private State state = State.None;
	private DisplayMode displayMode = DisplayMode.Time;
	
	/**
	 * 
	 * @param timelayout
	 * @param time
	 * @param btnum
	 * @param before
	 */
	public TimeButton(TimeLayout timelayout, int btnum) {
		super(timelayout.getContext());
		setOnClickListener(timelayout);
		setTag(btnum);
		setGravity(Gravity.CENTER_HORIZONTAL);
		setState(State.None);
	}

	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		this.state = state;
		
		setTextColor(state.getTextColor());
		setBackgroundColor(state.getBackgroundColor());
	}

	public void setDisplayMode(DisplayMode displayMode) {
		this.displayMode = displayMode;
		display();
	}

	/**
	 * 
	 * @param time
	 */
	public void setTime(Time time) {
		this.time = time;
		display();
	}
	
	/**
	 * 
	 * @return
	 */
	public Time getTime() {
		return time;
	}
	
	/**
	 * 
	 * @param duration Duration in seconds
	 */
	public void setDuration(int duration) {
		this.duration = duration;
		display();
	}
	
	/**
	 * Displays time or duration in a proper format depending on displayMode.
	 */
	private void display() {
		if(DisplayMode.Time.equals(displayMode) && time != null) {
			int hour = time.hour;
			String AMPM = " AM ";
			if(hour>12) {
				hour = hour-12;
				AMPM = " PM ";
			}
			if(hour == 0) {
				hour = 12;
			}
			int min = time.minute;
			setText(String.format("%d:%02d %s", hour, min, AMPM));
		}
		else if(DisplayMode.Duration.equals(displayMode)) {
			// TODO: Need to consider cases where duration > 3600
			if(duration >= 0) {
				setText(String.format("%d min", Math.round((float)duration/60)));
			}
			else {
				setText("...");
			}
		}
		else {
			setText("Unknown");
		}
	}
	
	/**
	 * 
	 */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rect = new Rect();
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        getLocalVisibleRect(rect);
        canvas.drawRect(rect, paint);       
    }
}