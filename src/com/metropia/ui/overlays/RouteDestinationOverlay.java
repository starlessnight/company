package com.metropia.ui.overlays;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.metropia.activities.R;
import com.metropia.utils.Dimension;
import com.metropia.utils.GeoPoint;

public class RouteDestinationOverlay extends BalloonItemizedOverlay<OverlayItem> {
	
	private OverlayCallback callback;

	private GeoPoint geoPoint;
	
	private Typeface font;
	
	private String text;
	
	private int marker;
	
	public RouteDestinationOverlay(MapView mapview, GeoPoint point, Typeface font, String text, int marker) {
		super(pinDrawable(mapview.getContext(), marker), mapview, null, font, font);
		
		this.font = font;
		this.text = text;
		this.marker = marker;
		
		centerOnTap = false;
		
		boolean isDestFlag = marker == R.drawable.pin_destination;
		
		balloonOffsetX = /*Dimension.dpToPx(103, mapview.getContext().getResources().getDisplayMetrics())*/ 0;
		balloonOffsetY = Dimension.dpToPx(isDestFlag ? -34 : 0, mapview.getContext().getResources().getDisplayMetrics());
		
		this.geoPoint = point;
		
		OverlayItem item = new OverlayItem(
				"",
				"",
				point);
		
		if(!isDestFlag) {
			item.setMarkerHotspot(HotspotPlace.CENTER);
		}
		addItem(item);
		
		mOnItemGestureListener = new OnItemGestureListener<OverlayItem>() {

			@Override
			public boolean onItemLongPress(int index, OverlayItem item) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onItemSingleTapUp(int index, OverlayItem item) {
//				onTap(index);
				
				if (callback != null) {
					return callback.onTap(index);
				}
				
				return false;
			}
		};
	}
	
	/**
	 * Currently supports only one callback instance. The name of this method
	 * would have been 'addCallback' otherwise.
	 * 
	 * @param callback
	 */
	public void setCallback(OverlayCallback callback) {
		this.callback = callback;
	}
	
	public GeoPoint getGeoPoint() {
		return geoPoint;
	}
	
	private Overlay overlay;
	
	public void hide() {
	    callback.onChange();
		hideBalloon();
	}
	
	public void showOverlay(){
	    if(overlay != null){
            mapView.getOverlays().add(overlay);
            overlay = null;
        }
    }
	
	public void hideOverlay(){
	    overlay = mapView.getOverlays().remove(currentFocussedIndex);
	}

	public void showBalloonOverlay(){
        currentFocussedIndex = 0;
        currentFocussedItem = createItem(0);
        
        createAndDisplayBalloonOverlay();
	}
	
	@Override
	protected final boolean onTap(int index) {
		if(callback != null){
		    callback.onChange();
		}
		
		// FIXME: Move the following code to RouteActivity
//		currentFocussedIndex = index;
//		currentFocussedItem = createItem(index);
//		
//		createAndDisplayBalloonOverlay();
		
		if (callback != null) {
			return callback.onTap(index);
		}
		else {
			return super.onTap(index);
		}
	}
	
	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
		if (callback != null) {
			return callback.onBalloonTap(index, item);
		}
		else {
			return super.onBalloonTap(index, item);
		}
	}
	
	/**
     * Creates the balloon view. Override to create a sub-classed view that
     * can populate additional sub-views.
     */
    protected IBalloonOverlayView<OverlayItem> createBalloonOverlayView() {
        return new RouteDestinationOverlayView<OverlayItem>(getMapView().getContext(), 
            font, text);
    }
	
	static Drawable pinDrawable(Context ctx, int marker){
        Resources res = ctx.getResources();
        Bitmap bm = BitmapFactory.decodeStream(res.openRawResource(marker));
        return new BitmapDrawable(res, bm);
    }

    public int getMarker() {
        return marker;
    }
    
    @Override
    protected boolean onLongPressHelper(int index, OverlayItem item) {
        boolean handled;
        if(callback != null){
            handled = callback.onLongPress(index, item);
        }else{
            handled = super.onLongPressHelper(index, item); 
        }
        return handled;
    }
    
    private final Rect mRect = new Rect();
    
    @Override
    protected synchronized Drawable boundToHotspot(final Drawable marker, HotspotPlace hotspot) {
    	if(this.marker == R.drawable.pin_destination) {
    		return super.boundToHotspot(marker, hotspot);
    	}
    	else {
			final int markerWidth = marker.getIntrinsicWidth();
			final int markerHeight = marker.getIntrinsicHeight();
			
			int widthShrink = getShirkSize(markerWidth, mapView.getZoomLevel());
			int heightShrink = getShirkSize(markerHeight, mapView.getZoomLevel());
			
			mRect.set(0 + (widthShrink / 2), 0 + (heightShrink / 2), 0 + markerWidth - (widthShrink / 2), 0 + markerHeight - (heightShrink / 2));
	
			if (hotspot == null) {
				hotspot = HotspotPlace.BOTTOM_CENTER;
			}
	
			switch (hotspot) {
			default:
			case NONE:
				break;
			case CENTER:
				mRect.offset(-markerWidth / 2, -markerHeight / 2);
				break;
			case BOTTOM_CENTER:
				mRect.offset(-markerWidth / 2, -markerHeight);
				break;
			case TOP_CENTER:
				mRect.offset(-markerWidth / 2, 0);
				break;
			case RIGHT_CENTER:
				mRect.offset(-markerWidth, -markerHeight / 2);
				break;
			case LEFT_CENTER:
				mRect.offset(0, -markerHeight / 2);
				break;
			case UPPER_RIGHT_CORNER:
				mRect.offset(-markerWidth, 0);
				break;
			case LOWER_RIGHT_CORNER:
				mRect.offset(-markerWidth, -markerHeight);
				break;
			case UPPER_LEFT_CORNER:
				mRect.offset(0, 0);
				break;
			case LOWER_LEFT_CORNER:
				mRect.offset(0, -markerHeight);
				break;
			}
			marker.setBounds(mRect);
			return marker;
    	}
	}
    
    private int getShirkSize(int originalSize, int zoomLevel) {
    	double ratio = zoomLevel < 10 ? 0.7 : (zoomLevel < 17 ? 0.8 : 1);
    	return Double.valueOf((originalSize * (1 - ratio))).intValue();
    }
	
}
