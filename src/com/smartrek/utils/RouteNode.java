package com.smartrek.utils;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;
import com.smartrek.models.JSONModel;

public final class RouteNode implements Parcelable, JSONModel {
	
	private float lat;
	private float lng;
	private int routeNum;
	private int nodeNum;
	
    /**
     * A reference to the previous node. A link consists of two route nodes. A
     * route node belongs to at least one link, at most two links.
     */
    private RouteNode prevNode;
	
	/**
	 * A reference to the next node;
	 */
	private RouteNode nextNode;
	
	public static final Parcelable.Creator<RouteNode> CREATOR = new Parcelable.Creator<RouteNode>() {
		public RouteNode createFromParcel(Parcel in) {
			return new RouteNode(in);
		}

		public RouteNode[] newArray(int size) {
			return new RouteNode[size];
		}
	};

	private RouteNode(Parcel in) {
		lat = in.readFloat();
		lng = in.readFloat();
		routeNum = in.readInt();
		nodeNum = in.readInt();
	}
	
	public RouteNode(float latitude, float longitude, int routeNum, int nodeNum) {
//		super(new GeoPoint(
//				  (int)Math.round(1E6*latitude),
//				  (int)Math.round(1E6*longitude)),
//				  "Route " + routeNum,
//				  "Node " + nodeNum );

		lat = latitude;
		lng = longitude;
		this.routeNum = routeNum;
		this.nodeNum = nodeNum;
	}
	
	public RouteNode(Bundle  bundle, int routeNum, int nodeNum) {
//		super(new GeoPoint(
//				  (int)Math.round(1E6*(Float.parseFloat(bundle.getString("latitude" + nodeNum)))),
//				  (int)Math.round(1E6*(Float.parseFloat(bundle.getString("longitude" + nodeNum))))),
//				  "Route " + routeNum,
//				  "Node " + nodeNum );
		
		
		lat = Float.parseFloat(bundle.getString("latitude" + nodeNum));
		lng = Float.parseFloat(bundle.getString("longitude" + nodeNum));
		this.routeNum = routeNum;
		this.nodeNum = nodeNum;
	}
	
	public GeoPoint getPoint() {
		return new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6));
	}
	
	public float getLatitude() {
		return lat;
	}
	
	public float getLongitude() {
		return lng;
	}
	
	public RouteNode getPrevNode() {
        return prevNode;
    }

    public void setPrevNode(RouteNode prevNode) {
        this.prevNode = prevNode;
    }

    public RouteNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(RouteNode nextNode) {
        this.nextNode = nextNode;
    }

    @Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(lat);
		dest.writeFloat(lng);
		dest.writeInt(routeNum);
		dest.writeInt(nodeNum);
	}
	
	@Override
	public String toString() {
	    return String.format("RouteNode (%f, %f)", lat, lng);
	}
	
	/**
	 * Example output:
	 * { "LATITUDE":37.797065,"LONGITUDE":-122.251991,"NODEID":14561}
	 */
	@Override
	public String toJSON() {
		return String.format("{\"LATITUDE\":%f,\"LONGITUDE\":%f,\"NODEID\":%d}", getLatitude(), getLongitude(), nodeNum);
	}
}