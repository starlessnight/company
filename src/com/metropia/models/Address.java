package com.metropia.models;

import android.os.Parcel;
import android.os.Parcelable;

public final class Address implements Parcelable {

    public static final String HOME_STRING = "home";
    
    public static final String WORK_STRING = "work";
    
	private int aid;
	private int uid;
	private String name;
	private String address;
	private double latitude;
	private double longitude;
	private String iconName;
	private double distance = -1;
	
	public static final Parcelable.Creator<Address> CREATOR = new Parcelable.Creator<Address>() {
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        public Address[] newArray(int size) {
            return new Address[size];
        }
    };
	
	public Address() {
	}
	
	public Address(Parcel in) {
		aid = in.readInt();
		uid = in.readInt();
		latitude = in.readDouble();
		longitude = in.readDouble();
		address = in.readString();
		name = in.readString();
		iconName = in.readString();
		distance = in.readDouble();
	}
	
	public Address(int id, int uid, String name, String address, double latitude, double longitude, String iconName) {
		this.aid = id;
		this.uid = uid;
		this.name = name;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.iconName = iconName;
	}
	
	public int getId() {
		return aid;
	}
	
	public void setId(int aid) {
		this.aid = aid;
	}
	
	public int getUid() {
		return uid;
	}
	
	public void setUid(int uid) {
		this.uid = uid;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(aid);
		dest.writeInt(uid);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeString(address);
		dest.writeString(name);
		dest.writeString(iconName);
		dest.writeDouble(distance);
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}
	
}
