package smartrek.mappers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import smartrek.db.PicuteDataBase;
import smartrek.models.Coupon;
import smartrek.models.User;
import smartrek.util.HTTP;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public final class CouponMapper extends Mapper {
	
	public enum Flag {
		All, Received, Sent
	}
	
	public CouponMapper() {
		
	}
	
	/**
	 * 
	 * @param uid User ID
	 * @return
	 */
	public ArrayList<Coupon> getCoupons(int uid, Flag flag) {
		
		Log.d("Coupon_Communicator","In Coupon_Communicator");
		Log.d("Coupon_Communicator","Begining Download");
		
		String url = null;
		if(Flag.All.equals(flag)) {
			url = "http://50.56.81.42:8080/getusercoupons/" + uid;
		}
		else if(Flag.Received.equals(flag)) {
			url = "http://50.56.81.42:8080/couponsharing-requestview/receiveruid=" + uid;
		}
		else if(Flag.Sent.equals(flag)) {
			url = "http://50.56.81.42:8080/couponsharing-sendview/senderuid=" + uid;
		}
		
		// FIXME:
		String response = HTTP.downloadText(url);

		ArrayList<Coupon> coupons = new ArrayList<Coupon>();
		
		try{
			JSONArray array = new JSONArray(response);
			for(int i = 0; i < array.length(); i++) {
				JSONObject obj = (JSONObject) array.get(i);
				
				// FIXME: Need to parse all fields...
				Coupon coupon = new Coupon();
				coupon.setTid(obj.getInt("TID"));
				coupon.setDid(obj.getInt("DID"));
				coupon.setVender(obj.getString("VENDOR"));
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date validDate = dateFormat.parse(obj.getString("VALID_DATE"));
				
				coupon.setValidDate(validDate);
				coupon.setDescription(obj.getString("DESCRIPTION"));
				coupon.setImageUrl(obj.getString("IMAGE_URL"));

				coupons.add(coupon);
			}
			
		}
		catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		
		Log.d("Coupon_Communicator","Got " + coupons.size() +" parsed Coupons");
		return coupons;
	}
	
	public void sendCouponTo(Coupon coupon, int senderUid, int receiverUid) {
		String url = String.format("http://50.56.81.42:8080/couponsharing-send/senderuid=%d%%20receiveruid=%d%%20did=%d", senderUid, receiverUid, coupon.getDid());
		
		String response = HTTP.downloadText(url);
		Log.d("CouponMapper", "url = " + url);
		Log.d("CouponMapper", "sendCouponTo(): response = " + response);
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
    			Log.d("Coupon_Communicator","Downloading image for " + coupon.getVendor());
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