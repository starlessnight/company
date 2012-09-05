package com.smartrek.utils;


/**
 * 2-dimensional vector
 *
 */
public class Vector2D {
    public double x, y;
    
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double dotProduct(Vector2D v) {
        return dotProduct(this, v);
    }
    
    public double magnitude() {
        return Math.sqrt(x*x + y*y);
    }
    
    public double angleBetween(Vector2D v) {
        return angleBetween(this, v);
    }
    
    public static double dotProduct(Vector2D u, Vector2D v) {
        return u.x*v.x + u.y*v.y;
    }
    
    public static double angleBetween(Vector2D u, Vector2D v) {
        return (float) Math.acos(dotProduct(u, v) / (u.magnitude() * v.magnitude()));
    }
}
