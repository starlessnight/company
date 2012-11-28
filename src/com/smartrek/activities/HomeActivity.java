package com.smartrek.activities;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.smartrek.dialogs.CancelableProgressDialog;
import com.smartrek.dialogs.FavoriteAddressAddDialog;
import com.smartrek.dialogs.FavoriteAddressListDialog;
import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.dialogs.TripListDialog;
import com.smartrek.dialogs.TripSaveDialog;
import com.smartrek.models.Address;
import com.smartrek.models.Reservation;
import com.smartrek.models.Trip;
import com.smartrek.models.User;
import com.smartrek.receivers.ReservationReceiver;
import com.smartrek.requests.ReservationListFetchRequest;
import com.smartrek.tasks.GeocodingTask;
import com.smartrek.tasks.GeocodingTaskCallback;
import com.smartrek.ui.EditAddress;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.Cache;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Geocoding;
import com.smartrek.utils.LocationService;
import com.smartrek.utils.SystemService;
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
	
	private EditAddress editAddressOrigin;
	private EditAddress editAddressDest;
	private EditText dateBox;
	
	private TextView originText;
	private TextView destText;
	private TextView dateText;
	
	private Button buttonLoadTrip;
	private Button buttonSaveTrip;
	private Button buttonDone;
	private ImageButton buttonSaveTripHelp;
	private ImageButton buttonFavAddrOrigin;
	private ImageButton destFavButton;
	private Button buttonOriginMyLocation;
	
	private GeoPoint originCoord;
	private GeoPoint destCoord;
	
	private Time current;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.home);
	    
	    /***************Start EditText Fields********************/
	    
	    editAddressOrigin = (EditAddress) findViewById(R.id.origin_box);
	    editAddressOrigin.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				updateSaveTripButtonState();
				return false;
			}
	    	
	    });
	    
	    editAddressDest = (EditAddress) findViewById(R.id.destination_box);
	    editAddressDest.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				updateSaveTripButtonState();
				return false;
			}
	    	
	    });
	    
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
	    buttonSaveTripHelp = (ImageButton) findViewById(R.id.button_save_trip_help);
	    buttonFavAddrOrigin = (ImageButton) findViewById(R.id.Favs1);
	    destFavButton = (ImageButton) findViewById(R.id.Favs2);
	    buttonLoadTrip = (Button) findViewById(R.id.button_load_trip);
	    buttonSaveTrip = (Button) findViewById(R.id.button_save_trip);
	    buttonDone = (Button) findViewById(R.id.Done);
	    
	    buttonSaveTripHelp.setOnClickListener(new OnClickListener() {
	    	
			@Override
			public void onClick(View v) {
				Context context = HomeActivity.this;
				NotificationDialog dialog = new NotificationDialog(context, context.getResources().getString(R.string.save_trip_help_message));
				dialog.show();
			}
			
		});
	    
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

        buttonLoadTrip.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onClickLoadTrip();
            }

        });

        buttonSaveTrip.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onClickSaveTrip();
            }

        });

	    buttonDone.setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View v) {
				debugMode = false;
				prepareMapActivity();
			}
	    	
	    });
	    buttonDone.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				debugMode = true;
				if (editAddressOrigin.getText().toString().equals(""))
					editAddressOrigin.setText("origin");
				if (editAddressDest.getText().toString().equals(""))
					editAddressDest.setText("destination");
				prepareMapActivity();
				return true;
			}
			
		});
	    
	    buttonFavAddrOrigin.setId(1);
	    destFavButton.setId(2);
	    buttonDone.setId(3);
	    
	    buttonOriginMyLocation = (Button) findViewById(R.id.origin_my_location);
	    buttonOriginMyLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		        
		        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
		            SystemService.alertNoGPS(HomeActivity.this);
		        }
		        else {
		        	editAddressOrigin.setAddressAsCurrentLocation();
		        }
			}
	    });

	    new NotificationTask().execute(User.getCurrentUser(this).getId());
	}
	
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
     	mi.inflate(R.menu.main, menu);
    	return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		MainMenu.onMenuItemSelected(this, featureId, item);
		
		switch (item.getItemId()) {
//		case R.id.menu_trip_list:
//			onClickLoadTrip();
//			break;
		
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void onClickLoadTrip() {
		if (tripListDialog == null) {
			tripListDialog = new TripListDialog(this);
			tripListDialog.setActionListener(new TripListDialog.ActionListener() {
				
				@Override
				public void onClickNeutralButton() {
				}
				
				@Override
				public void onClickNegativeButton() {
				}
				
				@Override
				public void onClickListItem(Trip trip, int position) {
					setOriginAddress(trip.getOrigin());
					setDestinationAddress(trip.getDestination());
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
			dialog = new CancelableProgressDialog(HomeActivity.this, "Geocoding origin address...");
	        dialog.show();
		}

		@Override
		public void callback(List<Geocoding.Address> addresses) {
			if (addresses.size() == 1) {
				originCoord = addresses.get(0).getGeoPoint();
			}
			else {
				// TODO: Popup a dialog to pick an address
				originCoord = addresses.get(0).getGeoPoint();
			}
		}

		@Override
		public void postCallback() {
			dialog.cancel();
			
			if (ehs.hasExceptions()) {
			    ehs.reportExceptions();
			}
			else {
				new GeocodingTask(ehs, destGeocodingTaskCallback).execute(getDestinationAddress().getAddress());
			}
		}
		
	};
	
	GeocodingTaskCallback destGeocodingTaskCallback = new GeocodingTaskCallback() {

		private ProgressDialog dialog;
		
		@Override
		public void preCallback() {
			dialog = new CancelableProgressDialog(HomeActivity.this, "Geocoding destination address...");
	        dialog.show();
		}

		@Override
		public void callback(List<Geocoding.Address> addresses) {
			if (addresses.size() == 1) {
				destCoord = addresses.get(0).getGeoPoint();
			}
			else {
				// TODO: Popup a dialog to pick an address
				destCoord = addresses.get(0).getGeoPoint();
			}
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
	private Address getOriginAddress() {
		if (editAddressOrigin.hasAddress()) {
			return editAddressOrigin.getAddress();
		}
		else {
			return new Address(0, User.getCurrentUser(this).getId(), "", editAddressOrigin.getText().toString().trim());
		}
	}
	
	/**
	 * 
	 * @return Destination address that user has entered
	 */
	private Address getDestinationAddress() {
		if (editAddressDest.hasAddress()) {
			return editAddressDest.getAddress();
		}
		else {
			return new Address(0, User.getCurrentUser(this).getId(), "", editAddressDest.getText().toString().trim());
		}
	}
	
    private void onClickSaveTrip() {
        TripSaveDialog dialog = new TripSaveDialog(this, getOriginAddress(), getDestinationAddress());
        dialog.setActionListener(new TripSaveDialog.ActionListener() {
            
            @Override
            public void onClickPositiveButton(String name, Address origin, Address destination) {
            }
            
            @Override
            public void onClickNegativeButton() {
            }
            
        });
        dialog.show();
    }
	
	private void onClickButtonFavAddrOrigin(View view) {
		showFavAddrListForOrigin();
	}
	
	private void onClickButtonFavAddrDest(View view) {
		showFavAddrListForDest();
	}
	
	private void showFavAddrListForOrigin() {

		FavoriteAddressListDialog dialog = new FavoriteAddressListDialog(HomeActivity.this);
		dialog.setActionListener(new FavoriteAddressListDialog.ActionListener() {
			
			@Override
			public void onClickNegativeButton() {
			}

			@Override
			public void onClickNeutralButton() {
				FavoriteAddressAddDialog dialog = new FavoriteAddressAddDialog(HomeActivity.this);
				dialog.setAddress(getOriginAddress());
				dialog.setActionListener(new FavoriteAddressAddDialog.ActionListener() {
					
					@Override
					public void onClickPositiveButton() {
						updateAddress(editAddressOrigin, getOriginAddress());
					}
					
					@Override
					public void onClickNegativeButton() {
					}
				});
				dialog.show();
			}
			
			@Override
			public void onClickListItem(Address item, int position) {
				setOriginAddress(item);
				updateSaveTripButtonState();
			}

		});
		dialog.show();
	}
	
	private void showFavAddrListForDest() {
		FavoriteAddressListDialog dialog = new FavoriteAddressListDialog(HomeActivity.this);
		dialog.setActionListener(new FavoriteAddressListDialog.ActionListener() {
			
			@Override
			public void onClickNegativeButton() {
			}
			
			@Override
			public void onClickNeutralButton() {
				FavoriteAddressAddDialog dialog = new FavoriteAddressAddDialog(HomeActivity.this);
				dialog.setAddress(getDestinationAddress());
				dialog.setActionListener(new FavoriteAddressAddDialog.ActionListener() {
					
					@Override
					public void onClickPositiveButton() {
						updateAddress(editAddressDest, getOriginAddress());
					}
					
					@Override
					public void onClickNegativeButton() {
					}
				});
				dialog.show();
			}
			
			@Override
			public void onClickListItem(Address item, int position) {
				setDestinationAddress(item);
				updateSaveTripButtonState();
			}
		});
		dialog.show();
	}
	
	private void updateAddress(EditAddress editAddress, Address address) {
		editAddress.setAddress(address);
	}
	
	private void updateSaveTripButtonState() {
		boolean isReadyToSave = editAddressOrigin.hasAddress() && editAddressDest.hasAddress();
		
		buttonSaveTrip.setEnabled(isReadyToSave);
		buttonSaveTripHelp.setVisibility(isReadyToSave ? View.INVISIBLE : View.VISIBLE);
	}
	
	private void prepareMapActivity() {
		if (editAddressOrigin.isCurrentLocationInUse()) {
			final CancelableProgressDialog dialog = new CancelableProgressDialog(this, "Acquiring current location...");
	        dialog.show();
			
	        // FIXME: When dialog gets canceled, requestCurrentLocation() must be canceled as well.
			LocationService locationService = LocationService.getInstance(this);
			locationService.requestCurrentLocation(new LocationServiceListener() {
				@Override
				public void locationAcquired(Location location) {
					originCoord = new GeoPoint(location.getLatitude(), location.getLongitude());
					dialog.cancel();
					
					String dest = getDestinationAddress().getAddress();
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
			String origin = getOriginAddress().getAddress();
			String destination = getDestinationAddress().getAddress();
			
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
		extras.putString("originAddr", editAddressOrigin.getText().toString());
		extras.putString("destAddr", editAddressDest.getText().toString());
		// TODO: Any better way to handle this?
		extras.putDouble("originLat", originCoord.getLatitude());
		extras.putDouble("originLng", originCoord.getLongitude());
		extras.putDouble("destLat", destCoord.getLatitude());
		extras.putDouble("destLng", destCoord.getLongitude());
		extras.putBoolean("debugMode", debugMode);
		intent.putExtras(extras);
		startActivity(intent);
	}
	
	private void setOriginAddress(String address) {
		editAddressOrigin.unsetAddress();
		editAddressOrigin.setText(address);
	}
	
	private void setOriginAddress(Address address) {
		editAddressOrigin.unsetAddress();
		editAddressOrigin.setAddress(address);
	}
	
	private void setDestinationAddress(String address) {
		editAddressDest.unsetAddress();
		editAddressDest.setText(address);
	}
	
	private void setDestinationAddress(Address address) {
		editAddressDest.unsetAddress();
		editAddressDest.setAddress(address);
	}
	
	private void registerNotification(Reservation reservation) {
		
		Intent intent = new Intent(this, ReservationReceiver.class);
		
		intent.putExtra("reservation", reservation);
		intent.putExtra("route", reservation.getRoute());
		
		// In reality, you would want to have a static variable for the
		// request code instead of 192837
		PendingIntent pendingOperation = PendingIntent.getBroadcast(this, 192837,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, reservation.getDepartureTime() - 60000*5, pendingOperation); // 5 min earlier than departure time

		Cache cache = Cache.getInstance();
		if (cache.has("pendingAlarms")) {
			@SuppressWarnings("unchecked")
			List<PendingIntent> pendingAlarms = (List<PendingIntent>) cache.fetch("pendingAlarms");
			pendingAlarms.add(pendingOperation);
		}
		else {
			List<PendingIntent> pendingOperations = new LinkedList<PendingIntent>();
			pendingOperations.add(pendingOperation);
			
			cache.put("pendingAlarms", pendingOperations);
		}
	}
	
	private class NotificationTask extends AsyncTask<Object, Object, List<Reservation>> {
		private ProgressDialog dialog;
		
		public NotificationTask() {
			super();
			
			dialog = new ProgressDialog(HomeActivity.this);
			dialog.setTitle("Smartrek");
			dialog.setMessage("Fetching existing reservations...");
		}
		
		@Override
		protected void onPreExecute() {
			dialog.show();
		}
		
		@Override
		protected List<Reservation> doInBackground(Object... params) {
			int uid = (Integer) params[0];
			
			ReservationListFetchRequest request = new ReservationListFetchRequest(uid);
			List<Reservation> reservations = null;
			try {
				reservations = request.execute();
			}
			catch (Exception e) {
				ehs.registerException(e);
			}
			
			return reservations;
		}
		
		@Override
		protected void onPostExecute(List<Reservation> result) {
			if (dialog.isShowing()) {
				dialog.cancel();
			}
			
			for (Reservation r : result) {
				registerNotification(r);
			}
		}
	}
}
