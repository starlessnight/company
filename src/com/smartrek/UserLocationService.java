package com.smartrek;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.smartrek.activities.DebugOptionsActivity;
import com.smartrek.activities.DebugOptionsActivity.LatLon;
import com.smartrek.activities.MainActivity;
import com.smartrek.activities.ValidationActivity;
import com.smartrek.models.Trajectory;
import com.smartrek.models.User;
import com.smartrek.requests.Request;
import com.smartrek.requests.SendTrajectoryRequest;
import com.smartrek.utils.RouteNode;

public class UserLocationService extends IntentService {
    
    public static Long getInterval(Context ctx){
        return 6 * 1000L;
    }
    
    private static final int RID = 9999;
    
    private static final long FIFTEEN_MINS = 15 * 60 * 1000L;
    
    public UserLocationService() {
        super(UserLocationService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            LocationInfo info = new LocationInfo(this);
            LatLon lastLoc = DebugOptionsActivity.getLastUserLatLon(this);
            long distanceInterval = DebugOptionsActivity.getActivityDistanceInterval(this);
            boolean hasLastLoc = lastLoc != null;
            double distance = hasLastLoc?RouteNode.distanceBetween(lastLoc.lat, lastLoc.lon, info.lastLat, info.lastLong):0;
            final long now = System.currentTimeMillis();
            if(!hasLastLoc || distance >= distanceInterval + info.lastAccuracy){
                DebugOptionsActivity.setLastUserLatLon(this, info.lastLat, info.lastLong, now);
                LocationLibrary.useFineAccuracyForRequests(this, true);
                final File file = getFile(this);
                final Trajectory traj;
                if(file.exists() && file.length() != 0){
                    traj = Trajectory.from(new JSONArray(FileUtils.readFileToString(file)));
                }else{
                    traj = new Trajectory();
                }
                traj.accumulate(info.lastLat, info.lastLong, info.lastAltitude, 
                    info.lastSpeed, info.lastHeading, now, 
                    Trajectory.DEFAULT_LINK_ID, info.lastAccuracy);
                FileUtils.write(file, traj.toJSON().toString());
            }else if(hasLastLoc && now - lastLoc.time > ValidationActivity.TWO_MINUTES){
                LocationLibrary.useFineAccuracyForRequests(this, false);
            }
            final User user = User.getCurrentUserWithoutCache(this);
            if(user != null && (now - DebugOptionsActivity.getLastUserLatLonSent(this)) >= FIFTEEN_MINS){
                File file = getFile(this);
                if(file.exists() && file.length() != 0){
                    final Trajectory traj = Trajectory.from(new JSONArray(FileUtils.readFileToString(file)));
                    if(traj.size() > 0){
                        DebugOptionsActivity.setLastUserLatLonSent(this, now);
                        MainActivity.initApiLinksIfNecessary(this, new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    SendTrajectoryRequest request = new SendTrajectoryRequest(false);
                                    if(Request.NEW_API){
                                        request.execute(user, traj, UserLocationService.this);
                                    }else{
                                        request.execute(0, user.getId(), RID, traj);
                                    }
                                    File newFile = getFile(UserLocationService.this);
                                    if(newFile.exists() && newFile.length() != 0){
                                        Trajectory newTraj = Trajectory.from(new JSONArray(FileUtils.readFileToString(newFile)));
                                        newTraj.removeOlderRecords(now);
                                        FileUtils.write(newFile, newTraj.toJSON().toString());
                                    }
                                }
                                catch (Throwable t) {
                                    Log.d("UserLocationService", Log.getStackTraceString(t));
                                }
                            }
                        });
                    }
                }
            }
        }
        catch (Throwable t) {
            Log.d("UserLocationService", Log.getStackTraceString(t));
        }
    }
    
    private static File getFile(Context ctx){
        return new File(ctx.getExternalFilesDir(null), "user_location");
    }
    
    public static void schedule(Context ctx) {
        PendingIntent sendTrajServ = PendingIntent.getService(ctx, 0,
                new Intent(ctx, UserLocationService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        Long interval = getInterval(ctx);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + interval, interval, sendTrajServ);
    }

}
