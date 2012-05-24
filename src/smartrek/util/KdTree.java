package smartrek.util;

import java.util.List;

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
	}
	
	/**
	 * Builds a K-d tree for a given list of nodes
	 * 
	 * @param nodes
	 * @param l Left boundary of nodes (inclusive)
	 * @param r Right boundary of nodes (exclusive)
	 * @param depth
	 * @return
	 */
	public static Node build(List<RouteNode> nodes, int l, int r, int depth) {
		
		// FIXME: This function causes stack overflow
		
		if (l >= r - 1) {
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
		
		int median = (r - l) / 2;
		
		Node node = new Node();
		node.routeNode = nodes.get(median);
		node.left = build(nodes, l, median, depth + 1);
		node.right = build(nodes, median, r, depth + 1);
		
		return node;
	}
	
	public KdTree() {
		
	}
	
	// TODO: Implement a function to visualize the tree
}
