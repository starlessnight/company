package com.smartrek.ui.timelayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.graphics.Color;
import android.graphics.Typeface;
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
	private TimeButton arriveButton;
	private TimeButton durationButton;
	private ProgressBar progressBar;
	private View bottomSpacing;
	private View bottomStripe;
	private View buttonSpacing;
	private View  topSpacing;
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
		
		LinearLayout timeColumnLayout = new LinearLayout(getContext());
		timeColumnLayout.setOrientation(LinearLayout.VERTICAL);

		DisplayMetrics dm = getResources().getDisplayMetrics();
//		topSpacing = new View(getContext());
//        LinearLayout.LayoutParams topSpacingLp = new LinearLayout.LayoutParams(
//            Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(15, dm)); 
//        topSpacing.setLayoutParams(topSpacingLp);
//        topSpacing.setBackgroundResource(R.drawable.timetable_scale);
//        timeColumnLayout.addView(topSpacing);
		
		FrameLayout departureTimeLayout = new FrameLayout(getContext());
		FrameLayout.LayoutParams departureTimeLayoutLp = new FrameLayout.LayoutParams(Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(34, dm));
		departureTimeLayout.setLayoutParams(departureTimeLayoutLp);
		departureTimeLayout.setBackgroundColor(Color.parseColor("#AAB3B3B3"));
		
		ImageView backgroundImg = new ImageView(getContext());
		FrameLayout.LayoutParams imgLp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
		backgroundImg.setLayoutParams(imgLp);
		backgroundImg.setScaleType(ScaleType.FIT_XY);
		backgroundImg.setBackgroundResource(R.drawable.time_table_gradiun);
		departureTimeLayout.addView(backgroundImg);
		
		departureTimeButton = new TimeButton(getContext(), 0, true, departureTimeFont);
		departureTimeButton.setGravity(Gravity.CENTER);
		departureTimeButton.setPadding(0, 0, 0, 0);
		departureTimeButton.setHeight(Dimension.dpToPx(34, dm));
		departureTimeLayout.addView(departureTimeButton);
		timeColumnLayout.addView(departureTimeLayout);
		
		buttonSpacing = new View(getContext());
        LinearLayout.LayoutParams buttonSpacingLp = new LinearLayout.LayoutParams(
            Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(1, dm)); 
        buttonSpacing.setLayoutParams(buttonSpacingLp);
        timeColumnLayout.addView(buttonSpacing);
		
        timeButtonLayout = new FrameLayout(getContext());
        LinearLayout.LayoutParams timeButtonLp = new LinearLayout.LayoutParams(Dimension.dpToPx(TimeButton.WIDTH, dm), LayoutParams.WRAP_CONTENT, 0);
        int stripeMargin = Dimension.dpToPx(0, dm);
        timeButtonLp.leftMargin = stripeMargin;
        timeButtonLp.rightMargin = stripeMargin;
        timeButtonLayout.setLayoutParams(timeButtonLp);
        
        mask = new View(getContext());
        int maskHeight = TimeButton.HEIGHT * 2 + 25 + spacingHeight + stripeHieght;                
        FrameLayout.LayoutParams maskLp = new FrameLayout.LayoutParams(Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(maskHeight, dm));
        maskLp.gravity = Gravity.TOP;
        maskLp.leftMargin = Dimension.dpToPx(1, dm);
        mask.setLayoutParams(maskLp);
        mask.setY(Dimension.dpToPx(TimeButton.HEIGHT * 2 + spacingHeight, dm));
        timeButtonLayout.addView(mask);
        
        LinearLayout lowerPart = new LinearLayout(getContext());
        LinearLayout.LayoutParams lowerPartLp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        lowerPart.setLayoutParams(lowerPartLp);
        lowerPart.setOrientation(LinearLayout.VERTICAL);
        
        arriveButton = new TimeButton(getContext(), 1, true, arrivalTimeFont);
        lowerPart.addView(arriveButton);
        
		durationButton = new TimeButton(getContext(), 1, true, arrivalTimeFont);
		lowerPart.addView(durationButton);
		
		bottomSpacing = new View(getContext());
		LinearLayout.LayoutParams bottomSpacingLp = new LinearLayout.LayoutParams(
	        Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(spacingHeight, dm)); 
		bottomSpacing.setLayoutParams(bottomSpacingLp);
		lowerPart.addView(bottomSpacing);
        
        mpointView = new TextView(getContext());
        mpointView.setTextColor(Color.WHITE);
        mpointView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Dimension.dpToPx(TimeButton.largeFont, dm));
        mpointView.setIncludeFontPadding(false);
        mpointView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
        LinearLayout.LayoutParams mpointViewLp = new LinearLayout.LayoutParams(
            Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(25, dm)); 
        mpointView.setLayoutParams(mpointViewLp);
        Font.setTypeface(arrivalTimeFont, mpointView);
        lowerPart.addView(mpointView);
		
        bottomStripe = new View(getContext());
        LinearLayout.LayoutParams bottomStipeLp = new LinearLayout.LayoutParams(
            Dimension.dpToPx(TimeButton.WIDTH, dm), Dimension.dpToPx(stripeHieght, dm));
        bottomStipeLp.leftMargin = stripeMargin;
        bottomStipeLp.rightMargin = stripeMargin;
        bottomStripe.setLayoutParams(bottomStipeLp);
        lowerPart.addView(bottomStripe);
        
        timeButtonLayout.addView(lowerPart);
        
        timeColumnLayout.addView(timeButtonLayout);
        
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
        	if(selected) {
//        		ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(mask, "translateY", Dimension.dpToPx(TimeButton.HEIGHT * 2 + spacingHeight, dm), 0);
        		mask.setY(0);
	        	TranslateAnimation slideUp = new TranslateAnimation(0, 0, Dimension.dpToPx(TimeButton.HEIGHT * 2 + spacingHeight, dm), 0);
	        	slideUp.setDuration(animationDuration);
	        	slideUp.setFillAfter(true);
	        	mask.startAnimation(slideUp);
        	}
        	else if(State.Selected.name().equals(originalState)){
        		TranslateAnimation slideDown = new TranslateAnimation(0, 0, 0, Dimension.dpToPx(TimeButton.HEIGHT * 2 + spacingHeight, dm));
            	slideDown.setDuration(animation?animationDuration:0);
            	slideDown.setFillAfter(true);
            	mask.startAnimation(slideDown);
        	}
        }
        int textColor = Color.parseColor("#606163");
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
		
		departureTimeButton.setText(btnum == 0?"Now":formatTime(time, timzoneOffset));
		
		postInvalidate();
	}
	
	public long getArrivalTime() {
		return arrivalTime;
	}
	
	public void setArrivalTime(long time) {
		this.arrivalTime = time;
		
		if (time != 0) {
		    arriveButton.setText(formatTime(time, timzoneOffset));
			durationButton.setText(getFormattedDuration(getDuration()));
    		
    		postInvalidate();
		}
	}
	
	public static String getFormattedDuration(int duration){
	    return String.format("%d min", duration/60);
	}
	
	public static String formatTime(long time, int timzoneOffset){
	    SimpleDateFormat dateFormat = new SimpleDateFormat("h:mma", Locale.US);
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
//            bottomStripe.setBackgroundColor(Color.parseColor(color));
//        	timeButtonLayout.setBackgroundColor(Color.parseColor(color));
        	mask.setBackgroundColor(Color.parseColor(color));
            postInvalidate();
        }
    }
}