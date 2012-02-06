package smartrek.overlays;

import java.util.ArrayList;

import smartrek.AdjustableCouponDisplay.CouponLayout;
import smartrek.activities.ConfirmTripActivity;
import smartrek.mapviewballon.BalloonItemizedOverlay;
import smartrek.models.Coupon;
import smartrek.models.Route;




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

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class RouteOverlay extends BalloonItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	private CouponLayout couponLayout;
	private TextView titleBar;
	private Route route;
//	private Bitmap bitmap;
	private boolean enabled;
	private int selectedRoute;
	
	public RouteOverlay(Drawable defaultMarker, MapView mapview){ // Context context) {
		  super(boundCenter(defaultMarker),mapview);
		  enabled = false;
		  mContext = mapview.getContext();
	}
	
	public void setCouponLayout(CouponLayout couponlayout, TextView titleBar){
		this.couponLayout = couponlayout;
		this.titleBar = titleBar;
		enabled = true;
	}
	
	public void setRoute(Route route,int routeNum){
		this.route = route;
		this.selectedRoute = routeNum;
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}

	@Override
	public int size() {
	  return mOverlays.size();
	}
	
	@Override
	protected final boolean onTap(int index) {
		
		currentFocussedIndex = index;
		currentFocussedItem = createItem(index);
		
		createAndDisplayBalloonOverlay();

		mc.animateTo(currentFocussedItem.getPoint());
		if(enabled) {
			couponLayout.setCoupons(route.getAllCoupons());
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
		if (enabled) {
			Log.d("RouteOverlay","Bitmap for coupon set, Continuing to Confirm_Trip_Activity");
			Intent intent = new Intent(mContext, ConfirmTripActivity.class);
			
			Bundle extras = new Bundle();
			extras.putParcelable("image", route.getDiscount().getBitmap());

			Coupon cp = route.getDiscount();
			extras.putString("Coupon Description", cp.getDescription());
			extras.putString("Vendor Name", cp.getVendorName());
			extras.putString("Valid Date", cp.getDate().toGMTString());
			extras.putInt("selected route", selectedRoute);
			
			route.putOntoBundle(extras);
			
			intent.putExtras(extras);
			mContext.startActivity(intent);
			
		} else {
			Log.d("RouteOverlay","Bitmap for coupon not set, won't continue to next Activity");
		}
	  return true;
	}
}
