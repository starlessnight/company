package com.smartrek.utils;

import java.io.PrintStream;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;

import com.smartrek.activities.R;
import com.smartrek.dialogs.ExceptionDialog;
import com.smartrek.exceptions.RouteNotFoundException;

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
			return hasPreferredMessage() ? getPreferredMessage() : getException().getMessage();
		}
		
		@Override
		public String toString() {
			return String.format("%s: %s", e.getClass().toString(), preferredMessage != null ? preferredMessage : e.getMessage());
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
    
    /**
     * Reports an exception immediately.
     * 
     * @param message
     */
    public synchronized void reportException(String message, final Runnable callback) {
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle("An error has occurred");
        dialog.setMessage(message);
        dialog.setButton(context.getResources().getString(R.string.close), new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if(callback != null){
                    callback.run();
                }
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    
    public synchronized void reportException(String message) {
        reportException(message, null);
    }
    
    public synchronized void reportException(Exception e) {
    	// TODO: Is there any better way to handle this?
    	if (e instanceof RouteNotFoundException) {
    		ExceptionDialog dialog = new ExceptionDialog(context, e.getMessage());
    		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.close), new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO: Is this safe to do?
					((Activity) context).finish();
				}
			});
    		dialog.show();
    	}
    	else {
            AlertDialog dialog = new AlertDialog.Builder(context).create();
            dialog.setTitle(e.getClass().toString());
            dialog.setMessage(e.getMessage());
            dialog.setButton(context.getResources().getString(R.string.close), new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialog.show();
    	}
    }
    
    public synchronized void reportExceptions(Runnable callback) {
        while (!exceptions.isEmpty()) {
        	ExceptionContainer ec = exceptions.pop();
            
            reportException(ec.getMessage(), callback);
        }
    }
    
    public synchronized void reportExceptions() {
        reportExceptions(null);
    }
    
    public ExceptionContainer popException() {
    	return exceptions.pop();
    }
}
