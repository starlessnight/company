package smartrek.overlays;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import smartrek.AdjustableCouponDisplay.CouponLayout;
import smartrek.activities.ReservationConfirmationActivity;
import smartrek.mappers.RouteMapper;
import smartrek.models.Route;
import smartrek.ui.mapviewballon.BalloonItemizedOverlay;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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

public class RouteOverlay extends BalloonItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context context;
	private CouponLayout couponLayout;
	private TextView titleBar;
	private Route route;
//	private Bitmap bitmap;
	private boolean enabled;
	private int selectedRoute;
	
	private OverlayItem infoOverlay;
	
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
		  
          infoOverlay = new OverlayItem(point,
                  "Route " + (0 + 1),
                  "Origin: \n" + route.getOrigin()  + " \n\n" +
                  "Destination: \n" + route.getDestination() + "\n\n" + 
                  "Estimated Travel Time: " + route.getMin() + " min\n" +
                  "Credits: " + route.getCredits() + "\n\n" +
                  "(Tap to reserve this route)");
          addOverlay(infoOverlay);
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
	
	public synchronized void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}

	@Override
	protected synchronized OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}

	@Override
	public synchronized int size() {
	  return mOverlays.size();
	}
	
	@Override
	protected final boolean onTap(int index) {
		Log.d("RouteOverlay", "onTab, index="+index);
		
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
		Log.d("RouteOverlay", String.format("index=%d, item=%s", index, item));
		
		Intent intent = new Intent(context, ReservationConfirmationActivity.class);
		Bundle extras = new Bundle();
		extras.putParcelable("route", route);
		intent.putExtras(extras);
		context.startActivity(intent);
		
		return true;
	}
	
	private final class RouteCreditsFetchTask extends AsyncTask<Object, Object, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {
			int credits = 0;
			RouteMapper mapper = new RouteMapper();
			try {
				credits = mapper.getRouteCredits(route.getId());
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			return credits;
		}

		@Override
		protected void onPostExecute(Integer credits) {
			Log.d("RouteOverlay", String.format("Route %d credits = %d", route.getId(), credits));
			route.setCredits(credits);
			
			// TODO: Updating OverlayItem here is going to be trickier than I thought
			// as OverlayItem does not provide any mechanism to update the snippet text.
		}
	}
}
