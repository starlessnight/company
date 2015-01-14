/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.metropia.ui.overlays;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.metropia.utils.GeoPoint;
import com.metropia.activities.R;

/**
 * An abstract extension of ItemizedOverlay for displaying an information balloon
 * upon screen-tap of each marker overlay.
 * 
 * @author Jeff Gilfelt
 */
public abstract class BalloonItemizedOverlay<Item extends OverlayItem> extends ItemizedIconOverlay<Item> {

	protected MapView mapView;
	protected IBalloonOverlayView<Item> balloonView;
	protected View clickRegion;
	protected int viewOffset;
	protected final IMapController mc;
	protected Item currentFocussedItem;
	protected int currentFocussedIndex;
	protected Typeface headerFont;
	protected Typeface bodyFont;
	protected boolean showArrow = true;

	/**
	 * Create a new BalloonItemizedOverlay
	 * 
	 * @param defaultMarker - A bounded Drawable to be drawn on the map for each item in the overlay.
	 * @param mapView - The view upon which the overlay items are to be drawn.
	 */
	public BalloonItemizedOverlay(Drawable defaultMarker, MapView mapView,
			OnItemGestureListener<Item> itemGestureListener, Typeface headerFont, Typeface bodyFont) {
		//super(defaultMarker, new DefaultResourceProxyImpl(mapView.getContext()));
		super(new ArrayList<Item>(), defaultMarker, itemGestureListener,
				new DefaultResourceProxyImpl(mapView.getContext()));
		
		this.mapView = mapView;
		viewOffset = 0;
		mc = mapView.getController();
		this.headerFont = headerFont;
		this.bodyFont = bodyFont;
	}

	/**
	 * Set the horizontal distance between the marker and the bottom of the information
	 * balloon. The default is 0 which works well for center bounded markers. If your
	 * marker is center-bottom bounded, call this before adding overlay items to ensure
	 * the balloon hovers exactly above the marker. 
	 * 
	 * @param pixels - The padding between the center point and the bottom of the
	 * information balloon.
	 */
	public void setBalloonBottomOffset(int pixels) {
		viewOffset = pixels;
	}
	public int getBalloonBottomOffset() {
		return viewOffset;
	}

	/**
	 * Override this method to handle a "tap" on a balloon. By default, does nothing 
	 * and returns false.
	 * 
	 * @param index - The index of the item whose balloon is tapped.
	 * @param item - The item whose balloon is tapped.
	 * @return true if you handled the tap, otherwise false.
	 */
	protected boolean onBalloonTap(int index, Item item) {
		return false;
	}

	protected boolean centerOnTap = true; 
	
	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#onTap(int)
	 */
	protected boolean onTap(int index) {

		currentFocussedIndex = index;
		currentFocussedItem = createItem(index);

		createAndDisplayBalloonOverlay();
		
		if(centerOnTap){
		    mc.animateTo(currentFocussedItem.getPoint());
		}

		return true;
	}

	/**
	 * Creates the balloon view. Override to create a sub-classed view that
	 * can populate additional sub-views.
	 */
	protected IBalloonOverlayView<Item> createBalloonOverlayView() {
		return new BalloonOverlayView<Item>(getMapView().getContext(), 
	        getBalloonBottomOffset(), headerFont, bodyFont);
	}

	/**
	 * Expose map view to subclasses.
	 * Helps with creation of balloon views. 
	 */
	protected MapView getMapView() {
		return mapView;
	}

	/**
	 * Sets the visibility of this overlay's balloon view to GONE. 
	 */
	public void hideBalloon() {
		if (balloonView != null) {
		    ((FrameLayout)balloonView).setVisibility(View.GONE);
		}
	}
	
    public boolean isBalloonVisible() {
        boolean visible = false;
        if (balloonView != null) {
            visible = ((FrameLayout)balloonView).getVisibility() == View.VISIBLE;
        }
        return visible;
    }

	/**
	 * Hides the balloon view for any other BalloonItemizedOverlay instances
	 * that might be present on the MapView.
	 * 
	 * @param overlays - list of overlays (including this) on the MapView.
	 */
//	private void hideOtherBalloons(List<Overlay> overlays) {
//
//		for (Overlay overlay : overlays) {
//			if (overlay instanceof BalloonItemizedOverlay<?> && overlay != this) {
//				((BalloonItemizedOverlay<?>) overlay).hideBalloon();
//			}
//		}
//
//	}
	private void hideOtherBalloons(List<Overlay> overlays) {
		for (int i = 0; i < overlays.size(); i++) {
			if (overlays.get(i) instanceof BalloonItemizedOverlay<?>
					&& overlays.get(i) != this) {
				((BalloonItemizedOverlay<?>) overlays.get(i)).hideBalloon();
			}
			else {
				currentFocussedIndex = i;
				currentFocussedItem = getItem(0);
			}
		}
	}

