package com.metropia.dialogs;

import com.metropia.activities.IntroActivity;
import com.metropia.activities.R;
import com.metropia.ui.animation.ClickAnimation;
import com.metropia.utils.Preferences;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.view.View;

public class BlurDialog extends Dialog implements android.view.View.OnClickListener {
	
	int[] clickableAnimated = {R.id.blurDialoNotNow, R.id.blurDialoLearnMore};
	Context context;

	public BlurDialog(Context context) {
		super(context, R.style.PopUpDialog);
		this.context = context;
		
		setContentView(R.layout.blur_dialog);
		getWindow().setBackgroundDrawableResource(R.drawable.duo_blur_back);
		getWindow().getAttributes().width = -1;
		getWindow().getAttributes().height = -1;

        ClickAnimation.OnClickListener onClickListener = new ClickAnimation.OnClickListener(this);
		for (int i=0 ; i<clickableAnimated.length ; i++) findViewById(clickableAnimated[i]).setOnClickListener(onClickListener);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.blurDialoNotNow:
				dismiss();
			break;
			case R.id.blurDialoLearnMore:
				Activity activity = (Activity) (getContext() instanceof Activity? getContext():((ContextWrapper)getContext()).getBaseContext());
				activity.findViewById(R.id.duoTutorial).setVisibility(View.VISIBLE);
				

				SharedPreferences prefs = Preferences.getGlobalPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(Preferences.Global.DUO_TUTORIAL_FINISH, 1);
                editor.commit();
				dismiss();
			break;
		}
	}

}
