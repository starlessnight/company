package com.metropia;

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

import com.metropia.activities.DebugOptionsActivity;
import com.metropia.activities.MainActivity;
import com.metropia.activities.PassengerActivity;
import com.metropia.activities.ValidationActivity;
import com.metropia.exceptions.WrappedIOException;
import com.metropia.models.Trajectory;
import com.metropia.models.User;
import com.metropia.requests.Request;
import com.metropia.requests.SendTrajectoryRequest;

@Deprecated
public class SendTrajectoryService extends IntentService {
    
    private static long sevenDays = 7 * 24 * 60 * 60 * 1000;
    
    private static long oneMins = 1 * 60 * 1000;
    private static long twoMins = 2 * 60 * 1000;
    private static long eightHours = 8 * 60 * 60 * 1000;
    private static long fiveMins = 5 * 60 * 1000;
    
    public static String ALL_FRAGMENT = "ALL_FRAGMENT";
    public static String FRONT_FRAGMENT = "FRONT_FRAGMENT";
    
    private static final String IMD_PREFIX = "[";
    
    public SendTrajectoryService() {
        super(SendTrajectoryService.class.getName());
    }
    
    public static boolean isSending(Context ctx, long rId){
        return new File(getInDir(ctx), "_" + rId).exists();
    }
    
    /**@param mode specify Driver or DUO**/
    public static boolean send(Context ctx, long rId, String mode){
    	File parentDir = mode.equals(PassengerActivity.PASSENGER_TRIP_VALIDATOR)? getDuoDir(ctx):getInDir(ctx);
        return send(ctx, new File(parentDir, String.valueOf(rId)), false, mode, ALL_FRAGMENT);
    }
    
    /**@param mode specify Driver or DUO**/
    public static boolean sendImd(Context ctx, long rId, String mode){
    	File parentDir = mode.equals(PassengerActivity.PASSENGER_TRIP_VALIDATOR)? getDuoDir(ctx):getInDir(ctx);
        return send(ctx, new File(parentDir, IMD_PREFIX + String.valueOf(rId)), true, mode, ALL_FRAGMENT);
    }
    
