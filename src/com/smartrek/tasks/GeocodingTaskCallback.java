package com.smartrek.tasks;

import java.util.List;

import com.smartrek.utils.Geocoding;

/**
 * Defines an interface that is going to be called when GeocodingTask.execute()
 * is completed.
 */
public interface GeocodingTaskCallback {
	public void preCallback();
	public void callback(List<Geocoding.Address> addresses);
	public void postCallback();
}