package smartrek.AdjustableTimeDisplay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.format.Time;
import android.view.Gravity;
import android.widget.TextView;

/*********************************************************************************************************
 * ********************** TimeButton ********************************************
 * 
 * 
 * 
 * @author Tim Olivas
 * 
 *********************************************************************************************************/
public class TimeButton extends TextView {
	
	public enum DisplayMode {
		Time, Duration
	}
	
	private Time time;
	private DisplayMode displayMode;
	
	/**
	 * 
	 * @param timelayout
	 * @param time
	 * @param btnum
	 * @param before
	 */
	public TimeButton(TimeLayout timelayout, Time time, int btnum, TimeButton before, DisplayMode displayMode) {
		super(timelayout.getContext());
		this.displayMode = displayMode;
		this.setOnClickListener(timelayout);
		this.setId(btnum);
		setParams(before);
		setTime(time);
		setGravity(Gravity.CENTER_HORIZONTAL);
	}
	
	/**
	 * 
	 * @param timelayout
	 * @param min
	 * @param sec
	 * @param btnum
	 * @param before
	 */
	public TimeButton(TimeLayout2 timelayout, int min, int sec, int btnum, TimeButton before, DisplayMode displayMode) {
		super(timelayout.getContext());
		this.displayMode = displayMode;
		this.setOnLongClickListener(timelayout);
		this.setId(btnum);
		setParams(before);
		setText(min + " min " + sec + "sec");
		setGravity(Gravity.CENTER_HORIZONTAL);
	}

	/**
	 * 
	 * @param before
	 */
	private void setParams(TimeButton before) {
		this.setPadding(3, 2, 3, 2);
		this.setBackgroundColor(Color.parseColor("#3f3e40"));
		this.setTextColor(Color.WHITE);
	}
	
	/**
	 * 
	 * @param time
	 */
	private void setTime(Time time) {
		this.time = new Time(time);
		
		if(DisplayMode.Time.equals(displayMode)) {
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
			String colon = " : ";
			if(min < 10) {
				colon += "0";
			}
			this.setText("  " + hour + colon + min + AMPM);
		}
		else if(DisplayMode.Duration.equals(displayMode)) {
			setText("...");
		}
		else {
			setText("Unknown");
		}
	}
	
	/**
	 * 
	 */
	public void resetColor(){
		this.setBackgroundColor(Color.parseColor("#3f3e40"));
		this.setTextColor(Color.WHITE);
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