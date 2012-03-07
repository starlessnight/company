package smartrek.activities;

import java.util.List;

import org.json.JSONException;

import smartrek.adapters.FavoriteAddressAdapter;
import smartrek.mappers.FavoriteAddressMapper;
import smartrek.models.Address;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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
public class HomeActivity extends Activity implements OnClickListener, OnTouchListener {
	
	private RelativeLayout RL;
	private RelativeLayout section1;
	private RelativeLayout section2;
	private RelativeLayout section3;
	
	private ScrollView SV;
	
	private EditText originBox;
	private EditText destBox;
	private EditText dateBox;
	
	private TextView originText;
	private TextView destText;
	private TextView dateText;
	
	private ListView originFavs;
	private ListView destFavs;
	
	private Button doneButton;
	private Button originFavButton;
	private Button destFavButton;
	private Button loadButton;
	
	private int Selected;
	private int SELECT_O_FAVS = 1;
	private int SELECT_D_FAVS = 2;
	private int SELECT_GO = 3;
	private int SELECT_LOAD = 4;
	
	private final String LOGIN_PREFS = "login_file";
	
	private String origin = "";
	private String destination = "";
	
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
        
        Log.d("Home_Activity","OnCreate Home_Activity");
        
        Selected = -1;
        
        RL = (RelativeLayout) findViewById(R.id.RL2);
        RL.setOnTouchListener(this);
        
        SV = (ScrollView) findViewById(R.id.SV);
        
        section1 = (RelativeLayout) findViewById(R.id.Section1);
        section2 = (RelativeLayout) findViewById(R.id.Section2);
        section3 = (RelativeLayout) findViewById(R.id.Section3);
        
        /***************Start EditText Fields********************/
        
        originBox = (EditText) findViewById(R.id.origin_box);
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
				
