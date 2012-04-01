package smartrek.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/****************************************************************************************************
 * 
 * 
 * @author timothyolivas
 *
 ****************************************************************************************************/
public class Coupon implements Parcelable {

	private int tid;
	private int cid;
	private int did;
	private String vendor;
	private String description;
	private Date validDate;
	private String imageUrl;
	private Bitmap image;
	private boolean bitmapset;
	private List<Integer> associatedRoutes;
	
	public static final Parcelable.Creator<Coupon> CREATOR = new Parcelable.Creator<Coupon>() {
		public Coupon createFromParcel(Parcel in) {
			return new Coupon(in);
		}

		public Coupon[] newArray(int size) {
			return new Coupon[size];
		}
	};
	
	public Coupon() {
		
	}
	
	public Coupon(Parcel in) {
		cid = in.readInt();
		did = in.readInt();
		vendor = in.readString();
		description = in.readString();
		validDate = (Date) in.readValue(Date.class.getClassLoader());
		imageUrl = in.readString();
		image = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
	}
	
	/****************************************************************************************************
	 * public Coupon(String vname,String desc, String date, String url)
	 * 
	 * 
	 * 
	 *
	 ****************************************************************************************************/
	public Coupon(int did, String vname,String desc, String date, String url) {
		this.did = did;
		this.bitmapset = false;
		this.vendor = vname;
		this.description = desc;
		this.validDate = parse_date(date); 
		this.imageUrl = url;
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
		this.did = bundle.getInt("did");
		this.vendor = bundle.getString("vendor_name");
		this.description = bundle.getString("description");
		
		int day = bundle.getInt("valid_day");
		int month = bundle.getInt("valid_month");
		int year = bundle.getInt("valid_year");
		
		this.validDate = new Date(year, month, day);
		this.imageUrl = bundle.getString("image_url");	
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
	
	/**************************************************************************
	 * private Date parse_date(String date)
	 * 
	 *
	 **************************************************************************/
	private Date parse_date(String date) {
		// FIXME: Use StringBuffer instead of String concatenation
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
	
	public int getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}
	
	public String getVendor() {
		return vendor;
	}
	
	public void setVender(String vendor) {
		this.vendor = vendor;
	}

	public boolean bitmapSet() {
		return bitmapset;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public Date getValidDate() {
		return validDate;
	}
	
	public void setValidDate(Date date) {
		this.validDate = date;
	}

	public int getDID() {
		return did;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String url) {
		this.imageUrl = url;
	}

	public Bitmap getBitmap() {
		return image;
	}

	public void setBitmap(Bitmap bitmap) {
		   int width = bitmap.getWidth();
	        int height = bitmap.getHeight();
	        int newWidth = width/2;
	        int newHeight = 95;
	       
	        // calculate the scale - in this case = 0.4f
	        float scaleWidth = ((float) newWidth) / width;
	        float scaleHeight = ((float) newHeight) / height;
	       
	        // create matrix for the manipulation
	        Matrix matrix = new Matrix();
	        // resize the bit map
	        matrix.postScale(scaleWidth, scaleHeight);
	 
	        // recreate the new Bitmap
	        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
	                          width, height, matrix, true);
	        this.bitmapset = true;
		    this.image = resizedBitmap;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append("Vendor = " + vendor + "\n");
		buf.append("Decription = " + description + "\n");
		buf.append("Valid Date = " + validDate.toLocaleString() + "\n");
		buf.append("Image Url = " + imageUrl + "\n");
		
		return new String(buf);
	}

	public void putOntoBundle(Bundle bundle) {
		bundle.putInt("did", did);
		bundle.putString("vendor_name", vendor);
		bundle.putString("description", description);
		bundle.putInt("valid_day", validDate.getDay());
		bundle.putInt("valid_month", validDate.getMonth());
		bundle.putInt("valid_year", validDate.getYear());
		bundle.putString("image_url", imageUrl);
		bundle.putParcelable("image", image);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(cid);
		dest.writeInt(did);
		dest.writeString(vendor);
		dest.writeString(description);
		dest.writeValue(validDate);
		dest.writeString(imageUrl);
		dest.writeValue(image);
	}
}
