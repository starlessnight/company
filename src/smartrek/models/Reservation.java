package smartrek.models;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A model class representing a reservation
 */
public final class Reservation implements Parcelable {

	/**
	 * Reservation ID
	 */
	private int rid;
	
	private Date startDatetime;
	
	private Date endDatetime;
	
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
		startDatetime = (Date) in.readValue(Date.class.getClassLoader());
		endDatetime = (Date) in.readValue(Date.class.getClassLoader());
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

	public Date getStartDatetime() {
		return startDatetime;
	}

	public void setStartDatetime(Date startDatetime) {
		this.startDatetime = startDatetime;
	}

	public Date getEndDatetime() {
		return endDatetime;
	}

	public void setEndDatetime(Date endDatetime) {
		this.endDatetime = endDatetime;
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
	 */
	public static Reservation parse(JSONObject object) throws JSONException {
		Reservation r = new Reservation();
		
		r.setRid(object.getInt("RID"));
		// TODO: START_DATETIME
		// TODO: END_DATETIME
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
		dest.writeValue(startDatetime);
		dest.writeValue(endDatetime);
		dest.writeString(originAddress);
		dest.writeString(destinationAddress);
		dest.writeInt(credits);
		dest.writeInt(validatedFlag);
	}
}
