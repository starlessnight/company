package com.smartrek.requests;

import java.io.IOException;

import org.json.JSONException;

public final class ReservationDeleteRequest extends DeleteRequest {

    /**
     * User ID
     */
    private int uid;
    
    /**
     * Reservation ID
     */
    private int rid;
    
    public ReservationDeleteRequest(int uid, int rid) {
        this.uid = uid;
        this.rid = rid;
    }
    
    public void execute() throws IOException, JSONException {
        String url = String.format("%s/deletereservations/?uid=%d&rid=%d", HOST, uid, rid);
        
        executeDeleteRequest(url);
    }
}
