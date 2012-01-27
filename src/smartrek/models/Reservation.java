package smartrek.models;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class Reservation {
	/**
	 * Deal ID
	 */
	private int did;
	
	/**
	 * Reservation ID
	 */
	private int rid;
	
	private Date startDatetime;
	
	private Date endDatetime;
	
	private String originAddress;
	
	private String destinationAddress;
	
	private int validatedFlag;
	
	public int getDid() {
		return did;
	}

	public void setDid(int did) {
		this.did = did;
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

	public int getValidatedFlag() {
		return validatedFlag;
	}

	public void setValidatedFlag(int validatedFlag) {
		this.validatedFlag = validatedFlag;
	}
	
	public static Reservation parse(JSONObject object) throws JSONException {
		Reservation r = new Reservation();
		
		r.setDid(object.getInt("DID"));
		r.setRid(object.getInt("RID"));
		// START_DATETIME
		// END_DATETIME
		r.setOriginAddress(object.getString("ORIGIN_ADDRESS"));
		r.setDestinationAddress(object.getString("DESTINATION_ADDRESS"));
		r.setValidatedFlag(object.getInt("VALIDATED_FLAG"));
		
		return r;
	}
}
