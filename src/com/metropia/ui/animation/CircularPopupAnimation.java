package com.metropia.ui.animation;

import java.util.ArrayList;

import com.metropia.ui.animation.CircularPopupAnimation.EasingType.Type;
import com.metropia.utils.Dimension;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class CircularPopupAnimation extends Animation {
	
	final static int[] angleOffset = new int[] {0, -90, 0, -90, -45, -18, 0, -51, 0, 0};
	
	View view;
	int position;
	int total;
	
	ArrayList<View> views;
	int[] initPosition;
	/*public CircularPopupAnimation(View view, int position) {
		
	}*/
	
	
	public CircularPopupAnimation(View view, int position, int total) {
		this.view = view;
		this.position = position;
		this.total = total;
		view.setTag(view.getId(), new int[] {((RelativeLayout.LayoutParams)view.getLayoutParams()).leftMargin, ((RelativeLayout.LayoutParams)view.getLayoutParams()).topMargin});
		setInterpolator(new OvershootInterpolator(4));
		setDuration(500);
	}
	
	public CircularPopupAnimation(final ArrayList<View> views, int direction) {
		
		this.views = views;
		for (int i=0; i<views.size() ; i++) {
			final View view = views.get(i);
			final int position = i;
			view.postDelayed(new Runnable() {
				public void run() {
					view.startAnimation(new CircularPopupAnimation(view, position, views.size()));
				}
			}, i*50);
		}
		
	}
	
	int ti = 0;
	@SuppressLint("NewApi")
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		
		DisplayMetrics displayMetrics = view.getContext().getResources().getDisplayMetrics();

    		int haloPadding = "halo".equals(view.getTag())? Dimension.dpToPx(5, displayMetrics):0;
			int interval = 360/total;
			int distance = (int)(2*Dimension.dpToPx(35, displayMetrics)*(0.575+(total/10f)));
			
			int offset = total<=8? angleOffset[total]:0;
			int x = (int) (Math.cos(Math.toRadians(interval*position+offset)) * interpolatedTime*distance) - haloPadding;
			int y = (int) (Math.sin(Math.toRadians(interval*position+offset)) * interpolatedTime*distance) - haloPadding;
			//Log.e(x+"", y+"");
			
			int originX = ((int[])view.getTag(view.getId()))[0];
			int originY = ((int[])view.getTag(view.getId()))[1];
			//Log.e(originX+"", originY+"");
			
			((RelativeLayout.LayoutParams)view.getLayoutParams()).setMargins(originX+x, originY+y, 0, 0);
			//float alpha = (float) (interpolatedTime>0.6? Math.pow(interpolatedTime, 5):0);
			view.setAlpha(interpolatedTime);
			view.setScaleX(interpolatedTime);
			view.setScaleY(interpolatedTime);
			view.requestLayout();
		
	}
	
	
	
	/*public static class BounceInterpolator implements Interpolator {
	    public BounceInterpolator() {}
	    public BounceInterpolator(Context context, AttributeSet attrs) {}
	 
	    private static float bounce(float t) {
	        return t * t * 8.0f;
	    }
	 
	    @Override
	    public float getInterpolation(float t) {
	        t *= 1.1226f;
	        if (t < 0.3535f) return bounce(t);
	        else if (t < 0.7408f) return bounce(t - 0.54719f) + 0.7f;
	        else if (t < 0.9644f) return bounce(t - 0.8526f) + 0.9f;
	        else return bounce(t - 1.0435f) + 0.95f;
	    }
	}*/
	
	
	/*public class BounceInterpolator implements Interpolator {

		private Type type;

		public BounceInterpolator(Type type) {
			this.type = type;
		}

		public float getInterpolation(float t) {
			if (type == Type.IN) {
				return in(t);
			} else
			if (type == Type.OUT) {
				return out(t);
			} else
			if (type == Type.INOUT) {
				return inout(t);
			}
			return 0;
		}

		private float out(float t) {
			if (t < (1/2.75)) {
				return 7.5625f*t*t;
			} else
			if (t < 2/2.75) {
				return 7.5625f*(t-=(1.5/2.75))*t + .75f;
			} else
			if (t < 2.5/2.75) {
				return 7.5625f*(t-=(2.25/2.75))*t + .9375f;
			} else {
				return 7.5625f*(t-=(2.625/2.75))*t + .984375f;
			}
		}

		private float in(float t) {
			return 1 - out(1-t);
		}

		private float inout(float t) {
			if (t < 0.5f) {
				return in(t*2) * .5f;
			} else {
				return out(t*2-1) * .5f + .5f;
			}
		}
	}*/
	
	public static class EasingType {
		public enum Type {
			IN, OUT, INOUT
		}
	}
	
	
	/*public class OvershootInterpolator implements Interpolator {
	    private final float mTension;
	 
	    public OvershootInterpolator() {
	        mTension = 4.0f;
	    }
	 
	    public OvershootInterpolator(float tension) {
	        mTension = tension;
	    }
	 
	    public OvershootInterpolator(Context context, AttributeSet attrs) {
	 
	        mTension = 2.0f;
	 
	    }
	 
	    public float getInterpolation(float t) {
	        // _o(t) = t * t * ((tension + 1) * t + tension)
	        // o(t) = _o(t - 1) + 1
	        t -= 1.0f;
	        return t * t * ((mTension + 1) * t + mTension) + 1.0f;
	    }
	}*/
}
