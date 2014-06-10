package com.smartrek.activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.smartrek.utils.Font;

public class TermsAndPrivacyActivity extends FragmentActivity{
	
	private Typeface boldFont;
	private Typeface lightFont;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.terms_and_privacy);

		AssetManager assets = getAssets();
		boldFont = Font.getBold(assets);
		lightFont = Font.getLight(assets);
		
		TextView backButton = (TextView) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.startAnimation(AnimationUtils.loadAnimation(TermsAndPrivacyActivity.this, R.anim.click_animation));
				finish();
			}
		});
		
		TextView termOfUseView = (TextView) findViewById(R.id.terms_of_use);
		termOfUseView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Animation clickAnimation = AnimationUtils.loadAnimation(TermsAndPrivacyActivity.this, R.anim.click_animation);
				clickAnimation.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						Intent intent = new Intent(TermsAndPrivacyActivity.this, TermOfUseActivity.class);
						startActivity(intent);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					
				});
				v.startAnimation(clickAnimation);
			}
		});
		
		TextView privacyPolicyView = (TextView) findViewById(R.id.privacy_policy);
		privacyPolicyView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Animation clickAnimation = AnimationUtils.loadAnimation(TermsAndPrivacyActivity.this, R.anim.click_animation);
				clickAnimation.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						Intent intent = new Intent(TermsAndPrivacyActivity.this, PrivacyPolicyActivity.class);
						startActivity(intent);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					
				});
				v.startAnimation(clickAnimation);
			}
		});
		
		Font.setTypeface(boldFont, (TextView)findViewById(R.id.header));
		Font.setTypeface(lightFont, termOfUseView, privacyPolicyView, backButton);
	}

}
