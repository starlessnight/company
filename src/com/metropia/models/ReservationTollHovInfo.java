package com.metropia.models;

import org.json.JSONException;
import org.json.JSONObject;

public class ReservationTollHovInfo {

	private Long reservationId;
	private boolean includeToll;
	private boolean hov;
	
	private ReservationTollHovInfo() {}
	
	public ReservationTollHovInfo(Long reservationId) {
		this.reservationId = reservationId;
	}

	public Long getReservationId() {
		return reservationId;
	}

	public void setReservationId(Long reservationId) {
		this.reservationId = reservationId;
	}

	public boolean isIncludeToll() {
		return includeToll;
	}

	public void setIncludeToll(boolean includeToll) {
		this.includeToll = includeToll;
	}

	public boolean isHov() {
		return hov;
	}

	public void setHov(boolean hov) {
		this.hov = hov;
	}
	
	public static final String RESERVATION_ID = "reservationId";
	public static final String INCLUDE_TOLL = "includeToll";
	public static final String HOV = "hov";
	
	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();
		try {
			json.put(RESERVATION_ID, this.reservationId + "");
			json.put(INCLUDE_TOLL, this.includeToll + "");
			json.put(HOV, this.hov + "");
		}
		catch(JSONException ignore) {}
		return json;
	}
	
	public static ReservationTollHovInfo parse(JSONObject json) {
		if(json == null) {
			return null;
		}
		
		ReservationTollHovInfo info = new ReservationTollHovInfo();
		info.setReservationId(json.optLong(RESERVATION_ID, 0));
		info.setIncludeToll(json.optBoolean(INCLUDE_TOLL, false));
		info.setHov(json.optBoolean(HOV, false));
		return info;
	}
}
