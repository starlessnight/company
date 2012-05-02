package smartrek.activities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import smartrek.mappers.ReservationMapper;
import smartrek.models.Reservation;
import smartrek.models.User;
import smartrek.util.HTTP;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Shows a list of reserved routes
 *
 */
public final class ReservationListActivity extends ListActivity {
	
	private List<Reservation> reservations;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        reservations = new ArrayList<Reservation>();
        
        User currentUser = User.getCurrentUser(this);
        
        new ReservationRetrivalTask().execute(currentUser.getId());
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, ReservationDetailsActivity.class);
		
		Bundle extras = new Bundle();
		extras.putParcelable("reservation", reservations.get(position));
		intent.putExtras(extras);
		startActivity(intent);
	}
	
	/**
	 * Inner class for an asynchronous task.
	 */
	private class ReservationRetrivalTask extends AsyncTask<Object, Object, String> {
		@Override
		protected String doInBackground(Object... params) {
			int uid = (Integer) params[0];
			
			ReservationMapper mapper = new ReservationMapper();
			String response = null;
			try {
				// FIXME: Hard-coded UID
				response = HTTP.downloadText("http://50.56.81.42:8080/getreservations/" + uid);
			}
			catch(Exception e) {
				e.printStackTrace();
			}

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
			setListAdapter(new ReservationItemAdapter(ReservationListActivity.this, R.layout.reservation_list_item, reservations));
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