package com.smartrek;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.smartrek.activities.LandingActivity;
import com.smartrek.exceptions.SmarTrekException;
import com.smartrek.models.User;
import com.smartrek.requests.TripValidationRequest;

public class TripService extends IntentService {
    
    private static long twoSecs = 2 * 1000; 
    
    public TripService() {
        super(TripService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LandingActivity.initializeIfNeccessary(this, new Runnable() {
            @Override
            public void run() {
                User user = User.getCurrentUser(TripService.this);
                if(user != null){
                    File[] files = getDir(TripService.this).listFiles();
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
                            try{
                                if(!SendTrajectoryService.isSending(TripService.this, rId)
                                        && SendTrajectoryService.send(TripService.this, rId)){
                                    new TripValidationRequest(user, rId).execute(TripService.this);
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
            }
        }, false);
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
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
            twoSecs, sendTrajServ);
    }

}
