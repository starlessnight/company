package com.metropia.requests;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.metropia.exceptions.SmarTrekException;
import com.metropia.models.Passenger;
import com.metropia.models.User;
import com.metropia.utils.HTTP.Method;

public class PassengerRequest extends Request {
	
    
    public ArrayList<Passenger> execute(User user, long rid, Context ctx) throws Exception {
        this.username = user.getUsername();
        this.password = user.getPassword();
        String apiUrl = "http://sandbox.metropia.com/dev3_v1/duo_rest_storage/bb_head.json/{reservation_id}";
        String url = apiUrl.replaceAll("\\{reservation_id\\}", String.valueOf(rid));
        
        try{
        	ArrayList<Passenger> passengers = new ArrayList<Passenger>();
            String str = executeHttpRequest(Method.POST, url, ctx);
            if (str==null) return passengers;
            
            JSONObject json = new JSONObject(str);
            JSONArray names = json.getJSONObject("data").getJSONArray("o_users_names");
            JSONArray photos = json.getJSONObject("data").getJSONArray("o_users_pic");
            for (int i=0 ; i<names.length() ; i++) {
            	passengers.add(new Passenger(names.getString(i), photos.getString(i)));
            }
            
            return passengers;
        }catch(Exception e){
            if(responseCode >= 400 && responseCode <= 499){
                throw new SmarTrekException(responseCode);
            }else{
                throw e;
            }
        }
    }
}
