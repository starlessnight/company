package com.smartrek.models;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;

import com.smartrek.requests.Request;

/**
 * A model class representing a reservation
 */
public final class Reservation implements Parcelable {
    
    private static final String TIME_FORMAT = "%A %b %d, %G\n%I:%M %p";
    
    private static final String TIME_FORMAT_SINGLE_LINE = "%A %b %d, %G %I:%M %p";
    
	/**
	 * Reservation ID
	 */
	private int rid;
	
	private Route route;
	
	private long departureTime;
	
	/**
	 * Estimated travel time in seconds
	 */
	private int duration;
	
	private String originAddress;
	
	private String destinationAddress;
	
	private String originName;
    
    private String destinationName;
	
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
		departureTime = in.readLong();
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

    /**
     * @return Departure time in milliseconds
     */
    public long getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(long departureTime) {
		this.departureTime = departureTime;
	}
	
	public String getFormattedDepartureTime() {
        return formatTime(departureTime, true);
    }

	public int getDuration() {
        return duration;
    }

	/**
	 * @param Trip duration in seconds
	 */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * @return Arrival time in milliseconds
     */
    public long getArrivalTime() {
        return departureTime + duration*1000;
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
	
	public boolean isPast() {
		return getDepartureTime() < System.currentTimeMillis();
	}
	
	/**
	 * The difference between {@code isPast()} and {@code hasExpired()} is that
	 * {@code isPast()} simply compares the current system time with the
	 * reserved departure time whereas {@code hasExpired()} considers grace
	 * period.
	 * 
	 * Grace period: 15 minutes
	 * 
	 * @return True if (the current system time) > (departure time) + (grace period)
	 */
	public boolean hasExpired() {
		return getExpiryTime() < System.currentTimeMillis();
	}
	
    public long getExpiryTime() {
        return getDepartureTime() + (15*60*1000);
    }
	
	/**
	 * Determines whether it is too early to start the trip.
	 * 
	 * Grace period: 15 minutes
	 * 
	 * @return
	 */
	public boolean isTooEarlyToStart() {
		return getDepartureTime() - (15*60*1000) > System.currentTimeMillis();
	}
	
	public boolean isEligibleTrip() {
		return !hasExpired() && !isTooEarlyToStart();
	}
	
	/**
	 * Parses a JSON object into a reservation object
	 * 
	 * @param object A JSON object
	 * @return An instance of Reservation
	 * @throws JSONException
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static Reservation parse(JSONObject object) throws JSONException, ParseException, IOException {
		Reservation r = new Reservation();
		
		r.setRid(object.getInt("RID"));
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		dateFormat.setTimeZone(TimeZone.getTimeZone(Request.TIME_ZONE));
		long departureTime = dateFormat.parse(object.getString("START_TIME")).getTime();
		r.setDepartureTime(departureTime);

		// travel duration
		r.setDuration(object.getInt("END_TIME") * 60);
		
		r.setOriginAddress(object.getString("ORIGIN_ADDRESS"));
		r.setDestinationAddress(object.getString("DESTINATION_ADDRESS"));
		r.setCredits(object.getInt("CREDITS"));
		r.setValidatedFlag(object.getInt("VALIDATED_FLAG"));
		
		r.setOriginName(object.getString("ORIGIN_NAME"));
        r.setDestinationName(object.getString("DESTINATION_NAME"));
		
//        Route route = new Route();
//        route.setId(r.getRid());
//        route.setOrigin(r.getOriginAddress());
//        route.setDestination(r.getDestinationAddress());
//        route.setDepartureTime(r.getDepartureTime());
//        route.setCredits(r.getCredits());
//        route.setNodes(object.getJSONArray("ROUTE"));
        
        r.setRoute(Route.parse(object, departureTime));
		
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
		dest.writeParcelable(route, flags);
		dest.writeLong(departureTime);
		dest.writeInt(duration);
		dest.writeString(originAddress);
		dest.writeString(destinationAddress);
		dest.writeInt(credits);
		dest.writeInt(validatedFlag);
	}
	
	public static String formatTime(long time){
	    return formatTime(time, false);  
	}
	
	public static String formatTime(long time, boolean singleLine){
        Time at = new Time();
        at.set(time);
        return at.format(singleLine?TIME_FORMAT_SINGLE_LINE:TIME_FORMAT);  
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }
	
}
