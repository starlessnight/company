package com.smartrek;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.json.JSONArray;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.smartrek.models.Trajectory;
import com.smartrek.models.User;
import com.smartrek.requests.SendTrajectoryRequest;

public class SendTrajectoryService extends IntentService {
    
    private static long sevenDays = 7 * 24 * 60 * 60 * 1000;
    
    private static long fiveMins = 5 * 60 * 1000; 
    
    public SendTrajectoryService() {
        super(SendTrajectoryService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User user = User.getCurrentUser(this);
        if(user != null){
            File inDir = getInDir(this);
            File[] routeDirs = inDir.listFiles();
            if(routeDirs.length > 0){
                Arrays.sort(routeDirs);
                File routeDir = null;
                for(File d:routeDirs){
                    if(d.list().length > 0){
                        routeDir = d;
                    }else if(d.lastModified() < System.currentTimeMillis() - sevenDays){
                        FileUtils.deleteQuietly(d);
                    }
                }
                if(routeDir != null){
                    File[] files = routeDir.listFiles();
                    Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
                    Trajectory traj = new Trajectory();
                    long startTime = files[0].lastModified();
                    List<File> toSendFiles = new ArrayList<File>();
                    for (File f : files) {
                        if(f.lastModified() < startTime + fiveMins){
                            try {
                                Trajectory t = Trajectory.from(new JSONArray(FileUtils.readFileToString(f)));
                                traj.append(t);
                                toSendFiles.add(f);
                            }
                            catch (Exception e) {
                                Log.d("SendTrajectoryService", Log.getStackTraceString(e));
                            }
                        }else{
                            break;
                        }
                    }
                    int routeId = Integer.parseInt(routeDir.getName());
                    File outFile = getOutFile(routeId);
                    int seq = 1;
                    if(outFile.exists()){
                        try {
                            seq = Integer.parseInt(FileUtils.readFileToString(outFile)) + 1;
                        }
                        catch (NumberFormatException e) {
                        }
                        catch (IOException e) {
                        }
                    }
                    SendTrajectoryRequest request = new SendTrajectoryRequest();
                    try {
                        request.execute(seq, user.getId(), routeId, traj);
                        try{
                            FileUtils.write(outFile, String.valueOf(seq));
                        }catch(Exception e){}
                        for(File f:toSendFiles){
                            FileUtils.deleteQuietly(f);
                        }
                    }
                    catch (Exception e) {
                        Log.d("SendTrajectoryService", Log.getStackTraceString(e));
                    }
                }
            }   
        }
        for (File f : getOutDir().listFiles()) {
            if(f.lastModified() < System.currentTimeMillis() - sevenDays){
                FileUtils.deleteQuietly(f);
            }
        }
    }
    
    private File getOutDir(){
        return new File(getExternalFilesDir(null), "trajectory/out");
    }
    
    private File getOutFile(int rId){
        return new File(getOutDir(), String.valueOf(rId));
    }
    
    private static File getInDir(Context ctx){
        return new File(ctx.getExternalFilesDir(null), "trajectory/in");
    }
    
    public static File getInFile(Context ctx, int rId, int seq){
        return new File(getInDir(ctx), rId + "/" + seq);
    }
    
    public static void schedule(Context ctx){
        PendingIntent sendTrajServ = PendingIntent.getService(ctx, 0, new Intent(
                ctx, SendTrajectoryService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
            fiveMins, sendTrajServ);
    }

}
