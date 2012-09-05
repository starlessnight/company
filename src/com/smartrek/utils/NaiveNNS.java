package com.smartrek.utils;

import java.util.List;

/**
 * Naive nearest neighbor search. This class provides utility functions to find
 * the nearest route node for a given GPS coordinate.
 * 
 * This will be replaced by k-d tree later on.
 *
 */
public class NaiveNNS {

    public static RouteNode findClosestNode(List<RouteNode> nodes, double lat, double lng) {
    	double minDistance = Double.MAX_VALUE;
        RouteNode cloestNode = nodes.get(0);
        
        for (RouteNode node : nodes) {
            double distance = LocationService.distanceBetween(node.getLatitude(), node.getLongitude(), lat, lng);
            
            if (distance < minDistance) {
                minDistance = distance;
                cloestNode = node;
            }
        }
        
        return cloestNode;
    }
}
