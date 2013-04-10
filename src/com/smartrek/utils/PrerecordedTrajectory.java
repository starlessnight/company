package com.smartrek.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import com.smartrek.activities.DebugOptionsActivity;

public class PrerecordedTrajectory {
	
	public static List<GeoPoint> read(InputStream in, int gpsMode) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		List<GeoPoint> trajectory = new LinkedList<GeoPoint>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] cols = line.split(",");
			
			if (cols.length == 2) {
				double latitude = Double.valueOf(cols[gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED?1:0]);
				double longitude = Double.valueOf(cols[gpsMode == DebugOptionsActivity.GPS_MODE_PRERECORDED?0:1]);
				GeoPoint geoPoint = new GeoPoint(latitude, longitude);
				
				trajectory.add(geoPoint);
			}
		}
		
		// I wish we could use Java 7
		reader.close();
		
		return trajectory;
	}
	
}
