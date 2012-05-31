package com.smartrek.tasks;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import com.smartrek.mappers.FavoriteAddressMapper;
import com.smartrek.models.Address;

import android.os.AsyncTask;

public class FavoriteAddressFetchTask extends AsyncTask<Integer, Object, Object> {

	@Override
	protected Object doInBackground(Integer... params) {

		int uid = params[0];
		
		FavoriteAddressMapper mapper = new FavoriteAddressMapper();
		try {
			List<Address> items = mapper.getAddresses(uid);
		}
		catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
