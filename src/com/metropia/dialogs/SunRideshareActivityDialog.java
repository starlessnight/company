package com.metropia.dialogs;

import org.json.JSONObject;

import com.metropia.activities.R;
import com.metropia.models.User;
import com.metropia.requests.SendMailRequest;
import com.metropia.requests.SunRideshareInfoRequest;
import com.metropia.requests.CityRequest.City;
import com.metropia.tasks.ICallback;
import com.metropia.tasks.ImageLoader;
import com.metropia.ui.animation.ClickAnimation;
import com.metropia.utils.Preferences;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SunRideshareActivityDialog extends BlurDialog implements OnDismissListener {

	Context context;
	Runnable cb;
	City city;
	String campaign;
	
	int[] clickableAnimated = {R.id.leftButton, R.id.rightButton};
	
	public SunRideshareActivityDialog(Context context, City city, String campaign, Runnable cb) {
		super(context);
		this.context = context;
		this.cb = cb;
		this.city = city;
		this.campaign = campaign;
		
		setContentView(R.layout.sunrideshare_activity_dialog);
		getWindow().setBackgroundDrawableResource(R.drawable.sun_rideshare_back);
		
		ClickAnimation.OnClickListener onClickListener = new ClickAnimation.OnClickListener(this);
		for (int i=0 ; i<clickableAnimated.length ; i++) findViewById(clickableAnimated[i]).setOnClickListener(onClickListener);
		this.setOnDismissListener(this);
	}
	
	public void showAsync() {
		
		if (city.name==null) return;
		new SunRideshareInfoRequest(User.getCurrentUser(context)).executeAsync(context, city, campaign, new ICallback() {

			@Override
			public void run(Object... obj) {
				if (obj[0]==null) return;
				JSONObject jsonObject = (JSONObject) obj[0];

				String logoURL = jsonObject.optString("logoURL", "");
				String title = jsonObject.optString("title", "");
				String content = jsonObject.optString("content", "");
				String leftButton = jsonObject.optString("leftbutton", "");
				String rightButton = jsonObject.optString("rightbutton", "");
				
				new ImageLoader(context, logoURL, new ICallback() {

					@Override
					public void run(Object... obj) {
						if (obj[0]==null) return;
						Drawable drawable = (Drawable) obj[0];
						((ImageView)findViewById(R.id.logo)).setImageDrawable(drawable);
					}
				}).execute(true);
				
				((TextView)findViewById(R.id.title)).setText(title);
				((TextView)findViewById(R.id.content)).setText(Html.fromHtml(content));
				((TextView)findViewById(R.id.leftButton)).setText(leftButton);
				((TextView)findViewById(R.id.rightButton)).setText(rightButton);
				
				((Activity)context).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						SunRideshareActivityDialog.this.show();
						
						SharedPreferences prefs = Preferences.getGlobalPreferences(getContext());
						prefs.edit().putInt("SunRideshareCount", 0).commit();
					}
				});
			}
		});
	}
	
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.rightButton:
				new SendMailRequest(User.getCurrentUser(getContext())).executeAsync(getContext(), city, campaign, null);
			break;
		}

		dismiss();
	}
	

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (cb!=null) cb.run();
	}

}
