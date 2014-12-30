package com.metropia.models;

import org.json.JSONException;
import org.json.JSONObject;

import com.metropia.utils.datetime.RecurringTime;

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
	 * Recurring dates
	 */
	private RecurringTime recurringTime;
	
	public Trip(int id, String name, int oid, String origin, int did, String destination, RecurringTime recurringTime) {
		this.id = id;
		this.name = name;
		this.oid = oid;
		this.origin = origin;
		this.did = did;
		this.destination = destination;
		this.recurringTime = recurringTime;
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
	
	public RecurringTime getRecurringTime() {
		return recurringTime;
	}
	
	public static Trip parse(JSONObject object) throws JSONException {
		int id = object.getInt("FID");
		int oid = object.getInt("OID");
		int did = object.getInt("DID");
		String name = object.getString("NAME");
		String origin = object.getString("ORIGIN_ADDRESS");
		String destination = object.getString("DESTINATION_ADDRESS");

		byte hour = 0;
		byte minute = 0;
		byte second = 0;
		
		// FIXME: This should be desired arrival time
		String departureTime = object.getString("ARRIVAL");
		if (departureTime.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
			String[] cols = departureTime.split(":");
			
			hour = (byte) Integer.parseInt(cols[0]);
			minute = (byte) Integer.parseInt(cols[1]);
			second = (byte) Integer.parseInt(cols[2]);
		}
		
		byte weekdays = (byte) object.getInt("DATETYPE");
		
		return new Trip(id, name, oid, origin, did, destination, new RecurringTime(hour, minute, second, weekdays));
	}

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
