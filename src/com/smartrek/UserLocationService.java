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
import com.smartrek.activities.DebugOptionsActivity;
import com.smartrek.activities.DebugOptionsActivity.LatLon;
import com.smartrek.activities.MainActivity;
import com.smartrek.models.Trajectory;
import com.smartrek.models.User;
import com.smartrek.requests.Request;
import com.smartrek.requests.SendTrajectoryRequest;
import com.smartrek.utils.RouteNode;

public class UserLocationService extends IntentService {
    
    public static Long getInterval(){
        Long interval = Request.getActivityDistanceInterval();
        return interval == null?null:(interval * 3600000 / 60000);
    }
    
    private static final int RID = 9999;
    
    private static final long FIFTEEN_MINS = 15 * 60 * 1000L;
    
    public UserLocationService() {
        super(UserLocationService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final User user = User.getCurrentUserWithoutCache(UserLocationService.this);
        if(user != null){
            MainActivity.initApiLinksIfNecessary(this, new Runnable() {
                @Override
                public void run() {
                    try {
                        LocationInfo info = new LocationInfo(UserLocationService.this);
                        LatLon lastLoc = DebugOptionsActivity.getLastUserLatLon(UserLocationService.this);
                        Long distanceInterval = Request.getActivityDistanceInterval();
                        if(distanceInterval != null && (lastLoc == null || RouteNode.distanceBetween(lastLoc.lat, 
                                lastLoc.lon, info.lastLat, info.lastLong) >= distanceInterval.doubleValue())){
                            Log.i("UserLocationService", "onHandleIntent");
                            DebugOptionsActivity.setLastUserLatLon(UserLocationService.this, info.lastLat, info.lastLong);
                            File file = getFile(UserLocationService.this);
                            Trajectory traj;
                            if(file.exists() && file.length() != 0){
                                traj = Trajectory.from(new JSONArray(FileUtils.readFileToString(file)));
                            }else{
                                traj = new Trajectory();
                            }
                            traj.accumulate(info.lastLat, info.lastLong, info.lastAltitude, 
                                info.lastSpeed, info.lastHeading, System.currentTimeMillis(), 
                                Trajectory.DEFAULT_LINK_ID);
                            FileUtils.write(file, traj.toJSON().toString());
                            long now = System.currentTimeMillis();
                            if(now - DebugOptionsActivity.getLastUserLatLonSent(UserLocationService.this) >= FIFTEEN_MINS){
                                SendTrajectoryRequest request = new SendTrajectoryRequest();
                                if(Request.NEW_API){
                                    request.execute(user, traj, UserLocationService.this);
                                }else{
                                    request.execute(0, user.getId(), RID, traj);
                                }
                                DebugOptionsActivity.setLastUserLatLonSent(UserLocationService.this, now);
                                FileUtils.deleteQuietly(file);
                            }
                        }
                    }
                    catch (Throwable t) {
                        Log.d("UserLocationService", Log.getStackTraceString(t));
                    }
                }
            });
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
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(), getInterval(), sendTrajServ);
    }

}
