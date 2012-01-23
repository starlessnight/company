package SmarTrek.AndroidPlatform.SeverCommunicator;

import java.util.ArrayList;
import org.json.JSONException;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import SmarTrek.AndroidPlatform.DataBaseFunctions.PicuteDataBase;
import SmarTrek.AndroidPlatform.Parsers.Parser;
import SmarTrek.AndroidPlatform.Utilities.Coupon;

public class Coupon_Communicator extends Server_Communicator {
	
	public Coupon_Communicator() {
		
	}
	
	public ArrayList<Coupon> discount() {
		
		Log.d("Coupon_Communicator","In Coupon_Communicator");
		Log.d("Coupon_Communicator","Begining Download");
		
		String route_response = DownloadText(sturl + appendToUrl());

		ArrayList<Coupon> coupons = null;
		
		try{
			coupons = Parser.parse_Coupon_List(route_response);
		} catch (JSONException e) {
				e.printStackTrace();
			}	
		Log.d("Coupon_Communicator","Got " + coupons.size() +" parsed Coupons");
		return coupons;
	}
	
	@Override
	protected String appendToUrl() {
		return "/discount";
	}
	
	public void doCouponBitmapDownloads(ArrayList<Coupon> coupons, Context context) {
    	Image_Communicator icom = new Image_Communicator();
    	
    	PicuteDataBase pDat = new PicuteDataBase(context);
    	pDat.open();
    	
    	Cursor cursor = null;
    	
    	Log.d("Coupon_Communicator","Downloading Coupon Images");
    	for (int i = 0; i < coupons.size(); i++) {
    		Coupon coupon = coupons.get(i);
    	
    		cursor = pDat.getPictue(coupon.getImageUrl());
    		
    		if(cursor == null || cursor.getCount() == 0) {
    			Log.d("Coupon_Communicator","Downloading image for " + coupon.getVendorName());
    			Bitmap bitmap = icom.DownloadImage(coupon.getImageUrl());
    			coupon.setBitmap(bitmap);
    			Log.d("Coupon_Communicator","Storing bitmap for " + coupon.getImageUrl() + " In Database");
    			pDat.insertPicture(coupon.getImageUrl(), bitmap);
    		} else if(cursor.getString(1).equals(coupon.getImageUrl())){
    			Log.d("Coupon_Communicator","Image found in Database. Loading...");
    			Bitmap bitmap = BitmapFactory.decodeByteArray(cursor.getBlob(2), 0,cursor.getBlob(2).length);
    			coupon.setBitmap(bitmap);
    		}
    	}
    	if(cursor != null)
    		if(!cursor.isClosed())
    			cursor.close();
  
    	pDat.close();		
	}
}