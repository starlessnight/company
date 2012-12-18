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
	
	private byte second;
	
	private byte weekdays;
	
	public RecurringTime() {
	}
	
	public RecurringTime(byte hour, byte minute, byte second, byte weekdays) {
		this.hour = hour;
		this.minute = minute;
		this.setSecond(second);
		this.weekdays = weekdays;
	}

	public byte getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = (byte) hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = (byte) minute;
	}

	public byte getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = (byte) second;
	}

	public byte getWeekdays() {
		return weekdays;
	}

	public void setWeekdays(int weekdays) {
		this.weekdays = (byte) weekdays;
	}
}
