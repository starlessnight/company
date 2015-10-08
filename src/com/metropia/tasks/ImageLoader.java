package com.metropia.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class ImageLoader extends AsyncTask<Boolean, Void, Drawable> {
	
	public boolean finished = false;
	Context context;
	String url;
	ICallback cb;
	
	public ImageLoader(Context context, String url, ICallback cb) {
		super();
		this.context= context;
		this.url = url;
		this.cb = cb;
	}

	@Override
	protected Drawable doInBackground(Boolean... params) {
		Drawable drawable = getCachedImage(context, this.url);
		if (drawable!=null) return drawable;
		
		try {
			URL url = new URL(this.url);
			Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
			if (params[0]) cacheImage(context, this.url, bmp);
			drawable = new BitmapDrawable(bmp);
		} catch (IOException e) {
			Log.e("fetch image fail", e.toString());
		}
		
		return drawable;
	}
	
	@Override
	protected void onPostExecute(final Drawable drawable) {
		
		finished = true;
		if (cb!=null) cb.run(drawable);
	}
	
	public ImageLoader execute(Boolean cache) {
		super.execute(cache);
		return this;
	}
	
	
	
	public interface ImageItem {
		public void setDrawable(Drawable drawable);
	}
	
	public static Drawable getCachedImage(Context context, String url) {
		String fileName = null;
		try {
			fileName = url.substring( url.lastIndexOf('/')+1, url.length() );
		} catch(Exception e) {return null;}
		
		File file = new File(context.getExternalFilesDir(null), "imageCache/"+fileName);
		if (file.exists()) {
			try {
				Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
				return new BitmapDrawable(bitmap);
			} catch (FileNotFoundException e) {}
		}
		return null;
	}
	public static void cacheImage(Context context, String url, Bitmap bitmap) {
		String fileName = null;
		try {
			fileName = url.substring( url.lastIndexOf('/')+1, url.length() );
		} catch(Exception e) {return;}
		
		File dir = new File(context.getExternalFilesDir(null), "imageCache");
		if (!dir.exists()) dir.mkdir();
		FileOutputStream out = null;
		try {
		    out = new FileOutputStream(dir+"/"+fileName);
		    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		} catch (Exception e) {
			Log.e("save image to file error", e.toString());
		} finally {
		    try {
		        if (out != null) {
		            out.close();
		        }
		    } catch (IOException e) {}
		}
	}

}
