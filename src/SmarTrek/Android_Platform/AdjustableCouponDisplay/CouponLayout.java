package SmarTrek.Android_Platform.AdjustableCouponDisplay;

import java.util.ArrayList;

import SmarTrek.Android_Platform.Map_Activity;
import SmarTrek.Android_Platform.Utilities.Coupon;
import SmarTrek.Android_Platform.Utilities.Route;
import android.content.Context;
import android.opengl.Visibility;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;

/*********************************************************************************************************
 * 
 * 
 * 
 *********************************************************************************************************/
public class CouponLayout extends LinearLayout implements OnLongClickListener {

	private Map_Activity map_activity;
	private ArrayList<Coupon> coupons;
	private ArrayList<Route> routes;
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public CouponLayout(Context context, AttributeSet attributes) {
		super(context,attributes);
		int numboxes = 10;
	    for (int i = 0; i < numboxes; i++) {
	    	 CouponButton bt1 = new CouponButton(this, i);
	    	 this.addView(bt1,i);
		}
	    this.setVisibility(View.GONE);
	    this.invalidate();
		
	}
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public void setMapActivity(Map_Activity map_activity){
		this.map_activity = map_activity;
	}
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public void setRoutes(ArrayList<Route> routes){
		this.routes = routes;
		Log.d("CouponLayout","Setting " + routes.get(0).getAllCoupons().size() + " Coupons into Coupon Layout");
		setCoupons(routes.get(0).getAllCoupons());
	}
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public void setCoupons(ArrayList<Coupon> coupons) {
		this.removeAllViewsInLayout();
		this.coupons = coupons;
     	for (int i = 0; i < coupons.size(); i++) {
     		Log.d("CouponLayout","Adding View for Coupon " + i);
     		Log.d("CouponLayout", "Coupon for " + coupons.get(i).getVendorName());
     		CouponButton couponButton = new CouponButton(this, i);
    		couponButton.setCoupon(coupons.get(i));
	    	this.addView(couponButton,i);
    	}
		this.invalidate();
	}

	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	@Override
	public boolean onLongClick(View v) {
		CouponButton couponButton = (CouponButton) v;
		if (couponButton.enabled()) {
			Log.d("Coupon Layout", "Coupon Button: " + v.getId()
					+ "OnLongClick Registered");
			for (int i = 0; i < this.getChildCount(); i++) {
				((CouponButton) getChildAt(i)).setA(120);
			}
			couponButton.setA(255);
			
			for (int i = 0; i < routes.size(); i++) {
				routes.get(i).setCoupon(coupons.get(v.getId()));
			}
		}
		return false;
	}

}
