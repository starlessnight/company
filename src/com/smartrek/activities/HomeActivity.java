package com.smartrek.activities;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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

import com.google.android.maps.GeoPoint;
import com.smartrek.adapters.FavoriteAddressAdapter;
import com.smartrek.mappers.FavoriteAddressMapper;
import com.smartrek.models.Address;
import com.smartrek.models.User;
import com.smartrek.tasks.GeocodingTask;
import com.smartrek.tasks.GeocodingTaskCallback;
import com.smartrek.ui.EditAddress;
import com.smartrek.ui.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.LocationService;
import com.smartrek.utils.LocationService.LocationServiceListener;

/****************************************************************************************************************
 * ******************************* public class Home_Activity ***************************************************
 * This Activity is the home screen for the Smartrek Application. From this screen the user can enter their
 * origin, destination, and trip date. Or the user can access their favorite locations or load a previously 
 * reserved trip. 
 * 
 * This class will communicate with the Smartrek server to down load user favorites and previously reserved
 * trips. Also this class will query the server for Route information given the user input.
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
 ****************************************************************************************************************/
public final class HomeActivity extends Activity implements OnClickListener, OnTouchListener {
    
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
	private ImageButton originFavButton;
	private ImageButton destFavButton;
	private Button loadButton;
	private Button hereButton;
	
	private int selected;
	private int SELECT_O_FAVS = 1;
	private int SELECT_D_FAVS = 2;
	private int SELECT_GO = 3;
	private int SELECT_LOAD = 4;
	
//	private String origin = "";
//	private String destination = "";
	
	private GeoPoint originCoord;
	private GeoPoint destCoord;
	
	private Time current;
	
	/****************************************************************************************************************
	 * ***************************** public void onCreate(Bundle savedInstanceState) ********************************
	 * 
	 * Called when the activity is first created.
	 * 
	 * 
	 ****************************************************************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        selected = -1;
        
        RL = (RelativeLayout) findViewById(R.id.RL2);
        RL.setOnTouchListener(this);
        
        SV = (ScrollView) findViewById(R.id.SV);
        
        section1 = (RelativeLayout) findViewById(R.id.Section1);
        section2 = (RelativeLayout) findViewById(R.id.Section2);
        section3 = (RelativeLayout) findViewById(R.id.Section3);
        section4 = (RelativeLayout) findViewById(R.id.Section4);
        
        /***************Start EditText Fields********************/
        
        originBox = (EditAddress) findViewById(R.id.origin_box);
        destBox = (EditText) findViewById(R.id.destination_box);
        dateBox = (EditText) findViewById(R.id.date_box);

		current = new Time();
		current.setToNow();
		dateBox.setText(current.month + " / " + current.monthDay + " / " + current.year);
        
        /***************End EditText Fields********************/
        
        /***************Start TextViews********************/
        
        // Instantiate TextViews from file main.xml
        originText = (TextView) findViewById(R.id.origin_text);
        destText = (TextView) findViewById(R.id.destination_text);
        dateText = (TextView) findViewById(R.id.date_text);
        
        // Declare a tiny animation to be used on startup.
        Animation animation = new TranslateAnimation(-400,0,0,0);
		animation.setDuration(1500);
		
