package com.metropia.requests;

public interface RequestCallback {
	/**
	 * Called when request is completed
	 */
	void onFinish();
	
	/**
	 * Called when an error has occurred
	 * @param e
	 */
	void onFail(Exception e);
}
