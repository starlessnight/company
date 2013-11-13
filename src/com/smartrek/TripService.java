package com.smartrek;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

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
        User user = User.getCurrentUser(this);
        if(user != null){
            File[] files = getDir(this).listFiles();
            if(ArrayUtils.isNotEmpty(files)){
                for (File f : files) {
                    try{
                        long rId = Long.parseLong(f.getName());
                        SendTrajectoryService.send(this, rId);
                        new TripValidationRequest(user, rId).execute(this);
                        FileUtils.deleteQuietly(f);
                    }catch(SmarTrekException e){
                        FileUtils.deleteQuietly(f);
                    }catch(Throwable t){
                        Log.w("TripService", Log.getStackTraceString(t));
                    }
                }
            }
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
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
            twoSecs, sendTrajServ);
    }

}
