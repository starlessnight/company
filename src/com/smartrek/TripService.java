package com.smartrek;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.smartrek.activities.MainActivity;
import com.smartrek.exceptions.SmarTrekException;
import com.smartrek.models.User;
import com.smartrek.requests.TripValidationRequest;

public class TripService extends IntentService {
    
    private static long threeMins = 3 * 60 * 1000;
    
    public TripService() {
        super(TripService.class.getName());
    }
    
    public synchronized static void run(Context ctx, User user){
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
                    if(!SendTrajectoryService.isSending(ctx, rId)
                            && SendTrajectoryService.send(ctx, rId)){
                        new TripValidationRequest(user, rId).execute(ctx, reservInfo);
                        FileUtils.deleteQuietly(f);
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
    
    public static void runImd(Context ctx, User user, final long rId) {
    	File[] files = getDir(ctx).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.equalsIgnoreCase(rId + "");
			}
    	});
    	
        if(ArrayUtils.isNotEmpty(files)){
            List<File> toSendFiles = new ArrayList<File>();
            for (File f : files) {
                String name = f.getName();
                try{
                    File newFile = new File(f.getParentFile(), "_" + name);
                    f.renameTo(newFile);
                    toSendFiles.add(newFile);
                }catch(Throwable t){
                    Log.w("TripService", Log.getStackTraceString(t));
                }
            }
            for (File f : toSendFiles) {
                boolean deleted = false;
                try{
                    if(!SendTrajectoryService.isSending(ctx, rId)
                            && SendTrajectoryService.sendImd(ctx, rId)){
                        new TripValidationRequest(user, rId).executeImd(ctx);
                        FileUtils.deleteQuietly(f);
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
    
    private static File getDir(Context ctx){
        return new File(ctx.getExternalFilesDir(null), "trip");
    }
    
    public static File getFile(Context ctx, long rId){
        return new File(getDir(ctx), String.valueOf(rId));
    }
    
    public static void schedule(Context ctx){
        PendingIntent sendTrajServ = PendingIntent.getService(ctx, 0, new Intent(
                ctx, TripService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 
            SystemClock.elapsedRealtime() + threeMins, threeMins, sendTrajServ);
    }

}
