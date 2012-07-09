package com.smartrek.utils;

/**
 * Manages configurable parameters for route validation
 *
 */
public class ValidationParameters {

	/**
	 * Unit is in meters
	 */
	private float distanceThreshold = 457.2f; // 1500 ft
	/**
	 * Users may depart after (departure time) - (threshold). Unit is in seconds.
	 */
	private int departureTimeNegativeThreshold = 60*5;
	
	/**
	 * Users may depart before (departure time) + (threshold). Unit is in seconds.
	 */
	private int departureTimePositiveThreshold = 60*15;
	
	private TimeRange timeRange;
	
	/**
	 * Unit is in meters.
	 */
	private float departureDistanceThreshold = 160.9344f;
	
	/**
	 * Unit is in meters.
	 */
	private float arrivalDistanceThreshold = 160.9344f;
	
	/**
	 * Unit is in seconds
	 */
	private float outOfRouteTimeout = 20*60;
	
	/**
	 * Singleton instance
	 */
	private static ValidationParameters instance;
	
	private ValidationParameters() {
		
	}
	
	public static ValidationParameters getInstance() {
		if (instance == null) {
			instance = new ValidationParameters();
		}
		return instance;
	}
	
	public float getDistanceThreshold() {
		return distanceThreshold;
	}

	public int getDepartureTimeNegativeThreshold() {
		return departureTimeNegativeThreshold;
	}

	public void setDepartureTimeNegativeThreshold(
			int departureTimeNegativeThreshold) {
		this.departureTimeNegativeThreshold = departureTimeNegativeThreshold;
	}

	public int getDepartureTimePositiveThreshold() {
		return departureTimePositiveThreshold;
	}

	public void setDepartureTimePositiveThreshold(int departureTimePositiveThreshold) {
		this.departureTimePositiveThreshold = departureTimePositiveThreshold;
	}

	public float getDepartureDistanceThreshold() {
		return departureDistanceThreshold;
	}

	public void setDepartureDistanceThreshold(float departureDistanceThreshold) {
		this.departureDistanceThreshold = departureDistanceThreshold;
	}

	public float getArrivalDistanceThreshold() {
		return arrivalDistanceThreshold;
	}

	public void setArrivalDistanceThreshold(float arrivalDistanceThreshold) {
		this.arrivalDistanceThreshold = arrivalDistanceThreshold;
	}
	
	public float getScoreThreshold() {
		return .75f;
	}

	public float getOutOfRouteTimeout() {
		return outOfRouteTimeout;
	}

	public void setOutOfRouteTimeout(float outOfRouteTimeout) {
		this.outOfRouteTimeout = outOfRouteTimeout;
	}
}
