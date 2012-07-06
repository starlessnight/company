package com.smartrek.models;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.smartrek.mappers.RouteMapper;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.ValidationParameters;

/****************************************************************************************************
 * 
 * 
 * @author timothyolivas
 *
 ****************************************************************************************************/
public final class Route implements Parcelable {

	private String origin;
	private String destination;
	private List<RouteNode> routeNodes = new ArrayList<RouteNode>();
	private int rid;
	private int validated;
	private int duration;
	private Time departureTime = null;
	private int uid;
	private int credits;
	
	
	public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
		public Route createFromParcel(Parcel in) {
			return new Route(in);
		}

		public Route[] newArray(int size) {
			return new Route[size];
		}
	};
	
	public Route() {
	    
	}

	private Route(Parcel in) {
		origin = in.readString();
		destination = in.readString();
		in.readTypedList(routeNodes, RouteNode.CREATOR);
		rid = in.readInt();
		validated = in.readInt();
		duration = in.readInt();
		//departureTime = (Time) in.readValue(Time.class.getClassLoader());
		departureTime = new Time();
		departureTime.parse(in.readString());
		uid = in.readInt();
		credits = in.readInt();
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public Route (ArrayList<RouteNode> locs, int rid, Time departureTime, int duration) {
		//this.cp = cp;
		this.routeNodes = locs;
		this.rid = rid;
		this.validated = 0;
		this.departureTime = departureTime;
		this.duration = duration;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public Route (Bundle bundle) {
		this.origin = bundle.getString("origin");
		this.destination = bundle.getString("destination");
		this.uid = bundle.getInt("uid");
		this.routeNodes = getRouteNodesFromBundle(bundle);
		this.rid = bundle.getInt("rid");
		this.validated = bundle.getInt("validated");
		this.departureTime = getTimeFromBundle(bundle);
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public void setOD(String loc1, String loc2) {
		this.origin = loc1;
		this.destination = loc2;
	}
	
	public String getOrigin(){
		return origin;
	}
	
	public void setOrigin(String origin) {
	    this.origin = origin;
	}
	
	public String getDestination(){
		return destination;
	}
	
	public void setDestination(String destination) {
	    this.destination = destination;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public void setUserId(int uid) {
		this.uid = uid;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public int getUserId() {
		return uid;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public List<RouteNode> getNodes() {
		return routeNodes;
	}
	
	public void setNodes(JSONArray nodes) throws JSONException {
	    routeNodes = RouteMapper.buildRouteNodes(nodes);
	}
	
	public int getId(){
		return rid;
	}
	
	public void setId(int rid) {
	    this.rid = rid;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	@Override
	public String toString(){
		String str = "";
		str += "Route ID = " + rid + "\n";
		str += "Validated = " + validated + "\n";
		str += "Node Locations: \n";
		int i = 0;
		while(i < routeNodes.size()){
			str += "Node " + (i + 1) + "\n";
			GeoPoint loc = routeNodes.get(i).getPoint();
			str += "	Lattitude = " + loc.getLatitudeE6() + "\n";
			str += "	Longitude = " + loc.getLongitudeE6() + "\n";
			i++;
		}
		str += "\n";
		if(departureTime != null) {
			str += "Time = " + timeToString() + "\n";
		}
		return str;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public String timeToString(){
		String temp = departureTime.toString();
		String formattedTime =  temp.substring(0, 4) + "-";
		formattedTime += temp.substring(6, 8) + "-";
		formattedTime += temp.substring(4, 6) + " ";
		formattedTime += temp.substring(9, 11) + ":";
		formattedTime += temp.substring(11, 13) + ":";
		formattedTime += temp.substring(13, 15);
		
		return formattedTime;
	}
	
	public Time getDepartureTime() {
		return departureTime;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public Time getArrivalTime() {
		long timestamp = departureTime.toMillis(false) + (duration * 1000);
		
		Time arrivalTime = new Time();
		arrivalTime.set(timestamp);
		
		return arrivalTime;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public int getMin() {
		return (int) Math.floor(duration/60);
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public int getSec() {
		return Math.round(duration%60);
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public String getTimeString(){
		return getMin() + " Minutes and " + getSec() + " Seconds";
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public void setDepartureTime(Time time) {
		this.departureTime = time;
	}
	
	public int getCredits() {
		return credits;
	}
	
	public void setCredits(int credits) {
		this.credits = credits;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public boolean isNearOrigin(float lat, float lng) {
		return false;
	}
	
	/**
	 * Determines whether a geocoordinate is close enough to the destination of the route
	 * 
	 * @param lat
	 * @param lng
	 * @return
	 */
	public boolean hasArrivedAtDestination(float lat, float lng) {
		RouteNode lastNode = routeNodes.get(routeNodes.size() - 1);
		ValidationParameters params = ValidationParameters.getInstance();
		
		return lastNode.distanceTo(lat, lng) <= params.getArrivalDistanceThreshold();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/	
	public void putOntoBundle(Bundle bundle) {
		bundle.putString("origin", origin);
		bundle.putString("destination", destination);
		bundle.putString("time", timeToString());
		bundle.putInt("uid", uid);
		bundle.putFloat("rid", rid);
		bundle.putInt("validated", validated);
		
		bundle.putString("time", timeToString());
//		bundle.putInt("valid_month", valid_date.getMonth());
//		bundle.putInt("valid_year", valid_date.getYear());
		
		putRouteNodeListOnToBundle(bundle);
		
	}
	
	public void putRouteNodeListOnToBundle(Bundle bundle) {
		bundle.putInt("Node_ListSize", routeNodes.size());
		for (int i = 0; i < routeNodes.size(); i++) {
			RouteNode routenode = routeNodes.get(i);
			bundle.putFloat("latitude" + i, routenode.getLatitude());
			bundle.putFloat("longitude" + i, routenode.getLongitude());
		}
	}
	
	public ArrayList<RouteNode> getRouteNodesFromBundle(Bundle bundle){
		ArrayList<RouteNode> nodes = new ArrayList<RouteNode>();
		int size = bundle.getInt("Node_ListSize");
		int routenum = bundle.getInt("routenum");
		for (int i = 0; i < size; i++) {
			RouteNode routenode = new RouteNode(bundle,routenum,i);
			nodes.add(routenode);
		}
		return nodes;	
	}
	
	public Time getTimeFromBundle(Bundle bundle) {
		Time t = new Time();
		String timeString = bundle.getString("time");
		
		Log.d("Route","Pulled Time String from bundle");
		Log.d("Route",timeString);
		
		int year = Integer.parseInt(timeString.substring(0, 4));
		int month = Integer.parseInt(timeString.substring(5, 7));
		int day = Integer.parseInt(timeString.substring(8, 10));
		int hour = Integer.parseInt(timeString.substring(11, 13));
		int minute = Integer.parseInt(timeString.substring(14, 16));
		int second = Integer.parseInt(timeString.substring(17, 19));
		
		Log.d("Route","" + year + " " + month + " " + day + " " + hour + " " + minute + " " +  second);
		
		t.set(second, minute, hour, day, month, year);
		
		return t;
		
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(origin);
		dest.writeString(destination);
		dest.writeTypedList(routeNodes);
		dest.writeInt(rid);
		dest.writeInt(validated);
		dest.writeInt(duration);
		//dest.writeValue(departureTime);
		dest.writeString(departureTime.format2445());
		dest.writeInt(uid);
		dest.writeInt(credits);
	}

//	/**
//	 * The following code does not work because JSONArray puts surrounding
//	 * double quotes on each element of its own.
//	 * 
//	 * new JSONObject(route.getPoints()).toString();
//	 * 
//	 * The result would be
//	 * 
//	 * ["{"key":"value"}", "{"key":"value"}", ... ]
//	 */
//	@Override
//	public String toJSON() throws JSONException {
//		StringBuffer buf = new StringBuffer();
//		buf.append("[");
//		
//		
//		
//		buf.append("]");
//		
//		return new String(buf);
//	}
}
