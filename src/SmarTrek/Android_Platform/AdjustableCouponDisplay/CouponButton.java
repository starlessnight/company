package SmarTrek.Android_Platform.AdjustableCouponDisplay;

import SmarTrek.Android_Platform.R;
import SmarTrek.Android_Platform.AdjustableTimeDisplay.TimeButton;
import SmarTrek.Android_Platform.Utilities.Coupon;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;


public class CouponButton extends ImageView {

	private CouponLayout couponLayout;
	private int cpnum;
	private boolean enabled;
	private Coupon coupon;
	
	public CouponButton(CouponLayout couponLayout,int cpnum){
		super(couponLayout.getContext());
		this.couponLayout = couponLayout;
		this.cpnum = cpnum;
		this.setOnLongClickListener(couponLayout);
		this.setParams();
		this.setId(cpnum);
	}
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	private void setParams() {
		this.enabled = false;
		this.setPadding(4, 2, 4, 2);
		this.setBackgroundColor(Color.parseColor("#FFFFFFFF"));

		
		
		this.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.icon));
		
		if(cpnum != 0)
			this.setAlpha(120);
		
	}
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rect = new Rect();
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#3f3e40"));
        paint.setStrokeWidth(3);
        getLocalVisibleRect(rect);
        canvas.drawRect(rect, paint);       
    }
    
    public boolean enabled(){
    	return enabled;
    }
    
    public void setA(int alpha){
    	BitmapDrawable bitdraw = new BitmapDrawable(coupon.getBitmap());
    	
    	bitdraw.setAlpha(alpha);
    	
    	this.setBackgroundDrawable(bitdraw);
    }
    
   
    public void setCoupon(Coupon coupon){
    	this.coupon = coupon;
    	Log.d("CouponButton","Setting New Coupon Button");
    	Log.d("CouponButton","Coupon Button for " + coupon.getVendorName());
    	if(coupon.bitmapSet()){
    		Log.d("CouponButton","Bitmapset");
    	} else {
    		Log.d("CouponButton","Bitmap NOT set");
    	}
    	
    	BitmapDrawable bitdraw = new BitmapDrawable(coupon.getBitmap());
    	
		if(cpnum != 0)
			bitdraw.setAlpha(100);
    	this.setBackgroundDrawable(bitdraw);
    	this.enabled = true;
    }
		
}
