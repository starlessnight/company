package com.metropia.utils.datetime;

import android.text.format.Time;

/**
 * Represents a range of time
 * 
 */
public class TimeRange {

	/**
	 * Lower boundary (inclusive)
	 */
	private Time startTime;
	
	/**
	 * Upper boundary (inclusive)
	 */
	private Time endTime;
	
	public TimeRange() {
		
	}
	
	public TimeRange(long startEpoch, long endEpoch) {
		startTime.set(startEpoch);
		endTime.set(endEpoch);
	}

	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}
	
	public boolean isInRange(Time time) {
		return Time.compare(startTime, time) <= 0 && Time.compare(endTime, time) >= 0;
	}

}
