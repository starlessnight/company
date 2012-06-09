package com.smartrek.utils;

import android.util.FloatMath;
import android.util.Log;

/**
 * A class contains two route nodes to form a link.
 *
 */
public class RouteLink {
    private RouteNode node1;
    private RouteNode node2;
    
    public RouteLink(RouteNode node1, RouteNode node2) {
        this.node1 = node1;
        this.node2 = node2;
    }
    
    public RouteNode getStartNode() {
    	return node1;
    }
    
    public RouteNode getEndNode() {
    	return node2;
    }
    
    /**
     * Unit is in meter
     * @param lat
     * @param lng
     * @return
     */
    public float distanceTo(float lat, float lng) {
        
        float cosAngle1 = FloatMath.cos(angle1(lat, lng));
        float cosAngle2 = FloatMath.cos(angle2(lat, lng));
        
        if (cosAngle1 > 0.0f && cosAngle2 > 0.0f) {
            // case 1 (distance between the link and a point)
        	
        	// Equations are taken from http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
        	float rx = lng - node1.getLongitude();
        	float ry = lat - node1.getLatitude();
        	Vector2D r = new Vector2D(rx, ry);
        	
        	float ux = node1.getLongitude() - node2.getLongitude();
        	float uy = node1.getLatitude() - node2.getLatitude();
        	Vector2D v = new Vector2D(uy, ux); // normal vector of (ux, uy)
        	
        	float dv = RouteNode.distanceBetween(node1, node2);
        	float dr = RouteNode.distanceBetween(node1.getLatitude(), node1.getLongitude(), lat, lng);
        	float d = dr * FloatMath.sin(angle1(lat, lng));
        	
        	Log.d("RouteLink", String.format("Case 1, distance = %f", d));
        	
        	return d;
        }
        else if (cosAngle1 <= 0.0f && cosAngle2 > 0.0f) {
            // case 2 (distance between node1 and a point)
        	Log.d("RouteLink", String.format("Case 2, distance = %f", node1.distanceTo(lat, lng)));
        	return node1.distanceTo(lat, lng);
        }
        else if (cosAngle1 > 0.0f && cosAngle2 <= 0.0f) {
            // case 3 (distance between node2 and a point)
        	Log.d("RouteLink", String.format("Case 3, distance = %f", node2.distanceTo(lat, lng)));
        	return node2.distanceTo(lat, lng);
        }
        else {
            Log.d("RouteLink", "Should not reach here");
        }
        
        return 0.0f;
    }

    /**
     * Angle between this link and another link drawn to a given geocoordinate from node1.
     * 
     * @param lat
     * @param lng
     * @return
     */
    public float angle1(float lat, float lng) {
        float ax = node2.getLongitude() - node1.getLongitude();
        float ay = node2.getLatitude() - node1.getLatitude();
        
        float bx = lng - node1.getLongitude();
        float by = lat - node1.getLatitude();
        
        Vector2D u = new Vector2D(ax, ay);
        Vector2D v = new Vector2D(bx, by);
        
        return Vector2D.angleBetween(u, v);
    }
    
    public float angle2(float lat, float lng) {
        float ax = node1.getLongitude() - node2.getLongitude();
        float ay = node1.getLatitude() - node2.getLatitude();
        
        float bx = lng - node2.getLongitude();
        float by = lat - node2.getLatitude();
        
        Vector2D u = new Vector2D(ax, ay);
        Vector2D v = new Vector2D(bx, by);
        
        return Vector2D.angleBetween(u, v);
    }
}
