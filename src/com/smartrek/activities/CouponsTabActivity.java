package com.smartrek.activities;

import java.io.IOException;
import java.net.ConnectException;
import java.text.ParseException;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.smartrek.adapters.CouponAdapter;
import com.smartrek.mappers.CouponMapper;
import com.smartrek.models.Coupon;
import com.smartrek.models.User;
import com.smartrek.ui.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;

/**
 * Holds three tabs (all, received, sent coupons)
 *
 */
public final class CouponsTabActivity extends Activity {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
	
	public static final String TAB1 = "Tab1";
	public static final String TAB2 = "Tab2";
	public static final String TAB3 = "Tab3";
	
	public static final int TAB_ID1 = 1;
	public static final int TAB_ID2 = 2;
	public static final int TAB_ID3 = 3;
	
	private List<Coupon> couponsAll;
	private List<Coupon> couponsReceived;
	private List<Coupon> couponsSent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.coupons_tab);
		TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
		tabHost.setup();
		
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				if(tabId.equals(TAB1)) {
					onTab1();
				}
				else if(tabId.equals(TAB2)) {
					onTab2();
				}
				else if(tabId.equals(TAB3)) {
					onTab3();
				}
				else {
					Log.d(this.getClass().toString(), "Unknown tabId = " + tabId);
				}
			}
		});

		TabSpec spec1 = tabHost.newTabSpec(TAB1);
		spec1.setContent(R.id.tab1);
		spec1.setIndicator("My Coupons");

		TabSpec spec2 = tabHost.newTabSpec(TAB2);
		spec2.setIndicator("Received");
		spec2.setContent(R.id.tab2);

		TabSpec spec3 = tabHost.newTabSpec(TAB3);
		spec3.setIndicator("Sent");
		spec3.setContent(R.id.tab3);

		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		tabHost.addTab(spec3);
		
		{
			ListView listView = (ListView) findViewById(R.id.listViewCouponsAll);
			listView.setOnItemClickListener(new OnItemClickListener() {
		        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		    		Intent intent = new Intent(CouponsTabActivity.this, CouponDetailsActivity.class);
	
		    		Bundle extras = new Bundle();
		    		extras.putParcelable("coupon", couponsAll.get(position));
		    		extras.putBoolean("ownership", true);
		    		intent.putExtras(extras);
		    		startActivityForResult(intent, TAB_ID1);
		            
		        }
		    });
		}
		{
			ListView listView = (ListView) findViewById(R.id.listViewCouponsReceived);
			listView.setOnItemClickListener(new OnItemClickListener() {
		        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		    		Intent intent = new Intent(CouponsTabActivity.this, CouponDetailsActivity.class);
	
		    		Bundle extras = new Bundle();
		    		extras.putParcelable("coupon", couponsReceived.get(position));
		    		extras.putBoolean("received", true);
		    		intent.putExtras(extras);
		    		startActivityForResult(intent, TAB_ID2);
		            
		        }
		    });
		}
		{
			ListView listView = (ListView) findViewById(R.id.listViewCouponsSent);
			listView.setOnItemClickListener(new OnItemClickListener() {
		        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		    		Intent intent = new Intent(CouponsTabActivity.this, CouponDetailsActivity.class);
	
		    		Bundle extras = new Bundle();
		    		extras.putParcelable("coupon", couponsSent.get(position));
		    		extras.putBoolean("sent", true);
		    		intent.putExtras(extras);
		    		startActivityForResult(intent, TAB_ID3);
		            
		        }
		    });
		}
	}
	
	private void onTab1() {
        User user = User.getCurrentUser(this);
        
		ListView listView = (ListView) findViewById(R.id.listViewCouponsAll);
		new CouponsTask(listView).execute(user.getId(), CouponMapper.Flag.All);
	}
	
	private void onTab2() {
		User user = User.getCurrentUser(this);
        
		ListView listView = (ListView) findViewById(R.id.listViewCouponsReceived);
		new CouponsTask(listView).execute(user.getId(), CouponMapper.Flag.Received);
	}
	
	private void onTab3() {
		User user = User.getCurrentUser(this);
        
		ListView listView = (ListView) findViewById(R.id.listViewCouponsSent);
		new CouponsTask(listView).execute(user.getId(), CouponMapper.Flag.Sent);
	}
	
    private final class CouponsTask extends AsyncTask<Object, Void, List<Coupon>> {
    	
    	private ListView listView;
    	
    	public CouponsTask(ListView listView) {
    		super();
    		
    		this.listView = listView;
    	}
    	
    	@Override
        protected List<Coupon> doInBackground(Object... args) {
    		
    		// FIXME: Potential array out of boundary exception
    		int uid = (Integer) args[0];
    		CouponMapper.Flag flag = (CouponMapper.Flag) args[1]; 
        	
            CouponMapper mapper = new CouponMapper();
            List<Coupon> coupons = null;
			try {
				coupons = mapper.getCoupons(uid, flag);
				
	            if(CouponMapper.Flag.All.equals(flag)) {
	            	couponsAll = coupons;
	            }
	            else if(CouponMapper.Flag.Received.equals(flag)) {
	            	couponsReceived = coupons;
	            }
	            else if(CouponMapper.Flag.Sent.equals(flag)) {
	            	couponsSent = coupons;
	            }
			}
			catch (ConnectException e) {
				ehs.registerException(e);
			}
			catch (IOException e) {
			    ehs.registerException(e);
			}
			catch (JSONException e) {
			    ehs.registerException(e);
			}
			catch (ParseException e) {
			    ehs.registerException(e);
			}
        	
            return coupons;
        }       
        
    	@Override
		protected void onPostExecute(List<Coupon> coupons) {
		    if (ehs.hasExceptions()) {
		        ehs.reportExceptions();
		    }
		    else {
				listView.setAdapter(new CouponAdapter(CouponsTabActivity.this, coupons));
			}
		}
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d("CouponsTabActivity", String.format("requestCode = %d, resultCode = %d", requestCode, resultCode));
    	
    	// TODO: Conditionally refresh the list view for better efficiency
    	if (requestCode == TAB_ID1) {
    		onTab1();
    	}
    	else if (requestCode == TAB_ID2) {
    		onTab2();
    	}
    	else if (requestCode == TAB_ID3) {
    		onTab3();
    	}
	}
}
