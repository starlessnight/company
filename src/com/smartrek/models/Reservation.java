package com.smartrek.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;

/**
 * A model class representing a reservation
 */
public final class Reservation implements Parcelable {

	/**
	 * Reservation ID
	 */
	private int rid;
	
	private Route route;
	
	private Time departureTime;
	
	/**
	 * Estimated travel time
	 */
	private int duration;
	
	/**
	 * Estimated arrival time
	 * @deprecated
	 */
	private Time arrivalTime;
	
	private String originAddress;
	
	private String destinationAddress;
	
	private int credits;
	
	private int validatedFlag;
	
	public static int COMPLETED = 0x0001;
	public static int VALIDATED = 0x0002;
	
	public static final Parcelable.Creator<Reservation> CREATOR = new Parcelable.Creator<Reservation>() {
		public Reservation createFromParcel(Parcel in) {
			return new Reservation(in);
		}

		public Reservation[] newArray(int size) {
			return new Reservation[size];
		}
	};
	
	public Reservation() {
		
	}
	
	private Reservation(Parcel in) {
		rid = in.readInt();
		route = in.readParcelable(Route.class.getClassLoader());
		departureTime = new Time();
		departureTime.parse(in.readString());
		duration = in.readInt();
		originAddress = in.readString();
		destinationAddress = in.readString();
		credits = in.readInt();
		validatedFlag = in.readInt();
	}

	public int getRid() {
		return rid;
	}

	public void setRid(int rid) {
		this.rid = rid;
	}

	public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Time getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(Time departureTime) {
		this.departureTime = departureTime;
	}

	public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Time getArrivalTime() {
        Time time = new Time();
        time.set(departureTime.toMillis(false) + duration*1000);
        
		return time;
	}

	public String getOriginAddress() {
		return originAddress;
	}

	public void setOriginAddress(String originAddress) {
		this.originAddress = originAddress;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}
	
	public int getCredits() {
		return credits;
	}
	
	public void setCredits(int credits) {
		this.credits = credits;
	}

	public int getValidatedFlag() {
		return validatedFlag;
	}

	public void setValidatedFlag(int validatedFlag) {
		this.validatedFlag = validatedFlag;
	}
	
	/**
	 * Parses a JSON object into a reservation object
	 * 
	 * @param object A JSON object
	 * @return An instance of Reservation
	 * @throws JSONException
	 * @throws ParseException 
	 */
	public static Reservation parse(JSONObject object) throws JSONException, ParseException {
		Reservation r = new Reservation();
		
		r.setRid(object.getInt("RID"));
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		Time departureTime = new Time();
		departureTime.set(dateFormat.parse(object.getString("START_TIME")).getTime());
		r.setDepartureTime(departureTime);

		// travel duration
		r.setDuration(object.getInt("END_TIME"));
		
		r.setOriginAddress(object.getString("ORIGIN_ADDRESS"));
		r.setDestinationAddress(object.getString("DESTINATION_ADDRESS"));
		r.setCredits(object.getInt("CREDITS"));
		r.setValidatedFlag(object.getInt("VALIDATED_FLAG"));
		
        Route route = new Route();
        route.setId(r.getRid());
        route.setOrigin(r.getOriginAddress());
        route.setDestination(r.getDestinationAddress());
        route.setDepartureTime(r.getDepartureTime());
        route.setCredits(r.getCredits());
        route.setNodes(object.getJSONArray("ROUTE"));
        
        r.setRoute(route);
		
		return r;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(rid);
		dest.writeParcelable(route, 0);
		dest.writeString(departureTime.format2445());
		dest.writeInt(duration);
		dest.writeString(originAddress);
		dest.writeString(destinationAddress);
		dest.writeInt(credits);
		dest.writeInt(validatedFlag);
	}
}
