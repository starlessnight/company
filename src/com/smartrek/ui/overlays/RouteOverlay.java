package com.smartrek.ui.overlays;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.smartrek.AdjustableCouponDisplay.CouponLayout;
import com.smartrek.activities.ReservationConfirmationActivity;
import com.smartrek.models.Route;
import com.smartrek.ui.mapviewballon.BalloonItemizedOverlay;

public class RouteOverlay extends BalloonItemizedOverlay<OverlayItem> {
	
	private RouteOverlayCallback callback;

	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private Context context;
	private CouponLayout couponLayout;
	private TextView titleBar;
	private Route route;
//	private Bitmap bitmap;
	private boolean enabled;
	
	public RouteOverlay(Drawable defaultMarker, MapView mapview, Route route){ // Context context) {
		  super(boundCenter(defaultMarker),mapview);
		  enabled = false;
		  context = mapview.getContext();
		  this.route = route;
	}
	
	public RouteOverlay(Drawable defaultMarker, MapView mapview, Route route, GeoPoint point) {
		  super(boundCenter(defaultMarker),mapview);
		  enabled = false;
		  context = mapview.getContext();
		  this.route = route;
	
		  OverlayItem item = new OverlayItem(point, "Title", "snippet");
		  addOverlay(item);
		  
          OverlayItem item2 = new OverlayItem(point,
        		  // TODO: Showing a route ID is a temporary solution.
        		  // Ultimately, we want to show "Route 1", "Route 2", ...
                  "Route #" + route.getId(),
                  "Origin: \n" + route.getOrigin()  + " \n\n" +
                  "Destination: \n" + route.getDestination() + "\n\n" + 
                  "Estimated Travel Time: " + route.getMin() + " min\n" +
                  "Credits: " + route.getCredits() + "\n\n" +
                  "(Tap to reserve this route)");
          addOverlay(item2);
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
	
	public void setCouponLayout(CouponLayout couponlayout, TextView titleBar){
		this.couponLayout = couponlayout;
		this.titleBar = titleBar;
		enabled = true;
	}
	
	public void setRoute(Route route, int routeNum) {
		this.route = route;
	}
	
	public synchronized void addOverlay(OverlayItem overlay) {
		overlays.add(overlay);
		populate();
	}

	@Override
	protected synchronized OverlayItem createItem(int i) {
		return overlays.get(i);
	}

	@Override
	public synchronized int size() {
		return overlays.size();
	}

	@Override
	protected final boolean onTap(int index) {
		Log.d("RouteOverlay", "onTab, index="+index);
		
		currentFocussedIndex = index;
		currentFocussedItem = createItem(index);
		
		createAndDisplayBalloonOverlay();

		mc.animateTo(currentFocussedItem.getPoint());
		if(enabled) {
			couponLayout.setVisibility(View.VISIBLE);
			titleBar.setVisibility(View.VISIBLE);
			
		ImageView close = balloonView.getCloseView();
		
		final LinearLayout layout = balloonView.getLayout();
		  
		close.setOnClickListener(new OnClickListener() {
									public void onClick(View v) {
											layout.setVisibility(View.GONE);
											couponLayout.setVisibility(View.GONE);
											titleBar.setVisibility(View.GONE);
											mapView.getZoomButtonsController().setVisible(true);
											mapView.invalidate();
									}
								});
		}
		
		return true;
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
