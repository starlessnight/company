package com.smartrek.ui.overlays;

import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.ui.overlays.BalloonItemizedOverlay.IBalloonOverlayView;
import com.smartrek.utils.Font;

public class RouteDestinationOverlayView<Item extends OverlayItem> extends FrameLayout implements IBalloonOverlayView<Item> {

	private LinearLayout layout;

	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * @param context - The activity context.
	 * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
	 * when rendering this view.
	 */
	public RouteDestinationOverlayView(Context context, Typeface font, String text) {

		super(context);
		
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);  

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.destination_overlay, layout);

		TextView textView = (TextView) v.findViewById(R.id.text);
		textView.setText(text);
		textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {}
        });
		
		Font.setTypeface(font, textView);
		
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

