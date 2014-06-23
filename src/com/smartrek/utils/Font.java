package com.smartrek.utils;

import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

public class Font {

    static final String BOLD = "Roboto-Bold.ttf";
    
    static final String LIGHT = "Roboto-Light.ttf";
    
    static final String MEDIUM = "Roboto-Medium.ttf";
    
    static final String REGULAR = "Roboto-Regular.ttf";
    
    public static Typeface getBold(AssetManager assets){
        return getTypeface(assets, BOLD);
    }
    
    public static Typeface getLight(AssetManager assets){
        return getTypeface(assets, LIGHT);
    }
    
    public static Typeface getMedium(AssetManager assets) {
    	return getTypeface(assets, MEDIUM);
    }
    
    public static Typeface getRegular(AssetManager assets) {
    	return getTypeface(assets, REGULAR);
    }
    
    public static void setTypeface(Typeface tf, TextView... views){
        for (TextView v : views) {
            v.setTypeface(tf);
            v.setPaintFlags(v.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }
    }
    
    public static void autoScaleTextSize(TextView tv, float width){
        String s = tv.getText().toString();
        float currentWidth = tv.getPaint().measureText(s);
        while(currentWidth > width) {         
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.getTextSize() - 1.0f); 
            currentWidth = tv.getPaint().measureText(s);
        }
    }
    
    static Typeface getTypeface(AssetManager assets, String path){
        return Typeface.createFromAsset(assets, path);
    }
    
}
