package com.smartrek.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StringUtil {
	
	public static String formatImperialDistance(double meter) {
	    String miles = new BigDecimal(UnitConversion.meterToMile(meter)).setScale(1, RoundingMode.CEILING).toString();
	    if(miles.equals("1.0")){
	        miles = "1";
	    }
		return miles + " mile" + (miles.equals("1")?"":"s");
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
