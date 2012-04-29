package smartrek.models;

import java.util.ArrayList;
import java.util.List;

import smartrek.util.RouteNode;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;
import android.util.Log;

import com.google.android.maps.GeoPoint;

/****************************************************************************************************
 * 
 * 
 * @author timothyolivas
 *
 ****************************************************************************************************/
public final class Route implements Parcelable {

	private Coupon cp;
	private ArrayList<Coupon> coupons;
	private String origin;
	private String destination;
	private List<RouteNode> routeNodes = new ArrayList<RouteNode>();
	private int rid;
	private int validated;
	private int duration;
	private Time departureTime = null;
	private int uid;
	
	
	public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
		public Route createFromParcel(Parcel in) {
			return new Route(in);
		}

		public Route[] newArray(int size) {
			return new Route[size];
		}
	};

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
		this.cp = new Coupon(bundle);
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
	public Coupon getDiscount(){
		return cp;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public void setCoupon(Coupon coupon){
		this.cp = coupon;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public void setAllCoupons(ArrayList<Coupon> coupons) {
		this.coupons = coupons;
		this.cp = coupons.get(0);
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public ArrayList<Coupon> getAllCoupons() {
		return coupons;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public void setOD(String loc1, String loc2) {
		this.origin = loc1;
		this.destination = loc2;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public String getOrigin(){
		return origin;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public String getDestination(){
		return destination;
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
	public List<RouteNode> getPoints() {
		return routeNodes;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public int getRID(){
		return rid;
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
		if(cp != null)
			str += cp.toString();
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
		bundle.putInt("did", cp.getDid());
		bundle.putInt("validated", validated);
		
		bundle.putString("time", timeToString());
//		bundle.putInt("valid_month", valid_date.getMonth());
//		bundle.putInt("valid_year", valid_date.getYear());
		
		cp.putOntoBundle(bundle);
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
	}
}
