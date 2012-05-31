package com.smartrek.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends Activity implements AnimationListener {

	private ImageView logo;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		logo = (ImageView) findViewById(R.id.imageViewLogo);
		Animation fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade);
		fadeAnimation.setAnimationListener(this);
		logo.startAnimation(fadeAnimation);

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		logo.setAlpha(0);
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void onAnimationStart(Animation animation) {

	}
}
