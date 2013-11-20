package com.smartrek;

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
    
    public UserLocationService() {
        super(UserLocationService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final User user = User.getCurrentUser(this);
        if(user != null){
            try {
                LocationInfo info = new LocationInfo(UserLocationService.this);
                LatLon lastLoc = DebugOptionsActivity.getLastUserLatLon(UserLocationService.this);
                Long distanceInterval = Request.getActivityDistanceInterval();
                if(distanceInterval != null && (lastLoc == null || RouteNode.distanceBetween(lastLoc.lat, 
                        lastLoc.lon, info.lastLat, info.lastLong) >= distanceInterval.doubleValue())){
                    Log.i("UserLocationService", "onHandleIntent");
                    DebugOptionsActivity.setLastUserLatLon(UserLocationService.this, info.lastLat, info.lastLong);
                    Trajectory traj = new Trajectory();
                    traj.accumulate(info.lastLat, info.lastLong, info.lastAltitude, 
                        info.lastSpeed, info.lastHeading, System.currentTimeMillis(), 
                        Trajectory.DEFAULT_LINK_ID);
                    SendTrajectoryRequest request = new SendTrajectoryRequest();
                    if(Request.NEW_API){
                        request.execute(user, traj, this);
                    }else{
                        request.execute(0, user.getId(), RID, traj);
                    }
                }
            }
            catch (Throwable t) {
                Log.d("UserLocationService", Log.getStackTraceString(t));
            }
        }
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
