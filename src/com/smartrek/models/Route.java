package com.smartrek.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;
import android.util.Log;

import com.smartrek.activities.DebugOptionsActivity.NavigationLink;
import com.smartrek.requests.Request;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.NaiveNNS;
import com.smartrek.utils.RouteLink;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.ValidationParameters;

/**
 * @author timothyolivas
 * @author Sumin Byeon
 */
public final class Route implements Parcelable {

	private String origin;
	private String destination;
	private List<RouteNode> routeNodes = new ArrayList<RouteNode>();
	private int rid;
	private int validated;
	private int duration;
	private long departureTime;
	private int uid;
	private int credits;
	private boolean fake;
	private int seq;
	private Double length;
	private NavigationLink link;
	
	
	public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
		public Route createFromParcel(Parcel in) {
			return new Route(in);
		}

		public Route[] newArray(int size) {
			return new Route[size];
		}
	};
	
	public static Route parse(JSONObject routeObject, long departureTime) throws JSONException, IOException {
	    return parse(routeObject, departureTime, false);
	}
	
	public static Route parse(JSONObject routeObject, long departureTime, boolean forceOld) throws JSONException, IOException {
	    boolean newAPI = !forceOld && Request.NEW_API; 
	    JSONArray rts;
	    String attr = "ROUTE";
	    if(!routeObject.has(attr) || newAPI){
	        rts = new JSONArray(routeObject.getString("route"));
	    }else{
            rts = routeObject.getJSONArray(attr);
	    }
	    
	    ArrayList<RouteNode> routeNodes = new ArrayList<RouteNode>();
        for (int i = 0; i < rts.length(); i++) {
            JSONObject ro = (JSONObject) rts.get(i);
            
            RouteNode node = new RouteNode(
                ro.getDouble(newAPI?"lat":"LATITUDE"),
                ro.getDouble(newAPI?"lon":"LONGITUDE"), 
                0, 
                ro.getInt(newAPI?"node":"NODEID")
            );
            
            if (ro.has("FLAG")) {
            	node.setFlag(ro.getInt("FLAG"));
            }
            if (ro.has("MESSAGE")) {
            	node.setMessage(ro.getString("MESSAGE"));
            }
            if (ro.has("DIRECTION")) {
                node.setDirection(ro.getString("DIRECTION"));
            }
            if (ro.has("DISTANCE")) {
            	// conversion from mile to meter
            	node.setDistance(ro.getDouble("DISTANCE") * 1609.34);
            }
            if (ro.has("ROADNAME")) {
            	node.setRoadName(ro.getString("ROADNAME"));
            }
            
            routeNodes.add(node);
        }
        
        buildRouteNodeReferenceChain(routeNodes);
        
        // Route ID
        int rid = 0;
        String ridAttr = "RID";
        if(!newAPI && routeObject.has(ridAttr)){
            rid = routeObject.getInt(ridAttr);
        }
        
        // Web service returns the estimated travel time in minutes, but we
        // internally store it as seconds.
        //
        // Server returns ESTIMATED_TRAVEL_TIME in some APIs, and returns END_TIME in other APIs. FUCK ME...
        double ett = 0;
        if(newAPI){
            ett = routeObject.getDouble("estimated_travel_time");
        }else{
            if (routeObject.has("ESTIMATED_TRAVEL_TIME")) {
            	ett = routeObject.getDouble("ESTIMATED_TRAVEL_TIME");
            }
            else if (routeObject.has("END_TIME")) {
            	ett = routeObject.getDouble("END_TIME");
            }
        }
        
        Route route = new Route(routeNodes, rid, departureTime, (int)(ett * 60));
        route.setCredits(routeObject.optInt(newAPI?"credit":"CREDITS"));
        
        String distanceAttr = newAPI?"distance":"DISTANCE"; 
        if (routeObject.has(distanceAttr)) {
            // conversion from mile to meter
            route.length = routeObject.getDouble(distanceAttr) * 1609.34;
        }
        
        return route;
	}
	
	public static Route parse(JSONArray navInfo, long departureTime, int duration)
            throws JSONException, IOException {
        ArrayList<RouteNode> routeNodes = new ArrayList<RouteNode>();
        for (int i = 0; i < navInfo.length(); i++) {
            JSONObject ro = (JSONObject) navInfo.get(i);
            
            if(ro.has("node")){
                RouteNode node = new RouteNode(
                    ro.getDouble("lon"),
                    ro.getDouble("lat"), 
                    0, 
                    ro.getInt("node")
                );
                
                if (ro.has("remind")) {
                    node.setFlag(ro.getInt("remind"));
                }
                if (ro.has("msg")) {
                    node.setMessage(ro.getString("msg"));
                }
                if (ro.has("direction")) {
                    node.setDirection(ro.getString("direction"));
                }
                if (ro.has("distance")) {
                    node.setDistance(ro.getLong("distance") * 0.3048);
                }
                if (ro.has("road")) {
                    node.setRoadName(ro.getString("road"));
                }
                routeNodes.add(node);
            }
        }
        
        buildRouteNodeReferenceChain(routeNodes);
        
        Route route = new Route(routeNodes, 0, departureTime, duration);
        
        return route;
    }
	
	public Route() {
	    
	}

	private Route(Parcel in) {
		origin = in.readString();
		destination = in.readString();
		in.readTypedList(routeNodes, RouteNode.CREATOR);
		rid = in.readInt();
		validated = in.readInt();
		duration = in.readInt();
		departureTime = in.readLong();
		uid = in.readInt();
		credits = in.readInt();
		fake = (Boolean) in.readValue(null);
		seq = in.readInt();
		int lId = in.readInt();
		String lUrl = in.readString();
		if(lUrl != null){
		    NavigationLink l = new NavigationLink();
		    l.id = lId;
		    l.url = lUrl;
		    link = l;
		}
		
		buildRouteNodeReferenceChain(routeNodes);
	}
	
	public Route (ArrayList<RouteNode> locs, int rid, long departureTime, int duration) {
		this.routeNodes = locs;
		this.rid = rid;
		this.validated = 0;
		this.departureTime = departureTime;
		this.duration = duration;
	}
	
	public Route (Bundle bundle) {
		this.origin = bundle.getString("origin");
		this.destination = bundle.getString("destination");
		this.uid = bundle.getInt("uid");
		this.routeNodes = getRouteNodesFromBundle(bundle);
		this.rid = bundle.getInt("rid");
		this.validated = bundle.getInt("validated");
		this.departureTime = bundle.getLong("departureTime");
	}
	
	public void setAddresses(String origin, String destination) {
		this.origin = origin;
		this.destination = destination;
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
	
	public void setUserId(int uid) {
		this.uid = uid;
	}
	
	public int getUserId() {
		return uid;
	}
	
	public List<RouteNode> getNodes() {
		return routeNodes;
	}
	
	public RouteNode getFirstNode() {
		if (routeNodes != null && routeNodes.size() > 0) {
			return routeNodes.get(0);
		}
		return null;
	}
	
	public RouteNode getLastNode() {
		if (routeNodes != null && routeNodes.size() > 0) {
			return routeNodes.get(routeNodes.size() - 1);
		}
		return null;
	}
	
	public RouteNode getNearestNode(double latitude, double longitude) {
		return NaiveNNS.findClosestNode(routeNodes, latitude, longitude);
	}
	
	public RouteLink getNearestLink(double latitude, double longitude) {
	    RouteNode node = getNearestNode(latitude, longitude);
	    
	    RouteNode prevNode = node.getPrevNode();
        RouteNode nextNode = node.getNextNode();
        
        if (prevNode != null && nextNode != null) {
            RouteLink prevLink = new RouteLink(node.getPrevNode(), node);
            RouteLink nextLink = new RouteLink(node, node.getNextNode()); 

            double distanceToPrev = prevLink.distanceTo(latitude, longitude);
            double distanceToNext = nextLink.distanceTo(latitude, longitude);
            
            return distanceToPrev < distanceToNext ? new RouteLink(prevNode, node) : new RouteLink(node, nextNode);
        }
        else if (node.getPrevNode() != null) {
            return new RouteLink(node.getPrevNode(), node);
        }
        else if (node.getNextNode() != null) {
            return new RouteLink(node, node.getNextNode());
        }
        else {
            Log.e("Route", "Should not reach here. A route link must have at least one of prevNode and nextNode.");
        }
        return null;
	}
	
	public RouteNode getNextTurnNode(RouteNode currentNode, int indexOffset) {
		// TODO: Implement this
		return null;
	}
	
	public void setNodes(JSONArray nodes) throws JSONException {
	    routeNodes = buildRouteNodes(nodes);
	}
	
	public int getId(){
		return rid;
	}
	
	public void setId(int rid) {
	    this.rid = rid;
	}
	
	@Override
	public String toString(){
		String str = "";
		str += "Route ID = " + rid + "\n";
		str += "Validated = " + validated + "\n";
		str += "Node Locations: \n";
		int i = 0;
		while(i < routeNodes.size()){
			str += "Node " + (i + 1) + "\n";
			GeoPoint loc = routeNodes.get(i).getGeoPoint();
			str += "	Lattitude = " + loc.getLatitudeE6() + "\n";
			str += "	Longitude = " + loc.getLongitudeE6() + "\n";
			i++;
		}
		str += "\n";
		if(departureTime != 0) {
			str += "Time = " + timeToString() + "\n";
		}
		return str;
	}
	
	/*****************************************************************************************
	 * What the fuck is this?
	 *****************************************************************************************/
	public String timeToString(){
		Time t = new Time();
		t.set(departureTime);
		String temp = t.toString();
		String formattedTime =  temp.substring(0, 4) + "-";
		formattedTime += temp.substring(6, 8) + "-";
		formattedTime += temp.substring(4, 6) + " ";
		formattedTime += temp.substring(9, 11) + ":";
		formattedTime += temp.substring(11, 13) + ":";
		formattedTime += temp.substring(13, 15);
		
		return formattedTime;
	}
	
	public long getDepartureTime() {
		return departureTime;
	}
	
	/**
	 * @return Duration of this route in seconds.
	 */
	public int getDuration() {
		return duration;
	}
	
	public long getArrivalTime() {
		return departureTime + (duration * 1000);
	}
	
	public void setDepartureTime(long time) {
		this.departureTime = time;
	}
	
	public int getCredits() {
		return credits;
	}
	
	public void setCredits(int credits) {
		this.credits = credits;
	}
	
	/**
	 * Calculates the total length of the route.
	 * NOTE: This haven't tested
	 * 
	 * @return Length of the route in meters.
	 */
	public double getLength() {
	    double length = 0.0;
	    if(this.length != null){
	        length = this.length; 
	    }else{
	        for (RouteNode node : routeNodes) {
                double d = node.getDistance();
                length += d;
            }
	    }
		return length;
	}
	
	/**
	 * This might not be accurate. Need to consider forward nodes only.
	 * 
	 * @param latitude
	 * @param longitude
	 * @return Distance from the given geocoordinate to the next turn.
	 */
	public double getDistanceToNextTurn(double latitude, double longitude) {
		double distance = 0.0;
		RouteLink nearestLink = getNearestLink(latitude, longitude);
		
		// nearest downstream node
		RouteNode nearestNode = nearestLink.getEndNode();
		
		if (nearestNode != null) {
			distance = nearestNode.distanceTo(latitude, longitude);
			
			RouteNode nextNode = nearestNode;//.getNextNode();
			while (nearestNode.getFlag() == 0 && nextNode != null && nextNode.getFlag() == 0) {
				distance += nextNode.getDistance();
				
				nextNode = nextNode.getNextNode();
			}
			
			// FIXME: Temporary
//			if (nearestNode.getFlag() != 0 && nearestNode.distanceTo(latitude, longitude) < 5) {
//			    nearestNode.setFlag(0);
//			}
		}
		return distance;
	}
	
	/**
	 * @return Sum of the length of validated nodes in meters
	 */
	public double getValidatedDistance() {
		double distance = 0.0;
		
		for (RouteNode node : getNodes()) {
			if (node.getMetadata().isValidated()) {
				distance += node.getDistance();
			}
		}
		
		return distance;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public boolean isNearOrigin(double lat, double lng) {
		return false;
	}
	
	/**
	 * Determines whether a geocoordinate is close enough to the destination of the route
	 * 
	 * @param lat
	 * @param lng
	 * @return
	 */
	public boolean hasArrivedAtDestination(double lat, double lng) {
		RouteNode lastNode = routeNodes.get(routeNodes.size() - 1);
		ValidationParameters params = ValidationParameters.getInstance();
		
		return lastNode.distanceTo(lat, lng) <= params.getArrivalDistanceThreshold();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
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
			bundle.putDouble("latitude" + i, routenode.getLatitude());
			bundle.putDouble("longitude" + i, routenode.getLongitude());
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
	
	public void preprocessNodes() {
		RouteNode prevNode = null;
		for(int i = 0; i < routeNodes.size(); i++) {
			RouteNode node = routeNodes.get(i);
			node.setPrevNode(prevNode);
			node.setNodeIndex(i);
			
			if (prevNode != null) {
				prevNode.setNextNode(node);
			}
			
			if (node.getFlag() != 0) {
				if (!node.hasMetadata()) {
					node.setMetadata(new RouteNode.Metadata());
				}
			}
			
			prevNode = node;
		}
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
		dest.writeLong(departureTime);
		dest.writeInt(uid);
		dest.writeInt(credits);
		dest.writeValue(fake);
		dest.writeInt(seq);
		dest.writeInt(link == null?0:link.id);
		dest.writeString(link == null?null:link.url);
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
	
	public static void buildRouteNodeReferenceChain(List<RouteNode> nodes) {
		RouteNode prevNode = null;
		for(int i = 0; i < nodes.size(); i++) {
			RouteNode node = nodes.get(i);
			node.setPrevNode(prevNode);
			node.setNodeIndex(i);
			
			if (prevNode != null) {
				prevNode.setNextNode(node);
			}
			
			prevNode = node;
		}
	}
	
	public static List<RouteNode> buildRouteNodes(JSONArray array) throws JSONException {
	    List<RouteNode> nodes = new ArrayList<RouteNode>();
	    
	    for (int i = 0; i < array.length(); i++) {
	        JSONObject obj = (JSONObject) array.get(i);
	        double latitude = obj.getDouble("LATITUDE");
	        double longitude = obj.getDouble("LONGITUDE");
	        int nodeId = obj.getInt("NODEID");
	        RouteNode node = new RouteNode(latitude, longitude, 0, nodeId);
	        
	        nodes.add(node);
	    }
	    
	    return nodes;
	}

    public boolean isFake() {
        return fake;
    }

    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public NavigationLink getLink() {
        return link;
    }

    public void setLink(NavigationLink link) {
        this.link = link;
    }
}
