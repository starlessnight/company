package com.smartrek.ui;

import com.smartrek.activities.R;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class ClickAnimation {
	
	public interface ClickAnimationEndCallback {
		void onAnimationEnd();
	}
	
	private View clickedView;
	private Context ctx;
	
	public ClickAnimation(Context ctx, View clickedView) {
		this.ctx = ctx;
		this.clickedView = clickedView;
	}
	
	public void startAnimation(final ClickAnimationEndCallback callback) {
		Animation clickAnimation = AnimationUtils.loadAnimation(ctx, R.anim.click_animation);
		clickAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				clickedView.setClickable(false);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				clickedView.setClickable(true);
				if(callback!=null) {
					callback.onAnimationEnd();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
		});
		clickedView.startAnimation(clickAnimation);
	}

}
