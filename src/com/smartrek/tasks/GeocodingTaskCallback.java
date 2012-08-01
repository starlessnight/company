package com.smartrek.tasks;

import org.osmdroid.util.GeoPoint;

/**
 * Defines an interface that is going to be called when GeocodingTask.execute()
 * is completed.
 */
public interface GeocodingTaskCallback {
	public void preCallback();
	public void callback(GeoPoint coordinate);
	public void postCallback();
}