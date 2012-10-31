package com.smartrek.ui.overlays;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.smartrek.activities.R;
import com.smartrek.models.Route;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.HumanReadableTime;

public class RouteInfoOverlay extends BalloonItemizedOverlay<OverlayItem> {
	
	private RouteOverlayCallback callback;

	private Route route;
	private GeoPoint geoPoint;
	
	public RouteInfoOverlay(MapView mapview, Route route, int routeSeq, GeoPoint point) {
		super(mapview.getResources().getDrawable(R.drawable.pin_route), mapview, null);
		this.route = route;
		this.geoPoint = point;

		OverlayItem item = new OverlayItem(
				"Route " + (routeSeq + 1),
				"Estimated Travel Time: " + HumanReadableTime.formatDuration(route.getDuration())
				// This won't work because the server does not return any extra info unless the route is reserved
				+ String.format("\nLength: %.01f mi", route.getLength() * 0.000621371)
				+ "\nTrekpoints: " + route.getCredits(),
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
	
	public void hide() {
		hideBalloon();
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
}
