package smartrek.models;

import java.sql.Date;
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
	
	private Time departureTime;
	
	/**
	 * Estimated arrival time
	 */
	private Time arrivalTime;
	
	private String originAddress;
	
	private String destinationAddress;
	
	private int credits;
	
	private int validatedFlag;
	
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
		departureTime = new Time();
		departureTime.parse(in.readString());
		arrivalTime = new Time();
		arrivalTime.parse(in.readString());
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

	public Time getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(Time departureTime) {
		this.departureTime = departureTime;
	}

	public Time getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(Time arrivalTime) {
		this.arrivalTime = arrivalTime;
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
		
		dateFormat = new SimpleDateFormat("HH:mm:ss");
		Time arrivalTime = new Time();
		arrivalTime.set(dateFormat.parse(object.getString("END_TIME")).getTime());
		r.setArrivalTime(arrivalTime);
		
		r.setOriginAddress(object.getString("ORIGIN_ADDRESS"));
		r.setDestinationAddress(object.getString("DESTINATION_ADDRESS"));
		r.setCredits(object.getInt("CREDITS"));
		r.setValidatedFlag(object.getInt("VALIDATED_FLAG"));
		
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
		dest.writeString(departureTime.format2445());
		dest.writeString(arrivalTime.format2445());
		dest.writeString(originAddress);
		dest.writeString(destinationAddress);
		dest.writeInt(credits);
		dest.writeInt(validatedFlag);
	}
}
