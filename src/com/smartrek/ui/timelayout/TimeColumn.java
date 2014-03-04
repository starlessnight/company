package com.smartrek.ui.timelayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.requests.Request;
import com.smartrek.ui.timelayout.TimeButton.DisplayMode;
import com.smartrek.ui.timelayout.TimeButton.State;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.Font;

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
	private View buttonSpacing;
	private View  topSpacing;
	
	private TextView mpointView;
	
	private int mpoint;
	private String color;
	
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

		DisplayMetrics dm = getResources().getDisplayMetrics();
		topSpacing = new View(getContext());
        LinearLayout.LayoutParams topSpacingLp = new LinearLayout.LayoutParams(
            Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(15, dm)); 
        topSpacing.setLayoutParams(topSpacingLp);
        topSpacing.setBackgroundResource(R.drawable.timetable_scale);
        timeColumnLayout.addView(topSpacing);
		
		departureTimeButton = new TimeButton(getContext(), 0, true, departureTimeFont);
		departureTimeButton.setGravity(Gravity.TOP|Gravity.CENTER);
		departureTimeButton.setPadding(0, 0, 0, 0);
		departureTimeButton.setHeight(Dimension.dpToPx(15, dm));
		timeColumnLayout.addView(departureTimeButton);
		
		buttonSpacing = new View(getContext());
        LinearLayout.LayoutParams buttonSpacingLp = new LinearLayout.LayoutParams(
            Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(5, dm)); 
        buttonSpacing.setLayoutParams(buttonSpacingLp);
        timeColumnLayout.addView(buttonSpacing);
		
		arrivalTimeButton = new TimeButton(getContext(), 1, true, arrivalTimeFont);
		timeColumnLayout.addView(arrivalTimeButton);
		
		bottomSpacing = new View(getContext());
		LinearLayout.LayoutParams bottomSpacingLp = new LinearLayout.LayoutParams(
	        Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(spacingHeight, dm)); 
		bottomSpacing.setLayoutParams(bottomSpacingLp);
	    timeColumnLayout.addView(bottomSpacing);
        
        mpointView = new TextView(getContext());
        mpointView.setTextColor(Color.parseColor("#606163"));
        mpointView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Dimension.dpToPx(TimeButton.largeFont, dm));
        mpointView.setIncludeFontPadding(false);
        mpointView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
        LinearLayout.LayoutParams mpointViewLp = new LinearLayout.LayoutParams(
            Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(25, dm)); 
        mpointView.setLayoutParams(mpointViewLp);
        Font.setTypeface(arrivalTimeFont, mpointView);
        timeColumnLayout.addView(mpointView);
		
        bottomStripe = new View(getContext());
        LinearLayout.LayoutParams bottomStipeLp = new LinearLayout.LayoutParams(
            Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(stripeHieght, dm));
        int stripeMargin = Dimension.dpToPx(2, getResources().getDisplayMetrics());
        bottomStipeLp.leftMargin = stripeMargin;
        bottomStipeLp.rightMargin = stripeMargin;
        bottomStripe.setLayoutParams(bottomStipeLp);
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
		
		setBackgroundColor(TimeButton.IN_PREGRESS_BACKGROUND_COLOR);
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
		LinearLayout.LayoutParams stripeLp = (LinearLayout.LayoutParams) bottomStripe.getLayoutParams();
		boolean selected = State.Selected.equals(state);
		DisplayMetrics dm = getResources().getDisplayMetrics();
        int stripeMargin = Dimension.dpToPx(2, dm);
		stripeLp.leftMargin = selected?0:stripeMargin;
		stripeLp.rightMargin = selected?0:stripeMargin;
		int width = Dimension.dpToPx(TimeButton.WIDTH, dm) - (selected?0:stripeMargin*2);
		stripeLp.width = width;
		bottomStripe.setLayoutParams(stripeLp);
		if(color != null){
    		mpointView.setBackgroundColor(selected?Color.parseColor(color):bgColor);
    		bottomSpacing.setBackgroundColor(selected?Color.parseColor(color):bgColor);
            arrivalTimeButton.setBackgroundColor(selected?Color.parseColor(color):bgColor);
		}
        int textColor = Color.parseColor("#606163");
        mpointView.setTextColor(selected?Color.WHITE:textColor);
        arrivalTimeButton.setTextColor(selected?Color.WHITE:textColor);
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
    			arrivalTimeButton.setText(getFormattedDuration(getDuration()));
    		}
    		else {
    			arrivalTimeButton.setText(formatTime(time, timzoneOffset));
    		}
    		
    		postInvalidate();
		}
	}
	
	public static String getFormattedDuration(int duration){
	    return String.format("%d min", duration/60);
	}
	
	public static String formatTime(long time, int timzoneOffset){
	    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone(Request.getTimeZone(timzoneOffset)));
        return dateFormat.format(new Date(time));
	}
	
	public static String formatTime(long time){
        return formatTime(time, 0);
    }
	
	/**
	 * Returns (arrival time) - (departure time) in seconds.
	 * 
	 * @return Travel duration in seconds.
	 */
	public int getDuration() {
		return (int) ((arrivalTime - departureTime) / 1000);
	}

    public int getMpoint() {
        return mpoint;
    }

    public void setMpoint(int mpoint) {
        this.mpoint = mpoint;
        
        mpointView.setText(String.valueOf(mpoint));
        postInvalidate();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        if(color != null){
            bottomStripe.setBackgroundColor(Color.parseColor(color));
            postInvalidate();
        }
    }
}