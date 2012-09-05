package com.smartrek.utils;

import java.util.List;

import android.util.Log;

/**
 * This class provides functionalities related to the route validation. 
 *
 */
public final class ValidationService {

    public static RouteNode getNearestNode(List<RouteNode> nodes, double lat, double lng) {
        RouteNode nearestNode = NaiveNNS.findClosestNode(nodes, lat, lng);
        
        return nearestNode;
    }
    
    /**
     * @param node
     * @param lat
     * @param lng
     * @return
     */
    public static RouteLink getNearestLink(RouteNode node, double lat, double lng) {
    	RouteNode prevNode = node.getPrevNode();
    	RouteNode nextNode = node.getNextNode();
    	
    	if (prevNode != null && nextNode != null) {
    		RouteLink prevLink = new RouteLink(node.getPrevNode(), node);
    		RouteLink nextLink = new RouteLink(node, node.getNextNode()); 

    		double distanceToPrev = prevLink.distanceTo(lat, lng);
    		double distanceToNext = nextLink.distanceTo(lat, lng);
    		
    		return distanceToPrev < distanceToNext ? new RouteLink(prevNode, node) : new RouteLink(node, nextNode);
    	}
    	else if (node.getPrevNode() != null) {
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
    
}
