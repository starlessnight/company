package com.smartrek.utils;

public class HumanReadableTime {
	
	/**
	 * 
	 * @param duration Number of seconds
	 * @return Human readable time
	 */
	public static String formatDuration(int duration) {
		return formatDuration(duration, false);
	}
	
	/**
	 * 
	 * @param duration Number of seconds
	 * @return Human readable time
	 */
	public static String formatDuration(int duration, boolean showSeconds) {
		StringBuffer buf = new StringBuffer();
		
		if (duration > 3600) {
			int hours = duration / 3600;
			duration %= 3600;
			
			buf.append(hours);
			buf.append(" hr");
		}
		if (duration > 60) {
			int minutes = duration / 60;
			duration %= 60;
			
			if (buf.length() > 0) {
			    buf.append(" ");
			}
			
			buf.append(minutes);
			buf.append(" min");
		}
		if (showSeconds) {
		    if (buf.length() > 0) {
                buf.append(" ");
            }
		    
			buf.append(duration);
			buf.append(" sec");
		}
		
		return new String(buf);
	}
}
