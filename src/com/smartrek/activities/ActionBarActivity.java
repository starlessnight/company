package com.smartrek.activities;

import java.lang.reflect.Field;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.smartrek.utils.Font;

public class ActionBarActivity extends SherlockActivity {

    protected Typeface boldFont;
    
    protected Typeface lightFont;
    
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
        AssetManager assets = getAssets();
        boldFont = Font.getBold(assets);
        lightFont = Font.getLight(assets);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        try {
            final int titleId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? Resources
                    .getSystem().getIdentifier("action_bar_title", "id", "android")
                    : com.actionbarsherlock.R.id.abs__action_bar_title;
            TextView title = (TextView) getWindow().findViewById(titleId);
            Font.setTypeface(boldFont, title);
        } catch (Exception e) {
        }
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
