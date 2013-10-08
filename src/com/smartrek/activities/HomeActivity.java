package com.smartrek.activities;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.dialogs.FavoriteAddressEditDialog;
import com.smartrek.dialogs.FavoriteAddressListDialog;
import com.smartrek.dialogs.FloatingMenuDialog;
import com.smartrek.dialogs.TripEditDialog;
import com.smartrek.dialogs.TripListDialog;
import com.smartrek.models.Address;
import com.smartrek.models.Reservation;
import com.smartrek.models.Trip;
import com.smartrek.models.User;
import com.smartrek.receivers.ReservationReceiver;
import com.smartrek.requests.AddressLinkRequest;
import com.smartrek.requests.FavoriteAddressFetchRequest;
import com.smartrek.requests.FavoriteAddressUpdateRequest;
import com.smartrek.requests.Request;
import com.smartrek.requests.ReservationListFetchRequest;
import com.smartrek.requests.UpdateDeviceIdRequest;
import com.smartrek.tasks.GeocodingTask;
import com.smartrek.tasks.GeocodingTaskCallback;
import com.smartrek.ui.EditAddress;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.GeoPoint;
import com.smartrek.utils.Misc;
import com.smartrek.utils.Preferences;
import com.smartrek.utils.SystemService;

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
public final class HomeActivity extends ActionBarActivity implements TextWatcher {
    
    public static final String INIT = "init";
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private EditAddress editAddressOrigin;
	private EditAddress editAddressDest;
	
	private Button buttonLoadTrip;
	private Button buttonSaveTrip;
	private Button buttonDone;
	private ImageButton buttonFavAddrOrigin;
	private ImageButton destFavButton;
	private Button buttonOriginMyLocation;
	
	private Time current;
	
	LocationManager locationManager;
	
	LocationListener locationListener;
	
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
	    editAddressOrigin.addTextChangedListener(this);
	    editAddressOrigin.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(editAddressOrigin.isCurrentLocationInUse() && hasFocus){
                    editAddressOrigin.unsetAddress();
                }
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
	    editAddressDest.addTextChangedListener(this);
	    
	    editAddressOrigin.setAddressAsCurrentLocation();
	    
	    //dateBox = (EditText) findViewById(R.id.date_box);
	
		current = new Time();
		current.setToNow();
		//dateBox.setText(current.month + " / " + current.monthDay + " / " + current.year);
	    
	    /***************End EditText Fields********************/
	    
	    /***************Start TextViews********************/
	    
	    // Instantiate TextViews from file main.xml
	    //dateText = (TextView) findViewById(R.id.date_text);
	    
	    // Declare a tiny animation to be used on startup.
	    Animation animation = new TranslateAnimation(-400,0,0,0);
		animation.setDuration(1500);
		
		// Set animation to be used by TextViews.
	    //dateText.setAnimation(animation);  
	    
	    /***************End TextViews********************/
	    
	    /***************Start Buttons********************/
	    
