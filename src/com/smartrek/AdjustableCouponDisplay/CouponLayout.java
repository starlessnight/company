package com.smartrek.AdjustableCouponDisplay;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;

import com.smartrek.activities.RouteActivity;
import com.smartrek.models.Coupon;
import com.smartrek.models.Route;

/*********************************************************************************************************
 * 
 * 
 * 
 *********************************************************************************************************/
public class CouponLayout extends LinearLayout implements OnLongClickListener {

	private RouteActivity map_activity;
	private ArrayList<Coupon> coupons;
	private List<Route> routes;
	
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
	public void setMapActivity(RouteActivity map_activity){
		this.map_activity = map_activity;
	}
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public void setRoutes(List<Route> routes){
		this.routes = routes;
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
     		Log.d("CouponLayout", "Coupon for " + coupons.get(i).getVendor());
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
		}
		return false;
	}

}
