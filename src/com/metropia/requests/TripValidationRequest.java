package com.metropia.requests;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.metropia.activities.CongratulationActivity;
import com.metropia.activities.ValidationActivity;
import com.metropia.exceptions.SmarTrekException;
import com.metropia.models.User;
import com.metropia.utils.Misc;
import com.metropia.utils.HTTP.Method;
import com.metropia.activities.R;

public class TripValidationRequest extends Request {
	
    private long rid;
    
	public TripValidationRequest(User user, long rid) {
        url = getLinkUrl(Link.trip).replaceAll("\\{user_id\\}", String.valueOf(user.getId()));
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.rid = rid;
	}
	
	public void execute(Context ctx, JSONObject reservInfo) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("reservation_id", String.valueOf(rid));
        try{
            String res = executeHttpRequest(Method.POST, url, params, ctx);
            JSONObject json = new JSONObject(res);
            JSONObject data = json.getJSONObject("data");
            Intent congraIntent = new Intent(ctx, CongratulationActivity.class);
    		putInfo(congraIntent, data);
    		congraIntent.putExtra(CongratulationActivity.DESTINATION, reservInfo.optString(CongratulationActivity.DESTINATION, ""));
    		congraIntent.putExtra(CongratulationActivity.DEPARTURE_TIME, reservInfo.optLong(CongratulationActivity.DEPARTURE_TIME, 0));
    		PendingIntent sender = PendingIntent.getActivity(ctx, 0, congraIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            StringBuffer notiInfo = new StringBuffer();
            notiInfo.append(data.optString("message", "")).append("\n");
            notiInfo.append(reservInfo.optString(CongratulationActivity.DESTINATION, ""));
            Notification notification;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                notification = new Notification.BigTextStyle(
                        new Notification.Builder(ctx)
                           .setContentTitle("Metropia")
                           .setContentText(notiInfo.toString())
                           .setContentIntent(sender)
                           .setWhen(System.currentTimeMillis())
                           .setSmallIcon(R.drawable.icon_small)
                        )
                    .bigText(notiInfo.toString())
                    .build();
            }else{
            	notification = new Notification(R.drawable.icon_small, "Metropia", System.currentTimeMillis());
                notification.setLatestEventInfo(ctx, "Metropia", notiInfo.toString(), sender);
            }
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(ID, notification);
            Misc.playDefaultNotificationSound(ctx);
            Misc.wakeUpScreen(ctx, TripValidationRequest.class.getSimpleName());
        }catch(Exception e){
            Log.w("TripValidationRequest", Log.getStackTraceString(e));
            if(responseCode >= 400 && responseCode <= 499){
                throw new SmarTrekException(responseCode);
            }else{
            	throw e;
            }
        }
	}
	
	public void executeImd(Context ctx) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("reservation_id", String.valueOf(rid));
        Intent intent = new Intent(ValidationActivity.TRIP_VALIDATOR);
        intent.putExtra(ValidationActivity.ID, String.valueOf(rid));
        timeout = fifteenSecsTimeout;
        try{
            String res = executeHttpRequest(Method.POST, url, params, ctx);
            JSONObject json = new JSONObject(res);
            JSONObject data = json.getJSONObject("data");
           	putInfo(intent, data);
        }catch(Exception e){
        	intent.putExtra(ValidationActivity.REQUEST_SUCCESS, false);
        	Log.w("TripValidationRequest", Log.getStackTraceString(e));
            if(responseCode >= 400 && responseCode <= 499){
                throw new SmarTrekException(responseCode);
            }else{
            	throw e;
            }
        }
        finally {
        	ctx.sendBroadcast(intent);
        }
	}
	
	private static final Integer ID = Integer.valueOf(123452);
	
	private void putInfo(Intent intent, JSONObject data) {
		intent.putExtra(ValidationActivity.ID, data.optString("id"));
		intent.putExtra(ValidationActivity.CREDIT, data.optInt("credit", 0));
		intent.putExtra(ValidationActivity.CO2_SAVING, data.optDouble("co2_saving", 0));
		intent.putExtra(ValidationActivity.TIME_SAVING_IN_MINUTE, data.optDouble("time_saving_in_minute", 0));
		intent.putExtra(ValidationActivity.VOICE, data.optString("voice"));
		intent.putExtra(ValidationActivity.MESSAGE, data.optString("message"));
		intent.putExtra(ValidationActivity.REQUEST_SUCCESS, true);
	}
	
}
