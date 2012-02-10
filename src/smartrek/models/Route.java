package smartrek.models;

import java.util.ArrayList;

import smartrek.util.RouteNode;

import com.google.android.maps.GeoPoint;

import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;

/****************************************************************************************************
 * 
 * 
 * @author timothyolivas
 *
 ****************************************************************************************************/
public class Route {

	private Coupon cp;
	private ArrayList<Coupon> coupons;
	private String origin;
	private String destination;
	private ArrayList<RouteNode> routeNodes;
	private int rid;
	private int validated;
	private Float time;
	private Time time2 = null;
	private int uid;
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public Route (ArrayList<RouteNode> locs, int rid, Float time) {
		//this.cp = cp;
		this.routeNodes = locs;
		this.rid = rid;
		this.validated = 0;
		this.time = time;
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
		this.time2 = getTimeFromBundle(bundle);
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
	public ArrayList<RouteNode> getPoints() {
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
		if(time2 != null) {
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
		String temp = time2.toString();
		String formattedTime =  temp.substring(0, 4) + "-";
		formattedTime += temp.substring(6, 8) + "-";
		formattedTime += temp.substring(4, 6) + " ";
		formattedTime += temp.substring(9, 11) + ":";
		formattedTime += temp.substring(11, 13) + ":";
		formattedTime += temp.substring(13, 15);
		
		return formattedTime;
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public int getMin() {
		return (int) Math.floor(time);
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public int getSec() {
		return Math.round((60*(time - getMin())));
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
	public void setTime(Time time) {
		this.time2 = time;
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
		bundle.putInt("did", cp.getDID());
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
			bundle.putString("latitude" + i, routenode.getFloatLat());
			bundle.putString("longitude" + i, routenode.getFloatLon());
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
}
