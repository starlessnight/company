package smartrek.mappers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import smartrek.models.Address;
import smartrek.util.HTTP;

// http://50.56.81.42:8080/getfavadd/10
// {"FID":"26","NAME":"1905 W. Jefferson Street","UID":"10","ADDRESS":"1905 W. Jefferson Street, Phoenix,AZ, 85007"}
public class FavoriteAddressMapper extends Mapper {

	private List<Address> addresses = new ArrayList<Address>();
	
	public List<Address> getAddresses(int uid) throws JSONException {
		// FIXME: Temporary
		String url = "http://50.56.81.42:8080/getfavadd/" + uid;
		
		String response = HTTP.downloadText(url);
		
		JSONArray array = new JSONArray(response);
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
		
		return addresses;
	}

}
