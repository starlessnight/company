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
 */
public class TimeButton extends TextView {
	
//	public static final int DEFAULT_BACKGROUND_COLOR = Color.parseColor("#3f3e40");
//	public static final int BACKGROUND
	
	public enum State {
		None, InProgress, Selected, Disabled;
		
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
	
	private boolean redraw = false;
	
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
		setText("Unknown");
	}

	public State getState() {
		return state;
	}
	
	public synchronized void setState(State state) {
		this.state = state;
		
		// Cause onDraw() to be called at some point in the future
		this.redraw = true;
		postInvalidate();
	}

	public synchronized void setDisplayMode(DisplayMode displayMode) {
		this.displayMode = displayMode;
		this.redraw = true;
		postInvalidate();
	}

	/**
	 * 
	 * @param time
	 */
	public synchronized void setTime(Time time) {
		this.time = time;
		this.redraw = true;
		postInvalidate();
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
	public synchronized void setDuration(int duration) {
		this.duration = duration;
		this.redraw = true;
		postInvalidate();
	}
	
	/**
	 * Displays time or duration in a proper format depending on displayMode.
	 */
	private synchronized void display() {
		setTextColor(state.getTextColor());
		setBackgroundColor(state.getBackgroundColor());
		
		if(State.InProgress.equals(state)) {
			setText("...");
		}
		
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
		}
		else {
			setText("Unknown");
		}
		
		redraw = false;
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
        
        if(redraw) {
        	display();
        }
    }
}