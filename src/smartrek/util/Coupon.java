package smartrek.util;

import java.util.ArrayList;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;

/****************************************************************************************************
 * 
 * 
 * @author timothyolivas
 *
 ****************************************************************************************************/
public class Coupon {


	private int DID;
	private String vendor_name;
	private String description;
	private Date valid_date;
	private String image_url;
	private Bitmap image;
	private boolean bitmapset;
	private ArrayList<Integer> associatedRoutes;
	
	/****************************************************************************************************
	 * public Coupon(String vname,String desc, String date, String url)
	 * 
	 * 
	 * 
	 *
	 ****************************************************************************************************/
	public Coupon(int did, String vname,String desc, String date, String url) {
		this.DID = did;
		this.bitmapset = false;
		this.vendor_name = vname;
		this.description = desc;
		this.valid_date = parse_date(date); 
		this.image_url = url;
		this.associatedRoutes = new ArrayList<Integer>();
	}
	
	/****************************************************************************************************
	 * public Coupon(Bundle bundle)
	 * 
	 * 
	 * 
	 *
	 ****************************************************************************************************/
	public Coupon(Bundle bundle) {
		this.DID = bundle.getInt("did");
		this.vendor_name = bundle.getString("vendor_name");
		this.description = bundle.getString("description");
		
		int day = bundle.getInt("valid_day");
		int month = bundle.getInt("valid_month");
		int year = bundle.getInt("valid_year");
		
		this.valid_date = new Date(year, month, day);
		this.image_url = bundle.getString("image_url");	
	}
	
	/****************************************************************************************************
	 * public Coupon(String vname,String desc, String date, String url)
	 * 
	 * 
	 * 
	 *
	 ****************************************************************************************************/
//	public Coupon(String vname,String desc, String date, String url) {
//		this.vendor_name = vname;
//		this.description = desc;
//		if(!date.equals(""))
//		this.valid_date = parse_date(date); 
//		this.image_url = url;
//	}
	
	/****************************************************************************************************
	 * private Date parse_date(String date)
	 * 
	 *
	 ****************************************************************************************************/
	private Date parse_date(String date) {
		String temp = "";
		int i = 0;
		while(date.charAt(i) != '-'){
			temp += date.charAt(i);
			i++;
		}
		i++;
		int year = Integer.parseInt(temp);
		temp = "";
		while(date.charAt(i) != '-'){
			temp += date.charAt(i);
			i++;
		}
		i++;
		int month = Integer.parseInt(temp);
		temp = "";
		while(i < date.length()){
			temp += date.charAt(i);
			i++;
		}
		int day = Integer.parseInt(temp);
		return new Date(year,month,day);
	}
	
	/****************************************************************************************************
	 * public String getVendorName()
	 * 
	 *
	 ****************************************************************************************************/
	public String getVendorName() {
		return vendor_name;
	}
	
	public boolean bitmapSet() {
		return bitmapset;
	}
	/****************************************************************************************************
	 * public String getDescription()
	 * 
	 *
	 ****************************************************************************************************/
	public String getDescription() {
		return description;
	}
	
	/****************************************************************************************************
	 * public Date getDate() 
	 * 
	 *
	 ****************************************************************************************************/
	public Date getDate() {
		return valid_date;
	}
	
	/****************************************************************************************************
	 * public int getDID() 
	 * 
	 *
	 ****************************************************************************************************/
	public int getDID() {
		return DID;
	}
	
	/****************************************************************************************************
	 * public String getImageUrl()
	 * 
	 *
	 ****************************************************************************************************/
	public String getImageUrl() {
		return image_url;
	}
	
	/****************************************************************************************************
	 * public Bitmap getBitmap() 
	 * 
	 *
	 ****************************************************************************************************/
	public Bitmap getBitmap() {
		return image;
	}
	
	/****************************************************************************************************
	 * public void setBitmap(Bitmap bitmap) 
	 * 
	 *
	 ****************************************************************************************************/
	public void setBitmap(Bitmap bitmap) {
		   int width = bitmap.getWidth();
	        int height = bitmap.getHeight();
	        int newWidth = width/2;
	        int newHeight = 95;
	       
	        // calculate the scale - in this case = 0.4f
	        float scaleWidth = ((float) newWidth) / width;
	        float scaleHeight = ((float) newHeight) / height;
	       
	        // createa matrix for the manipulation
	        Matrix matrix = new Matrix();
	        // resize the bit map
	        matrix.postScale(scaleWidth, scaleHeight);
	 
	        // recreate the new Bitmap
	        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
	                          width, height, matrix, true);
	        this.bitmapset = true;
		    this.image = resizedBitmap;
	}
	
	/****************************************************************************************************
	 * public String toString()
	 * 
	 *
	 ****************************************************************************************************/
	public String toString() {
		String str = "";
		str += "Vendor = " + vendor_name + "\n";
		str += "Decription = " + description + "\n";
		str += "Valid Date = " + valid_date.toLocaleString() + "\n";
		str += "Image Url = " + image_url + "\n"; 
		return str;
	}

	/****************************************************************************************************
	 * public void puOntoBundle(Bundle bundle)
	 * 
	 *
	 ****************************************************************************************************/
	public void putOntoBundle(Bundle bundle) {
		bundle.putInt("did", DID);
		bundle.putString("vendor_name", vendor_name);
		bundle.putString("description", description);
		bundle.putInt("valid_day", valid_date.getDay());
		bundle.putInt("valid_month", valid_date.getMonth());
		bundle.putInt("valid_year", valid_date.getYear());
		bundle.putString("image_url", image_url);
		bundle.putParcelable("image", image);
	}
}
