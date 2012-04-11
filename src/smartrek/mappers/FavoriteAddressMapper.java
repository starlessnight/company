package smartrek.mappers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import smartrek.models.Address;
import smartrek.util.HTTP;

public class FavoriteAddressMapper extends Mapper {

	private List<Address> addresses = new ArrayList<Address>();
	
	public List<Address> getAddresses(int uid) throws JSONException, IOException {
		String url = String.format("%s/getfavadd/%d", host, uid);
		
		HTTP http = new HTTP(url);
		http.connect();
		
		int responseCode = http.getResponseCode();
		if(responseCode == 200) {
			JSONArray array = new JSONArray(http.getResponseBody());
			addresses.clear();
			
			for(int i = 0; i < array.length(); i++) {
				JSONObject object = (JSONObject) array.get(i);
				
				Address address = new Address();
				address.setAid(object.getInt("FID"));
				address.setUid(object.getInt("UID"));
				address.setName(object.getString("NAME"));
				address.setAddress(object.getString("ADDRESS"));
				
				addresses.add(address);
			}
		}
		else {
			throw new IOException(String.format("HTTP %d - %s", responseCode, http.getResponseBody()));
		}
		
		return addresses;
	}

}
