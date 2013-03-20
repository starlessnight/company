package com.smartrek.ui.overlays;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.smartrek.activities.R;
import com.smartrek.models.Route;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.StringUtil;
import com.smartrek.utils.datetime.HumanReadableTime;

public class RouteInfoOverlay extends BalloonItemizedOverlay<OverlayItem> {
	
	private RouteOverlayCallback callback;

	private Route route;
	private GeoPoint geoPoint;
	
	public RouteInfoOverlay(MapView mapview, Route route, int routeSeq, GeoPoint point, Typeface headerFont, Typeface bodyFont) {
		super(pinRouteDrawable(mapview.getContext(), routeSeq + 1, headerFont),
	        mapview, null, headerFont, bodyFont);
		this.route = route;
		this.geoPoint = point;
		
		OverlayItem item = new OverlayItem(
				"Route " + (routeSeq + 1),
				"Estimated Travel Time: " + HumanReadableTime.formatDuration(route.getDuration())
				+ "\nTrekpoints: " + route.getCredits()
				// This won't work because the server does not return any extra info unless the route is reserved
				+ String.format("\nLength: %s", StringUtil.formatImperialDistance(route.getLength())),
				point);
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
	public void setCallback(RouteOverlayCallback callback) {
		this.callback = callback;
	}
	
	public void setRoute(Route route, int routeNum) {
		this.route = route;
	}
	
	public GeoPoint getGeoPoint() {
		return geoPoint;
	}
	
//	public void addOverlay(OverlayItem overlay) {
//		synchronized(overlays) {
//			overlays.add(overlay);
//			populate();	
//		}
//	}

//	@Override
//	protected synchronized OverlayItem createItem(int i) {
//		return overlays.get(i);
//	}
//
//	@Override
//	public synchronized int size() {
//		return overlays.size();
//	}
	
	private Overlay overlay;
	
	public void hide() {
		hideBalloon();
		mapView.getOverlays().add(currentFocussedIndex, overlay);
		overlay = null;
	}

	@Override
	protected final boolean onTap(int index) {
		Log.d("RouteOverlay", "onTap, index=" + index);
		
		// FIXME: Move the following code to RouteActivity
		
		currentFocussedIndex = index;
		currentFocussedItem = createItem(index);
		
		createAndDisplayBalloonOverlay();

		mc.animateTo(currentFocussedItem.getPoint());
			
		ImageView imageViewClose = balloonView.getCloseView();
		
		imageViewClose.setOnClickListener(new OnClickListener() {
									public void onClick(View v) {
										Log.d("RouteInfoOverlay", "onClick() on closeView");
											hide();
											mapView.invalidate();
											
											if (callback != null) {
												callback.onClose();
											}
									}
								});
		
		overlay = mapView.getOverlays().remove(currentFocussedIndex);
		
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
	
	static Drawable pinRouteDrawable(Context ctx, int routeSeq, Typeface font){
        Resources res = ctx.getResources();
        Bitmap bm = BitmapFactory.decodeResource(res, R.drawable.pin_route).copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint(); 
        paint.setStyle(Style.FILL);  
        paint.setColor(Color.WHITE);
        float textSize = res.getDimension(R.dimen.smallest_font);
        paint.setTextSize(textSize);
        paint.setTypeface(font);
        paint.setAntiAlias(true);
        
        Canvas canvas = new Canvas(bm);
        canvas.drawText(String.valueOf(routeSeq), bm.getWidth()*3.427f/10, 
            bm.getHeight()*5.3f/10, paint);

        return new BitmapDrawable(res, bm);
    }
	
}
