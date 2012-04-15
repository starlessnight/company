package smartrek.ui.timelayout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.TextView;

public final class TimeButton extends TextView {
	
	public static final int WIDTH = 148;
	public static final int HEIGHT = 28;
	
	public enum State {
		None, Unknown, InProgress, Selected, Disabled;
		
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
	
	private State state = State.Unknown;
	
	public TimeButton(Context context) {
		super(context);
		
		setWidth(WIDTH);
		setHeight(HEIGHT);
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
        Rect rect = new Rect();
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        getLocalVisibleRect(rect);
        canvas.drawRect(rect, paint);
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
