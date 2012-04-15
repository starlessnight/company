package smartrek.ui.timelayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.TextView;

public final class TimeButton extends TextView {
	
	public static final int WIDTH = 148;
	public static final int HEIGHT = 28;
	
	public TimeButton(Context context) {
		super(context);
		
		setWidth(WIDTH);
		setHeight(HEIGHT);
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
