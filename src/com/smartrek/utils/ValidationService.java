package com.smartrek.utils;

import java.util.List;

import android.util.Log;

import com.smartrek.models.Route;

/**
 * This class provides functionalities related to the route validation. 
 *
 */
public final class ValidationService {

    public static RouteNode getNearestNode(List<RouteNode> nodes, float lat, float lng) {
        RouteNode nearestNode = NaiveNNS.findClosestNode(nodes, lat, lng);
        
        return nearestNode;
    }
    
    /**
     * FIXME: This is not fully implemented.
     * 
     * @param node
     * @param lat
     * @param lng
     * @return
     */
    public static RouteLink getNearestLink(RouteNode node, float lat, float lng) {
        if (node.getPrevNode() != null) {
            return new RouteLink(node.getPrevNode(), node);
        }
        else if (node.getNextNode() != null) {
            return new RouteLink(node, node.getNextNode());
        }
        else {
            Log.d("ValidationService", "Should not reach here. A route link must have at least one of prevNode and nextNode.");
            return null;
        }
    }
    
//    public float distanceBetween(RouteLink link, float lat, float lng) {
//        return 0.0f;
//    }
    
    public static boolean isInRoute(Route route, float lat, float lng) {
        RouteNode nearestNode = getNearestNode(route.getNodes(), lat, lng);
        RouteLink nearestLink = getNearestLink(nearestNode, lat, lng);
        
        ValidationParameters params = new ValidationParameters();
        return nearestLink.distanceTo(lat, lng) < params.getArrivalDistanceThreshold();
    }
    
}
