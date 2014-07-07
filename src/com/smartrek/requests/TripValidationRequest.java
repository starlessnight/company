package com.smartrek.requests;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smartrek.activities.ValidationActivity;
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
            String res = executeHttpRequest(Method.POST, url, params, ctx);
            Intent intent = new Intent(ValidationActivity.TRIP_VALIDATOR);
            try{
                JSONObject json = new JSONObject(res);
                JSONObject data = json.getJSONObject("data");
                intent.putExtra(ValidationActivity.ID, data.optString("id"));
                intent.putExtra(ValidationActivity.CREDIT, data.optInt("credit"));
                intent.putExtra(ValidationActivity.TIME_SAVING_IN_SECOND, data.optInt("time_saving_in_second"));
                intent.putExtra(ValidationActivity.CO2_SAVING, data.optDouble("co2_saving", 0));
            } finally{
                ctx.sendBroadcast(intent);
            }
        }catch(Exception e){
            Log.w("TripValidationRequest", Log.getStackTraceString(e));
            if(responseCode >= 400 && responseCode <= 499){
                throw new SmarTrekException(responseCode);
            }else{
                throw e;
            }
        }
	}
}
