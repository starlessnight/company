package com.smartrek.activities;

import java.lang.reflect.Field;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.Font;

public class ActionBarActivity extends SherlockFragmentActivity {

    private static final int titleWidth = 120;
    
    private static final int titleTopOffset = -3;

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
        TextView title = getActionBarTitle();
        Font.setTypeface(boldFont, title);
        resizeActionBarTitle();
        repositionActionBarTitle();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView title = getActionBarTitle();
                repositionActionBarTitle();
                Font.setTypeface(boldFont, title);
                resizeActionBarTitle();
                title.requestLayout();
            }
        }, 500);
    }
    
    void resizeActionBarTitle(){
        float offset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, titleWidth, getResources().getDisplayMetrics());
        int width = getWindowManager().getDefaultDisplay().getWidth();
        Font.autoScaleTextSize(getActionBarTitle(), width - offset);
    }
    
    void repositionActionBarTitle(){
        TextView title = getActionBarTitle();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) title.getLayoutParams();
        layoutParams.topMargin = Dimension.dpToPx(titleTopOffset, getResources().getDisplayMetrics());
    }
    
    TextView getActionBarTitle(){
        int titleId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ? Resources
                .getSystem().getIdentifier("action_bar_title", "id", "android")
                : com.actionbarsherlock.R.id.abs__action_bar_title;
        return (TextView) getWindow().findViewById(titleId);
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
