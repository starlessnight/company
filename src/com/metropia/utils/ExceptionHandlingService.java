package com.metropia.utils;

import java.io.PrintStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Stack;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;

import com.metropia.dialogs.NotificationDialog2;
import com.metropia.activities.R;

public class ExceptionHandlingService {
	
	public static final String LOG_TAG = "ExceptionHandlingService";
	
	/**
	 * Contains an exception instance and other metadata
	 */
	public static class ExceptionContainer {
		private Exception e;
		private String preferredMessage;
		
		public ExceptionContainer(Exception e) {
			this.e = e;
		}
		
		public ExceptionContainer(Exception e, String preferredMessage) {
			this.e = e;
			this.preferredMessage = preferredMessage;
		}
		
		public Exception getException() {
			return e;
		}
		
		public String getPreferredMessage() {
			return preferredMessage;
		}
		
		public boolean hasPreferredMessage() {
			return preferredMessage != null && !preferredMessage.equals("");
		}
		
		public String getMessage() {
			return hasPreferredMessage() ? getPreferredMessage() : Log.getStackTraceString(getException());
		}
		
		@Override
		public String toString() {
			return String.format("%s: %s", e.getClass().toString(), preferredMessage != null ? preferredMessage : Log.getStackTraceString(e));
		}
	}
	
    private Stack<ExceptionContainer> exceptions = new Stack<ExceptionContainer>();
    private Context context;
    
    public ExceptionHandlingService(Context context) {
        this.context = context;
    }
    
    public synchronized boolean hasExceptions() {
        return !exceptions.isEmpty();
    }
    
    /**
     * Adds an exception instance to the stack
     */
    public synchronized void registerException(Exception e) {
        registerException(e, (String)null);
    }
    
    /**
     * Adds an exception instance to the stack
     */
    public synchronized void registerException(Exception e, String preferredMessage) {
    	ExceptionContainer ec = new ExceptionContainer(e, preferredMessage);
    	exceptions.push(ec);
    	
    	Log.d(LOG_TAG, ec.toString());
    	e.printStackTrace();
    }
    
    /**
     * Adds an exception instance to the stack
     * 
     * @deprecated Not to use System.err. Use Log.d().
     */
    public synchronized void registerException(Exception e, PrintStream err) {
        if (err != null) {
            e.printStackTrace(err);
        }
        exceptions.push(new ExceptionContainer(e));
    }
    
    private Dialog lastDialog; 
    
    /**
     * Reports an exception immediately.
     * 
     * @param message
     */
    public synchronized void reportException(final String message, final Runnable callback) {
        try{
            if(lastDialog != null && lastDialog.isShowing()){
                lastDialog.dismiss();
                lastDialog = null;
            }
        	final NotificationDialog2 dialog = new NotificationDialog2(context, "An error has occurred.");
        	dialog.setVerticalOrientation(false);
        	dialog.setNegativeButtonText("OK");
            dialog.setNegativeActionListener(new NotificationDialog2.ActionListener() {
                @Override
                public void onClick() {
                    if(callback != null) {
                        callback.run();
                    }
                }
            });
            
            dialog.setPositiveButtonText("More");
            dialog.setPostiveClickDismiss(false);
            dialog.setDetailMessage(message);
            dialog.setPositiveActionListener(new NotificationDialog2.ActionListener() {
				@Override
				public void onClick() {
					dialog.hideNegativeButtonAndShowDetail();
				}
			});
            
        	dialog.show();
        	lastDialog = dialog;
        }catch(Throwable t){}
    }
    
    public synchronized void reportException(String message) {
        reportException(message, null);
    }
    
    public synchronized void reportException(Exception e) {
//    	reportException(Log.getStackTraceString(e));
    	reportException(e.getMessage());
    }
    
    public synchronized void reportExceptions(Runnable callback) {
        while (!exceptions.isEmpty()) {
        	ExceptionContainer ec = exceptions.pop();
            String message;
            if(ec.e instanceof ConnectException || ec.e instanceof UnknownHostException){
                message = context.getString(R.string.no_connection);
            }else if(ec.e instanceof SocketTimeoutException){
                message = context.getString(R.string.connection_timeout); 
            }else{
                message = ec.getMessage();
            }
            reportException(message, callback);
        }
    }
    
    public synchronized void reportExceptions() {
        reportExceptions(null);
    }
    
    public ExceptionContainer popException() {
    	return exceptions.pop();
    }
}
