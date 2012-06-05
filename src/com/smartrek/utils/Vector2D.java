package com.smartrek.utils;

import android.util.FloatMath;

/**
 * 2-dimensional vector
 *
 */
public class Vector2D {
    public float x, y;
    
    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public float dotProduct(Vector2D v) {
        return dotProduct(this, v);
    }
    
    public float magnitude() {
        return FloatMath.sqrt(x*x + y*y);
    }
    
    public float angleBetween(Vector2D v) {
        return angleBetween(this, v);
    }
    
    public static float dotProduct(Vector2D u, Vector2D v) {
        return u.x*v.x + u.y*v.y;
    }
    
    public static float angleBetween(Vector2D u, Vector2D v) {
        return (float) Math.acos(dotProduct(u, v) / (u.magnitude() * v.magnitude()));
    }
}
