package smartrek.activities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import smartrek.SeverCommunicator.ReservationCommunicator;
import smartrek.models.Reservation;
import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ReservationActivity extends ListActivity {
	
	private List<Reservation> reservations;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.reservation);
        
        //String[] items = new String[] { "alpha", "beta", "charlie" };
        
        //setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, items));
        
        reservations = new ArrayList<Reservation>();
        
        new ReservationRetrivalTask().execute();
	}
	
	/**
	 * Inner class for an asynchronous task.
	 */
	private class ReservationRetrivalTask extends AsyncTask<Object, Object, String> {
		@Override
		protected String doInBackground(Object... params) {
			ReservationCommunicator comm = new ReservationCommunicator();
			String response = comm.downloadText("http://api.smartrekmobile.com/reservation?uid=1");

			try {
				JSONArray array = new JSONArray(response);
				for(int i = 0; i < array.length(); i++) {
					Reservation r = Reservation.parse(new JSONObject(array.get(i).toString()));
					reservations.add(r);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return response;
		}
		
		@Override
		protected void onPostExecute(String result) {
//			String[] items = new String[reservations.size()];
//			for(int i = 0; i < reservations.size(); i++) {
//				items[i] = reservations.get(i).getOriginAddress();
//			}
			setListAdapter(new ReservationItemAdapter(ReservationActivity.this, R.layout.list_item, reservations));
	    }
	}
	
	private class ReservationItemAdapter extends ArrayAdapter<Reservation> {
		
		private int textViewResourceId;
		private List<Reservation> objects;

		public ReservationItemAdapter(Context context, int textViewResourceId,
				List<Reservation> objects) {
			super(context, textViewResourceId, objects);
			
			this.textViewResourceId = textViewResourceId;
			this.objects = objects;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Reservation r = objects.get(position);
			
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(textViewResourceId, parent, false);
			
			TextView textView1 = (TextView)view.findViewById(R.id.textViewOrigin);
			textView1.setText(r.getOriginAddress());
			
			TextView textView2 = (TextView)view.findViewById(R.id.textViewDestination);
			textView2.setText(r.getDestinationAddress());
			
			return view;
		}
	}
}