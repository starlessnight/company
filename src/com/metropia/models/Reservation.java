package com.metropia.models;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.metropia.activities.DebugOptionsActivity;
import com.metropia.receivers.ReservationReceiver;
import com.metropia.requests.Request;
import com.metropia.ui.NavigationView;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.Misc;
import com.metropia.utils.RouteNode;
import com.metropia.utils.ValidationParameters;

/**
 * A model class representing a reservation
 */
public final class Reservation implements Parcelable {
    
    private static final String TIME_FORMAT = "EEEE MMM dd, yyyy\nhh:mm a";
    
    private static final String TIME_FORMAT_SINGLE_LINE = "EEEE MMM dd, yyyy hh:mm a";

    public static final long GRACE_INTERVAL = 30*60*1000L;
	
	public static final String Driver = "driver";
	public static final String DUO = "duo";
    
	/**
	 * Reservation ID
	 */
	private long rid;
	
	private Route route;
	
	private long departureTime;
	
	private long departureTimeUtc;
	
	/**
	 * Estimated travel time in seconds
	 */
	private int duration;
	
	private String originAddress;
	
	private String destinationAddress;
	
	private String originName;
    
    private String destinationName;
	
	private int credits;
	
	private int mpoint;
	
	private int validatedFlag;
	
	private String navLink;
	
	private double endlat;
	
	private double endlon;
	
	public static int COMPLETED = 0x0001;
	public static int VALIDATED = 0x0002;
	
