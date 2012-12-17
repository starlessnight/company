package com.smartrek.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Simply defines an origin and a destination addresses. No association with a
 * specific departure time, route, etc.
 * 
 * @author Sumin Byeon
 * 
 */
public class Trip {
	
	public static final byte SUN = 0x40;
	public static final byte MON = 0x20;
	public static final byte TUE = 0x10;
	public static final byte WED = 0x08;
	public static final byte THU = 0x04;
	public static final byte FRI = 0x02;
	public static final byte SAT = 0x01;

	private int id;
	private String name;
	
	/**
	 * Origin address ID
	 */
	private int oid;
	
	private String origin;
	
	/**
	 * Destination address ID
	 */
	private int did;
	
	private String destination;
	
	/**
	 * Desired arrival time
	 */
	private String arrivalTime;
	
	/**
	 * Recurring dates
	 */
	private int weekdays;
	
	public Trip(int id, String name, int oid, String origin, int did, String destination) {
		this.id = id;
		this.name = name;
		this.oid = oid;
		this.origin = origin;
		this.did = did;
		this.destination = destination;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public int getOriginID() {
		return oid;
	}

	public String getOrigin() {
		return origin;
	}
	
	public int getDestinationID() {
		return did;
	}

	public String getDestination() {
		return destination;
	}
	
	public String getFormattedDesiredArrivalTime() {
		// TODO: Unimplemented
		return "0:00:00";
	}
	
	public int getRecurringWeekdays() {
		return 0;
	}
	
	public static Trip parse(JSONObject object) throws JSONException {
		int id = object.getInt("FID");
		int oid = object.getInt("OID");
		int did = object.getInt("DID");
		String name = object.getString("NAME");
		String origin = object.getString("ORIGIN_ADDRESS");
		String destination = object.getString("DESTINATION_ADDRESS");
		
		return new Trip(id, name, oid, origin, did, destination);
	}
}
