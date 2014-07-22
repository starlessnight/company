package com.smartrek.utils;

import java.util.Collection;
import java.util.List;


public class RouteRect {
	
    private static final double padding = 1.15;
    
    private static final int minRange = 100;
    
	int latMax;
	int lonMax;
	int latMin;
	int lonMin;
	
	private double verticalOffest;
	
	public RouteRect(int latMax, int lonMax, int latMin, int lonMin) {
	    this.latMax = latMax;
	    this.lonMax = lonMax;
	    this.latMin = latMin;
	    this.lonMin = lonMin;
	}
	
	public RouteRect(List<RouteNode> nodes) {
        int latMax = (int) (-81 * 1E6);
        int lonMax = (int) (-181 * 1E6);
        int latMin = (int) (+81 * 1E6);
        int lonMin = (int) (+181 * 1E6);
        for (int i = 0; i < nodes.size(); i++) {
            GeoPoint point = nodes.get(i).getGeoPoint();
            int curLat = point.getLatitudeE6();
            int curLon = point.getLongitudeE6();
            latMax = Math.max(latMax, curLat);
            lonMax = Math.max(lonMax, curLon);
            latMin = Math.min(latMin, curLat);
            lonMin = Math.min(lonMin, curLon);
        }
        this.latMax = latMax;
        this.lonMax = lonMax;
        this.latMin = latMin;
        this.lonMin = lonMin;
    }
	
	public RouteRect(List<RouteNode> nodes, double verticalOffest) {
		this(nodes);
		this.verticalOffest = verticalOffest;
	}
	
	public RouteRect(Collection<GeoPoint> nodes, double verticalOffest) {
	    this.verticalOffest = verticalOffest;
        int latMax = (int) (-81 * 1E6);
        int lonMax = (int) (-181 * 1E6);
        int latMin = (int) (+81 * 1E6);
        int lonMin = (int) (+181 * 1E6);
        for (GeoPoint point : nodes) {
            int curLat = point.getLatitudeE6();
            int curLon = point.getLongitudeE6();
            latMax = Math.max(latMax, curLat);
            lonMax = Math.max(lonMax, curLon);
            latMin = Math.min(latMin, curLat);
            lonMin = Math.min(lonMin, curLon);
        }
        this.latMax = latMax;
        this.lonMax = lonMax;
        this.latMin = latMin;
        this.lonMin = lonMin;
    }
    
    public GeoPoint getMidPoint(){
        return new GeoPoint((latMax + latMin) / 2 + Double.valueOf((latMax - latMin) * verticalOffest).intValue(), 
            (lonMax + lonMin) / 2);
    }
    
    public int[] getRange(){
    	return new int[]{
	        Math.max(Double.valueOf((latMax - latMin) * (1 + Math.abs(verticalOffest)) * padding).intValue(), minRange), 
	        Math.max(Double.valueOf((lonMax - lonMin) * padding).intValue(), minRange)
        };
    }
	
}