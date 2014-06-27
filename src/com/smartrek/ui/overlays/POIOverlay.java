package com.smartrek.ui.overlays;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartrek.activities.LandingActivity2.PoiOverlayInfo;
import com.smartrek.activities.R;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.GeoPoint;

public class POIOverlay extends BalloonItemizedOverlay<OverlayItem>{
	
	private OverlayCallback callback;

	private String address;
	
	private GeoPoint geoPoint;
	
	private int marker;
	
	private int aid;
	
	private PoiOverlayInfo poiInfo;
	
	private POIActionListener listener;
	
	private boolean marked;
	
	private boolean fromPoi;
	
	private boolean routePage;
	
	public interface POIActionListener {
		
		public void onClickEdit();
		
	}
	
	public POIOverlay(MapView mapview, Typeface font, PoiOverlayInfo poiInfo, HotspotPlace markerHotspot, POIActionListener listener) {
		super(pinDrawable(mapview.getContext(), poiInfo.markerWithShadow), mapview, null, font, font);
		
		this.address = poiInfo.address;
		this.marker = poiInfo.marker;
		this.poiInfo = poiInfo;
		this.listener = listener;
		
		centerOnTap = false;
		
		balloonOffsetX = 0;
//		balloonOffsetY = Dimension.dpToPx(marker==R.drawable.marker_poi?95:105, mapview.getContext().getResources().getDisplayMetrics());
		balloonOffsetY = 0;
		
		this.geoPoint = poiInfo.geopoint;
		
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
		if(routePage) {
			showMiniBalloonOverlay();
		}
		else {
	        currentFocussedIndex = 0;
	        currentFocussedItem = createItem(0);
	        changeToDefault();
	        createAndDisplayBalloonOverlay();
		}
	}
	
	public void showMiniBalloonOverlay() {
        changeToMini();
	}
	
	public void showDetailBalloonOverlay() {
		changeToDetail();
	}
	
	private void changeToDefault() {
		if(balloonView!=null) {
			LinearLayout layout = balloonView.getLayout();
			layout.findViewById(R.id.poi_content).setVisibility(View.VISIBLE);
			layout.findViewById(R.id.poi_content_detail).setVisibility(View.GONE);
			layout.findViewById(R.id.poi_content_mini).setVisibility(View.GONE);
		}
	}
	
	private void changeToMini() {
		if(balloonView == null) {
			currentFocussedIndex = 0;
	        currentFocussedItem = createItem(0);
	        createAndDisplayBalloonOverlay();
		}
		LinearLayout layout = balloonView.getLayout();
		DisplayMetrics dm = layout.getResources().getDisplayMetrics();
		layout.findViewById(R.id.poi_content).setVisibility(View.GONE);
		layout.findViewById(R.id.poi_content_detail).setVisibility(View.GONE);
		View poiContentMini = layout.findViewById(R.id.poi_content_mini);
		BubbleDrawable bubble = new BubbleDrawable(BubbleDrawable.CENTER);
		bubble.setCornerRadius(Dimension.dpToPx(5, dm));
		bubble.addShadowEffect();
		bubble.setColor(Color.parseColor("#B2FFFFFF"));
		bubble.setPadding(0, 0, 0, 0);
		bubble.setPointerWidth(Dimension.dpToPx(11, dm));
		bubble.setPointerHeight(Dimension.dpToPx(8, dm));
		poiContentMini.setBackgroundDrawable(bubble);
		poiContentMini.setVisibility(View.VISIBLE);
	}
	
	private void changeToDetail() {
		LinearLayout layout = balloonView.getLayout();
		DisplayMetrics dm = layout.getResources().getDisplayMetrics();
		layout.findViewById(R.id.poi_content).setVisibility(View.GONE);
		layout.findViewById(R.id.poi_content_mini).setVisibility(View.GONE);
		View poiContentDetail = layout.findViewById(R.id.poi_content_detail);
		BubbleDrawable bubble = new BubbleDrawable(BubbleDrawable.CENTER);
		bubble.setCornerRadius(Dimension.dpToPx(5, dm));
		bubble.addShadowEffect();
		bubble.setPadding(0, 0, 0, 0);
		bubble.setPointerWidth(Dimension.dpToPx(5, dm));
		bubble.setPointerHeight(Dimension.dpToPx(5, dm));
		poiContentDetail.setBackgroundDrawable(bubble);
		poiContentDetail.setVisibility(View.VISIBLE);
	}
	
	public void setIsFromPoi(boolean isFromPoi) {
		fromPoi = isFromPoi;
		String title = isFromPoi?"FROM":"TO";
		if(balloonView==null){
			currentFocussedIndex = 0;
	        currentFocussedItem = createItem(0);
	        createAndDisplayBalloonOverlay();
		}
		LinearLayout layout = balloonView.getLayout();
		TextView defaultContentTitleView = (TextView) layout.findViewById(R.id.poi_title);
		defaultContentTitleView.setText(title);
		TextView miniContentTitleView = (TextView) layout.findViewById(R.id.poi_mini_title);
		miniContentTitleView.setText(title);
		TextView detailContentTitleView = (TextView) layout.findViewById(R.id.poi_detail_title);
		detailContentTitleView.setText(title);
		if(!isFromPoi) {
			detailContentTitleView.setBackgroundResource(R.drawable.poi_popup_title_background);
			layout.findViewById(R.id.poi_content_detail_spliter).setBackgroundColor(layout.getResources().getColor(R.color.metropia_blue));
		}
		else {
			detailContentTitleView.setTextColor(layout.getResources().getColor(R.color.metropia_blue));
		}
	}
	
	public void switchBalloon() {
		LinearLayout layout = balloonView.getLayout();
		View miniContent = layout.findViewById(R.id.poi_content_mini); 
		if(miniContent.getVisibility()==View.VISIBLE) {
			changeToDetail();
		}
		else {
			changeToMini();
		}
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
        return new POIOverlayView<OverlayItem>(getMapView().getContext(), poiInfo, listener);
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
    
    public void markODPoi() {
    	this.marked = true;
    }
    
    public void cancelMark() {
    	this.marked = false;
    }
    
    public boolean isMarked() {
    	return this.marked;
    }
    
    public boolean isFromPoi() {
    	return this.fromPoi;
    }
    
    public PoiOverlayInfo getPoiOverlayInfo() {
    	return this.poiInfo;
    }
    
    public void inRoutePage() {
    	this.routePage = true;
    }
    
}
