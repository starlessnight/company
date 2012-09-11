package com.smartrek;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "dFdwTW1tbERoS1N4RlhNbFBjeHc4dXc6MQ") 
public final class SmarTrekApplication extends Application {
    @Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        super.onCreate();
    }
}
