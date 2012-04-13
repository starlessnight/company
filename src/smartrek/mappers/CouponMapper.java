package smartrek.mappers;

import java.io.IOException;
import java.net.ConnectException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import smartrek.db.PicuteDataBase;
import smartrek.models.Coupon;
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
	 * @throws JSONException 
	 * @throws ParseException 
	 */
	public ArrayList<Coupon> getCoupons(int uid, Flag flag) throws ConnectException, IOException, JSONException, ParseException {
		
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
		
		// FIXME: Handle a case where flag = null
		
		HTTP http = new HTTP(url);
		http.connect();
		
		ArrayList<Coupon> coupons = null;

		int responseCode = http.getResponseCode();
		if(responseCode == 200) {
			String responseBody = http.getResponseBody();
			
			Log.d("CouponMapper", "response = " + responseBody);
			
			coupons = new ArrayList<Coupon>();			
			JSONArray array = new JSONArray(responseBody);
			for(int i = 0; i < array.length(); i++) {
				JSONObject obj = (JSONObject) array.get(i);
				
				// FIXME: Need to parse all fields...
				Coupon coupon = new Coupon();
				coupon.setTid(obj.getInt("TID"));
				coupon.setDid(obj.getInt("DID"));
				coupon.setVender(obj.getString("VENDOR"));
				
				if (Flag.Received.equals(flag) && obj.has("UID")) {
					coupon.setSenderUid(obj.getInt("UID"));
					coupon.setReceiverUid(uid);
				}
				else if(Flag.Sent.equals(flag)) {
					coupon.setSenderUid(uid);
					coupon.setReceiverUid(obj.getInt("UID"));
				}
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date validDate = dateFormat.parse(obj.getString("VALID_DATE"));
				
				coupon.setValidDate(validDate);
				coupon.setDescription(obj.getString("DESCRIPTION"));
				coupon.setImageUrl(obj.getString("IMAGE_URL"));

				coupons.add(coupon);
			}
		}
		else {
			throw new IOException(String.format("HTTP %d", responseCode));
		}

		return coupons;
	}
	
	public void sendCouponTo(Coupon coupon, int senderUid, int receiverUid) throws IOException {
		String url = String.format("%s/couponsharing-send/senderuid=%d%%20receiveruid=%d%%20did=%d",
				host, senderUid, receiverUid, coupon.getDid());
		
		HTTP http = new HTTP(url);
		http.connect();
		
		int responseCode = http.getResponseCode();
		if(responseCode == 200) {
			Log.d("CouponMapper", "sendCouponTo - success");
		}
		else {
			throw new IOException(String.format("HTTP %d - %s", responseCode, http.getResponseBody()));
		}
	}
	
	public void cancelSentCoupon(Coupon coupon) throws IOException {
		String url = String.format("%s/couponsharing-cancel/senderuid=%d%%20receiveruid=%d%%20did=%d",
				host, coupon.getSenderUid(), coupon.getReceiverUid(), coupon.getDid());
		
		HTTP http = new HTTP(url);
		http.connect();
		
		int responseCode = http.getResponseCode();
		if(responseCode == 200) {
			Log.d("CouponMapper", "cancelSentCoupon - success");
		}
		else {
			throw new IOException(String.format("HTTP %d - %s", responseCode, http.getResponseBody()));
		}
	}
	
	public void acceptCoupon(Coupon coupon, int senderUid, int receiverUid) throws IOException {
		String url = String.format("%s/couponsharing-accept/senderuid=%d%%20receiveruid=%d%%20did=%d",
				host, senderUid, receiverUid, coupon.getDid());
		
		HTTP http = new HTTP(url);
		http.connect();
		
		Log.d("CouponMapper", "url = " + url);
		
		int responseCode = http.getResponseCode();
		if(responseCode == 200) {
			Log.d("CouponMapper", "acceptCoupon - success");
		}
		else {
			throw new IOException(String.format("HTTP %d - %s", responseCode, http.getResponseBody()));
		}
	}
	
	public void rejectCoupon(Coupon coupon, int senderUid, int receiverUid) throws IOException {
		String url = String.format("%s/couponsharing-decline/senderuid=%d%%20receiveruid=%d%%20did=%d",
				host, senderUid, receiverUid, coupon.getDid());
		
		HTTP http = new HTTP(url);
		http.connect();
		
		int responseCode = http.getResponseCode();
		if(responseCode == 200) {
			Log.d("CouponMapper", "rejectCoupon - success");
		}
		else {
			throw new IOException(String.format("HTTP %d - %s", responseCode, http.getResponseBody()));
		}
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