package com.metropia.ui.timelayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.metropia.requests.Request;
import com.metropia.ui.timelayout.TimeButton.DisplayMode;
import com.metropia.ui.timelayout.TimeButton.State;
import com.metropia.utils.Dimension;
import com.metropia.utils.Font;
import com.metropia.activities.R;

/**
 * 
 * 
 */
public final class TimeColumn extends FrameLayout {
	
	private TimeButton departureTimeButton;
	private TimeButton arriveButton;
	private TimeButton durationButton;
	private ProgressBar progressBar;
	private View buttonSpacing;
	private FrameLayout timeButtonLayout;
	private View mask;
	
	private TextView mpointView;
	
	private int mpoint;
	private String color;
	
	private long departureTime;
	private long arrivalTime;
	private int timzoneOffset;
	
	private State state = State.Unknown;
	private DisplayMode displayMode = DisplayMode.Arrival;
	
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
		
		DisplayMetrics dm = getResources().getDisplayMetrics();
		FrameLayout timeColumn = new FrameLayout(getContext());
		
		mask = new View(getContext());
        int maskHeight = TimeButton.HEIGHT * 3 + TimeButton.FIRST_ROW_HEIGHT /* - Dimension.dpToPx(1, dm)*/;                
        FrameLayout.LayoutParams maskLp = new FrameLayout.LayoutParams(TimeButton.WIDTH - 2 * Dimension.dpToPx(1, dm), maskHeight);
        maskLp.gravity = Gravity.TOP|Gravity.CENTER_HORIZONTAL;
        maskLp.leftMargin = Dimension.dpToPx(1, dm);
        maskLp.rightMargin = Dimension.dpToPx(1, dm);
        maskLp.topMargin = TimeButton.HEIGHT * 2 + TimeButton.FIRST_ROW_HEIGHT - Dimension.dpToPx(1, dm);
        mask.setLayoutParams(maskLp);
        timeColumn.addView(mask);
		
		LinearLayout timeColumnLayout = new LinearLayout(getContext());
		timeColumnLayout.setOrientation(LinearLayout.VERTICAL);

		
		FrameLayout departureTimeLayout = new FrameLayout(getContext());
		FrameLayout.LayoutParams departureTimeLayoutLp = new FrameLayout.LayoutParams(TimeButton.WIDTH, TimeButton.FIRST_ROW_HEIGHT - Dimension.dpToPx(1, dm));
		departureTimeLayout.setLayoutParams(departureTimeLayoutLp);
		
		ImageView backgroundImg = new ImageView(getContext());
		FrameLayout.LayoutParams imgLp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
		backgroundImg.setLayoutParams(imgLp);
		backgroundImg.setScaleType(ScaleType.FIT_XY);
		backgroundImg.setBackgroundResource(R.drawable.time_table_gradiun);
		departureTimeLayout.addView(backgroundImg);
		
		departureTimeButton = new TimeButton(getContext(), 0, true, departureTimeFont);
		departureTimeButton.setGravity(Gravity.CENTER);
		departureTimeButton.setPadding(0, 0, 0, 0);
		departureTimeButton.setHeight(TimeButton.FIRST_ROW_HEIGHT - Dimension.dpToPx(1, dm));
		departureTimeLayout.addView(departureTimeButton);
		timeColumnLayout.addView(departureTimeLayout);
		
		buttonSpacing = new View(getContext());
        LinearLayout.LayoutParams buttonSpacingLp = new LinearLayout.LayoutParams(TimeButton.WIDTH, Dimension.dpToPx(1, dm)); 
        buttonSpacing.setLayoutParams(buttonSpacingLp);
        timeColumnLayout.addView(buttonSpacing);
		
        timeButtonLayout = new FrameLayout(getContext());
        LinearLayout.LayoutParams timeButtonLp = new LinearLayout.LayoutParams(TimeButton.WIDTH, LayoutParams.WRAP_CONTENT, 0);
        timeButtonLayout.setLayoutParams(timeButtonLp);
        
        LinearLayout lowerPart = new LinearLayout(getContext());
        LinearLayout.LayoutParams lowerPartLp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        lowerPart.setLayoutParams(lowerPartLp);
        lowerPart.setOrientation(LinearLayout.VERTICAL);
        
        arriveButton = new TimeButton(getContext(), 1, true, arrivalTimeFont);
        lowerPart.addView(arriveButton);
        
