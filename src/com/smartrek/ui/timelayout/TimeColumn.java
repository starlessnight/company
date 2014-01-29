package com.smartrek.ui.timelayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.smartrek.activities.R;
import com.smartrek.requests.Request;
import com.smartrek.ui.timelayout.TimeButton.DisplayMode;
import com.smartrek.ui.timelayout.TimeButton.State;
import com.smartrek.utils.Dimension;

/**
 * 
 * 
 */
public final class TimeColumn extends FrameLayout {
	
    private static final int spacingHeight = 7;
    private static final int stripeHieght = 7;
    
	private TimeButton departureTimeButton;
	private TimeButton arrivalTimeButton;
	private ProgressBar progressBar;
	private View bottomSpacing;
	private View bottomStripe;
	
	private long departureTime;
	private long arrivalTime;
	private int timzoneOffset;
	
	private State state = State.Unknown;
	private DisplayMode displayMode = DisplayMode.Time;
	
	private int btnum;
	
	/**
	 * 
	 * @param timelayout Container
	 * @param time
	 * @param btnum
	 * @param before
	 */
	public TimeColumn(TimeLayout timelayout, int btnum, Typeface departureTimeFont, 
	        Typeface arrivalTimeFont, int timzoneOffset) {
		super(timelayout.getContext());
		
		this.timzoneOffset = timzoneOffset;
		this.btnum = btnum;
		
		setTag(btnum);
		
		LinearLayout timeColumnLayout = new LinearLayout(getContext());
		timeColumnLayout.setOrientation(LinearLayout.VERTICAL);

		departureTimeButton = new TimeButton(getContext(), 0, false, departureTimeFont);
		timeColumnLayout.addView(departureTimeButton);
		
		arrivalTimeButton = new TimeButton(getContext(), 1, true, arrivalTimeFont);
		timeColumnLayout.addView(arrivalTimeButton);
		
		bottomSpacing = new View(getContext());
		DisplayMetrics dm = getResources().getDisplayMetrics();
		LinearLayout.LayoutParams bottomSpacingLp = new LinearLayout.LayoutParams(
	        Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(spacingHeight, dm)); 
		bottomSpacing.setLayoutParams(bottomSpacingLp);
	    timeColumnLayout.addView(bottomSpacing);
	    
	    bottomStripe = new View(getContext());
        LinearLayout.LayoutParams bottomStipeLp = new LinearLayout.LayoutParams(
            Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(stripeHieght, dm)); 
        bottomStripe.setLayoutParams(bottomStipeLp);
        bottomStripe.setBackgroundResource(R.color.light_green);
        bottomStripe.setVisibility(View.INVISIBLE);
        timeColumnLayout.addView(bottomStripe);
		
		addView(timeColumnLayout);
		
		progressBar = new ProgressBar(getContext());
		progressBar.setVisibility(View.INVISIBLE);
		
		addView(progressBar);
		
		// The following statement must be called after addView(progressBar); 
		FrameLayout.LayoutParams params = (LayoutParams) progressBar.getLayoutParams();
		params.width = 48;
		params.height = 48;
		params.gravity = Gravity.CENTER;
	}

	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		this.state = state;
		
		departureTimeButton.setState(state);
		arrivalTimeButton.setState(state);
		
		int bgColor;
		if (State.InProgress.equals(state)) {
			progressBar.setVisibility(View.VISIBLE);
			bgColor = TimeButton.IN_PREGRESS_BACKGROUND_COLOR;
		}
		else {
			progressBar.setVisibility(View.INVISIBLE);
			bgColor = TimeButton.IN_PREGRESS_BACKGROUND_COLOR;
		}
		setBackgroundColor(bgColor);
		bottomStripe.setVisibility(State.Selected.equals(state)?View.VISIBLE:View.INVISIBLE);
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
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone(Request.getTimeZone(timzoneOffset)));
		departureTimeButton.setText(btnum == 0?"Now":dateFormat.format(new Date(time)));
		
		postInvalidate();
	}
	
	public long getArrivalTime() {
		return arrivalTime;
	}
	
	public void setArrivalTime(long time) {
		this.arrivalTime = time;
		
		if (time != 0) {
    		if (displayMode.equals(DisplayMode.Duration)) {
    			arrivalTimeButton.setText(String.format("%d m", getDuration()/60));
    		}
    		else {
    		    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    	        dateFormat.setTimeZone(TimeZone.getTimeZone(Request.getTimeZone(timzoneOffset)));
    			arrivalTimeButton.setText(dateFormat.format(new Date(time)));
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