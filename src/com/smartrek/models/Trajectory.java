package com.smartrek.models;

import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;

import android.location.Location;

import com.smartrek.utils.RouteNode;

public class Trajectory {
	
    public static final long DEFAULT_LINK_ID = -1;
    
	public static class Record {
	    
		/**
		 * Latitude
		 */
		private float lat;
		
		/**
		 * Longitude
		 */
		private float lng;
		
		/**
		 * Altitude in meters
		 */
		private float altitude;
		
		/**
		 * Speed in m/s (meters per second)
		 */
		private float speed;
		
		/**
		 * Angle in degrees between the headed direction and the North
		 */
		private float heading;
		
		/**
		 * Epoch in milliseconds
		 */
		private long time;
		
		private long linkId = DEFAULT_LINK_ID;
		
		public Record(float lat, float lng, float altitude, float speed, float heading, long time, long linkId) {
			setLatitude(lat);
			setLongitude(lng);
			setAltitude(altitude);
			setSpeed(speed);
			setHeading(heading);
			setTime(time);
			this.linkId = linkId;
		}

		public float getLatitude() {
			return lat;
		}

		public void setLatitude(float lat) {
			this.lat = lat;
		}

		public float getLongitude() {
			return lng;
		}

		public void setLongitude(float lng) {
			this.lng = lng;
		}

		public float getAltitude() {
			return altitude;
		}

		public void setAltitude(float altitude) {
			this.altitude = altitude;
		}

		public float getSpeed() {
			return speed;
		}

		public void setSpeed(float speed) {
			assert(speed >= 0.0f);
			
			this.speed = speed;
		}

		public float getHeading() {
			return heading;
		}

		/**
		 * The value of heading stays between 0 (inclusive) and 360 (exclusive)
		 * 
		 * @param heading
		 */
		public void setHeading(float heading) {
			while (heading < 0) {
				heading += 360;
			}
			while (heading >= 360) {
				heading -= 360;
			}
			
			this.heading = heading;
		}

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}
		
		/**
		 * Order matters when reporting user trajectory
		 * 
		 * @return
		 * @throws JSONException
		 */
		public JSONArray toJSON() throws JSONException {
			JSONArray array = new JSONArray();
			array.put(getLatitude());
			array.put(getLongitude());
			array.put(getAltitude() * 3.2808399f); // conversion from meter to feet
			array.put(getHeading());
			array.put(getTime());
			array.put(msToMph(getSpeed())); // conversion from m/s to mph
			array.put(linkId);
			
			return array;
		}
		
		public static Record from(JSONArray array) throws JSONException {
	        return new Record((float)array.getDouble(0), (float)array.getDouble(1), 
	            (float)(array.getDouble(2) / 3.2808399f), (float)(array.getDouble(5) / msToMphFactor), 
	            (float) array.getDouble(3), array.getLong(4), array.optLong(6, DEFAULT_LINK_ID));
	    }
		
	}
	
	public static final double msToMph(float speed){
	    return speed * msToMphFactor;
	}
	
	private static final float msToMphFactor = 2.2369356f; 
	
	private List<Record> records = new Vector<Record>();
	
	public Trajectory() {
		
	}

	/**
	 * 
	 * @param lat Latitude
	 * @param lng Longitude
	 * @param altitude Altitude in meters
	 * @param speed Speed in m/s
	 * @param heading Angle between the headed direction and the North 
	 * @param time Epoch in milliseconds
	 */
	public void accumulate(float lat, float lng, float altitude, float speed, float heading, long time, long linkId) {
		records.add(new Record(lat, lng, altitude, speed, heading, time, linkId));
	}
	
	public void accumulate(Location location, long linkId) {
		accumulate((float) location.getLatitude(),
				(float) location.getLongitude(),
				(float) location.getAltitude(),
				location.getSpeed(),
				location.getBearing(),
				location.getTime(),
				linkId);
	}
	
	public void clear() {
		records.clear();
	}
	
	public int size() {
		return records.size();
	}
	
	public JSONArray toJSON() throws JSONException {
		JSONArray array = new JSONArray();
		for (Record record : records) {
			array.put(record.toJSON());
		}
		
		return array;
	}
	
	public void append(Trajectory traj){
	    records.addAll(traj.records);
	}
	
	public Record poll(){
	    Record r = null;
	    if(!records.isEmpty()){
	        r = records.remove(0);
	    }
	    return r;
	}
	
	public Trajectory decimateBy500ft(){
	    ListIterator<Record> recordIter = records.listIterator();
	    Record lastRecord = null;
	    while(recordIter.hasNext()){
	        Record r = recordIter.next();
	        if(lastRecord != null && RouteNode.distanceBetween(lastRecord.lat, 
	                lastRecord.lng, r.lat, r.lng) < 152.4){
	            recordIter.remove();
	        }else{
	            lastRecord = r; 
	        }
	    }
	    return this;
	}
	
	public static Trajectory from(JSONArray array) throws JSONException {
        Trajectory traj = new Trajectory();
        for(int i=0; i<array.length(); i++){
            traj.records.add(Record.from(array.getJSONArray(i)));
        }
        return traj;
    }

    public List<Record> getRecords() {
        return records;
    }
	
}