    /**@param mode specify Driver or DUO**/
    private static boolean send(final Context ctx, File routeDir, boolean imdSend, String mode, String range){
    	synchronized (mutex) {
	        boolean success = true;
	        User user = User.getCurrentUser(ctx);
	        if(user != null && ArrayUtils.isNotEmpty(routeDir.list())){
	            String originalName = imdSend ? routeDir.getName().substring(1) : routeDir.getName();
	            String newName = "_" + originalName;
	            try {
	                File newDir = new File(routeDir.getParentFile(), newName);
	                routeDir.renameTo(newDir);
	                routeDir = newDir;
	                File[] files = routeDir.listFiles();
	                Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
	                Trajectory traj = new Trajectory();
	                List<File> toSendFiles = new ArrayList<File>();
	                for (File f : files) {
	                    try {
	                        Trajectory t = Trajectory.from(new JSONArray(FileUtils.readFileToString(f)));
	                        traj.append(t);
	                        toSendFiles.add(f);
	                    }
	                    catch (Exception e) {
	                        Log.d("SendTrajectoryService", Log.getStackTraceString(e));
	                    }
	                    if (range.equals(FRONT_FRAGMENT)) break;
	                }
	                long routeId = Long.parseLong(StringUtils.startsWith(originalName, IMD_PREFIX) ? originalName.substring(1) : originalName);
	                File outFile = getOutFile(ctx, routeId);
	                int seq = 1;
	                if(outFile.exists()){
	                    try {
	                        seq = Integer.parseInt(FileUtils.readFileToString(outFile)) + 1;
	                    }
	                    catch (NumberFormatException e) {}
	                    catch (IOException e) {}
	                }
	                SendTrajectoryRequest request = new SendTrajectoryRequest(imdSend, files.length);
	                if(Request.NEW_API){
	                    try{
	                    	request.setSerialNum(range.equals(FRONT_FRAGMENT)? seq:null);
	                    	request.execute(user, routeId, traj, ctx, mode);
	                    	lastSendResult = true;
	                    }catch(Exception e){
	                    	if (e instanceof WrappedIOException && ((WrappedIOException)e).getResponseCode()==404) lastSendResult = true;
	                    	else lastSendResult = false;
	                    }
	                }else{
	                    //request.execute(seq, user.getId(), routeId, traj);
	                }
	                
	                if (lastSendResult)
	                try{
	                    FileUtils.write(outFile, String.valueOf(seq));
	                }catch(Exception e){}
	                
	                if (lastSendResult)
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
    }
    
    static int timer = 0;
    static boolean lastSendResult = true;
    static boolean isExecuting = false;
    static boolean empty = false;
    static int emptyCounter = 0;
    @Override
    protected void onHandleIntent(Intent intent) {
    	timer+=1000;
    	if (isExecuting || ((empty || !lastSendResult) && timer%(20*1000)!=0)) return;
    	isExecuting = true;
    	
        User user = User.getCurrentUserWithoutCache(this);
        if(user != null){
            MainActivity.initApiLinksIfNecessary(this, new Runnable() {
                @Override
                public void run() {
                    File inDir = getInDir(SendTrajectoryService.this);
                    File duoDir = getDuoDir(SendTrajectoryService.this);
                    File[] routeDirs = inDir.listFiles();
                    File[] duoDirs = duoDir.listFiles();
                    
                    File[] routes = ArrayUtils.addAll(routeDirs, duoDirs);
                    if(ArrayUtils.isNotEmpty(routes)){
                    	empty = true;
                        for(File d:routes){
                            String[] files = d.list();
                            if(d.lastModified() < System.currentTimeMillis() - sevenDays){
                                FileUtils.deleteQuietly(d);
                            }
                            else if(d != null && (StringUtils.isNumeric(d.getName()) || StringUtils.startsWith(d.getName(), IMD_PREFIX))){
                            	String mode = d.getParentFile().equals(duoDir)? PassengerActivity.PASSENGER_TRIP_VALIDATOR:ValidationActivity.TRIP_VALIDATOR;
                                send(SendTrajectoryService.this, d, false, mode, FRONT_FRAGMENT);
                                if (ArrayUtils.isNotEmpty(files)) empty = false;
                            }
                        }
                        if (empty) {
                        	emptyCounter++;
                        }
                        else emptyCounter = 0;
                    }
                }
            });
        }
        File[] oFiles = getOutDir(SendTrajectoryService.this).listFiles();
        if(ArrayUtils.isNotEmpty(oFiles)){
            for (File f : oFiles) {
                if(f.lastModified() < System.currentTimeMillis() - sevenDays){
                    FileUtils.deleteQuietly(f);
                }
            }
        }
        isExecuting = false;
        if (empty && emptyCounter>=5) {
        	stopSchedule(SendTrajectoryService.this);
        	emptyCounter = 0;
        }
    }
    
    private static File getOutDir(Context ctx){
        return new File(ctx.getExternalFilesDir(null), "trajectory/out");
    }
    private static File getOutFile(Context ctx, long rId){
        return new File(getOutDir(ctx), String.valueOf(rId));
    }
    
    private static final Object mutex = new Object();
    
    private static File getInDir(Context ctx){
    	return new File(ctx.getExternalFilesDir(null), "trajectory/in");
    }
    public static File getInFile(Context ctx, long rId, int seq){
    	synchronized (mutex) {
    		return new File(getInDir(ctx), IMD_PREFIX + rId + "/" + seq);
    	}
    }
    
    public static File getDuoDir(Context ctx) {
    	return new File(ctx.getExternalFilesDir(null), "trajectory/duo");
    }
    public static File getDuoFile(Context ctx, long rId, int seq) {
    	return new File(getDuoDir(ctx), IMD_PREFIX + rId + "/" + seq);
    }
    
    
    public static boolean isRunning;
    static int interval;
    public static void schedule(final Context ctx){
    	
        PendingIntent sendTrajServ = PendingIntent.getService(ctx, 0, new Intent(ctx, SendTrajectoryService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+10000, 1000, sendTrajServ);
        isRunning = true;
    }
    public static void stopSchedule(final Context ctx) {
    	PendingIntent stopIntent = PendingIntent.getService(ctx, 0, new Intent(ctx, SendTrajectoryService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        alarm.cancel(stopIntent);
        isRunning = false;
    }

}
