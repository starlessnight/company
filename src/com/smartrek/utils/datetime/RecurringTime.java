package com.smartrek.utils.datetime;

public class RecurringTime {
	
	public static final byte SUN = 0x40;
	public static final byte MON = 0x20;
	public static final byte TUE = 0x10;
	public static final byte WED = 0x08;
	public static final byte THU = 0x04;
	public static final byte FRI = 0x02;
	public static final byte SAT = 0x01;

	private byte hour;
	
	private byte minute;
	
	private byte weekdays;
	
	public RecurringTime(byte hour, byte minute, byte weekdays) {
		this.hour = hour;
		this.minute = minute;
		this.weekdays = weekdays;
	}

	public byte getHour() {
		return hour;
	}

	public void setHour(byte hour) {
		this.hour = hour;
	}

	public byte getMinute() {
		return minute;
	}

	public void setMinute(byte minute) {
		this.minute = minute;
	}

	public byte getWeekdays() {
		return weekdays;
	}

	public void setWeekdays(byte weekdays) {
		this.weekdays = weekdays;
	}
	
	
}
