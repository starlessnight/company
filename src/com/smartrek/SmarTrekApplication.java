package com.smartrek;

import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.smartrek.activities.R;

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
//        Crashlytics.start(this);
        SendTrajectoryService.schedule(this);
        CalendarService.schedule(this);
        TripService.schedule(this);
        ContactListService.schedule(this);
        super.onCreate();
    }
}
