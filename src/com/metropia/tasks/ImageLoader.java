package com.metropia.tasks;

import java.io.IOException;
import java.net.URL;

import com.metropia.activities.PassengerActivity;
import com.metropia.utils.Dimension;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ImageLoader extends AsyncTask<String, Void, Drawable> {
	
	public boolean finished = false;
	ImageItem obj;
	String url;
	View view;
	Runnable cb;
	
	public ImageLoader(ImageItem obj, View view, String url, Runnable cb) {
		super();
		this.obj = obj;
		this.url = url;
		this.view = view;
		this.cb = cb;
	}

	@Override
	protected Drawable doInBackground(String... params) {
		Drawable drawable = null;
		
		try {
			URL url = new URL(this.url);
			Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
			drawable = Dimension.getRoundedShape(bmp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return drawable;
	}
	
	@Override
	protected void onPostExecute(final Drawable drawable) {
		((Activity) view.getContext()).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				obj.setDrawable(drawable);
				((ImageView)view).setImageDrawable(drawable);
				finished = true;
				if (cb!=null) cb.run();
			}
		});
	}
	
	public ImageLoader execute() {
		this.execute("");
		return this;
	}
	
	/*private Bitmap combine(Bitmap bitmap1, Bitmap bitmap2) {
		
		Bitmap result = Bitmap.createBitmap(bitmap2.getWidth(), bitmap2.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas comboImage = new Canvas(result);
		
		comboImage.drawBitmap(bitmap1, 0, 0, null);
		comboImage.drawBitmap(bitmap2, 0, 0, null);
		
		return bitmap2;
		
	}*/
	
	
	
	public interface ImageItem {
		public void setDrawable(Drawable drawable);
	}
	

}
