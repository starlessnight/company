package com.smartrek.utils;

public class RecurringTime {
	
	public static final byte SUN = 0x40;
	public static final byte MON = 0x20;
	public static final byte TUE = 0x10;
	public static final byte WED = 0x08;
	public static final byte THU = 0x04;
	public static final byte FRI = 0x02;
	public static final byte SAT = 0x01;

	private int hour;
	
	private int minute;
	
	private byte weekdays;
	
	public RecurringTime(int hour, int minute, byte weekdays) {
		this.hour = hour;
		this.minute = minute;
		this.weekdays = weekdays;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public byte getWeekdays() {
		return weekdays;
	}

	public void setWeekdays(byte weekdays) {
		this.weekdays = weekdays;
	}
	
	
}