		durationButton = new TimeButton(getContext(), 1, true, arrivalTimeFont);
		lowerPart.addView(durationButton);
		
        mpointView = new TextView(getContext());
        mpointView.setTextColor(Color.WHITE);
        mpointView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Dimension.dpToPx(19, dm));
        mpointView.setIncludeFontPadding(false);
        mpointView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams mpointViewLp = new LinearLayout.LayoutParams(TimeButton.WIDTH, TimeButton.HEIGHT); 
        mpointView.setLayoutParams(mpointViewLp);
        Font.setTypeface(arrivalTimeFont, mpointView);
        lowerPart.addView(mpointView);
		
        timeButtonLayout.addView(lowerPart);
        
        timeColumnLayout.addView(timeButtonLayout);
        
        timeColumn.addView(timeColumnLayout);
        
        addView(timeColumn);
		
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
	
	private static final long animationDuration = 400;
	
	public void setState(State state, boolean animation) {
		String originalState = this.state.name();
		this.state = state;
		
		departureTimeButton.setState(state);
		arriveButton.setState(state);
		durationButton.setState(state);
		
		
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
		boolean selected = State.Selected.equals(state);
		DisplayMetrics dm = getResources().getDisplayMetrics();
        if(color != null){
        	if(selected) {
        		FrameLayout.LayoutParams maskLp = (LayoutParams) mask.getLayoutParams();
        		maskLp.topMargin = 0;
        		mask.setLayoutParams(maskLp);
	        	TranslateAnimation slideUp = new TranslateAnimation(0, 0, TimeButton.HEIGHT * 2 + TimeButton.FIRST_ROW_HEIGHT - Dimension.dpToPx(1, dm), 0);
	        	slideUp.setDuration(animationDuration);
	        	slideUp.setFillAfter(true);
	        	mask.startAnimation(slideUp);
        	}
        	else if(State.Selected.name().equals(originalState)){
        		TranslateAnimation slideDown = new TranslateAnimation(0, 0, 0, TimeButton.HEIGHT * 2 + TimeButton.FIRST_ROW_HEIGHT - Dimension.dpToPx(1, dm));
            	slideDown.setDuration(animation?animationDuration:0);
            	slideDown.setFillAfter(true);
            	mask.startAnimation(slideDown);
        	}
        }
        int textColor = Color.parseColor("#606163");
        departureTimeButton.setTextColor(selected?Color.WHITE:textColor);
        arriveButton.setTextColor(selected?Color.WHITE:textColor);
        durationButton.setTextColor(selected?Color.WHITE:textColor);
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
		
		departureTimeButton.setText(btnum == 0?"Now":formatTimeDesc(StringUtils.lowerCase(formatTime(time, timzoneOffset))));
		
		postInvalidate();
	}
	
	public long getArrivalTime() {
		return arrivalTime;
	}
	
	public void setArrivalTime(long time) {
		this.arrivalTime = time;
		
		if (time != 0) {
		    arriveButton.setText(formatTimeDesc(StringUtils.lowerCase(formatTime(time, timzoneOffset))));
			durationButton.setText(formatDurationTime(getFormattedDuration(getDuration())));
    		
    		postInvalidate();
		}
	}
	
	public static String getFormattedDuration(int duration){
	    return String.format("%dm", duration/60);
	}
	
	public static String formatTime(long time, int timzoneOffset){
	    SimpleDateFormat dateFormat = new SimpleDateFormat("h:mma", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone(Request.getTimeZone(timzoneOffset)));
        return dateFormat.format(new Date(time));
	}
	
	public static String formatTime(long time){
        return formatTime(time, 0);
    }
	
	private SpannableString formatDurationTime(String durationTime) {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		SpannableString durationSpan = SpannableString.valueOf(durationTime);
		durationSpan.setSpan(new AbsoluteSizeSpan(Dimension.dpToPx(12, dm)), durationTime.length()-1,
				durationTime.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return durationSpan;
	}
	
	private SpannableString formatTimeDesc(String time) {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		SpannableString timeSpan = SpannableString.valueOf(time);
		timeSpan.setSpan(new AbsoluteSizeSpan(Dimension.dpToPx(8, dm)), time.length() - 2,
				time.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return timeSpan;
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
        	mask.setBackgroundColor(Color.parseColor(color));
            postInvalidate();
        }
    }
}