package com.metropia.utils;

import java.io.PrintStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;

import com.metropia.activities.DebugOptionsActivity;
import com.metropia.activities.R;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.exceptions.ServiceFailException;
import com.metropia.exceptions.WrappedIOException;
import com.metropia.requests.Request;

public class ExceptionHandlingService {
	
	public static final String LOG_TAG = "ExceptionHandlingService";
	
	/**
	 * Contains an exception instance and other metadata
	 */
	public static class ExceptionContainer {
		private Exception e;
		private String preferredMessage;
		private String detailMessage;
		
		public ExceptionContainer(Exception e) {
			this.e = e;
		}
		
		public ExceptionContainer(Exception e, String preferredMessage, String detailMessage) {
			this.e = e;
			this.preferredMessage = preferredMessage;
			this.detailMessage = detailMessage;
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

		public String getDetailMessage() {
			return detailMessage;
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
    	String detailMessage = null;
    	Exception cause = null;
    	try {
    		if(e instanceof WrappedIOException) {
    			WrappedIOException wioe = (WrappedIOException) e;
    			cause = (wioe.getCause() != null && wioe.getCause() instanceof Exception) ? (Exception)wioe.getCause() : e;
    			JSONObject detailMessageJson = new JSONObject(StringUtils.defaultString(wioe.getDetailMessage(), ""));
    			detailMessage = detailMessageJson.optString(Request.ERROR_MESSAGE, preferredMessage);
    		}
    	}
    	catch(JSONException ignore) {}
    	ExceptionContainer ec = new ExceptionContainer(cause != null ? cause : e, preferredMessage, detailMessage);
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
    public synchronized void reportException(final String message, final String detailMessage, final Runnable callback) {
        try{
            if(lastDialog != null && lastDialog.isShowing()){
                lastDialog.dismiss();
                lastDialog = null;
            }
            
            String messageContent = StringUtils.isNotBlank(message) ? message : "An error has occurred.";
            
        	final NotificationDialog2 dialog = new NotificationDialog2(context, messageContent);
        	dialog.setVerticalOrientation(false);
        	NotificationDialog2.ActionListener okActionListener = new NotificationDialog2.ActionListener() {
                @Override
                public void onClick() {
                    if(callback != null) {
                        callback.run();
                    }
                }
            }; 
        	            
            if(DebugOptionsActivity.isPopupMessageMoreEnabled(context)) {
            	dialog.setPositiveButtonText("OK");
	            dialog.setPositiveActionListener(okActionListener);
	            
	            dialog.setNegativeButtonText("More");
	            //dialog.setPostiveClickDismiss(false);
	            dialog.setDetailMessage(detailMessage);
	            dialog.setNegativeActionListener(new NotificationDialog2.ActionListener() {
					@Override
					public void onClick() {
						dialog.hideNegativeButtonAndShowDetail();
					}
				});
            }
            else {
            	dialog.setPositiveButtonText("OK");
            	dialog.setPositiveActionListener(okActionListener);
            }
            
        	dialog.show();
        	lastDialog = dialog;
        }catch(Throwable t){}
    }
    
    public synchronized void reportException(String message) {
        reportException(message, null, null);
    }
    
    public synchronized void reportException(Exception e) {
//    	reportException(Log.getStackTraceString(e));
    	reportException(e.getMessage());
    }
    
    public synchronized void reportExceptions(Runnable callback) {
        while (!exceptions.isEmpty()) {
        	ExceptionContainer ec = exceptions.pop();
            String message = "";
            String detailMessage;
            Log.d("ExceptionHandler", (ec.e.getCause() != null ? ec.e.getCause().toString() : ""));
            if(ec.e instanceof ConnectException || ec.e instanceof UnknownHostException){
                message = context.getString(R.string.no_connection);
                detailMessage = StringUtils.defaultString(ec.detailMessage, message);
            }else if(ec.e instanceof SocketTimeoutException){
            	message = context.getString(R.string.connection_timeout);
                detailMessage = StringUtils.defaultString(ec.detailMessage, message);
            }else if(ec.e instanceof ServiceFailException) {
            	message = ec.e.getMessage();
            	detailMessage = ((ServiceFailException)ec.e).getDetailMessage();
            }else{
            	message = ec.getPreferredMessage();
                detailMessage = ec.getDetailMessage();
            }
            reportException(message, detailMessage, callback);
        }
    }
    
    public synchronized void reportExceptions() {
        reportExceptions(null);
    }
    
    public ExceptionContainer popException() {
    	return exceptions.pop();
    }
}
