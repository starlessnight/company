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

import com.smartrek.activities.LandingActivity;
import com.smartrek.exceptions.SmarTrekException;
import com.smartrek.models.User;
import com.smartrek.requests.RouteValidationRequest;

public class ValidationService extends IntentService {
    
    private static long tenSecs = 10 * 1000; 
    
    public ValidationService() {
        super(ValidationService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LandingActivity.initializeIfNeccessary(this, new Runnable() {
            @Override
            public void run() {
                User user = User.getCurrentUser(ValidationService.this);
                if(user != null){
                    File[] files = getDir(ValidationService.this).listFiles();
                    if(ArrayUtils.isNotEmpty(files)){
                        for (File f : files) {
                            try{
                                new RouteValidationRequest(user, Long.parseLong(f.getName())).execute(ValidationService.this);
                                FileUtils.deleteQuietly(f);
                            }catch(SmarTrekException e){
                                FileUtils.deleteQuietly(f);
                            }catch(Throwable t){
                                Log.w("ValidationService", Log.getStackTraceString(t));
                            }
                        }
                    }
                }
            }
        }, false);
    }
    
    private static File getDir(Context ctx){
        return new File(ctx.getExternalFilesDir(null), "validation");
    }
    
    public static File getFile(Context ctx, long rId){
        return new File(getDir(ctx), String.valueOf(rId));
    }
    
    public static void schedule(Context ctx){
        PendingIntent sendTrajServ = PendingIntent.getService(ctx, 0, new Intent(
                ctx, ValidationService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
            tenSecs, sendTrajServ);
    }

}
