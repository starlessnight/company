package com.metropia;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.metropia.activities.CongratulationActivity;
import com.metropia.activities.LandingActivity2;
import com.metropia.activities.MainActivity;
import com.metropia.activities.PassengerActivity;
import com.metropia.activities.R;
import com.metropia.activities.ValidationActivity;
import com.metropia.exceptions.SmarTrekException;
import com.metropia.models.User;
import com.metropia.receivers.NotificationExpiry;
import com.metropia.requests.DuoSpinWheelRequest;
import com.metropia.requests.TripValidationRequest;
import com.metropia.tasks.ICallback;
import com.metropia.utils.Misc;
import com.metropia.utils.HTTP.Method;

public class TripService extends IntentService {
    
    private static long threeMins = 3 * 60 * 1000;
    private static long eightHours = 8 * 60 * 60 * 1000;
    
    public TripService() {
        super(TripService.class.getName());
    }
    
    public static void run(Context ctx, User user){
        File[] files = getDir(ctx).listFiles();
        if(ArrayUtils.isNotEmpty(files)){
            Map<Long, File> toSendFiles = new HashMap<Long, File>();
            for (File f : files) {
                String name = f.getName();
                if(StringUtils.isNumeric(name)){
                    try{
                        File newFile = new File(f.getParentFile(), "_" + name);
                        f.renameTo(newFile);
                        long rId = Long.parseLong(name);
                        toSendFiles.put(rId, newFile);
                    }catch(Throwable t){
                        Log.w("TripService", Log.getStackTraceString(t));
                    }
                }
            }
            for (Entry<Long, File> e : toSendFiles.entrySet()) {
                boolean deleted = false;
                File f = e.getValue();
                long rId = e.getKey();
                JSONObject reservInfo = null;
                try {
					reservInfo = new JSONObject(FileUtils.readFileToString(f));
				} catch (Exception exp) {
					reservInfo = new JSONObject();
				} 
                try{
                	String mode = (String) reservInfo.get("MODE");
                	
                	if (reservInfo.optJSONObject("result")!=null) {
                		int bonusAngle = reservInfo.getJSONObject("result").optInt("bonusAngle", -2);
                		if (bonusAngle==-2) continue;
                		
                		int bonus = new DuoSpinWheelRequest(User.getCurrentUser(ctx)).execute(ctx, rId, bonusAngle);
                		TripService.finishTrip(ctx, rId);
                		TripService.notifySpinSuccess(ctx, bonus);
                		continue;
                	}
                    if(!SendTrajectoryService.isSending(ctx, rId) && SendTrajectoryService.send(ctx, rId, mode)){
                        JSONObject obj = new TripValidationRequest(user, rId, mode).execute(ctx);
                        
                        if (ValidationActivity.TRIP_VALIDATOR.equals(mode)) {
                        	FileUtils.deleteQuietly(f);
                        }
                        else {
                            reservInfo.put("result", obj.getJSONObject("data"));
                            FileUtils.write(f, reservInfo.toString());
                        }

                    	TripService.notifyValidation(ctx, mode, reservInfo, obj);
                    	
                    	ActivityManager am = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
                    	List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                    	if (taskInfo.get(0).topActivity.getClassName().equals(LandingActivity2.class.getName())) {
                    		LandingActivity2.getInstance().runOnUiThread(new Runnable() {
								public void run() {
		                    		LandingActivity2.getInstance().checkBackgroundValidation();
								}
							});
                    	}
                        
                    }
                }catch(SmarTrekException ex){
                    deleted = true;
                    FileUtils.deleteQuietly(f);
                }catch(Throwable t){
                    Log.w("TripService", Log.getStackTraceString(t));
                }finally{
                    if(!deleted){
                        f.renameTo(new File(f.getParentFile(), String.valueOf(rId)));
                    }
                }
            }
        }
    }
    
    private static final String IMD_PREFIX = "[";
    
    public static void runImd(Context ctx, User user, final long rId, String reciverName) {
        File toSendFile = null;
        File imdSendFile = getFile(ctx, rId);
        if(imdSendFile.exists()) {
            try{
            	String name = imdSendFile.getName().substring(1);
                File newFile = new File(imdSendFile.getParentFile(), "_" + name);
                imdSendFile.renameTo(newFile);
                toSendFile = newFile;
            }catch(Throwable t){
                Log.w("TripService", Log.getStackTraceString(t));
            }
        }
        if(toSendFile != null) {
            boolean deleted = false;
            try{
                if(!SendTrajectoryService.isSending(ctx, rId) && SendTrajectoryService.sendImd(ctx, rId, reciverName)){
                    JSONObject obj = new TripValidationRequest(user, rId, reciverName).executeImd(ctx);
                    notifyValidationImd(ctx, reciverName, obj);
                    if (ValidationActivity.TRIP_VALIDATOR.equals(reciverName)) FileUtils.deleteQuietly(toSendFile);
                    else {
                    	JSONObject reservInfo = new JSONObject(FileUtils.readFileToString(toSendFile));
                    	reservInfo.put("result", obj.getJSONObject("data"));
                        FileUtils.write(toSendFile, reservInfo.toString());
                    }
                }
            }catch(SmarTrekException ex){
                deleted = true;
                FileUtils.deleteQuietly(toSendFile);
            }catch(Throwable t){
                Log.w("TripService", Log.getStackTraceString(t));
            }finally{
                if(!deleted){
                    toSendFile.renameTo(new File(toSendFile.getParentFile(), String.valueOf(rId)));
                }
            }
        }
    }
    

