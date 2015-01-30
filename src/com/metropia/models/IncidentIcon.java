package com.metropia.models;

import android.content.Context;

public enum IncidentIcon {
	
	accident(1), fullclosure(2), partialclosure(3), incident(4), congested(5), hazards(6);
	
	private int type;
	
	private IncidentIcon(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	
	public static IncidentIcon fromType(int type) {
		switch(type) {
			case 1:
				return accident;
			case 2:
				return fullclosure;
			case 3:
				return partialclosure;
			case 4:
				return incident;
			case 5:
				return congested;
			default:
				return hazards;
		}
	}
	
	public int getResourceId(Context ctx) {
       	return ctx.getResources().getIdentifier(name(), "drawable", ctx.getPackageName());
	}

}
