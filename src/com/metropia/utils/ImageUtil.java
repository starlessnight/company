package com.metropia.utils;

import com.metropia.activities.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageUtil {
	
	public static Drawable getRoundedShape(Context context, int color, boolean halo) {
		Bitmap b = Bitmap.createBitmap(200, 200, Config.ARGB_8888);
		b.eraseColor(context.getResources().getColor(color));
		return addShadow(context, getRoundedShape(b), halo);
	}
	
	public static Drawable getRoundedShape(Drawable drawable) {
    	Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
		return getRoundedShape(bitmap);
	}
    public static Drawable getRoundedShape(Bitmap scaleBitmapImage) {
		if (scaleBitmapImage==null) return null;
		int targetWidth = 200;
		int targetHeight = 200;
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,Bitmap.Config.ARGB_8888);
		
		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(((float) targetWidth - 1) / 2, ((float) targetHeight - 1) / 2, (Math.min(((float) targetWidth), ((float) targetHeight)) / 2), Path.Direction.CCW);

		canvas.clipPath(path);
		Bitmap sourceBitmap = scaleBitmapImage;
		canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new Rect(0, 0, targetWidth, targetHeight), null);
		return new BitmapDrawable(targetBitmap);
    }
    
    
    public static Drawable addShadow(Context context, Drawable drawable, boolean halo) {
    	int padding = Dimension.dpToPx(5, context.getResources().getDisplayMetrics());
    	
    	Bitmap origin = ((BitmapDrawable)drawable).getBitmap();
    	Bitmap shadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.circle_shadow);
    	Bitmap result = Bitmap.createBitmap(origin.getWidth() + padding, origin.getHeight() + padding, Bitmap.Config.ARGB_8888);
    	
    	Canvas canvas = new Canvas(result);
    	canvas.drawBitmap(shadow, new Rect(0, 0, shadow.getWidth(), shadow.getHeight()), new Rect(0, 0, 200+padding, 200+padding), null);
    	canvas.drawBitmap(origin, 0, 0, null);
    	
    	if (halo) {
    		Paint p = new Paint();  
    		p.setColor(Color.parseColor("#FFCC22")); 
    		p.setStyle(Paint.Style.STROKE);
    		p.setAntiAlias(true);
    		p.setStrokeWidth(11);
    		canvas.drawCircle(origin.getWidth()/2, origin.getWidth()/2, origin.getWidth()/2-5, p);
    	}
    	
		return new BitmapDrawable(result);
    }
}
