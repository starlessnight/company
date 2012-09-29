package com.smartrek.utils;

import org.osmdroid.api.IGeoPoint;

/**
 * Google's GeoPoint implementation is capable of storing geolocation in six
 * digit floating point precision. However, OpenStreetMap (OSM) provides seven
 * digit precision, and osmdroid tries to remain compatible with Google APIs.
 * Thus, we needed out own GeoPoint to take a full advantage of OSM data.
 * 
 * @author Sumin Byeon
 * 
 */
public class GeoPoint extends org.osmdroid.util.GeoPoint {

	/**
	 * Auto-generated serial UID.
	 */
	private static final long serialVersionUID = 6259134118438831393L;

	private double latitude;
	private double longitude;
	
	public GeoPoint(double latitude, double longitude) {
		super(latitude, longitude);
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public GeoPoint(int latitudeE6, int longitudeE6) {
		super(latitudeE6, longitudeE6);
		this.latitude = latitudeE6 / 1E6;
		this.longitude = longitudeE6 / 1E6;
	}
	
	public GeoPoint(IGeoPoint geoPoint) {
		this(geoPoint.getLatitudeE6(), geoPoint.getLongitudeE6());
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
}
