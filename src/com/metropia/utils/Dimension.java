package com.metropia.utils;

import android.util.DisplayMetrics;
import android.util.TypedValue;

public class Dimension {

    public static int dpToPx(int dp, DisplayMetrics dm){
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
        return Float.valueOf(px).intValue();
    }
    
    public static int pxToDp(int px, DisplayMetrics dm){
        return Double.valueOf(Math.ceil(px * dm.density)).intValue();
    }
    
}
