package com.metropia.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.activities.LandingActivity2.BalloonModel;
import com.metropia.activities.LandingActivity2.PoiOverlayInfo;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.models.FavoriteIcon;
import com.metropia.models.User;
import com.metropia.requests.AddressLinkRequest;
import com.metropia.requests.FavoriteAddressAddRequest;
import com.metropia.requests.FavoriteAddressDeleteRequest;
import com.metropia.requests.FavoriteAddressUpdateRequest;
import com.metropia.requests.Request;
import com.metropia.ui.ClickAnimation;
import com.metropia.ui.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.ui.DelayTextWatcher;
import com.metropia.ui.DelayTextWatcher.TextChangeListener;
import com.metropia.ui.overlays.OverlayCallback;
import com.metropia.ui.overlays.POIOverlay;
import com.metropia.utils.Dimension;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.Geocoding;
import com.metropia.utils.Geocoding.Address;
import com.metropia.utils.Misc;
import com.metropia.utils.SystemService;

public class OnBoardActivity extends FragmentActivity {
	
	private MapView mapView;
	
	private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastLocation;
	
	private View contentPanel;
	private TextView skip;
	private TextView next;
	private TextView back;
	private TextView page1Next;
	private TextView page2Next;
	private TextView page3Next;
	private TextView finish;
	
	private List<Address> searchAddresses = new ArrayList<Address>();
	
	private Integer homeOverlayId;
	private EditText homeSearchBox;
	private ListView homeSearchList;
	private ArrayAdapter<Address> homeSearchListAdapter;
	
	private Integer workOverlayId;
	private EditText workSearchBox;
	private ListView workSearchList;
	private ArrayAdapter<Address> workSearchListAdapter;
	
	private AtomicBoolean showAutoComplete = new AtomicBoolean();
	
