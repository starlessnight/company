package com.smartrek.models;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.location.Location;

public class Trajectory {
	
	public class Record {
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
		 * Angle between the headed direction and the North
		 */
		private float heading;
		
		/**
		 * Epoch in milliseconds
		 */
		private long time;
		
		public Record(float lat, float lng, float altitude, float speed, float heading, long time) {
			setLatitude(lat);
			setLongitude(lng);
			setAltitude(altitude);
			setSpeed(speed);
			setHeading(heading);
			setTime(time);
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

		public void setHeading(float heading) {
			while (heading < 0.0f) {
				heading += (float) Math.PI;
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
			array.put(getSpeed() * 2.23693629f); // conversion from m/s to mph
			
			return array;
		}
	}
	
	private List<Record> records = new ArrayList<Record>();
	
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
	public void accumulate(float lat, float lng, float altitude, float speed, float heading, long time) {
		records.add(new Record(lat, lng, altitude, speed, heading, time));
	}
	
	public void accumulate(Location location) {
		accumulate((float) location.getLatitude(),
				(float) location.getLongitude(),
				(float) location.getAltitude(),
				location.getSpeed(),
				location.getBearing(),
				location.getTime());
	}
	
	public void clear() {
		records.clear();
	}
	
	public JSONArray toJSON() throws JSONException {
		JSONArray array = new JSONArray();
		for (Record record : records) {
			array.put(record.toJSON());
		}
		
		return array;
	}
}
