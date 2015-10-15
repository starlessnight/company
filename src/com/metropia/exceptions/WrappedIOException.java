package com.metropia.exceptions;

import java.io.IOException;

public class WrappedIOException extends IOException {

	private static final long serialVersionUID = 1L;
	
	private String detailMessage;
	int responseCode;

	public WrappedIOException() {
		super();
	}

	public WrappedIOException(String message, Throwable cause) {
		super(message, cause);
	}

	public WrappedIOException(String message) {
		super(message);
	}

	public WrappedIOException(Throwable cause) {
		super(cause);
	}
	
	public WrappedIOException(String message, String detailMessage, Throwable cause, int responseCode) {
		super(message, cause);
		this.detailMessage = detailMessage;
		this.responseCode = responseCode;
	}

	public String getDetailMessage() {
		return detailMessage;
	}
	
	public int getResponseCode() {
		return responseCode;
	}

}
