package com.smartrek.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

public class Misc {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void disableHardwareAcceleration(View v){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
    
}
