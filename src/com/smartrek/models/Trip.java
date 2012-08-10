package com.smartrek.models;

/**
 * Simply defines an origin and a destination addresses. No association with a
 * specific departure time, route, etc.
 * 
 * @author Sumin Byeon
 * 
 */
public class Trip {

	private String origin;
	private String destination;

	public Trip(String origin, String destination, Route route) {
		this.origin = origin;
		this.destination = destination;
	}

	public String getOrigin() {
		return origin;
	}

	public String getDestination() {
		return destination;
	}
}
