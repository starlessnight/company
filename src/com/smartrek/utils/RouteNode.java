package com.smartrek.utils;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.smartrek.models.JSONModel;

public final class RouteNode implements Parcelable, JSONModel {
	
	/**
	 * Extra information for navigation
	 *
	 */
	public static class Metadata implements Parcelable {
		public boolean[] pingFlags = new boolean[3];
		
		public static final Parcelable.Creator<Metadata> CREATOR = new Parcelable.Creator<Metadata>() {
			public Metadata createFromParcel(Parcel in) {
				return new Metadata(in);
			}

			public Metadata[] newArray(int size) {
				return new Metadata[size];
			}
		};
		
		public Metadata() {
			resetPingFlags();
		}
		
		public Metadata(Parcel in) {
			in.readBooleanArray(pingFlags);
		}
		
		public void resetPingFlags() {
			for (int i = 0; i < pingFlags.length; i++) {
				pingFlags[i] = false;
			}
		}
		
		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeBooleanArray(pingFlags);
		}
	}
	
	private double latitude;
	private double longitude;
	private int routeNum;
	private int nodeNum;
	
	/**
	 * This must be non-null for route nodes with a non-zero {@code flag}.
	 */
	private Metadata metadata;
	
	/**
	 * Navigation metadata. Non-zero value indicates change of navigation information.
	 */
	private int flag;
	
	/**
	 * Navigation metadata. Distance to the next node.
	 * I think we're getting this from the server in terms of miles but
	 * we only use meters internally.
	 */
	private double distance;
	
	/**
	 * Navigation message
	 */
	private String message;
	
	/**
	 * Navigation metadata
	 */
	private String roadName;
	
    /**
     * A reference to the previous node. A link consists of two route nodes. A
     * route node belongs to at least one link, at most two links.
     */
    private transient RouteNode prevNode;
	
	/**
	 * A reference to the next node;
	 */
	private transient RouteNode nextNode;
	
	/**
	 * RouteNode index in List<RouteNode>
	 */
	private transient int nodeIndex;
	
	public static final Parcelable.Creator<RouteNode> CREATOR = new Parcelable.Creator<RouteNode>() {
		public RouteNode createFromParcel(Parcel in) {
			return new RouteNode(in);
		}

		public RouteNode[] newArray(int size) {
			return new RouteNode[size];
		}
	};

	private RouteNode(Parcel in) {
		latitude = in.readDouble();
		longitude = in.readDouble();
		routeNum = in.readInt();
		nodeNum = in.readInt();
		//metadata = in.readParcelable(Metadata.class.getClassLoader());
		flag = in.readInt();
		distance = in.readDouble();
		message = in.readString();
		roadName = in.readString();
	}
	
	public RouteNode(double latitude, double longitude, int routeNum, int nodeNum) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.routeNum = routeNum;
		this.nodeNum = nodeNum;
	}
	
	public RouteNode(Bundle  bundle, int routeNum, int nodeNum) {
		this.latitude = Double.parseDouble(bundle.getString("latitude" + nodeNum));
		this.longitude = Double.parseDouble(bundle.getString("longitude" + nodeNum));
		this.routeNum = routeNum;
		this.nodeNum = nodeNum;
	}
	
	public GeoPoint getGeoPoint() {
		return new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
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
    
    public int getNodeIndex() {
		return nodeIndex;
	}

	public void setNodeIndex(int nodeIndex) {
		this.nodeIndex = nodeIndex;
	}
	
	public Metadata getMetadata() {
		return metadata;
	}
	
	public boolean hasMetadata() {
		return metadata != null;
	}
	
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
     * Calculates the distance from this route node to a geocoordinate.
     * 
     * @param lat
     * @param lng
     * @return Distance to a geocoordinate. Unit is in meters.
     */
    public double distanceTo(double lat, double lng) {
        return distanceBetween(getLatitude(), getLongitude(), lat, lng);
    }
    
    /**
     * Calculates the distance from this route node to another node.
     * 
     * @param node
     * @return
     */
    public double distanceTo(RouteNode node) {
        return distanceBetween(getLatitude(), getLongitude(), node.getLatitude(), node.getLongitude());
    }

    public String getRoadName() {
		return roadName;
	}

	public void setRoadName(String roadName) {
		this.roadName = roadName;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeInt(routeNum);
		dest.writeInt(nodeNum);
		//dest.writeParcelable(metadata, flags);
		dest.writeInt(flag);
		dest.writeDouble(distance);
		dest.writeString(message);
		dest.writeString(roadName);
	}
	
	@Override
	public String toString() {
	    return String.format("RouteNode (%f, %f)", latitude, longitude);
	}
	
	/**
	 * Example output:
	 * { "LATITUDE":37.797065,"LONGITUDE":-122.251991,"NODEID":14561}
	 */
	@Override
	public String toJSON() {
		return String.format("{\"LATITUDE\":%f,\"LONGITUDE\":%f,\"NODEID\":%d}", getLatitude(), getLongitude(), nodeNum);
	}
	
	public boolean equals(RouteNode node) {
		return node != null && latitude == node.latitude && longitude == node.longitude && nodeNum == node.nodeNum;
	}
	
	public static double distanceBetween(RouteNode node1, RouteNode node2) {
		return distanceBetween(node1.getLatitude(), node1.getLongitude(), node2.getLatitude(), node2.getLongitude());
	}
	
	public static double distanceBetween(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6378.137;
        double radLat1 = lat1 * Math.PI / 180.0;
        double radLat2 = lat1 * Math.PI / 180.0;
        double radLat = (lat1-lat2) * Math.PI / 180.0;
        double radLng = (lng1-lng2) * Math.PI / 180.0;

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(radLat / 2.0), 2) +

         Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(radLng / 2.0), 2)));

        s = s * earthRadius;

        return s * 1000;
	}
}