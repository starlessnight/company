package com.smartrek.ui.timelayout;

import android.text.format.Time;

/*********************************************************************************************************
 * 
 * 
 * 
 *********************************************************************************************************/
public class AdjustableTime extends Time {
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public AdjustableTime() {
		super();
		this.setToNow();
	}
	
	public Time initTime() {
		return new Time(this);
	}
	
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public void incrementBy(int timeinterval) {
		int hour = this.hour;
		int minute = this.minute;
		if(this.minute % timeinterval == 0){
			minute = this.minute + timeinterval;
			if(minute >= 60){
				minute = 0;
				hour += 1;
			}
		} else {
			minute = timeinterval - (this.minute % timeinterval) + this.minute;
			if(minute >= 60){
				minute = 0;
				hour += 1;
			}
		}
		this.set(0, minute, hour, this.monthDay, this.month, this.year);
	}

	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public int getNumTimeBoxes() {
		int numboxes = 0;
		numboxes += (60 - this.minute) / 15;
		numboxes += (23 - this.hour) * 4;
		return numboxes + 1;
	}
	
}