	/**
	 * Sets the onTouchListener for the balloon being displayed, calling the
	 * overridden {@link #onBalloonTap} method.
	 */
	private OnTouchListener createBalloonTouchListener() {
		return new OnTouchListener() {

			float startX;
			float startY;

			public boolean onTouch(View v, MotionEvent event) {

				View l =  ((View) v.getParent()).findViewById(R.id.balloon_main_layout);//id.balloon_main_layout);
				Drawable d = l.getBackground();

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					int[] states = {android.R.attr.state_pressed};
					if (d.setState(states)) {
						d.invalidateSelf();
					}
					startX = event.getX();
					startY = event.getY();
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					int newStates[] = {};
					if (d.setState(newStates)) {
						d.invalidateSelf();
					}
					if (Math.abs(startX - event.getX()) < 100 && 
							Math.abs(startY - event.getY()) < 100 ) {
						// call overridden method
						onBalloonTap(currentFocussedIndex, currentFocussedItem);
					}
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					return true;
				}
				else {
					return false;
				}

			}
		};
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#getFocus()
	 */
	@Override
	public Item getFocus() {
		return currentFocussedItem;
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#setFocus(Item)
	 */
	@Override
	public void setFocus(Item item) {
		currentFocussedItem = item;

		if (currentFocussedItem == null){
			hideBalloon();
		}
		else{
			createAndDisplayBalloonOverlay();
		}	
	}


	protected int balloonOffsetX;
	
	protected int balloonOffsetY;
	
	/**
	 * Creates and displays the balloon overlay by recycling the current 
	 * balloon or by inflating it from xml. 
	 * @return true if the balloon was recycled false otherwise 
	 */
	protected boolean createAndDisplayBalloonOverlay(){
		boolean isRecycled;
		View defaultBalloonView = null;
		View miniBalloonView = null;
		View detailBalloonView = null;
		if (balloonView == null) {
			balloonView = createBalloonOverlayView();
			
			clickRegion = (View) ((FrameLayout)balloonView).findViewById(R.id.balloon_item_snippet);
			defaultBalloonView = balloonView.getLayout().findViewById(R.id.poi_content);
			miniBalloonView = balloonView.getLayout().findViewById(R.id.poi_content_mini);
			detailBalloonView = balloonView.getLayout().findViewById(R.id.poi_content_detail);
			
			if(clickRegion != null){
			    clickRegion.setOnTouchListener(createBalloonTouchListener());
			}
			
			if(defaultBalloonView != null) {
				defaultBalloonView.setOnTouchListener(createBalloonTouchListener());
			}
			
			if(miniBalloonView != null) {
				miniBalloonView.setOnTouchListener(createBalloonTouchListener());
			}
			
			if(detailBalloonView != null) {
				detailBalloonView.setOnTouchListener(createBalloonTouchListener());
			}
			
			isRecycled = false;
		} else {
			isRecycled = true;
		}
		
		if(clickRegion != null){
    		((TextView)clickRegion).setCompoundDrawablesWithIntrinsicBounds(0, 0, 
    	        showArrow?R.drawable.icon_more_small:0, 0);
		}
		
		((FrameLayout)balloonView).setVisibility(View.GONE);

//		List<Overlay> mapOverlays = mapView.getOverlays();
//		if (mapOverlays.size() > 1) {
//			hideOtherBalloons(mapOverlays);
//		}

		if (currentFocussedItem != null)
			balloonView.setData(currentFocussedItem);

		GeoPoint point = (GeoPoint) currentFocussedItem.getPoint();
		MapView.LayoutParams params = new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, point,
				MapView.LayoutParams.BOTTOM_CENTER, balloonOffsetX, balloonOffsetY);
		//params.mode = MapView.LayoutParams.MODE_MAP;

		((FrameLayout)balloonView).setVisibility(View.VISIBLE);

		if (isRecycled) {
		    ((FrameLayout)balloonView).setLayoutParams(params);
		    ((FrameLayout)balloonView).bringToFront();
		} else {
			mapView.addView((FrameLayout)balloonView, params);
		}

		return isRecycled;
	}

    public boolean isShowArrow() {
        return showArrow;
    }

    public void setShowArrow(boolean showArrow) {
        this.showArrow = showArrow;
    }
	
//	@Override
//	public boolean onSingleTapUp(final MotionEvent event, final MapView mapView) {
//		Log.d("BalloonItemizedOverlay", "onSingleTapUp " + this);
//		if (super.onSingleTapConfirmed(event, mapView)) {
//			createAndDisplayBalloonOverlay();
//			return true;
//		}
//
//		return false;
//	}

    public interface IBalloonOverlayView<Item> {
        
        void setData(Item item);
        
        ImageView getCloseView();
        
        LinearLayout getLayout();
        
    }

    public IBalloonOverlayView<Item> getBalloonView() {
        return balloonView;
    }

    public int getBalloonOffsetX() {
        return balloonOffsetX;
    }

    public void setBalloonOffsetX(int balloonOffsetX) {
        this.balloonOffsetX = balloonOffsetX;
    }

    public int getBalloonOffsetY() {
        return balloonOffsetY;
    }

    public void setBalloonOffsetY(int balloonOffsetY) {
        this.balloonOffsetY = balloonOffsetY;
    }
    
}