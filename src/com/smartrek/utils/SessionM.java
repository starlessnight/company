package com.smartrek.utils;

import android.app.Activity;



public class SessionM {

    public static final boolean ENABLED = true;
    
    public static void logAction(String action){
        if(ENABLED){
            com.sessionm.api.SessionM.getInstance().logAction(action);
        }
    }
    
    public static void onActivityPause(Activity activity){
        if(ENABLED){
            com.sessionm.api.SessionM.getInstance().onActivityPause(activity);
        }
    }
    
    public static void onActivityResume(Activity activity){
        if(ENABLED){
            com.sessionm.api.SessionM.getInstance().onActivityResume(activity);
        }
    }
    
    public static void onActivityStart(Activity activity){
        if(ENABLED){
            com.sessionm.api.SessionM.getInstance().onActivityStart(activity);
        }
    }
    
    public static void onActivityStop(Activity activity){
        if(ENABLED){
            com.sessionm.api.SessionM.getInstance().onActivityStop(activity);
        }
    }
    
}
