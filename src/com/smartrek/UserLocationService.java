package com.smartrek;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.smartrek.models.Trajectory;
import com.smartrek.models.User;
import com.smartrek.requests.Request;
import com.smartrek.requests.SendTrajectoryRequest;

public class UserLocationService extends IntentService {

    private static final long FIFTHTEEN_MINS = 15 * 60 * 1000 /* 10000 */;

    private static final int RID = 9999;
    
    public UserLocationService() {
        super(UserLocationService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("UserLocationService", "onHandleIntent");
        
        User user = User.getCurrentUser(this);
        if(user != null){
            LocationInfo info = new LocationInfo(this);
            Trajectory traj = new Trajectory();
            traj.accumulate(info.lastLat, info.lastLong, info.lastAltitude, 
                info.lastSpeed, info.lastHeading, System.currentTimeMillis(), 
                Trajectory.DEFAULT_LINK_ID);
            SendTrajectoryRequest request = new SendTrajectoryRequest();
            try {
                if(Request.NEW_API){
                    request.execute(user, traj);
                }else{
                    request.execute(0, user.getId(), RID, traj);
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
                SystemClock.elapsedRealtime(), FIFTHTEEN_MINS, sendTrajServ);
    }

}
