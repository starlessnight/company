package com.smartrek.activities;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.smartrek.dialogs.FavoriteAddressAddDialog;
import com.smartrek.dialogs.FavoriteAddressListDialog;
import com.smartrek.dialogs.TripListDialog;
import com.smartrek.models.Address;
import com.smartrek.models.Trip;
import com.smartrek.models.User;
import com.smartrek.requests.FavoriteAddressMapper;
import com.smartrek.tasks.GeocodingTask;
import com.smartrek.tasks.GeocodingTaskCallback;
import com.smartrek.ui.EditAddress;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.LocationService;
import com.smartrek.utils.LocationService.LocationServiceListener;

/**
 * This Activity is the home screen for the Smartrek Application. From this
 * screen the user can enter their origin, destination, and trip date. Or the
 * user can access their favorite locations or load a previously reserved trip.
 * 
 * This class will communicate with the Smartrek server to down load user
 * favorites and previously reserved trips. Also this class will query the
 * server for Route information given the user input.
 * 
 * The layout used for this class is in res/layout.home.xml.
 * 
 * This class is responsible for handling the functionality described above.
 * 
 * @author Tim Olivas
 * @author Sumin Byeon
 * 
 * @version 1.0
 * 
 */
public final class HomeActivity extends Activity {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private EditAddress originBox;
	private EditAddress destBox;
	private EditText dateBox;
	
	private TextView originText;
	private TextView destText;
	private TextView dateText;
	
	private Button doneButton;
	private ImageButton buttonFavAddrOrigin;
	private ImageButton destFavButton;
	private Button hereButton;
	
	private GeoPoint originCoord;
	private GeoPoint destCoord;
	
	private Time current;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.home);
	    
	    /***************Start EditText Fields********************/
	    
	    originBox = (EditAddress) findViewById(R.id.origin_box);
	    destBox = (EditAddress) findViewById(R.id.destination_box);
	    //dateBox = (EditText) findViewById(R.id.date_box);
	
		current = new Time();
		current.setToNow();
		//dateBox.setText(current.month + " / " + current.monthDay + " / " + current.year);
	    
	    /***************End EditText Fields********************/
	    
	    /***************Start TextViews********************/
	    
	    // Instantiate TextViews from file main.xml
	    originText = (TextView) findViewById(R.id.origin_text);
	    destText = (TextView) findViewById(R.id.destination_text);
	    //dateText = (TextView) findViewById(R.id.date_text);
	    
	    // Declare a tiny animation to be used on startup.
	    Animation animation = new TranslateAnimation(-400,0,0,0);
		animation.setDuration(1500);
		
		// Set animation to be used by TextViews.
	    destText.setAnimation(animation);
	    originText.setAnimation(animation);
	    //dateText.setAnimation(animation);  
	    
	    /***************End TextViews********************/
	    
	    /***************Start Buttons********************/
	    
	    // Instantiate Buttons from file main.xml
	    buttonFavAddrOrigin = (ImageButton) findViewById(R.id.Favs1);
	    destFavButton = (ImageButton) findViewById(R.id.Favs2);
	    doneButton = (Button) findViewById(R.id.Done);
	    
		// Set Button OnClickListerners to be declared by this class
	    buttonFavAddrOrigin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickButtonFavAddrOrigin(v);
			}
		});
	    destFavButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickButtonFavAddrDest(v);				
			}
		});
	    doneButton.setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View v) {
				debugMode = false;
				prepareMapActivity();
			}
	    	
	    });
	    doneButton.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				debugMode = true;
				if (originBox.getText().toString().equals(""))
					originBox.setText("origin");
				if (destBox.getText().toString().equals(""))
					destBox.setText("destination");
				prepareMapActivity();
				return true;
			}
			
		});
	    
	    buttonFavAddrOrigin.setId(1);
	    destFavButton.setId(2);
	    doneButton.setId(3);
	    
