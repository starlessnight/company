package com.smartrek.utils;

import java.io.PrintStream;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.smartrek.dialogs.ExceptionDialog;
import com.smartrek.exceptions.RouteNotFoundException;

public class ExceptionHandlingService {
    private Stack<Exception> exceptions = new Stack<Exception>();
    private Context context;
    
    public ExceptionHandlingService(Context context) {
        this.context = context;
    }
    
    public synchronized boolean hasExceptions() {
        return !exceptions.isEmpty();
    }
    
    /**
     * Reports an exception when {@code reportExceptions()} is called.
     * 
     * @param e
     */
    public synchronized void registerException(Exception e) {
        registerException(e, System.err);
    }
    
    public synchronized void registerException(Exception e, PrintStream err) {
        if (err != null) {
            e.printStackTrace(err);
        }
        exceptions.push(e);
    }
    
    /**
     * Reports an exception immediately.
     * 
     * @param message
     */
    public synchronized void reportException(String message) {
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle("Exception");
        dialog.setMessage(message);
        dialog.setButton("Dismiss", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }
    
    public synchronized void reportException(Exception e) {
    	// TODO: Is there any better way to handle this?
    	if (e instanceof RouteNotFoundException) {
    		ExceptionDialog dialog = new ExceptionDialog(context, e.getMessage());
    		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Dismiss", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO: Is this safe to do?
					((Activity) context).finish();
				}
			});
    		dialog.show();
    	}
    	else {
    		reportException(e.getMessage());
    	}
    }
    
    public synchronized void reportExceptions() {
        while (!exceptions.isEmpty()) {
            Exception e = exceptions.pop();
            
            reportException(e);
        }
    }
}
