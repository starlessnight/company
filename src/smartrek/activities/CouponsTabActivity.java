package smartrek.activities;

import java.util.List;

import smartrek.adapters.CouponAdapter;
import smartrek.mappers.CouponMapper;
import smartrek.models.Coupon;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public final class CouponsTabActivity extends Activity {
	
	public static final String TAB1 = "Tab1";
	public static final String TAB2 = "Tab2";
	public static final String TAB3 = "Tab3";
	
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

	}
	
	private void onTab1() {
		ListView listView = (ListView) findViewById(R.id.listViewCouponsAll);
		new CouponsTask(couponsAll, listView).execute(10, CouponMapper.Flag.All);
	}
	
	private void onTab2() {
		ListView listView = (ListView) findViewById(R.id.listViewCouponsReceived);
		new CouponsTask(couponsReceived, listView).execute(10, CouponMapper.Flag.Received);
	}
	
	private void onTab3() {
		ListView listView = (ListView) findViewById(R.id.listViewCouponsSent);
		new CouponsTask(couponsSent, listView).execute(10, CouponMapper.Flag.Sent);
	}
	
    private final class CouponsTask extends AsyncTask<Object, Void, List<Coupon>> {
    	
    	private List<Coupon> coupons;
    	private ListView listView;
    	
    	public CouponsTask(List<Coupon> coupons, ListView listView) {
    		super();
    		
    		this.coupons = coupons;
    		this.listView = listView;
    	}
    	
    	@Override
        protected List<Coupon> doInBackground(Object... args) {
    		
    		// FIXME: Potential array out of boundary exception
    		int uid = (Integer) args[0];
    		CouponMapper.Flag flag = (CouponMapper.Flag) args[1]; 
        	
            CouponMapper mapper = new CouponMapper();
            coupons = mapper.getCoupons(uid, flag);
            //mapper.doCouponBitmapDownloads(coupons, CouponsTabActivity.this);
        	
            return coupons;
        }       
        
		protected void onPostExecute(List<Coupon> coupons) {
		    listView.setAdapter(new CouponAdapter(CouponsTabActivity.this, coupons));
		}
    }
}
