package com.metropia.ui;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import android.content.Context;
import android.widget.ImageView;

public class SkobblerImageView extends ImageView {
	
	private int resourceId;
	private int sizeRatio;
	private double lat;
	private double lon;
	private String desc;
	
	public SkobblerImageView(Context context, int resourceId, int sizeRatio) {
		super(context);
		this.resourceId = resourceId;
		this.sizeRatio = sizeRatio;
	}
	
	public void setLat(double lat) {
		this.lat = lat;
	}
	
	public void setLon(double lon) {
		this.lon = lon;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(sizeRatio);
		if(resourceId > 0) {
			hashCodeBuilder.append(resourceId);
		}
		if(lat > 0) {
			hashCodeBuilder.append(lat);
		}
		if(lon > 0) {
			hashCodeBuilder.append(lon);
		}
		if(StringUtils.isNotBlank(desc)) {
			hashCodeBuilder.append(desc);
		}
		return hashCodeBuilder.toHashCode();
	}

}
