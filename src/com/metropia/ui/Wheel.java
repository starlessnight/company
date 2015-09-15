package com.metropia.ui;

import java.util.concurrent.atomic.AtomicBoolean;

import com.metropia.activities.R;
import com.metropia.models.User;
import com.metropia.requests.DuoSpinWheelRequest;
import com.metropia.utils.Dimension;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;

public class Wheel extends RelativeLayout implements OnGestureListener, OnTouchListener {
	
	GestureDetector gestureDetector;
	Runnable callback;
	AtomicBoolean spinned = new AtomicBoolean(false);
	AtomicBoolean spinning = new AtomicBoolean(false);
	
	int driverId;
	int resultAngle;
	public Integer bonus;
	
	ImageView pin;
	ImageView wheel;

	public Wheel(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		gestureDetector = new GestureDetector(this);
		
		int pinSize = Dimension.dpToPx(30, context.getResources().getDisplayMetrics());
		
		pin = new ImageView(context);
		pin.setImageResource(R.drawable.spin);
		wheel = new ImageView(context);
		wheel.setScaleType(ScaleType.FIT_CENTER);
		wheel.setAdjustViewBounds(true);
		wheel.setOnTouchListener(this);
		
		addView(wheel, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		addView(pin, pinSize, pinSize);
		
		((RelativeLayout.LayoutParams)pin.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
	}
	
	public void setImage(Bitmap bitmap) {
		wheel.setImageBitmap(bitmap);
		center = new Point(getWidth()/2, getHeight()/2);
	}
	
	public void setDriverId(int driverId) {
		this.driverId = driverId;
	}
	public void setCallback(Runnable callback) {
		this.callback = callback;
	}
	
	
	
	
	@SuppressLint("NewApi")
	private void spin(final int direction) {
		if (spinned.getAndSet(true)) return;
		
		resultAngle = (int) (Math.random() * 360);
		
		GetBonusTask task = new GetBonusTask(callback);
		task.execute();
		
		RotateAnimation an = new RotateAnimation(0, 3600*direction + resultAngle-(int)angle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		an.setDuration(5000);
		an.setInterpolator(new DecelerateInterpolator());
		an.setFillEnabled(true);
		an.setFillAfter(true);
		an.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				spinning.set(false);
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
		});
		
		wheel.startAnimation(an);
		spinning.set(true);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}
	
	@Override
	public boolean onDown(MotionEvent e) {return true;}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
		if (Math.abs(vx)<3000&&Math.abs(vy)<3000) return false;
		
		float px = e1.getX() - center.x;
		float py = e1.getY() - center.y;
		
		if (py*vx<px*vy) spin(1);
		else spin(-1);
		return true;
	}
	@Override
	public void onLongPress(MotionEvent e) {}
	
	Point center;
	double angle=0;
	@SuppressLint("NewApi")
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if (spinned.get()) return false;
		
		float ax = e1.getX()-center.x, ay = e1.getY()-center.y;
		float bx = e2.getX()-center.x, by = e2.getY()-center.y;
		
		double dAngle = Math.toDegrees(Math.acos(((ax*bx) + (ay*by)) / (Math.pow((ax*ax+ay*ay), 0.5) * Math.pow((bx*bx+by*by), 0.5))));
		if (by*ax<bx*ay) dAngle*=(-1);
		
		if (!Double.isNaN(dAngle)) angle+=dAngle;
		
		wheel.setRotation((float) angle);
		return true;
		
	}
	@Override
	public void onShowPress(MotionEvent e) {}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {return false;}

	
	class GetBonusTask extends AsyncTask<Void, Void, Void> {

		Runnable cb;
		
		public GetBonusTask(Runnable cb) {
			this.cb = cb;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			DuoSpinWheelRequest request = new DuoSpinWheelRequest(User.getCurrentUser(Wheel.this.getContext()));
			try {
				bonus = request.execute(Wheel.this.getContext(), driverId, resultAngle);
			} catch (Exception e) {}
			
			while (spinning.get()) ;
			if (cb!=null) Wheel.this.post(cb);
			return null;
		}
	};

}
