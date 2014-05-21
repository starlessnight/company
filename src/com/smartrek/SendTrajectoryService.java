package com.smartrek;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.smartrek.activities.LandingActivity;
import com.smartrek.models.Trajectory;
import com.smartrek.models.User;
import com.smartrek.requests.Request;
import com.smartrek.requests.SendTrajectoryRequest;

public class SendTrajectoryService extends IntentService {
    
    private static long sevenDays = 7 * 24 * 60 * 60 * 1000;
    
    private static long fiveMins = 5 * 60 * 1000; 
    
    public SendTrajectoryService() {
        super(SendTrajectoryService.class.getName());
    }
    
    public static boolean isSending(Context ctx, long rId){
        return new File(getInDir(ctx), "_" + rId).exists();
    }
    
    public static boolean send(Context ctx, long rId){
        return send(ctx, new File(getInDir(ctx), String.valueOf(rId)));
    }
    
    private static boolean send(Context ctx, File routeDir){
        boolean success = true;
        User user = User.getCurrentUser(ctx);
        if(user != null && ArrayUtils.isNotEmpty(routeDir.list())){
            String originalName = routeDir.getName();
            String newName = "_" + originalName;
            try {
                File newDir = new File(routeDir.getParentFile(), newName);
                routeDir.renameTo(newDir);
                routeDir = newDir;
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
                long routeId = Long.parseLong(originalName);
                File outFile = getOutFile(ctx, routeId);
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
                if(Request.NEW_API){
                    request.execute(user, routeId, traj, ctx);
                }else{
                    request.execute(seq, user.getId(), routeId, traj);
                }
                try{
                    FileUtils.write(outFile, String.valueOf(seq));
                }catch(Exception e){}
                for(File f:toSendFiles){
                    FileUtils.deleteQuietly(f);
                }
            }
            catch (Exception e) {
                success = false;
                Log.d("SendTrajectoryService", Log.getStackTraceString(e));
            }
            finally{
                routeDir.renameTo(new File(routeDir.getParentFile(), originalName));
            }
        }
        return success;
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        LandingActivity.initializeIfNeccessary(this, new Runnable() {
            @Override
            public void run() {
                User user = User.getCurrentUser(SendTrajectoryService.this);
                if(user != null){
                    File inDir = getInDir(SendTrajectoryService.this);
                    File[] routeDirs = inDir.listFiles();
                    if(ArrayUtils.isNotEmpty(routeDirs)){
                        for(File d:routeDirs){
                            String[] files = d.list();
                            if(d.lastModified() < System.currentTimeMillis() - sevenDays){
                                FileUtils.deleteQuietly(d);
                            }else if(ArrayUtils.isNotEmpty(files) && d != null 
                                    && StringUtils.isNumeric(d.getName())){
                                send(SendTrajectoryService.this, d);
                            }
                        }
                    }   
                }
                File[] oFiles = getOutDir(SendTrajectoryService.this).listFiles();
                if(ArrayUtils.isNotEmpty(oFiles)){
                    for (File f : oFiles) {
                        if(f.lastModified() < System.currentTimeMillis() - sevenDays){
                            FileUtils.deleteQuietly(f);
                        }
                    }
                }
            }
        }, false);
    }
    
    private static File getOutDir(Context ctx){
        return new File(ctx.getExternalFilesDir(null), "trajectory/out");
    }
    
    private static File getOutFile(Context ctx, long rId){
        return new File(getOutDir(ctx), String.valueOf(rId));
    }
    
    private static File getInDir(Context ctx){
        return new File(ctx.getExternalFilesDir(null), "trajectory/in");
    }
    
    public static File getInFile(Context ctx, long rId, int seq){
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
