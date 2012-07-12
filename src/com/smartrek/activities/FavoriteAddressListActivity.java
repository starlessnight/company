package com.smartrek.activities;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.smartrek.adapters.FavoriteAddressAdapter;
import com.smartrek.mappers.FavoriteAddressMapper;
import com.smartrek.models.Address;
import com.smartrek.models.User;
import com.smartrek.utils.ExceptionHandlingService;

public class FavoriteAddressListActivity extends Activity {
	private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	private List<Address> addresses;
	private ListView listViewFavoriteAddresses;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorite_address_list);

		listViewFavoriteAddresses = (ListView) findViewById(R.id.listViewFavoriteAddresses);
		listViewFavoriteAddresses
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Intent resultIntent = new Intent();
						// TODO: Maybe we want to return Address instance
						// instead of String value
						resultIntent.putExtra("address", addresses
								.get(position).getAddress());
						setResult(Activity.RESULT_OK, resultIntent);
						finish();
					}

				});

		new FavoriteAddressFetchTask().execute(User.getCurrentUser(this)
				.getId());
	}

	// private class FavoriteAddressAdapter extends ArrayAdapter<Address> {
	//
	// private int textViewResourceId;
	// private List<Address> objects;
	//
	// public ReservationItemAdapter(Context context, int textViewResourceId,
	// List<Address> objects) {
	// super(context, textViewResourceId, objects);
	//
	// this.textViewResourceId = textViewResourceId;
	// this.objects = objects;
	// }
	//
	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// Address address = objects.get(position);
	//
	// LayoutInflater inflater = getLayoutInflater();
	// View view = inflater.inflate(textViewResourceId, parent, false);
	//
	// return view;
	// }
	// }

	private class FavoriteAddressFetchTask extends
			AsyncTask<Integer, Object, List<Address>> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(FavoriteAddressListActivity.this);
			dialog.setMessage("Fetching favorite addresses...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected List<Address> doInBackground(Integer... params) {

			// FIXME: Potential array out of boundary exception
			int uid = params[0];

			FavoriteAddressMapper mapper = new FavoriteAddressMapper();
			try {
				addresses = mapper.getAddresses(uid);
			}
			catch (JSONException e) {
				ehs.registerException(e);
			}
			catch (IOException e) {
				ehs.registerException(e);
			}

			return addresses;
		}

		@Override
		protected void onPostExecute(List<Address> result) {
			dialog.cancel();

			if (ehs.hasExceptions()) {
				ehs.reportExceptions();
			}

			if (result != null) {
				listViewFavoriteAddresses
						.setAdapter(new FavoriteAddressAdapter(
								FavoriteAddressListActivity.this, result));
			}
		}
	}
}
