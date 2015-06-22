package com.metropia.requests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONObject;

import android.content.Context;

import com.metropia.SkobblerUtils;
import com.metropia.requests.FetchSunriseSunsetTimeRequest.SunInfo;

public final class FetchSunriseSunsetTimeRequest extends FetchRequest<SunInfo> {
	
	public class SunInfo {
		public Integer sunrise;
		public Integer sunset;
	}

	public FetchSunriseSunsetTimeRequest(double lat, double lon) {
		super(SkobblerUtils.SUNSET_SUNRISE_API_URL.replaceAll("\\{lat\\}", lat + "").replaceAll("\\{lon\\}", lon + ""));
	}
	
	private static final DateFormat HHMMSSA = new SimpleDateFormat("hh:mm:ss a", Locale.US);
	{
		HHMMSSA.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	private static final SimpleDateFormat HHMM = new SimpleDateFormat("HHmm");

	@Override
	public SunInfo execute(Context ctx) throws Exception {
		SunInfo info = new SunInfo();
		info.sunrise = Integer.valueOf(600); // api fail default value
		info.sunset = Integer.valueOf(1800); // api fail default value
		try {
			String response = executeFetchRequest(getURL(), ctx);
			JSONObject json  = new JSONObject(response);
			if("OK".equalsIgnoreCase(json.optString("status"))){
			    JSONObject data = json.getJSONObject("results");
			    String sunrise = data.optString("sunrise");
			    String sunset = data.optString("sunset");
			    info.sunrise = Integer.valueOf(HHMM.format(HHMMSSA.parse(sunrise))); 
			    info.sunset = Integer.valueOf(HHMM.format(HHMMSSA.parse(sunset)));
			}
		}
		catch(Exception ignore) {}
		return info;
	}
}
