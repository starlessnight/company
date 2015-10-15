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
import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;

import com.metropia.activities.MainActivity;
import com.metropia.activities.PassengerActivity;
import com.metropia.activities.ValidationActivity;
import com.metropia.exceptions.WrappedIOException;
import com.metropia.models.Trajectory;
import com.metropia.models.User;
import com.metropia.requests.Request;
import com.metropia.requests.SendTrajectoryRequest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class TrajectorySendingService extends Service {
	
	private static long sevenDays = 7 * 24 * 60 * 60 * 1000;
	
	public static String ALL_FRAGMENT = "ALL_FRAGMENT";
    public static String FRONT_FRAGMENT = "FRONT_FRAGMENT";
    private static final String IMD_PREFIX = "[";
	
	Handler handler;
    private static final Object mutex = new Object();
    public static boolean isRunning;
	

	@Override
	public IBinder onBind(Intent i) {return null;}
	
	@Override  
    public void onCreate() {  
        super.onCreate();
    }  
	
	@Override  
    public int onStartCommand(Intent intent, int flags, int startId) {
		isRunning = true;
		
		handler = new Handler();
		new Thread(checkFiles).run();
		
        return super.onStartCommand(intent, flags, startId);  
    }  
	

    @Override
    public void onDestroy() {
        handler.removeCallbacks(checkFiles);
        isRunning = false;
        super.onDestroy();
    }
    
    
    

    static boolean isExecuting = false;
    static boolean lastSendResult = true;
    static boolean empty = false;
    static int emptyCounter = 0;
    
    Runnable checkFiles = new Runnable() {

		@Override
		public void run() {
			handler.postDelayed(checkFiles, 20000);
			
	    	if (isExecuting) return;
	    	isExecuting = true;
	    	lastSendResult = true;
        	empty = true;
	    	
	        User user = User.getCurrentUserWithoutCache(TrajectorySendingService.this);
	        if(user != null){
	            MainActivity.initApiLinksIfNecessary(TrajectorySendingService.this, new Runnable() {
	                @Override
	                public void run() {
	                    File inDir = getInDir(TrajectorySendingService.this);
	                    File duoDir = getDuoDir(TrajectorySendingService.this);
	                    File[] routeDirs = inDir.listFiles();
	                    File[] duoDirs = duoDir.listFiles();
	                    
	                    File[] routes = ArrayUtils.addAll(routeDirs, duoDirs);
	                    if(ArrayUtils.isNotEmpty(routes)){
	                        for(File d:routes){
	                            String[] files = d.list();
	                            if(d.lastModified() < System.currentTimeMillis() - sevenDays){
	                                FileUtils.deleteQuietly(d);
	                            }
	                            else if(d != null && (StringUtils.isNumeric(d.getName()) || StringUtils.startsWith(d.getName(), IMD_PREFIX))){
	                                if (ArrayUtils.isEmpty(files)) continue;
	                                empty = false;
	                                
	                            	String mode = d.getParentFile().equals(duoDir)? PassengerActivity.PASSENGER_TRIP_VALIDATOR:ValidationActivity.TRIP_VALIDATOR;
	                                while (d.list().length>0 && lastSendResult) send(TrajectorySendingService.this, d, false, mode, FRONT_FRAGMENT);
	                            }
	                        }
	                    }
	                    else empty = true;
	                }
	            });
	        }
	        File[] oFiles = getOutDir(TrajectorySendingService.this).listFiles();
	        if(ArrayUtils.isNotEmpty(oFiles)){
	            for (File f : oFiles) {
	                if(f.lastModified() < System.currentTimeMillis() - sevenDays){
	                    FileUtils.deleteQuietly(f);
	                }
	            }
	        }
	        isExecuting = false;
	        if (!empty) emptyCounter = 0;
	        else if (++emptyCounter>5) {
	        	isRunning = false;
	        	emptyCounter = 0;
	        	TrajectorySendingService.this.stopSelf();
	        }
		}
	};
	
	
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
	                
	                
	                long routeId = Long.parseLong(StringUtils.startsWith(originalName, IMD_PREFIX) ? originalName.substring(1) : originalName);
	                File outFile = getOutFile(ctx, routeId);
	                int seq = 1;
	                if(outFile.exists()){
	                    try {
	                        seq = Integer.parseInt(FileUtils.readFileToString(outFile)) + 1;
	                    }
	                    catch (Exception e) {}
	                }
	                
	                
	                for (File f : files) {
	                    try {
	                        Trajectory t = Trajectory.from(new JSONArray(FileUtils.readFileToString(f)));
	                        traj.append(t);
	                        toSendFiles.add(f);
	                    }
	                    catch (Exception e) {}
	                    if (range.equals(FRONT_FRAGMENT)) break;
	                }
	                
	                SendTrajectoryRequest request = new SendTrajectoryRequest(imdSend, files.length);
	                if(Request.NEW_API){
	                    try{
	                    	request.setSerialNum(range.equals(FRONT_FRAGMENT)? seq:null);
	                    	Exception e = (Exception) request.executeAsync(user, routeId, traj, ctx, mode).get();
	                    	if (e!=null) throw e;
	                    	
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
	            }
	            finally{
	                routeDir.renameTo(new File(routeDir.getParentFile(), originalName));
	            }
	        }
	        return success;
    	}
    }
}