    private static void notifyValidationImd(Context context, String receiverName, JSONObject obj) {
    	Intent intent = new Intent(receiverName);
    	try{
            JSONObject data = obj.getJSONObject("data");
           	putInfo(intent, data);
        }catch(Exception e){
        	intent.putExtra(ValidationActivity.REQUEST_SUCCESS, false);
        }
        finally {
        	context.sendBroadcast(intent);
        }
    }
    

	public static final Integer DIRVER_NOTI_ID = Integer.valueOf(123451);
	public static final Integer DUO_NOTI_ID = Integer.valueOf(123452);
	
    @SuppressLint("NewApi")
	public static void notifyValidation(Context context, String receiverName, JSONObject reservInfo, JSONObject obj) {
    	JSONObject data = null;
    	try {
    		data = obj.getJSONObject("data");
        	if (PassengerActivity.PASSENGER_TRIP_VALIDATOR.equals(receiverName)) data.put("message", "Please spin the DUO Prize Wheel to earn your reward!");
    	} catch(Exception e) {Log.e("fetch data attribute failed", e.toString());}
    	
    	Class<?> target = ValidationActivity.TRIP_VALIDATOR.equals(receiverName)? CongratulationActivity.class:PassengerActivity.class;
    	int notiID = ValidationActivity.TRIP_VALIDATOR.equals(receiverName)? DIRVER_NOTI_ID:DUO_NOTI_ID;
    	
        Intent congraIntent = new Intent(context, target);
		putInfo(congraIntent, data);
		congraIntent.putExtra(CongratulationActivity.DESTINATION, reservInfo.optString(CongratulationActivity.DESTINATION, ""));
		congraIntent.putExtra(CongratulationActivity.DEPARTURE_TIME, reservInfo.optLong(CongratulationActivity.DEPARTURE_TIME, 0));
		PendingIntent sender = PendingIntent.getActivity(context, 0, congraIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        StringBuffer notiInfo = new StringBuffer();
        notiInfo.append(data.optString("message", "")).append("\n");
        notiInfo.append(reservInfo.optString(CongratulationActivity.DESTINATION, ""));
        Notification notification;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            notification = new Notification.BigTextStyle(
                    new Notification.Builder(context)
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
            notification.setLatestEventInfo(context, "Metropia", notiInfo.toString(), sender);
        }
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(notiID, notification);
        Misc.playDefaultNotificationSound(context);
        Misc.wakeUpScreen(context, TripValidationRequest.class.getSimpleName());
    }
    
    private static void notifySpinSuccess(Context context, int bonus) {
    	String message = "We've deposited " + bonus + " points into your account for using DUO!";
    	Intent intentMain = new Intent(context, MainActivity.class);
		intentMain.setAction(Intent.ACTION_MAIN);
		intentMain.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent sender = PendingIntent.getActivity(context, 0, intentMain, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification(R.drawable.icon_small, "Metropia", System.currentTimeMillis());
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notification.setLatestEventInfo(context, "Metropia", message, sender);
        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;            
        notificationManager.notify(0, notification);
    }
    
	
	public static void putInfo(Intent intent, JSONObject data) {
		intent.putExtra(ValidationActivity.ID, data.optString("id"));
		intent.putExtra(ValidationActivity.CREDIT, data.optInt("credit", 0));
		intent.putExtra(ValidationActivity.CO2_SAVING, data.optDouble("co2_saving", 0));
		intent.putExtra(ValidationActivity.TIME_SAVING_IN_MINUTE, data.optDouble("time_saving_in_minute", 0));
		intent.putExtra(ValidationActivity.VOICE, data.optString("voice"));
		intent.putExtra(ValidationActivity.MESSAGE, data.optString("message"));
		intent.putExtra(ValidationActivity.REQUEST_SUCCESS, true);
		intent.putExtra("driver_name", data.optString("driver_name"));
		intent.putExtra("duration", data.optDouble("duration", 0));
		intent.putExtra("distance", data.optDouble("distance", 0));
		intent.putExtra("DUO_duration", data.optDouble("DUO_duration", 10));
		intent.putExtra("DUO_distance", data.optDouble("DUO_distance", 3));
		intent.putExtra("wheel_url", data.optString("wheel_url"));
		intent.putExtra("wheel_name", data.optString("wheel_name"));
		
		
	}
	
	public static void logTripInfo(Context context, long rid, String attr, int value) {
		try {
			File file = new File(getDir(context), String.valueOf(rid));
			JSONObject obj = new JSONObject(FileUtils.readFileToString(file));
			obj.getJSONObject("result").put(attr, value);
			FileUtils.write(file, obj.toString());
		} catch(Exception e) {Log.e("write trip info failed", e.toString());}
		
	}
	public static void finishTrip(Context context, long rid) {
        File file = new File(getDir(context), String.valueOf(rid));
        if(file.exists()) FileUtils.deleteQuietly(file);
	}
    
    private static File getDir(Context ctx){
        return new File(ctx.getExternalFilesDir(null), "trip");
    }
    public static File getFile(Context ctx, long rId){
        return new File(getDir(ctx), IMD_PREFIX + String.valueOf(rId));
    }
    

    @Override
    protected void onHandleIntent(Intent intent) {
        final User user = User.getCurrentUserWithoutCache(TripService.this);
        if(user != null){
            MainActivity.initApiLinksIfNecessary(this, new Runnable() {
                @Override
                public void run() {
                    TripService.run(TripService.this, user);
                }
            });
        }
    }
    
    public static void schedule(Context ctx){
        PendingIntent sendTrajServ = PendingIntent.getService(ctx, 0, new Intent(ctx, TripService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10*1000, 60*1000, sendTrajServ);
    }

}
