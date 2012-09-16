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
}
