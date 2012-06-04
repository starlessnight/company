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
    
    /**
     * Calculates the distance from this route node to a geocoordinate.
     * 
     * @param lat
     * @param lng
     * @return Distance to a geocoordinate. Unit is in meters.
     */
    public float distanceTo(float lat, float lng) {
        double earthRadius = 6378.137;

        double radLat1 = this.lat * Math.PI / 180.0;

        double radLat2 = lat * Math.PI / 180.0;

        double radLat = (this.lat-lat) * Math.PI / 180.0;

        double radLng = (this.lng-lng) * Math.PI / 180.0;

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(radLat / 2.0), 2) +

         Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(radLng / 2.0), 2)));

        s = s * earthRadius;

        //s = Math.round(s * 10000) / 10000.0;

        return (float) s * 1000;
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