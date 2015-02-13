package com.metropia;

import java.util.HashMap;

import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.metropia.activities.ValidationActivity;
import com.metropia.activities.R;

@ReportsCrashes(formKey="dFdwTW1tbERoS1N4RlhNbFBjeHc4dXc6MQ",
                mode = ReportingInteractionMode.DIALOG, // I have decided to use this mode for the beta testing period.
                resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
                resDialogText = R.string.crash_dialog_text,
                resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
                resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
                resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
                resDialogOkToast = R.string.crash_dialog_ok_toast) // optional. displays a Toast message when the user accepts to send a report.
public final class SmarTrekApplication extends Application {
    @Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        //ACRA.init(this);
        CrashlyticsUtils.start(this);
        int interval = 15 * 60 * 1000;
        LocationLibrary.initialiseLibrary(this, interval, 
            interval, false, "com.metropia.activities");
        LocationLibrary.stopAlarmAndListener(this);
        startServices(this);
        super.onCreate();
    }
    
    public static void startServices(Context context){
        SendTrajectoryService.schedule(context);
        CalendarService.schedule(context);
        TripService.schedule(context);
        ContactListService.schedule(context);
        LocationLibrary.startAlarmAndListener(context);
        UserLocationService.schedule(context);
    }
    
    public enum TrackerName {
    	APP_TRACKER, 
    	GLOBAL_TRACKER
    }
    
    private static final String PROPERTY_ID = "UA-51420707-2";
    
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    
    public synchronized Tracker getTracker(TrackerName trackerId) {
    	if (!mTrackers.containsKey(trackerId)) {
    		GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
    	    Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker) : analytics.newTracker(PROPERTY_ID);
    	    t.enableAdvertisingIdCollection(true);
    	    mTrackers.put(trackerId, t);
    	}
    	return mTrackers.get(trackerId);
    }
    
}
