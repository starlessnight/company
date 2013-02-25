package com.smartrek.activities;

import java.lang.reflect.Field;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import com.actionbarsherlock.app.SherlockActivity;

public class ActionBarActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                ViewConfiguration config = ViewConfiguration.get(this);
                Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
                if(menuKeyField != null) {
                    menuKeyField.setAccessible(true);
                    menuKeyField.setBoolean(config, false);
                }
            } catch (Exception ex) {
                // Ignore
            }
        }
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            if (event.getAction() == KeyEvent.ACTION_UP &&
                keyCode == KeyEvent.KEYCODE_MENU) {
                openOptionsMenu();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
    
}
