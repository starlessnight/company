package com.smartrek.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.smartrek.activities.R;
import com.smartrek.dialogs.NotificationDialog2;
import com.smartrek.models.User;
import com.smartrek.requests.IssueReportRequest;

public class ExceptionHandlingService {
	
	public static final String LOG_TAG = "ExceptionHandlingService";
	
	/**
	 * Contains an exception instance and other metadata
	 */
	public static class ExceptionContainer {
		private Exception e;
		private String preferredMessage;
		private String url;
		private String responseStatus;
		private String responseContent;
		
		public ExceptionContainer(Exception e) {
			this.e = e;
			processDetailInfo(e);
		}
		
		public ExceptionContainer(Exception e, String preferredMessage) {
			this.e = e;
			processDetailInfo(e);
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
		
		private void processDetailInfo(Exception e) {
			this.url = e.getMessage();
			Throwable cause = e.getCause();
			if(cause instanceof HttpResponseException) {
				HttpResponseException httpException = (HttpResponseException)cause;
				this.responseStatus = String.valueOf(httpException.getStatusCode());
				this.responseContent = httpException.getMessage();
			}
			else if(cause instanceof IOException) {
				Pattern pattern = Pattern.compile("HTTP\\s(\\d{3}):\\s(.*)");
				String message = cause.getMessage();
				Matcher matcher = pattern.matcher(message);
				if(matcher.find()) {
					this.responseStatus = matcher.group(1);
					this.responseContent = matcher.group(2);
				}
			}
		}

		public String getUrl() {
			return url;
		}

		public String getResponseStatus() {
			return responseStatus;
		}

		public String getResponseContent() {
			return responseContent;
		}
		
		public boolean hasStatusCode() {
			return StringUtils.isNotBlank(responseStatus);
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
    public synchronized void reportException(ExceptionContainer ec, final String message, final Runnable callback) {
        try{
            if(lastDialog != null && lastDialog.isShowing()){
                lastDialog.dismiss();
                lastDialog = null;
            }
        	NotificationDialog2 dialog = new NotificationDialog2(context, "An error has occurred.");
        	dialog.setVerticalOrientation(false);
        	dialog.setPositiveButtonText("Cancel");
            dialog.setPositiveActionListener(new NotificationDialog2.ActionListener() {
                @Override
                public void onClick() {
                    if(callback != null) {
                        callback.run();
                    }
                }
            });
//        	dialog.setPositiveButtonText("More");
//    	    dialog.setPositiveActionListener(new ActionListener() {
//                @Override
//                public void onClick() {
//                    Intent feedback = new Intent(context, FeedbackActivity.class);
//                    feedback.putExtra(FeedbackActivity.CATEGORY, "api");
//                    feedback.putExtra(FeedbackActivity.MESSAGE, message);
//                    context.startActivity(feedback);
//                    if(callback != null) {
//                        callback.run();
//                    }
//                }
//            });
            
        	dialog.show();
        	lastDialog = dialog;
        	
        	sendIssue(ec, message);
        	
//        	Crashlytics.logException(new Exception(message));
        }catch(Throwable t){}
    }
    
    public synchronized void reportException(String message) {
        reportException(null, message, null);
    }
    
    public synchronized void reportException(Exception e) {
    	reportException(new ExceptionContainer(e), Log.getStackTraceString(e), null);
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
            reportException(ec, message, callback);
        }
    }
    
    public synchronized void reportExceptions() {
        reportExceptions(null);
    }
    
    public ExceptionContainer popException() {
    	return exceptions.pop();
    }
    
    private void sendIssue(final ExceptionContainer ec, final String msg) {
    	if(ec != null) {
	    	Misc.parallelExecute(new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					IssueReportRequest request = new IssueReportRequest(User.getCurrentUser(context), 
							msg, ec.getUrl(), ec.getResponseStatus(), ec.getResponseContent());
					Log.d("ExceptionReport", request.toString());
					request.execute(context);
					return null;
				}
	    	});
    	}
    }
}
