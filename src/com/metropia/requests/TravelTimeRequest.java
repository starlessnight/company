package com.metropia.requests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import android.content.Context;

import com.metropia.models.User;

public class TravelTimeRequest extends FetchRequest<Double> {
	
	private static final DateFormat df = new SimpleDateFormat("yyyyMMddHHss");
	
	public TravelTimeRequest(User user, String city, String nodes) {
		super(StringUtils.defaultString(getLinkUrl(Link.travel_time)).replaceAll("\\{city\\}", city)
				.replaceAll("\\{departtime\\}", df.format(new Date(System.currentTimeMillis())))
				.replaceAll("\\{nodes\\}", nodes));
		username = user.getUsername();
		password = user.getPassword();
	}

	@Override
	public Double execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getUrl(), ctx);
		JSONObject res = new JSONObject(response);
		if("success".equals(res.optString("status"))) {
			return res.optDouble("data");
		}
		return Double.valueOf(-1);
	}
	
}
