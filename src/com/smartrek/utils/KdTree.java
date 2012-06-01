package com.smartrek.utils;

import java.util.List;

import com.google.android.maps.GeoPoint;

import android.util.Log;

/**
 * 
 *
 */
public class KdTree {
	
	public static int DIMENSION = 2;

	public static class Node {
		public RouteNode routeNode;
		public Node left;
		public Node right;
		
		public Node lookup(GeoPoint point) {
		    return null;
		}
	}
	
	/**
	 * Builds a K-d tree for a given list of nodes.
	 * NOTE: 'nodes' must be in order.
	 * 
	 * @param nodes
	 * @param l Left boundary of nodes (inclusive)
	 * @param r Right boundary of nodes (inclusive)
	 * @param depth
	 * @return
	 */
	public static Node build(List<RouteNode> nodes, int l, int r, int depth) {
		
	    Log.d("KdTree", String.format("left=%d, right=%d, depth=%d", l, r, depth));
		
		if (l > r || depth >= 10) {
		    Log.d("KdTree", "Return");
			return null;
		}
		
		int axis = depth % DIMENSION;
		
		/*
	# Sort point list and choose median as pivot element
    point_list.sort(key=lambda point: point[axis])
    median = len(point_list) // 2 # choose median
 
    # Create node and construct subtrees
    node = Node()
    node.location = point_list[median]
    node.left_child = kdtree(point_list[:median], depth + 1)
    node.right_child = kdtree(point_list[median + 1:], depth + 1)
		 */
		
		int median = (r + l) / 2;
		
		Node node = new Node();
		node.routeNode = nodes.get(median);
		node.left = build(nodes, l, median-1, depth + 1);
		node.right = build(nodes, median+1, r, depth + 1);
		
		return node;
	}
	
	public static void print(Node node, int depth) {
	    String padding = "";
        for (int i=0; i<depth; i++)
            padding += "  ";
        Log.d("KdTree", String.format("%s%f, %f", padding, node.routeNode.getLatitude(), node.routeNode.getLongitude()));
        
        if (node.left != null)
            print(node.left, depth+1);
        if (node.right != null)
            print(node.right, depth+1);
    }
	
	public KdTree() {
		
	}
	
}
