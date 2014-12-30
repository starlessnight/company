package com.metropia.utils;

import android.test.AndroidTestCase;
import android.util.Log;

public class RouteLinkTest extends AndroidTestCase {

    public void testAngle() {
        RouteNode node1 = new RouteNode(33, -111, 0, 0);
        RouteNode node2 = new RouteNode(33, -110, 0, 0);
        RouteLink link = new RouteLink(node1, node2);
        
        Log.d("RouteLinkTest", String.format("angle1 = %f, cos = %f", link.angle1(32.5f, -110.5f), Math.cos(link.angle1(32.5f, -110.5f))));
        Log.d("RouteLinkTest", String.format("angle2 = %f, cos = %f", link.angle2(32.5f, -110.5f), Math.cos(link.angle2(32.5f, -110.5f))));
        
        Log.d("RouteLinkTest", String.format("angle1 = %f, cos = %f", link.angle1(32.5f, -112), Math.cos(link.angle1(32.5f, -112))));
        Log.d("RouteLinkTest", String.format("angle2 = %f, cos = %f", link.angle2(32.5f, -112), Math.cos(link.angle2(32.5f, -112))));
        
        Log.d("RouteLinkTest", String.format("angle1 = %f, cos = %f", link.angle1(32.5f, -109), Math.cos(link.angle1(32.5f, -109))));
        Log.d("RouteLinkTest", String.format("angle2 = %f, cos = %f", link.angle2(32.5f, -109), Math.cos(link.angle2(32.5f, -109))));
    }
    
    public void testDistance() {
    	RouteNode node1 = new RouteNode(33, -111, 0, 0);
        RouteNode node2 = new RouteNode(33, -110, 0, 0);
        RouteLink link = new RouteLink(node1, node2);
        
        Log.d("RouteLinkTest", String.format("case1, distance = %f", link.distanceTo(32.5f, -110.5f)));
        assertEquals(55660.0, link.distanceTo(32.5f, -110.5f), 100);
        Log.d("RouteLinkTest", String.format("case2, distance = %f", link.distanceTo(32.5f, -112)));
        assertEquals(108920.0, link.distanceTo(32.5f, -112), 100);
        Log.d("RouteLinkTest", String.format("case3, distance = %f", link.distanceTo(32.5f, -109)));
        assertEquals(108920.0, link.distanceTo(32.5f, -109), 100);
    }
}
