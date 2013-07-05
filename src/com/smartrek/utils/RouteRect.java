package com.smartrek.utils;

import java.util.List;


public class RouteRect {
	
    static final double padding = 1.15; 
    
	int latMax;
	int lonMax;
	int latMin;
	int lonMin;
	
	public RouteRect(List<RouteNode> nodes) {
        int latMax = (int) (-81 * 1E6);
        int lonMax = (int) (-181 * 1E6);
        int latMin = (int) (+81 * 1E6);
        int lonMin = (int) (+181 * 1E6);
        for (int i = 0; i < nodes.size() - 1; i++) {
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
    
    public GeoPoint getMidPoint(){
    	return new GeoPoint((latMax + latMin) / 2, (lonMax + lonMin) / 2);
    }
    
    public int[] getRange(){
    	return new int[]{Double.valueOf((latMax - latMin) * padding).intValue(), 
	        Double.valueOf((lonMax - lonMin) * padding).intValue()};
    }
	
}