//	    hereButton = (Button) findViewById(R.id.hereAndNow);
//	    hereButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				originBox.setAddressAsCurrentLocation();
//			}
//	    });
	    
	    
	    /***************End Buttons********************/
	}

	private List<Address> favoriteAddresses;
	
	private TripListDialog tripListDialog;
	
	/**
	 * Indicates if we want to start {@code RouteActivity} in a debug mode.
	 */
	private boolean debugMode;
	
    /**
	 * Override the onBackPressed() method so the user does not return to the
	 * Login_Activity after a successful login. Pressing back from the
	 * Home_Activity will quit the application.
	 * 
	 */
	@Override
	public void onBackPressed() {
		finish();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
     	MenuInflater mi = getMenuInflater();
     	mi.inflate(R.menu.home, menu);
    	return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		MainMenu.onMenuItemSelected(this, featureId, item);
		
		switch (item.getItemId()) {
		case R.id.menu_trip_list:
			onMenuItemTripList();
			break;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void onMenuItemTripList() {
		if (tripListDialog == null) {
			tripListDialog = new TripListDialog(this);
			tripListDialog.setActionListener(new TripListDialog.ActionListener() {
				
				@Override
				public void onClickNegativeButton() {
					
				}
				
				@Override
				public void onClickListItem(Trip trip, int position) {
					setOriginAddress(trip.getOrigin());
					setDestinationAddress(trip.getDestination());
				}
	
				@Override
				public void onClickAddTripButton() {
					
				}
			});
		}
		tripListDialog.show();
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch (requestCode) {
		}
	}

	GeocodingTaskCallback originGeocodingTaskCallback = new GeocodingTaskCallback() {
		
		private ProgressDialog dialog;

		@Override
		public void preCallback() {
			dialog = new ProgressDialog(HomeActivity.this);
	        dialog.setMessage("Geocoding origin address...");
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(false);
	        dialog.show();
		}

		@Override
		public void callback(GeoPoint coordinate) {
			originCoord = coordinate;
		}

		@Override
		public void postCallback() {
			dialog.cancel();
			
			if (ehs.hasExceptions()) {
			    ehs.reportExceptions();
			}
			else {
				new GeocodingTask(ehs, destGeocodingTaskCallback).execute(getDestinationAddress());
			}
		}
		
	};
	
	GeocodingTaskCallback destGeocodingTaskCallback = new GeocodingTaskCallback() {

		private ProgressDialog dialog;
		
		@Override
		public void preCallback() {
			dialog = new ProgressDialog(HomeActivity.this);
	        dialog.setMessage("Geocoding destination address...");
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(false);
	        dialog.show();
		}

		@Override
		public void callback(GeoPoint coordinate) {
			destCoord = coordinate;
		}

		@Override
		public void postCallback() {
			dialog.cancel();
			
			if (ehs.hasExceptions()) {
			    ehs.reportExceptions();
			}
			else {
				startMapActivity();
			}
		}
	};
	
	/**
	 * 
	 * @return Origin address that user has entered
	 */
	private String getOriginAddress() {
		return originBox.getText().toString().trim();
	}
	
	/**
	 * 
	 * @return Destination address that user has entered
	 */
	private String getDestinationAddress() {
		return destBox.getText().toString().trim();
	}
	
	private void onClickButtonFavAddrOrigin(View view) {
		if (originBox.hasAddress()) {
			showFavAddrListForOrigin();
		}
		else {
			String origin = getOriginAddress();
			
			if (origin.equals("")) {
				showFavAddrListForOrigin();
			}
			else {
				FavoriteAddressAddDialog dialog = new FavoriteAddressAddDialog(this);
				dialog.setAddress(origin);
				dialog.setActionListener(new FavoriteAddressAddDialog.ActionListener() {
					
					@Override
					public void onClickPositiveButton() {
						// FIXME: Temporary solution
						originBox.setAddress(new Address(0, 0, "", getOriginAddress()));
					}
					
					@Override
					public void onClickNegativeButton() {
						showFavAddrListForOrigin();
					}
				});
				dialog.show();
			}
		}
	}
	
	private void onClickButtonFavAddrDest(View view) {
		if (destBox.hasAddress()) {
			showFavAddrListForDest();
		}
		else {
			String destination = getDestinationAddress();
			
			if (destination.equals("")) {
				showFavAddrListForDest();
			}
			else {
				FavoriteAddressAddDialog dialog = new FavoriteAddressAddDialog(this);
				dialog.setAddress(destination);
				dialog.setActionListener(new FavoriteAddressAddDialog.ActionListener() {
					
					@Override
					public void onClickPositiveButton() {
						// FIXME: Temporary solution
						destBox.setAddress(new Address(0, 0, "", getDestinationAddress()));
					}
					
					@Override
					public void onClickNegativeButton() {
						showFavAddrListForDest();
					}
				});
				dialog.show();
			}
		}
	}
	
	/**
	 * Fetches favorite address list and shows a dialog to select the origin address
	 */
	private void fetchFavAddrListForOrigin() {
		User currentUser = User.getCurrentUser(this);
		new FavoriteAddressFetchTask(true).execute(currentUser.getId());
	}
	
	/**
	 * Fetches favorite address list and shows a dialog to select the destination address
	 */
	private void fetchFavAddrListForDest() {
		User currentUser = User.getCurrentUser(this);
		new FavoriteAddressFetchTask(false).execute(currentUser.getId());
	}
	
	private void showFavAddrListForOrigin() {
		if (favoriteAddresses == null) {
			fetchFavAddrListForOrigin();
		}
		else {
			showFavAddrListForOrigin(favoriteAddresses);
		}
	}
	
	private void showFavAddrListForOrigin(List<Address> listItems) {
		FavoriteAddressListDialog dialog = new FavoriteAddressListDialog(HomeActivity.this, listItems);
		dialog.setActionListener(new FavoriteAddressListDialog.ActionListener() {
			
			@Override
			public void onClickNegativeButton() {
			}
			
			@Override
			public void onClickListItem(Address item, int position) {
				setOriginAddress(item);
			}
		});
		dialog.show();
	}
	
	private void showFavAddrListForDest() {
		if (favoriteAddresses == null) {
			fetchFavAddrListForDest();
		}
		else {
			showFavAddrListForDest(favoriteAddresses);
		}
	}
	
	private void showFavAddrListForDest(List<Address> listItems) {
		FavoriteAddressListDialog dialog = new FavoriteAddressListDialog(HomeActivity.this, listItems);
		dialog.setActionListener(new FavoriteAddressListDialog.ActionListener() {
			
			@Override
			public void onClickNegativeButton() {
			}
			
			@Override
			public void onClickListItem(Address item, int position) {
				setDestinationAddress(item);
			}
		});
		dialog.show();
	}
	
	private void prepareMapActivity() {
		if (originBox.isCurrentLocationInUse()) {
			final ProgressDialog dialog = new ProgressDialog(this);
	        dialog.setMessage("Acquiring current location...");
	        dialog.setIndeterminate(true);
	        dialog.setCancelable(false);
	        dialog.show();
			
			LocationService locationService = LocationService.getInstance(this);
			locationService.requestCurrentLocation(new LocationServiceListener() {
				@Override
				public void locationAcquired(Location location) {
					originCoord = new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6));
					dialog.cancel();
					
					String dest = getDestinationAddress();
					if (dest.equals("")) {
						ehs.reportException("Destination address cannot be empty.");
					}
					else {
						new GeocodingTask(ehs, destGeocodingTaskCallback).execute(dest);
					}
				}
			});
		}
		else {
			String origin = getOriginAddress();
			String destination = getDestinationAddress();
			
			if (origin.equals("")) {
				ehs.reportException("Origin address cannot be empty.");
			}
			else if (destination.equals("")) {
				ehs.reportException("Destination address cannot be empty.");
			}
			else {
				new GeocodingTask(ehs, originGeocodingTaskCallback).execute(origin);
			}
		}
	}
	
	/**
	 * This private helper method will bundle the possible routes as well as the
	 * user information to be be passed to the Map_Activity. The Route are
	 * placed in the bundle individually because I was having some trouble
	 * placing The entire array of Routes on the bundle and reading the in the
	 * Map_Activity. Each Route's toString method is called to log all of the
	 * Route's information to the Debug screen.
	 * 
	 * @author Tim
	 */
	private void startMapActivity() {
		Intent intent = new Intent(this, RouteActivity.class);
		
		Bundle extras = new Bundle();
		extras.putString("originAddr", originBox.getText().toString());
		extras.putString("destAddr", destBox.getText().toString());
		// TODO: Any better way to handle this?
		extras.putInt("originLat", originCoord.getLatitudeE6());
		extras.putInt("originLng", originCoord.getLongitudeE6());
		extras.putInt("destLat", destCoord.getLatitudeE6());
		extras.putInt("destLng", destCoord.getLongitudeE6());
		extras.putBoolean("debugMode", debugMode);
		intent.putExtras(extras);
		startActivity(intent);
	}
	
	private void setOriginAddress(String address) {
		originBox.unsetAddress();
		originBox.setText(address);
	}
	
	private void setOriginAddress(Address address) {
		originBox.unsetAddress();
		originBox.setAddress(address);
	}
	
	private void setDestinationAddress(String address) {
		destBox.unsetAddress();
		destBox.setText(address);
	}
	
	private void setDestinationAddress(Address address) {
		destBox.unsetAddress();
		destBox.setAddress(address);
	}
	
	private class FavoriteAddressFetchTask extends AsyncTask<Integer, Object, List<Address>> {
		
		private ProgressDialog dialog;
		
		/**
		 * If isForOrigin is true, {@code showFavAddrListForOrigin()} will be
		 * called. Otherwise, {@code showFavAddrListForDest()} will be called.
		 * However, this is a temporary solution and must be replace with more
		 * robust solution in the near future.
		 */
		private boolean isForOrigin;
		
		public FavoriteAddressFetchTask(boolean isForOrigin) {
			super();
			this.isForOrigin = isForOrigin;
		}
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(HomeActivity.this);
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
				favoriteAddresses = mapper.getAddresses(uid);
			}
			catch (JSONException e) {
				ehs.registerException(e);
			}
			catch (IOException e) {
				ehs.registerException(e);
			}

			return favoriteAddresses;
		}

		@Override
		protected void onPostExecute(List<Address> result) {
			dialog.cancel();

			if (ehs.hasExceptions()) {
				ehs.reportExceptions();
			}
			else {
				if (isForOrigin) {
					showFavAddrListForOrigin(result);
				}
				else {
					showFavAddrListForDest(result);
				}
			}
		}
	}
}