	private String city;
	private String mode;
	
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
		rid = in.readLong();
		route = in.readParcelable(Route.class.getClassLoader());
		departureTime = in.readLong();
		departureTimeUtc = in.readLong();
		duration = in.readInt();
		originAddress = in.readString();
		destinationAddress = in.readString();
		credits = in.readInt();
		mpoint = in.readInt();
		validatedFlag = in.readInt();
		navLink = in.readString();
		endlat = in.readDouble();
		endlon = in.readDouble();
		city = in.readString();
	}

	public long getRid() {
		return rid;
	}

	public void setRid(long rid) {
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
    
    public long getArrivalTimeUtc() {
        return departureTimeUtc + duration*1000;
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
	
	public String getCity() {
		return this.city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getMode() {
		return mode;
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public boolean isPast() {
		return getDepartureTimeUtc() < System.currentTimeMillis();
	}
	
	/**
	 * The difference between {@code isPast()} and {@code hasExpired()} is that
	 * {@code isPast()} simply compares the current system time with the
	 * reserved departure time whereas {@code hasExpired()} considers grace
	 * period.
	 * 
	 * Grace period: 30 minutes
	 * 
	 * @return True if (the current system time) > (departure time) + (grace period)
	 */
	public boolean hasExpired() {
		return hasExpired(getExpiryTime());
	}
	
	public static boolean hasExpired(long expiryTime){
	    return expiryTime < System.currentTimeMillis();
	}
	
    public long getExpiryTime() {
        return getExpiryTime(getDepartureTimeUtc());
    }
    
    public static long getExpiryTime(long departureTime){
        return departureTime + (GRACE_INTERVAL);
    }
	
	/**
	 * Determines whether it is too early to start the trip.
	 * 
	 * Grace period: 15 minutes
	 * 
	 * @return
	 */
	public boolean isTooEarlyToStart() {
		return isTooEarlyToStart(getDepartureTimeUtc());
	}
	
	private static final long EARLY_GRACE_INTERVAL = 15 * 60 * 1000;
	
	public static boolean isTooEarlyToStart(long departureTime) {
        return departureTime - (EARLY_GRACE_INTERVAL) > System.currentTimeMillis();
    }
	
	public boolean isEligibleTrip() {
		return !hasExpired() && !isTooEarlyToStart();
	}
	
	public static boolean isEligibleTrip(long departureTime){
	    return !hasExpired(getExpiryTime(departureTime)) && !isTooEarlyToStart(departureTime);
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
		
		r.setRid(object.getLong("RID"));
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		dateFormat.setTimeZone(TimeZone.getTimeZone(Request.getTimeZone()));
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
        r.setCity(object.optString("city"));
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
		dest.writeLong(rid);
		dest.writeParcelable(route, flags);
		dest.writeLong(departureTime);
		dest.writeLong(departureTimeUtc);
		dest.writeInt(duration);
		dest.writeString(originAddress);
		dest.writeString(destinationAddress);
		dest.writeInt(credits);
		dest.writeInt(mpoint);
		dest.writeInt(validatedFlag);
		dest.writeString(navLink);
		dest.writeDouble(endlat);
		dest.writeDouble(endlon);
		dest.writeString(city);
	}
	
	public static String formatTime(long time, TimeZone timezone){
	    return formatTime(time, false, timezone);  
	}
	
	public static String formatTime(long time){
        return formatTime(time, false);  
    }
	
	public static String formatTime(long time, boolean singleLine){
        return formatTime(time, singleLine, null);  
    }
	
	public static String formatTime(long time, boolean singleLine, TimeZone timezone){
	    SimpleDateFormat dateFormat = new SimpleDateFormat(singleLine?TIME_FORMAT_SINGLE_LINE:TIME_FORMAT);	    
        dateFormat.setTimeZone(timezone == null?TimeZone.getTimeZone(Request.getTimeZone()):timezone);
        return dateFormat.format(new Date(time));  
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
    
    public static Comparator<Reservation> orderByDepartureTime(){
        return new Comparator<Reservation>() {
            @Override
            public int compare(Reservation lhs, Reservation rhs) {
                return new CompareToBuilder()
                    .append(lhs.departureTime, rhs.departureTime)
                    .toComparison();
            }
        };
    }

    public String getNavLink() {
        return navLink;
    }

    public void setNavLink(String navLink) {
        this.navLink = navLink;
    }

    public long getDepartureTimeUtc() {
        return departureTimeUtc;
    }

    public void setDepartureTimeUtc(long departureTimeUtc) {
        this.departureTimeUtc = departureTimeUtc;
    }

    public int getMpoint() {
        return mpoint;
    }

    public void setMpoint(int mpoint) {
        this.mpoint = mpoint;
    }

    public double getEndlat() {
        return endlat;
    }

    public void setEndlat(double endlat) {
        this.endlat = endlat;
    }

    public double getEndlon() {
        return endlon;
    }

    public void setEndlon(double endlon) {
        this.endlon = endlon;
    }
    
    public GeoPoint getStartGpFromNavLink() throws MalformedURLException {
    	return getGPFromNavLink(true);
    }
    
    public GeoPoint getEndGpFromNavLink() throws MalformedURLException {
    	return getGPFromNavLink(false);
    }
    
    private GeoPoint getGPFromNavLink(boolean startGp) throws MalformedURLException {
    	if(StringUtils.isNotBlank(navLink)) {
    		URL navigationLink = new URL(navLink);
    		Map<String, String> paramValueMap = Misc.processQueryString(navigationLink.getQuery());
    		String latName = startGp?"startlat":"endlat";
    		String lonName = startGp?"startlon":"endlon";
    		double lat = Double.valueOf(paramValueMap.get(latName));
    		double lon = Double.valueOf(paramValueMap.get(lonName));
    		return new GeoPoint(lat, lon);
    	}
    	return null;
    }
    
	/**
	 * Determines whether a geocoordinate is close enough to the destination of the route
	 * 
	 * @param lat
	 * @param lng
	 * @return
	 */
    
    public double distanceToDestInMeter=-1, arrivalThreshold=0, tally=0, minArrivalThreshold = Double.MAX_VALUE;
    private boolean beClose = false;
    
    
	public boolean hasArrivedAtDestination(Context ctx, double lat, double lng, long startCountDownTime) {
		ValidationParameters params = ValidationParameters.getInstance();
		boolean arrived = false;
		distanceToDestInMeter = RouteNode.distanceBetween(lat, lng, endlat, endlon);
		beClose |= (distanceToDestInMeter <= params.getArrivalDistanceThreshold());
		if(beClose){
			arrivalThreshold = getDelayTime(ctx, distanceToDestInMeter);
			minArrivalThreshold = Math.max(Math.min(arrivalThreshold, minArrivalThreshold), 2);
			tally = System.currentTimeMillis() - startCountDownTime;
			arrived = tally >= minArrivalThreshold;
		}
		return arrived;
	}
	
	public Long getStartCountDownTime(double lat, double lon, double speedMph, long oldStartCountDownTime) {
		Long startCountDownTime = Long.valueOf(oldStartCountDownTime);
		ValidationParameters params = ValidationParameters.getInstance();
		double distanceToDest = RouteNode.distanceBetween(lat, lon, endlat, endlon);
		beClose |= (distanceToDest <= params.getArrivalDistanceThreshold());
		if(beClose) {
			if(speedMph <= params.getStopSpeedThreshold()) {
				startCountDownTime = Math.min(startCountDownTime, Long.valueOf(System.currentTimeMillis()));
			}
			else {
				startCountDownTime = Long.valueOf(System.currentTimeMillis());
			}
		}
		return startCountDownTime;
	}
	
	private long getDelayTime(Context ctx, double distanceInMeter) {
		return 1000 * Double.valueOf(DebugOptionsActivity.getArrivalLogicCoefficientA(ctx) * Math.pow(NavigationView.metersToFeet(distanceInMeter), 3) + 
				DebugOptionsActivity.getArrivalLogicCoefficientB(ctx) * Math.pow(NavigationView.metersToFeet(distanceInMeter), 2) + 
				DebugOptionsActivity.getArrivalLogicCoefficientC(ctx) * NavigationView.metersToFeet(distanceInMeter)).longValue(); 
	}
	
	/**
     * Creates a system alarm for a single route
     */
	public static void scheduleNotification(Context ctx, long reservationId, Route route) {
		
		long departureTime = route.getDepartureTime();
		
		Intent intent = new Intent(ctx, ReservationReceiver.class);
		
		// TODO: We need a reservation instance here...
		intent.putExtra("route", route);
		
		// NOTE: It appears custom Parcelable objects cannot passed across
		// different processes. Since a PendingIntent launched by AlarmManager
		// is on a separate process, we cannot pack a Route object.
		intent.putExtra(ReservationReceiver.RESERVATION_ID, reservationId);
		
		// In reality, you would want to have a static variable for the
		// request code instead of 192837
		PendingIntent pendingOperation = PendingIntent.getBroadcast(ctx, 192837,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, departureTime - EARLY_GRACE_INTERVAL, pendingOperation);
	}
	
	public static void cancelNotification(Context ctx){
        Intent intent = new Intent(ctx, ReservationReceiver.class);
        PendingIntent.getBroadcast(ctx, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT).cancel();
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(ReservationReceiver.ID);
	}
	
}
