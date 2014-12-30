package com.metropia.ui.overlays;

import org.apache.commons.lang3.StringUtils;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.metropia.ui.overlays.BalloonItemizedOverlay.IBalloonOverlayView;
import com.metropia.utils.Font;
import com.metropia.activities.R;

public class POIActionOverlayView<Item extends OverlayItem> extends FrameLayout implements IBalloonOverlayView<Item> {

	private LinearLayout layout;

	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * @param context - The activity context.
	 * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
	 * when rendering this view.
	 */
	public POIActionOverlayView(Context context, int balloonBottomOffset, Typeface headerFont, Typeface bodyFont,
	        String addrStr, String labelStr) {

		super(context);
		
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);  

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.poi_action_overlay, layout);

		TextView address = (TextView) v.findViewById(R.id.address);
		address.setText(addrStr);
		address.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {}
        });
		EditText label = (EditText) v.findViewById(R.id.label);
		if(StringUtils.isNotBlank(labelStr)){
		    label.setText(labelStr);
		}
		
		Font.setTypeface(bodyFont, address, label);
		
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);

	}

	/**
	 * Sets the view data from a given overlay item.
	 * 
	 * @param item - The overlay item containing the relevant view data 
	 * (title and snippet). 
	 */
	public void setData(Item item) {

		layout.setVisibility(VISIBLE);
		
	}
	
	public ImageView getCloseView(){
		return null;
	}
	
	public LinearLayout getLayout(){
		return layout;
	}

}

