package com.metropia.requests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.metropia.activities.R;
import com.metropia.dialogs.CancelableProgressDialog;
import com.metropia.exceptions.ServiceFailException;
import com.metropia.exceptions.WrappedIOException;
import com.metropia.models.User;
import com.metropia.requests.CityRequest.City;
import com.metropia.tasks.ICallback;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.HTTP.Method;

public class PassengerReservationRequest extends Request {
	
	private String id;
	private User user;
	private String departureTime;
	private String version;
	
	public PassengerReservationRequest(User user, String version) {
		Date now = new Date(System.currentTimeMillis());
		DateFormat idDf = new SimpleDateFormat("yyyyMMddHHmm");
		idDf.setTimeZone(TimeZone.getTimeZone("GMT"));
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		id = idDf.format(now);
		departureTime = dateFormat.format(now); 
		this.user = user;
		this.version = version;
		url = getLinkUrl(Link.passenger_reservation);
	}
	
	public Long execute(Context ctx, City city) throws Exception {
		this.username = user.getUsername();
        this.password = user.getPassword();
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("origin", "");
        params.put("destination", "");
        params.put("start_datetime", departureTime);
        params.put("estimated_travel_time", 0);
        params.put("route", new JSONArray("[]"));
        params.put("app_version", version);
        params.put("validated", 0);
        params.put("navigation_url", "");
        params.put("city", city!=null&&city.name!=null? city.name:"");
        params.put("trajectory_fields", "lat,lon,altitude,heading,timestamp,speed,link,accuracy");
        
        String res = null;
        boolean throwException = false;
        try {
            res = executeHttpRequest(Method.POST, url, params, ctx);
        } catch (Exception e){
        	throwException = true;
        	if(e instanceof WrappedIOException) {
        		res = ((WrappedIOException)e).getDetailMessage();
        	}
        	else {
        		res = e.getMessage();
        	}
        }
        
        JSONObject resJson = new JSONObject(res);
        JSONObject json;
        if(throwException) {
        	json = new JSONObject(resJson.optString(RESPONSE, ""));
        }
        else {
          	json = resJson;
        }
        Long reservId;    
        if("fail".equals(json.getString("status"))){
            throw new ServiceFailException("", resJson.optString(ERROR_MESSAGE, ""));
        }else{
        	JSONObject data = json.getJSONObject("data");
            reservId = data.getLong("id");
        }
        return reservId;
	}
	
	
	public void executeAsync(final Context ctx, final City city, final ICallback cb) {
		
		new AsyncTask<Void, Void, Long>() {
			
			CancelableProgressDialog dialog;
			ExceptionHandlingService es = new ExceptionHandlingService(ctx);
			
			@Override
			protected void onPreExecute() {
				dialog = new CancelableProgressDialog(ctx, "Preparing...");
				dialog.setActionListener(new CancelableProgressDialog.ActionListener() {
					@Override
					public void onClickNegativeButton() {
						((Activity)ctx).finish();
					}
				});
				dialog.show();
			}

			@Override
			protected Long doInBackground(Void... params) {
				try {
			
					PassengerReservationRequest resvReq = new PassengerReservationRequest(User.getCurrentUser(ctx), ctx.getString(R.string.distribution_date));
					return resvReq.execute(ctx, city);
				}
				catch(Exception e) {
					es.registerException(e);
				}
				return -1L;
			}
	
			@Override
			protected void onPostExecute(final Long reserId) {
				if(dialog.isShowing()) {
					dialog.dismiss();
				}
				if(es.hasExceptions()) {
					es.reportExceptions();
				}
				
				if (cb!=null) cb.run(reserId);
			}
		}.execute();
		
	}
}
