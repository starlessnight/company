package com.smartrek.requests;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.smartrek.exceptions.SmarTrekException;
import com.smartrek.models.User;
import com.smartrek.utils.HTTP.Method;

public class TripValidationRequest extends Request {
	
    private long rid;
    
	public TripValidationRequest(User user, long rid) {
        url = getLinkUrl(Link.trip).replaceAll("\\{user_id\\}", String.valueOf(user.getId()));
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.rid = rid;
	}
	
	public void execute(Context ctx) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("reservation_id", String.valueOf(rid));
        try{
            executeHttpRequest(Method.POST, url, params, ctx);
        }catch(Exception e){
            if(responseCode >= 400 && responseCode <= 499){
                throw new SmarTrekException(responseCode);
            }else{
                throw e;
            }
        }
	}
}