				originBox.setText(item.getAddress());
			}
		});
        
        // TODO: Need to implement lazy loading of favorite addresses
		// FIXME: Temporary uid = 10
		new FavoriteAddressFetchTask(originFavs).execute(10);
        
        /***************Start Buttons********************/
        
        // Instantiate Buttons from file main.xml
        originFavButton = (Button) findViewById(R.id.Favs1);
        destFavButton = (Button) findViewById(R.id.Favs2);
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
        
        /***************End Buttons********************/
    }

	/****************************************************************************************************************
	 * ******************************* public void onClick(View v) ***********************************************
	 * 
	 * 
	 ****************************************************************************************************************/
	@Override
	public void onClick(View v) {
		
		int prev = Selected;
		
		if(Selected != v.getId()){
			resetAll();
			Selected = v.getId();
		}
		
		// Animation that will be fired when 'favs' button on the origin address
		// side is clicked.
		if(Selected == SELECT_O_FAVS){
			if(Selected != prev) {
				expandSection1();
			} else {
				resetAll();
				Selected = -1;
			}
		}
		
		if(Selected == SELECT_D_FAVS){
	      
		}
		
		if(Selected == SELECT_GO){
//			origin = "2929 E 6th Street, Tucson, AZ";
//			destination = "3289 E HEMISPHERE Loop, Tucson, AZ";
//			
			//
			//
			//
			// left off here ready to add OD pair to shared preferences
			// for Map Activity
			//
			
//			origin = originBox.getText().toString();
//			destination = destBox.getText().toString();
			
			startMapActivity();
		}
		
		if(Selected == SELECT_LOAD) {
			
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
		
		if(Selected == 1){
//			Animation animation = new TranslateAnimation(0,0,300,0);
//			animation.setDuration(1500);
//			animation.setFillAfter(true);
//			
//			section2.setAnimation(animation);
//			section3.setAnimation(animation);
//			doneButton.setAnimation(animation);
			
	        section1.layout(section1.getLeft(), 
    			    section1.getTop(), 
	                section1.getRight(), 
	                section1.getBottom()-300);
			
			section2.layout(section2.getLeft(), section2.getTop()-300, section2.getRight(), section2.getBottom()-300);
	        section3.layout(section3.getLeft(), section3.getTop()-300, section3.getRight(), section3.getBottom()-300);
	        doneButton.layout(doneButton.getLeft(), doneButton.getTop()-300, doneButton.getRight(), doneButton.getBottom()-300);
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
		origin = originBox.getText().toString();
		destination = destBox.getText().toString();
		
//		origin = "1905 W.Jefferson Street, Phoenix, AZ, 85007";
//		destination = "2825 N.Central Ave,Phoenix,AZ 85012";
		
		// Put in error checking for OD pair here // 
		
		Intent intent = new Intent(this, RouteActivity.class);
		
		Bundle extras = new Bundle();
		extras.putString("origin", origin);
		extras.putString("destination", destination);
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
    	Log.d("Home_Activity","Menu Opened from Map_Activity");
     	MenuInflater mi = getMenuInflater();
     	mi.inflate(R.menu.mapoptions, menu);
    	return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item){
		Log.d("Map_Activity", "Menu Open: Entering Map Mode Options");
		Intent intent = null;
		switch(item.getItemId()){
		case R.id.contacts:
			intent = new Intent(this, ContactsActivity.class);
			startActivity(intent);
			break;

    	case R.id.mycoupons:
    		intent = new Intent(this, MyCouponsActivity.class);
    		startActivity(intent);
    		return true;
    		
    	case R.id.reservation:
    		intent = new Intent(this, ReservationActivity.class);
    		startActivity(intent);
    		break;
    		
    	case R.id.logout_option:
    		// FIXME: Need to refactor
			SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_PREFS,MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString("user", "");
			editor.putInt("uid", -1);
			editor.commit();
			finish();
    		return true;
    	}
		return super.onMenuItemSelected(featureId, item);
	}
	
	
	private void expandSection1() {
		
//        Animation animation = new TranslateAnimation(0,0,-300,0);
//        animation.setDuration(1500);
		
        section1.layout(section1.getLeft(), 
        			    section1.getTop(), 
		                section1.getRight(), 
		                section1.getBottom()+300);
        section2.layout(section2.getLeft(), 
        		        section2.getTop()+300, 
        		        section2.getRight(), 
        		        section2.getBottom()+300);
        section3.layout(section3.getLeft(), 
        		        section3.getTop()+300, 
        		        section3.getRight(),
        		        section3.getBottom()+300);
        doneButton.layout(doneButton.getLeft(),
        		          doneButton.getTop()+300, 
        		          doneButton.getRight(), 
        		          doneButton.getBottom()+300);
		originFavs.layout(originFavs.getLeft(),
				originFavs.getTop(),
				originFavs.getRight(),
				originFavs.getBottom() + 200);
        
        SV.layout(SV.getLeft(), 
        		  SV.getTop(), 
        		  SV.getRight(), 
        		  SV.getBottom()+500);
        
// FIXME: No animations for now
//        section2.setAnimation(animation);
//        section3.setAnimation(animation);
//        originFavs.setAnimation(new TranslateAnimation(0, 0, -200, 0));
//        doneButton.setAnimation(animation);
	}
	
	private class FavoriteAddressFetchTask extends AsyncTask<Integer, Object, List<Address>> {
		
		/**
		 * A list view to be updated when the fetch task is done.
		 */
		private ListView listView;
		
		/**
		 * Default constructor
		 * 
		 * @param listView A list view to be updated when the fetch task is done.
		 */
		public FavoriteAddressFetchTask(ListView listView) {
			super();
			this.listView = listView;
		}

		@Override
		protected List<Address> doInBackground(Integer... params) {

			int uid = params[0];
			
			List<Address> items = null;
			FavoriteAddressMapper mapper = new FavoriteAddressMapper();
			try {
				items = mapper.getAddresses(uid);
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			
			return items;
		}
		
		@Override
		protected void onPostExecute(List<Address> result) {
			if(result != null) {
				listView.setAdapter(new FavoriteAddressAdapter(HomeActivity.this, result));
//				FavoriteAddressAdapter adapter = (FavoriteAddressAdapter) originFavs.getAdapter();
//				adapter.setItems(result);

			}
		}

	}

}
