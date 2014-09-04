package com.smartrek.models;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
		
		/**
		 * Altitude in meters
		 */
		private float accuracy;  
		
		public Record(float lat, float lng, float altitude, float speed, float heading, long time, long linkId, float accuracy) {
			setLatitude(lat);
			setLongitude(lng);
			setAltitude(altitude);
			setSpeed(speed);
			setHeading(heading);
			setTime(time);
			this.linkId = linkId;
			setAccuracy(accuracy);
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
		
		private static final NumberFormat nf = new DecimalFormat("#.######");
		
		/**
		 * Order matters when reporting user trajectory
		 * 
		 * @return
		 * @throws JSONException
		 */
		public JSONArray toJSON() throws JSONException {
			JSONArray array = new JSONArray();
			array.put(Float.valueOf(nf.format(getLatitude())));
			array.put(Float.valueOf(nf.format(getLongitude())));
			array.put(Float.valueOf(getAltitude() * 3.2808399f).intValue()); // conversion from meter to feet
			array.put(Float.valueOf(getHeading()).intValue());
			array.put(getTime());
			array.put(Double.valueOf(msToMph(getSpeed())).intValue()); // conversion from m/s to mph
			array.put(linkId);
			array.put(Float.valueOf(getAccuracy() * 3.2808399f).intValue()); // conversion from meter to feet
			return array;
		}
		
		public static Record from(JSONArray array) throws JSONException {
	        return new Record((float)array.getDouble(0), (float)array.getDouble(1), 
	            (float)(array.getDouble(2) / 3.2808399f), (float)(array.getDouble(5) / msToMphFactor), 
	            (float) array.getDouble(3), array.getLong(4), array.optLong(6, DEFAULT_LINK_ID), 
	            (float)(array.optInt(7, 0)/3.2808399f));
	    }

		public float getAccuracy() {
			return accuracy;
		}

		public void setAccuracy(float accuracy) {
			this.accuracy = accuracy;
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
	public void accumulate(float lat, float lng, float altitude, float speed, float heading, long time, long linkId, float accuracy) {
		records.add(new Record(lat, lng, altitude, speed, heading, time, linkId, accuracy));
	}
	
	public void accumulate(Location location, long linkId) {
		accumulate((float) location.getLatitude(),
				(float) location.getLongitude(),
				(float) location.getAltitude(),
				location.getSpeed(),
				location.getBearing(),
				location.getTime(),
				linkId, 
				location.getAccuracy());
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
