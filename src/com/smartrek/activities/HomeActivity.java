package com.smartrek.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.smartrek.adapters.FavoriteAddressAdapter;
import com.smartrek.dialogs.FavoriteAddressAddDialog;
import com.smartrek.models.Address;
import com.smartrek.models.User;
import com.smartrek.tasks.GeocodingTask;
import com.smartrek.tasks.GeocodingTaskCallback;
import com.smartrek.ui.EditAddress;
import com.smartrek.ui.MainMenu;
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
 * 
 * @category Smartrek Activity
 * 
 * @version 1.0
 * 
 */
public final class HomeActivity extends Activity {
    
	private static final int FAV_ADDR_ORIGIN = 1;
	private static final int FAV_ADDR_DEST = 2;
	
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private RelativeLayout RL;
	private RelativeLayout section1;
	private RelativeLayout section2;
	private RelativeLayout section3;
	private RelativeLayout section4;
	
	private ScrollView SV;
	
	private EditAddress originBox;
	private EditText destBox;
	private EditText dateBox;
	
	private TextView originText;
	private TextView destText;
	private TextView dateText;
	
	private ListView originFavs;
	private ListView destFavs;
	
	private Button doneButton;
	private ImageButton buttonFavAddrOrigin;
	private ImageButton destFavButton;
	private Button loadButton;
	private Button hereButton;
	
	private int selected;
	
	private GeoPoint originCoord;
	private GeoPoint destCoord;
	
	private Time current;
	
	/**
	 * Indicates if we want to start {@code RouteActivity} in a debug mode.
	 */
	private boolean debugMode;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        selected = -1;
        
        RL = (RelativeLayout) findViewById(R.id.RL2);
        
        SV = (ScrollView) findViewById(R.id.SV);
        
        section1 = (RelativeLayout) findViewById(R.id.Section1);
        section2 = (RelativeLayout) findViewById(R.id.Section2);
        section3 = (RelativeLayout) findViewById(R.id.Section3);
        section4 = (RelativeLayout) findViewById(R.id.Section4);
        
        /***************Start EditText Fields********************/
        
        originBox = (EditAddress) findViewById(R.id.origin_box);
        destBox = (EditText) findViewById(R.id.destination_box);
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
        
        originFavs = (ListView) findViewById(R.id.originFavs);
        //originFavs.setAdapter(new FavoriteAddressAdapter(HomeActivity.this, null));
        
        originFavs.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FavoriteAddressAdapter adapter = (FavoriteAddressAdapter) parent.getAdapter();
				Address item = (Address) adapter.getItem(position);
				
				originBox.unsetAddress();
				originBox.setText(item.getAddress());
			}
		});
        
		destFavs = (ListView) findViewById(R.id.destFavs);
		destFavs.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FavoriteAddressAdapter adapter = (FavoriteAddressAdapter) parent.getAdapter();
				Address item = (Address) adapter.getItem(position);
				
				destBox.setText(item.getAddress());
			}
		});
		
		User currentUser = User.getCurrentUser(this);
		
        /***************Start Buttons********************/
        
        // Instantiate Buttons from file main.xml
        buttonFavAddrOrigin = (ImageButton) findViewById(R.id.Favs1);
        destFavButton = (ImageButton) findViewById(R.id.Favs2);
        doneButton = (Button) findViewById(R.id.Done);
        //loadButton = (Button) findViewById(R.id.Load);
        
        // Set Button OnClickListerners to be declared by
        // this class     
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
        //loadButton.setOnClickListener(this);
        
        buttonFavAddrOrigin.setId(1);
        destFavButton.setId(2);
        doneButton.setId(3);
        //loadButton.setId(4);
        
        hereButton = (Button) findViewById(R.id.hereAndNow);
        hereButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				originBox.setAddressAsCurrentLocation();
			}
        });
        
        
        /***************End Buttons********************/
    }
	
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
		String origin = getOriginAddress();
		
		if (origin.equals("") || originBox.isCurrentLocationInUse()) {
			showFavAddrListForOrigin();
		}
		else {
			FavoriteAddressAddDialog dialog = new FavoriteAddressAddDialog(this);
			dialog.setAddress(origin);
			dialog.setOnClickListener(new FavoriteAddressAddDialog.OnClickListener() {
				
				@Override
				public void onClickPositiveButton() {
				}
				
				@Override
				public void onClickNegativeButton() {
					showFavAddrListForOrigin();
				}
			});
			dialog.show();
		}
		
	}
	
	private void onClickButtonFavAddrDest(View view) {
		String destination = getDestinationAddress();
		
		if (destination.equals("")) {
			showFavAddrListForDest();
		}
		else {
			FavoriteAddressAddDialog dialog = new FavoriteAddressAddDialog(this);
			dialog.setAddress(destination);
			dialog.setOnClickListener(new FavoriteAddressAddDialog.OnClickListener() {
				
				@Override
				public void onClickPositiveButton() {
				}
				
				@Override
				public void onClickNegativeButton() {
					showFavAddrListForDest();
				}
			});
			dialog.show();
		}
	}
	
	private void showFavAddrListForOrigin() {
		Intent intent = new Intent(HomeActivity.this, FavoriteAddressListActivity.class);
		startActivityForResult(intent, FAV_ADDR_ORIGIN);
	}
	
	private void showFavAddrListForDest() {
		Intent intent = new Intent(HomeActivity.this, FavoriteAddressListActivity.class);
		startActivityForResult(intent, FAV_ADDR_DEST);
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
	
	/****************************************************************************************************************
	 * ******************************** private void startMapActivity()**********************************************
	 * 
	 * This private helper method will bundle the possible routes as well as the user information to be be passed
	 * to the Map_Activity. The Route are placed in the bundle individually because I was having some trouble placing
	 * The entire array of Routes on the bundle and reading the in the Map_Activity. Each Route's toString method is
	 * called to log all of the Route's information to the Debug screen.
	 * 
	 ****************************************************************************************************************/
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
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch (requestCode) {
		case FAV_ADDR_ORIGIN:
			if (resultCode == Activity.RESULT_OK) {
				String address = intent.getStringExtra("address");
				setOriginAddress(address);
			}
			break;
			
		case FAV_ADDR_DEST:
			if (resultCode == Activity.RESULT_OK) {
				String address = intent.getStringExtra("address");
				setDestinationAddress(address);
			}
			break;
		}
	}
	
	private void setOriginAddress(String address) {
		originBox.unsetAddress();
		originBox.setText(address);
	}
	
	private void setDestinationAddress(String address) {
		destBox.setText(address);
	}
}