	    // Instantiate Buttons from file main.xml
	    buttonFavAddrOrigin = (ImageButton) findViewById(R.id.Favs1);
	    destFavButton = (ImageButton) findViewById(R.id.Favs2);
	    buttonLoadTrip = (Button) findViewById(R.id.button_load_trip);
	    buttonSaveTrip = (Button) findViewById(R.id.button_save_trip);
	    buttonDone = (Button) findViewById(R.id.Done);
	    
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
				//prepareMapActivity();
				startMapActivity();
			}
	    	
	    });
	    buttonDone.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
			    if(!Request.NEW_API){
    				debugMode = true;
    				setOriginAddress("origin");
    				setDestinationAddress("destination");
    				//prepareMapActivity();
    				startMapActivity();
			    }
				return true;
			}
			
		});
	    
	    ImageButton reverseButton = (ImageButton) findViewById(R.id.reverse_button);
	    reverseButton.setEnabled(false);
	    reverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Address oAddress = getOriginAddress();
                setOriginAddress(getDestinationAddress());
                setDestinationAddress(oAddress);
            }
        });
	    
	    buttonFavAddrOrigin.setId(1);
	    destFavButton.setId(2);
	    buttonDone.setId(3);
	    
	    buttonOriginMyLocation = (Button) findViewById(R.id.origin_my_location);
	    buttonOriginMyLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	        	editAddressOrigin.setAddressAsCurrentLocation();
	        	editAddressOrigin.clearFocus();
			}
	    });
	    
	    findViewById(R.id.floating_menu_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingMenuDialog dialog = new FloatingMenuDialog(HomeActivity.this);
                dialog.show();
            }
        });

	    if(getIntent().getBooleanExtra(INIT, false)){
	        //new NotificationTask().execute(User.getCurrentUser(this));
	        updateAllFavAddrLatLon();
	        if(Request.NEW_API){
	            updateDeviceId(); 
	        }
	    }
	    
	    final View activityRootView = findViewById(android.R.id.content);
	    activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
	        @Override
	        public void onGlobalLayout() {
	            Rect r = new Rect();
	            activityRootView.getWindowVisibleDisplayFrame(r);
	            int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
	            findViewById(R.id.floating_menu_button).setVisibility(heightDiff > 100?View.INVISIBLE:View.VISIBLE);
	         }
	    });
	    
	   Font.setTypeface(boldFont, buttonDone, buttonLoadTrip, buttonSaveTrip,
           buttonOriginMyLocation);
	   Font.setTypeface(lightFont, editAddressDest, editAddressOrigin);
	}
	
	private void updateDeviceId(){
	    SharedPreferences globalPrefs = Preferences.getGlobalPreferences(this);
        final String gcmRegistrationId = globalPrefs.getString(Preferences.Global.GCM_REG_ID, "");
        final User currentUser = User.getCurrentUser(HomeActivity.this);
        if(!gcmRegistrationId.equals(currentUser.getDeviceId())){
            currentUser.setDeviceId(gcmRegistrationId);
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        new UpdateDeviceIdRequest().execute(currentUser.getId(), gcmRegistrationId,
                            currentUser.getUsername(), currentUser.getPassword());
                    }
                    catch (Exception e) {}
                    return null;
                }
            };
            Misc.parallelExecute(task);
        }
	}
	
	private void updateAllFavAddrLatLon(){
	    new AsyncTask<Void, Void, List<Address>>(){
	        @Override
	        protected List<Address> doInBackground(Void... params) {
	            User currentUser = User.getCurrentUser(HomeActivity.this);
	            FavoriteAddressFetchRequest req = new FavoriteAddressFetchRequest(currentUser);
	            req.invalidateCache(HomeActivity.this);
	            List<Address> addresses;
	            try {
	                addresses = req.execute(HomeActivity.this);
                }
                catch (Exception e) {
                    ehs.registerException(e);
                    addresses = Collections.emptyList();
                }
	            return addresses;
	        }
	        @Override
	        protected void onPostExecute(List<Address> addresses) {
	            boolean isPatched = DebugOptionsActivity.isGoogleGeocodingPatched(HomeActivity.this);
	            for (final Address address : addresses) {
	                if(!isPatched || (address.getLatitude() == 0 && address.getLongitude() == 0)){
	                    final String addressStr = address.getAddress();
                        GeocodingTask gTask = new GeocodingTask(ehs, new GeocodingTaskCallback() {
	                        @Override
	                        public void preCallback() {}
	                        @Override
	                        public void postCallback() {
                                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        User user = User.getCurrentUser(HomeActivity.this);
                                        try {
                                            FavoriteAddressUpdateRequest request = new FavoriteAddressUpdateRequest(
                                                new AddressLinkRequest(user).execute(HomeActivity.this),
                                                address.getId(),
                                                user,
                                                address.getName(),
                                                addressStr,
                                                address.getLatitude(),
                                                address.getLongitude());
                                            request.execute();
                                        }
                                        catch (Exception e) {
                                            ehs.registerException(e);
                                        }
                                        return null;
                                    }
                                };
                                Misc.parallelExecute(task);
	                        }
	                        @Override
	                        public void callback(List<com.smartrek.utils.Geocoding.Address> addresses) {
	                            GeoPoint geoPoint = addresses.get(0).getGeoPoint();
	                            address.setLatitude(geoPoint.getLatitude());
                                address.setLongitude(geoPoint.getLongitude());
	                        }
	                    }, false);
                        Misc.parallelExecute(gTask, addressStr);
	                }
                }
	            DebugOptionsActivity.setGoogleGeocodingPatched(HomeActivity.this, true);
	        }
	    }.execute();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
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
     	MenuInflater mi = getSupportMenuInflater();
     	//mi.inflate(R.menu.main, menu);
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
				public void onClickListItem(final Trip trip, int position) {
					setOriginAddress(trip.getOrigin());
					setDestinationAddress(trip.getDestination());
					new AsyncTask<Void, Void, List<Address>>(){
			            @Override
			            protected List<Address> doInBackground(Void... params) {
			                User currentUser = User.getCurrentUser(HomeActivity.this);
			                FavoriteAddressFetchRequest req = new FavoriteAddressFetchRequest(currentUser);
			                List<Address> addresses;
			                try {
			                    addresses = req.execute(HomeActivity.this);
			                }
			                catch (Exception e) {
			                    ehs.registerException(e);
			                    addresses = Collections.emptyList();
			                }
			                return addresses;
			            }
			            @Override
			            protected void onPostExecute(List<Address> addresses) {
			                for (final Address address : addresses) {
			                    int id = address.getId();
			                    if(id == trip.getOriginID()){
			                        setOriginAddress(address);
			                    }else if(id == trip.getDestinationID()){
			                        setDestinationAddress(address);
			                    }
			                }
			            }
			        }.execute();
				}

			});
		}
		tripListDialog.show();
	}
	  
	/**
	 * 
	 * @return Origin address that user has entered
	 */
	private Address getOriginAddress() {
		if (editAddressOrigin.hasAddress()) {
			return editAddressOrigin.getAddress();
		}
		else {
			return getRawOriginAddress();
		}
	}
	
	private Address getRawOriginAddress(){
	    return new Address(0, User.getCurrentUser(this).getId(), "", editAddressOrigin.getText().toString().trim(), 0, 0);
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
			return getRawDestinationAddress();
		}
	}
	
	private Address getRawDestinationAddress() {
        return new Address(0, User.getCurrentUser(this).getId(), "", editAddressDest.getText().toString().trim(), 0, 0);
    }
	
	private TripEditDialog dialog;
	
    private void onClickSaveTrip() {
        dialog = new TripEditDialog(this, getOriginAddress(), getDestinationAddress());
        dialog.setActionListener(new TripEditDialog.ActionListener() {
            
            @Override
            public void onClickPositiveButton(String name, Address origin, Address destination) {
            }
            
            @Override
            public void onClickNegativeButton() {
                dialog = null;
            }
            
        });
        dialog.show();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(dialog != null && dialog.isShowing()){
            dialog.resizeButtonText();
        }
        if(tripListDialog != null && tripListDialog.isShowing()){
            tripListDialog.resizeButtonText();
        }
    }
	
	private void onClickButtonFavAddrOrigin(View view) {
		showFavAddrListForOrigin();
	}
	
	private void onClickButtonFavAddrDest(View view) {
		showFavAddrListForDest();
	}
	
	private void showFavAddrListForOrigin() {

		final FavoriteAddressListDialog listDialog = new FavoriteAddressListDialog(HomeActivity.this);
		listDialog.setActionListener(new FavoriteAddressListDialog.ActionListener() {
			
			@Override
			public void onClickNegativeButton() {
			}

			@Override
			public void onClickNeutralButton() {
				final FavoriteAddressEditDialog editDialog = new FavoriteAddressEditDialog(HomeActivity.this);
				editDialog.setAddress(getRawOriginAddress());
				editDialog.setActionListener(new FavoriteAddressEditDialog.ActionListener() {
					
					@Override
					public void onClickPositiveButton() {
						updateAddress(editAddressOrigin, editDialog.getAddressObject());
						listDialog.requestRefresh();
					}
					
					@Override
					public void onClickNegativeButton() {
					}
				});
				editDialog.show();
			}
			
			@Override
			public void onClickListItem(Address item, int position) {
				setOriginAddress(item);
				updateSaveTripButtonState();
			}

		});
		listDialog.show();
	}
	
	private void showFavAddrListForDest() {
		FavoriteAddressListDialog dialog = new FavoriteAddressListDialog(HomeActivity.this);
		dialog.setActionListener(new FavoriteAddressListDialog.ActionListener() {
			
			@Override
			public void onClickNegativeButton() {
			}
			
			@Override
			public void onClickNeutralButton() {
				final FavoriteAddressEditDialog dialog = new FavoriteAddressEditDialog(HomeActivity.this);
				dialog.setAddress(getRawDestinationAddress());
				dialog.setActionListener(new FavoriteAddressEditDialog.ActionListener() {
					
					@Override
					public void onClickPositiveButton() {
						updateAddress(editAddressDest, dialog.getAddressObject());
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
	    boolean proceed;
	    Intent intent = new Intent(this, RouteActivity.class);
	    if(editAddressOrigin.isCurrentLocationInUse()){
	        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
	            SystemService.alertNoGPS(this);
	            proceed = false;
	        }else{
	            intent.putExtra(RouteActivity.CURRENT_LOCATION, true);
	            proceed = true;
	        }
	    }else{
	        proceed = true;
	    }
	    if(proceed){
    	    Bundle extras = new Bundle();
            extras.putString("originAddr", editAddressOrigin.getText().toString());
            Address originAddr = getOriginAddress();
            extras.putParcelable(RouteActivity.ORIGIN_COORD, 
                new GeoPoint(originAddr.getLatitude(), originAddr.getLongitude()));
            extras.putString("destAddr", editAddressDest.getText().toString());
            Address destAddr = getDestinationAddress();
            extras.putParcelable(RouteActivity.DEST_COORD, 
                new GeoPoint(destAddr.getLatitude(), destAddr.getLongitude()));
            extras.putBoolean("debugMode", debugMode);
            intent.putExtras(extras);
            startActivity(intent);
	    }
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
		
		intent.putExtra(ReservationReceiver.RESERVATION, reservation);
		intent.putExtra("route", reservation.getRoute());
		
		// In reality, you would want to have a static variable for the
		// request code instead of 192837
		PendingIntent pendingOperation = PendingIntent.getBroadcast(this, 192837,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, reservation.getDepartureTime() - 60000*5, pendingOperation); // 5 min earlier than departure time
	}
	
	private class NotificationTask extends AsyncTask<Object, Object, List<Reservation>> {
		private ProgressDialog dialog;
		
		public NotificationTask() {
			super();
			
			dialog = new ProgressDialog(HomeActivity.this);
			dialog.setTitle("Smartrek");
			dialog.setMessage("Fetching existing reservations...");
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
		}
		
		@Override
		protected void onPreExecute() {
			dialog.show();
		}
		
		@Override
		protected List<Reservation> doInBackground(Object... params) {
			User user = (User) params[0];
			
			ReservationListFetchRequest request = new ReservationListFetchRequest(user);
			List<Reservation> reservations = null;
			try {
				reservations = request.execute(HomeActivity.this);
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
			if (ehs.hasExceptions()) {
                ehs.reportExceptions();
            }
            else if(result != null){
    			for (Reservation r : result) {
    				registerNotification(r);
    			}
			}
		}
	}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(buttonDone != null && editAddressOrigin != null 
                && editAddressDest != null){
            buttonDone.setEnabled(StringUtils.isNotBlank(editAddressOrigin.getText()) 
                && StringUtils.isNotBlank(editAddressDest.getText()));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        ImageButton reverseBtn = (ImageButton) findViewById(R.id.reverse_button);
        reverseBtn.setEnabled(StringUtils.isNotBlank(editAddressOrigin.getText()) 
            || StringUtils.isNotBlank(editAddressDest.getText()));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeLocationUpdates();
    }
    
    private void removeLocationUpdates(){
        if(locationManager != null && locationListener != null){
            locationManager.removeUpdates(locationListener); 
        }
    }
    
}
