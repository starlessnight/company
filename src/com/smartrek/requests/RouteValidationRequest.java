package com.smartrek.requests;

import java.io.IOException;

/**
 * Marks a reserved trip as validated
 *
 */
public class RouteValidationRequest extends Request {
	
	public RouteValidationRequest(int uid, int rid) {
		url = String.format("%s/validationdone/?uid=%d&rid=%d", HOST, uid, rid);
	}
	
	public void execute() throws IOException {
		executeHttpGetRequest(url);
	}
}
