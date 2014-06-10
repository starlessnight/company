package com.smartrek.exceptions;

public class SmarTrekException extends Exception {

    int responseCode;
    
	/**
	 * Auto-generated serial UID
	 */
	private static final long serialVersionUID = 3245555681226745968L;

	public SmarTrekException() {
		super();
	}
	
	public SmarTrekException(String message) {
		super(message);
	}
	
	public SmarTrekException(int responseCode) {
        this.responseCode = responseCode;
    }
	
}
