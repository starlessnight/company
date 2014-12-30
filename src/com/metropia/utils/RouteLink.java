package com.metropia.utils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


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
    public double distanceTo(double lat, double lng) {
        
    	double cosAngle1 = Math.cos(angle1(lat, lng));
    	double cosAngle2 = Math.cos(angle2(lat, lng));
        
        if (cosAngle1 > 0.0f && cosAngle2 > 0.0f) {
            // case 1 (distance between the link and a point)
        	
        	// Equations are taken from http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
        	double rx = lng - node1.getLongitude();
        	double ry = lat - node1.getLatitude();
        	Vector2D r = new Vector2D(rx, ry);
        	
        	double ux = node1.getLongitude() - node2.getLongitude();
        	double uy = node1.getLatitude() - node2.getLatitude();
        	Vector2D v = new Vector2D(uy, ux); // normal vector of (ux, uy)
        	
        	double dv = RouteNode.distanceBetween(node1, node2);
        	double dr = RouteNode.distanceBetween(node1.getLatitude(), node1.getLongitude(), lat, lng);
        	double d = dr * Math.sin(angle1(lat, lng));
        	
        	//Log.d("RouteLink", String.format("Case 1, distance = %f", d));
        	
        	return d;
        }
        else if (cosAngle1 <= 0.0f && cosAngle2 > 0.0f) {
            // case 2 (distance between node1 and a point)
        	//Log.d("RouteLink", String.format("Case 2, distance = %f", node1.distanceTo(lat, lng)));
        	return node1.distanceTo(lat, lng);
        }
        else if (cosAngle1 > 0.0f && cosAngle2 <= 0.0f) {
            // case 3 (distance between node2 and a point)
        	//Log.d("RouteLink", String.format("Case 3, distance = %f", node2.distanceTo(lat, lng)));
        	return node2.distanceTo(lat, lng);
        }
        else {
            return node1.distanceTo(lat, lng);
        }
        
    }

    /**
     * Angle between this link and another link drawn to a given geocoordinate from node1.
     * 
     * @param lat
     * @param lng
     * @return
     */
    public double angle1(double lat, double lng) {
        double ax = node2.getLongitude() - node1.getLongitude();
        double ay = node2.getLatitude() - node1.getLatitude();
        
        double bx = lng - node1.getLongitude();
        double by = lat - node1.getLatitude();
        
        Vector2D u = new Vector2D(ax, ay);
        Vector2D v = new Vector2D(bx, by);
        
        return Vector2D.angleBetween(u, v);
    }
    
    public double angle2(double lat, double lng) {
        double ax = node1.getLongitude() - node2.getLongitude();
        double ay = node1.getLatitude() - node2.getLatitude();
        
        double bx = lng - node2.getLongitude();
        double by = lat - node2.getLatitude();
        
        Vector2D u = new Vector2D(ax, ay);
        Vector2D v = new Vector2D(bx, by);
        
        return Vector2D.angleBetween(u, v);
    }
    
    @Override
    public String toString() {
        return "{" + node1.getNodeNum() + "-" + node2.getNodeNum() + "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }
        RouteLink rhs = (RouteLink) obj;
        return new EqualsBuilder()
          .append(node1.getLatitude(), rhs.node1.getLatitude())
          .append(node1.getLongitude(), rhs.node1.getLongitude())
          .append(node2.getLatitude(), rhs.node2.getLatitude())
          .append(node2.getLongitude(), rhs.node2.getLongitude())
          .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(node1.getLatitude())
            .append(node1.getLongitude())
            .append(node2.getLatitude())
            .append(node2.getLongitude())
            .toHashCode();
    }
    
}
