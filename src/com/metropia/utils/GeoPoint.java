package com.metropia.utils;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Google's GeoPoint implementation is capable of storing geolocation in six
 * digit floating point precision. However, OpenStreetMap (OSM) provides seven
 * digit precision, and osmdroid tries to remain compatible with Google APIs.
 * Thus, we needed out own GeoPoint to take a full advantage of OSM data.
 * 
 * @author Sumin Byeon
 * 
 */
public class GeoPoint implements Parcelable, Serializable {

	/**
	 * Auto-generated serial UID.
	 */
	private static final long serialVersionUID = 6259134118438831393L;

	private double latitude;
	private double longitude;
	private int latitudeE6;
	private int longitudeE6;
	private float heading;
	
	public GeoPoint(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.latitudeE6 = (int)(latitude * 1E6);
		this.longitudeE6 = (int)(longitude * 1E6);
	}
	
	public GeoPoint(double latitude, double longitude, float heading) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.latitudeE6 = (int)(latitude * 1E6);
		this.longitudeE6 = (int)(longitude * 1E6);
		this.heading = heading;
	}
	
	public GeoPoint(int latitudeE6, int longitudeE6) {
		this.latitudeE6 = latitudeE6;
		this.longitudeE6 = longitudeE6;
		this.latitude = latitudeE6 / 1E6;
		this.longitude = longitudeE6 / 1E6;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public int getLatitudeE6() {
		return latitudeE6;
	}
	
	public int getLongitudeE6() {
		return longitudeE6;
	}
	
	public boolean isEmpty(){
	    return latitude == 0 && longitude == 0;
	}

	// ===========================================================
	// Parcelable
	// ===========================================================
	private GeoPoint(final Parcel in) {
		this.latitude = in.readDouble();
		this.longitude = in.readDouble();
		this.latitudeE6 = in.readInt();
		this.longitudeE6 = in.readInt();
		this.heading = in.readFloat();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {
		out.writeDouble(latitude);
		out.writeDouble(longitude);
		out.writeInt(latitudeE6);
		out.writeInt(longitudeE6);
		out.writeFloat(heading);
	}

	public static final Parcelable.Creator<GeoPoint> CREATOR = new Parcelable.Creator<GeoPoint>() {
		@Override
		public GeoPoint createFromParcel(final Parcel in) {
			return new GeoPoint(in);
		}

		@Override
		public GeoPoint[] newArray(final int size) {
			return new GeoPoint[size];
		}
	};

	public float getHeading() {
		return heading;
	}

	public void setHeading(float heading) {
		this.heading = heading;
	}
	
	public void updateLocation(double lat, double lon, float heading) {
		this.latitude = lat;
		this.longitude = lon;
		this.latitudeE6 = (int)(latitude * 1E6);
		this.longitudeE6 = (int)(longitude * 1E6);
		this.heading = heading;
	}
}
