package com.smartrek.utils;

public class StringUtil {
	
	public static String formatImperialDistance(double meter) {
		double foot = UnitConversion.meterToFoot(meter);
		
		if (foot < 5280) {
			return String.format("%.0f feet", foot);
		}
		else {
			return String.format("%.1f miles", UnitConversion.meterToMile(meter));
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
