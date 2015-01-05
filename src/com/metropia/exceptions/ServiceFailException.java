package com.metropia.exceptions;

public class ServiceFailException extends Exception{

	private static final long serialVersionUID = 1L;
	
	private String detailMessage = "";

	public ServiceFailException(String message) {
		super(message);
	}

	public ServiceFailException(Throwable throwable) {
		super(throwable);
	}
	
	public ServiceFailException(String message, String detailMessage) {
		super(message);
		this.detailMessage = detailMessage;
	}
	
	public ServiceFailException(String message, String detailMessage, Throwable throwable) {
		super(message, throwable);
		this.detailMessage = detailMessage;
	}
	
	public String getDetailMessage() {
		return detailMessage;
	}

}
