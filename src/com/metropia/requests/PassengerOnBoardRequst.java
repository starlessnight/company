package com.metropia.requests;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.metropia.models.Passenger;
import com.metropia.models.User;
import com.metropia.tasks.ICallback;

import android.content.Context;
import android.os.AsyncTask;

public class PassengerOnBoardRequst extends FetchRequest<ArrayList<Passenger>> {

	ArrayList<Passenger> passengers;

	public PassengerOnBoardRequst(User user, ArrayList<Passenger> passengers) {
		super(getLinkUrl(Link.driver_head));
		this.passengers = passengers;
		this.username = user.getUsername();
		this.password = user.getPassword();
		this.timeout = 30*1000;
	}

	static int counter=0;
	@Override
	public ArrayList<Passenger> execute(Context ctx) throws Exception {
		ArrayList<Passenger> remotePassengers = new ArrayList<Passenger>();
		String str = this.executeFetchRequest(url, ctx);
		
		if (counter++==3)
		str = "{'status':'success', 'data':{'pax_id':[761], 'voice':['you are carpooling with Ali']}}";
		
		JSONObject jsonObject = new JSONObject(str);
		
		JSONArray ids = jsonObject.getJSONObject("data").getJSONArray("pax_id");
		JSONArray voice = jsonObject.getJSONObject("data").getJSONArray("voice");
		for (int i=0 ; i<ids.length() ; i++) remotePassengers.add(new Passenger(ids.getInt(i), voice.getString(i)));
		
		return remotePassengers;
	}
	
	public void executeAsync(final Context ctx, final ICallback cb) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ArrayList<Passenger> remotePassengers = null;
				try {
					remotePassengers = PassengerOnBoardRequst.this.execute(ctx);
				} catch (Exception e) {}
				if (cb!=null) cb.run(remotePassengers);
				return null;
			}
		}.execute();
	}
	
	

}
