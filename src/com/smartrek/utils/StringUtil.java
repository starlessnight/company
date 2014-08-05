package com.smartrek.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

public class StringUtil {
	
	public static String formatImperialDistance(double meter, boolean shorter) {
	    String miles = new BigDecimal(UnitConversion.meterToMile(meter)).setScale(1, RoundingMode.CEILING).toString();
	    if(miles.equals("1.0")){
	        miles = "1";
	    }
		return miles + " " + (shorter?"mi":("mile" + (miles.equals("1")?"":"s")));
	}
	
	public static String formatRoundingDistance(double mile, boolean shorter) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		String miles = nf.format(mile);
	    if(miles.equals("1.0")){
	        miles = "1";
	    }
		return miles + " " + (shorter?"mi":("mile" + (miles.equals("1")?"":"s")));
	}
	
	public static String formatImperialDistance(double meter) {
        return formatImperialDistance(meter, false);
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
