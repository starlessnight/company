package com.metropia.models;

import org.apache.commons.lang3.StringUtils;

import com.metropia.activities.R;
import com.metropia.tasks.ImageLoader;
import com.metropia.tasks.ImageLoader.ImageItem;
import com.metropia.utils.Dimension;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Passenger implements ImageItem {
	public String userName;
	public String photoUrl;
	public Drawable drawable = null;
	
	public Passenger(String userName, String photoUrl) {
		this.userName = userName;
		this.photoUrl = photoUrl;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Passenger && userName.equals(((Passenger)obj).userName);
	}

	@Override
	public void setDrawable(Drawable drawable) {
		this.drawable = drawable;
	}
}
