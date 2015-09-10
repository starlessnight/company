package com.metropia.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
    
    public static Drawable getRoundedShape(Drawable drawable) {
    	Bitmap scaleBitmapImage = ((BitmapDrawable)drawable).getBitmap();
		return getRoundedShape(scaleBitmapImage);
	}
    public static Drawable getRoundedShape(Bitmap scaleBitmapImage) {
		if (scaleBitmapImage==null) return null;
		int targetWidth = 200;
		int targetHeight = 200;
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(((float) targetWidth - 1) / 2,
		((float) targetHeight - 1) / 2,
		(Math.min(((float) targetWidth), ((float) targetHeight)) / 2), Path.Direction.CCW);

		canvas.clipPath(path);
		Bitmap sourceBitmap = scaleBitmapImage;
		canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new Rect(0, 0, targetWidth, targetHeight), null);
		return new BitmapDrawable(targetBitmap);
    }
}
