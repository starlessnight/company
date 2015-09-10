package com.metropia.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.localytics.android.Localytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.activities.LandingActivity2.PoiOverlayInfo;
import com.metropia.models.Address;
import com.metropia.models.FavoriteIcon;
import com.metropia.models.User;
import com.metropia.requests.FavoriteAddressFetchRequest;
import com.metropia.ui.animation.ClickAnimation;
import com.metropia.ui.animation.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.utils.Dimension;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;
import com.metropia.utils.RouteNode;

public class FavoriteListActivity extends FragmentActivity {

	private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	public static final String FAVORITE_LIST = "favCollections";
	
	private static final String ADD_NEW = "+ Add New...";
	
	private ListView favoriteListView;
    private ArrayAdapter<Address> favoriteAdapter;
    private LocationInfo userLoc;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorite_list);
		
		// Integrate Localytics
		Localytics.integrate(this);

		favoriteListView = (ListView) findViewById(R.id.favorite_list);
		favoriteAdapter = new ArrayAdapter<Address>(FavoriteListActivity.this, R.layout.favorite_list_view, R.id.name) {
        	@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		View view = super.getView(position, convertView, parent);
                final Address item = getItem(position);
                View favoriteInfoPanel = view.findViewById(R.id.favorite_info_panel);
                View namePanel = view.findViewById(R.id.name_panel);
                View editView = view.findViewById(R.id.edit_favorite);
                TextView name = (TextView) view.findViewById(R.id.name);
                boolean addNew = ADD_NEW.equals(item.getName()) && item.getId() == 0;
                name.setText(addNew ? formatAddNew(item.getName()) : item.getName());
                name.setTextColor(addNew ? getResources().getColor(R.color.metropia_blue) : getResources().getColor(android.R.color.black));
                TextView address = (TextView) view.findViewById(R.id.address);
                address.setText(item.getAddress());
                ImageView favIcon = (ImageView) view.findViewById(R.id.fav_icon);
                
                FavoriteIcon icon = FavoriteIcon.fromName(item.getIconName(), null);
                if(icon == null) {
                	favIcon.setImageBitmap(Misc.getBitmap(FavoriteListActivity.this, R.drawable.poi_pin, 1));
                	favIcon.setVisibility(View.VISIBLE);
                }
                else {
                	favIcon.setImageBitmap(Misc.getBitmap(FavoriteListActivity.this, icon.getFavoritePageResourceId(FavoriteListActivity.this), 2));
                	favIcon.setVisibility(View.VISIBLE);
                }
                
                favIcon.setVisibility(addNew ? View.GONE : View.VISIBLE);
                editView.setVisibility(addNew ? View.GONE : View.VISIBLE);
                
                Font.setTypeface(Font.getRegular(getAssets()), name, address);
                favoriteInfoPanel.requestLayout();
                namePanel.requestLayout();
                name.requestLayout();
                address.requestLayout();
                favIcon.requestLayout();
                editView.requestLayout();
                int leftRightPadding = Dimension.dpToPx(10, getResources().getDisplayMetrics());
                int topBottomPadding = Dimension.dpToPx(2, getResources().getDisplayMetrics());
                view.setPadding(leftRightPadding, topBottomPadding, leftRightPadding, topBottomPadding);
                return view;
        	}
        };
        favoriteListView.setAdapter(favoriteAdapter);
        
        favoriteListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Address selected = (Address)parent.getItemAtPosition(position);
				if(selected.getId() == 0 && ADD_NEW.equals(selected.getName())) {
					Misc.suppressTripInfoPanel(FavoriteListActivity.this);
					Intent addIntent = new Intent(FavoriteListActivity.this, FavoriteOperationActivity.class);
					addIntent.putExtra(FavoriteOperationActivity.FROM_LIST, true);
					PoiOverlayInfo info = PoiOverlayInfo.fromAddress(FavoriteListActivity.this, selected);
					info.label = "";
					addIntent.putExtra(FavoriteOperationActivity.FAVORITE_POI_INFO, info);
					startActivity(addIntent);
				}
				else {
					Misc.suppressTripInfoPanel(FavoriteListActivity.this);
					Intent editIntent = new Intent(FavoriteListActivity.this, FavoriteOperationActivity.class);
					editIntent.putExtra(FavoriteOperationActivity.FROM_LIST, true);
					editIntent.putExtra(FavoriteOperationActivity.FAVORITE_POI_INFO, PoiOverlayInfo.fromAddress(FavoriteListActivity.this, selected));
					startActivity(editIntent);
				}
			}
        });
        
        TextView backButton = (TextView) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(FavoriteListActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						finish();
					}
				});
			}
		});
		
		userLoc = new LocationInfo(FavoriteListActivity.this);
		
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			List<Address> favs = extras.getParcelableArrayList(FAVORITE_LIST);
			if(favs != null) {
				updateFavoriteListView(favs);
			}
		}
		
		Font.setTypeface(Font.getRegular(getAssets()), backButton, (TextView) findViewById(R.id.header));
        
        ((SmarTrekApplication)getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	private SpannableString formatAddNew(String name) {
		SpannableString addNewSpan = SpannableString.valueOf(name);
		addNewSpan.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.medium_font)), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return addNewSpan;
    } 

	private void refreshFavorites() {
		AsyncTask<Void, Void, List<Address>> task = new AsyncTask<Void, Void, List<Address>>() {
			@Override
			protected List<Address> doInBackground(Void... params) {
				List<Address> addrs = Collections.emptyList();
				FavoriteAddressFetchRequest request = new FavoriteAddressFetchRequest(User.getCurrentUser(FavoriteListActivity.this));
				try {
					request.invalidateCache(FavoriteListActivity.this);
					addrs = request.execute(FavoriteListActivity.this);
				} catch (Exception e) {
					 ehs.registerException(e, "[" + request.getURL() + "]\n" + e.getMessage());
				}
				return addrs;
			}

			@Override
			protected void onPostExecute(List<Address> result) {
				if (ehs.hasExceptions()) {
					 ehs.reportExceptions();
				} else {
					updateFavoriteListView(result);
				}
			}
		};
		Misc.parallelExecute(task);
	}
	
	private void updateFavoriteListView(List<Address> favorites) {
		List<Address> newFavorites = new ArrayList<Address>();
		if(favorites.size() > 0) {
    		List<Address> homeFavorite = new ArrayList<Address>();
    		List<Address> workFavorite = new ArrayList<Address>();
    		List<Address> otherFavorite = new ArrayList<Address>();
    		for(Address addr : favorites) {
    			if(userLoc != null) {
    				addr.setDistance(RouteNode.distanceBetween(addr.getLatitude(), addr.getLongitude(), userLoc.lastLat, userLoc.lastLong));
    			}
    			
    			if(FavoriteIcon.home.name().equals(addr.getIconName())) {
    				homeFavorite.add(addr);
    			}
    			else if(FavoriteIcon.work.name().equals(addr.getIconName())) {
    				workFavorite.add(addr);
    			}
    			else {
    				otherFavorite.add(addr);
    			}
    		}
    		
    		Comparator<Address> comparator = new Comparator<Address>() {
				@Override
				public int compare(Address lhs, Address rhs) {
					return Double.valueOf(lhs.getDistance()).compareTo(Double.valueOf(rhs.getDistance()));
				}
    		};
    		
    		Collections.sort(homeFavorite, comparator);
    		Collections.sort(workFavorite, comparator);
    		Collections.sort(otherFavorite, comparator);
    		newFavorites.addAll(homeFavorite);
    		newFavorites.addAll(workFavorite);
    		newFavorites.addAll(otherFavorite);
    	}
		// add new
		Address addNew = new Address();
		addNew.setName(ADD_NEW);
		newFavorites.add(addNew);

		favoriteAdapter.clear();
		favoriteAdapter.addAll(newFavorites);
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    Localytics.openSession();
	    Localytics.tagScreen(this.getClass().getSimpleName());
	    Localytics.upload();
	    Localytics.setInAppMessageDisplayActivity(this);
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
	    refreshFavorites();
	}
	
	@Override
	public void onPause() {
	    Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
	    super.onPause();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
        Misc.tripInfoPanelOnActivityStop(this);
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();
	    Misc.tripInfoPanelOnActivityRestart(this);
	}

}