		// Set animation to be used by TextViews.
        destText.setAnimation(animation);
        originText.setAnimation(animation);
        dateText.setAnimation(animation);  
        
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
				resetAll();
			}
		});
        
		destFavs = (ListView) findViewById(R.id.destFavs);
		destFavs.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FavoriteAddressAdapter adapter = (FavoriteAddressAdapter) parent.getAdapter();
				Address item = (Address) adapter.getItem(position);
				
				destBox.setText(item.getAddress());
				resetAll();
			}
		});
		
		User currentUser = User.getCurrentUser(this);
		
		new FavoriteAddressFetchTask().execute(currentUser.getId());
        
        /***************Start Buttons********************/
        
        // Instantiate Buttons from file main.xml
        originFavButton = (ImageButton) findViewById(R.id.Favs1);
        destFavButton = (ImageButton) findViewById(R.id.Favs2);
        doneButton = (Button) findViewById(R.id.Done);
        //loadButton = (Button) findViewById(R.id.Load);
        
        // Set Button OnClickListerners to be declared by
        // this class     
        originFavButton.setOnClickListener(this);
        destFavButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);
        //loadButton.setOnClickListener(this);
        
        originFavButton.setId(1);
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

	/****************************************************************************************************************
	 * ******************************* public void onClick(View v) ***********************************************
	 * 
	 * 
	 ****************************************************************************************************************/
	@Override
	public void onClick(View v) {
		
		int prev = selected;
		
		if(selected != v.getId()){
			resetAll();
			selected = v.getId();
		}
		
		// Animation that will be fired when 'favs' button on the origin address
		// side is clicked.
		if(selected == SELECT_O_FAVS){
			if(selected != prev) {
				expandSection1(300);
			}
			else {
				resetAll();
			}
		}
		
		if(selected == SELECT_D_FAVS){
			if (selected != prev) {
				expandSection2(300);
			}
			else {
				resetAll();
			}
		}
		
		if(selected == SELECT_GO){
			prepareMapActivity();
		}
		
		if(selected == SELECT_LOAD) {
			
		}
	}

	/******************************************************************************************************************
	 * 
	 *
	 ******************************************************************************************************************/
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		return false;
	}
	
	/****************************************************************************************************************
	 * ******************************** public void onBackedPressed() ***********************************************
	 * 
	 * Override the onBackPressed() method so the user does not return to the Login_Activity after a successful
	 * login. Pressing back from the Home_Activity will quit the application.
	 * 
	 ****************************************************************************************************************/
	@Override
	public void onBackPressed() {
		finish();
	}

	/****************************************************************************************************************
	 * ************************************* private void resetAll() ************************************************
	 * 
	 * Selecting one of the favorites buttons will cause animations to re-shape the layout on the screen. This method
	 * will put elements back in their starting positions.
	 * 
	 ****************************************************************************************************************/
	private void resetAll() {
		
		if(selected == SELECT_O_FAVS){
//			Animation animation = new TranslateAnimation(0,0,300,0);
//			animation.setDuration(1500);
//			animation.setFillAfter(true);
//			
//			section2.setAnimation(animation);
//			section3.setAnimation(animation);
//			doneButton.setAnimation(animation);
			
			expandView(section1, -300);
			pushDownView(section2, -300);
	        pushDownView(section3, -300);
			pushDownView(section4, -300);
		}
		else if(selected == SELECT_D_FAVS) {
			expandView(section2, -300);
			pushDownView(section3, -300);
			pushDownView(section4, -300);
			
			expandView(destFavs, -200);
			expandView(SV, -500);
		}
		selected = -1;
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
				String dest = destBox.getText().toString();
				if (dest.trim().equals("")) {
					ehs.reportException("Destination address cannot be empty.");
				}
				else {
					new GeocodingTask(ehs, destGeocodingTaskCallback).execute(dest);
				}
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
					
					String dest = destBox.getText().toString();
					if (dest.trim().equals("")) {
						ehs.reportException("Destination address cannot be empty.");
					}
					else {
						new GeocodingTask(ehs, destGeocodingTaskCallback).execute(dest);
					}
				}
			});
		}
		else {
			String origin = originBox.getText().toString();
			if (origin.trim().equals("")) {
				ehs.reportException("Origin address cannot be empty.");
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
		intent.putExtras(extras);
		startActivity(intent);
	}
	
	
	/****************************************************************************************************************
	 * 
	 *
	 *
	 ****************************************************************************************************************/
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
	
	
	private void expandView(View view, int delta) {
		view.layout(view.getLeft(), 
		        view.getTop(),
		        view.getRight(), 
		        view.getBottom() + delta);
	}
	
	private void pushDownView(View view, int delta) {
		view.layout(view.getLeft(), 
		        view.getTop() + delta, 
		        view.getRight(), 
		        view.getBottom() + delta);
	}
	
	private void expandSection1(int delta) {
//        Animation animation = new TranslateAnimation(0,0,-300,0);
//        animation.setDuration(1500);
		
        expandView(section1, delta);
        pushDownView(section2, delta);
        pushDownView(section3, delta);
        pushDownView(section4, delta);
        
        expandView(originFavs, delta - 100);
        expandView(SV, delta + 200);
        
// FIXME: No animations for now
//        section2.setAnimation(animation);
//        section3.setAnimation(animation);
//        originFavs.setAnimation(new TranslateAnimation(0, 0, -200, 0));
//        doneButton.setAnimation(animation);
	}
	
	private void expandSection2(int delta) {
        expandView(section2, delta);
        pushDownView(section3, delta);
        pushDownView(section4, delta);
        
        expandView(destFavs, delta - 100);        
        expandView(SV, delta + 200);
	}
	
	private class FavoriteAddressFetchTask extends AsyncTask<Integer, Object, List<Address>> {

	    @Override
	    protected void onPreExecute() {
	        
	    }
	    
		@Override
		protected List<Address> doInBackground(Integer... params) {

			// FIXME: Potential array out of boundary exception
			int uid = params[0];
			
			List<Address> items = null;
			FavoriteAddressMapper mapper = new FavoriteAddressMapper();
			try {
				items = mapper.getAddresses(uid);
			}
			catch (JSONException e) {
				ehs.registerException(e);
			}
			catch (IOException e) {
			    ehs.registerException(e);
			}
			
			return items;
		}
		
		@Override
		protected void onPostExecute(List<Address> result) {
			if(result != null) {
				originFavs.setAdapter(new FavoriteAddressAdapter(HomeActivity.this, result));
				destFavs.setAdapter(new FavoriteAddressAdapter(HomeActivity.this, result));
//				FavoriteAddressAdapter adapter = (FavoriteAddressAdapter) originFavs.getAdapter();
//				adapter.setItems(result);

			}
		}

	}

}
