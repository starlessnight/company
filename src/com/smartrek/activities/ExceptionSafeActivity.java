package com.smartrek.activities;

import java.io.PrintStream;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

public class ExceptionSafeActivity extends Activity {
    protected Stack<Exception> exceptions = new Stack<Exception>();
    
    protected void registerException(Exception e) {
        registerException(e, System.err);
    }
    
    protected void registerException(Exception e, PrintStream err) {
        if (err != null) {
            e.printStackTrace(err);
        }
        exceptions.push(e);
    }
    
    protected void reportException(String message) {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
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
    
    protected void reportExceptions() {
        while (!exceptions.isEmpty()) {
            Exception e = exceptions.pop();
            
            reportException(e.getMessage());
        }
    }
}
