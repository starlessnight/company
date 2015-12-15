package com.metropia.models;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.metropia.activities.FavoriteListActivity;
import com.metropia.activities.R;
import com.metropia.activities.LandingActivity2.BalloonModel;
import com.metropia.tasks.ICallback;
import com.metropia.tasks.ImageLoader;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.Misc;

public class PoiOverlayInfo extends BalloonModel implements Parcelable {
	
	public int marker = R.drawable.transparent_poi;
	public String markerURL;
	public Drawable drawable;
	
	public int markerWithShadow = R.drawable.transparent_poi;
	
	public String iconName;
	
	public int uniqueId;
	
	public static final Parcelable.Creator<PoiOverlayInfo> CREATOR = new Parcelable.Creator<PoiOverlayInfo>() {
        public PoiOverlayInfo createFromParcel(Parcel in) {
            return new PoiOverlayInfo(in);
        }

        public PoiOverlayInfo[] newArray(int size) {
            return new PoiOverlayInfo[size];
        }
    };
    
    public PoiOverlayInfo() {}
	
	public PoiOverlayInfo(Parcel in) {
		id = in.readInt();
        lat = in.readDouble();
        lon = in.readDouble();
        address = in.readString();
        label = in.readString();
        marker = in.readInt();
        markerWithShadow = in.readInt();
        iconName = in.readString();
        geopoint = new GeoPoint(lat, lon);
        uniqueId = in.readInt();
        potypeid = in.readInt();
        markerURL = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeDouble(lat);
		dest.writeDouble(lon);
		dest.writeString(address);
		dest.writeString(label);
		dest.writeInt(marker);
		dest.writeInt(markerWithShadow);
		dest.writeString(iconName);
		dest.writeInt(uniqueId);
		dest.writeInt(potypeid);
		dest.writeString(markerURL);
	}
	
	public static PoiOverlayInfo fromAddress(final Context ctx, final com.metropia.models.Address address) {
		final PoiOverlayInfo poiInfo = new PoiOverlayInfo();
		poiInfo.id = address.getId();
		poiInfo.label = address.getName();
		poiInfo.address = address.getAddress();
		poiInfo.lat = address.getLatitude();
		poiInfo.lon = address.getLongitude();
		poiInfo.geopoint = new GeoPoint(address.getLatitude(), address.getLongitude());
		poiInfo.iconName = address.getIconName();
		poiInfo.markerURL = address.getIconURL();
		poiInfo.potypeid = address.getPOITYPEID();
		FavoriteIcon icon = FavoriteIcon.fromName(address.getIconName(), null);
		
		if (icon!=null) {
			poiInfo.marker = icon.getResourceId(ctx);
			poiInfo.markerWithShadow = icon.getShadowResourceId(ctx);
		}
		return poiInfo;
	}
	
	public static PoiOverlayInfo fromLocation(com.metropia.requests.WhereToGoRequest.Location location) {
		PoiOverlayInfo poiInfo = new PoiOverlayInfo();
		poiInfo.label = "";
		poiInfo.address = location.addr;
		poiInfo.lat = location.lat;
		poiInfo.lon = location.lon;
		poiInfo.geopoint = new GeoPoint(location.lat, location.lon);
		poiInfo.marker = R.drawable.bulb_poi;
		poiInfo.markerWithShadow = R.drawable.bulb_poi_with_shadow;
		return poiInfo;
	}
	
	public static PoiOverlayInfo fromBalloonModel(BalloonModel model) {
		PoiOverlayInfo poiInfo = new PoiOverlayInfo();
		poiInfo.id = model.id;
		poiInfo.label = model.label;
		poiInfo.address = model.address;
		poiInfo.lat = model.lat;
		poiInfo.lon = model.lon;
		poiInfo.geopoint = model.geopoint;
		poiInfo.marker = R.drawable.poi_pin;
		poiInfo.markerWithShadow = R.drawable.poi_pin_with_shadow;
		return poiInfo;
	}
	
	public static PoiOverlayInfo fromCurrentLocation(GeoPoint currentLoc) {
		PoiOverlayInfo poiInfo = new PoiOverlayInfo();
		poiInfo.lat = currentLoc.getLatitude();
		poiInfo.lon = currentLoc.getLongitude();
		poiInfo.geopoint = new GeoPoint(currentLoc.getLatitude(), currentLoc.getLongitude(), currentLoc.getHeading());
		poiInfo.marker = R.drawable.landing_page_current_location;
		poiInfo.markerWithShadow = R.drawable.landing_page_current_location;
		return poiInfo;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof PoiOverlayInfo) {
			PoiOverlayInfo that = (PoiOverlayInfo) other;
			return new EqualsBuilder().append(that.lat + "", this.lat + "").append(that.lon + "", that.lon + "").append(that.marker + "", this.marker + "").isEquals();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.lat + "").append(this.lon + "").append(this.marker + "").toHashCode();
	}

}
