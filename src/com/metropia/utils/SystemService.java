package com.metropia.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import com.metropia.dialogs.NotificationDialog2;
import com.metropia.dialogs.NotificationDialog2.ActionListener;

public class SystemService {
    
    public interface Callback {
        
        void onNo();
        
    }
    
    /**
     * Directly copied from http://stackoverflow.com/questions/843675/how-do-i-find-out-if-the-gps-of-an-android-device-is-enabled
     */
    public static void alertNoGPS(final Context context, boolean reverseButtons, final Callback callback) {
//        String leftButtonText = "No";
//        String rightButtonText = "Yes";
//        DialogInterface.OnClickListener leftOnClick = new DialogInterface.OnClickListener() {
//            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
//                dialog.cancel();
//                if(callback != null){
//                    callback.onNo();
//                }
//           }
//        };
//        DialogInterface.OnClickListener rightOnClick = new DialogInterface.OnClickListener() {
//            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
//                context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//            }
//        };
//        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setMessage("Yout GPS seems to be disabled. Do you want to enable it?")
//               .setCancelable(false)
//               .setPositiveButton(reverseButtons?leftButtonText:rightButtonText, 
//                   reverseButtons?leftOnClick:rightOnClick)
//               .setNegativeButton(reverseButtons?rightButtonText:leftButtonText, 
//                   reverseButtons?rightOnClick:leftOnClick);
//        final AlertDialog alert = builder.create();
//        alert.show();
    	NotificationDialog2 dialog = new NotificationDialog2(context, "Your location couldn't be found. Do you like to enable the location service?");
    	dialog.setVerticalOrientation(false);
    	dialog.setTitle("Notification");
    	dialog.setPositiveButtonText("No");
    	dialog.setPositiveActionListener(new ActionListener() {
			@Override
			public void onClick() {
				if(callback != null) {
					callback.onNo();
				}
			}
		});
    	
    	dialog.setNegativeButtonText("Yes");
	    dialog.setNegativeActionListener(new ActionListener() {
			@Override
			public void onClick() {
				context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
	    });
    	dialog.show();
    }
    
    public static void alertNoGPS(final Context context, boolean reverseButtons) {
        alertNoGPS(context, reverseButtons, null);
    }
    
    public static void alertNoGPS(final Context context) {
        alertNoGPS(context, Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB?
            false:true, null);
    }
    
}
