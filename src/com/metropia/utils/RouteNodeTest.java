package com.metropia.utils;

import android.test.AndroidTestCase;
import android.util.Log;

public class RouteNodeTest extends AndroidTestCase {

    /**
     * This isn't really a test case (yet).
     * 
     * We'll verify our results against http://www.movable-type.co.uk/scripts/latlong.html
     */
    public void testDistanceTo() {
        RouteNode node = new RouteNode(33, -112, 0, 0);
        Log.d("RouteNodeTest", String.format("distance = %f", node.distanceTo(34, -112)));
        Log.d("RouteNodeTest", String.format("distance = %f", node.distanceTo(33.1f, -112)));
        Log.d("RouteNodeTest", String.format("distance = %f", node.distanceTo(33.1f, -113)));
    }

}
