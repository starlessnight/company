package com.smartrek.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;

public class Font {

    static final String BOLD = "Helvetica-Bold.ttf";
    
    static final String LIGHT = "Helvetica-Light.ttf";
    
    public static Typeface getBold(AssetManager assets){
        return getTypeface(assets, BOLD);
    }
    
    public static Typeface getLight(AssetManager assets){
        return getTypeface(assets, LIGHT);
    }
    
    static Typeface getTypeface(AssetManager assets, String path){
        return Typeface.createFromAsset(assets, path);
    }
    
}
