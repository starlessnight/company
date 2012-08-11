package com.smartrek.dialogs;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.models.Trip;
import com.smartrek.models.User;
import com.smartrek.requests.TripListFetchRequest;
import com.smartrek.utils.ExceptionHandlingService;

public class TripListDialog extends AlertDialog {
	
	/**
	 * Dialog action listener
	 *
	 */
	public interface ActionListener {
		void onClickAddTripButton();
		void onClickNegativeButton();
		void onClickListItem(Trip trip, int position);
	}
	
	private ExceptionHandlingService ehs = new ExceptionHandlingService(getContext());
	
	private DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (listener != null) {
				listener.onClickNegativeButton();
			}
		}
		
	};
	
	private ActionListener listener;
	private ViewGroup dialogView;
	private ListView listViewTrip;
	private TextView textViewEmpty;
	
	public TripListDialog(Context context) {
		super(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.trip_list, null);
		
		setView(dialogView);
		setTitle("Trip List");
		
		setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", onClickListener);
		
		prepareTripList();
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
	}
	
	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}
	
	private void prepareTripList() {
		listViewTrip = (ListView) dialogView.findViewById(R.id.list_view_trip);
		textViewEmpty = (TextView) dialogView.findViewById(R.id.text_view_empty);
		
		User currentUser = User.getCurrentUser(getContext());
		new TripListFetchTask().execute(currentUser.getId());
	}
	
	private void initTripList(final List<Trip> trips) {
		listViewTrip.setAdapter(new TripListAdapter(getContext(), trips));
		listViewTrip.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (listener != null) {
					Trip trip = trips.get(position);
					listener.onClickListItem(trip, position);
				}
				dismiss();
			}
			
		});
	}
	
	private void initEmptyTripList() {
		// FIXME: Temporary
		listViewTrip.setVisibility(View.INVISIBLE);
		textViewEmpty.setVisibility(View.VISIBLE);
		setButton(DialogInterface.BUTTON_NEGATIVE, "Dismiss", onClickListener);
		setButton(DialogInterface.BUTTON_POSITIVE, "Add Trip", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.onClickAddTripButton();
				}
			}
		});
	}
	
	private class TripListFetchTask extends AsyncTask<Object, Object, List<Trip>> {

		@Override
		protected List<Trip> doInBackground(Object... params) {
			int uid = (Integer) params[0];
			
			TripListFetchRequest request = new TripListFetchRequest();
			List<Trip> trips = null;
			try {
				trips = request.execute(uid);
			}
			catch (IOException e) {
				ehs.registerException(e);
			}
			catch (JSONException e) {
				ehs.registerException(e);
			}

			return trips;
		}
		
		@Override
		protected void onPostExecute(List<Trip> result) {
			if (ehs.hasExceptions()) {
				ehs.reportExceptions();
			}
			else {
				if (result != null && result.size() > 0) {
					initTripList(result);
				}
				else {
					initEmptyTripList();
				}
			}
		}
		
	}

	private class TripListAdapter extends ArrayAdapter<Trip> {
		
		public TripListAdapter(Context context,	List<Trip> objects) {
			super(context, R.layout.trip_list_item, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			
			if (view == null) {
				LayoutInflater inflater = getLayoutInflater();
				view = inflater.inflate(R.layout.trip_list_item, parent, false);
			}
			
			Trip trip = getItem(position);
			
			TextView textView3 = (TextView) view.findViewById(R.id.text_view_name);
			textView3.setText(trip.getName());
			
			TextView textView1 = (TextView) view.findViewById(R.id.text_view_origin);
			textView1.setText(trip.getOrigin());
			
			TextView textView2 = (TextView) view.findViewById(R.id.text_view_destination);
			textView2.setText(trip.getDestination());
			
			return view;
		}
	}
}
