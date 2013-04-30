package com.smartrek.ui.timelayout;

import android.text.format.Time;

import com.smartrek.requests.Request;

/*********************************************************************************************************
 * 
 * 
 * 
 *********************************************************************************************************/
public class AdjustableTime extends Time {
	
    private Time deviceTime;
    
	/*********************************************************************************************************
	 * 
	 * 
	 * 
	 *********************************************************************************************************/
	public AdjustableTime() {
		super();
		setToNow();
		switchTimezone(Request.TIME_ZONE);
		deviceTime = new Time();
		deviceTime.setToNow();
	}
	
	public Time initTime() {
		Time t = new Time(this);
		t.switchTimezone(Request.TIME_ZONE);
        return t;
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
		return Math.min(getNumTimeBoxes(this), getNumTimeBoxes(deviceTime));
	}
	
	private static int getNumTimeBoxes(Time t) {
        int numboxes = 0;
        numboxes += (60 - t.minute) / 15;
        numboxes += (23 - t.hour) * 4;
        return numboxes + (t.minute == 0?0:1);
    }
	
}
