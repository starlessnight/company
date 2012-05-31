package com.smartrek.mappers;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smartrek.models.Coupon;
import com.smartrek.utils.HTTP;

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
		
		String url = null;
		if(Flag.All.equals(flag)) {
			url = String.format("%s/getusercoupons/%d", host, uid);
		}
		else if(Flag.Received.equals(flag)) {
			url = String.format("%s/couponsharing-requestview/receiveruid=%d", host, uid);
		}
		else if(Flag.Sent.equals(flag)) {
			url = String.format("%s/couponsharing-sendview/senderuid=%d", host, uid);
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
			throw new IOException(String.format("HTTP %d - %s", responseCode, http.getResponseBody()));
		}

		return coupons;
	}
	
	/**
	 * Takes care of image download process for a coupon
	 * 
	 * @param coupon
	 * @throws IOException 
	 */
	public void downloadImage(Coupon coupon) throws IOException {
		HTTP http = new HTTP(coupon.getImageUrl());
		http.connect();
		
		int responseCode = http.getResponseCode();
		if (responseCode == 200) {
			InputStream in = http.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(in);
			
			coupon.setBitmap(bitmap);
		}
		else {
			throw new IOException(String.format("HTTP %d", responseCode));
		}
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
}