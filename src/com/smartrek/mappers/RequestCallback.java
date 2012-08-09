package com.smartrek.mappers;

public interface RequestCallback {
	/**
	 * Called when mapper task is completed
	 */
	void onFinish();
	
	/**
	 * Called when an error has occurred
	 * @param e
	 */
	void onFail(Exception e);
}