    private Typeface mediumFont;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.on_board);
		
		Localytics.integrate(this);
		
		mapView = (MapView) findViewById(R.id.mapview);
		contentPanel = findViewById(R.id.content_panel);
		contentPanel.setTag(Integer.valueOf(1));
		back = (TextView) findViewById(R.id.back);
		back.setVisibility(View.INVISIBLE);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation clickAnimation = new ClickAnimation(OnBoardActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Integer currentPageNo = (Integer) contentPanel.getTag();
						changePageTo(--currentPageNo);
						v.setClickable(true);
					}
				});
			}
		});
		
		changeIndicator(1);
		
		OnClickListener clickNext = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation clickAnimation = new ClickAnimation(OnBoardActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Integer currentPageNo = (Integer) contentPanel.getTag();
						changePageTo(++currentPageNo);
						v.setClickable(true);
					}
				});
			}
		};
		
		next = (TextView) findViewById(R.id.next);
		next.setOnClickListener(clickNext);
		
		page1Next = (TextView) findViewById(R.id.page1_next);
		page1Next.setOnClickListener(clickNext);
		page2Next = (TextView) findViewById(R.id.page2_next);
		page2Next.setOnClickListener(clickNext);
		page3Next = (TextView) findViewById(R.id.page3_next);
		page3Next.setOnClickListener(clickNext);
		skip = (TextView) findViewById(R.id.skip);
		skip.setOnClickListener(clickNext);
		
		finish = (TextView) findViewById(R.id.finish);
		finish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation clickAnimation = new ClickAnimation(OnBoardActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						v.setClickable(true);
						startLandingPage();
					}
				});
			}
		});
		
		homeSearchList = (ListView) findViewById(R.id.home_search_result_list);
        workSearchList = (ListView) findViewById(R.id.work_search_result_list);
        homeSearchBox = (EditText) findViewById(R.id.home_search_box);
        homeSearchBox.setHint(Html.fromHtml("<b>my home address</b>"));
        homeSearchBox.clearFocus();
        workSearchBox = (EditText) findViewById(R.id.work_search_box);
        workSearchBox.setHint(Html.fromHtml("<b>my work address</b>"));
        workSearchBox.clearFocus();
        homeSearchListAdapter = createAutoCompleteAdapter(homeSearchBox);
        workSearchListAdapter = createAutoCompleteAdapter(workSearchBox);
        homeSearchList.setAdapter(homeSearchListAdapter);
        workSearchList.setAdapter(workSearchListAdapter);
        
        homeSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                final String addrInput = v.getText().toString();
                boolean handled = StringUtils.isNotBlank(addrInput);
                if(handled){
                    searchHomeAddress(addrInput, true);
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    clearHomeSearchResult();
                }
                return handled;
            }
        });
        
        workSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                final String addrInput = v.getText().toString();
                boolean handled = StringUtils.isNotBlank(addrInput);
                if(handled){
                	searchWorkAddress(addrInput, true);
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    clearWorkSearchResult();
                }
                return handled;
            }
        });
        
        final View homeSearchBoxClear = findViewById(R.id.home_search_box_clear);
        DelayTextWatcher homeDelayTextWatcher = new DelayTextWatcher(homeSearchBox, new TextChangeListener(){
			@Override
			public void onTextChanged(CharSequence text) {
				homeSearchBoxClear.setVisibility(StringUtils.isBlank(text)?View.GONE:View.VISIBLE); 
                final String addrInput = text.toString();
                if(StringUtils.isNotBlank(addrInput)) {
                	AsyncTask<Void, Void, List<Address>> searchPoiTask = new AsyncTask<Void, Void, List<Address>>(){
                		
        				@Override
        				protected List<Address> doInBackground(Void... params) {
        					List<Address> addresses = new ArrayList<Address>();
        					try {
        						if(lastLocation != null) {
        							addresses = Geocoding.searchPoi(OnBoardActivity.this, addrInput, lastLocation.getLatitude(), lastLocation.getLongitude());
        						}
        						else {
        							addresses = Geocoding.searchPoi(OnBoardActivity.this, addrInput);
        						}
        					}
        					catch(Exception e) {
        						Log.e("LandingActivity2", "search error!");
        					}
        					return addresses;
        				}
        				
        				@Override
        				protected void onPostExecute(List<Address> addresses) {
        					searchAddresses.clear();
        					for(Address a:addresses){
        					    if(StringUtils.isNotBlank(a.getAddress())){
        					        searchAddresses.add(a);
        					    }
        					}
        					if(searchAddresses.isEmpty()) {
        						Address notFound = new Address();
        						notFound.setName(NO_AUTOCOMPLETE_RESULT);
        						notFound.setAddress("");
        						searchAddresses.add(notFound);
        					}
        					refreshHomeSearchAutoCompleteData();
        				}
                	};
                	Misc.parallelExecute(searchPoiTask); 
                }
                else {
                	clearHomeSearchResult();
                }
			}

			@Override
			public void onTextChanging() {
				showAutoComplete.set(true);
				if(searchAddresses.isEmpty()) {
					Address searching = new Address();
					searching.setName(SEARCHING);
					searching.setAddress("");
					searchAddresses.add(searching);
				}
				else {
					boolean hasResult = false;
					for(Address addr : searchAddresses) {
						if(StringUtils.isNotBlank(addr.getAddress())) {
							hasResult = true;
						}
					}
					if(!hasResult) {
						searchAddresses.clear();
						Address searching = new Address();
						searching.setName(SEARCHING);
						searching.setAddress("");
						searchAddresses.add(searching);
					}
				}
				refreshHomeSearchAutoCompleteData();
			}
		}, 500);
        
        homeSearchBox.addTextChangedListener(homeDelayTextWatcher);
        homeSearchBoxClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                homeSearchBox.setText("");
                clearHomeSearchResult();
                removeOverlay(true);
            }
        });
        
        homeSearchList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	Address selected = (Address)parent.getItemAtPosition(position);
            	if(StringUtils.isNotBlank(selected.getAddress())) {
            		homeSearchBox.setText(selected.getAddress());
            		saveFavorite(selected, true);
	                InputMethodManager imm = (InputMethodManager)getSystemService(
	                        Context.INPUT_METHOD_SERVICE);
	                imm.hideSoftInputFromWindow(homeSearchBox.getWindowToken(), 0);
	                showAutoComplete.set(false);
	                clearHomeSearchResult();
            	}
            }
        });
        
        workSearchList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address selected = (Address)parent.getItemAtPosition(position);
                if(StringUtils.isNotBlank(selected.getAddress())) {
                	workSearchBox.setText(selected.getAddress());
                	saveFavorite(selected, false);
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(workSearchBox.getWindowToken(), 0);
                    showAutoComplete.set(false);
                    clearWorkSearchResult();
                }
            }
        });
        
        final View workSearchBoxClear = findViewById(R.id.work_search_box_clear);
        DelayTextWatcher workDelayTextWatcher = new DelayTextWatcher(workSearchBox, new TextChangeListener(){
            @Override
            public void onTextChanged(CharSequence text) {
                workSearchBoxClear.setVisibility(StringUtils.isBlank(text)?View.GONE:View.VISIBLE);
                final String addrInput = text.toString();
                if(StringUtils.isNotBlank(addrInput)) {
                    AsyncTask<Void, Void, List<Address>> searchPoiTask = new AsyncTask<Void, Void, List<Address>>(){
                        @Override
                        protected List<Address> doInBackground(Void... params) {
                            List<Address> addresses = new ArrayList<Address>();
                            try {
                                if(lastLocation != null) {
                                    addresses = Geocoding.searchPoi(OnBoardActivity.this, addrInput, lastLocation.getLatitude(), lastLocation.getLongitude());
                                }
                                else {
                                    addresses = Geocoding.searchPoi(OnBoardActivity.this, addrInput);
                                }
                            }
                            catch(Exception e) {
                                Log.e("LandingActivity2", "search error!");
                            }
                            return addresses;
                        }
                        
                        @Override
                        protected void onPostExecute(List<Address> addresses) {
                            searchAddresses.clear();
                            for(Address a:addresses){
                                if(StringUtils.isNotBlank(a.getAddress())){
                                    searchAddresses.add(a);
                                }
                            }
                            if(searchAddresses.isEmpty()) {
                                Address notFound = new Address();
                                notFound.setName(NO_AUTOCOMPLETE_RESULT);
                                notFound.setAddress("");
                                searchAddresses.add(notFound);
                            }
                            refreshWorkSearchAutoCompleteData();
                        }
                    };
                    Misc.parallelExecute(searchPoiTask); 
                }
                else {
                    clearWorkSearchResult();
                }
            }
            
            @Override
			public void onTextChanging() {
            	showAutoComplete.set(true);
				if(searchAddresses.isEmpty()) {
					Address searching = new Address();
					searching.setName(SEARCHING);
					searching.setAddress("");
					searchAddresses.add(searching);
				}
				else {
					boolean hasResult = false;
					for(Address addr : searchAddresses) {
						if(StringUtils.isNotBlank(addr.getAddress())) {
							hasResult = true;
						}
					}
					if(!hasResult) {
						searchAddresses.clear();
						Address searching = new Address();
						searching.setName(SEARCHING);
						searching.setAddress("");
						searchAddresses.add(searching);
					}
				}
				refreshWorkSearchAutoCompleteData();
			}
        }, 500);
        
        workSearchBox.addTextChangedListener(workDelayTextWatcher);
        workSearchBoxClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                workSearchBox.setText("");
                clearWorkSearchResult();
                removeOverlay(false);
            }
        });
		
		locationListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location location) {
//                fake lat-lon
//                location.setLatitude(34.0291747); // LA
//                location.setLongitude(-118.2734106);
//                location.setLatitude(32.1559094); // Tucson
//                location.setLongitude(-110.883805);
                if (ValidationActivity.isBetterLocation(location, lastLocation)) {
                    lastLocation = location;
                }
            }
            @Override
            public void onProviderDisabled(String provider) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }
            @Override
            public void onStatusChanged(String provider, int status,
                    Bundle extras) {
            }
        };
		
		AssetManager assets = getAssets();
		mediumFont = Font.getMedium(assets);
	
		Font.setTypeface(mediumFont, skip, next, back, page1Next, page2Next, page3Next, finish);
		
		((SmarTrekApplication)getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	private static final Integer PAGE_SIZE = Integer.valueOf(4);
	
	private void changePageTo(Integer pageNo) {
		if(pageNo > PAGE_SIZE) {
			startLandingPage();
		}
		else {
			closeAllPage();
			switch(pageNo) {
			  	case 1 : 
			  		findViewById(R.id.on_board_page1).setVisibility(View.VISIBLE);
			  		back.setVisibility(View.INVISIBLE);
			  		skip.setVisibility(View.VISIBLE);
			  		break;
			  	case 2 :
			  		findViewById(R.id.on_board_page2).setVisibility(View.VISIBLE);
			  		back.setVisibility(View.VISIBLE);
			  		skip.setVisibility(View.VISIBLE);
			  		moveToOverlay(true);
			  		break;
			  	case 3 : 
			  		findViewById(R.id.on_board_page3).setVisibility(View.VISIBLE);
			  		back.setVisibility(View.VISIBLE);
			  		skip.setVisibility(View.VISIBLE);
			  		moveToOverlay(true);
			  		break;
			  	case 4 : 
			  		findViewById(R.id.on_board_page4).setVisibility(View.VISIBLE);
			  		skip.setVisibility(View.INVISIBLE);
			  		break;
			}
			changeIndicator(pageNo);
			contentPanel.setTag(pageNo);
		}
	}
	
	private void changeIndicator(Integer pageNo) {
		LinearLayout indicators = (LinearLayout)findViewById(R.id.indicators);
        for(int i=0; i<indicators.getChildCount(); i++){
        	View child = indicators.getChildAt(i);
            if(i == (pageNo - 1)){
                child.setEnabled(true);
            }else{
            	child.setEnabled(false);
            }
        }
	}
	
	private void moveToOverlay(boolean isHome) {
		Integer overlayId = isHome ? homeOverlayId : workOverlayId;
		if(overlayId != null && overlayId > 0) {
			POIOverlay overlay = null;
			List<Overlay> overlays = mapView.getOverlays();
			for(Overlay curOverlay : overlays) {
				if(curOverlay instanceof POIOverlay) {
					POIOverlay curPoiOverlay = (POIOverlay)curOverlay;
					if(overlayId.equals(curPoiOverlay.getAid())) {
						overlay = curPoiOverlay;
					}
				}
			}
			if(overlay != null) {
		        overlay.hideBalloon();
		        IMapController mc = mapView.getController();
		        mc.setZoom(SEARCH_ZOOM_LEVEL);
		        mc.setCenter(overlay.getGeoPoint());
		        mapView.postInvalidate();
			}
		}
	}
	
	private void startLandingPage() {
		Intent landingIntent = new Intent(OnBoardActivity.this, LandingActivity2.class);
		startActivity(landingIntent);
		finish();
	}
	
	private void closeAllPage() {
		findViewById(R.id.on_board_page1).setVisibility(View.INVISIBLE);
		findViewById(R.id.on_board_page2).setVisibility(View.INVISIBLE);
		findViewById(R.id.on_board_page3).setVisibility(View.INVISIBLE);
		findViewById(R.id.on_board_page4).setVisibility(View.INVISIBLE);
	}
	
	private void clearHomeSearchResult() {
    	searchAddresses.clear();
		homeSearchListAdapter.clear();
		refreshHomeSearchAutoCompleteData();
    }
    
    private void clearWorkSearchResult() {
        searchAddresses.clear();
        workSearchListAdapter.clear();
        refreshWorkSearchAutoCompleteData();
    }
	
    private void refreshHomeSearchAutoCompleteData(){
    	refreshAutoCompleteData(homeSearchList, homeSearchListAdapter, searchAddresses, homeSearchBox);
    }
    
    private void refreshWorkSearchAutoCompleteData(){
        refreshAutoCompleteData(workSearchList, workSearchListAdapter, searchAddresses, workSearchBox);
    }
	
	private static final String NO_AUTOCOMPLETE_RESULT = "No results found.";
    
    private static final String SEARCHING = "Searching...";
	
	private ArrayAdapter<Address> createAutoCompleteAdapter(final EditText searchBox) {
    	return new ArrayAdapter<Address>(OnBoardActivity.this, R.layout.dropdown_select, R.id.name) {
        	@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		View view = super.getView(position, convertView, parent);
                Address item = getItem(position);
                View namePanel = view.findViewById(R.id.name_panel);
                TextView name = (TextView) view.findViewById(R.id.name);
                name.setText(item.getName());
                TextView address = (TextView) view.findViewById(R.id.address);
                address.setText(item.getAddress());
                TextView distance = (TextView) view.findViewById(R.id.distance);
//                View iconPanel = view.findViewById(R.id.icon_panel);
                ImageView favIcon = (ImageView) view.findViewById(R.id.fav_icon);
                if(item.getDistance() >= 0) {
                	distance.setVisibility(View.VISIBLE);
                	distance.setText("> " + item.getDistance() + "mi");
                }
                else {
                	distance.setVisibility(View.GONE);
                }
                
                favIcon.setVisibility(View.GONE);
                
                name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                name.setCompoundDrawablePadding(0);
                int paddingSize = Dimension.dpToPx(5, getResources().getDisplayMetrics());
                namePanel.setPadding(paddingSize, paddingSize, 0, 0);
                address.setVisibility(View.VISIBLE);
                
                Font.setTypeface(mediumFont, name, distance);
                Font.setTypeface(mediumFont, address);
                
                namePanel.requestLayout();
                name.requestLayout();
                distance.requestLayout();
                address.requestLayout();
                favIcon.requestLayout();
//                view.setPadding(0, 0, 0, position == getCount() - 1 ? 
//                    Dimension.dpToPx(135, getResources().getDisplayMetrics()) : 0);
                return view;
        	}
        	
        	@Override
        	public Filter getFilter() {
        		Filter filter = new Filter() {
					@Override
					protected FilterResults performFiltering(CharSequence constraint) {
						List<Address> all = new ArrayList<Address>();
						List<Address> result = new ArrayList<Address>();
						for(int i = 0 ; i < getCount() ; i++) {
							all.add(getItem(i));
						}
						if(constraint != null) {
				            result.clear();
				            for (Address addr : all) {
				                if(addr.getName().toLowerCase().startsWith(constraint.toString().toLowerCase()) 
				               		|| addr.getAddress().toLowerCase().startsWith(constraint.toString().toLowerCase())){
				                    result.add(addr);
				                }
				            }
				            FilterResults filterResults = new FilterResults();
				            filterResults.values = result;
				            filterResults.count = result.size();
				            return filterResults;
				        } else {
				            return new FilterResults();
				        }
					}

					@Override
					protected void publishResults(CharSequence constraint,	FilterResults results) {
						ArrayList<Address> filteredList = (ArrayList<Address>) results.values;
			            if(results != null && results.count > 0) {
			                clear();
			                for (Address c : filteredList) {
			                    add(c);
			                }
			                notifyDataSetChanged();
			            }
					}
					
					@Override
					public CharSequence convertResultToString(Object selected) {
						String selectedAddr = ((Address)selected).getAddress();
						String selectedName = ((Address)selected).getName();
						if(NO_AUTOCOMPLETE_RESULT.equals(selectedName) && StringUtils.isBlank(selectedAddr)) {
							return searchBox.getText();
						}
						return selectedAddr;
					}
        			
        		};
        		return filter;
        	}
        };
    }
	
	private void searchHomeAddress(String addrStr, boolean zoomIn) {
    	searchPOIAddress(addrStr, zoomIn, lastLocation, true);
    }
    
    private void searchWorkAddress(String addrStr, boolean zoomIn) {
    	searchPOIAddress(addrStr, zoomIn, lastLocation, false);
    }
	
	private void searchPOIAddress(final String addrStr, final boolean zoomIn, final Location _location, final boolean isHome){
        AsyncTask<Void, Void, Address> task = new AsyncTask<Void, Void, Address>(){
            @Override
            protected Address doInBackground(Void... params) {
                Address addr = null;
                try {
                    List<Address> addrs;
                    if(_location == null) {
                        addrs = Geocoding.lookup(OnBoardActivity.this, addrStr);
                    }
                    else {
                        addrs = Geocoding.lookup(OnBoardActivity.this, addrStr, _location.getLatitude(), _location.getLongitude());
                    }
                    for (Address a : addrs) {
                        addr = a;
                        break;
                    }
                }
                catch (Exception e) {
                }
                return addr;
            }
            @Override
            protected void onPostExecute(Address addr) {
                if(addr != null){
                    saveFavorite(addr, isHome);
                }
                else {
                	final NotificationDialog2 dialog = new NotificationDialog2(OnBoardActivity.this, "No results");
                	dialog.setTitle("");
                	dialog.setPositiveButtonText("OK");
                	Misc.doQuietly(new Runnable() {
						@Override
						public void run() {
							dialog.show();
						}
                	});
                }
            }
        };
        Misc.parallelExecute(task);
    }
	
	private void removeOverlay(final boolean isHome) {
		final Integer overlayId = isHome ? homeOverlayId : workOverlayId;
		if(overlayId != null && overlayId > 0) {
			AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>(){
	            @Override
	            protected Integer doInBackground(Void... params) {
	                Integer id = null;
	                Request req = null;
	                User user = User.getCurrentUser(OnBoardActivity.this);
	                try {
	                    FavoriteAddressDeleteRequest request = new FavoriteAddressDeleteRequest(
	                            new AddressLinkRequest(user).execute(OnBoardActivity.this), user, overlayId);
	                    req = request;
	                    request.execute(OnBoardActivity.this);
	                }
	                catch (Exception e) {
	                    ehs.registerException(e, "[" + (req==null?"":req.getUrl()) + "]\n" + e.getMessage());
	                }
	                return id;
	            }
	            protected void onPostExecute(Integer id) {
	                if (ehs.hasExceptions()) {
	                    ehs.reportExceptions();
	                }
	                else {
	                	if(isHome) {
	                		homeOverlayId = null;
	                	}
	                	else {
	                		workOverlayId = null;
	                	}
	                	removeOldOverlay(isHome);
	                }
	            }
	       };
	       Misc.parallelExecute(task);
		}
	}
	
	private void refreshAutoCompleteData(ListView searchList, ArrayAdapter<Address> adapter, List<Address> searchedAddresses, EditText _searchBox) {
    	adapter.clear();
    	if(showAutoComplete.get() && _searchBox.isFocused()) {
	        for(Address a : searchedAddresses) {
	        	adapter.add(a);
	        }
	        if(!adapter.isEmpty()) {
	        	searchList.setVisibility(View.VISIBLE);
	        }else{
	        	searchList.setVisibility(View.GONE);
	        }
    	}
    	else {
    		searchList.setVisibility(View.GONE);
    	}
    }
	
	private static final int SEARCH_ZOOM_LEVEL = 16;
	
	private void updateOrDropAddress(Address addr, boolean isHome) {
		removeOldOverlay(isHome);
		Integer overlayId = isHome ? homeOverlayId : workOverlayId;
    	GeoPoint gp = addr.getGeoPoint();
        BalloonModel model = new BalloonModel();
        model.lat = addr.getLatitude();
        model.lon = addr.getLongitude();
        model.address = addr.getAddress();
        model.label = addr.getIconName();
        model.geopoint = gp;
        final PoiOverlayInfo poiInfo = PoiOverlayInfo.fromBalloonModel(model);
        poiInfo.marker = isHome ? R.drawable.home : R.drawable.work;
        poiInfo.markerWithShadow = isHome ? R.drawable.home_with_shadow : R.drawable.work_with_shadow;
        if(overlayId != null && overlayId > 0) {
        	poiInfo.id = overlayId;
        }
        
        final POIOverlay marker = new POIOverlay(mapView, Font.getBold(getAssets()), poiInfo, 
        		HotspotPlace.CENTER , null);
        
        marker.setCallback(new OverlayCallback() {
            @Override
            public boolean onTap(int index) {
                return false;
            }
            @Override
            public boolean onLongPress(int index, OverlayItem item) {
                return false;
            }
            
            @Override
            public boolean onClose() {
                return false;
            }
            @Override
            public void onChange() {
            }
            @Override
            public boolean onBalloonTap(int index, OverlayItem item) {
            	return false;
            }
        });
        
        List<Overlay> overlays = mapView.getOverlays();
        overlays.add(marker);
        marker.showOverlay();
        marker.hideBalloon();
        IMapController mc = mapView.getController();
        mc.setZoom(SEARCH_ZOOM_LEVEL);
        mc.setCenter(gp);
        mapView.postInvalidate();
        
        if(isHome) {
        	findViewById(R.id.home_mask).setVisibility(View.GONE);
        	page2Next.setVisibility(View.VISIBLE);
        }
        else {
        	findViewById(R.id.work_mask).setVisibility(View.GONE);
        	page3Next.setVisibility(View.VISIBLE);
        }
    }
	
	private void removeOldOverlay(boolean isHome) {
		List<Overlay> overlays = mapView.getOverlays();
		for(Overlay overlay : overlays) {
			if(overlay instanceof POIOverlay && 
					(isHome?FavoriteIcon.home.name() : FavoriteIcon.work.name()).equals(((POIOverlay)overlay).getPoiOverlayInfo().label)) {
				overlays.remove(overlay);
                mapView.postInvalidate();
			}
 		}
	}
		
	private void saveFavorite(Address _address, final boolean isHome) {
		final Integer overlayId = isHome ? homeOverlayId : workOverlayId; 
		final String iconName = isHome ? FavoriteIcon.home.name() : FavoriteIcon.work.name();
		final String favName = isHome ? FavoriteIcon.home.name() : FavoriteIcon.work.name();
		_address.setIconName(iconName);
		final Address address = _address;
		AsyncTask<Void, Void, Integer> saveTask = new AsyncTask<Void, Void, Integer>() {
			@Override
            protected Integer doInBackground(Void... params) {
                Integer id = 0;
                Request req = null;
                User user = User.getCurrentUser(OnBoardActivity.this);
                try {
                	if(overlayId == null) {
                        FavoriteAddressAddRequest request = new FavoriteAddressAddRequest(
                            user, StringUtils.capitalize(favName), address.getAddress(), iconName, address.getLatitude(), address.getLongitude());
                        req = request;
                        id = request.execute(OnBoardActivity.this);
                	}
                	else {
                		FavoriteAddressUpdateRequest request = new FavoriteAddressUpdateRequest(
                                new AddressLinkRequest(user).execute(OnBoardActivity.this),  overlayId, user, StringUtils.capitalize(favName), 
                                address.getAddress(), iconName, address.getLatitude(), address.getLongitude());
                        req = request;
                        request.execute(OnBoardActivity.this);
                	}
                }
                catch (Exception e) {
                    ehs.registerException(e, "[" + (req==null?"":req.getUrl()) + "]\n" + e.getMessage());
                }
                return id;
            }
			
            protected void onPostExecute(Integer id) {
                if (ehs.hasExceptions()) {
                    ehs.reportExceptions();
                }
                else {
                	if(isHome) {
                		homeOverlayId = id;
                	}
                	else {
                		workOverlayId = id;
                	}
                    updateOrDropAddress(address, isHome);
                }
            }
		};
		Misc.parallelExecute(saveTask);
	}
	
	private void prepareGPS(){
        closeGPS();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                10000, 5, locationListener);
        }else{
            SystemService.alertNoGPS(this, true);
        }
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }
    
    private void closeGPS(){
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
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
    	prepareGPS();
    }
    
    @Override
    public void onPause() {
    	Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
    	super.onPause();
    	closeGPS();
    }

}
