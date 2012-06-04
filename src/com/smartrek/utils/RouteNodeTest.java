package com.smartrek.utils;

import android.test.AndroidTestCase;
import android.util.Log;

public class RouteNodeTest extends AndroidTestCase {

    public void testDistanceTo() {
        RouteNode node = new RouteNode(33, -112, 0, 0);
        Log.d("RouteNodeTest", String.format("distance = %f", node.distanceTo(34, -112)));
        Log.d("RouteNodeTest", String.format("distance = %f", node.distanceTo(33.1f, -112)));
        Log.d("RouteNodeTest", String.format("distance = %f", node.distanceTo(33.1f, -113)));
    }

}
