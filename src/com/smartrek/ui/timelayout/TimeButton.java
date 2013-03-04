package com.smartrek.ui.timelayout;

import com.smartrek.utils.Dimension;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Gravity;
import android.widget.TextView;

public final class TimeButton extends TextView {
	
	public static final int WIDTH = 148;
	public static final int HEIGHT = 25;
	public static final int SMALL_HEIGHT = 15;
	
	public enum State {
		None, Unknown, InProgress, Selected, Disabled;
		
		public int getTextColor() {
			if(Disabled.equals(this)) {
				return Color.parseColor("#C0C0C0");
			}
			else if(InProgress.equals(this)) {
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
				return 0xFF296A07;
			}
			else if(InProgress.equals(this)) {
				return Color.parseColor("#5f5e60");
			}
			else {
				return Color.parseColor("#3f3e40");
			}
		}
	}
	
	public enum DisplayMode {
		Time, Duration
	}
	
	private State state = State.Unknown;
	private DisplayMode displayMode = DisplayMode.Time;
	
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
	
	public TimeButton(Context context, int row, boolean large) {
		super(context);
		
		this.row = row;
		
		setWidth(WIDTH);
		setHeight(Dimension.dpToPx(large?HEIGHT:SMALL_HEIGHT, getResources().getDisplayMetrics()));
		setGravity(Gravity.CENTER);
		
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFF606060);
        paint.setStrokeWidth(1);
        
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setColor(0xFFD3E0D3);
        paint2.setStrokeWidth(1);
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
        
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        
        // vertical border
        canvas.drawLine(width-1, 0, width-1, height, paint);
        
        // horizontal border
        if (row == 0) {
        	canvas.drawLine(0, height-1, width, height-1, State.Selected.equals(getState()) ? paint2 : paint);
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
			setBackgroundColor(buttonState.getBackgroundColor());
		}
	}
}
