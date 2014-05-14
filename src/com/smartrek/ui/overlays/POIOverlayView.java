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
import com.smartrek.ui.overlays.POIOverlay.POIActionListener;
import com.smartrek.utils.Font;

public class POIOverlayView<Item extends OverlayItem> extends FrameLayout
		implements IBalloonOverlayView<Item> {

	private LinearLayout layout;

	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * @param context
	 *            - The activity context.
	 * @param balloonBottomOffset
	 *            - The bottom padding (in pixels) to be applied when rendering
	 *            this view.
	 */
	public POIOverlayView(Context context, Typeface font, String label,
			String address, int aid, int marker, final POIActionListener listener) {

		super(context);

		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.poi_overlay, layout);
		
		View poiContent = v.findViewById(R.id.poi_content);

		BubbleDrawable bubble = new BubbleDrawable(BubbleDrawable.CENTER);
		bubble.setCornerRadius(10);
		bubble.setPadding(10, 10, 10, 10);
		bubble.setPointerWidth(30);
		poiContent.setBackgroundDrawable(bubble);

		TextView labelView = (TextView) v.findViewById(R.id.label);
		labelView.setText(label);
		labelView.setCompoundDrawablesWithIntrinsicBounds(marker!=R.drawable.marker_poi?marker:0, 0, 0, 0);

		TextView addressView = (TextView) v.findViewById(R.id.address);
		addressView.setText(address);

		TextView favOptView = (TextView) v.findViewById(R.id.fav_option);
		if (aid != 0) {
			favOptView.setText("Edit");
		} else {
			favOptView.setText("Add to Favorites");
		}

		favOptView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onClickEdit();
				}
			}
		});

		TextView nextView = (TextView) v.findViewById(R.id.next);
		nextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onClickNext();
				}
			}
		});

		Font.setTypeface(font, labelView, addressView, favOptView, nextView);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);

	}

	public void setData(Item item) {
		layout.setVisibility(VISIBLE);
	}

	public ImageView getCloseView() {
		return null;
	}

	public LinearLayout getLayout() {
		return layout;
	}

}
