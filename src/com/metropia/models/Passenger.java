package com.metropia.models;

import com.metropia.tasks.ImageLoader.ImageItem;
import android.graphics.drawable.Drawable;

public class Passenger implements ImageItem {
	public int id = -1;
	public String userName = "";
	public String photoUrl;
	public Drawable drawable = null;
	public String onBoardVoice = "";
	
	public Passenger(int id, String userName, String photoUrl) {
		this.id = id;
		this.userName = userName;
		this.photoUrl = photoUrl;
	}
	
	public Passenger(int id, String onBoardVoice) {
		this.id = id;
		this.onBoardVoice = onBoardVoice;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Passenger && id==((Passenger)obj).id;
	}

	@Override
	public void setDrawable(Drawable drawable) {
		this.drawable = drawable;
	}
}
