package com.metropia.utils;

import android.test.AndroidTestCase;
import android.util.FloatMath;

public class Vector2DTest extends AndroidTestCase {

    public void testMagnitude() {
        Vector2D u = new Vector2D(1, 1);
        assertEquals(FloatMath.sqrt(2.0f), u.magnitude(), 0.0f);
        
        Vector2D v = new Vector2D(-2, 3);
        assertEquals(FloatMath.sqrt(13.0f), v.magnitude(), 0.0f);
    }
    
    public void testDotProduct() {
        Vector2D u = new Vector2D(1, 2);
        Vector2D v = new Vector2D(-3, 4);
        assertEquals(5.0f, Vector2D.dotProduct(u, v), 0.0f);
        assertEquals(5.0f, Vector2D.dotProduct(v, u), 0.0f);
        assertEquals(5.0f, u.dotProduct(v), 0.0f);
        assertEquals(5.0f, v.dotProduct(u), 0.0f);
    }
    
    public void testAngle() {
        Vector2D u = new Vector2D(1, 0);
        Vector2D v = new Vector2D(0, 3);
        assertEquals(Math.PI/2, Vector2D.angleBetween(u, v), 0.0001f);
        assertEquals(Math.PI/2, Vector2D.angleBetween(v, u), 0.0001f);
    }
    
    public void testAngle2() {
        Vector2D u = new Vector2D(1, 0);
        Vector2D v = new Vector2D(2, 0);
        assertEquals(0.0f, Vector2D.angleBetween(u, v), 0.0f);
        assertEquals(0.0f, Vector2D.angleBetween(v, u), 0.0f);
    }
    
    public void testAngle3() {
        Vector2D u = new Vector2D(3, 0);
        Vector2D v = new Vector2D(3, 3);
        assertEquals(Math.PI/4, Vector2D.angleBetween(u, v), 0.0001f);
        assertEquals(Math.PI/4, Vector2D.angleBetween(v, u), 0.0001f);
    }
}
