package request;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONException;

import com.smartrek.requests.UpdateRequest;

public class TripUpdateRequest extends UpdateRequest {
	
	/**
	 * Trip ID
	 */
	private int tid;
	
	/**
	 * User ID
	 */
	private int uid;
	
	/**
	 * Trip name
	 */
	private String name;
	
	/**
	 * Origin address ID
	 */
	private int oid;
	
	/**
	 * Destination address ID
	 */
	private int did;
	
	public TripUpdateRequest(int tid, int uid, String name, int oid, int did) {
		this.tid = tid;
		this.uid = uid;
		this.name = name;
		this.oid = oid;
		this.did = did;
	}

	public void execute() throws IOException, JSONException {
		String url = String.format("%s/favroutes-update/?rid=%d&uid=%d&name=%s&oid=%d&did=%d",
				HOST, tid, uid, URLEncoder.encode(name), oid, did);
		executeUpdateRequest(url);
	}
}
