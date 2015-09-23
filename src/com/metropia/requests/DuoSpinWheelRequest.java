package com.metropia.requests;

import org.json.JSONObject;

import android.content.Context;

import com.metropia.models.User;
import com.metropia.utils.HTTP.Method;

public class DuoSpinWheelRequest extends Request {
	
	public DuoSpinWheelRequest(User user) {
		this.username = user.getUsername();
		this.password = user.getPassword();
		
		url = getLinkUrl(Link.passenger_spin_wheel).replaceAll("\\{user_id\\}", String.valueOf(user.getId()));
	}
	
	public int execute(Context ctx, long reservationId, int degree) throws Exception {
		JSONObject params = new JSONObject();
		params.put("reservation_id", reservationId);
		params.put("degree", degree);
		
        String str = executeHttpRequest(Method.POST, url, params, ctx);
        
        JSONObject json = new JSONObject(str);
        int bonus = json.getJSONObject("data").getInt("credit_bonus");
        return bonus;
    }
}
