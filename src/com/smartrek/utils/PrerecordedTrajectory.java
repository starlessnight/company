package com.smartrek.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class PrerecordedTrajectory {
	
	public static List<GeoPoint> read(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		List<GeoPoint> trajectory = new LinkedList<GeoPoint>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] cols = line.split(",");
			
			if (cols.length == 2) {
				double latitude = Double.valueOf(cols[1]);
				double longitude = Double.valueOf(cols[0]);
				GeoPoint geoPoint = new GeoPoint(latitude, longitude);
				
				trajectory.add(geoPoint);
			}
		}
		
		// I wish we could use Java 7
		reader.close();
		
		return trajectory;
	}
	
}
