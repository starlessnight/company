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

public class RouteInfoOverlay extends BalloonItemizedOverlay<OverlayItem> {
	
	private RouteOverlayCallback callback;

	private Route route;
	private GeoPoint geoPoint;
	
	public RouteInfoOverlay(MapView mapview, Route route, GeoPoint point) {
		super(mapview.getResources().getDrawable(R.drawable.marker_default), mapview, null);
		this.route = route;
		this.geoPoint = point;

		OverlayItem item = new OverlayItem(
		// TODO: Showing a route ID is a temporary solution.
		// Ultimately, we want to show "Route 1", "Route 2", ...
				"Route #" + route.getId(),
				"Origin: \n" + route.getOrigin()
						+ " \n\n" + "Destination: \n" + route.getDestination()
						+ "\n\n" + "Estimated Travel Time: " + route.getDuration()/60
						+ " min\n" + "Trekpoints: " + route.getCredits() + "\n\n"
						+ "(Tap to reserve this route)",
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
