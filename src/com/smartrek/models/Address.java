package com.smartrek.models;

public final class Address {
	
	private int aid;
	private int uid;
	private String name;
	private String address;
	private double latitude;
	private double longitude;
	
	public Address() {
	}
	
	public Address(int id, int uid, String name, String address, double latitude, double longitude) {
		this.aid = id;
		this.uid = uid;
		this.name = name;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
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
}
