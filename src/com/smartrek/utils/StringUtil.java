package com.smartrek.utils;

public class StringUtil {

	public static String capitalizeFirstLetter(String str) {
		if (str != null && str.length() > 0) {
			return Character.toUpperCase(str.charAt(0)) + str.substring(1);
		}
		else {
			return str;
		}
	}
	
	public static String formatImperialDistance(double meter) {
		double foot = UnitConversion.meterToFoot(meter);
		
		if (foot < 1000) {
			return String.format("%.0f ft", foot);
		}
		else {
			return String.format("%.1f mi", UnitConversion.meterToMile(meter));
		}
	}
	
	public static String formatMetricDistance(double meter) {
		if (meter < 1000) {
			return String.format("%.0f m", meter);
		}
		else {
			return String.format("%.1f km", meter/1000);
		}
	}
}
