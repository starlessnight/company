package com.smartrek.ui.overlays;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;


import com.smartrek.activities.R;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.GeoPoint;

public class POIOverlay extends BalloonItemizedOverlay<OverlayItem>{
	
	private OverlayCallback callback;

	private String label;
	
	private String address;
	
	private GeoPoint geoPoint;
	
	private Typeface font;
	
	private int marker;
	
	private int aid;
	
	private POIActionListener listener;
	
	public interface POIActionListener {
		public void onClickEdit();
		
		public void onClickNext();
	}
	
	public POIOverlay(MapView mapview, GeoPoint geoPoint, Typeface font, String label, String address, 
			int marker, HotspotPlace markerHotspot, POIActionListener listener) {
		super(pinDrawable(mapview.getContext(), marker), mapview, null, font, font);
		
		this.font = font;
		this.label = label;
		this.address = address;
		this.marker = marker;
		this.listener = listener;
		
		centerOnTap = false;
		
		balloonOffsetX = Dimension.dpToPx(0, mapview.getContext().getResources().getDisplayMetrics());
		balloonOffsetY = Dimension.dpToPx(marker==R.drawable.marker_poi?95:105, mapview.getContext().getResources().getDisplayMetrics());
		
		this.geoPoint = geoPoint;
		
		OverlayItem item = new OverlayItem(
				"",
				"",
				geoPoint);
		item.setMarkerHotspot(markerHotspot);
		addItem(item);
		
		mOnItemGestureListener = new OnItemGestureListener<OverlayItem>() {

			@Override
			public boolean onItemLongPress(int index, OverlayItem item) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onItemSingleTapUp(int index, OverlayItem item) {
				onTap(index);
				
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
        return new POIOverlayView<OverlayItem>(getMapView().getContext(), 
            font, label, address, aid, marker, listener);
    }
	
	static Drawable pinDrawable(Context ctx, int marker){
        Resources res = ctx.getResources();
        Bitmap bm = BitmapFactory.decodeResource(res, marker);
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

	public int getAid() {
		return aid;
	}

	public void setAid(int aid) {
		this.aid = aid;
	}

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
