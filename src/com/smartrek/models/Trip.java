package com.smartrek.models;

/**
 * Simply defines an origin and a destination addresses. No association with a
 * specific departure time, route, etc.
 * 
 * @author Sumin Byeon
 * 
 */
public class Trip {

	private int id;
	private String name;
	private String origin;
	private String destination;

	public Trip(int id, String name, String origin, String destination) {
		this.id = id;
		this.name = name;
		this.origin = origin;
		this.destination = destination;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getOrigin() {
		return origin;
	}

	public String getDestination() {
		return destination;
	}
}
