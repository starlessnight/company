package com.smartrek.ui.overlays;

import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartrek.activities.LandingActivity2.PoiOverlayInfo;
import com.smartrek.activities.R;
import com.smartrek.ui.ClickAnimation;
import com.smartrek.ui.ClickAnimation.ClickAnimationEndCallback;
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
	public POIOverlayView(Context context, PoiOverlayInfo poiInfo, final POIActionListener listener) {

		super(context);

		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.poi_overlay, layout);
		
		TextView titleView = (TextView) v.findViewById(R.id.poi_title);
		
		TextView miniTitleView = (TextView) v.findViewById(R.id.poi_mini_title);
		
		TextView detailTitleView = (TextView) v.findViewById(R.id.poi_detail_title);
		
		TextView labelView = (TextView) v.findViewById(R.id.label);
		labelView.setText(poiInfo.label);
		labelView.setCompoundDrawablesWithIntrinsicBounds(poiInfo.marker!=R.drawable.marker_poi?poiInfo.marker:0, 0, 0, 0);

		TextView addressView = (TextView) v.findViewById(R.id.address);
		addressView.setText(poiInfo.address);
		
		TextView detailAddressView = (TextView) v.findViewById(R.id.detail_address);
		detailAddressView.setText(poiInfo.address);

		TextView favOptView = (TextView) v.findViewById(R.id.fav_option);
		if (poiInfo.id != 0) {
			favOptView.setText("Edit");
		} else {
			favOptView.setText("Add to Favorites");
		}

		favOptView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(getContext(), v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						if (listener != null) {
							listener.onClickEdit();
						}
					}
				});
			}
		});
		
		AssetManager assets = context.getAssets();
		Typeface mediumFont = Font.getMedium(assets);
		Typeface regularFont = Font.getRegular(assets);

		Font.setTypeface(regularFont, labelView, miniTitleView);
		Font.setTypeface(mediumFont, addressView, favOptView, titleView, detailTitleView, detailAddressView);

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
