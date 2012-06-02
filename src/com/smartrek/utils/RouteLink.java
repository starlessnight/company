package com.smartrek.utils;

/**
 * A class contains two route nodes to form a link.
 *
 */
public class RouteLink {
    public RouteNode node1;
    public RouteNode node2;
    
    public RouteLink(RouteNode node1, RouteNode node2) {
        this.node1 = node1;
        this.node2 = node2;
    }
    
    public float distanceTo(float lat, float lng) {
        // FIXME: Not implemented
        return 0.0f;
    }
}
