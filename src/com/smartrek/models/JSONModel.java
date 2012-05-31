package com.smartrek.models;

import org.json.JSONException;

public interface JSONModel {

	/**
	 * 
	 * @return
	 * @throws JSONException
	 */
	public String toJSON() throws JSONException;
}
