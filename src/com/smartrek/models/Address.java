package com.smartrek.models;

public final class Address {
	
	private int aid;
	private int uid;
	private String name;
	private String address;
	
	public Address() {
		
	}
	
	public Address(int id, int uid, String name, String address) {
		this.aid = id;
		this.uid = uid;
		this.name = name;
		this.address = address;
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
	
	

}
