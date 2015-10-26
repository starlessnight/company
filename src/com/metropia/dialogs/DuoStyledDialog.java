package com.metropia.dialogs;

import com.metropia.activities.R;
import com.metropia.tasks.ICallback;
import com.metropia.utils.Dimension;
import com.metropia.utils.Font;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DuoStyledDialog extends Dialog {

	public DuoStyledDialog(Context context) {
		super(context, R.style.PopUpDialog);
		
		setContentView(R.layout.duo_styled_dialog);
		
		int[] RobotMedium = {R.id.title, R.id.contentText};
		Font.setTypeface(this, Font.getRobotoLight(context.getAssets()), RobotMedium);
	}
	
	
	public DuoStyledDialog setContent(String title, String content) {
		if (title!=null) ((TextView)findViewById(R.id.title)).setText(title);
		if (content!=null) ((TextView)findViewById(R.id.contentText)).setText(content);
		
		return this;
	}
	
	public DuoStyledDialog centerContent() {
		int margin = Dimension.dpToPx(15, getContext().getResources().getDisplayMetrics());
		((RelativeLayout.LayoutParams)findViewById(R.id.contentText).getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		((RelativeLayout.LayoutParams)findViewById(R.id.contentText).getLayoutParams()).setMargins(margin, margin, margin, margin);
		return this;
	}
	
	public DuoStyledDialog addButton(String text, final ICallback cb) {
		
		TextView button = new TextView(getContext());
		button.setGravity(Gravity.CENTER);
		button.setTextColor(getContext().getResources().getColor(R.color.metropia_blue));
		button.setTypeface(Typeface.DEFAULT_BOLD);
		button.setText(text);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {if (cb!=null) cb.run();}
		});
		
		((LinearLayout)findViewById(R.id.buttons)).addView(button);
		((LinearLayout.LayoutParams)button.getLayoutParams()).weight = 1;
		((LinearLayout.LayoutParams)button.getLayoutParams()).height = LayoutParams.MATCH_PARENT;
		
		Font.setTypeface(Font.getRobotoBold(getContext().getAssets()), button);
		return this;
	}

}
