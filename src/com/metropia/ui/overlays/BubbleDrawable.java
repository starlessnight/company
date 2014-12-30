package com.metropia.ui.overlays;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class BubbleDrawable extends Drawable {

    // Public Class Constants
    ////////////////////////////////////////////////////////////

    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;

    // Private Instance Variables
    ////////////////////////////////////////////////////////////

    private Paint mPaint;
    private int mColor = Color.WHITE;

    private RectF mBoxRect;
    private int mBoxWidth;
    private int mBoxHeight;
    private float mCornerRad;
    private Rect mBoxPadding = new Rect();

    private Path mPointer;
    private int mPointerWidth;
    private int mPointerHeight;
    private int mPointerAlignment;
    
    private boolean shadowEffect = false;

    // Constructors
    ////////////////////////////////////////////////////////////

    public BubbleDrawable(int pointerAlignment) {
        setPointerAlignment(pointerAlignment);
        initBubble();
    }

    // Setters
    ////////////////////////////////////////////////////////////

    public void setPadding(int left, int top, int right, int bottom) {
        mBoxPadding.left = left;
        mBoxPadding.top = top;
        mBoxPadding.right = right;
        mBoxPadding.bottom = bottom;
    }

    public void setCornerRadius(float cornerRad) {
        mCornerRad = cornerRad;
    }

    public void setPointerAlignment(int pointerAlignment) {
        if (pointerAlignment < 0 || pointerAlignment > 3) {
            Log.e("BubbleDrawable", "Invalid pointerAlignment argument");
        } else {
            mPointerAlignment = pointerAlignment;
        }
    }

    public void setPointerWidth(int pointerWidth) {
        mPointerWidth = pointerWidth;
    }

    public void setPointerHeight(int pointerHeight) {
        mPointerHeight = pointerHeight;
    }

    // Private Methods
    ////////////////////////////////////////////////////////////

    private void initBubble() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mColor);
        mCornerRad = 0;
        setPointerWidth(20);
        setPointerHeight(20);
    }
    
    public void setColor(int color) {
    	mPaint.setColor(color);
    }

    private void updatePointerPath() {
        mPointer = new Path();
        mPointer.setFillType(Path.FillType.EVEN_ODD);

        // Set the starting point
        mPointer.moveTo(pointerHorizontalStart(), mBoxHeight-1);

        // Define the lines
        mPointer.rLineTo(mPointerWidth - 2, 0);
        mPointer.rLineTo(-((mPointerWidth -2) / 2), mPointerHeight - 1);
        mPointer.rLineTo(-((mPointerWidth - 2) / 2), -mPointerHeight + 1);
        mPointer.close();
    }
    
    private float pointerHorizontalStart() {
        float x = 0;
        switch (mPointerAlignment) {
        case LEFT:
            x = mCornerRad;
            break;
        case CENTER:
            x = (mBoxWidth / 2) - (mPointerWidth / 2);
            break;
        case RIGHT:
            x = mBoxWidth - mCornerRad - mPointerWidth;
        }
        return x;
    }

    // Superclass Override Methods
    ////////////////////////////////////////////////////////////

    @Override
    public void draw(Canvas canvas) {
    	if(shadowEffect) {
    		mPaint.setShadowLayer(mCornerRad, 2, 2, Color.parseColor("#4d000000"));
    	}
        mBoxRect = new RectF(1.0f, 1.0f, mBoxWidth - 2, mBoxHeight - 2);
        canvas.drawRoundRect(mBoxRect, mCornerRad, mCornerRad, mPaint);
        updatePointerPath();
        canvas.drawPath(mPointer, mPaint);
    }

    @Override
    public int getOpacity() {
        return 255;
    }

    @Override
    public void setAlpha(int alpha) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getPadding(Rect padding) {
        padding.set(mBoxPadding);

        // Adjust the padding to include the height of the pointer
        padding.bottom += mPointerHeight;
        return true;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mBoxWidth = bounds.width();
        mBoxHeight = getBounds().height() - mPointerHeight;
        super.onBoundsChange(bounds);
    }
    
    public void addShadowEffect() {
    	this.shadowEffect = true;
    }
}