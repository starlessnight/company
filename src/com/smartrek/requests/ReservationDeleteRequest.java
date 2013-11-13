package com.smartrek.requests;

import java.io.IOException;

import org.json.JSONException;

import android.content.Context;

import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

public final class ReservationDeleteRequest extends DeleteRequest {

    private User user;
    
    /**
     * Reservation ID
     */
    private long rid;
    
    public ReservationDeleteRequest(User user, long rid) {
        this.user = user;
        this.rid = rid;
    }
    
    public void execute(Context ctx) throws IOException, JSONException, InterruptedException {
        if(NEW_API){
            this.username = user.getUsername();
            this.password = user.getPassword();
            String url = getLinkUrl(Link.reservation) + "/" +rid;
            executeHttpRequest(Method.DELETE, url, ctx);
        }else{
            String url = String.format("%s/deletereservations/?uid=%d&rid=%d", HOST, user.getId(), rid);
            executeDeleteRequest(url, ctx);
        }
    }
}
