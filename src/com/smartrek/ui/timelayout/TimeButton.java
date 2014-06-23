package com.smartrek.ui.timelayout;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.Font;

public final class TimeButton extends TextView {
	
	public static int WIDTH = 65;
	public static int HEIGHT = 31;
	public static int FIRST_ROW_HEIGHT = 35;
//	public static int SMALL_HEIGHT = 18;
	public static final int IN_PREGRESS_BACKGROUND_COLOR = Color.parseColor("#aaffffff");
	
//	private static final int largeTopOffset = 8;
//	private static final int smallTopOffset = 8;
	
	public static final int largeFont = 15;
//    private static final int smallFont = 13;
	
	public enum State {
		None, Unknown, InProgress, Selected, Disabled;
		
		public int getTextColor() {
			if(Disabled.equals(this)) {
				return Color.parseColor("#606163");
			}
			else if(InProgress.equals(this)) {
				return Color.parseColor("#606163");
			}
			else {
				return Color.parseColor("#606163");
			}
		}
		
		public int getBackgroundColor(Resources res) {
			if(InProgress.equals(this)) {
				return IN_PREGRESS_BACKGROUND_COLOR;
			}
			else {
				return IN_PREGRESS_BACKGROUND_COLOR;
			}
		}
	}
	
	public enum DisplayMode {
		Arrival, Duration
	}
	
	private State state = State.Unknown;
	private DisplayMode displayMode = DisplayMode.Arrival;
	
	/**
	 * To draw column borders
	 */
	private Paint paint = new Paint();
	
	/**
	 * To draw column borders for a highlighted column
	 */
	private Paint paint2 = new Paint();
	
	/**
	 * Zero-based index
	 */
	private int row;
	
	private boolean large;
	
	public TimeButton(Context context, int row, boolean large, Typeface font) {
		super(context);
		
		this.row = row;
		this.large = large;
		
		Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
		
		setWidth(WIDTH);
        setHeight(HEIGHT);
		setGravity(Gravity.CENTER);
		
		setTextSize(TypedValue.COMPLEX_UNIT_PX, Dimension.dpToPx(largeFont, dm));
		
		Font.setTypeface(font, this);
		setIncludeFontPadding(false);
//		setPadding(0, Dimension.dpToPx(large?largeTopOffset:smallTopOffset, dm), 0, 0);
		
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setColor(res.getColor(R.color.secondary_font));
//        paint.setStrokeWidth(res.getDimension(R.dimen.timetable_separator_width));
//        
//        paint2.setStyle(Paint.Style.STROKE);
//        paint2.setColor(0xFFD3E0D3);
//        paint2.setStrokeWidth(1);
	}
	
	public State getState() {
		return state;
	}
	
	public synchronized void setState(State state) {
		this.state = state;

		((Activity) getContext()).runOnUiThread(new StateUpdateTask(state));
	}

	/**
	 * 
	 */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        // vertical border
        float borderX = width - getResources().getDimension(R.dimen.timetable_separator_width);
        //canvas.drawLine(borderX, large?0:height*4f/5, borderX, height, paint);
        
        // horizontal border
        if (row == 0) {
        	//canvas.drawLine(0, height-1, width, height-1, State.Selected.equals(getState()) ? paint2 : paint);
        }
    }
    
	private final class StateUpdateTask extends Thread {
		
		private TimeButton.State buttonState;
		
		public StateUpdateTask(TimeButton.State state) {
			this.buttonState = state;
		}
		
		@Override
		public void run() {
			setTextColor(buttonState.getTextColor());
//			setBackgroundColor(buttonState.getBackgroundColor(getResources()));
		}
	}
	
	//caculate by default ratio
	public static void initButtonDimension(DisplayMetrics dm, Display display) {
		int timeLayoutHeight = Double.valueOf(Math.floor(display.getHeight() / 2.618)).intValue() - Dimension.dpToPx(56, dm);
    	int ticketHeight = timeLayoutHeight - Dimension.dpToPx(5, dm);
    	FIRST_ROW_HEIGHT = 35 * ticketHeight /128;
    	HEIGHT = 31 * ticketHeight / 128;
    	WIDTH = (display.getWidth() - Dimension.dpToPx(70, dm)) / 3;
	}
}
